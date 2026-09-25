#!/usr/bin/env bash
set -euo pipefail

BE_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
API_POM="$BE_ROOT/gestione-ordini-api/pom.xml"
PUSH=true
DRY_RUN=false

for arg in "$@"; do
  case "$arg" in
    --no-push) PUSH=false ;;
    --dry-run) DRY_RUN=true ;;
    *)
      echo "Uso: $0 [--no-push] [--dry-run]" >&2
      exit 2
      ;;
  esac
done

if [[ ! -f "$API_POM" ]]; then
  echo "POM API non trovato: $API_POM" >&2
  exit 1
fi

current_version="$(awk '
  /<artifactId>gestione-ordini-api<\/artifactId>/ { in_api=1; next }
  in_api && /<version>[0-9]+\.[0-9]+\.[0-9]+<\/version>/ {
    line=$0
    sub(/^.*<version>/, "", line)
    sub(/<\/version>.*$/, "", line)
    print line
    exit
  }
' "$API_POM")"

if [[ ! "$current_version" =~ ^([0-9]+)\.([0-9]+)\.([0-9]+)$ ]]; then
  echo "Versione API non valida o non trovata: '$current_version'" >&2
  exit 1
fi

major="${BASH_REMATCH[1]}"
minor="${BASH_REMATCH[2]}"
patch="${BASH_REMATCH[3]}"
next_version="$major.$minor.$((patch + 1))"

echo "Versione API: $current_version -> $next_version"

if [[ "$DRY_RUN" == true ]]; then
  exit 0
fi

tmp_pom="$(mktemp "${TMPDIR:-/tmp}/gestione-ordini-api-pom.XXXXXX")"
trap 'rm -f "$tmp_pom"' EXIT

awk -v next_version="$next_version" '
  /<artifactId>gestione-ordini-api<\/artifactId>/ { in_api=1; print; next }
  in_api && !updated && /<version>[0-9]+\.[0-9]+\.[0-9]+<\/version>/ {
    sub(/<version>[0-9]+\.[0-9]+\.[0-9]+<\/version>/,
        "<version>" next_version "</version>")
    updated=1
  }
  { print }
' "$API_POM" > "$tmp_pom"

if ! grep -q "<version>$next_version</version>" "$tmp_pom"; then
  echo "Impossibile aggiornare la versione nel POM" >&2
  exit 1
fi

mv "$tmp_pom" "$API_POM"
trap - EXIT

maven_args=(clean package)
if [[ "$PUSH" == false ]]; then
  maven_args+=("-Dquarkus.container-image.push=false")
fi

cd "$BE_ROOT"
exec "$BE_ROOT/scripts/run-with-env.sh" mvn -f "$API_POM" "${maven_args[@]}"
