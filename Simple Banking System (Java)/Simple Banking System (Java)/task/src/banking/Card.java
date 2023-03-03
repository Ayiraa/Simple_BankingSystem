package banking;

public class Card {

    String accountNumber;
    String cardNumber;

    String lastDigit;

    int balance;

    String pin;

    public Card(String cardNumber,String pin) {
        this.cardNumber = cardNumber;
        this.accountNumber=cardNumber.substring(6,15);
        this.lastDigit=cardNumber.substring(15);
        this.pin=pin;
        this.balance=0;
    }
    public Card(String cardNumber,String pin, int balance) {
        this.cardNumber = cardNumber;
        this.accountNumber=cardNumber.substring(6,15);
        this.lastDigit=cardNumber.substring(15);
        this.pin=pin;
        this.balance=balance;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public int getBalance() {
        return balance;
    }

    public String getPin() {
        return pin;
    }

    @Override
    public String toString() {
        return "Card{" +
                "accountNumber='" + accountNumber + '\'' +
                ", cardNumber='" + cardNumber + '\'' +
                ", lastDigit='" + lastDigit + '\'' +
                ", balance=" + balance +
                ", pin='" + pin + '\'' +
                '}';
    }
}
