package guiTools;

public class NameRecognizer {
	/**
	 * <p> Title: FSM-translated NameRecognizer. </p>
	 * 
	 * <p> Description: A demonstration of the mechanical translation of Finite State Machine 
	 * diagram into an executable Java program using the Name Recognizer. The code 
	 * detailed design is based on a while loop with a select list</p>
	 * 
	 * <p> Copyright: Riya Nalla @ 2026</p>
	 * 
	 * @author Riya Nalla
	 * 
	 * @version 1.00		2024-02-09	
	 * 
	 */

	/**********************************************************************************************
	 * 
	 * Result attributes to be used for GUI applications where a detailed error message and a 
	 * pointer to the character of the error will enhance the user experience.
	 * 
	 */

    public static String nameRecognizerErrorMessage = "";
    public static String nameRecognizerInput = "";
    public static int nameRecognizerIndexOfError = -1;

    private static int state;
    private static int nextState;
    private static boolean finalState;
    private static boolean running;

    private static String inputLine;
    private static char currentChar;
    private static int currentCharNdx;
    private static int nameSize;

    private static void moveToNextCharacter() {
        currentCharNdx++;
        if (currentCharNdx < inputLine.length())
            currentChar = inputLine.charAt(currentCharNdx);
        else {
            currentChar = ' ';
            running = false;
        }
    }

    public static String checkForValidName(String input) {

        if (input == null || input.length() == 0) {
            nameRecognizerIndexOfError = 0;
            return "\n*** ERROR IN NAME *** Name cannot be empty.";
        }

        // Initialize FSM
        state = 0;
        nextState = -1;
        finalState = false;
        running = true;

        inputLine = input;
        nameRecognizerInput = input;

        currentCharNdx = 0;
        currentChar = input.charAt(0);
        nameSize = 0;

        while (running) {
            switch (state) {

            case 0:
                // First character must be a letter
                if ((currentChar >= 'A' && currentChar <= 'Z') ||
                    (currentChar >= 'a' && currentChar <= 'z')) {

                    nextState = 1;
                    nameSize++;
                } else {
                    running = false;
                }
                break;

            case 1:
                // Letters or hyphens allowed
                if ((currentChar >= 'A' && currentChar <= 'Z') ||
                    (currentChar >= 'a' && currentChar <= 'z') ||
                    currentChar == '-') {

                    nextState = 1;
                    nameSize++;
                } else {
                    running = false;
                }
                break;
            }

            if (running) {
                moveToNextCharacter();
                state = nextState;
                finalState = (state == 1);
                nextState = -1;
            }
        }

        
        nameRecognizerIndexOfError = currentCharNdx;
        nameRecognizerErrorMessage = "\n*** ERROR IN NAME *** ";

        switch (state) {
        case 0:
            nameRecognizerErrorMessage +=
                "A name must start with a letter (A–Z or a–z).";
            return nameRecognizerErrorMessage;

        case 1:
            if (nameSize < 2) {
                nameRecognizerErrorMessage +=
                    "A name must have at least 2 characters.";
                return nameRecognizerErrorMessage;
            }
            else if (currentCharNdx < input.length()) {
                nameRecognizerErrorMessage +=
                    "A name may only contain letters and hyphens (-).";
                return nameRecognizerErrorMessage;
            }
            else {
                // Valid name
                nameRecognizerIndexOfError = -1;
                nameRecognizerErrorMessage = "";
                return nameRecognizerErrorMessage;
            }

        default:
            return "";
        }
    }
}
