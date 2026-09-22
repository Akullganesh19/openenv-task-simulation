import pytest
from fastapi.testclient import TestClient
from server.app import app

client = TestClient(app)

def test_leaderboard_limit_valid():
    response = client.get("/api/v1/enhanced/leaderboard?limit=5")
    assert response.status_code == 200
    data = response.json()
    assert "leaderboard" in data

def test_leaderboard_limit_negative():
    response = client.get("/api/v1/enhanced/leaderboard?limit=-1")
    assert response.status_code == 422 # Unprocessable Entity due to validation error

def test_leaderboard_limit_too_large():
    response = client.get("/api/v1/enhanced/leaderboard?limit=101")
    assert response.status_code == 422
