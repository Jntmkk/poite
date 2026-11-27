import unittest

from release_trigger import conventional_level, marker_level, next_version, release_message


class ReleaseTriggerTest(unittest.TestCase):
    def test_marker_locations_and_levels(self):
        self.assertEqual(marker_level("fix: repair cache [release]"), "patch")
        self.assertEqual(marker_level(None, "docs: publish", "[release=minor]"), "minor")
        self.assertEqual(marker_level("docs: publish", None, "feat/release-mode[release=major]"), "major")
        self.assertIsNone(marker_level("fix: normal change", "feature/release-notes"))

    def test_starred_branch_is_default_release_trigger(self):
        self.assertEqual(marker_level("feat: normal change", None, "feat/new-screen-*"), "patch")
        self.assertEqual(marker_level("fix*: repair cache"), "patch")
        self.assertEqual(marker_level("feat*: add screen"), "patch")
        self.assertEqual(marker_level(None, None, "feat/new-screen-*[release=minor]"), "minor")

    def test_conventional_commit_takes_precedence(self):
        self.assertEqual(conventional_level("feat: add page [release]"), "minor")
        self.assertEqual(conventional_level("fix!: migrate settings [release]"), "major")
        self.assertEqual(conventional_level("docs: guide\n\nBREAKING CHANGE: config"), "major")
        self.assertIsNone(conventional_level("docs: guide [release]"))
        self.assertEqual(release_message("fix*: repair cache"), "fix: repair cache")
        self.assertEqual(release_message("feat*: add screen"), "feat: add screen")

    def test_fallback_bumps_patch(self):
        self.assertEqual(next_version("1.2.3\n", "patch"), "1.2.4")
        self.assertEqual(next_version("1.2.3", "minor"), "1.3.0")
        self.assertEqual(next_version("1.2.3", "major"), "2.0.0")
        with self.assertRaises(ValueError):
            next_version("1.2", "patch")


if __name__ == "__main__":
    unittest.main()
