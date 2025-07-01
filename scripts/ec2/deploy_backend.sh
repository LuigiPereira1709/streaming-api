#!/bin/bash
set -eo pipefail
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m' # No Color

# Load environment variables
set -a
source ./.env
set +a

# Remove existing container
CONTAINER_NAME=$BACKEND_CONTAINER_NAME
if [ "$(docker ps -aq -f name=$CONTAINER_NAME)" ]; then
  echo -e "${YELLOW}⚠️ Existing container $CONTAINER_NAME found. Removing it...${NC}"
  docker stop $CONTAINER_NAME || true
  docker rm $CONTAINER_NAME
  if [ $? -ne 0 ]; then
    echo -e "${RED}❌ Failed to remove existing container $CONTAINER_NAME.${NC}"
    exit 1
  fi
  echo -e "${GREEN}✅ Existing container $CONTAINER_NAME removed successfully.${NC}"
fi

# Pull and run container
echo "Pulling Docker image: $BACKEND_IMAGE_NAME:$BACKEND_IMAGE_TAG"
docker pull $BACKEND_IMAGE_NAME:$BACKEND_IMAGE_TAG
if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Failed to pull Docker image $BACKEND_IMAGE_NAME:$BACKEND_IMAGE_TAG.${NC}"
  exit 1
fi
echo -e "${GREEN}✅ Docker image $BACKEND_IMAGE_NAME:$BACKEND_IMAGE_TAG pulled successfully.${NC}"

echo "Running Docker container: $CONTAINER_NAME"
docker run -d \
  --name $CONTAINER_NAME \
  -p $BACKEND_PORT:$BACKEND_CONTAINER_PORT \
  -e "MONGO_URI=$MONGO_URI" \
  -e "MONGO_DATABASE=$MONGO_DATABASE" \
  -e "S3_REGION=$S3_REGION" \
  -e "S3_BUCKET_NAME=$S3_BUCKET_NAME" \
  -e "CLOUDFRONT_DISTRIBUTION_ID=$CLOUDFRONT_DISTRIBUTION_ID" \
  -e "CLOUDFRONT_ENDPOINT=$CLOUDFRONT_ENDPOINT" \
  -e "CLOUDFRONT_KEY_PAIR_ID=$CLOUDFRONT_KEY_PAIR_ID" \
  -e "CLOUDFRONT_PRIVATE_KEY_NAME=$CLOUDFRONT_PRIVATE_KEY_NAME" \
  $BACKEND_IMAGE_NAME:$BACKEND_IMAGE_TAG

# Health check
echo "Waiting for backend service to be healthy..."
timeout 30 bash -c "
until curl -s -o /dev/null -w '%{http_code}' http://localhost:$BACKEND_PORT/actuator/health | grep -q '200'; do
    echo 'Waiting for backend service to be healthy...'
    sleep 5
done
"
if [ $? -ne 0 ]; then
  echo -e "${RED}❌ Backend service did not become healthy within the timeout period.${NC}"
  exit 1
fi

echo -e "${GREEN}✅ Backend service is healthy.${NC}"

echo "${YELLOW}⚠️ Last 50 lines of logs from the backend container:${NC}"
sudo docker logs $CONTAINER_NAME --tail 50
