# Annotation Tidy

`Annotation Tidy` is an IntelliJ IDEA plugin for cleaning up Java annotation blocks so they read more clearly and stay consistent.

## MVP Scope

- Works on the currently open Java file
- Reorders annotations on the same declaration
- Splits stacked annotations into standalone lines
- Keeps Java modifiers in a stable order after the annotation block
- Currently targets class, method, and field declarations to avoid over-formatting parameter lists

## Sorting Rules

Annotations are ordered by common semantic groups, then by name:

1. Language-level annotations such as `@Override` and `@Deprecated`
2. Nullability and contract annotations such as `@NotNull` and `@Nullable`
3. Validation annotations such as `@Valid` and `@Size`
4. Dependency-injection annotations such as `@Inject` and `@Autowired`
5. Web mapping annotations such as `@GetMapping`
6. Persistence annotations such as `@Entity` and `@Column`
7. All other annotations in alphabetical order

## Triggering the Action

- `Tools -> Tidy Java Annotations`
- Editor context menu -> `Tidy Java Annotations`
- Shortcut: `Ctrl + Alt + Shift + A`

## Project Layout

- `src/main/kotlin/com/bingo/annotationtidy/AnnotationLayoutService.kt`: core annotation tidy logic
- `src/main/kotlin/com/bingo/annotationtidy/TidyJavaAnnotationsAction.kt`: IDE action entry point
- `src/test/kotlin/com/bingo/annotationtidy/AnnotationLayoutServiceTest.kt`: basic behavior test

## Local Run Notes

This project is configured with the official IntelliJ Platform Gradle Plugin `2.12.0` and targets:

- Gradle `8.13`
- Java `17`
- IntelliJ IDEA Community `2025.3.3`

Typical commands:

```bash
gradle wrapper
./gradlew runIde
./gradlew test
```

## Next Improvements

- Add a settings page for custom annotation groups and priorities
- Provide intentions for tidying only the current class or method
- Preserve relative order for special framework annotations when needed
- Add project-wide batch tidy support
