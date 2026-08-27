<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# OTel Span Naming Companion Changelog

## [Unreleased]

## [0.1.1]

### Fixed

- **Java detector never actually fired** (found during manual testing
  2026-08-24, present since 0.1.0): `PsiPolyadicExpression.operationTokenType`
  compares as `IElementType`, whose `toString()` returns `"PLUS"`, not
  the `"+"` symbol the check compared against -- so the concatenation
  check always failed silently and no warning was ever shown, in any
  version. Fixed by comparing against `JavaTokenType.PLUS` directly.
- Also fixed the `LineMarkerInfo` anchor: it pointed at a leaf inside
  the span name argument instead of the method call itself, which the
  platform's line-marker collection pass never picked up in practice
  (same anchoring discipline as the rest of the catalog, applied to
  the wrong element). The Kotlin detector (string interpolation) was
  not affected by either bug.

## [0.1.0]

### Added

- Gutter warning icon on any Java/Kotlin `tracer.spanBuilder(...)`/
  `tracer.startSpan(...)` call whose name argument mixes a static
  literal with a dynamic value (string concatenation/interpolation) --
  the high-cardinality-span-name anti-pattern.
- 100% static text/PSI analysis, Java and Kotlin, no network calls, no
  telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/otel-span-naming-companion/compare/0.1.1...HEAD
[0.1.1]: https://github.com/GapHunterLabs/otel-span-naming-companion/compare/0.1.0...0.1.1
[0.1.0]: https://github.com/GapHunterLabs/otel-span-naming-companion/commits/0.1.0
