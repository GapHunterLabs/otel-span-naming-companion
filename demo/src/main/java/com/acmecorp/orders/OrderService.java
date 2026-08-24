package com.acmecorp.orders;

public class OrderService {

    // Span name mixes a literal with the order id -- high cardinality,
    // flagged.
    void process(String orderId) {
        Span span = tracer.spanBuilder("order " + orderId).startSpan();
    }

    // Static, low-cardinality span name -- not flagged.
    void processCorrect(String orderId) {
        Span span = tracer.spanBuilder("process order").startSpan();
        span.setAttribute("order.id", orderId);
    }
}
