from fastapi import FastAPI, WebSocket, HTTPException, Depends
from typing import Dict, Any, Optional
import json
import uuid
import logging
from .environment import CodingEnvironment
from models import Observation, Action, Reward, EnvironmentState
import uvicorn

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger("openenv-server")

app = FastAPI(title="OpenEnv Server", description="Production-grade RL coding environment with session-isolated WebSockets.")

sessions: Dict[str, CodingEnvironment] = {}

@app.get("/")
def read_root():
    return {"status": "ok", "message": "Welcome to OpenEnv Professional Server."}

@app.websocket("/ws")
async def websocket_endpoint(websocket: WebSocket):
    """Persistent session logic over WebSockets.
    Each connection creates a unique instance of CodingEnvironment.
    """
    await websocket.accept()
    session_id = str(uuid.uuid4())
    env = CodingEnvironment()
    sessions[session_id] = env
    
    logger.info(f"New session established: {session_id}")
    
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
        logger.error(f"Error in session {session_id}: {str(e)}")
    finally:
        if session_id in sessions:
            del sessions[session_id]
        logger.info(f"Session terminated: {session_id}")

@app.get("/health")
def health_check():
    return {"status": "ok", "active_sessions": len(sessions)}

def main():
    uvicorn.run(app, host="0.0.0.0", port=7860)

if __name__ == "__main__":
    main()
