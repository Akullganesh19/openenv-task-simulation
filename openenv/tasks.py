from typing import Dict, Any, Callable
from .models import TaskType, TaskState

def grade_easy_task(solution: str) -> float:
    expected = "def hello_world():\n    return 'Hello, World!'"
    if solution.strip() == expected.strip():
        return 1.0
    return 0.5

def grade_medium_task(solution: str) -> float:
    # Enhancement: More robust check for fibonacci
    if "def fibonacci(n):" in solution and "return" in solution:
        if "fibonacci(n-1)" in solution:
            return 1.0
        return 0.7
    return 0.3

def grade_hard_task(solution: str) -> float:
    # Improved heuristic for Quicksort
    keywords = ["def quicksort", "pivot", "left", "right", "return"]
    found = sum(1 for k in keywords if k in solution)
    score = found / len(keywords)
    
    # Check for core recursion
    if "quicksort(left)" in solution or "quicksort(right)" in solution:
        score = max(score, 0.9)
    
    return min(score, 1.0)

class TaskManager:
    def __init__(self):
        self._tasks = {
            "task_1": {
                "type": TaskType.EASY,
                "description": "Complete a simple coding exercise: Implement a hello_world() function that returns 'Hello, World!'.",
                "grader": grade_easy_task
            },
            "task_2": {
                "type": TaskType.MEDIUM,
                "description": "Debug and implement a recursive fibonacci(n) function.",
                "grader": grade_medium_task
            },
            "task_3": {
                "type": TaskType.HARD,
                "description": "Optimize or implement the Quicksort algorithm.",
                "grader": grade_hard_task
            }
        }

    def get_task(self, task_id: str) -> Optional[Dict[str, Any]]:
        return self._tasks.get(task_id)

    def list_tasks(self) -> List[str]:
        return list(self._tasks.keys())

    def grade_solution(self, task_id: str, solution: str) -> float:
        task = self.get_task(task_id)
        if task:
            return task["grader"](solution)
        return 0.0
