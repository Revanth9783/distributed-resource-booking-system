import csv
import json
import math
from collections import Counter
from pathlib import Path

root = Path(__file__).resolve().parent
summary = []
for path in sorted((root/'results').glob('*.jtl')):
    with path.open(encoding='utf-8-sig',newline='') as stream:
        rows = list(csv.DictReader(stream))
    if not rows:
        continue
    elapsed = sorted(int(row['elapsed']) for row in rows)
    first = min(int(row['timeStamp']) for row in rows)
    last = max(int(row['timeStamp'])+int(row['elapsed']) for row in rows)
    duration = (last-first)/1000
    summary.append({
        'run':path.stem,'samples':len(rows),'duration_s':round(duration,3),
        'throughput_per_s':round(len(rows)/duration,2),
        'mean_ms':round(sum(elapsed)/len(elapsed),2),
        'p95_ms':elapsed[math.ceil(.95*len(elapsed))-1],
        'p99_ms':elapsed[math.ceil(.99*len(elapsed))-1],
        'failed_samples':sum(row['success'].lower()!='true' for row in rows),
        'response_codes':dict(Counter(row['responseCode'] for row in rows)),
        'failure_messages':dict(Counter(row['responseMessage'] for row in rows if row['success'].lower()!='true')),
        'maximum_observed_active_threads':max(int(row.get('allThreads','0')) for row in rows)
    })
(root/'results-summary.json').write_text(json.dumps(summary,indent=2),encoding='utf-8')
print(json.dumps(summary,indent=2))
