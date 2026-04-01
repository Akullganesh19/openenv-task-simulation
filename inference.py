import asyncio
import logging
import sys
import os
from openai import AsyncOpenAI
from models import Action, ActionType
from client import CodingEnvClient

# Configure baseline logging to emit structured format logs
logging.basicConfig(level=logging.INFO, format='%(message)s', handlers=[logging.StreamHandler(sys.stdout)])
logger = logging.getLogger("inference-agent")

class BaselineAgent:
    """Reference agent for baseline reproduction scoring using OpenAI client."""
    
    def __init__(self, name="Agent-alpha-01"):
        self.name = name
        
        api_base_url = os.environ.get("API_BASE_URL", "https://api.openai.com/v1")
        model_name = os.environ.get("MODEL_NAME", "gpt-3.5-turbo")
        api_key = os.environ.get("HF_TOKEN")
        
        if not api_key:
            logger.warning("HF_TOKEN is not set. Using dummy-key, calls will likely fail if hitting production OpenAI.")
            api_key = "dummy-key"
            
        self.model_name = model_name
        
        self.client = AsyncOpenAI(
            base_url=api_base_url,
            api_key=api_key,
        )

    async def act(self, observation) -> Action:
        """Asynchronous logic for baseline reproduction using OpenAI client."""
        task_id = observation.task_id
        instruction = observation.instruction
        context = observation.context
        
        prompt = f"You are an expert software engineer resolving real-world tickets.\nTask: {instruction}\n\nStarter Code:\n{context}\n\nPlease provide ONLY the raw python/logic requested as your response. Do not use markdown blocks, just raw code."
        
        try:
            response = await self.client.chat.completions.create(
                model=self.model_name,
                messages=[
                    {"role": "system", "content": "You are a helpful coding assistant. Output only the requested code, without formatting or explanation."},
                    {"role": "user", "content": prompt}
                ],
                max_tokens=256,
                temperature=0.1
            )
            
            solution = response.choices[0].message.content.strip()
            if solution.startswith("```python"):
                solution = solution[9:]
            if solution.startswith("```"):
                solution = solution[3:]
            if solution.endswith("```"):
                solution = solution[:-3]
            solution = solution.strip()
            
            return Action(
                action_type=ActionType.SUBMIT,
                solution=solution,
                explanation=f"LLM generated solution using {self.model_name}"
            )
        except Exception as e:
            # Fallback action on error (e.g. invalid API key) to ensure pipeline completion
            return Action(action_type=ActionType.SKIP)


async def run_baseline_repro(base_url="ws://localhost:7860/ws"):
    """Main task loop for scoring."""
    client = CodingEnvClient(base_url)
    
    try:
        async with client as env:
            agent = BaselineAgent()
            obs = await env.reset()
            episode_id = await env.get_state()
            episode_id = episode_id.episode_id
            
            # Formatted stdout requirement
            logger.info(f"[START] Episode {episode_id} initialization sequence begun...")
            
            while obs.task_id != "complete":
                action = await agent.act(obs)
                obs, reward, done, info = await env.step(action)
                
                # Formatted stdout requirement
                logger.info(f"[STEP] Task {obs.task_id} Executed | Action: {action.action_type.value} | Reward: {reward.score:.2f} | Completed: {done}")
                
                if done and obs.task_id == "complete":
                    break

            # Final state verification
            final_state = await env.get_state()
            
            # Formatted stdout requirement
            logger.info(f"[END] Episode {final_state.episode_id} Final Cumulative Reward: {final_state.total_reward:.2f}")
            
    except ConnectionRefusedError:
        logger.error("Connection failed. Is the OpenEnv server running?")
    except Exception as e:
        logger.error(f"Execution Error: {str(e)}")

def main():
    url = os.getenv("OPENENV_URL", "ws://localhost:7860/ws")
    asyncio.run(run_baseline_repro(url))

if __name__ == "__main__":
    main()
