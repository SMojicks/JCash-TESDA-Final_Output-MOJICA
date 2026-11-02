package Service;

import Model.Transaction;
import Model.User;
import Util.DatabaseConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BankingService {

    public User login(String mobileNumber, String pin) throws IllegalArgumentException {
        String query = "SELECT * FROM users WHERE mobile_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, mobileNumber);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                if (rs.getString("pin").equals(pin)) {
                    return new User(
                            rs.getLong("id"),
                            rs.getString("mobile_number"),
                            rs.getString("pin"),
                            rs.getString("full_name"),
                            rs.getDouble("balance")
                    );
                } else {
                    throw new IllegalArgumentException("Incorrect PIN.");
                }
            } else {
                throw new IllegalArgumentException("Mobile number is incorrect or does not exist.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    public void cashIn(User user, double amount) throws IllegalArgumentException {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");

        String updateBalance = "UPDATE users SET balance = balance + ? WHERE id = ?";
        String logTransaction = "INSERT INTO transactions (user_id, type, amount, details) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false); // Start transaction

            try (PreparedStatement updateStmt = conn.prepareStatement(updateBalance);
                 PreparedStatement logStmt = conn.prepareStatement(logTransaction)) {

                updateStmt.setDouble(1, amount);
                updateStmt.setLong(2, user.getUserId());
                updateStmt.executeUpdate();

                logStmt.setLong(1, user.getUserId());
                logStmt.setString(2, "Cash-In");
                logStmt.setDouble(3, amount);
                logStmt.setString(4, "Added funds via CLI");
                logStmt.executeUpdate();

                conn.commit();
                user.setBalance(user.getBalance() + amount); // Update local object
            } catch (SQLException e) {
                conn.rollback();
                throw new RuntimeException("Cash-in failed: " + e.getMessage());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Connection error: " + e.getMessage());
        }
    }

    public void transfer(User sender, String receiverMobile, double amount) throws IllegalArgumentException {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be greater than zero.");
        if (sender.getBalance() < amount) throw new IllegalArgumentException("Insufficient balance.");
        if (sender.getMobileNumber().equals(receiverMobile)) throw new IllegalArgumentException("Cannot transfer to yourself.");

        String checkReceiver = "SELECT id, full_name FROM users WHERE mobile_number = ?";
        String updateBalance = "UPDATE users SET balance = balance + ? WHERE id = ?";
        String logTransaction = "INSERT INTO transactions (user_id, type, amount, details) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                // 1. Verify Receiver
                PreparedStatement checkStmt = conn.prepareStatement(checkReceiver);
                checkStmt.setString(1, receiverMobile);
                ResultSet rs = checkStmt.executeQuery();
                if (!rs.next()) throw new IllegalArgumentException("Receiver does not exist.");

                long receiverId = rs.getLong("id");
                String receiverName = rs.getString("full_name");

                // 2. Deduct from Sender
                PreparedStatement deductStmt = conn.prepareStatement(updateBalance);
                deductStmt.setDouble(1, -amount);
                deductStmt.setLong(2, sender.getUserId());
                deductStmt.executeUpdate();

                // 3. Add to Receiver
                PreparedStatement addStmt = conn.prepareStatement(updateBalance);
                addStmt.setDouble(1, amount);
                addStmt.setLong(2, receiverId);
                addStmt.executeUpdate();

                // 4. Log Sender Transaction
                PreparedStatement logSender = conn.prepareStatement(logTransaction);
                logSender.setLong(1, sender.getUserId());
                logSender.setString(2, "Transfer-Out");
                logSender.setDouble(3, amount);
                logSender.setString(4, "Sent to " + receiverName);
                logSender.executeUpdate();

                // 5. Log Receiver Transaction
                PreparedStatement logReceiver = conn.prepareStatement(logTransaction);
                logReceiver.setLong(1, receiverId);
                logReceiver.setString(2, "Transfer-In");
                logReceiver.setDouble(3, amount);
                logReceiver.setString(4, "Received from " + sender.getFullName());
                logReceiver.executeUpdate();

                conn.commit();
                sender.setBalance(sender.getBalance() - amount); // Update local object
            } catch (SQLException | IllegalArgumentException e) {
                conn.rollback();
                throw new IllegalArgumentException(e.getMessage());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Database error: " + e.getMessage());
        }
    }

    public List<Transaction> getTransactionLogs(long userId) {
        List<Transaction> logs = new ArrayList<>();
        String query = "SELECT * FROM transactions WHERE user_id = ? ORDER BY date_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setLong(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                logs.add(new Transaction(
                        rs.getString("type"),
                        rs.getDouble("amount"),
                        rs.getString("details"),
                        rs.getTimestamp("date_time").toLocalDateTime()
                ));
            }
        } catch (SQLException e) {
            System.out.println("Error fetching logs: " + e.getMessage());
        }
        return logs;
    }
    public boolean userExists(String mobileNumber) {
        String query = "SELECT 1 FROM users WHERE mobile_number = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, mobileNumber);
            ResultSet rs = stmt.executeQuery();
            return rs.next(); // Returns true if a record is found, false otherwise

        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return false;
        }
    }
}