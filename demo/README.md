# Demo data for screenshots

`OrderService.java` — `process` concatenates the order id into the span
name (flagged), `processCorrect` uses a static name with the id as an
attribute instead (not flagged).

## How to get the screenshot

1. `./gradlew runIde` from `otel-span-naming-companion`, open this
   `demo/` folder as the project.
2. Full Screen, open `OrderService.java` — a warning icon should appear
   on the `spanBuilder` call in `process` but not in `processCorrect`.
3. Screenshot with both methods visible, save into
   `otel-span-naming-companion/docs/screenshots/`. Close the sandbox.
