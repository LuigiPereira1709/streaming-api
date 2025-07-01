#!/bin/bash
set -euo pipefail
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

# Logging (opcional)
exec > >(tee /var/log/bootstrap.log) 2>&1

# Verificação do sistema
if ! grep -q "amzn" /etc/os-release; then
  echo -e "${RED}❌ This script is intended for Amazon Linux only.${NC}"
  exit 1
fi

# Atualização do sistema
ACTUAL_SYS_VERSION=$(grep VERSION_ID /etc/os-release | cut -d '=' -f2 | tr -d '"')
echo "Updating system packages..."
sudo yum update -y
UPDATE_SYS_VERSION=$(grep VERSION_ID /etc/os-release | cut -d '=' -f2 | tr -d '"')
echo -e "${GREEN}System updated successfully. FROM: $ACTUAL_SYS_VERSION - TO: $UPDATE_SYS_VERSION${NC}"

# Instalação de pacotes
echo -e "${YELLOW}Installing required packages: jq, aws-cli, amazon-ssm-agent...${NC}"
sudo yum install -y jq aws-cli amazon-ssm-agent
echo -e "${GREEN}Required packages installed successfully.${NC}"

# Iniciar e habilitar o SSM Agent
echo -e "${YELLOW}Enabling and starting amazon-ssm-agent...${NC}"
sudo systemctl enable amazon-ssm-agent
sudo systemctl start amazon-ssm-agent
echo -e "${GREEN}amazon-ssm-agent is running.${NC}"

# Docker
echo -e "${YELLOW}Checking for Docker installation...${NC}"
if ! command -v docker &>/dev/null; then
  echo -e "${YELLOW}Docker is not installed. Installing Docker...${NC}"
  sudo yum install -y docker
  sudo systemctl start docker
  sudo systemctl enable docker
  sudo usermod -aG docker $USER
  exec sg docker newgrp docker
  echo -e "${YELLOW}⚠️ You must log out and log back in to use Docker without sudo.${NC}"
  echo -e "${GREEN}Docker installed successfully.${NC}"
else
  ACTUAL_DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
  echo -e "${YELLOW}Docker is already installed. Current version: $ACTUAL_DOCKER_VERSION${NC}"
  sudo yum upgrade -y docker
  UPDATE_DOCKER_VERSION=$(docker --version | awk '{print $3}' | sed 's/,//')
  echo -e "${GREEN}Docker upgraded successfully. FROM: $ACTUAL_DOCKER_VERSION - TO: $UPDATE_DOCKER_VERSION${NC}"
fi
