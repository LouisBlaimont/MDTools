FROM python:3.12-slim-bookworm

RUN apt-get update && apt-get install -y --no-install-recommends \
  bash tzdata coreutils \
  curl jq rsync \
  docker.io \
  gnupg ca-certificates lsb-release \
  restic \
  && rm -rf /var/lib/apt/lists/*

# Add PostgreSQL Apt Repository (PGDG) to get postgresql-client-17
RUN curl -fsSL https://www.postgresql.org/media/keys/ACCC4CF8.asc \
  | gpg --dearmor -o /usr/share/keyrings/postgresql.gpg \
  && echo "deb [signed-by=/usr/share/keyrings/postgresql.gpg] http://apt.postgresql.org/pub/repos/apt bookworm-pgdg main" \
  > /etc/apt/sources.list.d/pgdg.list \
  && apt-get update \
  && apt-get install -y --no-install-recommends postgresql-client-17 \
  && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY app.py /app/app.py
COPY scripts /scripts
RUN chmod +x /scripts/*.sh

EXPOSE 9000
CMD ["uvicorn", "app:app", "--host", "0.0.0.0", "--port", "9000"]
