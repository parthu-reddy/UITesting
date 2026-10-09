"""One live E2E run at a time, shared with e2e-plan/_handoff/tools/run_e2e_batch.py.

Every live run signs in seeded people and Identity keeps at most three sessions per person, so two runs
evict each other's sessions; run_e2e_batch.py enforces that with _handoff/RUN-IN-PROGRESS ("<pid> <time>").
These fresh-person runners call mvn themselves, so they take the same lock, and refuse while the owner's
deploy is in progress (_handoff/DEPLOY-IN-PROGRESS), exactly like run_e2e_batch.py.
"""
import contextlib
import os
from pathlib import Path
import time


@contextlib.contextmanager
def held(root):
    handoff = Path(root) / 'e2e-plan' / '_handoff'
    if (handoff / 'DEPLOY-IN-PROGRESS').exists():
        raise SystemExit('REFUSED: the owner is deploying (_handoff/DEPLOY-IN-PROGRESS); run nothing until they say deployed')
    lock = handoff / 'RUN-IN-PROGRESS'
    if lock.exists():
        try:
            pid = int(lock.read_text().split()[0])
            os.kill(pid, 0)
            raise SystemExit(f'REFUSED: another live run (pid {pid}) holds {lock}; seeded sessions would collide')
        except (ValueError, IndexError, ProcessLookupError):
            pass  # stale lock from a run that died
    lock.parent.mkdir(parents=True, exist_ok=True)
    mine = f'{os.getpid()} {time.strftime("%Y-%m-%dT%H:%M:%S")}\n'
    lock.write_text(mine)
    try:
        yield
    finally:
        if lock.exists() and lock.read_text() == mine:
            lock.unlink()
