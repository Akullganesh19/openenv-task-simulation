"""Executable backend authorization boundary for UniGig adapters.

Kong must verify the JWT signature before forwarding verified claims here. This
small dependency-free component then enforces issuer, identity/role claims,
OpenBao reference scope, and Blnk-only money operations.
"""
import re
from dataclasses import dataclass

_OPENBAO_REF = re.compile(r"^OPENBAO_REF:secret/data/unigig/[a-z0-9][a-z0-9/_-]*$")

@dataclass(frozen=True)
class PolicyConfig:
    keycloak_issuer: str
    allowed_roles: frozenset[str]

class PolicyViolation(ValueError):
    pass

def authorize(claims: dict, request: dict, config: PolicyConfig, now: int) -> None:
    """Raise PolicyViolation unless a verified request may reach an adapter."""
    if claims.get("iss") != config.keycloak_issuer:
        raise PolicyViolation("Keycloak issuer is not configured for this boundary")
    if not claims.get("sub") or not claims.get("preferred_username"):
        raise PolicyViolation("Keycloak sub and preferred_username claims are required")
    roles = set(claims.get("realm_access", {}).get("roles", []))
    if not roles.intersection(config.allowed_roles):
        raise PolicyViolation("no allowed Keycloak realm role")
    if int(claims.get("exp", 0)) <= now:
        raise PolicyViolation("Keycloak token is expired")
    for ref in request.get("secretRefs", []):
        if not isinstance(ref, str) or not _OPENBAO_REF.fullmatch(ref):
            raise PolicyViolation("secretRefs must be scoped OpenBao references")
    if request.get("moneyOperation"):
        if request.get("ledgerProvider") != "blnk":
            raise PolicyViolation("money operations must use Blnk")
        if not request.get("idempotencyKey"):
            raise PolicyViolation("money operations require idempotencyKey")
