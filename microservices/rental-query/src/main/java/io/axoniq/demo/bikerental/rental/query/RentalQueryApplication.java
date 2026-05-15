package io.axoniq.demo.bikerental.rental.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import org.axonframework.extension.spring.config.EventProcessorDefinition;
import org.axonframework.messaging.eventhandling.processing.streaming.token.store.jpa.TokenEntry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@EntityScan(basePackageClasses = {BikeStatus.class, TokenEntry.class})
@SpringBootApplication
public class RentalQueryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentalQueryApplication.class, args);
    }

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService workerExecutorService() {
        return Executors.newScheduledThreadPool(2);
    }

    @Autowired
    public void configureSerializers(ObjectMapper objectMapper) {
        objectMapper.activateDefaultTyping(objectMapper.getPolymorphicTypeValidator(), ObjectMapper.DefaultTyping.JAVA_LANG_OBJECT);
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
