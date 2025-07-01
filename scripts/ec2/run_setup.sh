#!/bin/bash
set -euo pipefail
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

echo "Starting EC2 setup..."

./setup_system.sh
if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Failed to set up the system. Exiting...${NC}"
  exit 1
fi
echo -e "${GREEN}✅ System setup completed successfully.${NC}"

source ./env_handler.sh

load_env_variables

echo "Deploying backend to EC2..."
./deploy_backend.sh
if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Failed to deploy the backend. Exiting...${NC}"
  exit 1
fi
echo -e "${GREEN}✅ Backend deployed successfully.${NC}"

echo "Excluding the .env file..."
exclude_env_file "$(dirname "$0")"

echo -e "${GREEN}✅ EC2 setup completed successfully.${NC}"
