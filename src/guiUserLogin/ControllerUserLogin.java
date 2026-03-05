package guiUserLogin;


import java.sql.SQLException;

import database.Database;
import entityClasses.User;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.VBox;
import javafx.scene.control.PasswordField;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*******
 * <p> Title: ControllerUserLogin Class. </p>
 * 
 * <p> Description: The Java/FX-based User Login Page.  This class provides the controller
 * actions basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This controller determines if the log in is valid.  If so set up the link to the database, 
 * determines how many roles this user is authorized to play, and the calls one the of the array of
 * role home pages if there is only one role.  If there are more than one role, it setup up and
 * calls the multiple roles dispatch page for the user to determine which role the user wants to
 * play.
 * 
 * The class has been written assuming that the View or the Model are the only class methods that
 * can invoke these methods.  This is why each has been declared at "protected".  Do not change any
 * of these methods to public.</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 * @version 1.01		2025-09-16 Update Javadoc documentation *  
 */

public class ControllerUserLogin {
	
	/*-********************************************************************************************

	The User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/

	/**
	 * Default constructor is not used.
	 */
	public ControllerUserLogin() {
	}

	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.HW2.database;

	private static Stage theStage;	
	
	/**********
	 * <p> Method: public doLogin() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Login button. This
	 * method checks the username and password to see if they are valid. (Riya) Particularly, I added a string builder to 
	 * print both username and password validation errors. When they do not follow particular requirements, such as length and 
	 * type/amount of certain characters, it uses other classes' (UserNameRecognizer and PasswordEvaluationGUITestbed) methods to 
	 * verify them.
	 * 
	 * If so, it then logs that
	 * user in my determining which role to use.
	 * 
	 * The method reaches batch to the view page and to fetch the information needed rather than
	 * passing that information as parameters.
	 * 
	 */	
	protected static void doLogin(Stage ts) {
		theStage = ts;
		String username = ViewUserLogin.text_Username.getText();
		String password = ViewUserLogin.text_Password.getText();
    	boolean loginResult = false;
    	StringBuilder errorMessage = new StringBuilder();
    	
    	String usernameError = guiTools.UserNameRecognizer.checkForValidUserName(username);
    	
    	if (!usernameError.isEmpty()) {
    		errorMessage.append(usernameError).append("\nThere is an error at position ").
    		append(guiTools.UserNameRecognizer.userNameRecognizerIndexofError +  1).append(" in your username.\n");
    	}
    	
    	String passwordError = passwordPopUpWindow.Model.evaluatePassword(password);
    	if (!passwordError.isEmpty()) {
    		errorMessage.append(passwordError).append("\n\n\n");
    	}
    	
    	if (errorMessage.length() > 0) {
    	    ViewUserLogin.alertUsernamePasswordError.setContentText(errorMessage.toString());
    	    ViewUserLogin.alertUsernamePasswordError.showAndWait();
    	    return;
    	}
    	
		// Fetch the user and verify the username
     	if (theDatabase.getUserAccountDetails(username) == false) {
     		// Don't provide too much information.  Don't say the username is invalid or the
     		// password is invalid.  Just say the pair is invalid.
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
		// System.out.println("*** Username is valid");
		
		// Check to see that the login password matches the account password
    	String actualPassword = theDatabase.getCurrentPassword();
    	
    	if (password.compareTo(actualPassword) != 0) {
    		ViewUserLogin.alertUsernamePasswordError.setContentText(
    				"Incorrect username/password. Try again!");
    		ViewUserLogin.alertUsernamePasswordError.showAndWait();
    		return;
    	}
		// System.out.println("*** Password is valid for this user");
    	
    	if (actualPassword.startsWith("OTP-")) {
    	    // Force user to create a new password
    	    promptUserToSetNewPassword(username);
    	    return; // stop the normal login flow
    	}
		
		// Establish this user's details
    	User user = new User(username, password, theDatabase.getCurrentFirstName(), 
    			theDatabase.getCurrentMiddleName(), theDatabase.getCurrentLastName(), 
    			theDatabase.getCurrentPreferredFirstName(), theDatabase.getCurrentEmailAddress(), 
    			theDatabase.getCurrentAdminRole(), 
    			theDatabase.getCurrentNewRole1(), theDatabase.getCurrentNewRole2());
    	
    	// See which home page dispatch to use
		int numberOfRoles = theDatabase.getNumberOfRoles(user);		
		// System.out.println("*** The number of roles: "+ numberOfRoles);
		if (numberOfRoles == 1) {
			// Single Account Home Page - The user has no choice here
			
			// Admin role
			if (user.getAdminRole()) {
				loginResult = theDatabase.loginAdmin(user);
				if (loginResult) {
					guiAdminHome.ViewAdminHome.displayAdminHome(theStage, user);
				}
			} else if (user.getNewRole1()) {
				loginResult = theDatabase.loginRole1(user);
				if (loginResult) {
					guiRole1.ViewRole1Home.displayRole1Home(theStage, user);
				}
			} else if (user.getNewRole2()) {
				loginResult = theDatabase.loginRole2(user);
				if (loginResult) {
					guiRole2.ViewRole2Home.displayRole2Home(theStage, user);
				}
				// Other roles
			} else {
				System.out.println("***** UserLogin goToUserHome request has an invalid role");
			}
		} else if (numberOfRoles > 1) {
			// Multiple Account Home Page - The user chooses which role to play
			// System.out.println("*** Going to displayMultipleRoleDispatch");
			guiMultipleRoleDispatch.ViewMultipleRoleDispatch.
				displayMultipleRoleDispatch(theStage, user);
		}
	}
	
	/**********
	 * <p> ADDED HELPER Method: promptUserToSetNewPassword(String username) </p>
	 * 
	 * <p> Description: This method is called to reset the password for a user if they forget.</p>
	 * 
	 */
	private static void promptUserToSetNewPassword(String username) {
	    Stage dialog = new Stage();
	    dialog.initModality(Modality.APPLICATION_MODAL);
	    dialog.setTitle("Set New Password");

	    Label prompt1 = new Label("Enter new password:");
	    PasswordField pw1 = new PasswordField();

	    Label prompt2 = new Label("Confirm new password:");
	    PasswordField pw2 = new PasswordField();

	    Button confirm = new Button("Set Password");

	    confirm.setOnAction(e -> {
	        String newPassword1 = pw1.getText();
	        String newPassword2 = pw2.getText();
	        StringBuilder errorMessage = new StringBuilder();

	        // Validate password
	        String passwordError = passwordPopUpWindow.Model.evaluatePassword(newPassword1);
	        if (!passwordError.isEmpty()) {
	            errorMessage.append(passwordError).append("\n\n\n");
	        }

	        // Check match
	        if (!newPassword1.equals(newPassword2)) {
	            errorMessage.append("Passwords do not match.\n");
	        }

	        if (errorMessage.length() > 0) {
	            Alert alert = new Alert(Alert.AlertType.ERROR);
	            alert.setTitle("Password Error");
	            alert.setHeaderText("Fix the following issues:");
	            alert.setContentText(errorMessage.toString());
	            alert.showAndWait();
	        } else {
	            // Update the database
	            try {
	                Database db = new Database();
	                db.connectToDatabase();
	                db.updatePassword(username, newPassword1); // overwrite OTP
	            } catch (SQLException ex) {
	                ex.printStackTrace();
	            }

	            Alert success = new Alert(Alert.AlertType.INFORMATION);
	            success.setTitle("Success");
	            success.setHeaderText("Password Updated");
	            success.setContentText("Your password has been updated. Please log in again.");
	            success.showAndWait();

	            dialog.close();
	        }
	    });

	    VBox layout = new VBox(10, prompt1, pw1, prompt2, pw2, confirm);
	    layout.setAlignment(Pos.CENTER);
	    layout.setMinWidth(300);

	    Scene scene = new Scene(layout);
	    dialog.setScene(scene);
	    dialog.showAndWait();
	}
	
		
	/**********
	 * <p> Method: setup() </p>
	 * 
	 * <p> Description: This method is called to reset the page and then populate it with new
	 * content for the new user.</p>
	 * 
	 */
	protected static void doSetupAccount(Stage theStage, String invitationCode) {
		guiNewAccount.ViewNewAccount.displayNewAccount(theStage, invitationCode);
	}

	
	/**********
	 * <p> Method: public performQuit() </p>
	 * 
	 * <p> Description: This method is called when the user has clicked on the Quit button.  Doing
	 * this terminates the execution of the application.  All important data must be stored in the
	 * database, so there is no cleanup required.  (This is important so we can minimize the impact
	 * of crashed.)
	 * 
	 */	
	protected static void performQuit() {
		System.out.println("Perform Quit");
		System.exit(0);
	}	

}
