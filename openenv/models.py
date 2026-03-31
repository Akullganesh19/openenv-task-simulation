from enum import Enum
from typing import Dict, List, Optional
from pydantic import BaseModel, Field

class TaskType(Enum):
    EASY = "easy"
    MEDIUM = "medium"
    HARD = "hard"

class TaskState(BaseModel):
    task_id: str
    task_type: TaskType
    progress: float = Field(default=0.0, ge=0.0, le=1.0)
    completed: bool = False
    attempts: int = 0
    hints_used: int = 0

class EnvironmentState(BaseModel):
    current_task: TaskState
    total_score: float = 0.0
    task_history: List[TaskState] = []
    time_elapsed: int = 0
