# OTel Span Naming Companion

Gutter warning icon on any Java/Kotlin `tracer.spanBuilder(...)`/
`tracer.startSpan(...)` call whose name argument is built from a
dynamic value — string concatenation in Java, string template
interpolation in Kotlin — instead of a static literal. Per
OpenTelemetry's own documented naming guidance, each unique id/value
becomes its own span name in the tracing backend instead of a
low-cardinality attribute — the classic high-cardinality-span-name
footgun that quietly blows up cost and usability in a real tracing
backend.

## Why it exists

It's an easy, natural-looking mistake: `tracer.spanBuilder("order " +
orderId)` reads like a helpful, descriptive span name in the source,
but every distinct order id creates a brand-new span name in the
tracing backend — the opposite of what OpenTelemetry's naming
convention recommends (low-cardinality names, with the dynamic value as
a separate attribute instead). Nothing in the IDE flags this today.

## Why built this way

- **100% static text/PSI analysis** — matches the method name by simple
  text, so it works whether the real OpenTelemetry SDK is on the
  classpath or not.
- **Deliberately narrow trigger** — only a direct `+` concatenation
  (Java) or interpolated string template (Kotlin) written directly in
  the call's first argument is flagged, keeping false positives low.

## v0.1 scope — stated honestly, not exhaustively

A name built earlier and passed in as a single variable (`String name =
"order " + id; tracer.spanBuilder(name)`) isn't covered yet. Doesn't
validate the static part of the name against the `{verb} {object}`
semantic convention — only the high-cardinality anti-pattern.

## Usage

Open any Java/Kotlin file using an OpenTelemetry `Tracer`. A
`spanBuilder`/`startSpan` call whose name mixes a literal with a
dynamic value shows a warning icon.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
