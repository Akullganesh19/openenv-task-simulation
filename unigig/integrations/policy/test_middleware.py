import unittest
from middleware import PolicyConfig, PolicyViolation, authorize
class PolicyTests(unittest.TestCase):
 def setUp(self): self.c=PolicyConfig('https://keycloak.example/realms/unigig',frozenset({'student'})); self.claims={'iss':self.c.keycloak_issuer,'sub':'u1','preferred_username':'u','realm_access':{'roles':['student']},'exp':2000}
 def test_allows_scoped_non_money(self): authorize(self.claims,{'secretRefs':['OPENBAO_REF:secret/data/unigig/stripe']},self.c,1000)
 def test_rejects_wrong_issuer(self):
  with self.assertRaises(PolicyViolation): authorize({**self.claims,'iss':'https://evil'}, {}, self.c,1000)
 def test_rejects_unscoped_secret(self):
  with self.assertRaises(PolicyViolation): authorize(self.claims,{'secretRefs':['OPENBAO_REF:secret/data/other/x']},self.c,1000)
 def test_rejects_non_blnk_money(self):
  with self.assertRaises(PolicyViolation): authorize(self.claims,{'moneyOperation':True,'ledgerProvider':'stripe','idempotencyKey':'x'},self.c,1000)
 def test_requires_claims(self):
  with self.assertRaises(PolicyViolation): authorize({**self.claims,'preferred_username':''},{},self.c,1000)
if __name__=='__main__': unittest.main()
