package Model;

import java.util.ArrayList;
import java.util.List;

public class User {
    private long userId;
    private String mobileNumber;
    private String pin;
    private String fullName;
    private double balance;
    private List<Transaction> transactionList;

    public User(long userId, String mobileNumber, String pin, String fullName, double balance) {
        this.userId = userId;
        this.mobileNumber = mobileNumber;
        this.pin = pin;
        this.fullName = fullName;
        this.balance = balance;
        this.transactionList = new ArrayList<>();
    }

    public long getUserId() { return userId; }
    public void setUserId(long userId) { this.userId = userId; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public List<Transaction> getTransactionList() { return transactionList; }
    public void setTransactionList(List<Transaction> transactionList) { this.transactionList = transactionList; }
}
