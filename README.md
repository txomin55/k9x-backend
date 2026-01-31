# TMANAGER-K9X-BACKEND

The backend follows a hexagonal structure, the following sections explain how the 'conflicting'
parts or those that must be well understood of the application have been developed.

## Gradle resource filtering

As using Gradle resource filtering in Spring Boot application.yml, these variables are defined as
`'@spring.profiles.active@'`, `'@project.artifactId@'`, and `'@project.version@'` and replaced in
`k9x-backend-loader/build.gradle.kts` during `processResources`. You can override the default profile at build time
with `-PspringProfilesActive=production` (default is `develop`).

## Logging

Springboot by default brings a logging configuration that can be overwritten, that is
done through the logback.xml file, but in this case, to avoid more configuration files, the logback-spring.xml is used
where we can also use other Spring annotations (such as that of the profile) to have different configurations depending
on the chosen profile.

## Locale

In the WebMvcConfigurer class, the locale resolver is defined, which in each request it receives looks for the '
Accept-language' header to define the locale in which it has to display the messages.

## Exception handling

Exceptions are collected in CustomExceptionHandler, this is where they are transformed to CustomException so that the
frontend always receives the same fields regardless of the type of error.

## Timeout

The timeout is managed with the filter TimeoutFilter in the web project, this cuts the thread according to the value
defined of 'k9x-backend.timeoutValue' in the application.yml (5000ms by default).

## Validations

The routes and objects that are received in the requests are validated with the @Valid annotation and in case of not
passing it, the CustomExceptionHandler detects it, for an example see the case of createFoo in the web project, where it
receives a CreateFooWeb object and the name value is mandatory, in turn, it tells the label to display in the
appropriate locale. It is one more layer of validation, since the frontend also validates this.

## Spring

TODO

## To take into account with OAUTH2

TODO

## CORS

As it is a SPA intended to be used as a microservice and a microfrontend, the CORS must allow absolutely everything,
since in case an unidentified user wants to access an api resource he will not be able to because he does not have the
authentication token.

## Swagger

The swagger UI is provided by springdoc auto-configuration on the classpath
module).

http://localhost:4000/swagger-ui.html It's configured to be run only in develop profile.

## OpenAPI

OpenAPI is configured via `OpenApiConfiguration`:

- `k9x-backend-infrastructure/src/main/java/com/k9x/infrastructure/in/rest/configuration/openapi/OpenApiConfiguration.java`
- Sets API title to `K9x-Backend API`
- Defines `bearerAuth` security scheme (HTTP bearer, JWT)
- Groups:
  - `secured`: `/api/**` with `bearerAuth` requirement
  - `public`: all non-`/api/**` paths without security (removes security schemes for this group)
- Endpoints are generated in the OAS stubs package (`com.k9x.oas.stub.api`). The loader app scans that package so
  the generated controllers are registered at runtime.
- Use `http://localhost:4000/v3/api-docs/secured` for `/api/**` endpoints.

## The profiles for compiling and executing the application are as follows:

* develop (local server)
* production (IP server specified in the application-production.yml file)

To compile this project, run the build from the repository root. By default, the profile used is `develop` but there are
2 others (explained above). The build targets Java 25; for an exact JVM 25.0.2, set `JAVA_HOME` (or `org.gradle.java.home`)
to a JDK 25.0.2 installation.

The project pulls packages from GitHub Packages; configure credentials via environment variables or local
Gradle properties:

Environment variables:
`GITHUB_ACTOR`, `GITHUB_TOKEN`

Local properties (recommended):
`gpr.user`, `gpr.key` in `gradle.properties` (do not commit real tokens).

`GRADLE_USER_HOME=/tmp/gradle ./gradlew build -PspringProfilesActive=production`

The application has 2 types of implementation as regards persistence, this variable is set with the name of
module.build.tech:

* in-memory (implementation of in-memory bbdd)
* mongo (implementation of mongo bbdd)

For communication between modules, just It has the following implementation, but here you can configure others such as

Kafka, Rabbit ...

## Execution

To execute this project, you must go to the loader project and execute (this way we would use the persistence

implementation in mongo instead of the default one from memory):

`GRADLE_USER_HOME=/tmp/gradle ./gradlew :k9x-backend-loader:bootRun -Dspring.profiles.active=production -Dk9x-backend.deploy.tech=mongo`
