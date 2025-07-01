#!/bin/bash
set -euo pipefail
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[0;33m'
NC='\033[0m'

JAR_NAME="$(./gradlew jarName | grep '\.jar' | tail -n1)"

# Verify that the JAR version is the correct
echo -e "${YELLOW}Please ensure that the '${JAR_NAME}' is the correct version. (Y|N)${NC}"
read -r confirmation
if [[ "$confirmation" != "y" && "$confirmation" != "Y" ]]; then
  echo -e "${RED}❌ Build aborted by user.${NC}"
  exit 1
fi

echo -e "${YELLOW}Using JAR: $JAR_NAME${NC}"

# Running the tests
echo -e "${YELLOW}Running tests...${NC}"
./gradlew clean --refresh-dependencies test
if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Tests failed. Please check the output above for details.${NC}"
  exit 1
fi
echo -e "${GREEN}✅ Tests completed successfully!${NC}\n"

# Build the JAR file
echo -e "${YELLOW}Building JAR...${NC}"
./gradlew clean bootJar
if [ $? -ne 0 ]; then
  echo -e "${RED}❌ JAR build failed. Please check the output above for details.${NC}"
  exit 1
fi

if [ ! -f build/libs/$JAR_NAME ]; then
  echo -e "${RED}❌ JAR file $JAR_NAME not found in build/libs. Please check the build process.${NC}"
  exit 1
fi
echo -e "${GREEN}✅ JAR file $JAR_NAME built successfully!${NC}\n"

# Build and Push the docker image
echo -e "${YELLOW}Building Docker image...${NC}"
docker buildx build \
  --platform linux/amd64,linux/arm64 \
  --build-arg JAR_NAME=$JAR_NAME \
  -t $DOCKER_IMAGE:$DOCKER_TAG --push .

if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Docker image build failed. Please check the output above for details.${NC}"
  exit 1
fi
echo -e "${GREEN}✅ Docker image $DOCKER_IMAGE:$DOCKER_TAG built successfully!${NC}"
