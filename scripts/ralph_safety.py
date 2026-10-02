"""Read-only safety checks for a candidate integration (no third-party dependencies)."""
import argparse
import json
import re
from pathlib import Path


def migration_conflicts(root, overlay=None):
    """Check versions per service, including files a worker would add to main."""
    files = {}
    for tree in (root, overlay):
        if tree is None:
            continue
        for file in Path(tree).glob('*/src/main/resources/db/migration/V*.sql'):
            files[file.relative_to(tree).as_posix()] = file
    versions = {}
    conflicts = []
    for name in sorted(files):
        match = re.fullmatch(r'V([0-9][0-9._]*)__.+\.sql', Path(name).name)
        if not match:
            conflicts.append('INVALID_MIGRATION_NAME: ' + name)
            continue
        version = tuple(int(part) for part in re.split(r'[._]', match[1]))
        while len(version) > 1 and version[-1] == 0:
            version = version[:-1]
        key = (str(Path(name).parent), version)
        if key in versions:
            conflicts.append('DUPLICATE_MIGRATION: ' + versions[key] + ' / ' + name)
        else:
            versions[key] = name
    return conflicts


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--root', type=Path, required=True)
    parser.add_argument('--overlay', type=Path)
    args = parser.parse_args()
    if not (args.root / 'settings.gradle.kts').is_file():
        parser.error('root must be the Camra checkout')
    if args.overlay and not (args.overlay / 'settings.gradle.kts').is_file():
        parser.error('overlay must be a worker checkout')
    errors = migration_conflicts(args.root, args.overlay)
    print(json.dumps({'status': 'FAILED' if errors else 'PASSED', 'errors': errors}))
    return bool(errors)


if __name__ == '__main__':
    raise SystemExit(main())
