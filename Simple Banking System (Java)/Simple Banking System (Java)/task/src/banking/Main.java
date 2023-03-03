package banking;

public class Main {
    public static void main(String[] args) {
        CardDB db = new CardDB(args[1]);
        CardGenerator generator = new CardGenerator(db);
        Messager messager = new Messager();
        BankController controller = new BankController(generator,messager, db);
        controller.run();

    }
}