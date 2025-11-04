## Codebase Overview

**Project**: API Exporter - Burp Suite Extension
**Language**: Kotlin (JVM 21)
**Build System**: Gradle (Kotlin DSL)
**API**: Burp Suite Montoya API 2025.4
**Architecture**: Event-driven extension with async processing packaged as a shadow/fat JAR

## Build & Packaging
- Always build with `./gradlew --no-daemon --console=plain clean shadowJar` so the bundled artifact includes the Kotlin runtime.
- The distributable lives at `build/libs/<plugin-name-version>.jar`. Use this JAR when loading the extension into Burp Suite.
- The plain `jar` task is disabled; do not re-enable it unless you provide an alternative way to supply the Kotlin stdlib.

## Project Structure

```
``src/main
└── java
    ├── api_parser
    │   ├── ApiFrame.java
    │   ├── ApiTable.java
    │   ├── docType
    │   │   ├── IDocType.java
    │   │   ├── OpenApi31DocType.java
    │   │   └── PostmanDocType.java
    │   ├── Generator.java
    │   └── model
    │       ├── AuthContainer.java
    │       ├── GenerateResponse.java
    │       ├── RequestHeader.java
    │       └── RequestSource.java
    └── burp
        └── BurpExtender.java
```

## Kotlin Implementation Notes
- Follow idiomatic Kotlin style (use `data class`, `sealed` hierarchies, and null-safety instead of Java-style optional handling).
- Prefer immutable collections from `kotlin.collections` unless mutation is required for Montoya callbacks.
- Use coroutines only if you wire them into Montoya's threading expectations; current async work relies on Java executors via Kotlin interop.
- Keep UI code on the Swing EDT by wrapping updates with `SwingUtilities.invokeLater { ... }`.

## Montoya API Access
Because direct MCP access is unavailable, fetch Montoya API documentation via HTTP GET requests to Context7 when you need clarification.

**Endpoint template**
``` https://context7.com/api/v1/portswigger/burp-extensions-montoya-api?type=json&tokens=100000&topic=<TOPIC>
```

**Usage guidelines**
- Form topics with 2–4 technical keywords (e.g., `ui%20suite%20tab`, `http%20request%20builder`).
- Query before changing Montoya-dependent logic or when encountering compilation errors tied to Montoya classes.
- Parse the JSON response and extract method signatures or examples relevant to the task at hand.
- Retry with refined terms if the response lacks the needed details.

## Logging & Diagnostics
- Use `api.logging().logToOutput()` for informational messages and `logToError()` for failures.
- Prefer structured log prefixes (e.g., `[AutoChecker]`) so Burp users can filter messages.
- Async errors should be routed through the logging API within completion handlers.

## Compatibility & Dependencies
- Kotlin JVM toolchain pinned to 21—keep it aligned with the Burp Suite runtime.
- Montoya API dependency is `net.portswigger.burp.extensions:montoya-api:2025.4`; update in lockstep with Burp releases and test against the latest Montoya SDK.
- Avoid adding extra dependencies unless absolutely necessary; bundle everything through the shadow JAR to prevent classpath issues for users.
