package guiTools;

/*******
 * <p> Title: NameEvaluationTestingAutomation Class. </p>
 * 
 * <p> Description: A Java demonstration for semi-automated tests </p>
 * 
 * <p> Copyright: Riya Nalla © 2026 </p>
 * 
 * @author Riya Nalla
 * 
 * @version 1.00	2026-02-09
 * 
 */
public class NameEvaluationTestingAutomation {
	
	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	/*
	 * This mainline displays a header to the console, performs a sequence of
	 * test cases, and then displays a footer with a summary of the results
	 */
	public static void main(String[] args) {
		/************** Test cases semi-automation report header **************/
		System.out.println("______________________________________");
		System.out.println("\nTesting Automation");

		/************** Start of the test cases **************/
		
		// This is a properly written negative test
		performTestCase(1, "R", false);
		
		// This is a properly written negative test
		performTestCase(2, "A!", false);
		
		// This is a properly written positive test
		performTestCase(3, "Macy", true);
		
		// This is a properly written negative test
		performTestCase(4, "G8ilina", false);
		
		// This is an improperly properly written positive test
		performTestCase(5, "", true);
		
		// This is an improperly written negative test, because the name
		// is valid, but the second parameter asserts that it is not valid
		performTestCase(6, "Adami-Inden", false);
				
		// These are improperly written positive test, because the name
		// is not valid, but the second parameter asserts that it is valid
		performTestCase(7, "F9oq@", true);
		
		/************** End of the test cases **************/
		
		/************** Test cases semi-automation report footer **************/
		System.out.println("____________________________________________________________________________");
		System.out.println();
		System.out.println("Number of tests passed: "+ numPassed);
		System.out.println("Number of tests failed: "+ numFailed);
	}
	
	/*
	 * This method sets up the input value for the test from the input parameters,
	 * displays test execution information, invokes precisely the same recognizer
	 * that the interactive JavaFX mainline uses, interprets the returned value,
	 * and displays the interpreted result.
	 */
	private static void performTestCase(int testCase, String inputText, boolean expectedPass) {
				
		/************** Display an individual test case header **************/
		System.out.println("____________________________________________________________________________\n\nTest case: " + testCase);
		System.out.println("Input: \"" + inputText + "\"");
		System.out.println("______________");
		System.out.println("\nFinite state machine execution trace:");
		
		/************** Call the recognizer to process the input **************/
		String resultText= NameRecognizer.checkForValidName(inputText);
		
		/************** Interpret the result and display that interpreted information **************/
		System.out.println();
		
		// If the resulting text is empty, the recognizer accepted the input
		if (resultText != "") {
			 // If the test case expected the test to pass then this is a failure
			if (expectedPass) {
				System.out.println("***Failure*** The name <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be valid, so this is a failure!\n");
				System.out.println("Error message: " + resultText);
				numFailed++;
			}
			// If the test case expected the test to fail then this is a success
			else {			
				System.out.println("***Success*** The name <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be invalid, so this is a pass!\n");
				System.out.println("Error message: " + resultText);
				numPassed++;
			}
		}
		
		// If the resulting text is empty, the recognizer accepted the input
		else {	
			// If the test case expected the test to pass then this is a success
			if (expectedPass) {	
				System.out.println("***Success*** The name <" + inputText + 
						"> is valid, so this is a pass!");
				numPassed++;
			}
			// If the test case expected the test to fail then this is a failure
			else {
				System.out.println("***Failure*** The name <" + inputText + 
						"> was judged as valid" + 
						"\nBut it was supposed to be invalid, so this is a failure!");
				numFailed++;
			}
		}
		displayEvaluation(inputText);
	}
	
	private static void displayEvaluation(String name) {
		
		if (!name.isEmpty() && Character.isLetter(name.charAt(0))) {
            System.out.println("Name starts with a letter - Satisfied");
        } else {
            System.out.println("Name starts with a letter - Not Satisfied");
        }

        if (name.length() >= 2) {
            System.out.println("Name has at least 2 characters - Satisfied");
        } else {
            System.out.println("Name has at least 2 characters - Not Satisfied");
        }

        boolean allValid = true;
        for (char x : name.toCharArray()) {
            if (!Character.isLetter(x) && x != '-') {
                allValid = false;
                break;
            }
        }
        if (allValid) {
            System.out.println("Name contains only valid characters - Satisfied");
        } else {
            System.out.println("Name contains only valid characters - Not Satisfied");
        }
	}
}
	