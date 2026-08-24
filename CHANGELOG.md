<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# OTel Span Naming Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Gutter warning icon on any Java/Kotlin `tracer.spanBuilder(...)`/
  `tracer.startSpan(...)` call whose name argument mixes a static
  literal with a dynamic value (string concatenation/interpolation) --
  the high-cardinality-span-name anti-pattern.
- 100% static text/PSI analysis, Java and Kotlin, no network calls, no
  telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/otel-span-naming-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/otel-span-naming-companion/commits/0.1.0
