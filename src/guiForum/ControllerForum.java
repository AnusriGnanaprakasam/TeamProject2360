package guiForum;

import java.util.List;
import java.util.Optional;

import javafx.scene.control.ButtonType;
import database.Database;
import entityClasses.Post;
import entityClasses.PostRepository;
import entityClasses.Reply;
import entityClasses.ReplyRepository;
import entityClasses.User;

/*******
 * <p> Title: ControllerForum Class. </p>
 *
 * <p> Description: The Forum Page Controller.  This class provides the controller actions
 * based on the user's use of the JavaFX GUI widgets defined by ViewForum.
 *
 * Supports full CRUD for posts and replies for the currently signed-in user.
 * Students may post, reply, update their own posts/replies, and soft-delete their own posts
 * (with confirmation).  Deleted posts show a placeholder but replies are preserved.
 * Staff/Admin additionally have thread CRUD capability.
 *
 * The class has been written assuming that the View or the Model are the only class methods
 * that can invoke these methods.  This is why each has been declared as "protected".
 * Do not change any of these methods to public.</p>
 *
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 *
 * @author Lynn Robert Carter
 *
 * @version 1.00		2026-02-25 Initial version
 */

public class ControllerForum {

	/*-*******************************************************************************************
	Controller attributes for this page

	This controller is not a class that gets instantiated.  Rather, it is a collection of
	protected static methods that can be called by the View (singleton) and the Model.
	*/

	/**
	 * Default constructor — not used.
	 */
	public ControllerForum() {
	}

	// Reference to the shared in-memory database
	private static Database theDatabase = applicationMain.HW2.database;

	// In-memory repositories — kept in sync with the database after every operation
	private static PostRepository  postRepo  = new PostRepository();
	private static ReplyRepository replyRepo = new ReplyRepository();


	// ===================================================================
	// POST CRUD
	// ===================================================================

	/**********
	 * <p> Title: createPost() Method. </p>
	 *
	 * <p> Description: Reads the title, thread, and content fields from the View and
	 * creates a new post attributed to the currently signed-in user.  Validates that
	 * title and content are non-empty.  Thread defaults to "General" if left blank.</p>
	 */
	protected static void createPost() {
		String title   = ViewForum.text_PostTitle.getText().trim();
		String thread  = ViewForum.text_PostThread.getText().trim();
		String content = ViewForum.text_PostContent.getText().trim();
		String author  = ViewForum.theUser.getUserName();

		// Validate required fields
		if (title.isEmpty()) {
			showError("Validation Error", "Post title cannot be empty.");
			return;
		}
		if (content.isEmpty()) {
			showError("Validation Error", "Post content cannot be empty.");
			return;
		}

		// Thread defaults to "General" if not specified
		if (thread.isEmpty()) thread = "General";

		theDatabase.createPost(author, thread, title, content);

		// Clear input fields and refresh the post list
		ViewForum.text_PostTitle.clear();
		ViewForum.text_PostThread.clear();
		ViewForum.text_PostContent.clear();
		refreshPostList();
	}


	/**********
	 * <p> Title: loadSelectedPost() Method. </p>
	 *
	 * <p> Description: Loads the full content of the post currently selected in the post
	 * list into the display area.  Also marks the post as read by the current user and
	 * refreshes the reply list.</p>
	 */
	protected static void loadSelectedPost() {
		String selected = ViewForum.list_Posts.getSelectionModel().getSelectedItem();
		if (selected == null) return;

		int postId = parseIdFromListItem(selected);
		if (postId < 0) return;

		String content = theDatabase.getPostContent(postId, ViewForum.theUser.getUserName());
		ViewForum.text_PostDisplay.setText(content);
		ViewForum.currentPostId = postId;

		refreshReplyList(postId);
	}


	/**********
	 * <p> Title: updatePost() Method. </p>
	 *
	 * <p> Description: Updates the title and content of the currently selected post.
	 * Only the original author may update their own post.  Validates non-empty fields.</p>
	 */
	protected static void updatePost() {
		if (ViewForum.currentPostId < 0) {
			showError("No Post Selected", "Please select a post to update.");
			return;
		}

		String newTitle   = ViewForum.text_PostTitle.getText().trim();
		String newContent = ViewForum.text_PostContent.getText().trim();

		if (newTitle.isEmpty()) {
			showError("Validation Error", "Post title cannot be empty.");
			return;
		}
		if (newContent.isEmpty()) {
			showError("Validation Error", "Post content cannot be empty.");
			return;
		}

		boolean updated = theDatabase.updatePost(
				ViewForum.currentPostId,
				ViewForum.theUser.getUserName(),
				newTitle,
				newContent);

		if (!updated) {
			showError("Update Failed",
					"Could not update post.\nYou can only update your own posts.");
			return;
		}

		ViewForum.text_PostTitle.clear();
		ViewForum.text_PostContent.clear();
		refreshPostList();
		loadSelectedPost();
	}


	/**********
	 * <p> Title: deletePost() Method. </p>
	 *
	 * <p> Description: Soft-deletes the currently selected post after showing an
	 * "Are you sure?" confirmation dialog.  Only the original author may delete.
	 * Replies to the post are preserved; viewers will see a deleted placeholder.</p>
	 */
	protected static void deletePost() {
		if (ViewForum.currentPostId < 0) {
			showError("No Post Selected", "Please select a post to delete.");
			return;
		}

		// Show confirmation before deleting
		ViewForum.confirm_DeletePost.setTitle("Confirm Delete");
		ViewForum.confirm_DeletePost.setHeaderText("Delete Post");
		ViewForum.confirm_DeletePost.setContentText(
				"Are you sure you want to delete this post?\n\n" +
				"Replies will be preserved but the post content will be removed.");

		Optional<ButtonType> result = ViewForum.confirm_DeletePost.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.OK) {
			boolean deleted = theDatabase.softDeletePost(
					ViewForum.currentPostId,
					ViewForum.theUser.getUserName());

			if (!deleted) {
				showError("Delete Failed",
						"Could not delete post.\nYou can only delete your own posts.");
				return;
			}

			ViewForum.text_PostDisplay.clear();
			ViewForum.currentPostId = -1;
			refreshPostList();
		}
	}


	// ===================================================================
	// REPLY CRUD
	// ===================================================================

	/**********
	 * <p> Title: createReply() Method. </p>
	 *
	 * <p> Description: Creates a reply to the currently loaded post.  Validates that
	 * a post is selected and that reply content is non-empty.</p>
	 */
	protected static void createReply() {
		if (ViewForum.currentPostId < 0) {
			showError("No Post Selected", "Please select a post before replying.");
			return;
		}

		String content = ViewForum.text_ReplyInput.getText().trim();
		if (content.isEmpty()) {
			showError("Validation Error", "Reply content cannot be empty.");
			return;
		}

		theDatabase.createReply(
				ViewForum.currentPostId,
				ViewForum.theUser.getUserName(),
				content);

		ViewForum.text_ReplyInput.clear();
		refreshReplyList(ViewForum.currentPostId);
	}


	/**********
	 * <p> Title: updateReply() Method. </p>
	 *
	 * <p> Description: Updates the content of the currently selected reply.  Only the
	 * original reply author may update.  Validates non-empty content.</p>
	 */
	protected static void updateReply() {
		String selected = ViewForum.list_Replies.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showError("No Reply Selected", "Please select a reply to update.");
			return;
		}

		int replyId = parseIdFromListItem(selected);
		if (replyId < 0) return;

		String newContent = ViewForum.text_ReplyInput.getText().trim();
		if (newContent.isEmpty()) {
			showError("Validation Error", "Reply content cannot be empty.");
			return;
		}

		boolean updated = theDatabase.updateReply(
				replyId,
				ViewForum.theUser.getUserName(),
				newContent);

		if (!updated) {
			showError("Update Failed",
					"Could not update reply.\nYou can only update your own replies.");
			return;
		}

		ViewForum.text_ReplyInput.clear();
		refreshReplyList(ViewForum.currentPostId);
	}


	/**********
	 * <p> Title: deleteReply() Method. </p>
	 *
	 * <p> Description: Permanently deletes the currently selected reply after confirmation.
	 * Only the original reply author may delete.</p>
	 */
	protected static void deleteReply() {
		String selected = ViewForum.list_Replies.getSelectionModel().getSelectedItem();
		if (selected == null) {
			showError("No Reply Selected", "Please select a reply to delete.");
			return;
		}

		int replyId = parseIdFromListItem(selected);
		if (replyId < 0) return;

		// Confirmation dialog
		ViewForum.confirm_DeleteReply.setTitle("Confirm Delete");
		ViewForum.confirm_DeleteReply.setHeaderText("Delete Reply");
		ViewForum.confirm_DeleteReply.setContentText(
				"Are you sure you want to permanently delete this reply?");

		Optional<ButtonType> result = ViewForum.confirm_DeleteReply.showAndWait();

		if (result.isPresent() && result.get() == ButtonType.OK) {
			boolean deleted = theDatabase.deleteReply(
					replyId,
					ViewForum.theUser.getUserName());

			if (!deleted) {
				showError("Delete Failed",
						"Could not delete reply.\nYou can only delete your own replies.");
				return;
			}

			ViewForum.text_ReplyInput.clear();
			refreshReplyList(ViewForum.currentPostId);
		}
	}


	// ===================================================================
	// SEARCH AND FILTERING
	// ===================================================================

	/**********
	 * <p> Title: searchPosts() Method. </p>
	 *
	 * <p> Description: Searches posts by the keyword entered in the search field.
	 * If thread filter is blank, all threads are searched.  Results are shown in
	 * the post list with read/unread status and reply counts.</p>
	 */
	protected static void searchPosts() {
		String keyword = ViewForum.text_Search.getText().trim();
		String thread  = ViewForum.text_PostThread.getText().trim();

		// Pass null thread to search all threads if not specified
		List<String> results = theDatabase.searchPosts(
				keyword.isEmpty() ? null : keyword,
				thread.isEmpty()  ? null : thread);

		ViewForum.list_Posts.getItems().setAll(
				formatPostListWithStatus(results));
	}


	/**********
	 * <p> Title: showMyPosts() Method. </p>
	 *
	 * <p> Description: Filters the post list to show only posts authored by the
	 * currently signed-in user, along with reply counts.</p>
	 */
	protected static void showMyPosts() {
		List<String> myPosts = theDatabase.getPostsByUser(
				ViewForum.theUser.getUserName());
		ViewForum.list_Posts.getItems().setAll(
				formatPostListWithStatus(myPosts));
	}


	/**********
	 * <p> Title: showAllPosts() Method. </p>
	 *
	 * <p> Description: Reloads the full unfiltered post list.</p>
	 */
	protected static void showAllPosts() {
		refreshPostList();
	}


	/**********
	 * <p> Title: toggleUnreadReplies() Method. </p>
	 *
	 * <p> Description: Filters the reply list for the current post to show only
	 * replies the current user has not yet read.</p>
	 */
	protected static void toggleUnreadReplies() {
		if (ViewForum.currentPostId < 0) return;

		String username = ViewForum.theUser.getUserName();

		// Ask the ReplyRepository for the unread subset — it already knows what
		// this user has read because refreshReplyList() called replyRepo.markAsRead()
		// for every reply that was loaded.
		java.util.List<Reply> unreadObjs = replyRepo.filterUnread(ViewForum.currentPostId, username);

		// Convert Reply objects back to display strings for the ListView
		java.util.List<String> unread = new java.util.ArrayList<>();
		for (Reply r : unreadObjs) {
			unread.add(r.toString());
		}

		ViewForum.list_Replies.getItems().setAll(unread);
	}


	// ===================================================================
	// THREAD CRUD (admin/staff only — buttons hidden for student roles)
	// ===================================================================

	/**********
	 * <p> Title: createThread() Method. </p>
	 *
	 * <p> Description: Creates a new discussion thread using the thread name and
	 * description fields.  Only visible and callable by admin users.  Thread name
	 * is required; description is optional.</p>
	 */
	protected static void createThread() {
		String name = ViewForum.text_ThreadName.getText().trim();
		String desc = ViewForum.text_ThreadDesc.getText().trim();

		if (name.isEmpty()) {
			showError("Validation Error", "Thread name cannot be empty.");
			return;
		}

		theDatabase.createThread(name, desc, ViewForum.theUser.getUserName());
		ViewForum.text_ThreadName.clear();
		ViewForum.text_ThreadDesc.clear();
		showInfo("Thread Created", "Thread '" + name + "' was created successfully.");
	}

	/**********
	 * <p> Title: deleteThread() Method. </p>
	 *
	 * <p> Description: Deletes the thread whose name is typed in the thread name field.
	 * The 'General' thread is protected and cannot be deleted.  Only visible and
	 * callable by admin users.</p>
	 */
	protected static void deleteThread() {
		String name = ViewForum.text_ThreadName.getText().trim();

		if (name.isEmpty()) {
			showError("Validation Error", "Enter the thread name to delete.");
			return;
		}

		boolean deleted = theDatabase.deleteThread(name);
		if (!deleted) {
			showError("Delete Failed",
					"Could not delete thread '" + name + "'.\n" +
					"The 'General' thread cannot be deleted.");
			return;
		}

		ViewForum.text_ThreadName.clear();
		ViewForum.text_ThreadDesc.clear();
		showInfo("Thread Deleted", "Thread '" + name + "' was deleted.");
	}


	// ===================================================================
	// NAVIGATION
	// ===================================================================

	/**********
	 * <p> Title: goBack() Method. </p>
	 *
	 * <p> Description: Navigates back to whichever home page the user came from.
	 * Checks the activeHomePage flag: 1 = Admin, 2 = Role1, 3 = Role2.</p>
	 */
	protected static void goBack() {
		int activePage = applicationMain.HW2.activeHomePage;
		if (activePage == 2) {
			// Came from Role1 home page
			guiRole1.ViewRole1Home.displayRole1Home(ViewForum.theStage, ViewForum.theUser);
		} else if (activePage == 3) {
			// Came from Role2 home page
			guiRole2.ViewRole2Home.displayRole2Home(ViewForum.theStage, ViewForum.theUser);
		} else {
			// Default: came from Admin home page (activePage == 1)
			guiAdminHome.ViewAdminHome.displayAdminHome(ViewForum.theStage, ViewForum.theUser);
		}
	}


	// ===================================================================
	// PRIVATE HELPERS
	// ===================================================================

	/**********
	 * <p> Title: refreshPostList() Method. </p>
	 *
	 * <p> Description: Reloads all posts from the database and populates the post list
	 * with read/unread status and reply counts for the current user.  Also rebuilds
	 * the in-memory PostRepository so it stays in sync with the database.</p>
	 */
	protected static void refreshPostList() {
		List<String> raw = theDatabase.getAllPosts();

		// Rebuild the in-memory PostRepository so it mirrors the database exactly.
		// Each "id | title" (or "id | [DELETED]") string from the DB is turned into
		// a Post object using the reconstruction constructor.
		postRepo.clear();
		for (String item : raw) {
			int id = parseIdFromListItem(item);
			if (id < 0) continue;

			boolean isDeleted = item.contains("[DELETED]");
			String  title     = isDeleted ? "[DELETED]"
			                             : item.split("\\|", 2)[1].trim();

			// Use reconstruction constructor: Post(id, author, thread, title, content, deleted, timestamp)
			// Author/thread/timestamp are not needed for list operations so empty strings are fine here.
			Post p = new Post(id, "", "", title, title, isDeleted, "");
			postRepo.addPost(p);
		}

		ViewForum.list_Posts.getItems().setAll(formatPostListWithStatus(raw));
	}


	/**********
	 * <p> Title: refreshReplyList(int postId) Method. </p>
	 *
	 * <p> Description: Reloads all replies for a given post and marks each one as
	 * read by the current user.</p>
	 *
	 * @param postId the id of the post whose replies to display
	 */
	protected static void refreshReplyList(int postId) {
		List<String> replies = theDatabase.getRepliesForPost(postId);
		String username = ViewForum.theUser.getUserName();

		// Rebuild the in-memory ReplyRepository for this post so it mirrors the database.
		// We remove all existing replies for this postId then re-add from the fresh DB list.
		// Each "id | author [timestamp]: content" string is turned into a Reply object.
		replyRepo.clear();
		for (String item : replies) {
			int id = parseIdFromListItem(item);
			if (id < 0) continue;

			// Parse "id | author [timestamp]: content"
			// Use the reconstruction constructor: Reply(id, postId, author, content, timestamp)
			Reply r = new Reply(id, postId, "", item, "");
			replyRepo.addReply(r);

			// Mark as read in both the database and the in-memory repository
			theDatabase.markReplyAsRead(id, username);
			replyRepo.markAsRead(id, username);
		}

		ViewForum.list_Replies.getItems().setAll(replies);
	}


	/**********
	 * <p> Title: formatPostListWithStatus(List<String>) Method. </p>
	 *
	 * <p> Description: Takes raw post display strings ("id | title") and decorates
	 * each with read/unread status and reply counts for the current user.</p>
	 *
	 * @param raw the raw list of post display strings from the database
	 * @return a decorated list of strings suitable for display in the post ListView
	 */
	private static List<String> formatPostListWithStatus(List<String> raw) {
		String username = ViewForum.theUser.getUserName();
		java.util.List<String> formatted = new java.util.ArrayList<>();

		for (String item : raw) {
			int postId = parseIdFromListItem(item);
			if (postId < 0) {
				formatted.add(item);
				continue;
			}

			boolean read      = theDatabase.hasUserReadPost(postId, username);
			int totalReplies  = theDatabase.countReplies(postId);
			int unreadReplies = theDatabase.countUnreadReplies(postId, username);

			String status = read ? "[READ]" : "[UNREAD]";
			String replyInfo = "  Replies: " + totalReplies
					+ "  Unread: " + unreadReplies;

			formatted.add(item + "  " + status + replyInfo);
		}

		return formatted;
	}


	/**********
	 * <p> Title: parseIdFromListItem(String) Method. </p>
	 *
	 * <p> Description: Parses the integer id from a list item string formatted as
	 * "id | ...".  Returns -1 if parsing fails.</p>
	 *
	 * @param item a display string in the format "id | ..."
	 * @return the parsed integer id, or -1 on failure
	 */
	private static int parseIdFromListItem(String item) {
		if (item == null || !item.contains("|")) return -1;
		try {
			return Integer.parseInt(item.split("\\|")[0].trim());
		} catch (NumberFormatException e) {
			return -1;
		}
	}


	/**********
	 * <p> Title: showError(String, String) Method. </p>
	 *
	 * <p> Description: Convenience method to display an error alert to the user.</p>
	 *
	 * @param header  the header text for the alert
	 * @param message the message body for the alert
	 */
	private static void showError(String header, String message) {
		ViewForum.alertError.setHeaderText(header);
		ViewForum.alertError.setContentText(message);
		ViewForum.alertError.showAndWait();
	}

	/**********
	 * <p> Title: showInfo(String, String) Method. </p>
	 *
	 * <p> Description: Convenience method to display an informational alert to the user.</p>
	 *
	 * @param header  the header text for the alert
	 * @param message the message body for the alert
	 */
	private static void showInfo(String header, String message) {
		ViewForum.alertInfo.setHeaderText(header);
		ViewForum.alertInfo.setContentText(message);
		ViewForum.alertInfo.showAndWait();
	}

}