package io.axoniq.demo.bikerental.rental.paymentsaga;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentStateRepository extends JpaRepository<PaymentState, String> {
    Optional<PaymentState> findByPaymentReference(String paymentReference);
    List<PaymentState> findAllByTimestampLessThanAndStatusIn(long timestamp, PaymentState.Status... statuses);
}
