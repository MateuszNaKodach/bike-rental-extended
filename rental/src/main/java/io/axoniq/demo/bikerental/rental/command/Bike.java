package io.axoniq.demo.bikerental.rental.command;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeInUseEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRegisteredEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.BikeReturnedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RegisterBikeCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestBikeCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.ReturnBikeCommand;
import org.axonframework.eventsourcing.annotation.EventSourcingHandler;
import org.axonframework.eventsourcing.annotation.reflection.EntityCreator;
import org.axonframework.extension.spring.stereotype.EventSourced;
import org.axonframework.messaging.commandhandling.CommandExecutionException;
import org.axonframework.messaging.commandhandling.annotation.CommandHandler;
import org.axonframework.messaging.eventhandling.gateway.EventAppender;

import java.util.Objects;
import java.util.UUID;

// TODO #LLM: reconfigure snapshot trigger (AF4 had snapshotTriggerDefinition = "bikeSnapshotDefinition")
@EventSourced(tagKey = "Bike", idType = String.class)
public class Bike {

    private String bikeId;

    private boolean isAvailable;
    private String reservedBy;
    private boolean reservationConfirmed;

    /* We need to explicitly declare this one to support the constructor for Jackson */
    @EntityCreator
    public Bike() {
    }

    /* Constructor used to reconstruct the aggregate from a JSON based snapshot with Jackson */
    @JsonCreator
    public Bike(@JsonProperty("bikeId") String bikeId,
                @JsonProperty("available") boolean isAvailable,
                @JsonProperty("reservedBy") String reservedBy,
                @JsonProperty("reservationConfirmed") boolean reservationConfirmed) {
        this.bikeId = bikeId;
        this.isAvailable = isAvailable;
        this.reservedBy = reservedBy;
        this.reservationConfirmed = reservationConfirmed;
    }

    @CommandHandler
    public void handle(RegisterBikeCommand command, EventAppender eventAppender) {
        eventAppender.append(new BikeRegisteredEvent(command.getBikeId(), command.getBikeType(), command.getLocation()));
    }

    @CommandHandler
    public String handle(RequestBikeCommand command, EventAppender eventAppender) {
        if (!this.isAvailable) {
            throw new CommandExecutionException("Bike is already rented", null, "Already rented");
        }
        String rentalReference = UUID.randomUUID().toString();
        eventAppender.append(new BikeRequestedEvent(command.getBikeId(), command.getRenter(), rentalReference));

        return rentalReference;
    }

    @CommandHandler
    public void handle(ApproveRequestCommand command, EventAppender eventAppender) {
        if (!Objects.equals(reservedBy, command.getRenter())
                || reservationConfirmed) {
            return;
        }
        eventAppender.append(new BikeInUseEvent(command.getBikeId(), command.getRenter()));
    }

    @CommandHandler
    public void handle(RejectRequestCommand command, EventAppender eventAppender) {
        if (!Objects.equals(reservedBy, command.getRenter())
                || reservationConfirmed) {
            return;
        }
        eventAppender.append(new RequestRejectedEvent(command.getBikeId()));
    }

    @CommandHandler
    public void handle(ReturnBikeCommand command, EventAppender eventAppender) {
        if (this.isAvailable) {
            throw new IllegalStateException("Bike was already returned");
        }
        eventAppender.append(new BikeReturnedEvent(command.getBikeId(), command.getLocation()));
    }

    @EventSourcingHandler
    protected void handle(BikeRegisteredEvent event) {
        this.bikeId = event.getBikeId();
        this.isAvailable = true;
    }

    @EventSourcingHandler
    protected void handle(BikeReturnedEvent event) {
        this.isAvailable = true;
        this.reservationConfirmed = false;
        this.reservedBy = null;
    }

    @EventSourcingHandler
    protected void handle(BikeRequestedEvent event) {
        this.reservedBy = event.getRenter();
        this.reservationConfirmed = false;
        this.isAvailable = false;
    }

    @EventSourcingHandler
    protected void handle(RequestRejectedEvent event) {
        this.reservedBy = null;
        this.reservationConfirmed = false;
        this.isAvailable = true;
    }

    @EventSourcingHandler
    protected void on(BikeInUseEvent event) {
        this.isAvailable = false;
        this.reservationConfirmed = true;
    }

    // getters for Jackson / JSON Serialization

    @SuppressWarnings("unused")
    public String getBikeId() {
        return bikeId;
    }

    @SuppressWarnings("unused")
    public boolean isAvailable() {
        return isAvailable;
    }

    @SuppressWarnings("unused")
    public String getReservedBy() {
        return reservedBy;
    }

    @SuppressWarnings("unused")
    public boolean isReservationConfirmed() {
        return reservationConfirmed;
    }
}
