package Main;

import Model.Transaction;
import Model.User;
import Service.BankingService;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        BankingService service = new BankingService();
        User loggedInUser = null;
        int attempts = 0;

        printHeader();

        while (attempts < 3 && loggedInUser == null) {
            System.out.println(" ┌────────────────────────────────────────┐");
            System.out.println(" │             ACCOUNT LOGIN              │");
            System.out.println(" └────────────────────────────────────────┘");
            System.out.print("   ► Mobile Number : ");
            String mobile = scanner.nextLine();
            System.out.print("   ► PIN           : ");
            String pin = scanner.nextLine();

            try {
                loggedInUser = service.login(mobile, pin);
            } catch (IllegalArgumentException e) {
                attempts++;
                System.out.println("\n [!] " + e.getMessage() + " Attempts left: " + (3 - attempts) + "\n");
            } catch (RuntimeException e) {
                System.out.println("\n [!] Login Error: " + e.getMessage() + "\n");
                break;
            }
        }

        if (loggedInUser == null) {
            System.out.println("\n [X] Maximum login attempts reached. Security lock engaged.");
            scanner.close();
            return;
        }

        boolean running = true;
        while (running) {
            System.out.println("\n ╔════════════════════════════════════════╗");
            System.out.println(" ║               MAIN MENU                ║");
            System.out.println(" ╠════════════════════════════════════════╣");
            System.out.printf(" ║    User    : %-24s  ║\n", loggedInUser.getFullName());
            System.out.printf(" ║    Balance : PHP %-20.2f  ║\n", loggedInUser.getBalance());
            System.out.println(" ╠════════════════════════════════════════╣");
            System.out.println(" ║  [1] Check Balance                     ║");
            System.out.println(" ║  [2] Cash-In                           ║");
            System.out.println(" ║  [3] Transfer Money                    ║");
            System.out.println(" ║  [4] Transaction Logs                  ║");
            System.out.println(" ║  [5] Exit                              ║");
            System.out.println(" ╚════════════════════════════════════════╝");
            System.out.print("   ► Select an option: ");

            String choice = scanner.nextLine();
            System.out.println();

            switch (choice) {
                case "1":
                    System.out.println(" ┌────────────────────────────────────────┐");
                    System.out.printf(" │  CURRENT BALANCE: PHP %-16.2f │\n", loggedInUser.getBalance());
                    System.out.println(" └────────────────────────────────────────┘");
                    break;
                case "2":
                    System.out.println(" ─── CASH-IN ───");
                    System.out.print("   ► Enter amount: PHP ");
                    try {
                        double amount = Double.parseDouble(scanner.nextLine());
                        service.cashIn(loggedInUser, amount);
                        System.out.println("   [✓] Cash-in successful! New Balance: PHP " + loggedInUser.getBalance());
                    } catch (NumberFormatException e) {
                        System.out.println("   [!] Invalid input. Please enter a valid number.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("   [!] Error: " + e.getMessage());
                    }
                    break;
                case "3":
                    System.out.println(" ─── TRANSFER MONEY ───");
                    System.out.print("   ► Enter receiver's mobile : ");
                    String receiverMobile = scanner.nextLine();

                    // Validate existence immediately
                    if (!service.userExists(receiverMobile)) {
                        System.out.println("   [!] Error: Receiver account does not exist.");
                        break;
                    }
                    if (receiverMobile.equals(loggedInUser.getMobileNumber())) {
                        System.out.println("   [!] Error: You cannot transfer money to yourself.");
                        break;
                    }

                    System.out.print("   ► Enter amount            : PHP ");
                    try {
                        double transferAmt = Double.parseDouble(scanner.nextLine());
                        service.transfer(loggedInUser, receiverMobile, transferAmt);
                        System.out.println("   [✓] Transfer successful! Remaining Balance: PHP " + loggedInUser.getBalance());
                    } catch (NumberFormatException e) {
                        System.out.println("   [!] Invalid input. Please enter a valid number.");
                    } catch (IllegalArgumentException e) {
                        System.out.println("   [!] Transfer Failed: " + e.getMessage());
                    }
                    break;
                case "4":
                    System.out.println(" ┌───────────────────────────────────────────────────────────────────────────────────────┐");
                    System.out.println(" │                                  TRANSACTION LOGS                                     │");
                    System.out.println(" ├─────────────────────┬──────────────┬──────────────────┬────────────────────────────────┤");
                    System.out.println(" │ DATE & TIME         │ TYPE         │ AMOUNT (PHP)     │ DETAILS                        │");
                    System.out.println(" ├─────────────────────┼──────────────┼──────────────────┼────────────────────────────────┤");

                    List<Transaction> logs = service.getTransactionLogs(loggedInUser.getUserId());
                    if (logs.isEmpty()) {
                        System.out.println(" │ No transactions found.                                                                │");
                    } else {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        for (Transaction t : logs) {
                            String dateStr = t.getDateTime().format(formatter);
                            String typeStr = t.getType().length() > 12 ? t.getType().substring(0, 12) : t.getType();
                            // Increased limit from 14 to 30 characters
                            String detailsStr = t.getDetails().length() > 30 ? t.getDetails().substring(0, 30) : t.getDetails();

                            // Updated format specifier to %-30s
                            System.out.printf(" │ %-19s │ %-12s │ %-16.2f │ %-30s │\n",
                                    dateStr, typeStr, t.getAmount(), detailsStr);
                        }
                    }
                    System.out.println(" └─────────────────────┴──────────────┴──────────────────┴────────────────────────────────┘");
                    break;
                case "5":
                    running = false;
                    System.out.println(" [✓] Thank you for using JCash. Goodbye!");
                    break;
                default:
                    System.out.println(" [!] Invalid selection. Please try again.");
            }
        }
        scanner.close();
    }

    private static void printHeader() {
        System.out.println("\n\n");
        System.out.println(" ▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄");
        System.out.println(" ██                                                                ██");
        System.out.println(" ██                ██ ██████╗  █████╗  ██████╗ ██╗  ██╗            ██");
        System.out.println(" ██                ██║██╔════╝ ██╔══██╗██╔════╝██║  ██║            ██");
        System.out.println(" ██                ██║██║      ███████║███████╗███████║            ██");
        System.out.println(" ██           ██   ██║██║      ██╔══██║╚════██║██╔══██║            ██");
        System.out.println(" ██           ╚█████╔╝╚██████╗ ██║  ██║███████║██║  ██║            ██");
        System.out.println(" ██            ╚════╝  ╚═════╝ ╚═╝  ╚═╝╚══════╝╚═╝  ╚═╝            ██");
        System.out.println(" ██                   JCash Bank of the Philippines                ██");
        System.out.println(" ██                                                                ██");
        System.out.println(" ▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀");
        System.out.println("\n");
    }
}