# Stage 1: Build stage
FROM python:3.11-slim as builder

WORKDIR /app

# Install system build dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    build-essential \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Install python dependencies into a virtualenv or just a target directory
COPY requirements.txt .
RUN pip install --no-cache-dir --prefix=/install -r requirements.txt

# Stage 2: Final runtime stage
FROM python:3.11-slim

WORKDIR /app

# Copy installed dependencies from the builder stage
COPY --from=builder /install /usr/local

# Install runtime-only system dependencies
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Copy application source code
COPY . .

# Ensure non-root user for security compliance
RUN useradd -m -u 1000 user
USER user
ENV HOME=/home/user \
    PATH=/home/user/.local/bin:$PATH

# Environment configuration for Hugging Face Spaces
ENV PORT=7860
EXPOSE 7860

# Production-grade Uvicorn entry point
# Uses 4 workers for optimal resource utilization on 2 vCPU machines
CMD ["uvicorn", "server.app:app", "--host", "0.0.0.0", "--port", "7860", "--workers", "4"]