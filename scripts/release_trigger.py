"""Resolve explicit release markers for the master-branch release workflow."""

from __future__ import annotations

import argparse
import json
import os
from pathlib import Path
import re
import subprocess


MARKER_RE = re.compile(r"\[release(?:=(patch|minor|major))?\]", re.IGNORECASE)
CONVENTIONAL_RE = re.compile(r"^(?P<type>[a-z]+)(?:\([^)]*\))?(?P<breaking>!)?:", re.IGNORECASE)
RELEASE_BRANCH_PREFIX = "release-please--branches--master"


def marker_level(*values: str | None) -> str | None:
    """Return the requested bump level, defaulting to patch for [release]."""
    for value in values:
        if not value:
            continue
        match = MARKER_RE.search(value)
        if match:
            return (match.group(1) or "patch").lower()
    return None


def conventional_level(message: str) -> str | None:
    match = CONVENTIONAL_RE.match(message.strip())
    if not match:
        return None
    if match.group("breaking") or re.search(r"^BREAKING[ -]CHANGE:", message, re.MULTILINE):
        return "major"
    if match.group("type").lower() == "feat":
        return "minor"
    if match.group("type").lower() in {"fix", "perf", "refactor", "revert"}:
        return "patch"
    return None


def next_version(current: str, level: str) -> str:
    match = re.fullmatch(r"(\d+)\.(\d+)\.(\d+)", current.strip())
    if not match:
        raise ValueError(f"Invalid current version: {current}")
    major, minor, patch = map(int, match.groups())
    if level == "major":
        return f"{major + 1}.0.0"
    if level == "minor":
        return f"{major}.{minor + 1}.0"
    return f"{major}.{minor}.{patch + 1}"


def gh(*args: str) -> list[dict]:
    result = subprocess.run(["gh", *args], check=True, capture_output=True, text=True)
    return json.loads(result.stdout or "[]")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.parse_args()
    repository = os.environ["GITHUB_REPOSITORY"]
    sha = os.environ["GITHUB_SHA"]
    message = os.environ.get("HEAD_COMMIT_MESSAGE", "")
    pull_requests = gh("api", f"repos/{repository}/commits/{sha}/pulls")
    level = marker_level(
        message,
        *(pr.get("title") for pr in pull_requests),
        *(pr.get("body") for pr in pull_requests),
        *(pr.get("head", {}).get("ref") for pr in pull_requests),
    )
    pending = gh(
        "pr", "list", "--repo", repository, "--state", "open", "--limit", "100",
        "--json", "number,headRefName",
    )
    pending = [pr for pr in pending if pr["headRefName"].startswith(RELEASE_BRANCH_PREFIX)]
    if len(pending) > 1:
        raise RuntimeError("Multiple release PRs are open; close stale PRs before releasing.")

    release_as = ""
    if level and not pending:
        # Do not override the version of an already-prepared release PR.
        release_as = next_version(Path("version.txt").read_text(), conventional_level(message) or level)

    output = Path(os.environ["GITHUB_OUTPUT"])
    with output.open("a", encoding="utf-8") as handle:
        handle.write(f"auto={'true' if level else 'false'}\n")
        handle.write(f"release_as={release_as}\n")
        handle.write(f"pending_pr={pending[0]['number'] if pending else ''}\n")
    print(f"Explicit release: {bool(level)}; pending release PR: {pending[0]['number'] if pending else 'none'}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
