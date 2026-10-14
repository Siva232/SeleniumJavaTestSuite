# DemoProject – Web UI, REST API and MySQL test automation

BDD framework: **Java 17 · Maven · Selenium 4 · Cucumber 7 + TestNG · Rest Assured 5 · Jackson · Extent Reports 5 · MySQL JDBC**.
Only those eight dependencies are declared. Logging, encryption and video use the JDK alone
(`java.util.logging`, `javax.crypto`, `javax.imageio`).

The UI suites target <https://rahulshettyacademy.com/AutomationPractice/>, organised as 5 modules.

| # | Feature file | Module | What it covers |
|---|---|---|---|
| 1 | `01_form_controls.feature` | Form controls | Radio buttons (outline), checkbox select/unselect/select-all, dropdown default/options/selection |
| 2 | `02_auto_suggest.feature` | Auto-suggest | Suggestions per partial text (outline), every suggestion matches, keyboard selection, no suggestions for 1 char / unknown text |
| 3 | `03_alerts.feature` | Alerts | Alert text per name (outline incl. a random name), confirm accept, confirm dismiss, name field cleared |
| 4 | `04_windows_and_frames.feature` | Windows, tabs, iframes | New window, close and return, new tab, iframe content, back to main page |
| 5 | `05_web_tables_and_elements.feature` | Web tables & element state | Course table rows/headers/price lookup (outline)/total, fixed-header total vs. column sum, filter by city, hide/show textbox, mouse hover menu |

Framework samples in other files:

* `web/login.feature`: valid and invalid login as Scenario Outlines, on the site's login practice page.
* `api/posts_api.feature`: GET, GET with a query, POST, PUT, PATCH and DELETE, using Jackson POJOs.
* `db/post_persistence.feature`: a post created via the API is stored, verified and then deleted in MySQL.

---

## 1. Prerequisites

| Tool | Version | Notes |
|---|---|---|
| JDK | 17+ | `java -version` |
| Maven | 3.9+ | `mvn -v` |
| Browser | Chrome, Firefox or Edge | Selenium Manager downloads the matching driver automatically |
| MySQL | 8+ | Only for `@db` scenarios |

## 2. Setup

```bash
git clone <repo> && cd DemoProject
mvn -q test-compile                # downloads dependencies, compiles
```

### 2.1 Secret key (required for ENC(...) values)

Encrypted values are decrypted at runtime with an AES-256 key read from the environment variable
**`DEMO_SECRET_KEY`**. Never commit the key.

```bash
# generate a new key (after mvn compile)
java -cp target/classes com.demoproject.utils.EncryptionUtils generate-key
```

```powershell
# Windows - current session
$env:DEMO_SECRET_KEY = "<base64 key>"
# Windows - persist for your user
setx DEMO_SECRET_KEY "<base64 key>"
```

```bash
# Linux / macOS
export DEMO_SECRET_KEY="<base64 key>"
```

In CI, store the key as a masked secret variable.

### 2.2 Encrypting a password

```bash
java -cp target/classes com.demoproject.utils.EncryptionUtils encrypt "MyS3cret!"
# prints ENC(...)  ->  paste into config.properties or testdata.properties
```

If you generate a new key, re-encrypt every `ENC(...)` value with it. The samples are
`app.password`, `db.password` and `login.valid.password`. Decrypted values are never logged.

### 2.3 MySQL (only for `@db`)

```sql
-- src/test/resources/db/schema.sql
CREATE DATABASE IF NOT EXISTS demoproject;
```

Set `db.url`, `db.username` and `db.password=ENC(...)` in `config.properties`.
The scenario also creates its table (`CREATE TABLE IF NOT EXISTS`).

---

## 3. Running

```bash
mvn clean test                                             # everything, config defaults
mvn test -Denv=UAT                                         # UAT overrides (UAT.* keys)
mvn test -Dbrowser=firefox -Dheadless=true                 # browser / headless
mvn test -Dcucumber.filter.tags="@smoke"                   # by tag
mvn test -Dcucumber.filter.tags="@ui and not @alerts"
mvn test -Dcucumber.filter.tags="not @db"                  # without MySQL
mvn test -Dcucumber.filter.tags="@api"                     # API only (no browser opened)
mvn test -Dparallel.thread.count=8                         # parallel scenarios
mvn test -Drerun.count=0                                   # disable retries
mvn test -Dscreenshot.mode=ALL -Dvideo.recording=ALL       # evidence for every scenario
mvn test -Dapp.url=https://other-host/AutomationPractice/  # any config key
mvn test -Dlogin.valid.username=someone                    # any test-data key
```

Precedence for every config and test-data key: **`-Dkey=value` > `<ENV>.key` > `key`**.

Tags: `@ui` (starts a browser), `@api`, `@db`, `@smoke`, `@regression`, `@negative`, `@positive`,
plus one per module (`@formControls`, `@autoSuggest`, `@alerts`, `@windows`, `@webTables`,
`@elementState`, `@login`, `@posts`). `@ignore` is excluded by default.

## 4. Reports, screenshots, videos, logs

Each run creates `reports/Run_<yyyyMMdd_HHmmss>/`:

```
reports/Run_20261014_151649/
├── index.html        Extent Spark report (system info: env, browser, OS, Java, threads)
├── screenshots/      PNGs per screenshot.mode (also attached to the Cucumber report)
├── videos/           Motion-JPEG AVI per scenario per video.recording (linked from the report)
└── logs/execution.log
```

Only the newest `report.history.count` run folders are kept. Cucumber's own HTML and JSON reports
are written to `target/cucumber-reports/`.

* **Retries**: a failed scenario is rerun up to `rerun.count` times. The Extent report shows each
  scenario once, with its final result. Earlier attempts are removed, and the surviving entry notes
  the retry number and the previous failure.
* **Video**: each UI scenario's own browser is recorded on a background thread, one recorder per
  scenario thread. Frames go to a temp folder and are encoded into an AVI. Recordings that don't
  match `video.recording` are deleted. API/DB scenarios are never recorded, and recording problems
  are logged as warnings without failing the test.

---

## 5. Test data

* **Simple values** live in `src/test/resources/testdata/testdata.properties`. Keys are grouped by
  module (`login.*`, `formcontrols.*`, `api.*`, `db.*` …). Override per environment with
  `QA.<key>` or `UAT.<key>`.
* **Feature files pass keys, not values**:
  `When user logs in with "login.valid.username" and "login.valid.password"`.
  Steps resolve them through `TestDataReader`. A string that isn't a key is used as a literal.
* **Secrets** use `ENC(...)`.
* **Placeholders** are evaluated each time a value is read: `${timestamp}`, `${uuid}`,
  `${random.alpha:N}`, `${random.numeric:N}`, and `${other.key}`. Example:
  `api.post.create.title=Automation post ${random.alpha:6}`.
* **API bodies** are JSON templates in `testdata/payloads/`, filled from `testdata.properties`
  (values are JSON-escaped) and read into POJOs with Jackson.
* **Missing keys fail fast**, naming the key, the environment and every location checked.

Adding data: add `module.purpose.name=value` (plus optional `UAT.module.purpose.name=...`) and
reference the key from the feature file.

Avoid test-data keys that clash with JVM system properties (`user.name`, `java.version`, `os.name`),
because `-D`/system properties take precedence.

---

## 6. Project structure

```
DemoProject
├── pom.xml
├── testng.xml                       listeners: ParallelSuiteConfigurer, RetryTransformer
├── README.md
└── src
    ├── main/java/com/demoproject
    │   ├── config        ConfigReader, ConfigKeys, LayeredProperties, CaptureMode
    │   ├── core          FrameworkBootstrap
    │   ├── driver        DriverFactory, DriverManager (ThreadLocal), BrowserType
    │   ├── pages         BasePage, PracticePage, LoginPage, FormControlsPage, AutoSuggestPage,
    │   │                 AlertsPage, WindowsAndFramesPage, WebTablesPage, ElementStatePage
    │   ├── api
    │   │   ├── client    ApiClient, ResponseValidator
    │   │   ├── specs     SpecFactory (Request/ResponseSpecBuilder)
    │   │   ├── filters   ExtentLoggingFilter (masked request/response logging)
    │   │   └── models    Post
    │   ├── db            DbUtils (ThreadLocal connection, PreparedStatement only)
    │   ├── utils         WaitUtils, EncryptionUtils, PlaceholderResolver, JsonUtils,
    │   │                 ScreenshotUtils, FileHelper
    │   ├── reporting     ExtentManager, ExtentTestManager, ExtentCucumberListener,
    │   │                 ReportContext, ReportHistoryCleaner, ScenarioAttemptTracker, HtmlEscaper
    │   ├── video         VideoRecorder, VideoRecorderManager, AviWriter
    │   ├── testdata      TestDataReader, PayloadReader
    │   ├── logging       LoggingConfigurator, LogFormatter
    │   └── exceptions    FrameworkException
    └── test
        ├── java/com/demoproject
        │   ├── runners          TestRunner (AbstractTestNGCucumberTests, parallel DataProvider)
        │   ├── listeners        RetryAnalyzer, RetryTransformer, ParallelSuiteConfigurer
        │   ├── hooks            Hooks
        │   ├── context          ScenarioContext
        │   └── stepdefinitions  BaseSteps, CommonSteps, LoginSteps, FormControlsSteps,
        │                        AutoSuggestSteps, AlertsSteps, WindowsAndFramesSteps,
        │                        WebTablesSteps, ElementStateSteps, ApiSteps, DbSteps
        └── resources
            ├── config/config.properties
            ├── testdata/testdata.properties
            ├── testdata/payloads/  create_post.json, update_post.json, patch_post.json
            ├── features/web/practice/  01..05 module features
            ├── features/web/login.feature
            ├── features/api/posts_api.feature
            ├── features/db/post_persistence.feature
            ├── db/schema.sql
            └── cucumber.properties
```

---

## 7. Design decisions and deviations

| Topic | Decision | Why |
|---|---|---|
| Login sample | Uses `/loginpagePractise/` (`app.login.url`) | The AutomationPractice page has no login form |
| API sample | JSONPlaceholder (`api.base.url`) | Stable public REST API with GET/POST/PUT/PATCH/DELETE |
| DB sample | The step stores the API-created post in MySQL, then verifies it | JSONPlaceholder does not write to *your* database. With a real backend, drop the insert step and keep the verification step |
| SQL table names | Taken from test data, validated by `DbUtils.identifier()` and back-quoted | JDBC can't bind identifiers as `?`; every value still goes through PreparedStatement |
| Step-to-step state | `ScenarioContext` (ThreadLocal) | DI containers (cucumber-picocontainer) are not in the allowed dependency list |
| Extent step logging | `ExtentCucumberListener`, a Cucumber `ConcurrentEventListener` | Gets every step result on the executing thread, so the ThreadLocal node is correct in parallel |
| Unexpected alerts | `unhandledPromptBehavior=IGNORE` | Stops a background video screenshot from silently dismissing an alert the test is checking |
| Video capture rate | Fixed *delay* between frames (not fixed rate) | Screenshots can take longer than the interval; back-to-back captures would delay the test's own WebDriver commands |
| Short-lived messages | `BasePage.watchTransientText()` records banner text in the browser (MutationObserver) | The login error banner is visible for only about 2 s; polling over WebDriver can miss it under parallel load |
| Recording format | Motion-JPEG AVI written with the JDK only (no new dependency), linked from the report | Browsers cannot play AVI inline, so the report links to the file; it opens in the OS video player |
| Secrets | Decrypted on access, not at startup | Suites that don't need a secret (e.g. API only) run without `DEMO_SECRET_KEY` |
| Report retries | Previous attempt's node removed via `ExtentReports.removeTest` | Final result shown once with retry notes. TestNG/Cucumber's own reports still list each attempt (TestNG marks retried attempts as skipped) |
| Cucumber version | 7.x (not 8.x) | As specified |
