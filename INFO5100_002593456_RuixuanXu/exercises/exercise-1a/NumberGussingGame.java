import java.util.Random;
import java.util.Scanner;

public class NumberGussingGame {

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Random random = new Random();

        int choice;

        do {
            System.out.println("\n=== Number Guessing Game ===");
            System.out.println("1. Start Game");
            System.out.println("2. Exit");
            System.out.print("Enter your choice: ");
            choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    int secretNumber = random.nextInt(10) + 1;
                    boolean guessedCorrectly = false;

                    System.out.println("I have selected a number between 1 and 10.");
                    System.out.println("You have 5 attempts to guess it.");

                    for (int attempt = 1; attempt <= 5; attempt++) {
                        System.out.print("Attempt " + attempt + ": Enter your guess: ");
                        int guess = scanner.nextInt();

                        if (guess > secretNumber) {
                            System.out.println("Too High!");
                        } else if (guess < secretNumber) {
                            System.out.println("Too Low!");
                        } else {
                            System.out.println("Correct!");
                            guessedCorrectly = true;
                            break;
                        }
                    }

                    if (!guessedCorrectly) {
                        System.out.println("The correct number was: " + secretNumber);
                    }
                    break;

                case 2:
                    System.out.println("Exiting game. Goodbye!");
                    break;

                default:
                    System.out.println("Invalid choice.");
            }

        } while (choice != 2);

        scanner.close();
    }
}
