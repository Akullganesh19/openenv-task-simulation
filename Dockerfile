FROM python:3.11-slim

WORKDIR /app

# Install system dependencies if any
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/cache/*

# Install project dependencies
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy source code
COPY . .

# Environment configuration for Hugging Face Spaces
# HF Spaces use port 7860 by default
ENV PORT=7860
EXPOSE 7860

# Production-grade Uvicorn entry point for the professional server
# Uses 4 workers for single-container scaling as per reference benchmarks
CMD ["uvicorn", "server.app:app", "--host", "0.0.0.0", "--port", "7860", "--workers", "4"]