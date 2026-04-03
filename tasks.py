from typing import Dict, Any, Callable, List, Optional
import re
import ast
from models import TaskDifficulty
from enhanced_graders import enhanced_grader

# Original basic graders (kept for backward compatibility)
def grade_email_triage_basic(solution: str) -> float:
    if not solution or not isinstance(solution, str):
        return 0.0
    urgency_keywords = ["urgent", "ASAP", "down", "critical", "refund"]
    score = 0.2
    if "for" in solution and ("if" in solution or "filter" in solution):
        score += 0.4
    if any(kw in solution.lower() for kw in urgency_keywords):
        score += 0.4
    return max(0.0, min(1.0, score))

def grade_data_cleaning_basic(solution: str) -> float:
    if not solution or not isinstance(solution, str):
        return 0.0
    score = 0.0
    if "drop_duplicates" in solution or "set(" in solution:
        score += 0.4
    if "fillna" in solution or "None" in solution:
        score += 0.4
    if "return" in solution:
        score += 0.2
    return max(0.0, min(1.0, score))

def grade_code_review_basic(solution: str) -> float:
    if not solution or not isinstance(solution, str):
        return 0.0
    score = 0.0
    if "execute(" in solution and ("?" in solution or "%s" in solution):
        score += 0.5
    if "f-string" not in solution.lower() and "format(" not in solution:
        score += 0.5
    return max(0.0, min(1.0, score))

# Enhanced graders using the new system
def grade_email_triage_enhanced(solution: str) -> float:
    scores = enhanced_grader.grade_solution(solution, 'email_triage')
    return scores['total']

def grade_data_cleaning_enhanced(solution: str) -> float:
    scores = enhanced_grader.grade_solution(solution, 'data_cleaning')
    return scores['total']

def grade_code_review_enhanced(solution: str) -> float:
    scores = enhanced_grader.grade_solution(solution, 'code_review')
    return scores['total']

class TaskManager:
    def __init__(self):
        # We default to using enhanced graders for production, falling back to basic if needed
        self._tasks = {
            "task_1": {
                "difficulty": TaskDifficulty.EASY,
                "description": "Email Triage Task: Write a function `triage_emails(emails: list[dict])` that filters a list of email dictionaries and returns only those where the subject or body contains 'urgent', 'ASAP', or 'critical'.",
                "grader": grade_email_triage_enhanced,
                "context": "def triage_emails(emails):\n    pass",
                "type": "email_triage"
            },
            "task_2": {
                "difficulty": TaskDifficulty.MEDIUM,
                "description": "Data Pipeline Task: Write a function `clean_dataset(data: pd.DataFrame)` that removes duplicate rows and imputes missing numeric values with the column mean.",
                "grader": grade_data_cleaning_enhanced,
                "context": "import pandas as pd\n\ndef clean_dataset(data):\n    pass",
                "type": "data_cleaning"
            },
            "task_3": {
                "difficulty": TaskDifficulty.HARD,
                "description": "Security Patching Task: Refactor the following SQL query function to use parameterized queries to prevent SQL injection: `def run_query(cursor, user_input): cursor.execute(f'SELECT * FROM users WHERE name={user_input}')`.",
                "grader": grade_code_review_enhanced,
                "context": "import sqlite3\n\ndef secure_query(cursor, user_input):\n    # TODO: Implement parameterized query\n    pass",
                "type": "code_review"
            }
        }

    def get_task(self, task_id: str) -> Optional[Dict[str, Any]]:
        return self._tasks.get(task_id)

    def list_tasks(self) -> List[str]:
        return list(self._tasks.keys())

    def grade(self, task_id: str, solution: str) -> float:
        task = self.get_task(task_id)
        if not task:
            return 0.0
        return task["grader"](solution)
