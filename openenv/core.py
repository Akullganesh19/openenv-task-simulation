from typing import Dict, Any, Optional
from .models import EnvironmentState, TaskState, TaskType
from .tasks import TaskManager

class OpenEnv:
    def __init__(self):
        self.task_manager = TaskManager()
        self.reset()

    def reset(self) -> EnvironmentState:
        first_task_id = "task_1"
        task_info = self.task_manager.get_task(first_task_id)
        self.state = EnvironmentState(
            current_task=TaskState(
                task_id=first_task_id,
                task_type=task_info["type"]
            )
        )
        return self.state

    def step(self, action: Dict[str, Any]) -> EnvironmentState:
        current_task_id = self.state.current_task.task_id
        task = self.task_manager.get_task(current_task_id)
        
        if not task:
            return self.state

        self.state.current_task.attempts += 1

        if action.get("submit"):
            solution = action.get("solution", "")
            progress = self.task_manager.grade_solution(current_task_id, solution)
            self.state.current_task.progress = progress
            self.state.current_task.completed = progress >= 0.9
            
            if self.state.current_task.completed:
                self.state.total_score += progress
                self.state.task_history.append(self.state.current_task)
                
                # Logic to move to next task
                task_list = self.task_manager.list_tasks()
                current_idx = task_list.index(current_task_id)
                
                if current_idx + 1 < len(task_list):
                    next_task_id = task_list[current_idx + 1]
                    next_task_info = self.task_manager.get_task(next_task_id)
                    self.state.current_task = TaskState(
                        task_id=next_task_id,
                        task_type=next_task_info["type"]
                    )
                else:
                    self.state.current_task = TaskState(
                        task_id="completed",
                        task_type=self.state.current_task.task_type,
                        completed=True,
                        progress=1.0
                    )

        self.state.time_elapsed += 1
        return self.state
