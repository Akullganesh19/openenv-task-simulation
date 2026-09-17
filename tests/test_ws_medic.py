from fastapi.testclient import TestClient
from server.app import app
import json

def test_websocket_malformed_json_resilience():
    client = TestClient(app)
    with client.websocket_connect("/ws") as websocket:
        # Initial connect message
        start_msg = websocket.receive_json()
        assert start_msg["type"] == "session_start"

        # Send bad json
        websocket.send_text("NOT A JSON")

        # Should gracefully error
        err_msg = websocket.receive_json()
        assert err_msg["type"] == "error"
        assert err_msg["message"] == "Invalid JSON format"

        # Connection should still be alive, let's ask for state
        websocket.send_json({"type": "state"})

        # Expect state response
        state_msg = websocket.receive_json()
        assert state_msg["type"] == "state"
        assert "state" in state_msg
