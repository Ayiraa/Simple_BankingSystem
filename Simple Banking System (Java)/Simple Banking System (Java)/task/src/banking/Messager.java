package banking;

public class Messager {
    public void showMenu(){
        System.out.println("""
                
                1. Create an account
                2. Log into account
                0. Exit
                
                """);
    }

    public void showLoggedInOptions(){
        System.out.println("""
                1. Balance
                2. Add income
                3. Do transfer
                4. Close account
                5. Log out
                0. Exit
                """);
    }

    public void cardCreated(String cardNumber, String PIN){
        System.out.println("""
                Your card has been created
                Your card number:""");
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(PIN);
    }
}
