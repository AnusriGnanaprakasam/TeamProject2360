package guiAdminHome;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;
import guiAddRemoveRoles.ControllerAddRemoveRoles;
import guiUserUpdate.ViewUserUpdate;

/*******
 * <p> Title: GUIAdminHomePage Class. </p>
 * 
 * <p> Description: The Java/FX-based Admin Home Page.  This class provides the JavaFX GUI widgets
 * that enable an admin to perform admin functions.  This page contains a number of buttons that
 * have not yet been implemented.  What has been implemented may not work the way the final product
 * requires and there maybe defects in this code.
 * 
 * The class has been written using a singleton design pattern and is the View portion of the 
 * Model, View, Controller pattern.  The pattern is designed that the all accesses to this page and
 * its functions starts by invoking the static method displayAdminHome.  No other method should 
 * attempt to instantiate this class as that is controlled by displayAdminHome.  It ensure that
 * only one instance of class is instantiated and that one is properly configured for each use.  
 * 
 * Please note that this implementation is not appropriate for concurrent systems with multiple
 * users. This Baeldung article provides insight into the issues: 
 *           https://www.baeldung.com/java-singleton</p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 1.00		2025-08-17 Initial version
 *  
 */

public class ViewAdminHome {

	/*-*******************************************************************************************
	Attributes
	*/

	private static double width = applicationMain.HW2.WINDOW_WIDTH;
	private static double height = applicationMain.HW2.WINDOW_HEIGHT;

	// GUI Area 1
	protected static Label label_PageTitle = new Label();
	protected static Label label_UserDetails = new Label();
	protected static Button button_UpdateThisUser = new Button("Account Update");
	private static Line line_Separator1 = new Line(20, 95, width-20, 95);

	// GUI Area 2
	protected static Label label_NumberOfInvitations = new Label("Number of Outstanding Invitations: x");
	protected static Label label_NumberOfUsers = new Label("Number of Users: x");
	private static Line line_Separator2 = new Line(20, 165, width-20, 165);
	
	protected static Button button_Forum = new Button("forum");
	

	// GUI Area 3
	protected static Label label_Invitations = new Label("Send An Invitation");
	protected static Label label_InvitationEmailAddress = new Label("Email Address");
	protected static TextField text_InvitationEmailAddress = new TextField();
	protected static ComboBox<String> combobox_SelectRole = new ComboBox<>();
	protected static String[] roles = {"Admin", "Role1", "Role2"};
	protected static Button button_SendInvitation = new Button("Send Invitation");
	protected static Alert alertEmailError = new Alert(AlertType.INFORMATION);
	protected static Alert alertEmailSent = new Alert(AlertType.INFORMATION);

	// GUI Area 4
	// This is the button for the Manage Invitations that is displayed in the admin homepage
	protected static Button button_ManageInvitations = new Button("Manage Invitations"); 
	// This defines the alert that you can after typing in a email to delete an invite
	protected static  Alert confirm_deleteInvite = new Alert(Alert.AlertType.CONFIRMATION);
	
	
	protected static Button button_SetOnetimePassword = new Button("Set a One-Time Password");

	// Delete user button
	protected static Button button_DeleteUser = new Button("Delete a User");
	protected static  Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
	

	protected static Button button_ListUsers = new Button("List All Users");
	protected static Button button_AddRemoveRoles = new Button("Add/Remove Roles");
	protected static Alert alertNotImplemented = new Alert(AlertType.INFORMATION);

	private static Line line_Separator4 = new Line(20, 525, width-20,525);

	// GUI Area 5
	protected static Button button_Logout = new Button("Logout");
	protected static Button button_Quit = new Button("Quit");

	// Singleton & database references
	private static ViewAdminHome theView;		
	private static Database theDatabase = applicationMain.HW2.database;
	public static Stage theStage;
	private static Pane theRootPane;			
	public static User theUser;				
	private static Scene theAdminHomeScene;		
	private static final int theRole = 1;		

	/*-*******************************************************************************************
	Constructors
	*/

	public static void displayAdminHome(Stage ps, User user) {
		theStage = ps;
		theUser = user;

		if (theView == null) theView = new ViewAdminHome();	// Instantiate singleton if needed

		theDatabase.getUserAccountDetails(user.getUserName());		
		applicationMain.HW2.activeHomePage = theRole;		

		combobox_SelectRole.getSelectionModel().select(0);

		theStage.setTitle("CSE 360 Foundation Code: Admin Home Page");
		theStage.setScene(theAdminHomeScene);						
		theStage.show();											
	}

	private ViewAdminHome() {
		theRootPane = new Pane();
		theAdminHomeScene = new Scene(theRootPane, width, height);

		// GUI Area 1
		label_PageTitle.setText("Admin Home Page");
		setupLabelUI(label_PageTitle, "Arial", 28, width, Pos.CENTER, 0, 5);

		label_UserDetails.setText("User: " + theUser.getUserName());
		setupLabelUI(label_UserDetails, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 55);

		setupButtonUI(button_UpdateThisUser, "Dialog", 18, 170, Pos.CENTER, 610, 45);
		button_UpdateThisUser.setOnAction((_) -> { ViewUserUpdate.displayUserUpdate(theStage, theUser); });

		setupButtonUI(button_Forum, "Dialog", 16, 250, Pos.CENTER, 20, 540);
		button_Forum.setText("Forum");
		button_Forum.setOnAction((_) -> { ControllerAdminHome.openForum(); });
		
		
		// GUI Area 2
		setupLabelUI(label_NumberOfInvitations, "Arial", 20, 200, Pos.BASELINE_LEFT, 20, 105);
		label_NumberOfInvitations.setText("Number of outstanding invitations: " + theDatabase.getNumberOfInvitations());

		setupLabelUI(label_NumberOfUsers, "Arial", 20, 200, Pos.BASELINE_LEFT, 20, 135);
		label_NumberOfUsers.setText("Number of users: " + theDatabase.getNumberOfUsers());

		// GUI Area 3
		setupLabelUI(label_Invitations, "Arial", 20, width, Pos.BASELINE_LEFT, 20, 175);
		setupLabelUI(label_InvitationEmailAddress, "Arial", 16, width, Pos.BASELINE_LEFT, 20, 210);
		setupTextUI(text_InvitationEmailAddress, "Arial", 16, 360, Pos.BASELINE_LEFT, 130, 205, true);
		setupComboBoxUI(combobox_SelectRole, "Dialog", 16, 90, 500, 205);

		List<String> list = new ArrayList<>();
		for (String r : roles) list.add(r);
		combobox_SelectRole.setItems(FXCollections.observableArrayList(list));
		combobox_SelectRole.getSelectionModel().select(0);

		setupButtonUI(button_SendInvitation, "Dialog", 16, 150, Pos.CENTER, 630, 205);
		button_SendInvitation.setOnAction((_) -> { ControllerAdminHome.performInvitation(); });

		// GUI Area 4
		setupButtonUI(button_ManageInvitations, "Dialog", 16, 250, Pos.CENTER, 20, 270);
		// When the manage invitations button is pressed, it opens a form where you can type an email address
		button_ManageInvitations.setOnAction((_) -> {openManageInvitationsForm(); }); 
		
		

		setupButtonUI(button_SetOnetimePassword, "Dialog", 16, 250, Pos.CENTER, 20, 320);
		button_SetOnetimePassword.setOnAction((_) -> { ControllerAdminHome.setOnetimePassword(); });

		// Delete user button opens a small input form
		setupButtonUI(button_DeleteUser, "Dialog", 16, 250, Pos.CENTER, 20, 370);
		button_DeleteUser.setOnAction((_) -> openDeleteUserForm()); // sends user to form where they can input a user to delete
		theRootPane.getChildren().add(button_DeleteUser);

		setupButtonUI(button_ListUsers, "Dialog", 16, 250, Pos.CENTER, 20, 420);
		button_ListUsers.setOnAction((_) -> { ControllerAdminHome.listUsers(); });

		setupButtonUI(button_AddRemoveRoles, "Dialog", 16, 250, Pos.CENTER, 20, 470);
		button_AddRemoveRoles.setOnAction((_) -> { ControllerAdminHome.addRemoveRoles(); });

		// GUI Area 5
		setupButtonUI(button_Logout, "Dialog", 18, 250, Pos.CENTER, 20, 590);
		button_Logout.setOnAction((_) -> { ControllerAdminHome.performLogout(); });

		setupButtonUI(button_Quit, "Dialog", 18, 250, Pos.CENTER, 300, 590);
		button_Quit.setOnAction((_) -> { ControllerAdminHome.performQuit(); });

		// Add main widgets
		theRootPane.getChildren().addAll(
			    label_PageTitle, label_UserDetails, button_UpdateThisUser, line_Separator1,
			    label_NumberOfInvitations, label_NumberOfUsers, line_Separator2,
			    label_Invitations, label_InvitationEmailAddress, text_InvitationEmailAddress, combobox_SelectRole,
			    button_SendInvitation, line_Separator4,
			    button_ManageInvitations, button_SetOnetimePassword,
			    button_ListUsers, button_AddRemoveRoles,button_Forum,
			    button_Logout, button_Quit
			);
	}

	/*-*******************************************************************************************
	Helper methods
	*/
	
	private void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y){
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);		
	}

	private void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y){
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);		
	}

	private void setupTextUI(TextField t, String ff, double f, double w, Pos p, double x, double y, boolean e){
		t.setFont(Font.font(ff, f));
		t.setMinWidth(w);
		t.setMaxWidth(w);
		t.setAlignment(p);
		t.setLayoutX(x);
		t.setLayoutY(y);		
		t.setEditable(e);
	}

	private void setupComboBoxUI(ComboBox<String> c, String ff, double f, double w, double x, double y){
		c.setStyle("-fx-font: " + f + " " + ff + ";");
		c.setMinWidth(w);
		c.setLayoutX(x);
		c.setLayoutY(y);
	}

		
	private void openDeleteUserForm() {
		Stage dialog = new Stage();
		dialog.initOwner(theStage);
		dialog.initModality(Modality.APPLICATION_MODAL);
		dialog.setTitle("Delete User");

		Label prompt = new Label("Enter username to delete:");
		TextField input = new TextField();
		Button confirm = new Button("Delete");
		confirm.setOnAction(e -> {
			String username = input.getText().trim();
			if (!username.isEmpty()) {
				ControllerAdminHome.deleteUser(username);
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
	}
	
	private void openManageInvitationsForm() { //This function defines the form
	    Stage dialog = new Stage();
	    dialog.initOwner(theStage);
	    dialog.initModality(Modality.APPLICATION_MODAL);
	    dialog.setTitle("Delete Invitation");

	    Label prompt = new Label("Enter invitation email to delete:");
	    TextField input = new TextField();
	    Button confirmBtn = new Button("Delete Invitation");

	    confirmBtn.setOnAction(e -> {
	        String email = input.getText().trim();
	        if (!email.isEmpty()) {
	            ControllerAdminHome.manageInvitations(email);
	            dialog.close();
	        }
	    });

	    VBox layout = new VBox(10, prompt, input, confirmBtn);
	    layout.setAlignment(Pos.CENTER);
	    layout.setMinWidth(300);
	    layout.setMinHeight(150);

	    Scene scene = new Scene(layout);
	    dialog.setScene(scene);
	    dialog.showAndWait();
	}

}
