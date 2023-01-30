package com.miya10kei

import io.micrometer.tracing.Tracer
import io.micrometer.tracing.otel.bridge.*
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.ContextPropagators
import io.opentelemetry.exporter.zipkin.ZipkinSpanExporterBuilder
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import io.opentelemetry.sdk.trace.samplers.Sampler
import zipkin2.reporter.urlconnection.URLConnectionSender

class TracerFactory {

    companion object {

        fun create(): Tracer {
            val spanExporter = ZipkinSpanExporterBuilder()
                .setSender(URLConnectionSender.create("http://localhost:9411/api/v2/spans"))
                .build()
            val sdkTracerProvider = SdkTracerProvider.builder()
                .setSampler(Sampler.alwaysOn())
//                .addSpanProcessor(BatchSpanProcessor.builder(spanExporter).build())
                .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))
                .build()
            val openTelemetrySdk = OpenTelemetrySdk.builder()
                .setTracerProvider(sdkTracerProvider)
                .setPropagators(ContextPropagators.create(W3CTraceContextPropagator.getInstance()))
                .build()
            val otelTracer = openTelemetrySdk.tracerProvider
                .get("io.micrometer.micrometer-tracing")

            val slf4JEventListener = Slf4JEventListener()
            val slf4JBaggageEventListener = Slf4JBaggageEventListener(emptyList())
            val otelCurrentTraceContext = OtelCurrentTraceContext()

            return OtelTracer(
                otelTracer,
                otelCurrentTraceContext,
                { event ->
                    slf4JEventListener.onEvent(event);
                    slf4JBaggageEventListener.onEvent(event)
                },
                OtelBaggageManager(otelCurrentTraceContext, emptyList(), emptyList())
            )
        }
    }
}