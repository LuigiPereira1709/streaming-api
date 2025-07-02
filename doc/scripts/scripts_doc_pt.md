# Scripts Doc
[English Version](scripts_doc_en.md)   
Os scripts são utilizados neste projeto para automatizar a execução de tarefas, como a construção do projeto e o deploy da API para a instância EC2. Esses scripts são escritos em shell e utilizados no makefile para executar as tarefas.

## Scripts
- **`make build`**: Constrói o projeto e envia a imagem para o repositório Docker.
### Passo a passo do `make build`
1. Executa todos os testes para garantir que o projeto está funcionando corretamente.
2. Constrói o arquivo jar utilizando o comando `./gradlew bootJar`.
3. Constrói uma imagem multiplataforma utilizando o comando `docker buildx build`.
4. Envia a imagem para o repositório Docker utilizando o comando `docker push`.  
Nota: É necessário executar `docker login` antes de rodar o script para se autenticar com o Docker Hub.

- **`make ssh-deploy`**: Realiza o deploy da API na instância EC2 usando SSH.
### Passo a passo do `make ssh-deploy`
1. Acessa a instância EC2 via SSH utilizando o comando `ssh` para criar o diretório remoto.
2. Compacta os arquivos de `scripts/ec2` e os copia para a instância EC2 utilizando o comando `scp`.
3. Acessa novamente a instância EC2 via SSH para executar o script de deploy.
4. Descompacta os arquivos, define as permissões e configura o nome da variável de ambiente secreta.
5. Executa `run_setup.sh` para configurar a API e iniciar o serviço na instância EC2 utilizando um container Docker.
#### Pré-requisitos
- **Acesso via SSH**: Certifique-se de que você possui acesso SSH à instância EC2.
- **Imagem Docker existente**: Verifique se a imagem Docker já existe no repositório antes de executar o script.
- **Arquivo `.env` válido**: Verifique se o arquivo `.env` é válido e contém as variáveis de ambiente necessárias para o deploy.  
Exemplo de arquivo `.env`:
```plaintext
# Variáveis de ambiente remotas
REMOTE_PATH=seu_diretorio_remoto (opt/**)
EC2_HOST=seu_host_ec2

# Variáveis de ambiente locais
ENVIRONMENT_VARIABLES_SECRET_NAME=nome_do_segredo_ambiente (dev/api/environment)
SSH_KEY=caminho_para_chave_ssh (~/.ssh/**.pem)

# Variáveis Docker, aqui está minha imagem, se quiser usar não altere
DOCKER_IMAGE=pitanguinhamarvada/streaming-backend-java 
DOCKER_TAG=latest 
```
- **`make sloth`**: Executa o modo sloth para realizar todas as tarefas de deploy da API na instância EC2.
Nota: Sim, eu sou preguiçoso, então criei esse script para rodar todas as tarefas em um único comando. Ele executa os comandos `make build` e `make ssh-deploy` em sequência.

## Resultados Esperados
- [x] Construção do projeto com todos os testes passando.
- [x] Construção da imagem Docker para multiplataforma e envio para o repositório Docker.
- [x] Automação do deploy da API na instância EC2 em um container Docker, utilizando SSH para se conectar à instância e executar os comandos necessários.
- [x] Garantir que a API esteja rodando corretamente na instância EC2 após o deploy.

## Melhorias Futuras
- [ ] **Mudar SSH para SSM**: Utilizar o AWS Systems Manager (SSM) para conectar à instância EC2 em vez de SSH, proporcionando uma maneira mais segura e gerenciável de acessar a instância.
- [ ] **Escrever scripts para testes de integração**: Criar scripts para executar testes de integração após o deploy para garantir que a API esteja funcionando corretamente no ambiente de produção.
- [ ] **Escrever scripts para subir uma instância EC2**: Criar scripts para automatizar o processo de configuração de uma instância EC2, incluindo a instalação de software necessário e configuração do ambiente.

## Links
- [SSM doc](https://docs.aws.amazon.com/systems-manager/latest/userguide/what-is-systems-manager.html)
- [Pasta de Scripts](../../scripts)
- [Makefile](../../Makefile)
- [Voltar para o documento principal](../README_pt.md)

## Objetivos de Aprendizado
- [ ] **SSM**: Aprender a utilizar o AWS Systems Manager para gerenciar instâncias EC2 de forma segura e eficiente.
- [ ] **EC2 instancia setup por cli**: Aprender a automatizar o processo de configuração de uma instância EC2 utilizando scripts, incluindo a instalação de software necessário e configuração do ambiente.
- [ ] **Como implementar testes de integração**: Encontrar uma maneira de escrever testes de integração em aplicações AWS cloud native. 
