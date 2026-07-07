#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$ROOT_DIR"

echo "Bootstrapping Bank Transaction System with Docker Compose..."
docker compose up --build -d

cat <<'EOF'

Stack is starting.

  Login:        http://localhost:8083/login
  Home:         http://localhost:8083/home
  Transactions: http://localhost:8083/transactions
  Analytics:    http://localhost:8090/docs

Default credentials:
  admin / admin123   (administrator)
  admin1 / admin123  (employee)

View logs: docker compose logs -f bank-app
EOF
