import asyncio
import json
import uuid
import logging
from typing import Dict, Any, Optional
from fastapi import FastAPI, WebSocket, HTTPException, Depends, Body, APIRouter
from fastapi.middleware.cors import CORSMiddleware
from server.environment import CodingEnvironment
from models import Observation, Action, Reward, EnvironmentState
from server.enhanced_api import router as enhanced_router
from config import settings
import uvicorn

# Setup logging
logging.basicConfig(level=getattr(logging, settings.log_level), format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger("openenv-server")

app = FastAPI(
    title=f"{settings.app_name} Server", 
    description="Production-grade RL coding environment with session-isolated access.",
    version=settings.app_version
)

# CORS middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

v1_router = APIRouter(prefix="/api/v1")

# Thread-safe session management
sessions: Dict[str, CodingEnvironment] = {}
sessions_lock = asyncio.Lock()

@app.get("/")
async def read_root():
    return {"status": "ok", "message": "Welcome to OpenEnv Professional Server.", "version": "1.0.0"}

@app.get("/health")
async def health_check():
    return {"status": "ok", "active_sessions": len(sessions)}

@v1_router.post("/reset")
async def reset_endpoint(episode_id: Optional[str] = Body(None, embed=True)):
    """Reset the environment and return the initial observation and session_id."""
    session_id = str(uuid.uuid4())
    env = CodingEnvironment()
    obs = env.reset(episode_id=episode_id)
    
    async with sessions_lock:
        sessions[session_id] = env
    
    logger.info(f"New session established via REST: {session_id}")
    return {"session_id": session_id, "observation": obs.model_dump()}

@v1_router.post("/step")
async def step_endpoint(session_id: str = Body(...), action_data: Dict[str, Any] = Body(...)):
    """Execute a step in a given session."""
    async with sessions_lock:
        env = sessions.get(session_id)
    
    if not env:
        raise HTTPException(status_code=404, detail="Session not found or expired.")
    
    try:
        action = Action(**action_data)
        obs, reward, done, info = env.step(action)
        return {
            "observation": obs.model_dump(),
            "reward": reward.model_dump(),
            "done": done,
            "info": info
        }
    except Exception as e:
        logger.error(f"Error in session {session_id} step: {str(e)}")
        raise HTTPException(status_code=400, detail=str(e))

@v1_router.get("/state")
async def state_endpoint(session_id: str):
    """Retrieve the current state of a session."""
    async with sessions_lock:
        env = sessions.get(session_id)
        
    if not env:
        raise HTTPException(status_code=404, detail="Session not found.")
        
    return {"state": env.state.model_dump()}

app.include_router(v1_router)
app.include_router(enhanced_router)

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    """Persistent session logic over WebSockets."""
    await websocket.accept()
    session_id = str(uuid.uuid4())
    env = CodingEnvironment()
    
    async with sessions_lock:
        sessions[session_id] = env
    
    logger.info(f"New session established via WS: {session_id}")
    
    try:
        await websocket.send_json({"type": "session_start", "session_id": session_id})
        
        while True:
            data = await websocket.receive_text()
            message = json.loads(data)
            msg_type = message.get("type")
            payload = message.get("payload", {})
            
            if msg_type == "reset":
                obs = env.reset(episode_id=payload.get("episode_id"))
                await websocket.send_json({"type": "observation", "observation": obs.model_dump()})
                
            elif msg_type == "step":
                action = Action(**payload)
                obs, reward, done, info = env.step(action)
                await websocket.send_json({
                    "type": "step_result", 
                    "observation": obs.model_dump(), 
                    "reward": reward.model_dump(), 
                    "done": done, 
                    "info": info
                })
                
            elif msg_type == "state":
                await websocket.send_json({"type": "state", "state": env.state.model_dump()})
                
            else:
                await websocket.send_json({"type": "error", "message": f"Unsupported message type: {msg_type}"})
                
    except Exception as e:
        logger.error(f"Error in WS session {session_id}: {str(e)}")
    finally:
        async with sessions_lock:
            if session_id in sessions:
                del sessions[session_id]
        logger.info(f"Session terminated: {session_id}")

def main():
    uvicorn.run(app, host="0.0.0.0", port=7860)

if __name__ == "__main__":
    main()
