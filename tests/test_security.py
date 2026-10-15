import pytest
import os
import importlib
from pydantic import ValidationError

def test_secret_key_required(monkeypatch):
    monkeypatch.delenv("SECRET_KEY", raising=False)
    # The config module imports Settings and instantiates it globally.
    # Therefore, importing config will raise a ValidationError immediately
    # if SECRET_KEY is not set.
    with pytest.raises(ValidationError):
        import config
        importlib.reload(config)
