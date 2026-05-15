package io.axoniq.demo.bikerental.rental;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import org.axonframework.extension.spring.config.EventProcessorDefinition;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.jpa.TokenEntry;
// TODO(af5-saga): import org.axonframework.modelling.saga.repository.jpa.SagaEntry — SagaEntry package moved in AF5; restore when saga recipe is applied
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

// TODO(af5-saga): restore SagaEntry.class in @EntityScan when saga recipe is applied
@EntityScan(basePackageClasses = {BikeStatus.class, TokenEntry.class})
@SpringBootApplication
public class RentalApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalApplication.class, args);
    }

    // TODO(af5-saga): migrate DeadlineManager — SimpleDeadlineManager/ConfigurationScopeAwareProvider removed in AF5.
    // Replace with the AF5 deadline mechanism once the saga recipe is applied to PaymentSaga.

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService workerExecutorService() {
        return Executors.newScheduledThreadPool(4);
    }

    @Autowired
    public void configureSerializers(ObjectMapper objectMapper) {
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(),
                                           ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
    }

    @Bean
    public EventProcessorDefinition paymentSagaProcessor() {
        return EventProcessorDefinition
                .pooledStreaming("PaymentSagaProcessor")
                .assigningHandlers(descriptor -> descriptor.beanType().getPackageName()
                                                           .startsWith("io.axoniq.demo.bikerental.rental.paymentsaga"))
                // TODO(af5): initialToken(StreamableMessageSource::createHeadToken) removed — AF5 TrackingTokenSource.latestToken() requires ProcessingContext; processor will replay from beginning on first start
                .customized(c -> c.workerExecutor(workerExecutorService())
                                  .initialSegmentCount(2)
                                  .batchSize(100));
    }

    @Bean
    public EventProcessorDefinition rentalQueryProcessor() {
        return EventProcessorDefinition
                .pooledStreaming("io.axoniq.demo.bikerental.rental.query")
                .assigningHandlers(descriptor -> descriptor.beanType().getPackageName()
                                                           .startsWith("io.axoniq.demo.bikerental.rental.query"))
                .customized(c -> c.workerExecutor(workerExecutorService())
                                  .batchSize(100));
    }
}
