package Model;
import java.time.LocalDateTime;

public class Transaction {
    private String type;
    private double amount;
    private String details;
    private LocalDateTime dateTime;

    public Transaction(String type, double amount, String details, LocalDateTime dateTime) {
        this.type = type;
        this.amount = amount;
        this.details = details;
        this.dateTime = dateTime;
    }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }
}
