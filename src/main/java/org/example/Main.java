package org.example;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class BankAccount {
    private String accountName;
    private double balance;

    public BankAccount(String accountName, double balance) {
        this.accountName = accountName;
        this.balance = balance;
    }

    public String getAccountName() {
        return accountName;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double amount) {
        balance += amount;
        System.out.println("Deposit of " + amount + " successful. New balance: " + balance);
    }

    public void withdraw(double amount) {
        if (balance - amount >= 100) {
            balance -= amount;
            System.out.println("Withdrawal of " + amount + " successful. New balance: " + balance);
        } else {
            System.out.println("Withdrawal failed. Insufficient balance. Current balance: " + balance);
        }
    }
}

class SavingsAccount extends BankAccount {
    public SavingsAccount(String accountName, double balance) {
        super(accountName, balance);
    }

    @Override
    public void withdraw(double amount) {
        if (getBalance() - amount >= 100) {
            super.withdraw(amount);
        } else {
            System.out.println("Minimum balance of 100 required. Withdrawal failed. Current balance: " + getBalance());
        }
    }
}

public class Main {
    private static List<BankAccount> accounts = new ArrayList<>();

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        char option;

        do {
            displayMenu();
            option = scanner.next().charAt(0);
            scanner.nextLine(); // Consume newline character

            switch (option) {
                case 'a':
                    addAccount(scanner);
                    break;
                case 'l':
                    displayAccounts();
                    break;
                case 's':
                    saveToDatabase();
                    break;
                case 'd':
                    depositFunds(scanner);
                    break;
                case 'w':
                    withdrawFunds(scanner);
                    break;
                case 'q':
                    System.out.println("Goodbye!");
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
                    break;
            }
        } while (option != 'q');
    }

    private static void displayMenu() {
        System.out.println("Choose an option: (a)Add Account (l)Display Accounts (s)Save to database (d) Deposit funds (w)Withdraw funds (q)Quit");
    }

    private static void addAccount(Scanner scanner) {
        System.out.print("Enter account name: ");
        String accountName = scanner.nextLine();
        System.out.print("Enter account balance: ");
        double balance = scanner.nextDouble();
        scanner.nextLine(); // Consume newline character

        System.out.print("Choose account type (b)Bank Account (s)Savings Account: ");
        char accountType = scanner.next().charAt(0);
        scanner.nextLine(); // Consume newline character

        BankAccount account;
        if (accountType == 'b') {
            account = new BankAccount(accountName, balance);
        } else if (accountType == 's') {
            account = new SavingsAccount(accountName, balance);
        } else {
            System.out.println("Invalid account type. Defaulting to Bank Account.");
            account = new BankAccount(accountName, balance);
        }

        accounts.add(account);
        System.out.println("Account created successfully.");
    }

    private static void displayAccounts() {
        if (accounts.isEmpty()) {
            System.out.println("No accounts found.");
        } else {
            for (BankAccount account : accounts) {
                System.out.println("Account Name: " + account.getAccountName() + ", Balance: " + account.getBalance());
            }
        }
    }

    private static void depositFunds(Scanner scanner) {
        displayAccounts();
        System.out.print("Choose the account to deposit funds: ");
        int accountIndex = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        if (accountIndex < 0 || accountIndex >= accounts.size()) {
            System.out.println("Invalid account index.");
        } else {
            System.out.print("Enter the amount to deposit: ");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // Consume newline character

            accounts.get(accountIndex).deposit(amount);
        }
    }

    private static void withdrawFunds(Scanner scanner) {
        displayAccounts();
        System.out.print("Choose the account to withdraw funds: ");
        int accountIndex = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        if (accountIndex < 0 || accountIndex >= accounts.size()) {
            System.out.println("Invalid account index.");
        } else {
            System.out.print("Enter the amount to withdraw: ");
            double amount = scanner.nextDouble();
            scanner.nextLine(); // Consume newline character

            accounts.get(accountIndex).withdraw(amount);
        }
    }

    private static void saveToDatabase() {
        try (MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017")) {
            MongoDatabase database = mongoClient.getDatabase("mydatabase");

            MongoCollection<Document> collection = database.getCollection("accounts");

            for (BankAccount account : accounts) {
                Document accountDoc = new Document("accountName", account.getAccountName())
                        .append("balance", account.getBalance())
                        .append("accountType", account instanceof SavingsAccount ? "savings" : "bank");

                collection.insertOne(accountDoc);
            }

            System.out.println("Accounts saved to the database.");
        } catch (Exception e) {
            System.err.println("Error while saving accounts to the database: " + e);
        }
    }
}
