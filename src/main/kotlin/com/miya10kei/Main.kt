package com.miya10kei

import io.micrometer.tracing.Span
import io.micrometer.tracing.Tracer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

fun main() {
    val logger: Logger = LoggerFactory.getLogger("Main")
    val tracer = TracerFactory.create()
    val rootSpan = tracer.nextSpan()
        .name("root-span")
        .tag("rootTag1", "1")
        .tag("rootTag2", "2")

    tracer.withSpan(rootSpan.start()).use {
//        tracer.createBaggage("baggage_scope1", "baggage_scope1_value")
        logger.info("start root span")
        rootSpan.event("rootEvent1", System.currentTimeMillis(), TimeUnit.MILLISECONDS)

        childProcess(logger, tracer, rootSpan, "child")

        rootSpan.event("rootEvent2", System.currentTimeMillis(), TimeUnit.MILLISECONDS)
        rootSpan.end()
        logger.info("end root span")
    }
}

fun childProcess(logger: Logger, tracer: Tracer, parentSpan: Span, name: String) {
    logger.info("start $name span")
    val span = tracer.nextSpan(parentSpan)
        .name("child-span")
        .tag("${name}Tag1", "1")
        .tag("${name}Tag2", "2")
        .start()

    span.event("${name}Event1", System.currentTimeMillis(), TimeUnit.MILLISECONDS)

    Thread.sleep(100L)
    grandChildProcess(logger, tracer, span, "grandChild")

    span.event("${name}Event2", System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    span.end()
    logger.info("end $name span")
}

fun grandChildProcess(logger: Logger, tracer: Tracer, parentSpan: Span, name: String) {
    logger.info("start $name span")
    val span = tracer.nextSpan(parentSpan)
        .remoteServiceName("child")
        .name("child-span")
        .tag("${name}Tag1", "1")
        .tag("${name}Tag2", "2")
        .start()

    span.event("${name}Event1", System.currentTimeMillis(), TimeUnit.MILLISECONDS)

    Thread.sleep(100L)
    try {
        throw Exception()
    } catch (e: Exception) {
        span.error(e)
    }

    span.event("${name}Event2", System.currentTimeMillis(), TimeUnit.MILLISECONDS)
    span.end()
    logger.info("end $name span")
}
