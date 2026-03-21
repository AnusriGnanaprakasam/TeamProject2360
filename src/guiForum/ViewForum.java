package guiForum;

import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import database.Database;
import entityClasses.User;

/*******
 * <p> Title: ViewForum Class. </p>
 *
 * <p> Description: The Java/FX-based Forum Page.  This class provides the JavaFX GUI widgets
 * that enable a signed-in user to participate in the Student Discussion System.
 *
 * Supports full CRUD for posts and replies per the Version 2 requirements:
 *   - Students can create, read, update, and soft-delete their own posts and replies
 *   - Posts default to the "General" thread if no thread is specified
 *   - Deleting a post shows an "Are you sure?" confirmation; replies are preserved
 *   - Students can search posts by keyword across all threads or within a specific thread
 *   - Read/unread status and reply counts are shown for every post
 *   - Students can filter to show only unread replies
 *
 * The class follows the singleton design pattern and is the View portion of MVC.
 * All access starts by calling the static method displayForum().
 *
 * <p> Copyright: Anusri Gnanaprakasam © 2026 </p>
 *
 * @author Anusri Gnanaprakasam
 *
 * @version 1.00		2026-02-25 Initial version
 */

public class ViewForum {

	/*-*******************************************************************************************
	Attributes
	*/
	
	/**
	 * Maintains a consistent forum view size aligned with the main application window,
	 * ensuring a uniform layout and seamless integration with the overall UI.
	 */
	private static double width  = applicationMain.HW2.WINDOW_WIDTH;

	/**
	 * Maintains a consistent forum view height aligned with the main application window,
	 * supporting a predictable and user-friendly interface experience.
	 */
	private static double height = applicationMain.HW2.WINDOW_HEIGHT;

	// Area 1: Page header 
	/**
	 * Displays the title of the current page to orient users within the application.
	 */
	private static Label label_PageTitle  = new Label("Discussion Forum");

	/**
	 * Displays the currently logged-in user to provide context and reinforce session awareness.
	 */
	private static Label label_LoggedInAs = new Label();

	/**
	 * Visual separator used to structure the layout and improve readability of the header section.
	 */
	private static Line  line_Sep1        = new Line(20, 55, width - 20, 55);

	// Area 2: Post list (left column)
	/**
	 * Labels the post list section, helping users identify where available discussions are displayed.
	 */
	private static Label label_PostList = new Label("Posts");

	/**
	 * Displays available discussion posts and serves as the primary navigation component
	 * for selecting and loading post content.
	 */
	protected static ListView<String> list_Posts = new ListView<>();

	
	// Search / filter controls
	/**
	 * Input field for entering search queries to filter posts efficiently.
	 */
	protected static TextField text_Search     = new TextField();

	/**
	 * Triggers filtering of posts based on the user's search input.
	 */
	private static Button      button_Search   = new Button("Search");

	/**
	 * Resets the post list to show all available posts, removing any applied filters.
	 */
	private static Button      button_AllPosts = new Button("All Posts");

	/**
	 * Filters the post list to show only posts created by the current user,
	 * supporting personalized navigation.
	 */
	private static Button      button_MyPosts  = new Button("My Posts");


	// Area 3: Post content display (center)
	/**
	 * Labels the post content display area to clarify its purpose.
	 */
	private static Label label_PostDisplay = new Label("Post Content");

	/**
	 * Displays the full content of the selected post, allowing users to read and understand discussions.
	 */
	protected static TextArea text_PostDisplay = new TextArea();

	// Area 4: Reply list (right column)
	/**
	 * Labels the reply list section to indicate where responses to a post are shown.
	 */
	private static Label label_ReplyList = new Label("Replies");

	/**
	 * Displays replies associated with the selected post, enabling users to follow conversations.
	 */
	protected static ListView<String> list_Replies = new ListView<>();

	/**
	 * Filters replies to show only those that have not yet been read by the user.
	 */
	private static Button button_UnreadOnly = new Button("Unread Only");

	/**
	 * Displays all replies associated with the selected post, removing any filters.
	 */
	private static Button button_AllReplies = new Button("All Replies");

	// Area 5: Create / edit post inputs
	/**
	 * Labels the input section for creating or editing posts, guiding user interaction.
	 */
	private static Label      label_NewPost       = new Label("Create / Edit Post");

	/**
	 * Visual separator used to distinguish the post input section from other UI areas.
	 */
	private static Line       line_Sep2            = new Line(20, 435, width - 20, 435);

	/**
	 * Input field for specifying the title of a post, improving organization and readability.
	 */
	protected static TextField text_PostTitle      = new TextField();

	/**
	 * Input field for assigning a post to a thread, supporting topic-based organization.
	 */
	protected static TextField text_PostThread     = new TextField();

	/**
	 * Input area for composing the main content of a post.
	 */
	protected static TextArea  text_PostContent    = new TextArea();

	/**
	 * Initiates creation of a new post using the provided input fields.
	 */
	private static Button button_CreatePost = new Button("Create Post");

	/**
	 * Applies updates to an existing post, enabling content modification.
	 */
	private static Button button_UpdatePost = new Button("Update Post");

	/**
	 * Deletes the selected post after confirmation, supporting content management.
	 */
	private static Button button_DeletePost = new Button("Delete Post");

	
	// Area 6: Reply input
	/**
	 * Labels the reply input section, guiding users to contribute to discussions.
	 */
	private static Label     label_ReplyInput  = new Label("Write a Reply");

	/**
	 * Input area for composing replies to the selected post.
	 */
	protected static TextArea text_ReplyInput  = new TextArea();

	/**
	 * Submits a new reply to the currently selected post.
	 */
	private static Button button_CreateReply = new Button("Post Reply");

	/**
	 * Updates an existing reply, allowing users to edit their contributions.
	 */
	private static Button button_UpdateReply = new Button("Update Reply");

	/**
	 * Deletes a selected reply after confirmation, supporting moderation and cleanup.
	 */
	private static Button button_DeleteReply = new Button("Delete Reply");

	// Thread management (staff/admin only — hidden for student roles)
	/**
	 * Labels the thread management section, which is intended for administrative use only.
	 */
	private static Label      label_ThreadMgmt  = new Label("Thread Management (Staff/Admin)");

	/**
	 * Input field for specifying the name of a new discussion thread.
	 */
	protected static TextField text_ThreadName  = new TextField();

	/**
	 * Input field for providing a description of a discussion thread, aiding organization.
	 */
	protected static TextField text_ThreadDesc  = new TextField();

	/**
	 * Creates a new discussion thread, supporting topic organization (restricted to elevated roles).
	 */
	private static Button button_CreateThread   = new Button("Create Thread");

	/**
	 * Deletes an existing discussion thread, supporting administrative management of topics.
	 */
	private static Button button_DeleteThread   = new Button("Delete Thread");

	// Navigation
	/**
	 * Navigates the user back to the previous screen, supporting application flow control.
	 */
	private static Button button_Back = new Button("Back");

	// Alerts
	/**
	 * Displays error messages to inform users of issues during operations.
	 */
	protected static Alert alertError          = new Alert(AlertType.ERROR);

	/**
	 * Displays informational messages to provide feedback on successful operations.
	 */
	protected static Alert alertInfo           = new Alert(AlertType.INFORMATION);

	/**
	 * Confirmation dialog for deleting a post, preventing accidental data loss.
	 */
	protected static Alert confirm_DeletePost  = new Alert(AlertType.CONFIRMATION);

	/**
	 * Confirmation dialog for deleting a reply, ensuring intentional user actions.
	 */
	protected static Alert confirm_DeleteReply = new Alert(AlertType.CONFIRMATION);

	// State
	/**
	 * Tracks the ID of the currently selected post; -1 indicates no post is selected.
	 */
	protected static int currentPostId = -1;

	// Singleton infrastructure ----
	/**
	 * Singleton instance of the forum view, ensuring only one instance manages the UI state.
	 */
	private static ViewForum   theView;

	/**
	 * Reference to the shared database instance, enabling persistence and data retrieval.
	 */
	private static Database    theDatabase = applicationMain.HW2.database;

	/**
	 * Primary stage used to render the forum UI within the application window.
	 */
	protected static Stage     theStage;

	/**
	 * Root container for all UI components in the forum view.
	 */
	private static Pane        theRootPane;

	/**
	 * Represents the currently authenticated user, enabling role-based behavior and personalization.
	 */
	protected static User      theUser;

	/**
	 * Scene containing the forum UI, allowing switching between different application views.
	 */
	private static Scene       theForumScene;

	/**
	 * Constant representing the administrative role, used to control access to restricted features.
	 */
	private static final int   theRole = 1;


	/*-*******************************************************************************************
	Entry point
	*/

	/*******
	 * <p> Method: displayForum(Stage ps, User user) </p>
	 *
	 * <p> Description: Static entry point — creates the singleton if needed, then
	 * sets the stage title and scene and shows the window for student to see the forum.  Called from
	 * ControllerAdminHome.openForum().</p>
	 *
	 * @param ps   the JavaFX Stage to use
	 * @param user the currently signed-in User
	 */
	public static void displayForum(Stage ps, User user) {
		theStage = ps;
		theUser  = user;

		if (theView == null) theView = new ViewForum();  // Instantiate singleton if needed

		// Refresh the post list every time the forum is opened
		label_LoggedInAs.setText("Signed in as: " + theUser.getUserName());
		currentPostId = -1;
		text_PostDisplay.clear();
		text_ReplyInput.clear();
		list_Replies.getItems().clear();
		ControllerForum.refreshPostList();

		// Show thread management controls only for Admin (activeHomePage == 1).
		// Students (Role1 = 2, Role2 = 3) do not have authority to create or
		// delete threads per the Permissions user story.
		boolean isAdminOrStaff = (applicationMain.HW2.activeHomePage == 1);
		label_ThreadMgmt.setVisible(isAdminOrStaff);
		text_ThreadName.setVisible(isAdminOrStaff);
		text_ThreadDesc.setVisible(isAdminOrStaff);
		button_CreateThread.setVisible(isAdminOrStaff);
		button_DeleteThread.setVisible(isAdminOrStaff);

		theStage.setTitle("CSE 360 Foundation Code: Discussion Forum");
		theStage.setScene(theForumScene);
		theStage.show();
	}

	/*******
	 * <p> Constructor for ViewForum() </p>
	 *
	 * <p> Description: Creates a new scene for the forum and sets up all the text boxes
	 * along with labels, buttons, posts, and replies. This is for the student to have proper buttons
	 * to click, and it calls various methods (CRUD) for both posts and replies. </p>
	 *
	 * @param ps   the JavaFX Stage to use
	 * @param user the currently signed-in User
	 */

	/*-*******************************************************************************************
	Constructor
	*/

	private ViewForum() {
		theRootPane   = new Pane();
		theForumScene = new Scene(theRootPane, width, height);

		
		// Area 1 — Page header
		setupLabelUI(label_PageTitle,  "Arial", 26, width, Pos.CENTER,       0,   5);
		setupLabelUI(label_LoggedInAs, "Arial", 14, width, Pos.BASELINE_LEFT, 20, 35);

		// Area 2 — Post list (left column, x=20, width=280)
		setupLabelUI(label_PostList, "Arial", 14, 280, Pos.BASELINE_LEFT, 20, 62);

		list_Posts.setLayoutX(20);
		list_Posts.setLayoutY(80);
		list_Posts.setPrefWidth(340);
		list_Posts.setPrefHeight(330);
		// Load post content when user clicks a post
		list_Posts.setOnMouseClicked((_) -> ControllerForum.loadSelectedPost());

		// Search controls
		setupTextFieldUI(text_Search, "Arial", 14, 200, Pos.BASELINE_LEFT, 20, 422);
		text_Search.setPromptText("Search keyword...");

		setupButtonUI(button_Search,   "Dialog", 13, 90,  Pos.CENTER, 228, 420);
		setupButtonUI(button_AllPosts, "Dialog", 13, 90,  Pos.CENTER, 20,  420);
		setupButtonUI(button_MyPosts,  "Dialog", 13, 90,  Pos.CENTER, 118, 420);

		// Reorder: AllPosts and MyPosts above search bar — place at y=422 later;
		// easier to just put them all at the bottom of the list column
		setupButtonUI(button_AllPosts, "Dialog", 13, 100, Pos.CENTER, 20,  418);
		setupButtonUI(button_MyPosts,  "Dialog", 13, 100, Pos.CENTER, 128, 418);
		setupButtonUI(button_Search,   "Dialog", 13, 100, Pos.CENTER, 236, 418);
		setupTextFieldUI(text_Search, "Arial", 13, 340, Pos.BASELINE_LEFT, 20, 446);
		text_Search.setPromptText("Keyword (leave blank for all)...");

		button_AllPosts.setOnAction((_) -> ControllerForum.showAllPosts());
		button_MyPosts.setOnAction((_)  -> ControllerForum.showMyPosts());
		button_Search.setOnAction((_)   -> ControllerForum.searchPosts());

		// Area 3 — Post content display (centre column, x=375, width=280)
		setupLabelUI(label_PostDisplay, "Arial", 14, 280, Pos.BASELINE_LEFT, 375, 62);

		text_PostDisplay.setLayoutX(375);
		text_PostDisplay.setLayoutY(80);
		text_PostDisplay.setPrefWidth(280);
		text_PostDisplay.setPrefHeight(330);
		text_PostDisplay.setEditable(false);
		text_PostDisplay.setWrapText(true);

		// Area 4 — Reply list (right column, x=670, width=290)
		setupLabelUI(label_ReplyList, "Arial", 14, 290, Pos.BASELINE_LEFT, 670, 62);

		list_Replies.setLayoutX(670);
		list_Replies.setLayoutY(80);
		list_Replies.setPrefWidth(290);
		list_Replies.setPrefHeight(260);

		setupButtonUI(button_UnreadOnly, "Dialog", 13, 135, Pos.CENTER, 670, 348);
		setupButtonUI(button_AllReplies, "Dialog", 13, 135, Pos.CENTER, 812, 348);

		button_UnreadOnly.setOnAction((_) -> ControllerForum.toggleUnreadReplies());
		button_AllReplies.setOnAction((_) -> ControllerForum.refreshReplyList(currentPostId));

		// Area 5 — New / edit post inputs (bottom-left, y≈475)
		setupLabelUI(label_NewPost, "Arial", 14, 340, Pos.BASELINE_LEFT, 20, 473);

		setupTextFieldUI(text_PostTitle,  "Arial", 13, 165, Pos.BASELINE_LEFT, 20,  493);
		setupTextFieldUI(text_PostThread, "Arial", 13, 165, Pos.BASELINE_LEFT, 195, 493);
		text_PostTitle.setPromptText("Title (required)");
		text_PostThread.setPromptText("Thread (default: General)");

		text_PostContent.setLayoutX(20);
		text_PostContent.setLayoutY(520);
		text_PostContent.setPrefWidth(340);
		text_PostContent.setPrefHeight(80);
		text_PostContent.setPromptText("Post content (required)...");
		text_PostContent.setWrapText(true);

		setupButtonUI(button_CreatePost, "Dialog", 13, 105, Pos.CENTER, 20,  607);
		setupButtonUI(button_UpdatePost, "Dialog", 13, 105, Pos.CENTER, 133, 607);
		setupButtonUI(button_DeletePost, "Dialog", 13, 105, Pos.CENTER, 246, 607);

		button_CreatePost.setOnAction((_) -> ControllerForum.createPost());
		button_UpdatePost.setOnAction((_) -> ControllerForum.updatePost());
		button_DeletePost.setOnAction((_) -> ControllerForum.deletePost());

		// Area 6 — Reply input (bottom-right, y≈473)
		setupLabelUI(label_ReplyInput, "Arial", 14, 610, Pos.BASELINE_LEFT, 375, 473);

		text_ReplyInput.setLayoutX(375);
		text_ReplyInput.setLayoutY(493);
		text_ReplyInput.setPrefWidth(585);
		text_ReplyInput.setPrefHeight(80);
		text_ReplyInput.setPromptText("Write a reply...");
		text_ReplyInput.setWrapText(true);

		setupButtonUI(button_CreateReply, "Dialog", 13, 120, Pos.CENTER, 375, 580);
		setupButtonUI(button_UpdateReply, "Dialog", 13, 120, Pos.CENTER, 503, 580);
		setupButtonUI(button_DeleteReply, "Dialog", 13, 120, Pos.CENTER, 631, 580);

		button_CreateReply.setOnAction((_) -> ControllerForum.createReply());
		button_UpdateReply.setOnAction((_) -> ControllerForum.updateReply());
		button_DeleteReply.setOnAction((_) -> ControllerForum.deleteReply());

		// Navigation — Back button
		setupButtonUI(button_Back, "Dialog", 16, 120, Pos.CENTER, 20, 640);
		button_Back.setOnAction((_) -> ControllerForum.goBack());

		// Thread management (staff/admin only — visibility set in displayForum)
		setupLabelUI(label_ThreadMgmt, "Arial", 12, 400, Pos.BASELINE_LEFT, 375, 615);

		setupTextFieldUI(text_ThreadName, "Arial", 12, 180, Pos.BASELINE_LEFT, 375, 635);
		text_ThreadName.setPromptText("Thread name (required)");

		setupTextFieldUI(text_ThreadDesc, "Arial", 12, 180, Pos.BASELINE_LEFT, 563, 635);
		text_ThreadDesc.setPromptText("Description (optional)");

		setupButtonUI(button_CreateThread, "Dialog", 12, 120, Pos.CENTER, 375, 660);
		setupButtonUI(button_DeleteThread, "Dialog", 12, 120, Pos.CENTER, 503, 660);

		button_CreateThread.setOnAction((_) -> ControllerForum.createThread());
		button_DeleteThread.setOnAction((_) -> ControllerForum.deleteThread());

		// Add all widgets to the pane
		theRootPane.getChildren().addAll(
			// Header
			label_PageTitle, label_LoggedInAs, line_Sep1,

			// Post list column
			label_PostList, list_Posts,
			button_AllPosts, button_MyPosts, button_Search, text_Search,

			// Post display column
			label_PostDisplay, text_PostDisplay,

			// Reply list column
			label_ReplyList, list_Replies,
			button_UnreadOnly, button_AllReplies,

			// Create/edit post area
			label_NewPost,
			text_PostTitle, text_PostThread, text_PostContent,
			button_CreatePost, button_UpdatePost, button_DeletePost,

			// Reply input area
			label_ReplyInput, text_ReplyInput,
			button_CreateReply, button_UpdateReply, button_DeleteReply,

			// Navigation
			button_Back,

			// Thread management (visible to admin only)
			label_ThreadMgmt, text_ThreadName, text_ThreadDesc,
			button_CreateThread, button_DeleteThread
		);
	}


	/*******
	Helper methods
	*/

	/**********
	 * <p> Title: setupLabelUI(label, font, fontsize, width, position, positionX, positionY) Method. </p>
	 *
	 * <p> Description: Private local method that sets the label field user interface, including instantiating the text field object, setting the font type
	 * and size, creating the width and position, as well as set the text's position within the box. </p>
	 */
	private void setupLabelUI(Label l, String ff, double f, double w, Pos p, double x, double y) {
		l.setFont(Font.font(ff, f));
		l.setMinWidth(w);
		l.setAlignment(p);
		l.setLayoutX(x);
		l.setLayoutY(y);
	}

	/**********
	 * <p> Title: setupButtonUI(button, font, fontsize, width, position, positionX, positionY) Method. </p>
	 *
	 * <p> Description: Private local method that sets the button field user interface, including instantiating the text field object, setting the font type
	 * and size, creating the width and position, as well as set the text's position within the box. </p>
	 */
	private void setupButtonUI(Button b, String ff, double f, double w, Pos p, double x, double y) {
		b.setFont(Font.font(ff, f));
		b.setMinWidth(w);
		b.setAlignment(p);
		b.setLayoutX(x);
		b.setLayoutY(y);
	}

	/**********
	 * <p> Title: setupTextFieldUI(text, font, fontsize, width, position, positionX, positionY) Method. </p>
	 *
	 * <p> Description: Private local method that sets the text field user interface, including instantiating the text field object, setting the font type
	 * and size, creating the width and position, as well as set the text's position within the box. </p>
	 */
	private void setupTextFieldUI(TextField t, String ff, double f, double w, Pos p, double x, double y) {
		t.setFont(Font.font(ff, f));
		t.setMinWidth(w);
		t.setMaxWidth(w);
		t.setAlignment(p);
		t.setLayoutX(x);
		t.setLayoutY(y);
		t.setEditable(true);
	}

}