import os
from dotenv import load_dotenv
from pydantic_settings import BaseSettings
from typing import Optional

load_dotenv()

class Settings(BaseSettings):
    # Application settings
    app_name: str = "OpenEnv"
    app_version: str = "1.1.0"
    debug: bool = os.getenv("DEBUG", "false").lower() == "true"
    
    # Server settings
    host: str = os.getenv("HOST", "0.0.0.0")
    port: int = int(os.getenv("PORT", "7860"))
    workers: int = int(os.getenv("WORKERS", "4"))
    
    # Database settings
    database_url: str = os.getenv("DATABASE_URL", "sqlite:///./openenv.db")
    redis_url: str = os.getenv("REDIS_URL", "redis://localhost:6379")
    
    # Security settings
    secret_key: str = os.getenv("SECRET_KEY", "your-secret-key-change-in-production")
    algorithm: str = os.getenv("ALGORITHM", "HS256")
    access_token_expire_minutes: int = int(os.getenv("ACCESS_TOKEN_EXPIRE_MINUTES", "30"))
    
    # Session settings
    session_timeout_minutes: int = int(os.getenv("SESSION_TIMEOUT_MINUTES", "60"))
    max_sessions: int = int(os.getenv("MAX_SESSIONS", "1000"))
    
    # Grading settings
    passing_score: float = float(os.getenv("PASSING_SCORE", "0.9"))
    max_attempts: int = int(os.getenv("MAX_ATTEMPTS", "3"))
    hints_enabled: bool = os.getenv("HINTS_ENABLED", "true").lower() == "true"
    
    # Logging settings
    log_level: str = os.getenv("LOG_LEVEL", "INFO")
    log_file: str = os.getenv("LOG_FILE", "logs/openenv.log")
    
    # Metrics settings
    metrics_enabled: bool = os.getenv("METRICS_ENABLED", "true").lower() == "true"
    metrics_port: int = int(os.getenv("METRICS_PORT", "9090"))
    
    # CORS settings
    allowed_origins: list = os.getenv("ALLOWED_ORIGINS", "*").split(",")
    
    # Task settings
    max_tasks: int = int(os.getenv("MAX_TASKS", "10"))
    task_generation_enabled: bool = os.getenv("TASK_GENERATION_ENABLED", "false").lower() == "true"
    
    class Config:
        env_file = ".env"
        case_sensitive = False

settings = Settings()