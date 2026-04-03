from sqlalchemy import create_engine, Column, Integer, String, Float, Text, DateTime, Boolean, ForeignKey
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, relationship
from datetime import datetime
import structlog

logger = structlog.get_logger()

Base = declarative_base()

class User(Base):
    __tablename__ = 'users'
    id = Column(Integer, primary_key=True, index=True)
    username = Column(String, unique=True, index=True, nullable=False)
    email = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    last_login = Column(DateTime)
    sessions = relationship('Session', back_populates='user')

class Session(Base):
    __tablename__ = 'sessions'
    id = Column(Integer, primary_key=True, index=True)
    session_id = Column(String, unique=True, index=True, nullable=False)
    user_id = Column(Integer, ForeignKey('users.id'))
    task_id = Column(String, nullable=False)
    difficulty = Column(String, nullable=False)
    start_time = Column(DateTime, default=datetime.utcnow)
    end_time = Column(DateTime)
    total_reward = Column(Float, default=0.0)
    status = Column(String, default='active')
    user = relationship('User', back_populates='sessions')
    attempts = relationship('TaskAttempt', back_populates='session')

class TaskAttempt(Base):
    __tablename__ = 'task_attempts'
    id = Column(Integer, primary_key=True, index=True)
    session_id = Column(Integer, ForeignKey('sessions.id'))
    task_id = Column(String, nullable=False)
    solution = Column(Text)
    score = Column(Float)
    timestamp = Column(DateTime, default=datetime.utcnow)
    session = relationship('Session', back_populates='attempts')

class Analytics(Base):
    __tablename__ = 'analytics'
    id = Column(Integer, primary_key=True, index=True)
    metric_name = Column(String, nullable=False)
    metric_value = Column(Float, nullable=False)
    timestamp = Column(DateTime, default=datetime.utcnow)
    tags = Column(Text)

def get_database_session():
    # Use config-based URL if possible, fallback to sqlite
    from config import settings
    engine = create_engine(settings.database_url)
    Base.metadata.create_all(bind=engine)
    SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
    return SessionLocal()

def log_analytics(metric_name: str, metric_value: float, tags: dict = None):
    session = get_database_session()
    try:
        analytics = Analytics(
            metric_name=metric_name,
            metric_value=metric_value,
            tags=str(tags) if tags else None
        )
        session.add(analytics)
        session.commit()
        logger.info(f"Logged analytics: {metric_name}={metric_value}", tags=tags)
    except Exception as e:
        logger.error(f"Failed to log analytics: {str(e)}")
        session.rollback()
    finally:
        session.close()