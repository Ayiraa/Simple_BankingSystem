package banking;

import java.sql.*;

public class CardDB {
    private Connection connection;

    public CardDB(String filename) {
        try {
            // Load the JDBC driver class
            Class.forName("org.sqlite.JDBC");

            // Open a connection to the database
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + filename);

            // Create the card table if it doesn't exist
            String createTableSQL = "CREATE TABLE IF NOT EXISTS card (id INTEGER PRIMARY KEY, number TEXT, pin TEXT, balance INTEGER DEFAULT 0);";
            Statement statement = connection.createStatement();
            statement.executeUpdate(createTableSQL);
            statement.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addCard(Card card) throws SQLException {
        String sql = "SELECT * FROM card WHERE number = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, card.getCardNumber());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                // Card already exists in database
                throw new IllegalArgumentException("Card with number " + card.getCardNumber() + " already exists");
            } else {
                // Card does not exist in database - insert new row
                sql = "INSERT INTO card (number, pin, balance) VALUES (?, ?, ?)";
                try (PreparedStatement pstmt2 = connection.prepareStatement(sql)) {
                    pstmt2.setString(1, card.getCardNumber());
                    pstmt2.setString(2, card.getPin());
                    pstmt2.setInt(3, card.getBalance());
                    pstmt2.executeUpdate();
                }
            }
        }
    }


    public Card getCard(String number){
        try {
            String selectSQL = "SELECT * FROM card WHERE number = ?;";
            PreparedStatement preparedStatement = connection.prepareStatement(selectSQL);
            preparedStatement.setString(1, number);
            ResultSet resultSet = preparedStatement.executeQuery();
            Card card = null;
            if (resultSet.next()) {
                String accountNumber = resultSet.getString("number");
                String pin = resultSet.getString("pin");
                int balance = resultSet.getInt("balance");
                card = new Card(accountNumber, pin, balance);
            }
            resultSet.close();
            preparedStatement.close();
            return card;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void close(){
        try {
            connection.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public boolean validateCard(String cardNumber, String pin) throws SQLException {
        String sql = "SELECT * FROM card WHERE number = ? AND pin = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, cardNumber);
            pstmt.setString(2, pin);
            ResultSet rs = pstmt.executeQuery();
            return rs.next();
        }
    }
    public void printCardTable() throws SQLException {
        String sql = "SELECT * FROM card;";
        Statement stmt;
        try {
            stmt = connection.createStatement();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                int id = rs.getInt("id");
                String number = rs.getString("number");
                String pin = rs.getString("pin");
                int balance = rs.getInt("balance");
                System.out.printf("%d %s %s %d\n", id, number, pin, balance);
            }

    }

    public void addIncome(String number, int income) throws SQLException {
        String updateSQL = "UPDATE card SET balance = balance + ? WHERE number = ?;";
        PreparedStatement preparedStatement = connection.prepareStatement(updateSQL);
        preparedStatement.setInt(1, income);
        preparedStatement.setString(2, number);
        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public void doTransfer(String senderCardNumber, String receiverCardNumber, int amount) throws SQLException {
        if (!hasEnoughMoney(senderCardNumber, amount)) {
            System.out.println("Not enough money!");
            return;
        }
            try {
                connection.setAutoCommit(false);
                // Deduct money from the sender's account
                String updateSenderSQL = "UPDATE card SET balance = balance - ? WHERE number = ?";
                PreparedStatement updateSenderStatement = connection.prepareStatement(updateSenderSQL);
                updateSenderStatement.setInt(1, amount);
                updateSenderStatement.setString(2, senderCardNumber);
                updateSenderStatement.executeUpdate();

                // Add money to the receiver's account
                String updateReceiverSQL = "UPDATE card SET balance = balance + ? WHERE number = ?";
                PreparedStatement updateReceiverStatement = connection.prepareStatement(updateReceiverSQL);
                updateReceiverStatement.setInt(1, amount);
                updateReceiverStatement.setString(2, receiverCardNumber);
                updateReceiverStatement.executeUpdate();

                connection.commit();
                System.out.println("Success!");
            } catch (SQLException e) {
                connection.rollback();
                throw new SQLException("Transfer failed", e);
            } finally {
                connection.setAutoCommit(true);
            }
    }

    public boolean validateTransfer(String senderCardNumber, String receiverCardNumber) {
        // Check if the sender and receiver are different
        if (senderCardNumber.equals(receiverCardNumber)) {
            System.out.println("You can't transfer money to the same account!");
            return false;
        }

        // Check if the receiver's card number passes the Luhn algorithm
        if (!isLuhnValid(receiverCardNumber)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            return false;
        }

        // Check if the receiver's card number exists in the database
        Card receiverCard = getCard(receiverCardNumber);
        if (receiverCard == null) {
            System.out.println("Such a card does not exist.");
            return false;
        }

        // Validation passed
        return true;
    }
    public boolean hasEnoughMoney(String cardNumber, int amount) {
        Card card = getCard(cardNumber);
        return card != null && card.getBalance() >= amount;
    }
    public static boolean isLuhnValid(String cardNumber) {
        int[] digits = new int[cardNumber.length()];
        for (int i = 0; i < cardNumber.length(); i++) {
            digits[i] = Integer.parseInt(cardNumber.substring(i, i + 1));
        }
        for (int i = digits.length - 2; i >= 0; i = i - 2) {
            int j = digits[i];
            j = j * 2;
            if (j > 9) {
                j = j % 10 + 1;
            }
            digits[i] = j;
        }
        int sum = 0;
        for (int digit : digits) {
            sum += digit;
        }
        return sum % 10 == 0;
    }

    public void deleteCard(String number) throws SQLException {
        try {
            connection.setAutoCommit(false);
            String deleteSQL = "DELETE FROM card WHERE number = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(deleteSQL);
            preparedStatement.setString(1, number);
            int rowsDeleted = preparedStatement.executeUpdate();
            if (rowsDeleted == 1) {
                System.out.println("The account has been closed!");
                connection.commit();
            } else {
                System.out.println("Error: no account was found with that number.");
                connection.rollback();
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
            connection.rollback();
        } finally {
            connection.setAutoCommit(true);
        }
    }



}


