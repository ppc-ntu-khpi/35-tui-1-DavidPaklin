package com.mybank.tui;

import jexer.TAction;
import jexer.TApplication;
import jexer.TField;
import jexer.TText;
import jexer.TWindow;
import jexer.event.TMenuEvent;
import jexer.menu.TMenu;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TUIdemo extends TApplication {

    private static final int ABOUT_APP = 2000;
    private static final int CUST_INFO = 2010;

    private Bank bank;

    public static void main(String[] args) throws Exception {
        TUIdemo tdemo = new TUIdemo();
        (new Thread(tdemo)).start();
    }

    public TUIdemo() throws Exception {
        super(BackendType.SWING);

        addToolMenu();
        TMenu fileMenu = addMenu("&File");
        fileMenu.addItem(CUST_INFO, "&Customer Info");
        fileMenu.addDefaultItem(TMenu.MID_SHELL);
        fileMenu.addSeparator();
        fileMenu.addDefaultItem(TMenu.MID_EXIT);

        addWindowMenu();

        TMenu helpMenu = addMenu("&Help");
        helpMenu.addItem(ABOUT_APP, "&About...");

        setFocusFollowsMouse(true);
        ShowCustomerDetails();
    }

    @Override
    protected boolean onMenu(TMenuEvent menu) {
        if (menu.getId() == ABOUT_APP) {
            messageBox("About", "\t\t\t\t\t   Just a simple Jexer demo.\n\nCopyright \u00A9 2019 Alexander \'Taurus\' Babich")
                    .show();
            return true;
        }
        if (menu.getId() == CUST_INFO) {
            ShowCustomerDetails();
            return true;
        }
        return super.onMenu(menu);
    }

    private void ShowCustomerDetails() {
        TWindow custWin = addWindow("Customer Window", 2, 1, 40, 10, TWindow.NOZOOMBOX);
        custWin.newStatusBar("Enter valid customer number and press Show...");

        custWin.addLabel("Enter customer number: ", 2, 2);
        TField custNo = custWin.addField(24, 2, 3, false);
        TText details = custWin.addText("Owner Name: \nAccount Type: \nAccount Balance: ", 2, 4, 38, 8);
        custWin.addButton("&Show", 28, 2, new TAction() {
            @Override
            public void DO() {
                try {
                    int custNum = Integer.parseInt(custNo.getText());
                    // Get customer details from bank
                    String customerDetails = bank.getCustomerDetails(custNum);
                    details.setText(customerDetails);
                } catch (NumberFormatException e) {
                    messageBox("Error", "You must provide a valid customer number!").show();
                }
            }
        });

        bank = new Bank();
        try {
            bank.loadBankDataFromFile("test.dat");
        } catch (IOException e) {
            messageBox("Error", "Failed to load bank data from file: " + e.getMessage()).show();
        }
    }

    private class Bank {
        private List<Customer> customers;

        public Bank() {
            customers = new ArrayList<>();
        }

        public void loadBankDataFromFile(String filename) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] data = line.split(",");
                    int id = Integer.parseInt(data[0].trim());
                    String name = data[1].trim();
                    String accountType = data[2].trim();
                    double balance = Double.parseDouble(data[3].trim());

                    Account account;
                    if (accountType.equalsIgnoreCase("Checking")) {
                        account = new CheckingAccount(balance);
                    } else if (accountType.equalsIgnoreCase("Unchecking")) {
                        account = new UncheckingAccount(balance);
                    } else {
                        throw new IOException("Invalid account type: " + accountType);
                    }

                    Customer customer = new Customer(id, name);
                    customer.setAccount(account);
                    customers.add(customer);
                }
            }
        }

        public String getCustomerDetails(int customerId) {
            for (Customer customer : customers) {
                if (customer.getId() == customerId) {
                    StringBuilder details = new StringBuilder();
                    details.append("Owner Name: ").append(customer.getName()).append(" (id=").append(customer.getId()).append(")\n");
                    details.append("Account Type: ").append(customer.getAccount().getType()).append("\n");
                    details.append("Account Balance: ").append(customer.getAccount().getBalance());
                    return details.toString();
                }
            }
            return "Customer not found!";
        }
    }

    private class Customer {
        private int id;
        private String name;
        private Account account;

        public Customer(int id, String name) {
            this.id = id;
            this.name = name;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public Account getAccount() {
            return account;
        }

        public void setAccount(Account account) {
            this.account = account;
        }
    }

    private abstract class Account {
        protected double balance;

        public Account(double balance) {
            this.balance = balance;
        }

        public double getBalance() {
            return balance;
        }

        public abstract String getType();
    }

    private class CheckingAccount extends Account {
        public CheckingAccount(double balance) {
            super(balance);
        }

        @Override
        public String getType() {
            return "Checking";
        }
    }

    private class UncheckingAccount extends Account {
        public UncheckingAccount(double balance) {
            super(balance);
        }

        @Override
        public String getType() {
            return "Unchecking";
        }
    }
}
