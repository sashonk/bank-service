# AGENTS.md — архитектура проекта day2

**Роль:** в продуктовых вопросах («что делаем, в каком порядке, критерии приёмки») агент действует как владелец продукта — инструкции, бэклог и Definition of Done лежат в `PO.md` (обновляется при изменении приоритетов). Этот файл остаётся источником правды по архитектуре.

Учебный REST API на Spring Boot: развивался от CRUD справочника валют в сторону «мини-банка» — клиенты, биллинг-счета, бизнес-операции (депозит, перевод, оплата картой, снятие наличных) через REST и Kafka. Проект в активной разработке: часть кода — TODO-заглушки. Этот файл — источник правды об архитектуре; при изменениях структуры обновляй его.

## Стек

- **Java** — свойство `<java.version>` в `pom.xml` закомментировано, версия берётся от parent (проект писался под Java 25).
- **Spring Boot 4.1.1** (parent POM), модульность Boot 4: стартеры `spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`, `spring-boot-h2console`; плюс `spring-kafka` и `commons-lang3` 3.20.0.
- **Jackson 3** — используется `tools.jackson.databind.ObjectMapper` (НЕ `com.fasterxml.jackson`). Бин `ObjectMapper` объявлен вручную в `KafkaConfig`.
- **БД**: H2 в file-режиме (`jdbc:h2:file:./data/day2db`), `ddl-auto=update`, `show-sql=true`; файлы лежат в `data/` в корне проекта (не в .gitignore).
- **Kafka**: внешний брокер `localhost:9092`, адрес зашит в `KafkaConfig` (в `application.properties` его нет). Топики: `currency-create` и `business-operation`. Обработка ошибок — `DefaultErrorHandler` с `FixedBackOff(1000, 3)`, повторяемые исключения: `IOException`, `TransientDataAccessException`.
- **Сборка**: Maven 3.9.16 через wrapper (`mvnw` / `mvnw.cmd`), Maven-плагины: `spring-boot-maven-plugin`, `maven-compiler-plugin` (encoding UTF-8).
- **Devtools** подключены (runtime, optional) — включён auto-restart при запущенном приложении.

## Команды

```bash
./mvnw spring-boot:run        # запуск (порт 8080); Kafka-консюмеры требуют брокер на localhost:9092
./mvnw clean package          # сборка — сейчас падает: main не компилируется (см. «Известные особенности»)
./mvnw test                   # тестов нет (src/test пуст)
```

H2 console: `http://localhost:8080/h2-console` (JDBC URL `jdbc:h2:file:./data/day2db`).

Нагрузочное тестирование: `jmeter/testplan.jmx` (GET/POST `/api/currencies` на localhost, данные из `jmeter/currencies.csv` — пары `code,name`). `jmeter.log` в корне — артефакт прошлого запуска JMeter.

## Архитектура

Классическая слоистая архитектура; два способа входа — HTTP и Kafka-консюмеры. Запрос идёт строго сверху вниз:

```
HTTP  → CurrencyController          → CurrencyService          → CurrencyDao ← CurrencyDaoImpl
HTTP  → BusinessOperationController → BusinessOperationService → BusinessOperationDAO ← BusinessOperationDAOImpl
HTTP  → ClientController (заглушка) → ClientService            → ClientDao ← ClientDAOImpl
Kafka → CurrencyCreateConsumer      → CurrencyService
Kafka → BusinessOperationConsumer   → BusinessOperationService
```

`BusinessOperationService.createAndProcess` — одна `@Transactional`: создаёт `BusinessOperation`, валидирует клиентов через `ClientService.getClientOrThrow` и диспатчит по `BusinessOperationType`:

- `DEPOSIT` → `DepositService`, `CARD_PAYMENT` → `CardPaymentService`, `TRANSFER` → `TransferService`, `CASH_WITHDRAWAL` → `CashWithdrawalService` (все четыре процессора — пока пустые `//TODO`-заглушки).

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
| `controller` | `ClientController` | `POST /clients` — заглушка (`//TODO`, `return null`) |
| `controller` | `BusinessOperationController` | `POST /business-operation` |
| `service` | `CurrencyService`, `ClientService`, `BankService` | Бизнес-логика + границы транзакций (`@Transactional`), бросают `ResourceNotFoundException` |
| `service` | `BusinessOperationService` | Создание + диспатч операций в одной транзакции |
| `service` | `DepositService`, `TransferService`, `CardPaymentService`, `CashWithdrawalService` | Процессоры операций — пустые TODO |
| `dao` | `CurrencyDao`, `ClientDao`, `BankDao`, `BusinessOperationDAO` (+ Impl) | Доступ к данным на **чистом JPA** (`@PersistenceContext EntityManager`), без Spring Data; JPQL в Impl |
| `dao/billing` | `BillingTransactionDAO` / `Impl` | Пустые заглушки |
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
| POST | `/api/clients` | Заглушка: `return null` — эндпоинт объявлен, логики нет |
| POST | `/api/business-operation` | Тело `CreateBusinessOperationDTO {operationType, amount, clientId, clientId2?, accountNumber, accountNumber2?}` → 201 |

Ошибки: `ResourceNotFoundException` → 404, `DuplicateResourceException` → 422 (`UNPROCESSABLE_CONTENT`), `UnsupportedOperationException` → 501 (`NOT_IMPLEMENTED`, так отвечают TODO-процессоры операций); тело — `ErrorResponse`.

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

- **Проект сейчас НЕ компилируется**: `BusinessOperationService.createAndProcess` объявлен с возвратом `BusinessOperationDTO`, но возвращает сущность `BusinessOperation`; то же в `BusinessOperationController.create` (`ResponseEntity<BusinessOperationDTO>` с телом-сущностью). Нужен маппинг сущность → DTO.
- **Самоприсваивание amount**: `businessOperation.setAmount(businessOperation.getAmount())` — значение из DTO не копируется, останется `null` и упадёт на `nullable = false` при insert. Должно быть `businessOperationDTO.getAmount()`.
- **`BusinessOperationDAOImpl` — не Spring-бин и не реализует интерфейс**: нет `@Repository` и нет `implements BusinessOperationDAO` (метод `save` к тому же package-private) → `@Autowired BusinessOperationDAO` в сервисе упадёт на старте с NoSuchBeanDefinitionException.
- **`Posting` без `@Id`**: поле `id` помечено только `@Column` — сущность без первичного ключа, Hibernate при старте с `ddl-auto=update` не смапит её.
- **`Client.accounts`**: на одном поле одновременно `@ManyToOne` и `@OneToMany(mappedBy = "client")` — невалидная комбинация (лишний `@ManyToOne`).
- **TODO-заглушки**: `DepositService` / `TransferService` / `CardPaymentService` / `CashWithdrawalService` (бросают `UnsupportedOperationException("Not implemented yet")`), `ClientController.createClient` (`return null`), `ClientService.findClientByExtId` (`return null`), `BillingTransactionDAO(+Impl)` и `CreateBillingTransactionDTO` (пустые).
- **Kafka**: адрес брокера `localhost:9092` зашит в `KafkaConfig`; без запущенного брокера консьюмеры бесконечно пытаются соединиться — приложение при этом стартует, но топики не работают. `auto.offset.reset=earliest`, так что при появлении брокера консьюмер прочитает сообщения с начала.
- **Trailing slash не матчится**: в Spring Framework 6+ `/api/currencies/` не совпадает с маппингом `/api/currencies` — запрос уходит в статические ресурсы (`NoResourceFoundException: No static resource api/currencies`). Запрашивать без слэша или добавить `{"" , "/"}` в маппинг.
- `data/` (живая файловая БД H2: `day2db.mv.db`, `day2db.trace.db`, `diagdb.mv.db`) по-прежнему не добавлена в `.gitignore`; сам `.gitignore`, `.gitattributes`, `.mvn/`, `mvnw*`, `jmeter/` пока не закоммичены (untracked).
- `.github/modernize/java-upgrade/` — служебные hook-скрипты (Copilot java-upgrade), к приложению отношения не имеют.
- Тестов нет (`src/test` пуст).
