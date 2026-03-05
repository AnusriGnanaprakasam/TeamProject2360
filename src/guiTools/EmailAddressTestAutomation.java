package guiTools;

/*******
 * <p> Title: EmailAddressTestAutomation Class. </p>
 * 
 * <p> Description: A Java demonstration for semi-automated tests </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2022 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00	2022-02-11 A set of semi-automated test cases
 * 
 */
public class EmailAddressTestAutomation {
	
	static int numPassed = 0;	// Counter of the number of passed tests
	static int numFailed = 0;	// Counter of the number of failed tests

	/*
	 * This mainline displays a header to the console, performs a sequence of
	 * test cases, and then displays a footer with a summary of the results
	 */
	public static void main(String[] args) {
		/************** Test cases semi-automation report header **************/
		System.out.println("______________________________________");
		System.out.println("\nTest Case Automation");

		/************** Start of the test cases **************/
		
		// This is a properly written positive test
		performTestCase(1, "emailUser@organization.com", true);
		
		// This is a properly written negative test
		performTestCase(2, "emailUser", false);
		
		// This is an improperly written negative test, because the email address
		// is valid, but the second parameter asserts that it is not valid
		performTestCase(3, "emailUser@organization.com", false);
		
		// This is an improperly written positive test, because the email address
		// is not valid, but the second parameter asserts that it is valid
		performTestCase(4, "emailUser", true);
		
		// This is an properly written negative test, because the email address
		// is not valid in that the domain part is 70 characters long, and
		// the second parameter asserts that it is invalid
		performTestCase(5, "lrc@1234567890123456789012345678901234567890123456789012345678901234567890.com", false);
		
		performTestCase(6, "ag@gmail.com-", false);
		// has dash at the end
		
		performTestCase(7, "#g@gmail.com-", false);
		// the email starts with a special char
		
		performTestCase(8, "adg@#mail.com", false);
		// the email starts with a special char
		
		performTestCase(9, "adg@gmail.com", true);
		// the email starts with a special char
		
		/************** End of the test cases **************/
		
		/************** Test cases semi-automation report footer **************/
		System.out.println("______________________________________");
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
	public static void performTestCase(int testCase, String inputText, boolean expectedPass) {
		/************** Display an individual test case header **************/
		System.out.println("______________________________________\n\nTest case: " + testCase);
		System.out.println("Input: \"" + inputText + "\"");
		System.out.println("_______");
		System.out.println("\nFinite state machine execution trace:");
		
		/************** Call the recognizer to process the input **************/
		String resultText= EmailAddressRecognizer.checkEmailAddress(inputText);
		
		/************** Interpret the result and display that interpreted information **************/
		System.out.println();
		
		// If the resulting text is empty, the recognizer accepted the input
		if (resultText != "") {
			 // If the test case expected the test to pass then this is a failure
			if (expectedPass) {
				System.out.println("***Failure*** The email address <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be valid, so this is a failure!\n");
				System.out.println("Error message: " + resultText);
				numFailed++;
			}
			// If the test case expected the test to fail then this is a success
			else {			
				System.out.println("***Success*** The email address <" + inputText + "> is invalid." + 
						"\nBut it was supposed to be invalid, so this is a pass!\n");
				System.out.println("Error message: " + resultText);
				numPassed++;
			}
		}
		
		// If the resulting text is empty, the recognizer accepted the input
		else {	
			// If the test case expected the test to pass then this is a success
			if (expectedPass) {	
				System.out.println("***Success*** The email address <" + inputText + 
						"> is valid, so this is a pass!");
				numPassed++;
			}
			// If the test case expected the test to fail then this is a failure
			else {
				System.out.println("***Failure*** The email address <" + inputText + 
						"> was judged as valid" + 
						"\nBut it was supposed to be invalid, so this is a failure!");
				numFailed++;
			}
		}
	}
}
