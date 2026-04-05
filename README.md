# E-Learning (Guest + Student Scope)

This project currently targets only 2 roles:
- `guest`
- `student`

Teacher/Admin routes are intentionally blocked in security to keep the client scope consistent.

## IntelliJ Setup

1. Open IntelliJ and choose `Open`, then select `e-learning`.
2. Open Maven tool window and reload project from `elearning/pom.xml`.
3. Use Project SDK compatible with Java 8 source level.
4. Enable annotation processing for Lombok:
   - `Settings -> Build, Execution, Deployment -> Compiler -> Annotation Processors`
   - Check `Enable annotation processing`.
5. Run Maven wrapper once from `elearning`:
   - Windows: `mvnw.cmd clean package`
   - Linux/macOS: `./mvnw clean package`

## Database

Default config is in `elearning/src/main/resources/application.properties`.

Required:
- MySQL running locally
- Database named `e-learning`

Credentials:
- Set environment variable `DB_PASSWORD` to your MySQL user password, **or**
- Copy `elearning/src/main/resources/application-local.properties.example` to `application-local.properties` in the same folder and set `spring.datasource.password` (that file is gitignored).
- If `root` has no password locally, the default empty password works as-is.

Optional overrides: `DB_URL`, `DB_USERNAME`, `DB_DRIVER`.

## Notes

- Do not commit real database passwords; use `application-local.properties` or env vars.
