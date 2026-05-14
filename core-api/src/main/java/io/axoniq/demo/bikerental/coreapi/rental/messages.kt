package io.axoniq.demo.bikerental.coreapi.rental

import org.axonframework.eventsourcing.annotation.EventTag
import org.axonframework.messaging.commandhandling.annotation.Command
import org.axonframework.messaging.eventhandling.annotation.Event
import org.axonframework.modelling.annotation.TargetEntityId

@Command
data class RegisterBikeCommand(
    @TargetEntityId val bikeId: String,
    val bikeType: String,
    val location: String
)

@Command
data class RequestBikeCommand(@TargetEntityId val bikeId: String, val renter: String)

@Command
data class ApproveRequestCommand(@TargetEntityId val bikeId: String, val renter: String)

@Command
data class RejectRequestCommand(@TargetEntityId val bikeId: String, val renter: String)

@Command
data class ReturnBikeCommand(@TargetEntityId val bikeId: String, val location: String)

@Event
data class BikeRegisteredEvent(@EventTag(key = "Bike")
val bikeId: String, val bikeType: String, val location: String)

@Event
data class BikeRequestedEvent(@EventTag(key = "Bike")
val bikeId: String, val renter: String, val rentalReference: String)

@Event
data class BikeInUseEvent(@EventTag(key = "Bike")
val bikeId: String, val renter: String)

@Event
data class RequestRejectedEvent(@EventTag(key = "Bike")
val bikeId: String)

@Event
data class BikeReturnedEvent(@EventTag(key = "Bike")
val bikeId: String, val location: String)
