import uuid
import secrets
from typing import Dict, Any, Optional, Tuple
from models import EnvironmentState, TaskRecord, TaskDifficulty, Observation, Action, Reward, TaskStatus, ActionType
from tasks import TaskManager

class CodingEnvironment:
    """Professional OpenEnv environment for coding tasks. 
    Following the 3-component pattern for session-isolated, persistent RL training.
    """
    
    def __init__(self):
        self.task_manager = TaskManager()
        self._state = None
        self._nonce = None

    def reset(self, episode_id: Optional[str] = None) -> Observation:
        """Reset the environment to a fresh state with a new episode ID.
        Uses cryptographically secure nonces for state isolation.
        """
        self._nonce = secrets.token_hex(16)
        episode_id = episode_id or self._nonce
        
        first_task_id = "task_1"
        self._state = EnvironmentState(
            episode_id=episode_id,
            current_task_id=first_task_id,
            step=0,
            total_reward=0.0,
            task_history=[],
            done=False
        )
        return self._get_obs()

    def _get_obs(self) -> Observation:
        """Internal method to generate the current observation based on state."""
        if not self._state:
            raise RuntimeError("Environment not reset. Call reset() first.")
            
        task_info = self.task_manager.get_task(self._state.current_task_id)
        if not task_info:
            return Observation(
                task_id="complete",
                difficulty=TaskDifficulty.EASY,
                instruction="Session terminated. All tasks finalized.",
                context="",
                attempts_remaining=0
            )
        
        return Observation(
            task_id=self._state.current_task_id,
            difficulty=task_info["difficulty"],
            instruction=task_info["description"],
            context=task_info["context"],
            attempts_remaining=3 # Hardcoded for now, can be state-tracked
        )

    def step(self, action: Action) -> Tuple[Observation, Reward, bool, Dict[str, Any]]:
        """Execute a single step in the coding task environment.
        Returns (Observation, Reward, Done, Info).
        """
        if self._state.done:
            return self._get_obs(), Reward(score=0.0, partial_credit=0.0, final_reward=0.0, rationale="Episode already complete."), True, self._state.model_dump()

        current_task_id = self._state.current_task_id
        task = self.task_manager.get_task(current_task_id)
        
        self._state.step += 1
        
        if not task:
            return self._get_obs(), Reward(score=0.0, partial_credit=0.0, final_reward=0.0, rationale="Invalid task ID."), True, self._state.model_dump()

        progress = 0.0
        if action.action_type == ActionType.SUBMIT and action.solution:
            progress = self.task_manager.grade_solution(current_task_id, action.solution)
        elif action.action_type == ActionType.HINT:
            # Logic for hints could be added here
            progress = -0.1 # Placeholder penalty for hints
        
        reward = Reward(
            score=max(0.0, progress),
            partial_credit=max(0.0, progress),
            final_reward=max(0.0, progress),
            rationale=f"Action {action.action_type.value} processed. Progress: {progress}"
        )
        
        done_task = False
        if action.action_type == ActionType.SKIP:
            self._state.task_history.append(TaskRecord(
                task_id=current_task_id,
                difficulty=task["difficulty"],
                status=TaskStatus.FAILED,
                final_reward=0.0,
                attempts_used=0,
                hints_used=0
            ))
            done_task = True
        elif progress >= 0.8: # Threshold for completion
            self._state.total_reward += progress
            self._state.task_history.append(TaskRecord(
                task_id=current_task_id,
                difficulty=task["difficulty"],
                status=TaskStatus.COMPLETED,
                final_reward=progress,
                attempts_used=1,
                hints_used=0
            ))
            done_task = True
            
        if done_task:
            # Transition logic
            task_list = self.task_manager.list_tasks()
            try:
                current_idx = task_list.index(current_task_id)
                if current_idx + 1 < len(task_list):
                    self._state.current_task_id = task_list[current_idx + 1]
                else:
                    self._state.current_task_id = "complete"
                    self._state.done = True
            except ValueError:
                self._state.done = True
                
        return self._get_obs(), reward, self._state.done, self._state.model_dump()

    @property
    def state(self) -> EnvironmentState:
        return self._state
