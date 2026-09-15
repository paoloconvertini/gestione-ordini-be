#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
ENV_FILE="${ENV_FILE:-$ROOT_DIR/.env.local}"

if [[ ! -f "$ENV_FILE" ]]; then
  echo "Missing env file: $ENV_FILE" >&2
  echo "Create it from $ROOT_DIR/.env.example" >&2
  exit 1
fi

while IFS= read -r line || [[ -n "$line" ]]; do
  [[ -z "$line" || "$line" =~ ^[[:space:]]*# ]] && continue
  [[ "$line" != *=* ]] && continue

  key="${line%%=*}"
  value="${line#*=}"
  key="${key#"${key%%[![:space:]]*}"}"
  key="${key%"${key##*[![:space:]]}"}"
  value="${value#"${value%%[![:space:]]*}"}"
  value="${value%"${value##*[![:space:]]}"}"

  if [[ "$value" == \"*\" && "$value" == *\" ]]; then
    value="${value:1:${#value}-2}"
  fi

  export "$key=$value"
done < "$ENV_FILE"

if [[ -n "${JAVA21_HOME:-}" ]]; then
  export JAVA_HOME="$JAVA21_HOME"
elif command -v /usr/libexec/java_home >/dev/null 2>&1; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 21)"
else
  export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/graalvm-21.jdk/Contents/Home}"
fi

export PATH="$JAVA_HOME/bin:$PATH"

cd "$ROOT_DIR"
exec "$@"
