#!/usr/bin/env bash
set -euo pipefail

SECRET_NAME="mongo-keyfile"
NAMESPACE="${NAMESPACE:-default}"
KEY_FILE="./mongo-repl-key"
STATEFULSET_YAML="./statefulset.yaml"

# 1. Generate the key if it doesn't exist
if [[ ! -f "$KEY_FILE" ]]; then
  echo "Generating a new keyfile..."
  openssl rand -base64 752 | tr -d '\n' > "$KEY_FILE"
else
  echo "Using existing keyfile at $KEY_FILE"
fi

# 2. Create or update the Kubernetes Secret
if kubectl get secret "$SECRET_NAME" -n "$NAMESPACE" &>/dev/null; then
  echo "Updating Secret/$SECRET_NAME in namespace $NAMESPACE"
  kubectl delete secret "$SECRET_NAME" -n "$NAMESPACE"
fi

kubectl create secret generic "$SECRET_NAME" \
  --from-file=key="$KEY_FILE" \
  -n "$NAMESPACE"
echo "Secret/$SECRET_NAME created"

# 3. Apply your StatefulSet
echo "Deploying StatefulSet"
kubectl apply -f "$STATEFULSET_YAML" -n "$NAMESPACE"
echo "Deployment complete"