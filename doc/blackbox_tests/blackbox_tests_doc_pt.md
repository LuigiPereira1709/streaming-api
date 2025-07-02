# Blackbox Test Doc
[English Version](blackbox_tests_doc_en.md)  
O teste de caixa preta no endpoint da API é uma etapa crucial para garantir que a API se comporte conforme o esperado do ponto de vista do usuário. Eles são executados manualmente ou automaticamente, com exceções feitas para determinadas condições ou cenários de timeout. Os testes são projetados para verificar a funcionalidade da API sem analisar seu funcionamento interno.

## Casos de Teste
Os casos de teste são estruturados para cobrir diversos aspectos da API, incluindo:
- **Requisições POST**: 
    1. Testar a criação de recursos, garantindo que a API trate corretamente os dados de entrada e retorne a resposta esperada.
    2. Validar que a API responde adequadamente a entradas inválidas, como campos obrigatórios ausentes ou tipos de dados incorretos.

- **Requisições PUT**:
    1. Testar a atualização de recursos existentes, garantindo que a API modifique corretamente os dados e retorne a resposta esperada.
    2. Validar que a API lida com atualizações inválidas de forma adequada, como tentar atualizar um recurso que não existe ou fornecer dados inválidos.

- **Requisições de Busca (SEARCH)**:
    1. Testar a recuperação de recursos com base em critérios específicos, garantindo que a API retorne os dados corretos.
    2. Validar que a API lida adequadamente com consultas de busca inválidas, como buscar por recursos que não existem ou fornecer parâmetros de busca malformados. 
- **Requisições DELETE**:
    1. Testar a exclusão de recursos existentes, garantindo que a API remova corretamente o recurso especificado e retorne a resposta esperada.
    2. Validar que a API lida com tentativas de exclusão de recursos inexistentes de forma adequada, retornando uma mensagem de erro apropriada.

- **Obter URL de Conteúdo (Get Content URL)**:
    1. Testar a geração de URLs de conteúdo, garantindo que a API retorne URLs válidas para consumo do conteúdo.
    2. Validar que a API lida com requisições para URLs de conteúdo que não existem ou estão malformadas, retornando mensagens de erro apropriadas.

## Resultados Esperados
Os resultados esperados dos testes incluem:
- [x] Criação, atualização, recuperação e exclusão de recursos com sucesso.
- [x] Tratamento correto de entradas inválidas, incluindo mensagens de erro apropriadas e códigos de status.
- [x] Geração adequada de URLs de conteúdo, garantindo que sejam válidas e acessíveis.
- [x] Comportamento consistente e previsível da API em diferentes casos de teste.

## Execução
Os testes são executados usando o [Hurl](https://hurl.dev/), uma ferramenta baseada em curl para testar requisições e respostas HTTP. Para executar os testes, uma API de teste deve estar configurada e o seguinte comando pode ser utilizado:

### Pré-requisitos
- **[Hurl 6.1.1](https://hurl.dev/docs/installation.html)**: Uma ferramenta de linha de comando para testar requisições e respostas HTTP.
- **MongoDB**: Um banco de dados NoSQL para armazenar os dados utilizados nos testes.

### Passo a Passo
1. Navegue até o diretório que contém os arquivos de teste.
2. Carregue o script insertOne.js no banco de dados para garantir que ele esteja populado com os dados necessários para os testes. Use o seguinte comando:
```bash
mongosh <your_mongo_uri> --eval "load('**/insertOne**.js')"
```
3. Execute os testes do Hurl usando o seguinte comando, substituindo `name_of_test.hurl` pelo arquivo de teste específico que você deseja executar:
```bash
hurl --file-root .. --verbose name_of_test.hurl 
```
## Estrutura
``` plaintext
blackbox_test/
│
├── music/
│   ├── post.hurl
│   ├── put.hurl
│   ├── search.hurl
│   ├── delete.hurl
│   ├── get_content_url.hurl
│   └── insertOneMusic.js
│
├── video/
│   ├── post.hurl
│   ├── put.hurl
│   ├── search.hurl
│   ├── delete.hurl
│   ├── get_content_url.hurl
│   └── insertOnePodcast.js
│
├── content.mp3
├── thumbnail.jpeg
└── txt.txt
```

## Melhorias Futuras
- [ ]  **Melhorar a sequência de testes**: adicionar intervalos entre as requisições.
- [ ]  **Utilizar variáveis de ambiente**: Em vez de codificar valores diretamente nos arquivos de teste, utilizar variáveis de ambiente para tornar os testes mais flexíveis e adaptáveis a diferentes ambientes.
- [ ] **Adicionar mais casos de teste**: Incluir testes adicionais para cobrir mais cenários e garantir uma cobertura de teste mais abrangente.
- [ ]  **Adicionar mais testes de erro**: Garantir que a API lide adequadamente com condições inesperadas, como falhas de rede ou erros no servidor.
- [ ] **Adicionar testes de stress**: Implementar testes de estresse para avaliar o desempenho da API sob carga intensa, garantindo que ela possa lidar com um grande número de requisições sem degradação de desempenho.

## Links
[Hurl doc](https://hurl.dev/)  
[Para os testes de caixa preta](../../blackbox_tests)  
[Voltar para o documento principal](../README_pt.md)

## Metas de apredizagem?
- [ ] **Aprofundar o entendimento sobre o Hurl**: Aprender a usar o Hurl de forma eficaz para testar APIs, incluindo seus principais recursos.
- [ ] **Ganhar experiência prática com testes de caixa preta**: Compreender como estruturar e executar testes de caixa preta para APIs, focando na funcionalidade do ponto de vista do usuário.
- [ ] **Aprender boas práticas de teste de APIs**: Adquirir conhecimento sobre como escrever testes eficazes, incluindo a validação de entradas, tratamento de erros e verificação de respostas esperadas.
- [ ] **Aprender sobre testes de stress**: Compreender como implementar testes de stress para avaliar o desempenho da API sob carga intensa. 
