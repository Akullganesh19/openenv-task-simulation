import asyncio
import json
import websockets
from typing import Dict, Any, Optional, Tuple, Type
from pydantic import BaseModel
from models import Observation, Action, Reward, EnvironmentState

class CodingEnvClient:
    """Production-grade Python client for interacting with OpenEnv sessions.
    
    Supports WebSocket connections to handle session persistence and 
    low-latency (>10-50x faster) state transitions compared to HTTP.
    
    Usage:
        client = CodingEnvClient("ws://localhost:8000/ws")
        async with client as env:
            obs = await env.reset()
            obs, reward, done, info = await env.step(Action(action_type="submit", solution="..."))
    """
    
    def __init__(self, base_url: str):
        self.base_url = base_url
        self.websocket = None
        self.session_id = None

    async def __aenter__(self):
        self.websocket = await websockets.connect(self.base_url)
        # Handle initial session handshake
        response = await self.websocket.recv()
        data = json.loads(response)
        self.session_id = data.get("session_id")
        return self

    async def __aexit__(self, exc_type, exc_val, exc_tb):
        if self.websocket:
            await self.websocket.close()

    async def reset(self, episode_id: Optional[str] = None) -> Observation:
        """Reset the environment session."""
        msg = {"type": "reset", "payload": {"episode_id": episode_id}}
        await self.websocket.send(json.dumps(msg))
        
        response = await self.websocket.recv()
        data = json.loads(response)
        return Observation(**data.get("observation", {}))

    async def step(self, action: Action) -> Tuple[Observation, Reward, bool, Dict[str, Any]]:
        """Execute a single step."""
        msg = {"type": "step", "payload": action.model_dump()}
        await self.websocket.send(json.dumps(msg))
        
        response = await self.websocket.recv()
        data = json.loads(response)
        
        obs = Observation(**data.get("observation", {}))
        reward = Reward(**data.get("reward", {}))
        done = data.get("done", False)
        info = data.get("info", {})
        
        return obs, reward, done, info

    async def get_state(self) -> EnvironmentState:
        """Retrieve the current environment state."""
        msg = {"type": "state"}
        await self.websocket.send(json.dumps(msg))
        
        response = await self.websocket.recv()
        data = json.loads(response)
        return EnvironmentState(**data.get("state", {}))
