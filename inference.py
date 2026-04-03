import asyncio
import logging
import sys
import os
from typing import List
from openai import OpenAI
from models import Action, ActionType
from client import CodingEnvClient

# Configure baseline logging with ISO 8601 timestamps
logging.basicConfig(
    level=logging.INFO, 
    format='%(asctime)s [%(levelname)s] %(message)s',
    datefmt='%Y-%m-%dT%H:%M:%S%z',
    handlers=[logging.StreamHandler(sys.stdout)]
)
logger = logging.getLogger("inference-agent")

# Mandatory environment variables
API_BASE_URL = os.getenv("API_BASE_URL", "http://localhost:11434/v1")
MODEL_NAME = os.getenv("MODEL_NAME", "llama3")
HF_TOKEN = os.getenv("HF_TOKEN")
LOCAL_IMAGE_NAME = os.getenv("LOCAL_IMAGE_NAME")
TASK_NAME = os.getenv("OPENENV_TASK")
BENCHMARK = os.getenv("OPENENV_BENCHMARK")

class BaselineAgent:
    """Reference agent for baseline reproduction scoring using OpenAI client."""
    def __init__(self, name="Agent-alpha-01"):
        self.name = name
        self.model_name = MODEL_NAME
        self.client = OpenAI(
            base_url=API_BASE_URL,
            api_key=HF_TOKEN,
        )

    def act(self, observation) -> Action:
        """Logic for baseline reproduction."""
        try:
            response = self.client.chat.completions.create(
                model=self.model_name,
                messages=[
                    {"role": "system", "content": "Resolve the coding ticket. Output ONLY valid python code."},
                    {"role": "user", "content": f"Task: {observation.instruction}\nContext: {observation.context}"}
                ],
                max_tokens=256,
                temperature=0.1
            )
            solution = response.choices[0].message.content.strip().replace("```python", "").replace("```", "").strip()
            return Action(action_type=ActionType.SUBMIT, solution=solution)
        except Exception:
            return Action(action_type=ActionType.SKIP)

async def run_baseline_repro(base_url="ws://localhost:7860/ws"):
    """Main task loop implementing strict STDOUT formats."""
    steps_taken = 0
    rewards: List[float] = []
    success = False
    
    print(f"[START] task={TASK_NAME} env={BENCHMARK} model={MODEL_NAME}", flush=True)
    
    try:
        client = CodingEnvClient(base_url)
        async with client as env:
            agent = BaselineAgent()
            obs = await env.reset()
            
            for step in range(1, 11): # Max 10 steps
                steps_taken = step
                action = agent.act(obs)
                obs, reward, done, info = await env.step(action)
                
                r_val = float(reward.score) if hasattr(reward, 'score') else 0.0
                rewards.append(r_val)
                
                # [STEP] step=<n> action=<action_str> reward=<0.00> done=<true|false> error=<msg|null>
                print(f"[STEP] step={step} action={action.action_type.value} reward={r_val:.2f} done={str(done).lower()} error=null", flush=True)
                
                if done:
                    break
            
            total_reward = sum(rewards)
            score = min(max(total_reward, 0.0), 1.0)
            success = score >= 0.1
            
    except Exception as e:
        logger.error(f"Inference Failure: {str(e)}")
    finally:
        # [END] success=<true|false> steps=<n> score=<0.3f> rewards=<r1,r2,...,rn>
        rew_str = ",".join(f"{r:.2f}" for r in rewards)
        score_val = sum(rewards) / len(rewards) if rewards else 0.0
        print(f"[END] success={str(success).lower()} steps={steps_taken} score={score_val:.3f} rewards={rew_str}", flush=True)

def main():
    url = os.getenv("OPENENV_URL", "ws://localhost:7860/ws")
    asyncio.run(run_baseline_repro(url))

if __name__ == "__main__":
    main()
