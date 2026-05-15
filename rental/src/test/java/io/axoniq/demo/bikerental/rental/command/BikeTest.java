package io.axoniq.demo.bikerental.rental.command;

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
import org.axonframework.eventsourcing.configuration.EventSourcedEntityModule;
import org.axonframework.eventsourcing.configuration.EventSourcingConfigurer;
import org.axonframework.messaging.commandhandling.CommandExecutionException;
import org.axonframework.test.fixture.AxonTestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;


class BikeTest {

    private AxonTestFixture fixture;

    @BeforeEach
    void setUp() {
        fixture = AxonTestFixture.with(EventSourcingConfigurer.create().registerEntity(EventSourcedEntityModule.autodetected(String.class, Bike.class)));
    }

    @Test
    void canRegisterBike() {
        fixture.given()
               .noPriorActivity()
               .when()
               .command(new RegisterBikeCommand("bikeId", "city", "Amsterdam"))
               .then()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"));
    }

    @Test
    void canRequestAvailableBike() {
        fixture.given()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"))
               .when()
               .command(new RequestBikeCommand("bikeId", "rider"))
               .then()
               .resultMessagePayloadSatisfies(String.class, s -> {})
               .eventsMatch(events -> events.size() == 1
                       && events.get(0).payload() instanceof BikeRequestedEvent e
                       && e.getBikeId().equals("bikeId")
                       && e.getRenter().equals("rider"));
    }

    @Test
    void cannotRequestAlreadyRequestedBike() {
        fixture.given()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when()
               .command(new RequestBikeCommand("bikeId", "rider"))
               .then()
               .noEvents()
               .exception(CommandExecutionException.class);
    }

    @Test
    void canApproveRequestedBike() {
        fixture.given()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when()
               .command(new ApproveRequestCommand("bikeId", "rider"))
               .then()
               .events(new BikeInUseEvent("bikeId", "rider"));
    }

    @Test
    void canRejectRequestedBike() {
        fixture.given()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when()
               .command(new RejectRequestCommand("bikeId", "rider"))
               .then()
               .events(new RequestRejectedEvent("bikeId"));
    }

    @Test
    void canNotRejectRequestedForWrongRequester() {
        fixture.given()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when()
               .command(new RejectRequestCommand("bikeId", "otherRider"))
               .then()
               .success()
               .noEvents();
    }

    @Test
    void cannotApproveRequestedForAnotherRider() {
        fixture.given()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"))
               .when()
               .command(new ApproveRequestCommand("bikeId", "otherRider"))
               .then()
               .noEvents()
               .success();
    }

    @Test
    void canReturnedBikeInUse() {
        fixture.given()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                      new BikeInUseEvent("bikeId", "rider"))
               .when()
               .command(new ReturnBikeCommand("bikeId", "NewLocation"))
               .then()
               .events(new BikeReturnedEvent("bikeId", "NewLocation"));
    }

    @Test
    void cannotRequestBikeInUse() {
        fixture.given()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                      new BikeInUseEvent("bikeId", "rider"))
               .when()
               .command(new RequestBikeCommand("bikeId", "otherRenter"))
               .then()
               .noEvents()
               .exception(CommandExecutionException.class);
    }

    @Test
    void canRequestReturnedBike() {
        fixture.given()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                      new BikeInUseEvent("bikeId", "rider"),
                      new BikeReturnedEvent("bikeId", "NewLocation"))
               .when()
               .command(new RequestBikeCommand("bikeId", "newRider"))
               .then()
               .eventsMatch(events -> events.size() == 1
                       && events.get(0).payload() instanceof BikeRequestedEvent e
                       && e.getBikeId().equals("bikeId")
                       && e.getRenter().equals("newRider"));
    }

    @Test
    void canRequestRejectedBike() {
        fixture.given()
               .events(new BikeRegisteredEvent("bikeId", "city", "Amsterdam"),
                      new BikeRequestedEvent("bikeId", "rider", "rentalId"),
                      new RequestRejectedEvent("bikeId"))
               .when()
               .command(new RequestBikeCommand("bikeId", "newRider"))
               .then()
               .eventsMatch(events -> events.size() == 1
                       && events.get(0).payload() instanceof BikeRequestedEvent e
                       && e.getBikeId().equals("bikeId")
                       && e.getRenter().equals("newRider"));
    }

    @AfterEach
    void tearDown() {
        fixture.stop();
    }
}