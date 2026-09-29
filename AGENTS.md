# AGENTS.md — архитектура проекта day2

**Роль:** в продуктовых вопросах («что делаем, в каком порядке, критерии приёмки») агент действует как владелец продукта — инструкции, бэклог и Definition of Done лежат в `PO.md` (обновляется при изменении приоритетов). Этот файл остаётся источником правды по архитектуре.

Учебный REST API на Spring Boot: развивался от CRUD справочника валют в сторону «мини-банка» — клиенты, биллинг-счета, бизнес-операции (депозит, перевод, оплата картой, снятие наличных) через REST и Kafka. Проект в активной разработке: часть кода — TODO-заглушки. Этот файл — источник правды об архитектуре; при изменениях структуры обновляй его.

## Стек

- **Java** — свойство `<java.version>` в `pom.xml` закомментировано, версия берётся от parent (проект писался под Java 25).
- **Spring Boot 4.1.1** (parent POM), модульность Boot 4: стартеры `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-h2console`; плюс `spring-kafka` и `commons-lang3` 3.20.0.
- **Jackson 3** — используется `tools.jackson.databind.ObjectMapper` (НЕ `com.fasterxml.jackson`). Бин `ObjectMapper` объявлен вручную в `KafkaConfig`.
- **БД**: H2 в file-режиме (`jdbc:h2:file:./data/day2db`), `ddl-auto=update`, `show-sql=true`; файлы лежат в `data/` в корне проекта (не в .gitignore). Посевные данные — `sql/bank.sql`: банк «Банк Москвы» (БИК 044525219, `BankService.BankCode.BANK_OF_MOSCOW`) и его корсчета RUB/USD/EUR; скрипт выполняется вручную.
- **Kafka**: внешний брокер `localhost:9092`, адрес зашит в `KafkaConfig` (в `application.properties` его нет). Топики: `currency-create` и `business-operation`. Обработка ошибок — `DefaultErrorHandler` с `FixedBackOff(1000, 3)`, повторяемые исключения: `IOException`, `TransientDataAccessException`.
- **Сборка**: Maven 3.9.16 через wrapper (`mvnw` / `mvnw.cmd`), Maven-плагины: `spring-boot-maven-plugin`, `maven-compiler-plugin` (encoding UTF-8).
- **Тесты**: `spring-boot-starter-data-jpa-test` (test). В Boot 4 слайсы модульные: `@DataJpaTest` — пакет `org.springframework.boot.data.jpa.test.autoconfigure`, `TestEntityManager` — `org.springframework.boot.jpa.test.autoconfigure`. Тесты работают на in-memory H2, Kafka-контекст не поднимают.
- **Devtools** подключены (runtime, optional) — включён auto-restart при запущенном приложении.

## Команды

```bash
mvn spring-boot:run           # запуск (порт 8080); Kafka-консюмеры требуют брокер на localhost:9092
mvn clean package             # сборка зелёная (проверено 2026-09-30); wrapper (mvnw) не закоммичен — нужен системный Maven
mvn test                      # JPA-слайс тесты: инвариант двойной записи и баланс (src/test)
```

H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/day2db`).

Нагрузочное тестирование: `jmeter/testplan.jmx` (GET/POST `/api/currencies` на localhost, данные из `jmeter/currencies.csv` — пары `code,name`). `jmeter.log` в корне — артефакт прошлого запуска JMeter.

## Архитектура

Классическая слоистая архитектура; два способа входа — HTTP и Kafka-консюмеры. Запрос идёт строго сверху вниз:

```
HTTP  → CurrencyController          → CurrencyService          → CurrencyDao ← CurrencyDaoImpl
HTTP  → BusinessOperationController → BusinessOperationService → BusinessOperationDAO ← BusinessOperationDAOImpl
HTTP  → ClientController            → ClientService            → ClientDao ← ClientDAOImpl
Kafka → CurrencyCreateConsumer      → CurrencyService
Kafka → BusinessOperationConsumer   → BusinessOperationService
```

`BusinessOperationService.createAndProcess` — одна `@Transactional`: создаёт `BusinessOperation`, валидирует клиентов через `ClientService.getClientOrThrow` и диспатчит по `BusinessOperationType`:

- `DEPOSIT` → `DepositService` (реализован: одна проводка корсчёт банка → счёт клиента, `BillingTransaction` + `Posting`), `CARD_PAYMENT` → `CardPaymentService`, `TRANSFER` → `TransferService`, `CASH_WITHDRAWAL` → `CashWithdrawalService` (последние три — `//TODO`-заглушки, бросают `UnsupportedOperationException`).

### Домены (JPA-сущности, не покидают сервисный слой)

- `model/Currency` — справочник валют (id через `GenerationType.IDENTITY`); `model/CurrencyRate` — курсы валют pair+rate+date, сервисами пока не используется.
- `model/Party` — абстрактный участник отношений (`@Entity` + `@Inheritance(JOINED)`): id и список биллинг-счетов (`addAccount`/`removeAccount`). Подклассы: `model/Client` (клиент: firstName/lastName/externalId) и `model/Bank` (банк: name, code/БИК). Счёт `model/billing/BillingAccount` (accountNumber + валюта) ссылается на `Party` (`@ManyToOne`, колонка `party_id`).
- `model/billing/BillingTransaction` + `model/billing/Posting` — заготовка двойной записи: транзакция состоит из проводок (debitAccount/creditAccount/amount).
- `model/BusinessOperation` — операция: тип (enum), amount, client/client2 (`@ManyToOne` LAZY), accountNumber/accountNumber2.

### DTO и мапперы

Два поколения DTO:

- валютные — `CurrencyDto`, `CreateCurrencyDto`, `ErrorResponse`: records с `Serializable` (старый стиль);
- новые — `ClientDTO`, `CreateClientDTO`, `BusinessOperationDTO`, `CreateBusinessOperationDTO`, `dto/billing/*`: обычные классы с геттерами/сеттерами (текущая практика для нового кода).

Мапперы (все `@Component`, метод `map(entity)`, ручное копирование полей):

- `CurrencyMapper` — `Currency` → `CurrencyDto`;
- `ClientMapper.map(client, mapAccounts)` — счета у клиента ленивые, включаются в DTO флагом (в `ClientService.getById` перед маппингом делается `getAccounts().size()` для прогрева в транзакции); делегирует `BillingAccountMapper`;
- `BillingAccountMapper` — `BillingAccount` → `BillingAccountDTO`;
- `BusinessOperationMapper` — `BusinessOperation` → `BusinessOperationDTO` (client/client2 → clientId/clientId2, enum → строка);
- `BillingTransactionMapper.map(transaction, mapPostings)` — проводки включаются флагом (по аналогии со счетами клиента);
- `CurrencyRateMapper` — `CurrencyRate` → `CurrencyRateDTO` (валюты — вложенные `CurrencyDto` через `CurrencyMapper`).

### Слои и их роль

| Пакет | Класс | Роль |
|---|---|---|
| `(root)` | `Day2Application` | Точка входа: `@SpringBootApplication` + `@EnableKafka` + `@EnableAutoConfiguration` |
| `configuration` | `WebConfig` | Префикс `/api` для контроллеров пакета `ru.asocial.learn` через `configurePathMatch` |
| `configuration` | `KafkaConfig` | `ConsumerFactory` + фабрика контейнеров `@KafkaListener` + бин `ObjectMapper` (Jackson 3) |
| `consumer` | `CurrencyCreateConsumer`, `BusinessOperationConsumer` | `@KafkaListener`: десериализуют JSON в DTO, зовут сервисы |
| `controller` | `CurrencyController` | REST валют: CRUD, валидация `Assert.notNull`, HTTP-коды |
| `controller` | `ClientController` | REST клиентов: `POST /clients` (создание со счетами), `GET /clients/{id}` (со счетами) |
| `controller` | `BusinessOperationController` | `POST /business-operation` |
| `service` | `CurrencyService`, `ClientService`, `BankService` | Бизнес-логика + границы транзакций (`@Transactional`), бросают `ResourceNotFoundException` |
| `service` | `BusinessOperationService` | Создание + диспатч операций в одной транзакции |
| `service` | `DepositService` (реализован), `TransferService`, `CardPaymentService`, `CashWithdrawalService` (TODO) | Процессоры операций |
| `dao` | `CurrencyDao`, `ClientDao`, `BankDao`, `BusinessOperationDAO` (+ Impl) | Доступ к данным на **чистом JPA** (`@PersistenceContext EntityManager`), без Spring Data; JPQL в Impl |
| `dao/billing` | `BillingTransactionDAO` / `Impl` | Доступ к биллинг-транзакциям; `save` проверяет инвариант двойной записи (дебет = кредит, одна валюта, положительные суммы) и только потом persist |
| `dao/billing` | `PostingDAO` / `Impl` | Баланс счёта из проводок: `getAccountBalance(accountId)` = Σcredit − Σdebit (JPQL-агрегаты) |
| `mapper` | `CurrencyMapper`, `ClientMapper`, `BillingAccountMapper`, `BusinessOperationMapper`, `BillingTransactionMapper`, `CurrencyRateMapper` | Сущность → DTO |
| `handler` | `ExceptionHandler` | `@ControllerAdvice` (extends `ResponseEntityExceptionHandler`): `ResourceNotFoundException` → 404, `DuplicateResourceException` → 422, `UnsupportedOperationException` → 501, тело `ErrorResponse` |
| `exception` | `ResourceNotFoundException`, `DuplicateResourceException` | Runtime-исключения |

## REST API

Базовый префикс `/api` (добавляется `WebConfig`, в контроллере не указывается):

| Метод | Путь | Назначение |
|---|---|---|
| GET | `/api/currencies/{id}` | Получить по id (404, если нет) |
| GET | `/api/currencies?code=USD` | Поиск по коду (список 0..1, пустой — если не найдено) |
| POST | `/api/currencies` | Создать, тело `CreateCurrencyDto {code, name}` → 201 + Location; дубликат кода → 422 `DuplicateResourceException` |
| DELETE | `/api/currencies/{id}` | Удалить → 204 (404, если нет) |
| POST | `/api/clients` | Создать клиента (со счетами), тело `CreateClientDTO` → 201 + Location |
| GET | `/api/clients/{id}` | Получить клиента со счетами и балансами (404, если нет) |
| POST | `/api/business-operation` | Тело `CreateBusinessOperationDTO {operationType, amount, clientId, clientId2?, accountNumber, accountNumber2?}` → 201 |

Ошибки: `ResourceNotFoundException` → 404, `DuplicateResourceException` → 422 (`UNPROCESSABLE_CONTENT`), `BusinessValidationException` / `BusinessOperationException` → 422, `UnsupportedOperationException` → 501 (`NOT_IMPLEMENTED`, так отвечают TODO-процессоры операций); тело — `ErrorResponse`. В `BillingAccountDTO` появилось поле `balance` (баланс из проводок, заполняется в `ClientService` после маппинга через `PostingDAO`).

`rq.http` (IntelliJ HTTP Client) в корне почти пуст; `src/main/resources/static/index.html` — заглушка «DAY 2».

## Конвенции проекта

- Внедрение зависимостей — полевое `@Autowired` (не конструктор).
- Сущности — обычные классы с геттерами/сеттерами, без Lombok; DTO — смешанно: старые records, новые классы (см. выше).
- Транзакции объявляются в сервисе: чтение — `@Transactional(readOnly = true)`, запись — `@Transactional`; операции целиком — в `BusinessOperationService`.
- DAO строится по схеме «интерфейс + `Impl` на EntityManager» с `@Repository` на Impl, JPQL-запросы пишутся в Impl.
- JSON — Jackson 3: импорты `tools.jackson.databind.*`.
- Комментарии в коде местами на русском — это норма для проекта.

## Известные особенности (не «починить», а учитывать)

Раньше здесь были два бага — они уже исправлены: `CurrencyDaoImpl.findByCode` теперь делает `setParameter` + `getSingleResultOrNull()` (возвращает `Optional`), а предикат префикса в `WebConfig` ограничен пакетом `ru.asocial.learn` (ошибки больше не маскируются под 404 «No static resource error»).

- **Сборка зелёная** (`mvn clean package`, проверено 2026-09-30). Ранее не компилировалась и содержала баги — все уже исправлены в коде: `BusinessOperationService.createAndProcess` возвращает DTO через `BusinessOperationMapper`; `amount` копируется из DTO; `BusinessOperationDAOImpl` — `@Repository` + `implements BusinessOperationDAO`; у `Posting` есть `@Id`; поле `accounts` живёт только в `Party` (у `Client` дублирующего поля нет).
- **TODO-заглушки**: `TransferService` / `CardPaymentService` / `CashWithdrawalService` (бросают `UnsupportedOperationException("Not implemented yet")`), `ClientService.findClientByExtId` (`return null`), `CreateBillingTransactionDTO` (пустой).
- **Инвариант двойной записи** (issue #3) живёт в `BillingTransactionDAOImpl.save`: непрошедшая валидацию транзакция не сохраняется (`BusinessValidationException`). Баланс — производное от проводок (`PostingDAO.getAccountBalance`), хранимого поля нет.
- **Kafka**: адрес брокера `localhost:9092` зашит в `KafkaConfig`; без запущенного брокера консьюмеры бесконечно пытаются соединиться — приложение при этом стартует, но топики не работают. `auto.offset.reset=earliest`, так что при появлении брокера консьюмер прочитает сообщения с начала.
- **Trailing slash не матчится**: в Spring Framework 6+ `/api/currencies/` не совпадает с маппингом `/api/currencies` — запрос уходит в статические ресурсы (`NoResourceFoundException: No static resource api/currencies`). Запрашивать без слэша или добавить `{"" , "/"}` в маппинг.
- `data/` (живая файловая БД H2: `day2db.mv.db`, `day2db.trace.db`, `diagdb.mv.db`) по-прежнему не добавлена в `.gitignore`; сам `.gitignore`, `.gitattributes`, `.mvn/`, `mvnw*`, `jmeter/` пока не закоммичены (untracked).
- `.github/modernize/java-upgrade/` — служебные hook-скрипты (Copilot java-upgrade), к приложению отношения не имеют.
- Тесты: JPA-слайс (`@DataJpaTest`) на чистом H2 in-memory — `BillingTransactionDAOImplTest` (инвариант) и `PostingDAOImplTest` (баланс). Веб-слой и Kafka в тестах не поднимаются.
