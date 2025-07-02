# Scripts Doc
[Versão em Português](scripts_doc_pt.md)  
Scripts are used on this project to automate task execution, such as building the project and deploying the API to the EC2 instance. These scripts are written in shell and are used on makefile to execute the tasks.

## Available Scripts
- **`make build`**: Builds the project and push the image to the Docker repository.
### Step by Step of `make build`
1. Run all the tests to ensure that the project is working correctly.
2. Build the jar file using the `./gradlew bootJar` command.
3. Build a multiplatform image using the `docker buildx build` command.
4. Push the image to the Docker repository using the `docker push` command.
Note: Requires execute `docker login` before running the script to authenticate with the Docker Hub.

- **`make ssh-deploy`**: Deploys the API to the EC2 instance using SSH.
### Step by Step of `make ssh-deploy`
1. SSH into the EC2 instance using the `ssh` command to create the remote directory.
2. Zip the `scripts/ec2` files and copy them to the EC2 instance using the `scp` command.
3. SSH into the EC2 instance again to execute the deployment script.
4. Unzip the files, set the permissions and set up environment variables secret name.
5. Run `run_setup.sh` to setup API and start the service on the EC2 instance using a Docker container.
#### Pre-requisites
- **SSH Access**: Ensure you have SSH access to the EC2 instance.
- **Docker image exists**: Ensure that the Docker image exists in the Docker repository before running the script.
- **Valid .env file**: Ensure that the `.env` file is valid and contains the necessary environment variables for the deployment.
Example of `.env` file:
```plaintext
# Remote environment variables
REMOTE_PATH=your_work_path (opt/**)
EC2_HOST=your_ec2_host

# Local environment variables
ENVIRONMENT_VARIABLES_SECRET_NAME=your_environment_secret_name (dev/api/environment)
SSH_KEY=your_path_ssh_key (~/.ssh/**.pem)

# Docker variables, here is my image if you want to use it don't change
DOCKER_IMAGE=pitanguinhamarvada/streaming-backend-java 
DOCKER_TAG=latest 
```

- **`make sloth`**: Runs the sloth mode to execute all the tasks to deploy the API to the EC2 instance.
Note: Yeah, I'm lazy, so I created this script to run all the tasks in one command. It runs the `make build` and `make ssh-deploy` commands in sequence.

## Expected Outcomes
- [x] Build of the project with all tests passing.
- [x] Build the Docker image for multiplatform and push it to the Docker repository.
- [x] Automate the deployment of the API to the EC2 instance on a docker container, using SSH to connect to the instance and execute the necessary commands. 
- [x] Ensure that the API is running correctly on the EC2 instance after deployment.

## Future Improvements
- [ ] **Change SSH to SSM**: Use AWS Systems Manager (SSM) to connect to the EC2 instance instead of SSH, which provides a more secure and manageable way to access the instance.
- [ ] **Write scripts for integration tests**: Create scripts to run integration tests after the deployment to ensure that the API is working correctly in the production environment.
- [ ] **Write scripts for up an EC2 instance**: Create scripts to automate the process of setting up an EC2 instance, including installing necessary software and configuring the environment.

## Links
- [SSM doc](https://docs.aws.amazon.com/systems-manager/latest/userguide/what-is-systems-manager.html)  
- [Scripts folder](../../scripts)  
- [Makefile](../../Makefile)  
- [Back to the main doc](../../README.md)  

## Learning Goals?
- [ ] **SSM**: Learn how to use AWS Systems Manager (SSM) to connect to the EC2 instance and execute commands securely.
- [ ] **EC2 instance setup by cli**: Learn how to automate the process of setting up an EC2 instance using scripts, including installing necessary software and configuring the environment.
- [ ] **How implement integration tests**: Find a way to write integration tests in AWS cloud native applications. 
