package io.axoniq.demo.bikerental.coreapi.payment

import org.axonframework.messaging.queryhandling.annotation.Query

@Query(name = "getStatus")
data class GetPaymentStatusQuery(val paymentId: String)

@Query(name = "getPaymentId")
data class GetPaymentIdQuery(val paymentReference: String)

@Query(name = "getAllPayments")
data class GetAllPaymentsQuery(val status: PaymentStatus.Status?)
