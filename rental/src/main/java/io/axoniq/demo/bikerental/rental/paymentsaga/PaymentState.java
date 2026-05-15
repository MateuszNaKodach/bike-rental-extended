package io.axoniq.demo.bikerental.rental.paymentsaga;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PaymentState {

    @Id
    private String bikeId;
    private String paymentReference;
    private String renter;
    private Status status;
    private long timestamp;

    public PaymentState() {}

    public PaymentState(String bikeId, String renter, String paymentReference) {
        this.bikeId = bikeId;
        this.renter = renter;
        this.paymentReference = paymentReference;
        this.status = Status.PENDING;
        this.timestamp = System.currentTimeMillis();
    }

    public String getBikeId() { return bikeId; }
    public String getPaymentReference() { return paymentReference; }
    public String getRenter() { return renter; }
    public Status getStatus() { return status; }
    public long getTimestamp() { return timestamp; }

    public void setStatus(Status status) { this.status = status; }

    public enum Status { PENDING, PREPARED, CONFIRMED, REJECTED }
}
