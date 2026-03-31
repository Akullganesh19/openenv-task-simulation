import logging
import sys
from openenv.core import OpenEnv

# Setup basic logging enhancement
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s',
    handlers=[
        logging.StreamHandler(sys.stdout),
        logging.FileHandler("openenv.log")
    ]
)
logger = logging.getLogger("OpenEnv-App")

def main():
    logger.info("Initializing OpenEnv Environment...")
    env = OpenEnv()
    state = env.reset()
    logger.info(f"Started Task: {state.current_task.task_id} ({state.current_task.task_type.value})")

    # Example loop or entry point logic
    # For now, we'll just show the initialized state
    print(f"Current State: {state.model_dump_json(indent=2)}")

if __name__ == "__main__":
    main()