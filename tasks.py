from typing import Dict, Any, Callable, List, Optional
import re
from models import TaskDifficulty

def grade_email_triage(solution: str) -> float:
    urgency_keywords = ["urgent", "ASAP", "down", "critical", "refund"]
    score = 0.0
    # Check if the logic loops through emails and checks urgency
    if "for" in solution and ("if" in solution or "filter" in solution):
        score += 0.4
    if any(kw in solution.lower() for kw in urgency_keywords) or "urgent" in solution.lower():
        score += 0.4
    if "return" in solution:
        score += 0.2
    return max(0.0, min(1.0, score))

def grade_data_cleaning(solution: str) -> float:
    # A real-world task: dropping duplicates and handling NAs in pandas or vanilla python
    score = 0.0
    if "drop_duplicates" in solution or "set(" in solution:
        score += 0.4
    if "fillna" in solution or "None" in solution:
        score += 0.4
    if "return" in solution:
        score += 0.2
    return max(0.0, min(1.0, score))

def grade_code_review(solution: str) -> float:
    # Real-world: Detecting and patching SQL injection via parameterized queries
    score = 0.0
    if "execute(" in solution and "?" in solution or "%s" in solution:
        score += 0.5
    if "f-string" not in solution.lower() and "format(" not in solution:
        score += 0.5
    return max(0.0, min(1.0, score))

class TaskManager:
    def __init__(self):
        self._tasks = {
            "task_1": {
                "difficulty": TaskDifficulty.EASY,
                "description": "Email Triage Task: Write a function `triage_emails(emails: list[dict])` that filters a list of email dictionaries and returns only those where the subject or body contains 'urgent', 'ASAP', or 'critical'.",
                "grader": grade_email_triage,
                "context": "def triage_emails(emails):\n    pass",
            },
            "task_2": {
                "difficulty": TaskDifficulty.MEDIUM,
                "description": "Data Pipeline Task: Write a function `clean_dataset(data: pd.DataFrame)` that removes duplicate rows and imputes missing numeric values with the column mean.",
                "grader": grade_data_cleaning,
                "context": "import pandas as pd\n\ndef clean_dataset(data):\n    pass",
            },
            "task_3": {
                "difficulty": TaskDifficulty.HARD,
                "description": "Code Review & Security Patch: The provided Python snippet is vulnerable to SQL injection. Rewrite the `get_user(username)` function to use parameterized bounds.",
                "grader": grade_code_review,
                "context": "def get_user(cursor, username):\n    query = f'SELECT * FROM users WHERE username = \\'{username}\\''\n    return cursor.execute(query).fetchall()",
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
