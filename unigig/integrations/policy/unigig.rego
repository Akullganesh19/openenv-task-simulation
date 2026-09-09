package unigig.integration

# Backend services must pass these decisions before invoking an adapter.
default allow := false

allow if {
  input.token.iss == input.config.keycloak_issuer
  input.token.exp > input.now
  input.token.sub
  input.token.preferred_username
  input.token.realm_access.roles[_] in input.config.allowed_roles
  every ref in input.request.secretRefs {
    startswith(ref, "OPENBAO_REF:secret/data/unigig/")
  }
  not input.request.moneyOperation
}

# Money operations have one authority and cannot be delegated to a tool-local balance.
ledger_only if {
  input.request.moneyOperation
  input.request.ledgerProvider == "blnk"
  input.request.idempotencyKey
}
