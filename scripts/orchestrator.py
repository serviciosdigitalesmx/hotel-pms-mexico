#!/usr/bin/env python3
"""Small stdlib-only state machine; assignment uses an exclusive lock file."""
import json, os, sys, time
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]; D=ROOT/'.agent-orchestration'; STATE=D/'state.json'; DAG=D/'dag.json'; LOCK=D/'state.lock'
def locked():
    try: fd=os.open(LOCK,os.O_CREAT|os.O_EXCL|os.O_WRONLY); os.close(fd); return True
    except FileExistsError: return False
def unlock(): LOCK.unlink(missing_ok=True)
def load(): return json.loads(STATE.read_text(encoding='utf8'))
def save(s): s['revision']+=1; s['updated_at']=time.strftime('%Y-%m-%dT%H:%M:%SZ',time.gmtime()); STATE.write_text(json.dumps(s,indent=2),encoding='utf8')
def bootstrap():
    dag=json.loads(DAG.read_text(encoding='utf8')); s={'schema_version':1,'revision':0,'updated_at':None,'tasks':{},'workers':{},'events':[]}
    for n in dag['nodes']: s['tasks'][n['id']]={'status':n['status'],'worker':None,'branch':None,'worktree':None,'attempt':0,'last_result':None,'integration_status':'PENDING','dependencies':n['dependencies']}
    save(s); print('BOOTSTRAPPED')
def status():
    s=load(); counts={}
    for t in s['tasks'].values(): counts[t['status']]=counts.get(t['status'],0)+1
    print('\n'.join(f'{k:12} {v}' for k,v in sorted(counts.items())))
def claim():
    if len(sys.argv)<4: raise SystemExit('claim TASK_ID WORKER')
    if not locked(): raise SystemExit('LOCKED: another orchestrator is assigning a task')
    try:
        s=load(); tid=sys.argv[2]; worker=sys.argv[3]; t=s['tasks'].get(tid)
        if not t: raise SystemExit('UNKNOWN_TASK')
        if t['status'] not in ('READY','FAILED'): raise SystemExit('NOT_CLAIMABLE:'+t['status'])
        t.update(status='RUNNING',worker=worker,attempt=t['attempt']+1,last_result=None)
        s['workers'].setdefault(worker,{})['task']=tid; s['events'].append({'event':'CLAIMED','task':tid,'worker':worker,'ts':time.time()}); save(s); print('CLAIMED',tid,worker)
    finally: unlock()
def main():
    if len(sys.argv)<2: print('bootstrap|status|start'); return 2
    if sys.argv[1]=='bootstrap': bootstrap()
    elif sys.argv[1]=='status': status()
    elif sys.argv[1]=='start': status(); print('Provider activation requires a verified external adapter; state is resumable.')
    elif sys.argv[1]=='claim': claim()
    else: return 2
if __name__=='__main__': sys.exit(main() or 0)
