package dev.gaphunter.otelspannamingcompanion.detect

/**
 * Method names this plugin treats as "creates an OpenTelemetry span with
 * a name argument" -- matched by simple name only, never resolved to
 * the real `io.opentelemetry.api.trace.Tracer` interface, same
 * "match a known name, don't resolve a symbol" discipline used
 * catalog-wide.
 */
object SpanSignals {
    val SPAN_NAME_METHODS = setOf("spanBuilder", "startSpan")
}
