from prometheus_client import Counter, Histogram, Gauge, Info
import time

# Request metrics
REQUESTS_TOTAL = Counter(
    'openenv_requests_total',
    'Total number of requests',
    ['method', 'endpoint', 'status']
)

REQUEST_DURATION = Histogram(
    'openenv_request_duration_seconds',
    'Request duration in seconds',
    ['method', 'endpoint']
)

# Session metrics
ACTIVE_SESSIONS = Gauge(
    'openenv_active_sessions',
    'Number of active sessions'
)

SESSION_DURATION = Histogram(
    'openenv_session_duration_seconds',
    'Session duration in seconds',
    ['task_id', 'difficulty']
)

# Task metrics
TASKS_COMPLETED = Counter(
    'openenv_tasks_completed_total',
    'Total number of tasks completed',
    ['task_id', 'difficulty']
)

TASK_SCORES = Histogram(
    'openenv_task_scores',
    'Task scores distribution',
    ['task_id', 'difficulty'],
    buckets=[0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0]
)

# Grader metrics
GRADING_DURATION = Histogram(
    'openenv_grading_duration_seconds',
    'Grading duration in seconds',
    ['task_id']
)

# System info
SYSTEM_INFO = Info(
    'openenv_system',
    'System information'
)

class MetricsCollector:
    def __init__(self):
        self.start_time = time.time()
        SYSTEM_INFO.info({'version': '1.1.0', 'environment': 'production'})

    def record_request(self, method: str, endpoint: str, status: int, duration: float):
        REQUESTS_TOTAL.labels(method=method, endpoint=endpoint, status=status).inc()
        REQUEST_DURATION.labels(method=method, endpoint=endpoint).observe(duration)

    def record_session_start(self):
        ACTIVE_SESSIONS.inc()

    def record_session_end(self, task_id: str, difficulty: str, duration: float):
        ACTIVE_SESSIONS.dec()
        SESSION_DURATION.labels(task_id=task_id, difficulty=difficulty).observe(duration)

    def record_task_completion(self, task_id: str, difficulty: str, score: float):
        TASKS_COMPLETED.labels(task_id=task_id, difficulty=difficulty).inc()
        TASK_SCORES.labels(task_id=task_id, difficulty=difficulty).observe(score)

    def record_grading(self, task_id: str, duration: float):
        GRADING_DURATION.labels(task_id=task_id).observe(duration)

# Global metrics collector instance
metrics = MetricsCollector()