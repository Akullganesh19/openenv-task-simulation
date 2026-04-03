from typing import Dict, Any, List, Tuple
import re
import ast

class EnhancedGrader:
    def __init__(self):
        self.weights = {
            'correctness': 0.4,
            'code_quality': 0.2,
            'edge_cases': 0.2,
            'efficiency': 0.1,
            'documentation': 0.1
        }

    def grade_solution(self, solution: str, task_type: str) -> Dict[str, float]:
        """Grade a solution comprehensively"""
        scores = {}
        
        if task_type == 'email_triage':
            scores = self._grade_email_triage(solution)
        elif task_type == 'data_cleaning':
            scores = self._grade_data_cleaning(solution)
        elif task_type == 'code_review':
            scores = self._grade_code_review(solution)
        else:
            scores = {'correctness': 0.0, 'code_quality': 0.0, 'edge_cases': 0.0, 
                     'efficiency': 0.0, 'documentation': 0.0}

        # Calculate weighted total
        total_score = sum(scores[key] * self.weights[key] for key in scores)
        scores['total'] = min(1.0, max(0.0, total_score))
        
        return scores

    def _grade_email_triage(self, solution: str) -> Dict[str, float]:
        """Grade email triage task"""
        scores = {
            'correctness': 0.0,
            'code_quality': 0.0,
            'edge_cases': 0.0,
            'efficiency': 0.0,
            'documentation': 0.0
        }

        if not solution or not isinstance(solution, str):
            return scores

        # Correctness (40%)
        urgency_keywords = ["urgent", "ASAP", "down", "critical", "refund"]
        if "for" in solution and ("if" in solution or "filter" in solution):
            scores['correctness'] += 0.2
        if any(kw in solution.lower() for kw in urgency_keywords):
            scores['correctness'] += 0.2
        if "return" in solution:
            scores['correctness'] += 0.2

        # Code quality (20%)
        try:
            tree = ast.parse(solution)
            if len(tree.body) > 0:
                scores['code_quality'] += 0.1
            if any(isinstance(node, ast.FunctionDef) for node in ast.walk(tree)):
                scores['code_quality'] += 0.1
        except SyntaxError:
            pass

        # Edge cases (20%)
        if "try" in solution or "except" in solution:
            scores['edge_cases'] += 0.1
        if "isinstance" in solution:
            scores['edge_cases'] += 0.1

        # Efficiency (10%)
        if "in" in solution and len(re.findall(r'\bfor\b', solution)) < 2:
            scores['efficiency'] += 0.1

        # Documentation (10%)
        if '"""' in solution or "'''" in solution:
            scores['documentation'] += 0.05
        if "def" in solution and ":" in solution:
            scores['documentation'] += 0.05

        return scores

    def _grade_data_cleaning(self, solution: str) -> Dict[str, float]:
        """Grade data cleaning task"""
        scores = {
            'correctness': 0.0,
            'code_quality': 0.0,
            'edge_cases': 0.0,
            'efficiency': 0.0,
            'documentation': 0.0
        }

        if not solution or not isinstance(solution, str):
            return scores

        # Correctness
        if "drop_duplicates" in solution or "set(" in solution:
            scores['correctness'] += 0.2
        if "fillna" in solution or "None" in solution:
            scores['correctness'] += 0.2
        if "return" in solution:
            scores['correctness'] += 0.1

        # Code quality
        try:
            tree = ast.parse(solution)
            scores['code_quality'] += 0.1
            if any(isinstance(node, ast.Import) for node in ast.walk(tree)):
                scores['code_quality'] += 0.1
        except SyntaxError:
            pass

        # Edge cases
        if "try" in solution or "except" in solution:
            scores['edge_cases'] += 0.1
        if "isinstance" in solution or "dtype" in solution:
            scores['edge_cases'] += 0.1

        # Efficiency
        if "pandas" in solution.lower() or "numpy" in solution.lower():
            scores['efficiency'] += 0.1

        # Documentation
        if '"""' in solution or "'''" in solution:
            scores['documentation'] += 0.1

        return scores

    def _grade_code_review(self, solution: str) -> Dict[str, float]:
        """Grade code review task"""
        scores = {
            'correctness': 0.0,
            'code_quality': 0.0,
            'edge_cases': 0.0,
            'efficiency': 0.0,
            'documentation': 0.0
        }

        if not solution or not isinstance(solution, str):
            return scores

        # Correctness
        if "execute(" in solution and ("?" in solution or "%s" in solution):
            scores['correctness'] += 0.25
        if "f-string" not in solution.lower() and "format(" not in solution:
            scores['correctness'] += 0.25
        if "parameter" in solution.lower() or "prepare" in solution.lower():
            scores['correctness'] += 0.1

        # Code quality
        try:
            tree = ast.parse(solution)
            scores['code_quality'] += 0.1
            if any(isinstance(node, ast.FunctionDef) for node in ast.walk(tree)):
                scores['code_quality'] += 0.1
        except SyntaxError:
            pass

        # Edge cases
        if "validate" in solution.lower() or "sanitize" in solution.lower():
            scores['edge_cases'] += 0.1
        if "try" in solution or "except" in solution:
            scores['edge_cases'] += 0.1

        # Efficiency
        if "cursor" in solution or "connection" in solution:
            scores['efficiency'] += 0.1

        # Documentation
        if '"""' in solution or "'''" in solution:
            scores['documentation'] += 0.1

        return scores

    def get_feedback(self, scores: Dict[str, float], task_type: str) -> str:
        """Generate feedback based on scores"""
        feedback = []
        
        if scores['correctness'] < 0.3:
            feedback.append("The solution needs to better address the core requirements.")
        if scores['code_quality'] < 0.15:
            feedback.append("Consider improving code structure and readability.")
        if scores['edge_cases'] < 0.15:
            feedback.append("Think about handling edge cases and potential errors.")
        if scores['efficiency'] < 0.08:
            feedback.append("The solution could be more efficient.")
        if scores['documentation'] < 0.08:
            feedback.append("Adding comments and documentation would help.")

        if not feedback:
            feedback.append("Good job! The solution meets the requirements.")

        return " ".join(feedback)

# Global grader instance
enhanced_grader = EnhancedGrader()