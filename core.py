import secrets
import hashlib
import time
import logging
from typing import Dict, Any, Optional, Tuple
from .models import EnvironmentState, TaskRecord, TaskDifficulty, Observation, Action, Reward, TaskStatus, ActionType
from .tasks import TaskManager
from metrics import metrics
from enhanced_graders import enhanced_grader

logger = logging.getLogger(__name__)

class OpenEnv:
    def __init__(self):
        self.task_manager = TaskManager()
        self.start_time = time.time()
        self.reset()

    def reset(self) -> Observation:
        """Reset the environment with cryptographic security"""
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
        
        # Record metrics
        metrics.record_session_start()
        logger.info(f"Environment reset. Episode: {self.state.episode_id}")
        
        return self._get_obs()

    def _get_obs(self) -> Observation:
        """Get current observation"""
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
            attempts_remaining=self.state.attempts_remaining
        )

    def step(self, action: Action) -> Tuple[Observation, Reward, bool, dict]:
        """Execute a step in the environment"""
        current_task_id = self.state.current_task_id
        task = self.task_manager.get_task(current_task_id)
        
        self.state.step += 1
        self.state.attempts_remaining = max(0, self.state.attempts_remaining - 1)
        
        if not task:
            return self._get_obs(), Reward(score=0.0, partial_credit=0.0, final_reward=0.0, rationale="No active task"), True, {}

        progress = 0.0
        feedback = ""
        
        if action.action_type == ActionType.SUBMIT and action.solution:
            # Grade the solution
            start_time = time.time()
            progress = self.task_manager.grade_solution(current_task_id, action.solution)
            grading_time = time.time() - start_time
            
            # Record metrics
            metrics.record_grading(current_task_id, grading_time)
            
            # Get enhanced feedback if available
            try:
                scores = enhanced_grader.grade_solution(action.solution, current_task_id)
                feedback = enhanced_grader.get_feedback(scores, current_task_id)
            except Exception as e:
                logger.warning(f"Enhanced grading failed: {str(e)}")
                feedback = f"Graded with score {progress}"

        reward = Reward(
            score=progress,
            partial_credit=progress,
            final_reward=progress,
            rationale=feedback or f"Graded with score {progress}"
        )
        
        done = False
        task_completed = progress >= 0.9
        
        if task_completed or self.state.attempts_remaining <= 0:
            # Record task completion
            self.state.total_reward += progress
            
            task_record = TaskRecord(
                task_id=current_task_id,
                difficulty=task["difficulty"],
                status=TaskStatus.COMPLETED if task_completed else TaskStatus.FAILED,
                final_reward=progress,
                attempts_used=3 - self.state.attempts_remaining,
                hints_used=0
            )
            self.state.task_history.append(task_record)
            
            # Record metrics
            metrics.record_task_completion(current_task_id, task["difficulty"].value, progress)
            
            # Move to next task
            task_list = self.task_manager.list_tasks()
            current_idx = task_list.index(current_task_id)
            
            if current_idx + 1 < len(task_list):
                self.state.current_task_id = task_list[current_idx + 1]
                self.state.attempts_remaining = 3  # Reset attempts for new task
                logger.info(f"Moving to next task: {self.state.current_task_id}")
            else:
                self.state.current_task_id = "completed"
                self.state.done = True
                done = True
                
                # Record session end
                session_duration = time.time() - self.start_time
                metrics.record_session_end("all_tasks", "mixed", session_duration)
                logger.info(f"All tasks completed. Total reward: {self.state.total_reward}")
        else:
            # Record partial attempt
            self.state.task_history.append(TaskRecord(
                task_id=current_task_id,
                difficulty=task["difficulty"],
                status=TaskStatus.IN_PROGRESS,
                final_reward=progress,
                attempts_used=3 - self.state.attempts_remaining,
                hints_used=0
            ))
                
        return self._get_obs(), reward, done, self.state.model_dump()
    
    def get_state_info(self) -> Dict[str, Any]:
        """Get detailed state information"""
        return {
            "episode_id": self.state.episode_id,
            "current_task_id": self.state.current_task_id,
            "step": self.state.step,
            "total_reward": self.state.total_reward,
            "attempts_remaining": self.state.attempts_remaining,
            "task_history": [record.model_dump() for record in self.state.task_history],
            "done": self.state.done
        }
    
    def get_available_tasks(self) -> list:
        """Get list of available tasks"""
        return self.task_manager.list_tasks()
    
    def get_task_info(self, task_id: str) -> Optional[Dict[str, Any]]:
        """Get information about a specific task"""
        return self.task_manager.get_task(task_id)
