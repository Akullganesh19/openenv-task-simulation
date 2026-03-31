import gradio as gr
from openenv.core import OpenEnv

def demo():
    env = OpenEnv()
    obs = env.reset()
    return f"Environment Initialized (Crypto Nonce): {env._nonce.hex()}\n\nTask: {obs.instruction}\nDifficulty: {obs.difficulty.value}"

iface = gr.Interface(
    fn=demo,
    inputs=[],
    outputs="text",
    title="OpenEnv Task Simulation - ZK & Crypto Validated"
)

if __name__ == "__main__":
    iface.launch()
