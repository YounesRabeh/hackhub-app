# AppMap Setup

This project is configured to record AppMap data for the HackHub Spring Boot
backend.

## Configuration Files

- `appmap.yml` tells AppMap which application code to record.
- `build.gradle` applies the AppMap Gradle plugin and connects it to
  `appmap.yml`.
- `.gitignore` excludes generated AppMap recordings.

The current AppMap package scope is:

```yaml
name: hack-club-app
packages:
  - path: com.hackhub
```

This means AppMap records code under the `com.hackhub` Java package.

## Gradle Test Recording

The AppMap Gradle plugin is installed in `build.gradle`:

```gradle
id 'com.appland.appmap' version '1.2.0'
```

The plugin adds the AppMap Java agent to the Gradle `test` task when the
`appmap` task is included.

Run tests with AppMap enabled:

```bash
./gradlew appmap test
```

If Gradle has trouble with the local daemon or cache, use the project-local
Gradle home:

```bash
GRADLE_USER_HOME=.gradle-home ./gradlew appmap test --no-daemon
```

Test recordings are written to:

```text
build/appmap/junit/
```

The AppMap agent log is written to:

```text
build/appmap/agent.log
```

## Available AppMap Gradle Commands

List all available Gradle tasks:

```bash
./gradlew tasks --all
```

Validate the AppMap configuration:

```bash
./gradlew appmap-validate-config
```

Print the AppMap Java agent JAR path used by Gradle:

```bash
./gradlew appmap-print-jar-path
```

Record AppMaps from tests:

```bash
./gradlew appmap test
```

## Live HTTP Request Recording

For live Spring Boot request recording, run the application with the AppMap Java
agent attached.

If the AppMap editor extension installed the Java agent, it is usually located
at:

```text
$HOME/.appmap/lib/java/appmap.jar
```

Build the boot jar:

```bash
./gradlew bootJar
```

Run the application with AppMap:

```bash
java \
  -javaagent:$HOME/.appmap/lib/java/appmap.jar \
  -Dappmap.config.file=appmap.yml \
  -Dappmap.output.directory=tmp/appmap \
  -jar build/libs/hackhub-0.9.2.jar \
  --spring.profiles.active=dev
```

Then use the API normally, for example through Swagger:

```text
http://localhost:8080/swagger-ui.html
```

HTTP request recordings are written to:

```text
tmp/appmap/request_recording/
```

## Notes

- `build/appmap/` is ignored because it is inside Gradle's `build/` directory.
- `tmp/appmap/` and `.appmap/` are ignored explicitly.
- The project uses Java 25. AppMap may print Java agent or Byte Buddy warnings
  during startup. These warnings do not necessarily mean recording failed.
