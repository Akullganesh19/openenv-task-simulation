import os
import requests
import sys
import subprocess
import json
import time

def log(msg, status="INFO"):
    colors = {"INFO": "\033[0;34m", "PASS": "\033[0;32m", "FAIL": "\033[0;31m", "RESET": "\033[0m"}
    print(f"{colors.get(status, '')}[{status}] {msg}{colors['RESET']}")

def check_endpoint(url):
    log(f"Phase 1: Validating HF Space Deployment at {url}...")
    try:
        # Check /health endpoint
        resp = requests.get(f"{url}/health", timeout=10)
        if resp.status_code == 200:
            log(f"Health check passed: {resp.json()}", "PASS")
        else:
            log(f"Health check failed with status {resp.status_code}", "FAIL")
            return False

        # Check /api/v1/reset endpoint (OpenEnv Spec)
        log(f"Phase 2: Validating /api/v1/reset endpoint...")
        resp = requests.post(f"{url}/api/v1/reset", json={}, timeout=10)
        if resp.status_code == 200:
            data = resp.json()
            if "session_id" in data and "observation" in data:
                log(f"Reset check passed. Session: {data['session_id']}", "PASS")
            else:
                log(f"Reset response missing fields: {data}", "FAIL")
                return False
        else:
            log(f"Reset failed with status {resp.status_code}: {resp.text}", "FAIL")
            return False
        
        return True
    except Exception as e:
        log(f"Endpoint validation error: {str(e)}", "FAIL")
        return False

def check_docker():
    log("Phase 1: Validating Dockerfile Compliance...")
    if not os.path.exists("Dockerfile"):
        log("Dockerfile not found in root.", "FAIL")
        return False
    
    with open("Dockerfile", "r") as f:
        content = f.read()
        if "USER user" in content and "useradd" in content:
            log("Security check: Non-root user implementation detected.", "PASS")
        else:
            log("Security warning: Non-root user not explicitly defined in Dockerfile.", "INFO")
        
        if "builder" in content and "COPY --from" in content:
            log("Optimization check: Multi-stage build implemented.", "PASS")
    
    return True

def run_openenv_validate():
    log("Phase 2: Running openenv-core validation suite...")
    try:
        result = subprocess.run(["uv", "run", "openenv", "validate"], capture_output=True, text=True)
        if result.returncode == 0:
            log("openenv-core validation passed successfully.", "PASS")
            return True
        else:
            log(f"openenv-core validation failed:\n{result.stderr or result.stdout}", "FAIL")
            return False
    except FileNotFoundError:
        log("uv or openenv command not found. Ensure installation.", "FAIL")
        return False

def main():
    root_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), ".."))
    os.chdir(root_dir)

    print("\n" + "="*50)
    print("  OpenEnv Compliance Validator (Cybersecurity Agent Mode)")
    print("="*50 + "\n")

    # Local infrastructure checks
    if not check_docker():
        log("Docker validation failed. Submission likely invalid.", "FAIL")
    
    if not run_openenv_validate():
        log("Spec validation failed.", "FAIL")

    # Optional endpoint check if URL provided
    if len(sys.argv) > 1:
        url = sys.argv[1].rstrip("/")
        if not check_endpoint(url):
            log("Dynamic deployment validation failed.", "FAIL")
    else:
        log("Skipping dynamic endpoint validation. Run with 'python scripts/validate.py <url>' to check Space.", "INFO")

    print("\n" + "="*50)
    print("  Validation Cycle Complete")
    print("="*50 + "\n")

if __name__ == "__main__":
    main()
