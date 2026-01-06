# Vulnerable Spring Boot Demo (JPA)

> **For local testing only. Do NOT deploy.**
This project intentionally demonstrates common vulnerabilities so you can test detection and JSON reporting in a code security agent.

## Vulnerabilities
- SQL Injection via JPA native query concatenation (`/login` in `RawJpaLoginRepo`)
- Reflected XSS (`/search`)
- Path Traversal (`/upload`, `/file`)
- Command Injection (`/exec`)
- SSRF (`/fetch`)
- Open Redirect (`/redirect`)
- Insecure Deserialization (`/deserialize`)
- Weak Random (`/random-token?type=weak`)
- Hardcoded Secrets (`/secrets`, `application.properties`)
- Overly permissive CORS (`WebConfig`)
- Actuator exposed without auth (`management.endpoints.web.exposure.include=*`)
- PII logging (credentials printed)
- Outdated dependencies (`commons-io:2.4`, `jackson-databind:2.9.6`)

## Run
```bash
mvn clean package
mvn spring-boot:run
# App runs on http://localhost:8080
```

## Sample requests
- SQLi: `GET http://localhost:8080/login?username=alice' OR '1'='1&password=anything`
- XSS: `GET http://localhost:8080/search?term=<img src=x onerror=alert(1)>`
- Command Injection: `GET http://localhost:8080/exec?cmd=whoami`
- Path Traversal (write): `POST /upload` with `path=../../tmp/test.bin`
- Path Traversal (read): `GET /file?name=../../etc/hosts`
- SSRF: `GET http://localhost:8080/fetch?url=http://127.0.0.1:8080/actuator`
- Open Redirect: `GET http://localhost:8080/redirect?to=http://example.com`
- Insecure Deserialization: `POST /deserialize` binary Java-serialized payload
- Weak Random: `GET http://localhost:8080/random-token?type=weak`
- Secret Exposure: `GET http://localhost:8080/secrets`
- Actuator exposure: `GET http://localhost:8080/actuator`, `GET http://localhost:8080/actuator/env`

## Notes
- Uses H2 in-memory DB; schema auto-created by JPA, seeded via `data.sql`.
- All examples are intentionally unsafe for agent testing.
