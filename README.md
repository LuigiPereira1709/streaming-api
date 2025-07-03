# [Streaming API](http://d130vf1311tsqd.cloudfront.net/swagger-ui/index.html)
[Versão em Português](doc/README_pt.md)  
This project implements a cloudnative streaming API, designed to offer some operations like: upload, management and delivery of media files, specifically those based on audio like music and podcasts. Developed in Java using Spring Boot and reative paradigms, the API is native integrated with the AWS services like S3 and Cloudfront to provide a scalable and efficient solution for store and deliver media content. We use MongoDB for a better flexibility to store metadata of media files. The infrastructure include scripts for automate the build and deployment. 

## Features
- [x] **Upload and Management of media files**: Supports uploading and updating for audio files like music and podcasts, including metadata management(title, thumbnail file, etc).
- [x] **Secure Content Delivery**: Generated signed URLs for secure access to media files, ensuring that only authorized users can access the content.
- [x] **Scalable and Efficient**: Built on AWS services like S3 for storage and Cloudfront for content delivery, ensuring high availability and performance.
- [x] **Search and Filter**: Allows searching and filtering of media files based on metadata, enabling users to find content easily.
- [x] **Reactive Programming**: Utilizes Spring WebFlux for non-blocking, asynchronous processing, enhancing performance and scalability.
- [x] **Error Handling**: Implements comprehensive error handling and logging, ensuring robustness and ease of debugging.
- [x] **Unit and Integration Tests**: Includes tests to ensure the reliability and correctness of the API, with a focus on test-driven development.
- [x] **Blackbox Testing**: The API is designed to be tested independently of the underlying implementation, allowing for flexible and comprehensive testing strategies.
- [x] **Dockerized**: The application is containerized using Docker, making it easy to deploy and manage in various environments.
- [x] **Infrastructure as Code**: Includes scripts for building and deploying the application, ensuring a consistent and repeatable deployment process.
- [x] **AWS Integration**: Integrated with AWS services like S3 for storage, Cloudfront for content delivery, Secret Manager for secure environments variables and EC2 for hosting the application, providing a robust and scalable infrastructure.

## Technologies Used 
- Programming Language: Java 21 and [Bash](https://www.gnu.org/software/bash/)(for scripts)
- Framework: Spring Boot(3.x) and [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- Database: MongoDB
- AWS Services: [S3](https://aws.amazon.com/s3/), [Cloudfront](https://aws.amazon.com/cloudfront/), [Secret Manager](https://aws.amazon.com/secrets-manager/), [EC2](https://aws.amazon.com/ec2/)  
- Testing: [JUnit 5](https://junit.org/junit5/), [Mockito](https://site.mockito.org/), [Testcontainers](https://www.testcontainers.org/), [Hurl](https://hurl.dev/)
- Build and Deployment: [Gradle](https://gradle.org/), [Docker](https://www.docker.com/)

## Project Structure
```
.
├── black_box_tests/        # Blackbox tests for the API
├── doc/                    # Extra documentation (Blackbox tests, Scripts)
├── gradle/                 # Wrapper Gradle
├── scripts/                # Scripts for automated build and deployment
│   ├── ec2/                # Scripts for deployment on AWS EC2
│   └── build.sh            # Script for building the application 
└── src/
    ├── main/
    │   ├── java/com/pitanguinha/streaming/
    │   │   ├── annotation/     # Custom annotations 
    │   │   ├── config/         # Configuration classes 
    │   │   ├── controller/     # Endpoints for the API 
    │   │   ├── domain/         # Classes representing the domain model 
    │   │   ├── dto/            # Data Transfer Objects for API requests and responses 
    │   │   ├── enums/          # Enumerations used in the application 
    │   │   ├── exceptions/     # Custom exceptions for error handling 
    │   │   ├── mapper/         # Helper for mapping entity to dto and vice versa
    │   │   ├── repository/     # Repositories for data access 
    │   │   └── service/        # Business logic and service layer 
    │   └── resources/          # Application resources (application.properties, etc.) 
    └── test/                   # Unit and integration tests 
```
 
## Note (IAM)
To ensure the application has the necessary permissions to interact with AWS services, if you haven't already, see on the [main documentation](https://github.com/LuigiPereira1709/streaming-cloudnative-project) which IAM policies you need to create and attach to the EC2 instance role. This is crucial for the application to function correctly, especially when accessing S3, Cloudfront, and other AWS resources.

## Step-by-Step Guide
1. Clone the Repository
```bash
 git clone https://github.com/LuigiPereira1709/streaming-api
```
2. Settings ambient variables
- **Local development**: Create a `.env` following the example below:
```plaintext
## I'm at the root of the project ##
## Environment variables for deploy the api on the ec2 instance by ssh ##

# Remote environment variables
REMOTE_PATH=your_work_path (opt/**)
EC2_HOST=your_ec2_host

# Local environment variables
ENVIRONMENT_VARIABLES_SECRET_NAME=your_environment_secret_name (dev/api/environment)
SSH_KEY=your_path_ssh_key (~/.ssh/**.pem)

# Docker variables, if you want to use my image, don't change these variables
DOCKER_IMAGE=pitanguinhamarvada/streaming-backend-java 
DOCKER_TAG=latest 
```
- **SSM (AWS Secret Manager)**: Create a secret following the example below:
```json
{
  "BACKEND_CONTAINER_NAME":"backend-container",
  "BACKEND_IMAGE_NAME":"pitanguinhamarvada/streaming-backend-java",
  "BACKEND_IMAGE_TAG":"latest",
  "BACKEND_PORT":"8080",
  "BACKEND_CONTAINER_PORT":"8080",
  "MONGO_URI":"your_mongo_uri_with_credentials",
  "MONGO_DATABASE":"your_database_name",
  "S3_REGION":"your_region",
  "S3_BUCKET_NAME":"your_bucket_name",
  "CLOUDFRONT_DISTRIBUTION_ID":"your_distribution_id",
  "CLOUDFRONT_ENDPOINT":"your_endpoint",
  "CLOUDFRONT_KEY_PAIR_ID":"your_key_pair_id",
  "CLOUDFRONT_PRIVATE_KEY_NAME":"your_secret_private_key_name"
}
```
3. Build the Application with `make build` (Optional)
```bash 
# At the root of the project
make build
```
4. Deploy the Application with `make ssh-deploy`
```bash 
# At the root of the project
make ssh-deploy
```
5. Verify the `deploy.log` file
6. If already deployed, you can access the API at `http://your_ec2_public_dns:8080/swagger-ui/index.html`, this will show the Swagger UI for the API, allowing you to explore and test the endpoints.

## Future Improvements
- [ ] **Enhanced Security**: Implement additional security measures, such as OAuth2 or JWT for authentication and authorization.
- [ ] **Advanced Search and Filtering**: Implement more advanced search and filtering capabilities, maybe using Elasticsearch or similar technologies.
- [ ] **SQS and SNS Integration**: Integrate with AWS SQS and SNS for better message handling and notifications of media file uploads and conversions flags.
- [ ] **Support for Files with +100MB**: Implement support for larger files, ensuring efficient upload and processing of media files over 100 MB.
- [ ] **Monitoring and Logging**: Implement comprehensive monitoring and logging using AWS CloudWatch or similar services to track application performance and errors.
- [ ] **CI/CD Pipeline**: Set up a continuous integration and continuous deployment (CI/CD) pipeline using AWS CodePipeline or Github Actions to automate the build, test, and deployment processes. 
- [ ] **Create an User Interface 7-7**: Yeah, I'm not a great frontend dev, this gives me nightmares, but I will try to create a simple user interface for the API... SOME DAY :)... Or I'll create only a simple CLI to interact with the API? heheh, is a joke, or not? :D

## Links 
- [Blackbox Tests Doc](doc/blackbox_tests/blackbox_tests_doc_en.md)  
- [Scripts Doc](doc/scripts/scripts_doc_en.md)   
- [Local Env Example](doc/local_env_example)  
- [API Env Example](doc/api_env_example.json)   
- [Makefile](Makefile)
- [Main Doc](https://github.com/LuigiPereira1709/streaming-cloudnative-project)  
- [Access My API](http://d130vf1311tsqd.cloudfront.net/swagger-ui/index.html)

## Learning Goals
- [x] **Cloudnative Development**: Gain experience in developing cloudnative applications using AWS services.
- [x] **Reactive Programming**: Learn and apply reactive programming principles using Spring WebFlux.
- [x] **API Design and Development**: Improve skills in designing and developing RESTful APIs with Spring Boot.
- [x] **Testing Strategies**: Enhance understanding of testing strategies, including unit, integration, and blackbox testing.
- [x] **Infrastructure as Code**: Learn to automate deployment processes using scripts and Docker.
- [ ] **AWS SQS and SNS**: Learn to integrate AWS SQS and SNS for better message handling and notifications.
- [ ] **JWT and OAuth2**: Gain experience in implementing JWT and OAuth2 for secure authentication and authorization.
- [ ] **CI/CD Pipeline**: Learn to set up a CI/CD pipeline using AWS CodePipeline or Github Actions for automated build, test, and deployment processes.
- [ ] **User Interface Development**: Gain experience in developing a user interface for the API.

## License
This project is licensed under the GNU GPL v3.0. See the [LICENSE](LICENSE.txt) file for details.
