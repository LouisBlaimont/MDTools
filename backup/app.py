import os
import json
import uuid
import subprocess
from datetime import datetime
from typing import Dict, Any

from fastapi import FastAPI, HTTPException, Request, status
from pydantic import BaseModel
from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
from contextlib import asynccontextmanager

APP_PORT = 9000

JOBS_DIR = os.getenv("JOBS_DIR", "/backups/jobs")
LOGS_DIR = os.getenv("LOGS_DIR", "/backups/logs")

BACKUP_SERVICE_TOKEN = os.getenv("BACKUP_SERVICE_TOKEN", "")
BACKUP_RESTORE_PASSWORD = os.getenv("BACKUP_RESTORE_PASSWORD", "")

scheduler = BackgroundScheduler(timezone="Europe/Brussels")


def ensure_dirs() -> None:
  os.makedirs(JOBS_DIR, exist_ok=True)
  os.makedirs(LOGS_DIR, exist_ok=True)


def now_iso() -> str:
  return datetime.utcnow().isoformat() + "Z"


def job_path(job_id: str) -> str:
  return os.path.join(JOBS_DIR, f"{job_id}.json")


def log_path(job_id: str) -> str:
  return os.path.join(LOGS_DIR, f"{job_id}.log")


def write_job(job_id: str, data: Dict[str, Any]) -> None:
  with open(job_path(job_id), "w", encoding="utf-8") as f:
    json.dump(data, f, indent=2)


def read_job(job_id: str) -> Dict[str, Any]:
  p = job_path(job_id)
  if not os.path.exists(p):
    raise FileNotFoundError
  with open(p, "r", encoding="utf-8") as f:
    return json.load(f)


def check_token(request: Request) -> None:
  if not BACKUP_SERVICE_TOKEN:
    raise HTTPException(status_code=500, detail="BACKUP_SERVICE_TOKEN not set")
  token = request.headers.get("x-backup-token", "")
  if token != BACKUP_SERVICE_TOKEN:
    raise HTTPException(status_code=401, detail="Unauthorized")


def run_script_async(job_type: str, args: list[str]) -> str:
  job_id = str(uuid.uuid4())
  lp = log_path(job_id)

  meta = {
    "jobId": job_id,
    "type": job_type,
    "status": "running",
    "createdAt": now_iso(),
    "finishedAt": None,
    "exitCode": None,
    "args": args,
    "logFile": lp,
  }
  write_job(job_id, meta)

  with open(lp, "a", encoding="utf-8") as logf:
    logf.write(f"[INFO] Job started: {job_type} at {now_iso()}\n")
    logf.flush()

    proc = subprocess.Popen(
      args,
      stdout=logf,
      stderr=subprocess.STDOUT,
      env=os.environ.copy(),
    )

  def monitor():
    try:
      rc = proc.poll()
      if rc is None:
        return

      meta2 = read_job(job_id)
      meta2["status"] = "succeeded" if rc == 0 else "failed"
      meta2["finishedAt"] = now_iso()
      meta2["exitCode"] = rc
      write_job(job_id, meta2)

      with open(lp, "a", encoding="utf-8") as logf2:
        logf2.write(f"[INFO] Job finished with code={rc} at {now_iso()}\n")

      # stop polling once finished
      try:
        scheduler.remove_job(f"monitor-{job_id}")
      except Exception:
        pass

    except Exception:
      pass

  scheduler.add_job(monitor, "interval", seconds=2, id=f"monitor-{job_id}", replace_existing=True)
  return job_id


def run_backup(tag: str) -> None:
  run_script_async(f"backup_{tag}", ["/scripts/backup_tag.sh", tag])


class RestoreRequest(BaseModel):
  type: str  # daily|weekly
  password: str


@asynccontextmanager
async def lifespan(app: FastAPI):
  ensure_dirs()

  # Schedule backups:
  # Mon-Sat 00:00 => daily
  scheduler.add_job(
    lambda: run_backup("daily"),
    CronTrigger(day_of_week="mon-sat", hour=0, minute=0),
    id="daily-backup",
    replace_existing=True,
  )
  # Sun 00:00 => weekly
  scheduler.add_job(
    lambda: run_backup("weekly"),
    CronTrigger(day_of_week="sun", hour=0, minute=0),
    id="weekly-backup",
    replace_existing=True,
  )

  scheduler.start()
  try:
    yield
  finally:
    scheduler.shutdown(wait=False)


app = FastAPI(lifespan=lifespan)


@app.get("/internal/health")
def health(request: Request):
  check_token(request)
  return {"ok": True, "time": now_iso()}


@app.get("/internal/backups")
def backups(request: Request):
  check_token(request)
  try:
    out = subprocess.check_output(
      ["/scripts/list_backups.sh"],
      stderr=subprocess.STDOUT,
      env=os.environ.copy(),
      text=True,
    )
    return json.loads(out)
  except subprocess.CalledProcessError as e:
    raise HTTPException(status_code=500, detail=e.output)


@app.post("/internal/restore", status_code=status.HTTP_202_ACCEPTED)
def restore(request: Request, body: RestoreRequest):
  check_token(request)

  if not BACKUP_RESTORE_PASSWORD:
    raise HTTPException(status_code=500, detail="BACKUP_RESTORE_PASSWORD not set")

  if body.password != BACKUP_RESTORE_PASSWORD:
    raise HTTPException(status_code=403, detail="Invalid restore password")

  if body.type not in ("daily", "weekly"):
    raise HTTPException(status_code=400, detail="type must be daily or weekly")

  job_id = run_script_async(f"restore_{body.type}", ["/scripts/restore_tag.sh", body.type])
  return {"jobId": job_id}


@app.get("/internal/jobs/{job_id}")
def job_status(request: Request, job_id: str):
  check_token(request)
  try:
    return read_job(job_id)
  except FileNotFoundError:
    raise HTTPException(status_code=404, detail="Job not found")


@app.get("/internal/jobs/{job_id}/logs")
def job_logs(request: Request, job_id: str, offset: int = 0):
  check_token(request)
  lp = log_path(job_id)
  if not os.path.exists(lp):
    raise HTTPException(status_code=404, detail="Log not found")

  size = os.path.getsize(lp)
  offset = max(0, min(offset, size))

  with open(lp, "rb") as f:
    f.seek(offset)
    chunk = f.read(64 * 1024)

  content = chunk.decode("utf-8", errors="replace")

  try:
    meta = read_job(job_id)
  except FileNotFoundError:
    meta = None

  return {
    "offset": offset,
    "nextOffset": offset + len(chunk),
    "content": content,
    "job": meta,
  }
