import sys
import os

# Add parent directory to path to import openenv package
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from openenv.tasks import TaskManager, TaskType
from typing import Dict, Any

class BaselineAgent:
    def __init__(self):
        self.task_manager = TaskManager()

    def solve_task(self, task_id: str) -> str:
        task = self.task_manager.get_task(task_id)
        if not task:
            return ""

        t_type = task["type"]
        if t_type == TaskType.EASY:
            return self._solve_easy_task()
        elif t_type == TaskType.MEDIUM:
            return self._solve_medium_task()
        elif t_type == TaskType.HARD:
            return self._solve_hard_task()

        return ""

    def _solve_easy_task(self) -> str:
        return "def hello_world():\n    return 'Hello, World!'"

    def _solve_medium_task(self) -> str:
        return """def fibonacci(n):
    if n <= 0:
        return 0
    elif n == 1:
        return 1
    else:
        return fibonacci(n-1) + fibonacci(n-2)"""

    def _solve_hard_task(self) -> str:
        return """def quicksort(arr):
    if len(arr) <= 1:
        return arr
    pivot = arr[len(arr) // 2]
    left = [x for x in arr if x < pivot]
    middle = [x for x in arr if x == pivot]
    right = [x for x in arr if x > pivot]
    return quicksort(left) + middle + quicksort(right)"""

    def run_benchmark(self) -> Dict[str, Any]:
        results = {}
        for task_id in ["task_1", "task_2", "task_3"]:
            solution = self.solve_task(task_id)
            score = self.task_manager.grade_solution(task_id, solution)
            results[task_id] = {
                "solution": solution,
                "score": score,
                "passed": score >= 0.9
            }
        return results

if __name__ == "__main__":
    agent = BaselineAgent()
    benchmark_results = agent.run_benchmark()
    print("\n--- OpenEnv Baseline Results ---")
    for task_id, result in benchmark_results.items():
        pass_status = "[PASS]" if result['passed'] else "[FAIL]"
        print(f"{task_id}: Score={result['score']:.2f} {pass_status}")
