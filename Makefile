# Load environment variables
include .env
export $(shell sed 's/=.*//' .env)

SCRIPTS_DIR ?= ./scripts/ec2
.DEFAULT_GOAL := help

# Build the project jar file and the docker image of the project
.PHONY: build
build:
	@./scripts/build.sh

# Deploy the backend application via SSH
.PHONY: ssh-deploy
ssh-deploy:
	@ssh -i $(SSH_KEY) ec2-user@$(EC2_HOST) "\
		mkdir -p $(REMOTE_PATH)"
	@echo "Zipping deploy_scripts.zip from scripts/ec2..."
	@cd scripts/ec2 && zip -r ../../deploy_scripts.zip ./*
	@echo "Copying deploy_scripts.zip directly to EC2:$(REMOTE_PATH)..."
	@scp -i $(SSH_KEY) ./deploy_scripts.zip ec2-user@$(EC2_HOST):$(REMOTE_PATH)/
	@rm -f ./deploy_scripts.zip
	@echo "Running setup script remotely..."
	@ssh -i $(SSH_KEY) ec2-user@$(EC2_HOST) "\
		cd $(REMOTE_PATH) && \
		unzip -o deploy_scripts.zip && \
		chmod +x *.sh && \
		export ENVIRONMENT_VARIABLES_SECRET_NAME=$(ENVIRONMENT_VARIABLES_SECRET_NAME) && \
		./run_setup.sh | tee deploy.log" | tee deploy.log

# Sloth mode: build, push, and deploy
.PHONY: sloth
sloth: build ssh-deploy
	@echo "🦥 Yeah, I am a sloth!"

# Show available commands
.PHONY: help
help:
	@echo "Available commands:"
	@echo "  build         Build the project jar file and Docker image"
	@echo "  ssh-deploy    Deploy the backend application via SSH"
	@echo "  sloth         Build and deploy in one command (sloth mode)"
	@echo "  help          Show this help message"
