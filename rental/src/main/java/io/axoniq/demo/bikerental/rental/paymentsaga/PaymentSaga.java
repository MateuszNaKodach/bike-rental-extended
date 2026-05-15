package io.axoniq.demo.bikerental.rental.paymentsaga;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentConfirmedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentPreparedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PaymentRejectedEvent;
import io.axoniq.demo.bikerental.coreapi.payment.PreparePaymentCommand;
import io.axoniq.demo.bikerental.coreapi.payment.RejectPaymentCommand;
import io.axoniq.demo.bikerental.coreapi.rental.ApproveRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.BikeRequestedEvent;
import io.axoniq.demo.bikerental.coreapi.rental.RejectRequestCommand;
import io.axoniq.demo.bikerental.coreapi.rental.RequestRejectedEvent;
import org.axonframework.messaging.commandhandling.gateway.CommandDispatcher;
import org.axonframework.messaging.eventhandling.annotation.EventHandler;
import org.axonframework.messaging.eventhandling.replay.annotation.DisallowReplay;
import org.springframework.stereotype.Component;

// TODO AF5: import org.axonframework.deadline.DeadlineManager;
// TODO AF5: import org.axonframework.deadline.annotation.DeadlineHandler;
// TODO AF5: import org.axonframework.messaging.Scope;
// TODO AF5: import org.axonframework.messaging.ScopeDescriptor;

@Component
@DisallowReplay
public class PaymentSaga {

    private final PaymentStateRepository repository;

    public PaymentSaga(PaymentStateRepository repository) {
        this.repository = repository;
    }

    @EventHandler
    public void on(BikeRequestedEvent event, CommandDispatcher commandDispatcher) {
        var state = new PaymentState(event.getBikeId(), event.getRenter(), event.getRentalReference());
        repository.save(state);
        commandDispatcher.send(new PreparePaymentCommand(10, event.getRentalReference()));
    }

    @EventHandler
    public void on(PaymentConfirmedEvent event, CommandDispatcher commandDispatcher) {
        repository.findByPaymentReference(event.getPaymentReference()).ifPresent(state -> {
            commandDispatcher.send(new ApproveRequestCommand(state.getBikeId(), state.getRenter()));
            repository.deleteById(state.getBikeId());
        });
    }

    @EventHandler
    public void on(PaymentRejectedEvent event, CommandDispatcher commandDispatcher) {
        repository.findByPaymentReference(event.getPaymentReference()).ifPresent(state -> {
            commandDispatcher.send(new RejectRequestCommand(state.getBikeId(), state.getRenter()));
            state.setStatus(PaymentState.Status.REJECTED);
        });
    }

    @EventHandler
    public void on(RequestRejectedEvent event) {
        repository.deleteById(event.getBikeId());
        // TODO AF5: DeadlineManager removed — design replacement (e.g. @Scheduled poller on the state entity)
        // deadlineManager.cancelAllWithinScope("cancelPayment");
    }

    @EventHandler
    public void on(PaymentPreparedEvent event) {
        repository.findByPaymentReference(event.getPaymentReference())
                  .ifPresent(state -> state.setStatus(PaymentState.Status.PREPARED));
        // TODO AF5: DeadlineManager removed — design replacement (e.g. @Scheduled poller on the state entity)
        // deadlineManager.schedule(Duration.ofSeconds(30), "cancelPayment", event.getPaymentId());
    }

    // TODO AF5: @DeadlineHandler has no AF5 equivalent — implement as @Scheduled poller or manual scheduler
    // @DeadlineHandler(deadlineName = "cancelPayment")
    // public void cancelPayment(String paymentId) {
    //     commandGateway.send(new RejectPaymentCommand(paymentId));
    // }

    // TODO AF5: @DeadlineHandler has no AF5 equivalent — implement as @Scheduled poller or manual scheduler
    // @DeadlineHandler(deadlineName = "retryPayment")
    // public void preparePayment(String rentalReference) {
    //     ScopeDescriptor scope = Scope.describeCurrentScope();
    //     commandGateway.send(new PreparePaymentCommand(10, rentalReference))
    //                   .whenComplete((r, e) -> {
    //                       if (e != null) {
    //                           deadlineManager.schedule(Duration.ofSeconds(5), "retryPayment", rentalReference, scope);
    //                       }
    //                   });
    // }
}
