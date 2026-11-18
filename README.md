# PSoft-Arena

[Tasks](https://docs.google.com/spreadsheets/d/1qP-h_3fDj9RFAfiTe4q5Lcq9EyYk1Pb8Aw6yGY9UNh4/edit?usp=sharing)

[Docs](https://docs.google.com/document/d/1YWuCVkf-sueHspGZq28GL-J1hbUPzmQnTk0uCQE4kfU/edit?usp=sharing)

## Como rodar o projeto

### Requisitos

- JDK 21 instalado e configurado no `JAVA_HOME`.
- Acesso à internet na primeira execução para baixar o Gradle e as dependências.

O projeto inclui o Gradle Wrapper; não é necessário instalar o Gradle separadamente.
Os comandos abaixo devem ser executados na pasta raiz do projeto.

### Executar no terminal

A variável `APP_CRYPTO_SECRET` é obrigatória: ela é usada para criptografar as chaves de API dos LLMs cadastradas nas atividades. O valor abaixo é um exemplo para desenvolvimento local.

**Linux e macOS:**

```bash
export APP_CRYPTO_SECRET="segredo-local-de-desenvolvimento"
./gradlew bootRun
```

Se houver erro de permissão ao executar o wrapper, rode `chmod +x gradlew`.

**Windows (PowerShell):**

```powershell
$env:APP_CRYPTO_SECRET = "segredo-local-de-desenvolvimento"
.\gradlew.bat bootRun
```

A aplicação inicia em `http://localhost:8080`. Para encerrá-la, pressione `Ctrl+C` no terminal.

### Executar no IntelliJ IDEA

1. Abra a pasta do projeto e importe a configuração do Gradle.
2. Configure o Project SDK e a Gradle JVM para o JDK 21.
3. Na configuração de execução de `PsoftArenaApplication`, adicione a variável de ambiente `APP_CRYPTO_SECRET=segredo-local-de-desenvolvimento`.
4. Execute o método `main` dessa classe.

### Documentação da API e banco de dados

Com a aplicação em execução:

- [Swagger UI](http://localhost:8080/swagger-ui.html)
- [OpenAPI em JSON](http://localhost:8080/v3/api-docs)
- [Console H2](http://localhost:8080/h2-console)

O banco padrão é o H2 em memória, sem necessidade de instalar um banco externo. Para acessar o console H2, use:

| Campo | Valor |
| --- | --- |
| Driver Class | `org.h2.Driver` |
| JDBC URL | `jdbc:h2:mem:psoftarena` |
| User Name | `sa` |
| Password | Deixe em branco |

Os dados são temporários e são perdidos ao encerrar a aplicação.

### Testes e build

**Linux e macOS:**

```bash
./gradlew test
./gradlew build
```

**Windows (PowerShell):**

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

A tarefa de testes já configura `APP_CRYPTO_SECRET` com um valor próprio, portanto não é necessário definir a variável para rodar os testes.

O relatório dos testes fica em `build/reports/tests/test/index.html`.
