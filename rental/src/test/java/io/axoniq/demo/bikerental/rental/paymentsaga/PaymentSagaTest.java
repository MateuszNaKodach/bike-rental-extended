package io.axoniq.demo.bikerental.rental.paymentsaga;

import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentPreparedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import org.axonframework.messaging.commandhandling.gateway.CommandDispatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentSagaTest {

    @Mock
    private PaymentStateRepository repository;
    @Mock
    private CommandDispatcher commandDispatcher;

    private PaymentSaga saga;

    @BeforeEach
    void setUp() {
        saga = new PaymentSaga(repository);
    }

    @Test
    void shouldStartSagaOnBikeRequested() {
        var event = new BikeRequestedEvent("bikeId", "renter", "payRef");

        saga.on(event, commandDispatcher);

        var captor = ArgumentCaptor.forClass(PaymentState.class);
        verify(repository).save(captor.capture());
        assertEquals("bikeId", captor.getValue().getBikeId());
        assertEquals("renter", captor.getValue().getRenter());
        verify(commandDispatcher).send(new PreparePaymentCommand(10, "payRef"));
    }

    @Test
    void shouldAcceptRequestOnPaymentConfirmed() {
        var state = new PaymentState("bikeId", "renter", "rentalRef");
        when(repository.findByPaymentReference("rentalRef")).thenReturn(Optional.of(state));

        saga.on(new PaymentConfirmedEvent("paymentId", "rentalRef"), commandDispatcher);

        verify(commandDispatcher).send(new ApproveRequestCommand("bikeId", "renter"));
        verify(repository).deleteById("bikeId");
    }

    @Test
    void shouldRejectRequestOnPaymentRejected() {
        var state = new PaymentState("bikeId", "renter", "rentalRef");
        when(repository.findByPaymentReference("rentalRef")).thenReturn(Optional.of(state));

        saga.on(new PaymentRejectedEvent("paymentId", "rentalRef"), commandDispatcher);

        verify(commandDispatcher).send(new RejectRequestCommand("bikeId", "renter"));
    }

    @Test
    void shouldDeleteStateWhenRequestIsRejected() {
        saga.on(new RequestRejectedEvent("bikeId"));
        verify(repository).deleteById("bikeId");
    }

    @Test
    void shouldUpdateStateOnPaymentPrepared() {
        var state = new PaymentState("bikeId", "renter", "rentalRef");
        when(repository.findByPaymentReference("rentalRef")).thenReturn(Optional.of(state));

        saga.on(new PaymentPreparedEvent("paymentId", 10, "rentalRef"));

        assertEquals(PaymentState.Status.PREPARED, state.getStatus());
    }

    // Note: deadline-based test (shouldRejectPaymentWhenNotConfirmedIn30Seconds) is not testable
    // after DeadlineManager removal — see TODO AF5 comments in PaymentSaga.
}
