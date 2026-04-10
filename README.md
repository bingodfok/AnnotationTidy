# Annotation Tidy

`Annotation Tidy` is an IntelliJ IDEA plugin for quickly tidying annotations on Java classes and methods.

## MVP Scope

- Works on the currently open Java file
- Reorders annotations on the same declaration by annotation text length, from shortest to longest
- Splits stacked annotations into standalone lines
- Keeps Java modifiers in a stable order after the annotation block
- Targets class and method declarations only

## Sorting Rules

Annotations are ordered by their rendered text length, from shortest to longest.

If two annotations have the same length, the plugin falls back to annotation name and then full text for a stable result.

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

- Let users configure their own sorting rules
- Provide an intention action for only the current method
- Add support for field annotations if needed later
- Add project-wide batch tidy support
