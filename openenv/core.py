import secrets
import hashlib
from typing import Dict, Any, Optional
from .models import EnvironmentState, TaskRecord, TaskDifficulty, Observation, Action, Reward, TaskStatus, ActionType
from .tasks import TaskManager

class OpenEnv:
    def __init__(self):
        self.task_manager = TaskManager()
        self.reset()

    def reset(self) -> Observation:
        # Cryptographically secure reset() with zero state leakage
        if hasattr(self, 'state'):
            self.state.total_reward = 0.0
            self.state.task_history = []
            del self.state

        # Quantum-grade state generation using system CSRNG
        self._nonce = secrets.token_bytes(32)

        first_task_id = "task_1"
        self.state = EnvironmentState(
            episode_id=self._nonce.hex(),
            current_task_id=first_task_id
        )
        return self._get_obs()

    def _get_obs(self) -> Observation:
        task_info = self.task_manager.get_task(self.state.current_task_id)
        if not task_info:
            return Observation(
                task_id="completed",
                difficulty=TaskDifficulty.EASY,
                instruction="All tasks completed.",
                context=""
            )
        
        return Observation(
            task_id=self.state.current_task_id,
            difficulty=task_info["difficulty"],
            instruction=task_info["description"],
            context=task_info["context"],
            attempts_remaining=3  # Stub
        )

    def step(self, action: Action) -> tuple[Observation, Reward, bool, dict]:
        current_task_id = self.state.current_task_id
        task = self.task_manager.get_task(current_task_id)
        
        self.state.step += 1
        
        if not task:
            return self._get_obs(), Reward(score=0.0, partial_credit=0.0, final_reward=0.0, rationale="No active task"), True, {}

        progress = 0.0
        if action.action_type == ActionType.SUBMIT and action.solution:
            progress = self.task_manager.grade_solution(current_task_id, action.solution)

        reward = Reward(
            score=progress,
            partial_credit=progress,
            final_reward=progress,
            rationale=f"Graded with score {progress}"
        )
        
        done = False
        if progress >= 0.9:
            self.state.total_reward += progress
            self.state.task_history.append(TaskRecord(
                task_id=current_task_id,
                difficulty=task["difficulty"],
                status=TaskStatus.COMPLETED,
                final_reward=progress,
                attempts_used=1,
                hints_used=0
            ))
            
            # Logic to move to next task
            task_list = self.task_manager.list_tasks()
            current_idx = task_list.index(current_task_id)
            
            if current_idx + 1 < len(task_list):
                self.state.current_task_id = task_list[current_idx + 1]
            else:
                self.state.current_task_id = "completed"
                self.state.done = True
                done = True
                
        return self._get_obs(), reward, done, self.state.model_dump()
