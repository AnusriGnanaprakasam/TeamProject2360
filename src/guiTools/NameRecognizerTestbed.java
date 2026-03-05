package guiTools;

import java.util.Scanner;

/*******
 * <p> Title: UserNameRecognizerTestbed. </p>
 * 
 * <p> Description: A console terminal demonstration application</p>
 * 
 * <p> Copyright: Riya Nalla © 2026 </p>
 * 
 * @author Riya Nalla
 * 
 * @version 1.00	2024-02-09 The mainline of a console terminal implementation of a Name
 * 					Recognizer testbed
 * 
 */

public class NameRecognizerTestbed{
	
	static String inputLine;
	/*******************************************************************************************************/

	/*******************************************************************************************************
	 * This method is the Console user interface
	 * 
	 */
	public static void main(String[] args) {

		System.out.println("Welcome to the Name Recognizer Testbed\n");
        System.out.println("Please enter a Name or an empty line to stop.");

		// Associate the system keyboard with a Scanner object
		Scanner keyboard = new Scanner(System.in);
		// As long as there is a next line, read it in... Since the input is the keyboard, this is always true
		while (keyboard.hasNextLine()) {
			inputLine = keyboard.nextLine();		// Fetch the next line
			if (inputLine.length() == 0) {			// If the length of the trimmed line is zero, stop the loop
				System.out.println("\n*** Empty input line detected, the loop stops.");
				keyboard.close();					// Display the reason for terminating the loop.
				System.exit(0);
			}
			// Input has been provided, let's see if it is a valid date or not
 			String errMessage = NameRecognizer.checkForValidName(inputLine);
 			
 			// If the returned String is not empty, it is an error message
			if (errMessage != "") {
				// Display the error message
				System.out.println(errMessage);
				
				// Fetch the index where the processing of the input stopped
				if (NameRecognizer.nameRecognizerIndexOfError <= -1) return;	// Should never happen
				// Display the input line so the user can see what was entered		
				System.out.println(inputLine);
				// Display the line up to the error and the display an up arrow
				System.out.println(inputLine.substring(0,NameRecognizer.nameRecognizerIndexOfError) + "\u21EB");
			}
			else {
				// The returned String is empty, it, so there is no error in the input.
				System.out.println("Success! The Name is valid.");
			}
			// Request more input or an empty line to stop the application.
	        System.out.println("\nPlease enter Name or an empty line to stop.");
		}
	}
}
