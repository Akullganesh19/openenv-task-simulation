"""Run: KEYCLOAK_ISSUER=... ALLOWED_ROLES=student,client python3 -m integrations.policy.server"""
import json, os, time
from http.server import BaseHTTPRequestHandler, HTTPServer
from .middleware import PolicyConfig, PolicyViolation, authorize

class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path != "/authorize": self.send_error(404); return
        try:
            body=json.loads(self.rfile.read(int(self.headers.get("Content-Length", "0"))))
            issuer=os.environ.get("KEYCLOAK_ISSUER")
            roles=frozenset(filter(None, os.environ.get("ALLOWED_ROLES", "").split(",")))
            if not issuer or not roles: raise PolicyViolation("KEYCLOAK_ISSUER and ALLOWED_ROLES are required")
            authorize(body.get("claims", {}), body.get("request", {}), PolicyConfig(issuer, roles), int(time.time()))
            payload={"allowed": True}
            self.send_response(200)
        except (ValueError, TypeError, json.JSONDecodeError) as exc:
            payload={"allowed": False, "error": str(exc)}; self.send_response(403)
        self.send_header("Content-Type", "application/json"); self.end_headers(); self.wfile.write(json.dumps(payload).encode())
    def log_message(self, *_): pass

if __name__ == "__main__":
    HTTPServer((os.environ.get("POLICY_HOST", "127.0.0.1"), int(os.environ.get("POLICY_PORT", "8090"))), Handler).serve_forever()
