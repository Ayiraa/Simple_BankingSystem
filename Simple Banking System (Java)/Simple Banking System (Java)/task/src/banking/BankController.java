package banking;

import java.sql.SQLException;
import java.util.Scanner;

public class BankController {
    static Scanner scanner = new Scanner(System.in);
    CardGenerator generator;

    CardDB db;
    Messager messager;

    public BankController(CardGenerator generator, Messager messager, CardDB db) {
        this.generator = generator;
        this.messager = messager;
        this.db=db;
    }

    public void run(){
        String input;
        do {
            messager.showMenu();
            input = scanner.nextLine();
            switch (input) {
                case "1" -> {
                    Card newCard = generator.generateCard();
                    messager.cardCreated(newCard.getCardNumber(), newCard.getPin());
                }
                case "2" -> {
                    System.out.println("Enter your card number:");
                    String number = scanner.nextLine();
                    System.out.println("Enter your PIN:");
                    String pin = scanner.nextLine();
                    try {
                        if (db.validateCard(number, pin)) {
                            System.out.println("You have successfully logged in!");
                            loggedInOperations(scanner, number);
                        } else {
                            System.out.println("Wrong card number or PIN!");
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
                case "0" ->{ db.close();
                    System.out.println("Bye!");}

                default -> System.out.println("Invalid command");
            }
        }while (!input.equals("0"));
    }


    public void loggedInOperations(Scanner scanner, String cardNumber) throws SQLException {
        boolean loggedIn = true;
        while (loggedIn) {
            messager.showLoggedInOptions();
            int choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> getBalance(cardNumber);
                case 2 -> addIncome(cardNumber);
                case 3-> doTransfer(cardNumber);
                case 4-> deleteAccount(cardNumber);
                case 5 -> {
                    System.out.println("You have successfully logged out!");
                    loggedIn = false;
                }
                case 0 -> {
                    System.out.println("Bye!");
                    db.close();
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice");
            }
        }
    }

    private void getBalance(String cardNumber) {
        Card card = db.getCard(cardNumber);
        if (card != null) {
            System.out.println("Balance: " + card.getBalance());
        } else {
            System.out.println("Card not found");
        }
    }

    public void addIncome(String number){
        System.out.println("Enter income:");
        int income = scanner.nextInt();
        try {
            db.addIncome(number, income);
            System.out.println("Income was added!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void doTransfer(String number) throws SQLException {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        String recNumber = scanner.nextLine();
        if(db.validateTransfer(number,recNumber)){
            System.out.println("Enter how much money you want to transfer:");
            int amount = scanner.nextInt();
            db.doTransfer(number,recNumber,amount);
        }
    }
    public void deleteAccount(String number) throws SQLException {
        db.deleteCard(number);
    }

}
