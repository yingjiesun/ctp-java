package catchthepattern.com.models;

import java.time.Instant;

import com.ib.client.Decimal;

public class OrderStatusInfo {
    private final int orderId;
    private String status;
    private Decimal filled;
    private Decimal remaining;
    private String errorMessage;
    private Instant createdAt;
    private Instant lastUpdatedAt;

    public OrderStatusInfo(int orderId) {
        this.orderId = orderId;
        this.createdAt = Instant.now();
        this.lastUpdatedAt = Instant.now();
    }

    public int getOrderId() { return orderId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status; 
        this.lastUpdatedAt = Instant.now();
    }
    public Decimal getFilled() { return filled; }
    public void setFilled(Decimal filled) { 
        this.filled = filled; 
        this.lastUpdatedAt = Instant.now();
    }
    public Decimal getRemaining() { return remaining; }
    public void setRemaining(Decimal remaining) { 
        this.remaining = remaining; 
        this.lastUpdatedAt = Instant.now();
    }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { 
        this.errorMessage = errorMessage; 
        this.lastUpdatedAt = Instant.now();
    }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getLastUpdatedAt() { return lastUpdatedAt; }

    public boolean isCompleted() {
        return "Filled".equalsIgnoreCase(status) || "Cancelled".equalsIgnoreCase(status) || "Error".equalsIgnoreCase(status);
    }
}
