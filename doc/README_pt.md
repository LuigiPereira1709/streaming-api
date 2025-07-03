# [Streaming API](http://d130vf1311tsqd.cloudfront.net/swagger-ui/index.html)   
[English Version](../README.md)  
Esse projeto implementa uma API de streaming cloudnative, projetada para oferecer operações como: upload, gerenciamento e entrega de arquivos de mídia, especificamente aqueles baseados em áudio, como músicas e podcasts. Desenvolvida em Java usando Spring Boot e paradigmas reativos, a API é integrada nativamente com os serviços da AWS, como S3 e Cloudfront, para fornecer uma solução escalável e eficiente para armazenar e entregar conteúdo de mídia. Usamos MongoDB para melhor flexibilidade no armazenamento de metadados dos arquivos de mídia. A infraestrutura inclui scripts para automatizar a construção e o deployment.

## Funcionalidades
- [x] **Upload e Gerenciamento de arquivos de mídia**: Suporta upload e atualização de arquivos de áudio, como músicas e podcasts, incluindo gerenciamento de metadados (título, arquivo de miniatura, etc).
- [x] **Entrega Segura de Conteúdo**: URLs assinadas geradas para acesso seguro aos arquivos de mídia, garantindo que apenas usuários autorizados possam acessar o conteúdo.
- [x] **Escalável e Eficiente**: Construído sobre os serviços da AWS, como S3 para armazenamento e Cloudfront para entrega de conteúdo, garantindo alta disponibilidade e desempenho.
- [x] **Pesquisa e Filtro**: Permite pesquisar e filtrar arquivos de mídia com base em metadados, facilitando a localização de conteúdo.
- [x] **Programação Reativa**: Utiliza Spring WebFlux para processamento assíncrono e não bloqueante, melhorando o desempenho e a escalabilidade.
- [x] **Tratamento de Erros**: Implementa tratamento de erros abrangente e registro de logs, garantindo robustez e facilidade de depuração.
- [x] **Testes Unitários e de Integração**: Inclui testes para garantir a confiabilidade e correção da API, com foco no desenvolvimento orientado a testes.
- [x] **Testes Blackbox**: A API é projetada para ser testada independentemente da implementação subjacente, permitindo estratégias de teste flexíveis e abrangentes.
- [x] **Dockerizado**: A aplicação é containerizada usando Docker, facilitando o deployment e gerenciamento em vários ambientes.
- [x] **Infraestrutura como Código**: Inclui scripts para construção e deployment da aplicação, garantindo um processo de deployment consistente e repetível.
- [x] **Integração com AWS**: Integrada com serviços da AWS, como S3 para armazenamento, Cloudfront para entrega de conteúdo, Secret Manager para variáveis de ambiente seguras e EC2 para hospedagem da aplicação, fornecendo uma infraestrutura robusta e escalável.

## Tecnologias Utilizadas
- Linguagem de Programação: Java 21 e [Bash](https://www.gnu.org/software/bash/) (para scripts)
- Framework: Spring Boot (3.x) e [Spring WebFlux](https://docs.spring.io/spring-framework/reference/web/webflux.html)
- Banco de Dados: MongoDB
- Serviços AWS: [S3](https://aws.amazon.com/s3/), [Cloudfront](https://aws.amazon.com/cloudfront/), [Secret Manager](https://aws.amazon.com/secrets-manager/), [EC2](https://aws.amazon.com/ec2/)
- Testes: [JUnit 5](https://junit.org/junit5/), [Mockito](https://site.mockito.org/), [Testcontainers](https://www.testcontainers.org/), [Hurl](https://hurl.dev/)
- Build e Deployment: [Gradle](https://gradle.org/), [Docker](https://www.docker.com/)

## Estrutura do Projeto
```plaintext
.
├── black_box_tests/        # Testes de caixa preta para a API
├── doc/                    # Documentação extra (Testes de caixa preta, Scripts)
├── gradle/                 # Wrapper Gradle
├── scripts/                # Scripts para construção e deployment automatizado
│   ├── ec2/                # Scripts para deployment na AWS EC2
│   └── build.sh            # Script para construir a aplicação
└── src/
    ├── main/
    │   ├── java/com/pitanguinha/streaming/
    │   │   ├── annotation/     # Anotações personalizadas
    │   │   ├── config/         # Classes de configuração
    │   │   ├── controller/     # Endpoints da API
    │   │   ├── domain/         # Classes representando o modelo de domínio
    │   │   ├── dto/            # Objetos de Transferência de Dados para requisições e respostas da API
    │   │   ├── enums/          # Enumerações usadas na aplicação
    │   │   ├── exceptions/     # Exceções personalizadas para tratamento de erros
    │   │   ├── mapper/         # Auxiliar para mapear entidade para dto e vice-versa
    │   │   ├── repository/     # Repositórios para acesso a dados
    │   │   └── service/        # Lógica de negócios e camada de serviço
    │   └── resources/          # Recursos da aplicação (application.properties, etc.)
    └── test/                   # Testes unitários e de integração
```

## Nota (IAM)
Para garantir que a aplicação tenha as permissões necessárias para interagir com os serviços da AWS, se você ainda não fez isso, veja na [documentação principal](https://github.com/LuigiPereira1709/streaming-cloudnative-project/blob/main/doc/README_pt.md) quais políticas IAM você precisa criar e anexar ao papel da instância EC2. Isso é crucial para o funcionamento correto da aplicação, especialmente ao acessar S3, Cloudfront e outros recursos da AWS.

## Guia Passo a Passo
1. Clonar o Repositório
 ```bash
 git clone https://github.com/LuigiPereira1709/streaming-api
 ```
2. Configurar Variáveis de Ambiente
- **Desenvolvimento Local**: Crie um arquivo `.env` seguindo o exemplo abaixo:
```plaintext
## Estou na raiz do projeto ##
## Variáveis de ambiente para deployment da API na instância EC2 via SSH ##

# Variáveis de ambiente remotas
REMOTE_PATH=your_work_path (opt/**)
EC2_HOST=your_ec2_host

# Variáveis de ambiente locais
ENVIRONMENT_VARIABLES_SECRET_NAME=your_environment_secret_name (dev/api/environment)
SSH_KEY=your_path_ssh_key (~/.ssh/**.pem)

# Variáveis Docker, se você quiser usar minha imagem, não altere essas variáveis
DOCKER_IMAGE=pitanguinhamarvada/streaming-backend-java
DOCKER_TAG=latest
```
- **SSM (AWS Secret Manager)**: Crie um segredo seguindo o exemplo abaixo:
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
3. Construir a Aplicação com `make build` (Opcional)
```bash
# Na raiz do projeto
make build
```
4. Fazer o Deployment da Aplicação com `make ssh-deploy`
```bash
# Na raiz do projeto
make ssh-deploy
```
5. Verificar o arquivo `deploy.log`
6. Se tudo estiver deployado corretamente, voce pode acessar a API em `http://your_ec2_host:8080/swagger-ui/index.html`, isso mostrará o Swagger UI para a API, permitindo que você explore e teste os endpoints.

## Melhorias Futuras
- [ ] **Aprimorar a Segurança**: Implementar autenticação e autorização robustas para proteger os endpoints da API.
- [ ] **Busca e Filtro Avançados**: Implementar funcionalidades de busca e filtro mais avançadas, talvez usando Elasticsearch ou outro mecanismo de busca.
- [ ] **SQS e SNS**: Integrar com AWS SQS e SNS para menssagem e notificações dos uploads de arquivos de mídia e as flags de conversão.
- [ ] **Suporte para Arquivos com +100MB**: Implementar suporte para arquivos grandes, garantindo eficiencia no upload e processamento dos arquivos de mídia maiores que 100MB.
- [ ] **Monitoramento e Logging**: Implementar monitoramento e logging mais robustos, talvez usando AWS CloudWatch ou outro serviço de monitoramento.
- [ ] **CI/CD Pipeline**: Configurar a integração contínua e entrega contínua (CI/CD) pipeline usando ferramentas como GitHub Actions ou AWS CodePipeline para automação do processo de build e deployment.
- [ ] **Desenvolver uma Interface de Usuário 7-7**: É, não sou bom de frontend, isso me dá pesadelos, mas vou tentar criar uma interface simples para a API... UM DIA :)... Ou talvez só uma CLI simples para interagir com a API? heheh, é brincadeira, ou não? :D 

## Links 
- [Blackbox Tests](blackboc_tests/blackbox_tests_doc_pt.md)  
- [Scripts](scripts/scripts_doc_pt.md)   
- [Local Env Exemplo](local_env_example)   
- [API Env Exemplo](api_env_example.json)   
- [Makefile](../Makefile)   
- [Main Doc](https://github.com/LuigiPereira1709/streaming-cloudnative-project/blob/main/doc/README_pt.md)  
- [Acesse Minha API](http://d130vf1311tsqd.cloudfront.net/swagger-ui/index.html)

## Objetivos de Aprendizado
- [x] **Cloudnative Development**: Aprender a desenvolver aplicações cloudnative usando Java e Spring Boot, integrando com serviços da AWS.
- [x] **Reactive Programming**: Aprender e aplicar princípios de programação reativa usando Spring WebFlux.
- [x] **API Design and Development**: Melhorar habilidades em design e desenvolvimento de APIs RESTful com Spring Boot.
- [x] **Estratégias de Teste**: Compreender e aplicar estratégias de teste, incluindo testes unitários, de integração e blackbox, para garantir a qualidade e confiabilidade da API.
- [x] **Infraestrutura como Código**: Aprender a usar scripts para automatizar o processo de deployment usando scripts e Docker.
- [ ] **AWS SQS e SNS**: Aprender a integrar com AWS SQS e SNS para menssagem e notificações.
- [ ] **JWT e OAuth2**: Ganhar experiência na implementação de autenticação e autorização usando JWT e OAuth2.
- [ ] **CI/CD Pipeline**: Aprender a configurar uma pipeline de CI/CD usando AWS CodePipeline ou GitHub Actions para automação do processo de build e deployment.
- [ ] **Desenvolvimento de Interface de Usuário**: Ganhar experiência no desenvolvimento de uma interface de usuário para a API.

## Licença
Este projeto é licenciado sob a Licença GNU GPL v3.0. Veja o arquivo [LICENSE](../LICENSE.txt) para mais detalhes. 
