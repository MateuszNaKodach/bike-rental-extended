package io.axoniq.demo.bikerental.payment;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import io.axoniq.framework.axonserver.connector.api.AxonServerConnectionManager;
import io.axoniq.framework.axonserver.connector.event.AggregateBasedAxonServerEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.extension.spring.config.EventProcessorDefinition;
import org.axonframework.messaging.eventhandling.conversion.EventConverter;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.jpa.TokenEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EntityScan(basePackageClasses = {PaymentStatus.class, TokenEntry.class})
@SpringBootApplication
public class PaymentApplication {

    public static void main(String[] args) {
        SpringApplication.run(PaymentApplication.class, args);
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService workerExecutorService() {
        return Executors.newScheduledThreadPool(4);
    }

    @Autowired
    public void configureSerializers(ObjectMapper objectMapper) {
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
    }

    @Bean
    public EventStorageEngine eventStorageEngine(AxonServerConnectionManager connectionManager,
                                                  EventConverter eventConverter) {
        return new AggregateBasedAxonServerEventStorageEngine(
                connectionManager.getConnection(),
                eventConverter
        );
    }

    @Bean
    public EventProcessorDefinition paymentProcessor() {
        return EventProcessorDefinition
                .pooledStreaming("io.axoniq.demo.bikerental.payment")
                .assigningHandlers(descriptor -> descriptor.beanType().getPackageName()
                                                           .startsWith("io.axoniq.demo.bikerental.payment"))
                .customized(c -> c.workerExecutor(workerExecutorService())
                                  .batchSize(100));
    }
}
