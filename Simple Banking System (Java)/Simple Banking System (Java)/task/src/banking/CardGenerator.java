package banking;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Random;

public class CardGenerator {
    private static final Random random = new Random();

    private final CardDB db;



    public CardGenerator(CardDB db) {
        this.db = db;
    }

    public  String generatePIN(){

        return String.format("%04d", random.nextInt(10000));
    }

    public  String generateAccountNumber(){
        return String.format("%9d", 100000000 + random.nextInt(900000000));
    }

    public  String generateLastDigit(String number){
        String numberToProcess = "400000" + number;
        int[] intArr = Arrays.stream(numberToProcess.split(""))
                .mapToInt(Integer::parseInt)
                .toArray();
        int sum = 0;
        for (int i = 0; i < intArr.length; i++) {
            if ((i) % 2 == 0) {
                int doubled = intArr[i] * 2;
                intArr[i] = doubled > 9 ? doubled - 9 : doubled;
            }
            sum += intArr[i];
        }
        int checkDigit = (sum * 9) % 10;
        return String.format("%s", checkDigit);
    }

    public String generateCardNumber(){
        String accountNumber = generateAccountNumber();
        return "400000".concat(accountNumber).concat(generateLastDigit(accountNumber));
    }

    public Card generateCard(){
        Card newCard =new Card(generateCardNumber(), generatePIN());
        try {
            db.addCard(newCard);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return newCard;
    }


}
