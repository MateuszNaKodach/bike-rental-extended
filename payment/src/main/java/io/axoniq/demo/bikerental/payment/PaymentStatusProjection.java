package io.axoniq.demo.bikerental.payment;

import io.axoniq.demo.bikerental.coreapi.payment.GetAllPaymentsQuery;
import io.axoniq.demo.bikerental.coreapi.payment.GetPaymentIdQuery;
import io.axoniq.demo.bikerental.coreapi.payment.GetPaymentStatusQuery;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentPreparedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus;
import org.axonframework.messaging.core.annotation.Namespace;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.queryhandling.QueryUpdateEmitter;
import org.axonframework.messaging.queryhandling.annotation.QueryHandler;
import org.springframework.stereotype.Component;

import static io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus.Status.APPROVED;
import static io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus.Status.PENDING;
import static io.axoniq.demo.bikerental.coreapi.payment.PaymentStatus.Status.REJECTED;

@Namespace("io.axoniq.demo.bikerental.payment")
@Component
public class PaymentStatusProjection {

    private final PaymentStatusRepository paymentStatusRepository;

    public PaymentStatusProjection(PaymentStatusRepository paymentStatusRepository) {
        this.paymentStatusRepository = paymentStatusRepository;
    }

    @QueryHandler(queryName = "getStatus")
    public PaymentStatus getStatus(GetPaymentStatusQuery query) {
        return paymentStatusRepository.findById(query.getPaymentId()).orElse(null);
    }

    @QueryHandler(queryName = "getPaymentId")
    public String getPaymentId(GetPaymentIdQuery query) {
        return paymentStatusRepository.findByReferenceAndStatus(query.getPaymentReference(), PENDING).map(PaymentStatus::getId).orElse(null);
    }

    @QueryHandler(queryName = "getAllPayments")
    public Iterable<PaymentStatus> findByStatus(GetAllPaymentsQuery query) {
        if (query.getStatus() == null) {
            return paymentStatusRepository.findAll();
        }
        return paymentStatusRepository.findAllByStatus(query.getStatus());
    }

    @EventHandler
    public void handle(PaymentPreparedEvent event, QueryUpdateEmitter updateEmitter) {
        paymentStatusRepository.save(new PaymentStatus(event.getPaymentId(), event.getAmount(), event.getPaymentReference()));
        updateEmitter.emit(GetPaymentIdQuery.class, q -> q.getPaymentReference().equals(event.getPaymentReference()), event.getPaymentId());
    }

    @EventHandler
    public void handle(PaymentConfirmedEvent event) {
        paymentStatusRepository.findById(event.getPaymentId()).ifPresent(s -> s.setStatus(APPROVED));
    }

    @EventHandler
    public void handle(PaymentRejectedEvent event) {
        paymentStatusRepository.findById(event.getPaymentId()).ifPresent(s -> s.setStatus(REJECTED));
    }
}
