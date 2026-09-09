from server.enhanced_api import get_password_hash, verify_password

def test_password_hashing():
    password = "supersecretpassword123!"
    hashed = get_password_hash(password)
    assert verify_password(password, hashed)
    assert not verify_password("wrongpassword", hashed)
    # Check if argon2 is in the hash
    assert "argon2" in hashed
