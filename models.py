"""
Core Pydantic models for the OpenEnv interface.

Observation  — what the agent sees each step
Action       — what the agent can do
Reward       — structured reward signal with rationale
"""

from enum import Enum
from typing import Any, Dict, List, Optional
from pydantic import BaseModel, Field


class TaskDifficulty(str, Enum):
    EASY   = "easy"
    MEDIUM = "medium"
    HARD   = "hard"


class TaskStatus(str, Enum):
    PENDING   = "pending"
    ACTIVE    = "active"
    COMPLETED = "completed"
    FAILED    = "failed"


class Observation(BaseModel):
    """What the agent observes at every timestep."""
    task_id: str = Field(..., description="Unique identifier for the current task")
    difficulty: TaskDifficulty
    instruction: str = Field(..., description="Natural-language task description")
    context: str = Field(..., description="Starter code, examples, or data provided to the agent")
    test_cases: List[Dict[str, Any]] = Field(
        default_factory=list,
        description="Visible test cases (some may be hidden). Each has 'input' and 'expected_output'."
    )
    constraints: Dict[str, Any] = Field(
        default_factory=dict,
        description="Task constraints such as time complexity, forbidden constructs, etc."
    )
    hints_available: int = Field(default=0, ge=0, description="Number of hints remaining")
    attempts_remaining: int = Field(default=3, ge=0, description="Attempts left before task fails")
    previous_feedback: Optional[str] = Field(
        default=None,
        description="Feedback from the previous submission attempt"
    )


class ActionType(str, Enum):
    SUBMIT   = "submit"    # Submit a solution for grading
    HINT     = "hint"      # Request a hint (costs score)
    SKIP     = "skip"      # Abandon this task (score = 0)
    RESET    = "reset"     # Reset the current task (costs 1 attempt)


class Action(BaseModel):
    """What the agent can do at each step."""
    action_type: ActionType = Field(..., description="The type of action")
    solution: Optional[str] = Field(
        default=None,
        description="Python source code string (required when action_type=SUBMIT)"
    )
    explanation: Optional[str] = Field(
        default=None,
        description="Agent's optional explanation of its solution"
    )


class Reward(BaseModel):
    """Structured reward signal returned after each action."""
    score: float = Field(..., ge=0.0, le=1.0, description="Normalised task score [0, 1]")
    partial_credit: float = Field(..., ge=0.0, le=1.0, description="Fraction of test cases passed")
    time_penalty: float = Field(default=0.0, ge=0.0, le=1.0, description="Penalty for excess attempts")
    hint_penalty: float = Field(default=0.0, ge=0.0, le=1.0, description="Penalty for hints used")
    efficiency_bonus: float = Field(default=0.0, ge=0.0, le=1.0, description="Bonus for optimal solution")
    final_reward: float = Field(..., ge=0.0, le=1.0, description="Combined shaped reward signal")
    rationale: str = Field(..., description="Human-readable explanation of the reward")
    tests_passed: int = Field(default=0, ge=0)
    tests_total: int = Field(default=0, ge=0)
    syntax_valid: bool = Field(default=False)
    execution_error: Optional[str] = Field(default=None)


class TaskRecord(BaseModel):
    """Historical record of a completed task."""
    task_id: str
    difficulty: TaskDifficulty
    status: TaskStatus
    final_reward: float = Field(ge=0.0, le=1.0)
    attempts_used: int = Field(ge=0)
    hints_used: int = Field(ge=0)


class EnvironmentState(BaseModel):
    """Full environment state (internal use / debugging)."""
    episode_id: str
    current_task_id: str
    step: int = 0
    total_reward: float = 0.0
    task_history: List[TaskRecord] = Field(default_factory=list)
    done: bool = False
    info: Dict[str, Any] = Field(default_factory=dict)
