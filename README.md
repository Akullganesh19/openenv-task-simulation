# OpenEnv Task Simulation

A structured environment for evaluating AI agents on coding tasks.

## Project Structure
- `openenv/`: Core package containing models, tasks, and environment logic.
- `scripts/`: Benchmark and utility scripts.
- `configs/`: Configuration files.
- `tests/`: Unit tests for environment graders.

## Setup
```bash
pip install -r requirements.txt
```

## Running Baseline
```bash
python scripts/baseline.py
```

## Features
- Modular design with separation of concerns.
- Robust task grading system.
- Logging and telemetry support.
