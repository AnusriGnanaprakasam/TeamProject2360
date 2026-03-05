package guiAdminHome;

import database.Database;
import entityClasses.User;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the controller actions
 * basic on the user's use of the JavaFX GUI widgets defined by the View class.
 * 
 * This page contains a number of buttons that have not yet been implemented.  WHen those buttons
 * are pressed, an alert pops up to tell the user that the function associated with the button has
 * not been implemented. Also, be aware that What has been implemented may not work the way the
 * final product requires and there maybe defects in this code.
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

public class ControllerAdminHome {
	
	/*-*******************************************************************************************

	User Interface Actions for this page
	
	This controller is not a class that gets instantiated.  Rather, it is a collection of protected
	static methods that can be called by the View (which is a singleton instantiated object) and 
	the Model is often just a stub, or will be a singleton instantiated object.
	
	*/
	
	/**
	 * Default constructor is not used.
	 */
	public ControllerAdminHome() {
	}
	
	// Reference for the in-memory database so this package has access
	private static Database theDatabase = applicationMain.HW2.database;

	/**********
	 * <p> 
	 * 
	 * Title: performInvitation () Method. </p>
	 * 
	 * <p> Description: Protected method to send an email inviting a potential user to establish
	 * an account and a specific role. </p>
	 */
	protected static void performInvitation () {
		// Verify that the email address is valid - If not alert the user and return
		String emailAddress = ViewAdminHome.text_InvitationEmailAddress.getText();
		if (invalidEmailAddress(emailAddress)) {
			return;
		}
		
		// Check to ensure that we are not sending a second message with a new invitation code to
		// the same email address.  
		if (theDatabase.emailaddressHasBeenUsed(emailAddress)) {
			ViewAdminHome.alertEmailError.setContentText(
					"An invitation has already been sent to this email address.");
			ViewAdminHome.alertEmailError.showAndWait();
			return;
		}
		
		// Inform the user that the invitation has been sent and display the invitation code
		String theSelectedRole = (String) ViewAdminHome.combobox_SelectRole.getValue();
		String invitationCode = theDatabase.generateInvitationCode(emailAddress,
				theSelectedRole);
		String msg = "Code: " + invitationCode + " for role " + theSelectedRole + 
				" was sent to: " + emailAddress;
		System.out.println(msg);
		ViewAdminHome.alertEmailSent.setContentText(msg);
		ViewAdminHome.alertEmailSent.showAndWait();
		
		// Update the Admin Home pages status
		ViewAdminHome.text_InvitationEmailAddress.setText("");
		ViewAdminHome.label_NumberOfInvitations.setText("Number of outstanding invitations: " + 
				theDatabase.getNumberOfInvitations());
	}
	
	/********** 897987688-66
	 * <p>
	 *
	 * Title: openForum () Method. </p>
	 *
	 * <p> Description: Protected method that navigates to the Student Discussion Forum page.
	 * The currently signed-in admin user is passed through so their username is used for
	 * all posts and replies they create in the forum.</p>
	 */
	public static void openForum() {
		guiForum.ViewForum.displayForum(ViewAdminHome.theStage, ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: void manageInvitations (String email) Method. </p>
	 * 
	 * <p> Description: This protected method will delete an invitation code based on the email
	 * it was sent to
	 *  @param email which specifies the invite you want to delete based on what email it
	 *  was sent to</p>
	 * 
	 */
	protected static void manageInvitations (String email) {
		// This defines the alert's headers, which confirms deletion after you enter an email
		ViewAdminHome.confirm_deleteInvite.setTitle("Confirm Deletion");
		ViewAdminHome.confirm_deleteInvite.setHeaderText("Delete Invitation"); 
		ViewAdminHome.confirm_deleteInvite.setContentText(
		        "Are you sure you want to delete the invitation for:\n\n" + email);

		Optional<ButtonType> result =
		        ViewAdminHome.confirm_deleteInvite.showAndWait(); 

		if (result.isPresent() && result.get() == ButtonType.OK) { //If you press ok on the alert, it will delete the invite
		    theDatabase.deleteInvite(email); // It will call the deleteInvite method in the Database.java file
		    ViewAdminHome.label_NumberOfInvitations.setText(
		            "Number of outstanding invitations: " +
		            theDatabase.getNumberOfInvitations());
		}

	
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: setOnetimePassword () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void setOnetimePassword () {
		Stage dialog = new Stage();
	    dialog.initModality(Modality.APPLICATION_MODAL);
	    dialog.setTitle("Set One-Time Password");

	    Label prompt = new Label("Enter username to set One-time Password:");
	    TextField input = new TextField();
	    Button confirm = new Button("Generate One-Time Password");

	    confirm.setOnAction(e -> {
	        String username = input.getText().trim();
	        if (!username.isEmpty()) {
	            try {
	            	theDatabase.connectToDatabase();

	                if (theDatabase.doesUserExist(username)) {
	                    // Generate a random password
	                    String password = generateRandomPassword(10);

	                    // Update user's password
	                    theDatabase.updatePassword(username, password);

	                    // Show success
	                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
	                    alert.setTitle("OTP Generated");
	                    alert.setHeaderText("Success");
	                    alert.setContentText("One-time password for " + username + " is:\n" + password);
	                    alert.showAndWait();
	                } else {
	                    // User not found
	                    Alert alert = new Alert(Alert.AlertType.ERROR);
	                    alert.setTitle("Error");
	                    alert.setHeaderText("Failed");
	                    alert.setContentText("User \"" + username + "\" not found.");
	                    alert.showAndWait();
	                }

	            } catch (SQLException ex) {
	                ex.printStackTrace();
	                Alert alert = new Alert(Alert.AlertType.ERROR);
	                alert.setTitle("Database Error");
	                alert.setHeaderText("Failed");
	                alert.setContentText("An error occurred while accessing the database.");
	                alert.showAndWait();
	            }
	            dialog.close();
	        }
	    });

	    VBox layout = new VBox(10, prompt, input, confirm);
	    layout.setAlignment(Pos.CENTER);
	    layout.setMinWidth(300);
	    layout.setMinHeight(150);

	    Scene scene = new Scene(layout);
	    dialog.setScene(scene);
	    dialog.showAndWait();
		
//		System.out.println("\n*** WARNING ***: One-Time Password Not Yet Implemented");
//		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
//		ViewAdminHome.alertNotImplemented.setHeaderText("One-Time Password Issue");
//		ViewAdminHome.alertNotImplemented.setContentText("One-Time Password Not Yet Implemented");
//		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: void deleteUser (String username) Method. </p>
	 * <p> Description: This protected method is called by the delete button on the admin homepage.
	 * This function will ask for confirmation before deleting a user.
	 *  @param username which specifies the user to delete
	 *   </p>
	 */
	protected static void deleteUser(String username) {
		
		ViewAdminHome.confirm.setTitle("Confirm Deletion");
		ViewAdminHome.confirm.setHeaderText("Delete User");
	    ViewAdminHome.confirm.setContentText("Are you sure you want to delete the user:\n\n" + username);
	    Optional<ButtonType> result = ViewAdminHome.confirm.showAndWait();

	    if (result.isPresent() && result.get() == ButtonType.OK) {
	        theDatabase.deleteUser(username);
	    }
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: listUsers () Method. </p>
	 * 
	 * <p> Description: Protected method that is currently a stub informing the user that
	 * this function has not yet been implemented. </p>
	 */
	protected static void listUsers() {
		List<User> users = applicationMain.HW2.database.getAllUsers();

	    if (users == null || users.isEmpty()) {
	        Alert alert = new Alert(AlertType.INFORMATION);
	        alert.setTitle("List of Users");
	        alert.setHeaderText("No Users Found");
	        alert.setContentText("There are currently no users in the system.");
	        alert.showAndWait();
	        return;
	    }

	    StringBuilder userInfo = new StringBuilder();

	    for (User x : users) {
	        userInfo.append("Username: ").append(x.getUserName()).append("\n").append("Name: ").append(x.getFirstName() + " " + x.getLastName()).append("\n")
	        .append("Email Address: ").append(x.getEmailAddress()).append("\n").append("Admin: ").append(x.getAdminRole()).append("\n")
	        .append("Role1: ").append(x.getNewRole1()).append("\n").append("Role2: ").append(x.getNewRole2()).append("\n\n");
	    }

	    Alert alert = new Alert(AlertType.INFORMATION);
	    alert.setTitle("List of Users");
	    alert.setHeaderText("Registered Users");
	    alert.setContentText(userInfo.toString());
	    alert.getDialogPane().setPrefWidth(450); 
	    alert.showAndWait();
	    
//		System.out.println("\n*** WARNING ***: List Users Not Yet Implemented");
//		ViewAdminHome.alertNotImplemented.setTitle("*** WARNING ***");
//		ViewAdminHome.alertNotImplemented.setHeaderText("List User Issue");
//		ViewAdminHome.alertNotImplemented.setContentText("List Users Not Yet Implemented");
//		ViewAdminHome.alertNotImplemented.showAndWait();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: addRemoveRoles () Method. </p>
	 * 
	 * <p> Description: Protected method that allows an admin to add and remove roles for any of
	 * the users currently in the system.  This is done by invoking the AddRemoveRoles Page. There
	 * is no need to specify the home page for the return as this can only be initiated by and
	 * Admin.</p>
	 */
	protected static void addRemoveRoles() {
		guiAddRemoveRoles.ViewAddRemoveRoles.displayAddRemoveRoles(ViewAdminHome.theStage, 
				ViewAdminHome.theUser);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: invalidEmailAddress () Method. </p>
	 * 
	 * <p> Description: Protected method that is intended to check an email address before it is
	 * used to reduce errors.  The code currently only checks to see that the email address is not
	 * empty.  In the future, a syntactic check must be performed and maybe there is a way to check
	 * if a properly email address is active.</p>
	 * 
	 * @param emailAddress	This String holds what is expected to be an email address
	 */
	protected static boolean invalidEmailAddress(String emailAddress) {
		if (emailAddress.length() == 0) {
			ViewAdminHome.alertEmailError.setContentText(
					"Correct the email address and try again.");
			ViewAdminHome.alertEmailError.showAndWait();
			return true;
		}
		return false;
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: generateRandomPassword () ADDED HELPER Method. </p>
	 * 
	 * <p> Description: Private method that generates a one-time password for a user based on strings and random function.</p>
	 * 
	 * @param length this holds the length needed for generating a password
	 */
	private static String generateRandomPassword(int length) {
		String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	    String lower = "abcdefghijklmnopqrstuvwxyz";
	    String digits = "0123456789";
	    String special = "!@#$%^&*()_+-=[]{}|;:',.<>?";
	    String all = upper + lower + digits + special;

	    StringBuilder password = new StringBuilder();

	    // Ensure at least one of each required type
	    password.append(upper.charAt((int) (Math.random() * upper.length())));
	    password.append(lower.charAt((int) (Math.random() * lower.length())));
	    password.append(digits.charAt((int) (Math.random() * digits.length())));
	    password.append(special.charAt((int) (Math.random() * special.length())));

	    // Fill the rest
	    while (password.length() < length) {
	    	password.append(all.charAt((int) (Math.random() * all.length())));
	    }

	    // Shuffle the password
	    String prefix = "OTP-";
	    List<Character> charsList = new ArrayList<>();
	    for (char c : password.toString().toCharArray()) charsList.add(c);
	    java.util.Collections.shuffle(charsList);

	    StringBuilder finalP = new StringBuilder(prefix);
	    for (char c : charsList) finalP.append(c);

	    return finalP.toString();
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performLogout () Method. </p>
	 * 
	 * <p> Description: Protected method that logs this user out of the system and returns to the
	 * login page for future use.</p>
	 */
	protected static void performLogout() {
		guiUserLogin.ViewUserLogin.displayUserLogin(ViewAdminHome.theStage);
	}
	
	/**********
	 * <p> 
	 * 
	 * Title: performQuit () Method. </p>
	 * 
	 * <p> Description: Protected method that gracefully terminates the execution of the program.
	 * </p>
	 */
	protected static void performQuit() {
		System.exit(0);
	}
}
