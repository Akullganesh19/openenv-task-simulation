from fastapi import APIRouter, Depends, HTTPException, status
from fastapi.security import OAuth2PasswordBearer, OAuth2PasswordRequestForm
from jose import JWTError, jwt
from passlib.context import CryptContext
from datetime import datetime, timedelta
from typing import Optional, Dict, Any
from pydantic import BaseModel
import structlog

from config import settings
from models.database import get_database_session, User, Session, TaskAttempt, Analytics

logger = structlog.get_logger()

# Security
pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")
oauth2_scheme = OAuth2PasswordBearer(tokenUrl="token")

router = APIRouter(prefix="/api/v1/enhanced")

class UserCreate(BaseModel):
    username: str
    email: str
    password: str

class UserResponse(BaseModel):
    id: int
    username: str
    email: str
    is_active: bool
    created_at: datetime

class Token(BaseModel):
    access_token: str
    token_type: str

class TaskSubmission(BaseModel):
    session_id: str
    solution: str
    task_type: str

class AnalyticsResponse(BaseModel):
    total_sessions: int
    total_attempts: int
    average_score: float
    tasks_completed: int

def verify_password(plain_password: str, hashed_password: str) -> bool:
    return pwd_context.verify(plain_password, hashed_password)

def get_password_hash(password: str) -> str:
    return pwd_context.hash(password)

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    to_encode = data.copy()
    if expires_delta:
        expire = datetime.utcnow() + expires_delta
    else:
        expire = datetime.utcnow() + timedelta(minutes=15)
    to_encode.update({"exp": expire})
    encoded_jwt = jwt.encode(to_encode, settings.secret_key, algorithm=settings.algorithm)
    return encoded_jwt

async def get_current_user(token: str = Depends(oauth2_scheme)):
    credentials_exception = HTTPException(
        status_code=status.HTTP_401_UNAUTHORIZED,
        detail="Could not validate credentials",
        headers={"WWW-Authenticate": "Bearer"},
    )
    try:
        payload = jwt.decode(token, settings.secret_key, algorithms=[settings.algorithm])
        username: str = payload.get("sub")
        if username is None:
            raise credentials_exception
    except JWTError:
        raise credentials_exception
    
    db = get_database_session()
    user = db.query(User).filter(User.username == username).first()
    if user is None:
        raise credentials_exception
    return user

@router.post("/register", response_model=UserResponse)
async def register_user(user_data: UserCreate):
    """Register a new user"""
    db = get_database_session()
    try:
        # Check if user exists
        if db.query(User).filter(User.username == user_data.username).first():
            raise HTTPException(status_code=400, detail="Username already registered")
        if db.query(User).filter(User.email == user_data.email).first():
            raise HTTPException(status_code=400, detail="Email already registered")
        
        # Create user
        hashed_password = get_password_hash(user_data.password)
        user = User(
            username=user_data.username,
            email=user_data.email,
            hashed_password=hashed_password
        )
        db.add(user)
        db.commit()
        db.refresh(user)
        
        logger.info(f"User registered: {user.username}")
        return UserResponse(
            id=user.id,
            username=user.username,
            email=user.email,
            is_active=user.is_active,
            created_at=user.created_at
        )
    except Exception as e:
        db.rollback()
        logger.error(f"Registration error: {str(e)}")
        raise HTTPException(status_code=500, detail="Registration failed")

@router.post("/token", response_model=Token)
async def login_for_access_token(form_data: OAuth2PasswordRequestForm = Depends()):
    """Login and get access token"""
    db = get_database_session()
    user = db.query(User).filter(User.username == form_data.username).first()
    
    if not user or not verify_password(form_data.password, user.hashed_password):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect username or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    access_token_expires = timedelta(minutes=settings.access_token_expire_minutes)
    access_token = create_access_token(
        data={"sub": user.username}, expires_delta=access_token_expires
    )
    
    # Update last login
    user.last_login = datetime.utcnow()
    db.commit()
    
    logger.info(f"User logged in: {user.username}")
    return {"access_token": access_token, "token_type": "bearer"}

@router.get("/users/me", response_model=UserResponse)
async def read_users_me(current_user: User = Depends(get_current_user)):
    """Get current user info"""
    return UserResponse(
        id=current_user.id,
        username=current_user.username,
        email=current_user.email,
        is_active=current_user.is_active,
        created_at=current_user.created_at
    )

@router.post("/submit", response_model=Dict[str, Any])
async def submit_solution(
    submission: TaskSubmission,
    current_user: User = Depends(get_current_user)
):
    """Submit a solution for grading"""
    db = get_database_session()
    try:
        # Find session
        session = db.query(Session).filter(
            Session.session_id == submission.session_id,
            Session.user_id == current_user.id
        ).first()
        
        if not session:
            raise HTTPException(status_code=404, detail="Session not found")
        
        # Grade solution
        from enhanced_graders import enhanced_grader
        scores = enhanced_grader.grade_solution(submission.solution, submission.task_type)
        feedback = enhanced_grader.get_feedback(scores, submission.task_type)
        
        # Save attempt
        attempt = TaskAttempt(
            session_id=session.id,
            task_id=submission.task_type,
            solution=submission.solution,
            score=scores['total']
        )
        db.add(attempt)
        
        # Update session reward
        session.total_reward += scores['total']
        
        # Log analytics
        analytics = Analytics(
            metric_name="task_submission",
            metric_value=scores['total'],
            tags=f'{{"task_type": "{submission.task_type}", "user": "{current_user.username}"}}'
        )
        db.add(analytics)
        
        db.commit()
        
        logger.info(f"Solution submitted by {current_user.username} for {submission.task_type}")
        
        return {
            "scores": scores,
            "feedback": feedback,
            "attempt_id": attempt.id
        }
    except Exception as e:
        db.rollback()
        logger.error(f"Submission error: {str(e)}")
        raise HTTPException(status_code=500, detail="Submission failed")

@router.get("/analytics", response_model=AnalyticsResponse)
async def get_analytics(current_user: User = Depends(get_current_user)):
    """Get user analytics"""
    db = get_database_session()
    try:
        # Get user sessions
        sessions = db.query(Session).filter(Session.user_id == current_user.id).all()
        total_sessions = len(sessions)
        
        # Get attempts
        attempts = db.query(TaskAttempt).join(Session).filter(Session.user_id == current_user.id).all()
        total_attempts = len(attempts)
        
        # Calculate average score
        if total_attempts > 0:
            average_score = sum(a.score for a in attempts) / total_attempts
        else:
            average_score = 0.0
        
        # Count completed tasks
        tasks_completed = len([s for s in sessions if s.status == 'completed'])
        
        return AnalyticsResponse(
            total_sessions=total_sessions,
            total_attempts=total_attempts,
            average_score=average_score,
            tasks_completed=tasks_completed
        )
    except Exception as e:
        logger.error(f"Analytics error: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to get analytics")

@router.get("/leaderboard")
async def get_leaderboard():
    """Get top performers"""
    db = get_database_session()
    try:
        # Get top users by average score
        from sqlalchemy import func
        results = db.query(
            User.username,
            func.avg(TaskAttempt.score).label('avg_score'),
            func.count(TaskAttempt.id).label('attempts')
        ).join(Session, Session.user_id == User.id
        ).join(TaskAttempt, TaskAttempt.session_id == Session.id
        ).group_by(User.id
        ).order_by(func.avg(TaskAttempt.score).desc()
        ).limit(10).all()
        
        leaderboard = [
            {
                "username": r.username,
                "average_score": round(r.avg_score, 2),
                "attempts": r.attempts
            }
            for r in results
        ]
        
        return {"leaderboard": leaderboard}
    except Exception as e:
        logger.error(f"Leaderboard error: {str(e)}")
        raise HTTPException(status_code=500, detail="Failed to get leaderboard")