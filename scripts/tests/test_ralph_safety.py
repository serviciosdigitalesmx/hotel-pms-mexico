import importlib.util
import tempfile
import unittest
from pathlib import Path

spec = importlib.util.spec_from_file_location('ralph_safety', Path(__file__).parents[1] / 'ralph_safety.py')
safety = importlib.util.module_from_spec(spec)
spec.loader.exec_module(safety)


class MigrationSafetyTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name) / 'main'
        self.worker = Path(self.temp.name) / 'worker'

    def migration(self, tree, service, filename):
        path = tree / service / 'src/main/resources/db/migration' / filename
        path.parent.mkdir(parents=True, exist_ok=True)
        path.touch()

    def test_parallel_features_cannot_share_a_service_version(self):
        self.migration(self.root, 'frontdesk-service', 'V24__devices.sql')
        self.migration(self.worker, 'frontdesk-service', 'V24__workflow.sql')
        self.assertEqual(1, len(safety.migration_conflicts(self.root, self.worker)))

    def test_same_baseline_file_is_not_a_collision(self):
        for tree in (self.root, self.worker):
            self.migration(tree, 'frontdesk-service', 'V24__devices.sql')
        self.assertEqual([], safety.migration_conflicts(self.root, self.worker))

    def test_services_have_independent_version_namespaces(self):
        self.migration(self.root, 'guest-service', 'V24__devices.sql')
        self.migration(self.root, 'frontdesk-service', 'V24__workflow.sql')
        self.assertEqual([], safety.migration_conflicts(self.root))

    def test_flyway_equivalent_versions_collide(self):
        self.migration(self.root, 'frontdesk-service', 'V024__devices.sql')
        self.migration(self.root, 'frontdesk-service', 'V24_0__workflow.sql')
        self.assertEqual(1, len(safety.migration_conflicts(self.root)))

    def test_next_version_passes(self):
        self.migration(self.root, 'frontdesk-service', 'V24__devices.sql')
        self.migration(self.worker, 'frontdesk-service', 'V25__workflow.sql')
        self.assertEqual([], safety.migration_conflicts(self.root, self.worker))


if __name__ == '__main__':
    unittest.main()
