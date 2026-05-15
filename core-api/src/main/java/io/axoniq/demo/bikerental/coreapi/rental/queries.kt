package io.axoniq.demo.bikerental.coreapi.rental

import org.axonframework.messaging.queryhandling.annotation.Query

@Query(name = "findAll")
class FindAllBikesQuery

@Query(name = "findOne")
data class FindBikeByIdQuery(val bikeId: String)

@Query(name = "findAvailable")
data class FindAvailableBikesQuery(val bikeType: String)
