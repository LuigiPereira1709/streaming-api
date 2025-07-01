# Load environment variables
set -euo pipefail
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

load_env_variables() {
  echo "Loading environment variables from secret: $ENVIRONMENT_VARIABLES_SECRET_NAME"

  if ! aws secretsmanager get-secret-value \
    --secret-id "$ENVIRONMENT_VARIABLES_SECRET_NAME" \
    --query SecretString \
    --output text |
    jq -r 'to_entries | .[] | "export \(.key)=\"\(.value)\""' |
    tee ./.env >/dev/null; then
    echo -e "${RED}❌ Failed to load environment variables from AWS Secrets Manager.${NC}"
    exit 1
  fi
  echo -e "${GREEN}✅ Environment variables loaded successfully.${NC}"
}

# Exclude the .env file
exclude_env_file() {
  local env_file="${1}/.env"

  if [ -f "$env_file" ]; then
    echo ".env file found at $env_file. Removing it."
    rm -f "$env_file"
    if [ $? -ne 0 ]; then
      echo -e "${RED}❌ Failed to remove .env file at $env_file.${NC}"
      exit 1
    fi
    echo -e "${GREEN}✅ .env file removed successfully.${NC}"
  else
    echo -e "${YELLOW}⚠️ No .env file found in ${scripts_dir}. Skipping removal.${NC}"
  fi
}
