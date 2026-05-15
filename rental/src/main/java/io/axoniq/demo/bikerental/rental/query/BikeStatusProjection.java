package io.axoniq.demo.bikerental.rental.query;

import io.axoniq.demo.bikerental.coreapi.rental.BikeInUseEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRegisteredEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeReturnedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeStatus;
import io.axoniq.demo.bikerental.coreapi.rental.RentalStatus;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import org.axonframework.messaging.core.QualifiedName;
import org.axonframework.messaging.core.annotation.Namespace;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.queryhandling.QueryUpdateEmitter;
import org.axonframework.messaging.queryhandling.annotation.QueryHandler;
import org.springframework.stereotype.Component;

@Namespace("io.axoniq.demo.bikerental.rental.query")
@Component
public class BikeStatusProjection {

    private final BikeStatusRepository bikeStatusRepository;

    public BikeStatusProjection(BikeStatusRepository bikeStatusRepository) {
        this.bikeStatusRepository = bikeStatusRepository;
    }

    @EventHandler
    public void on(BikeRegisteredEvent event, QueryUpdateEmitter updateEmitter) {
        var bikeStatus = new BikeStatus(event.getBikeId(), event.getBikeType(), event.getLocation());
        bikeStatusRepository.save(bikeStatus);
        updateEmitter.emit(new QualifiedName("findAll"), q -> true, bikeStatus);
    }

    @EventHandler
    public void on(BikeRequestedEvent event, QueryUpdateEmitter updateEmitter) {
        bikeStatusRepository.findById(event.getBikeId())
                            .map(bs -> {
                                bs.requestedBy(event.getRenter());
                                return bs;
                            })
                            .ifPresent(bs -> {
                                updateEmitter.emit(new QualifiedName("findAll"), q -> true, bs);
                                updateEmitter.emit(String.class, event.getBikeId()::equals, bs);
                            });
    }

    @EventHandler
    public void on(BikeInUseEvent event, QueryUpdateEmitter updateEmitter) {
        bikeStatusRepository.findById(event.getBikeId())
                            .map(bs -> {
                                bs.rentedBy(event.getRenter());
                                return bs;
                            })
                            .ifPresent(bs -> {
                                updateEmitter.emit(new QualifiedName("findAll"), q -> true, bs);
                                updateEmitter.emit(String.class, event.getBikeId()::equals, bs);
                            });
    }

    @EventHandler
    public void on(BikeReturnedEvent event, QueryUpdateEmitter updateEmitter) {
        bikeStatusRepository.findById(event.getBikeId())
                            .map(bs -> {
                                bs.returnedAt(event.getLocation());
                                return bs;
                            })
                            .ifPresent(bs -> {
                                updateEmitter.emit(new QualifiedName("findAll"), q -> true, bs);
                                updateEmitter.emit(String.class, event.getBikeId()::equals, bs);
                            });
    }

    @EventHandler
    public void on(RequestRejectedEvent event, QueryUpdateEmitter updateEmitter) {
        bikeStatusRepository.findById(event.getBikeId())
                            .map(bs -> {
                                bs.returnedAt(bs.getLocation());
                                return bs;
                            })
                            .ifPresent(bs -> {
                                updateEmitter.emit(new QualifiedName("findAll"), q -> true, bs);
                                updateEmitter.emit(String.class, event.getBikeId()::equals, bs);
                            });
    }

    @QueryHandler(queryName = "findAll")
    public Iterable<BikeStatus> findAll() {
        return bikeStatusRepository.findAll();
    }

    @QueryHandler(queryName = "findAvailable")
    public Iterable<BikeStatus> findAvailable(String bikeType) {
        return bikeStatusRepository.findAllByBikeTypeAndStatus(bikeType, RentalStatus.AVAILABLE);
    }

    @QueryHandler(queryName = "findOne")
    public BikeStatus findOne(String bikeId) {
        return bikeStatusRepository.findById(bikeId).orElse(null);
    }
}
