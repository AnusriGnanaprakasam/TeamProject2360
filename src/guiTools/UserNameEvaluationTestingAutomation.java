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
public class UserNameEvaluationTestingAutomation {
	
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
		performTestCase(1, "ab", false);
		
		// This is a properly written positive test
		performTestCase(2, "maryjane", true);
		
		// This is a properly written negative test
		performTestCase(2, "a0u109383091dj0130311d", false);
		
		// This is a properly written positive test
		performTestCase(3, "Macy45", true);
		
		// This is a properly written negative test
		performTestCase(4, "bhq&9", false);
		
		// This is an improperly properly written positive test
		performTestCase(5, "", true);
		
		// This is an improperly written negative test, because the name
		// is valid, but the second parameter asserts that it is not valid
		performTestCase(6, "Adami.Inden", false);
				
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
		String resultText= UserNameRecognizer.checkForValidUserName(inputText);
		
		/************** Interpret the result and display that interpreted information **************/
		System.out.println();
		
		// If the resulting text is empty, the recognizer accepted the input
		if (resultText != "") {
			 // If the test case expected the test to pass then this is a failure
			if (expectedPass) {
				System.out.println("***Failure*** The username <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be valid, so this is a failure!\n");
				System.out.println("Error message: " + resultText);
				numFailed++;
			}
			// If the test case expected the test to fail then this is a success
			else {			
				System.out.println("***Success*** The username <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be invalid, so this is a pass!\n");
				System.out.println("Error message: " + resultText);
				numPassed++;
			}
		}
		
		// If the resulting text is empty, the recognizer accepted the input
		else {	
			// If the test case expected the test to pass then this is a success
			if (expectedPass) {	
				System.out.println("***Success*** The username <" + inputText + 
						"> is valid, so this is a pass!");
				numPassed++;
			}
			// If the test case expected the test to fail then this is a failure
			else {
				System.out.println("***Failure*** The username <" + inputText + 
						"> was judged as valid" + 
						"\nBut it was supposed to be invalid, so this is a failure!");
				numFailed++;
			}
		}
		displayEvaluation(inputText);
	}
	
	private static void displayEvaluation(String username) {
		
		if (!username.isEmpty() && Character.isLetter(username.charAt(0))) {
	        System.out.println("Username starts with a letter - Satisfied");
	    } else {
	        System.out.println("Username starts with a letter - Not Satisfied");
	    }

	    if (username.length() >= 4) {
	        System.out.println("Username has at least 4 characters - Satisfied");
	    } else {
	        System.out.println("Username has at least 4 characters - Not Satisfied");
	    }

	    if (username.length() <= 16) {
	        System.out.println("Username has no more than 16 characters - Satisfied");
	    } else {
	        System.out.println("Username has no more than 16 characters - Not Satisfied");
	    }

	    boolean validChars = true;
	    for (char x : username.toCharArray()) {
	        if (!Character.isLetterOrDigit(x) && x != '.') {
	            validChars = false;
	            break;
	        }
	    }
	    if (validChars) {
	        System.out.println("Username contains only valid characters - Satisfied");
	    } else {
	        System.out.println("Username contains only valid characters - Not Satisfied");
	    }

	    if (!username.endsWith(".")) {
	        System.out.println("Username does not end with a period - Satisfied");
	    } else {
	        System.out.println("Username does not end with a period - Not Satisfied");
	    }

	    boolean validPeriodUsage = true;
	    for (int i = 0; i < username.length() - 1; i++) {
	        if (username.charAt(i) == '.') {
	            char next = username.charAt(i + 1);
	            if (!Character.isLetterOrDigit(next)) {
	                validPeriodUsage = false;
	                break;
	            }
	        }
	    }
	    if (validPeriodUsage) {
	        System.out.println("Username period usage is valid - Satisfied");
	    } else {
	        System.out.println("Username period usage is valid - Not Satisfied");
	    }
	}
}
	