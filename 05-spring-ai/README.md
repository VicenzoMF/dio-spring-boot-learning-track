# DIO Spring Boot - Final Project 05: Spring AI (budgeting)

## Introduction

This final module applies Spring AI in a budgeting API while preserving the same layered architecture used across the track.

The goal is to integrate AI capabilities without bypassing domain and use case boundaries.

## Code Context

The project processes voice commands to create and query financial transactions.

Primary flow:

1. Client uploads an audio file.
2. Audio is transcribed into text.
3. The model selects an application tool/use case.
4. The use case persists or queries transaction data.
5. The final response is converted to audio.

## Project Structure

- `src/main/java/dio/budgeting/domain`
  - Domain model and repository contract.
- `src/main/java/dio/budgeting/application`
  - Use cases used by both REST and AI tool calling.
- `src/main/java/dio/budgeting/infrastructure`
  - HTTP adapters, JPA adapters, and integration glue.

## Module-Specific Topics

### Speech-to-text

- Uses `TranscriptionModel` for audio transcription.
- Model settings are configured in `application.properties`.

### Tool calling

- `ChatClient` registers use-case tools.
- `@Tool` methods expose business capabilities to the model.

### Text-to-speech

- `TextToSpeechModel` produces MP3 output from final text.
- AI endpoint returns generated audio.

## Spring AI Documentation

- Spring AI Reference: https://docs.spring.io/spring-ai/reference/index.html
- ChatModel API: https://docs.spring.io/spring-ai/reference/api/chatmodel.html
- ChatClient API: https://docs.spring.io/spring-ai/reference/api/chatclient.html
- Tools API: https://docs.spring.io/spring-ai/reference/api/tools.html
- Audio Transcriptions API: https://docs.spring.io/spring-ai/reference/api/audio/transcriptions.html
- Audio Speech API: https://docs.spring.io/spring-ai/reference/api/audio/speech.html

## Shared Architecture References

Common architecture concepts are documented in the root README:

- [DDD layers](../README.md#ddd-layered-architecture)
- [Class vs record](../README.md#java-class-vs-java-record-in-domain-modeling)
- [Strong typed identifiers](../README.md#strong-typed-identifiers)
- [Repository pattern](../README.md#repository-pattern)
- [Use cases and Clean Architecture](../README.md#use-cases-and-clean-architecture)
- [Docker Compose support](../README.md#docker-compose-support-in-development)

## How to Run

Set your OpenAI API key:

```bash
export OPENAI_API_KEY="your_api_key_here"
```

Run the application and tests:

```bash
./gradlew bootRun
./gradlew test
```

## Notes

- Educational final project focused on AI plus architectural discipline.
- External provider integration tests may require active credentials.

---

## Minha evolução: validação de transações e total por categoria

### O que foi implementado

1. **Validação no domínio.** Uma nova `Transaction` só é criada com descrição preenchida, valor maior que zero e categoria informada. Caso contrário, lança `InvalidTransactionException`. A descrição é salva sem espaços nas pontas. Transações lidas do banco não passam por essa validação, pois já foram validadas na criação.
2. **Erro HTTP 400 em vez de 500.** O `ApiExceptionHandler` converte a exceção de domínio em uma resposta `400 Bad Request` (formato `ProblemDetail`).
3. **Nova consulta financeira.** O `SumTransactionsByCategoryUseCase` retorna o total gasto e a quantidade de transações de uma categoria. Ele serve o REST (`GET /transactions/{category}/total`) e o modelo de IA via `@Tool` (`sum-transactions-by-category`), como em "quanto gastei no mercado?".
4. **Prompt atualizado** (`prompts/system-message.st`) para orientar o modelo a usar a nova ferramenta e a explicar erros de validação.
5. **Testes unitários** que não precisam de banco nem de chave da OpenAI (usam um repositório em memória).

### Tecnologias

Java, Spring Boot, Spring AI (ChatClient, Tool Calling, transcrição e síntese de voz), Spring Data JPA, MySQL, JUnit 5 e AssertJ.

### Como executar

Requisitos: JDK 25, Docker (o `compose.yml` sobe o MySQL) e uma chave da OpenAI.

```bash
export OPENAI_API_KEY=sua-chave
./gradlew bootRun
```

### Como testar

Testes unitários das novas regras (sem chave e sem banco):

```bash
./gradlew test --tests "dio.budgeting.PersistTransactionUseCaseTest" \
               --tests "dio.budgeting.SumTransactionsByCategoryUseCaseTest"
```

Fluxo principal por REST:

```bash
# cria uma transação (valor em centavos)
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Mercado","category":"GROCERIES","amount":5000}'

# valor inválido -> 400
curl -i -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{"description":"Mercado","category":"GROCERIES","amount":0}'

# total por categoria
curl http://localhost:8080/transactions/GROCERIES/total
```

Fluxo de voz (requer chave da OpenAI):

```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@src/test/resources/audio/recording-1.m4a" --output resposta.mp3
```

### O que aprendi

- Como o **Tool Calling** liga o modelo a casos de uso reais: o mesmo `@Tool` serve a API REST e o assistente por voz, sem duplicar regra de negócio.
- Que **validação de regra de negócio pertence ao domínio** (aqui, ao construtor de `Transaction`) e não ao controller, e que um `@RestControllerAdvice` transforma esse erro em HTTP 400.
- Que erros de validação lançados dentro de uma ferramenta voltam ao modelo, então o prompt também precisa orientar como explicá-los à pessoa usuária.
- Como testar casos de uso com um **repositório em memória**, sem banco de dados e sem chave da OpenAI, deixando o fluxo de voz para testes de integração.
- Limitação: o fluxo de áudio (transcrição e síntese de voz) exige chave da OpenAI e não foi executado nos testes desta entrega.
