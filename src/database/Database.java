package database;

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import entityClasses.User;

/*******
 * <p> Title: Database Class. </p>
 * 
 * <p> Description: This is an in-memory database built on H2.  Detailed documentation of H2 can
 * be found at https://www.h2database.com/html/main.html (Click on "PDF (2MP) for a PDF of 438 pages
 * on the H2 main page.)  This class leverages H2 and provides numerous special supporting methods.
 * </p>
 * 
 * <p> Copyright: Lynn Robert Carter © 2025 </p>
 * 
 * @author Lynn Robert Carter
 * 
 * @version 2.00		2025-04-29 Updated and expanded from the version produce by on a previous
 * 							version by Pravalika Mukkiri and Ishwarya Hidkimath Basavaraj
 * @version 2.01		2025-12-17 Minor updates for Spring 2026
 * @version 2.02		2026-02-25 Added forum/discussion system tables and methods
 */

/*
 * The Database class is responsible for establishing and managing the connection to the database,
 * and performing operations such as user registration, login validation, handling invitation 
 * codes, and numerous other database related functions.
 */
public class Database {

	// JDBC driver name and database URL 
	static final String JDBC_DRIVER = "org.h2.Driver";   
	static final String DB_URL = "jdbc:h2:~/FoundationDatabase";  

	//  Database credentials 
	static final String USER = "sa"; 
	static final String PASS = ""; 

	//  Shared variables used within this class
	private Connection connection = null;		// Singleton to access the database 
	private Statement statement = null;			// The H2 Statement is used to construct queries
	
	// These are the easily accessible attributes of the currently logged-in user
	// This is only useful for single user applications
	private String currentUsername;
	private String currentPassword;
	private String currentFirstName;
	private String currentMiddleName;
	private String currentLastName;
	private String currentPreferredFirstName;
	private String currentEmailAddress;
	private boolean currentAdminRole;
	private boolean currentNewRole1;
	private boolean currentNewRole2;

	/*******
	 * <p> Method: Database </p>
	 * 
	 * <p> Description: The default constructor used to establish this singleton object.</p>
	 * 
	 */
	
	public Database () {
		
	}
	
		/********* CLEAR DATABASE IS BELOW HERE ***************
/*******
 * <p> Method: connectToDatabase </p>
 * 
 * <p> Description: Used to establish the in-memory instance of the H2 database from secondary
 *		storage.</p>
 *
 * @throws SQLException when the DriverManager is unable to establish a connection
 * 
 */
	public void connectToDatabase() throws SQLException {
		try {
			Class.forName(JDBC_DRIVER); // Load the JDBC driver
			connection = DriverManager.getConnection(DB_URL, USER, PASS);
			statement = connection.createStatement(); 
			// You can use this command to clear the database and restart from fresh.
			//statement.execute("DROP ALL OBJECTS");

			createTables();  // Create the necessary tables if they don't exist
			clearAllDiscussionPosts(); //if you want the forum to be empty
		} catch (ClassNotFoundException e) {
			System.err.println("JDBC Driver not found: " + e.getMessage());
		}
	}

/*******
	 * <p> Method: clearAllDiscussionPosts </p>
	 *
	 * <p> Description: Permanently deletes ALL posts, replies, and read-tracking
	 * records from the forum tables.  This is an admin-only destructive operation
	 * used to reset the discussion board.  The forumThreads table (including the
	 * protected "General" thread) is preserved.</p>
	 *
	 * @return true if the operation succeeded, false if a database error occurred
	 */
	public boolean clearAllDiscussionPosts() {
	    try (Statement stmt = connection.createStatement()) {
	        stmt.execute("DELETE FROM forumReplyReads");
	        stmt.execute("DELETE FROM forumPostReads");
	        stmt.execute("DELETE FROM forumReplies");
	        stmt.execute("DELETE FROM forumPosts");
	        return true;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}
	
/*******
 * <p> Method: createTables </p>
 * 
 * <p> Description: Used to create new instances of the two database tables used by this class.</p>
 * 
 */
	private void createTables() throws SQLException {
		// Create the user database
		String userTable = "CREATE TABLE IF NOT EXISTS userDB ("
				+ "id INT AUTO_INCREMENT PRIMARY KEY, "
				+ "userName VARCHAR(255) UNIQUE, "
				+ "password VARCHAR(255), "
				+ "firstName VARCHAR(255), "
				+ "middleName VARCHAR(255), "
				+ "lastName VARCHAR (255), "
				+ "preferredFirstName VARCHAR(255), "
				+ "emailAddress VARCHAR(255), "
				+ "adminRole BOOL DEFAULT FALSE, "
				+ "newRole1 BOOL DEFAULT FALSE, "
				+ "newRole2 BOOL DEFAULT FALSE)";
		statement.execute(userTable);
		
		// Create the invitation codes table
	    String invitationCodesTable = "CREATE TABLE IF NOT EXISTS InvitationCodes ("
	            + "code VARCHAR(10) PRIMARY KEY, "
	    		+ "emailAddress VARCHAR(255), "
	            + "role VARCHAR(10))";
	    statement.execute(invitationCodesTable);

	    // ---------------------------------------------------------------
	    // FORUM / DISCUSSION SYSTEM TABLES
	    // ---------------------------------------------------------------

	    // Discussion threads (e.g. "General", "Homework", etc.) — staff-managed
	    String threadsTable = "CREATE TABLE IF NOT EXISTS forumThreads ("
	            + "name VARCHAR(255) PRIMARY KEY, "
	            + "description VARCHAR(500), "
	            + "createdBy VARCHAR(255), "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
	    statement.execute(threadsTable);

	    // Ensure the default "General" thread always exists
	    statement.execute("MERGE INTO forumThreads (name, description, createdBy) "
	            + "KEY(name) VALUES ('General', 'Default discussion thread', 'system')");

	    // Forum posts — supports soft delete (deleted flag) so replies are preserved
	    String postsTable = "CREATE TABLE IF NOT EXISTS forumPosts ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "author VARCHAR(255), "
	            + "thread VARCHAR(255) DEFAULT 'General', "
	            + "title VARCHAR(255), "
	            + "content CLOB, "
	            + "deleted BOOL DEFAULT FALSE, "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
	    statement.execute(postsTable);

	    // Forum replies — linked to posts by postId
	    String repliesTable = "CREATE TABLE IF NOT EXISTS forumReplies ("
	            + "id INT AUTO_INCREMENT PRIMARY KEY, "
	            + "postId INT, "
	            + "author VARCHAR(255), "
	            + "content CLOB, "
	            + "timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";
	    statement.execute(repliesTable);

	    // Post read-tracking — records which users have read which posts
	    String postReadsTable = "CREATE TABLE IF NOT EXISTS forumPostReads ("
	            + "postId INT, "
	            + "username VARCHAR(255), "
	            + "PRIMARY KEY (postId, username))";
	    statement.execute(postReadsTable);

	    // Reply read-tracking — records which users have read which replies
	    String replyReadsTable = "CREATE TABLE IF NOT EXISTS forumReplyReads ("
	            + "replyId INT, "
	            + "username VARCHAR(255), "
	            + "PRIMARY KEY (replyId, username))";
	    statement.execute(replyReadsTable);
	}


	// ===================================================================
	// FORUM: THREAD CRUD (staff only — permission enforced in controller)
	// ===================================================================

	/*******
	 * <p> Method: createThread </p>
	 * 
	 * <p> Description: Creates a new discussion thread. Staff-only operation;
	 * 		permission must be enforced by the calling controller.</p>
	 * 
	 * @param name        the thread name (required, must be non-blank)
	 * @param description a brief description of the thread's purpose
	 * @param createdBy   the username of the staff member creating the thread
	 */
	public void createThread(String name, String description, String createdBy) {
	    if (name == null || name.isBlank())
	        throw new IllegalArgumentException("Thread name cannot be empty.");
	    String query = "INSERT INTO forumThreads (name, description, createdBy) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, name.trim());
	        pstmt.setString(2, description);
	        pstmt.setString(3, createdBy);
	        pstmt.executeUpdate();
	    } catch (SQLException e) { e.printStackTrace(); }
	}

	/*******
	 * <p> Method: getAllThreadNames </p>
	 * 
	 * <p> Description: Returns a list of all thread names, sorted alphabetically.</p>
	 * 
	 * @return a List of thread name strings
	 */
	public List<String> getAllThreadNames() {
	    List<String> threads = new ArrayList<>();
	    String query = "SELECT name FROM forumThreads ORDER BY name";
	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        while (rs.next()) threads.add(rs.getString("name"));
	    } catch (SQLException e) { e.printStackTrace(); }
	    return threads;
	}

	/*******
	 * <p> Method: updateThread </p>
	 * 
	 * <p> Description: Updates the description of an existing thread. Staff-only operation.</p>
	 * 
	 * @param name           the name of the thread to update
	 * @param newDescription the new description for the thread
	 */
	public void updateThread(String name, String newDescription) {
	    String query = "UPDATE forumThreads SET description = ? WHERE name = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newDescription);
	        pstmt.setString(2, name);
	        pstmt.executeUpdate();
	    } catch (SQLException e) { e.printStackTrace(); }
	}

	/*******
	 * <p> Method: deleteThread </p>
	 * 
	 * <p> Description: Deletes a thread by name. Staff-only operation.  The "General"
	 * 		thread is protected and cannot be deleted.</p>
	 * 
	 * @param name the name of the thread to delete
	 * @return true if deleted, false if protected or not found
	 */
	public boolean deleteThread(String name) {
	    if ("General".equals(name)) return false; // Protect the default thread
	    String query = "DELETE FROM forumThreads WHERE name = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, name);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}


	// ===================================================================
	// FORUM: POST CRUD
	// ===================================================================

	/*******
	 * <p> Method: createPost </p>
	 * 
	 * <p> Description: Creates a new forum post.  Thread defaults to "General" if
	 * 		null or blank.  Title and content are required.</p>
	 * 
	 * @param author  the username of the posting user
	 * @param thread  the thread to post in (defaults to "General" if blank)
	 * @param title   the post title (required, non-blank)
	 * @param content the post body text (required, non-blank)
	 * @throws IllegalArgumentException if title or content is empty
	 */
	public void createPost(String author, String thread, String title, String content) {
	    if (title == null || title.isBlank())
	        throw new IllegalArgumentException("Post title cannot be empty.");
	    if (content == null || content.isBlank())
	        throw new IllegalArgumentException("Post content cannot be empty.");
	    if (thread == null || thread.isBlank()) thread = "General"; // Default thread

	    String query = "INSERT INTO forumPosts (author, thread, title, content) VALUES (?, ?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, author);
	        pstmt.setString(2, thread);
	        pstmt.setString(3, title);
	        pstmt.setString(4, content);
	        pstmt.executeUpdate();
	    } catch (SQLException e) { e.printStackTrace(); }
	}

	/*******
	 * <p> Method: getAllPosts </p>
	 * 
	 * <p> Description: Returns all posts as display strings "id | title" (or
	 * 		"id | [DELETED]" for soft-deleted posts), ordered newest first.</p>
	 * 
	 * @return a List of post display strings
	 */
	public List<String> getAllPosts() {
	    List<String> list = new ArrayList<>();
	    String query = "SELECT id, title, deleted FROM forumPosts ORDER BY timestamp DESC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {
	        while (rs.next()) {
	            String label = rs.getInt("id") + " | " +
	                    (rs.getBoolean("deleted") ? "[DELETED]" : rs.getString("title"));
	            list.add(label);
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return list;
	}

	/*******
	 * <p> Method: getPostsByUser </p>
	 * 
	 * <p> Description: Returns all posts authored by a specific user, newest first.</p>
	 * 
	 * @param username the username to filter posts by
	 * @return a List of post display strings for that user
	 */
	public List<String> getPostsByUser(String username) {
	    List<String> list = new ArrayList<>();
	    String query = "SELECT id, title, deleted FROM forumPosts "
	                 + "WHERE author = ? ORDER BY timestamp DESC";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            String label = rs.getInt("id") + " | " +
	                    (rs.getBoolean("deleted") ? "[DELETED]" : rs.getString("title"));
	            list.add(label);
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return list;
	}

	/*******
	 * <p> Method: getPostContent </p>
	 * 
	 * <p> Description: Returns the full formatted content of a post.  If the post has
	 * 		been soft-deleted a placeholder is returned instead.  Also marks the post
	 * 		as read by the viewing user.</p>
	 * 
	 * @param id          the integer id of the post
	 * @param viewingUser the username of the user viewing the post (for read tracking)
	 * @return formatted String with thread/author/time/title/content, or deleted placeholder
	 */
	public String getPostContent(int id, String viewingUser) {
	    String query = "SELECT * FROM forumPosts WHERE id = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, id);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            if (rs.getBoolean("deleted"))
	                return "[This post has been deleted by the author]";
	            markPostAsRead(id, viewingUser); // Auto-mark as read when viewed
	            return "Thread: " + rs.getString("thread") + "\n"
	                 + "Author: " + rs.getString("author") + "\n"
	                 + "Time:   " + rs.getTimestamp("timestamp") + "\n\n"
	                 + rs.getString("title") + "\n\n"
	                 + rs.getString("content");
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return "";
	}

	/*******
	 * <p> Method: updatePost </p>
	 * 
	 * <p> Description: Updates the title and content of a post.  Only the original
	 * 		author may update; soft-deleted posts cannot be updated.</p>
	 * 
	 * @param id             the integer id of the post to update
	 * @param requestingUser the username requesting the update (must match author)
	 * @param newTitle       replacement title (required, non-blank)
	 * @param newContent     replacement content (required, non-blank)
	 * @return true if updated, false if user is not the author or post not found
	 * @throws IllegalArgumentException if newTitle or newContent is empty
	 */
	public boolean updatePost(int id, String requestingUser, String newTitle, String newContent) {
	    if (newTitle == null || newTitle.isBlank())
	        throw new IllegalArgumentException("Post title cannot be empty.");
	    if (newContent == null || newContent.isBlank())
	        throw new IllegalArgumentException("Post content cannot be empty.");
	    String query = "UPDATE forumPosts SET title = ?, content = ? "
	                 + "WHERE id = ? AND author = ? AND deleted = FALSE";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newTitle);
	        pstmt.setString(2, newContent);
	        pstmt.setInt(3, id);
	        pstmt.setString(4, requestingUser);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}

	/*******
	 * <p> Method: softDeletePost </p>
	 * 
	 * <p> Description: Soft-deletes a post by setting deleted=TRUE and replacing its
	 * 		content with a placeholder.  Only the original author may delete.  Replies
	 * 		are preserved.</p>
	 * 
	 * @param id             the integer id of the post to soft-delete
	 * @param requestingUser the username requesting the delete (must match author)
	 * @return true if successfully soft-deleted, false otherwise
	 */
	public boolean softDeletePost(int id, String requestingUser) {
	    String query = "UPDATE forumPosts SET deleted = TRUE, "
	                 + "content = '[This post has been deleted by the author]' "
	                 + "WHERE id = ? AND author = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, id);
	        pstmt.setString(2, requestingUser);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}

	/*******
	 * <p> Method: searchPosts </p>
	 * 
	 * <p> Description: Searches non-deleted posts for keyword in title or content
	 * 		(case-insensitive).  If thread is null/blank all threads are searched.</p>
	 * 
	 * @param keyword the search term (case-insensitive); null or blank matches all
	 * @param thread  the thread to restrict to, or null/blank to search all threads
	 * @return a List of matching post display strings in "id | title" format
	 */
	public List<String> searchPosts(String keyword, String thread) {
	    List<String> list = new ArrayList<>();
	    String k = "%" + (keyword == null ? "" : keyword.toLowerCase()) + "%";
	    String query;

	    if (thread == null || thread.isBlank()) {
	        // Search all threads
	        query = "SELECT id, title FROM forumPosts "
	              + "WHERE deleted = FALSE "
	              + "AND (LOWER(title) LIKE ? OR LOWER(content) LIKE ?) "
	              + "ORDER BY timestamp DESC";
	    } else {
	        // Restrict to specified thread
	        query = "SELECT id, title FROM forumPosts "
	              + "WHERE deleted = FALSE AND thread = ? "
	              + "AND (LOWER(title) LIKE ? OR LOWER(content) LIKE ?) "
	              + "ORDER BY timestamp DESC";
	    }

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        if (thread == null || thread.isBlank()) {
	            pstmt.setString(1, k);
	            pstmt.setString(2, k);
	        } else {
	            pstmt.setString(1, thread);
	            pstmt.setString(2, k);
	            pstmt.setString(3, k);
	        }
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next())
	            list.add(rs.getInt("id") + " | " + rs.getString("title"));
	    } catch (SQLException e) { e.printStackTrace(); }
	    return list;
	}


	// ===================================================================
	// FORUM: REPLY CRUD
	// ===================================================================

	/*******
	 * <p> Method: createReply </p>
	 * 
	 * <p> Description: Creates a reply attached to the specified post.
	 * 		Content is required.</p>
	 * 
	 * @param postId  the id of the post being replied to
	 * @param author  the username of the reply author
	 * @param content the reply text (required, non-blank)
	 * @throws IllegalArgumentException if content is empty
	 */
	public void createReply(int postId, String author, String content) {
	    if (content == null || content.isBlank())
	        throw new IllegalArgumentException("Reply content cannot be empty.");
	    String query = "INSERT INTO forumReplies (postId, author, content) VALUES (?, ?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postId);
	        pstmt.setString(2, author);
	        pstmt.setString(3, content);
	        pstmt.executeUpdate();
	    } catch (SQLException e) { e.printStackTrace(); }
	}

	/*******
	 * <p> Method: getRepliesForPost </p>
	 * 
	 * <p> Description: Returns all replies for a given post as display strings,
	 * 		ordered oldest first.</p>
	 * 
	 * @param postId the id of the post whose replies to retrieve
	 * @return a List of reply display strings in "id | author [timestamp]: content" format
	 */
	public List<String> getRepliesForPost(int postId) {
	    List<String> list = new ArrayList<>();
	    String query = "SELECT id, author, content, timestamp FROM forumReplies "
	                 + "WHERE postId = ? ORDER BY timestamp";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postId);
	        ResultSet rs = pstmt.executeQuery();
	        while (rs.next()) {
	            list.add(rs.getInt("id") + " | "
	                   + rs.getString("author") + " ["
	                   + rs.getTimestamp("timestamp") + "]: "
	                   + rs.getString("content"));
	        }
	    } catch (SQLException e) { e.printStackTrace(); }
	    return list;
	}

	/*******
	 * <p> Method: updateReply </p>
	 * 
	 * <p> Description: Updates the content of a reply.  Only the original author
	 * 		may update their own reply.</p>
	 * 
	 * @param replyId        the id of the reply to update
	 * @param requestingUser the username requesting the update (must match author)
	 * @param newContent     replacement content (required, non-blank)
	 * @return true if updated, false if user is not the author or reply not found
	 * @throws IllegalArgumentException if newContent is empty
	 */
	public boolean updateReply(int replyId, String requestingUser, String newContent) {
	    if (newContent == null || newContent.isBlank())
	        throw new IllegalArgumentException("Reply content cannot be empty.");
	    String query = "UPDATE forumReplies SET content = ? WHERE id = ? AND author = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, newContent);
	        pstmt.setInt(2, replyId);
	        pstmt.setString(3, requestingUser);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}

	/*******
	 * <p> Method: deleteReply </p>
	 * 
	 * <p> Description: Permanently deletes a reply.  Only the original author may
	 * 		delete their own reply.</p>
	 * 
	 * @param replyId        the id of the reply to delete
	 * @param requestingUser the username requesting the delete (must match author)
	 * @return true if successfully deleted, false otherwise
	 */
	public boolean deleteReply(int replyId, String requestingUser) {
	    String query = "DELETE FROM forumReplies WHERE id = ? AND author = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, replyId);
	        pstmt.setString(2, requestingUser);
	        return pstmt.executeUpdate() > 0;
	    } catch (SQLException e) { e.printStackTrace(); return false; }
	}

	/*******
	 * <p> Method: countReplies </p>
	 * 
	 * <p> Description: Returns the total number of replies for a given post.</p>
	 * 
	 * @param postId the id of the post
	 * @return total reply count for that post
	 */
	public int countReplies(int postId) {
	    String query = "SELECT COUNT(*) AS cnt FROM forumReplies WHERE postId = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postId);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) return rs.getInt("cnt");
	    } catch (SQLException e) { e.printStackTrace(); }
	    return 0;
	}

	/*******
	 * <p> Method: countUnreadReplies </p>
	 * 
	 * <p> Description: Returns the number of replies for a given post that the
	 * 		specified user has not yet read.</p>
	 * 
	 * @param postId   the id of the post
	 * @param username the username to check unread replies for
	 * @return number of unread replies for that user on that post
	 */
	public int countUnreadReplies(int postId, String username) {
	    String query = "SELECT COUNT(*) AS cnt FROM forumReplies r "
	                 + "WHERE r.postId = ? AND NOT EXISTS ("
	                 + "  SELECT 1 FROM forumReplyReads rr "
	                 + "  WHERE rr.replyId = r.id AND rr.username = ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postId);
	        pstmt.setString(2, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) return rs.getInt("cnt");
	    } catch (SQLException e) { e.printStackTrace(); }
	    return 0;
	}


	// ===================================================================
	// FORUM: READ TRACKING
	// ===================================================================

	/*******
	 * <p> Method: markPostAsRead </p>
	 * 
	 * <p> Description: Records that the given user has read the given post.  Uses
	 * 		MERGE so calling this multiple times for the same user/post is safe.</p>
	 * 
	 * @param postId   the id of the post that was read
	 * @param username the username of the user who read it
	 */
	public void markPostAsRead(int postId, String username) {
	    String query = "MERGE INTO forumPostReads (postId, username) "
	                 + "KEY(postId, username) VALUES (?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postId);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	    } catch (SQLException e) { e.printStackTrace(); }
	}

	/*******
	 * <p> Method: markReplyAsRead </p>
	 * 
	 * <p> Description: Records that the given user has read the given reply.  Uses
	 * 		MERGE so calling this multiple times for the same user/reply is safe.</p>
	 * 
	 * @param replyId  the id of the reply that was read
	 * @param username the username of the user who read it
	 */
	public void markReplyAsRead(int replyId, String username) {
	    String query = "MERGE INTO forumReplyReads (replyId, username) "
	                 + "KEY(replyId, username) VALUES (?, ?)";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, replyId);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	    } catch (SQLException e) { e.printStackTrace(); }
	}

	/*******
	 * <p> Method: hasUserReadPost </p>
	 * 
	 * <p> Description: Returns whether the given user has already read the given post.</p>
	 * 
	 * @param postId   the id of the post to check
	 * @param username the username to check
	 * @return true if the user has read the post, false otherwise
	 */
	public boolean hasUserReadPost(int postId, String username) {
	    String query = "SELECT COUNT(*) AS cnt FROM forumPostReads "
	                 + "WHERE postId = ? AND username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setInt(1, postId);
	        pstmt.setString(2, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) return rs.getInt("cnt") > 0;
	    } catch (SQLException e) { e.printStackTrace(); }
	    return false;
	}


	// ===================================================================
	// ORIGINAL METHODS — UNCHANGED FROM HERE DOWN
	// ===================================================================

/*******
 * <p> Method: isDatabaseEmpty </p>
 * 
 * <p> Description: If the user database has no rows, true is returned, else false.</p>
 * 
 * @return true if the database is empty, else it returns false
 * 
 */
	public boolean isDatabaseEmpty() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count") == 0;
			}
		}  catch (SQLException e) {
	        return false;
	    }
		return true;
	}
	
	
/*******
 * <p> Method: getNumberOfUsers </p>
 * 
 * <p> Description: Returns an integer .of the number of users currently in the user database. </p>
 * 
 * @return the number of user records in the database.
 * 
 */
	public int getNumberOfUsers() {
		String query = "SELECT COUNT(*) AS count FROM userDB";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch (SQLException e) {
	        return 0;
	    }
		return 0;
	}
	
/*******
 * <p> Method: boolean deleteUser(String username) </p>
 * 
 * <p> Description: Deletes a user from the database given a username.</p>
 * 
 * @param username the username to delete
 * @return true if a user was deleted, false otherwise
 */
public boolean deleteUser(String username) {
    String query = "DELETE FROM userDB WHERE userName = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, username);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;   // true if a row was actually deleted
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

/*******
 * <p> Method: boolean deleteInvite(String email) </p>
 * 
 * <p> Description: Deletes an invite from the database given an email. See
 * the manageInvitations method in the ControllerAdminHome.java file </p>
 * 
 * @param email the invite to delete
 * @return true if a user was deleted, false otherwise
 */
public boolean deleteInvite(String email) {
    String query = "DELETE FROM InvitationCodes WHERE emailAddress = ?";
    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
        pstmt.setString(1, email);
        int rowsAffected = pstmt.executeUpdate();
        return rowsAffected > 0;   // true if a row was actually deleted
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}

/*******
 * <p> Method: register(User user) </p>
 * 
 * <p> Description: Creates a new row in the database using the user parameter. </p>
 * 
 * @throws SQLException when there is an issue creating the SQL command or executing it.
 * 
 * @param user specifies a user object to be added to the database.
 * 
 */
	public void register(User user) throws SQLException {
		String insertUser = "INSERT INTO userDB (userName, password, firstName, middleName, "
				+ "lastName, preferredFirstName, emailAddress, adminRole, newRole1, newRole2) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		try (PreparedStatement pstmt = connection.prepareStatement(insertUser)) {
			currentUsername = user.getUserName();
			pstmt.setString(1, currentUsername);
			
			currentPassword = user.getPassword();
			pstmt.setString(2, currentPassword);
			
			currentFirstName = user.getFirstName();
			pstmt.setString(3, currentFirstName);
			
			currentMiddleName = user.getMiddleName();			
			pstmt.setString(4, currentMiddleName);
			
			currentLastName = user.getLastName();
			pstmt.setString(5, currentLastName);
			
			currentPreferredFirstName = user.getPreferredFirstName();
			pstmt.setString(6, currentPreferredFirstName);
			
			currentEmailAddress = user.getEmailAddress();
			pstmt.setString(7, currentEmailAddress);
			
			currentAdminRole = user.getAdminRole();
			pstmt.setBoolean(8, currentAdminRole);
			
			currentNewRole1 = user.getNewRole1();
			pstmt.setBoolean(9, currentNewRole1);
			
			currentNewRole2 = user.getNewRole2();
			pstmt.setBoolean(10, currentNewRole2);
			
			pstmt.executeUpdate();
		}
		
	}

/*******
*  <p> ADDED Method: List getUser() </p>
*  
*  <P> Description: Get a user's details based on the username. </p>
*  
*  @return the User itself.
*/
	public User getUser(String username) {
	    String query = "SELECT * FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return new User(
	                rs.getString("userName"),
	                rs.getString("password"),
	                rs.getString("firstName"),
	                rs.getString("middleName"),
	                rs.getString("lastName"),
	                rs.getString("preferredFirstName"),
	                rs.getString("emailAddress"),
	                rs.getBoolean("adminRole"),
	                rs.getBoolean("newRole1"),
	                rs.getBoolean("newRole2")
	            );
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return null; 
	}

	
/*******
 *  <p> Method: List getUserList() </p>
 *  
 *  <P> Description: Generate a List of Strings, one for each user in the database,
 *  starting with "<Select User>" at the start of the list. </p>
 *  
 *  @return a list of userNames found in the database.
 */
	public List<String> getUserList () {
		List<String> userList = new ArrayList<String>();
		userList.add("<Select a User>");
		String query = "SELECT userName FROM userDB";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				userList.add(rs.getString("userName"));
			}
		} catch (SQLException e) {
	        return null;
	    }
//		System.out.println(userList);
		return userList;
	}
	
	/*******
	 * <p> ADDED Method: List<User> getAllUsers() </p>
	 * 
	 * <p> Description: Retrieve a list of all the users and their features (username, names, email address, and role(s) </p>
	 * 
	 * @return the list of users.
	 * 
	 */
	public List<User> getAllUsers() {
	    List<User> users = new ArrayList<>();

	    String query = "SELECT * FROM userDB";

	    try (PreparedStatement pstmt = connection.prepareStatement(query);
	         ResultSet rs = pstmt.executeQuery()) {

	        while (rs.next()) {
	            User user = new User(
	                rs.getString("userName"),
	                "", 
	                rs.getString("firstName"),
	                rs.getString("middleName"),
	                rs.getString("lastName"),
	                rs.getString("preferredFirstName"),
	                rs.getString("emailAddress"),
	                rs.getBoolean("adminRole"),
	                rs.getBoolean("newRole1"),
	                rs.getBoolean("newRole2")
	            );
	            users.add(user);
	        }

	    } catch (SQLException e) {
	        e.printStackTrace();
	    }

	    return users;
	}

/*******
 * <p> Method: boolean loginAdmin(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Admin role.
 * 
 * @return true if the specified user has been logged in as an Admin else false.
 * 
 */
	public boolean loginAdmin(User user){
		// Validates an admin user's login credentials so the user can login in as an Admin.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "adminRole = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();	// If a row is returned, rs.next() will return true		
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
/*******
 * <p> Method: boolean loginRole1(User user) </p>
 * 
 * <p> Description: Check to see that a user with the specified username, password, and role
 * 		is the same as a row in the table for the username, password, and role. </p>
 * 
 * @param user specifies the specific user that should be logged in playing the Student role.
 * 
 * @return true if the specified user has been logged in as an Student else false.
 * 
 */
	public boolean loginRole1(User user) {
		// Validates a student user's login credentials.
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "newRole1 = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}

	/*******
	 * <p> Method: boolean loginRole2(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username, password, and role
	 * 		is the same as a row in the table for the username, password, and role. </p>
	 * 
	 * @param user specifies the specific user that should be logged in playing the Reviewer role.
	 * 
	 * @return true if the specified user has been logged in as an Student else false.
	 * 
	 */
	// Validates a reviewer user's login credentials.
	public boolean loginRole2(User user) {
		String query = "SELECT * FROM userDB WHERE userName = ? AND password = ? AND "
				+ "newRole2 = TRUE";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, user.getUserName());
			pstmt.setString(2, user.getPassword());
			ResultSet rs = pstmt.executeQuery();
			return rs.next();
		} catch  (SQLException e) {
		       e.printStackTrace();
		}
		return false;
	}
	
	
	/*******
	 * <p> Method: boolean doesUserExist(User user) </p>
	 * 
	 * <p> Description: Check to see that a user with the specified username is  in the table. </p>
	 * 
	 * @param userName specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return true if the specified user is in the table else false.
	 * 
	 */
	// Checks if a user already exists in the database based on their userName.
	public boolean doesUserExist(String userName) {
	    String query = "SELECT COUNT(*) FROM userDB WHERE userName = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        
	        pstmt.setString(1, userName);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            // If the count is greater than 0, the user exists
	            return rs.getInt(1) > 0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return false; // If an error occurs, assume user doesn't exist
	}

	
	/*******
	 * <p> Method: int getNumberOfRoles(User user) </p>
	 * 
	 * <p> Description: Determine the number of roles a specified user plays. </p>
	 * 
	 * @param user specifies the specific user that we want to determine if it is in the table.
	 * 
	 * @return the number of roles this user plays (0 - 5).
	 * 
	 */	
	// Get the number of roles that this user plays
	public int getNumberOfRoles (User user) {
		int numberOfRoles = 0;
		if (user.getAdminRole()) numberOfRoles++;
		if (user.getNewRole1()) numberOfRoles++;
		if (user.getNewRole2()) numberOfRoles++;
		return numberOfRoles;
	}	

	
	/*******
	 * <p> Method: String generateInvitationCode(String emailAddress, String role) </p>
	 * 
	 * <p> Description: Given an email address and a roles, this method establishes and invitation
	 * code and adds a record to the InvitationCodes table.  When the invitation code is used, the
	 * stored email address is used to establish the new user and the record is removed from the
	 * table.</p>
	 * 
	 * @param emailAddress specifies the email address for this new user.
	 * 
	 * @param role specified the role that this new user will play.
	 * 
	 * @return the code of six characters so the new user can use it to securely setup an account.
	 * 
	 */
	// Generates a new invitation code and inserts it into the database.
	public String generateInvitationCode(String emailAddress, String role) {
	    String code = UUID.randomUUID().toString().substring(0, 6); // Generate a random 6-character code
	    String query = "INSERT INTO InvitationCodes (code, emailaddress, role) VALUES (?, ?, ?)";

	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        pstmt.setString(2, emailAddress);
	        pstmt.setString(3, role);
	        pstmt.executeUpdate();
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return code;
	}

	
	/*******
	 * <p> Method: int getNumberOfInvitations() </p>
	 * 
	 * <p> Description: Determine the number of outstanding invitations in the table.</p>
	 *  
	 * @return the number of invitations in the table.
	 * 
	 */
	// Number of invitations in the database
	public int getNumberOfInvitations() {
		String query = "SELECT COUNT(*) AS count FROM InvitationCodes";
		try {
			ResultSet resultSet = statement.executeQuery(query);
			if (resultSet.next()) {
				return resultSet.getInt("count");
			}
		} catch  (SQLException e) {
	        e.printStackTrace();
	    }
		return 0;
	}
	
	
	/*******
	 * <p> Method: boolean emailaddressHasBeenUsed(String emailAddress) </p>
	 * 
	 * <p> Description: Determine if an email address has been user to establish a user.</p>
	 * 
	 * @param emailAddress is a string that identifies a user in the table
	 *  
	 * @return true if the email address is in the table, else return false.
	 * 
	 */
	// Check to see if an email address is already in the database
	public boolean emailaddressHasBeenUsed(String emailAddress) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE emailAddress = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        ResultSet rs = pstmt.executeQuery();
	 //     System.out.println(rs);
	        if (rs.next()) {
	            // Mark the code as used
	        	return rs.getInt("count")>0;
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return false;
	}
	
	
	/*******
	 * <p> Method: String getRoleGivenAnInvitationCode(String code) </p>
	 * 
	 * <p> Description: Get the role associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the role for the code or an empty string.
	 * 
	 */
	// Obtain the roles associated with an invitation code.
	public String getRoleGivenAnInvitationCode(String code) {
	    String query = "SELECT * FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("role");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	    return "";
	}

	
	/*******
	 * <p> Method: String getEmailAddressUsingCode (String code ) </p>
	 * 
	 * <p> Description: Get the email addressed associated with an invitation code.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 * @return the email address for the code or an empty string.
	 * 
	 */
	// For a given invitation code, return the associated email address of an empty string
	public String getEmailAddressUsingCode (String code ) {
	    String query = "SELECT emailAddress FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	            return rs.getString("emailAddress");
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return "";
	}
	
	
	/*******
	 * <p> Method: void removeInvitationAfterUse(String code) </p>
	 * 
	 * <p> Description: Remove an invitation record once it is used.</p>
	 * 
	 * @param code is the 6 character String invitation code
	 *  
	 */
	// Remove an invitation using an email address once the user account has been setup
	public void removeInvitationAfterUse(String code) {
	    String query = "SELECT COUNT(*) AS count FROM InvitationCodes WHERE code = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, code);
	        ResultSet rs = pstmt.executeQuery();
	        if (rs.next()) {
	        	int counter = rs.getInt(1);
	            // Only do the remove if the code is still in the invitation table
	        	if (counter > 0) {
        			query = "DELETE FROM InvitationCodes WHERE code = ?";
	        		try (PreparedStatement pstmt2 = connection.prepareStatement(query)) {
	        			pstmt2.setString(1, code);
	        			pstmt2.executeUpdate();
	        		}catch (SQLException e) {
	        	        e.printStackTrace();
	        	    }
	        	}
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return;
	}
	
	/*******
	 * <p> ADDED Method: void updatePassword(String username, String password) </p>
	 * 
	 * <p> Description: Update the password of a user given that user's username and the new
	 *		password.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @param password is the new password for the user
	 *  
	 */
	// update the password
	public void updatePassword(String username, String password) {
	    String query = "UPDATE userDB SET password = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, password);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPassword = password;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getFirstName(String username) </p>
	 * 
	 * <p> Description: Get the first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the first name of a user given that user's username 
	 *  
	 */
	// Get the First Name
	public String getFirstName(String username) {
		String query = "SELECT firstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}


	/*******
	 * <p> Method: void updateFirstName(String username, String firstName) </p>
	 * 
	 * <p> Description: Update the first name of a user given that user's username and the new
	 *		first name.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @param firstName is the new first name for the user
	 *  
	 */
	// update the first name
	public void updateFirstName(String username, String firstName) {
	    String query = "UPDATE userDB SET firstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, firstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentFirstName = firstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}

	
	/*******
	 * <p> Method: String getMiddleName(String username) </p>
	 * 
	 * <p> Description: Get the middle name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the middle name of a user given that user's username 
	 *  
	 */
	// get the middle name
	public String getMiddleName(String username) {
		String query = "SELECT MiddleName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("middleName"); // Return the middle name if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}

	
	/*******
	 * <p> Method: void updateMiddleName(String username, String middleName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param middleName is the new middle name for the user
	 *  
	 */
	// update the middle name
	public void updateMiddleName(String username, String middleName) {
	    String query = "UPDATE userDB SET middleName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, middleName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentMiddleName = middleName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getLastName(String username) </p>
	 * 
	 * <p> Description: Get the last name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the last name of a user given that user's username 
	 *  
	 */
	// get he last name
	public String getLastName(String username) {
		String query = "SELECT LastName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("lastName"); // Return last name role if user exists
	        }
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateLastName(String username, String lastName) </p>
	 * 
	 * <p> Description: Update the middle name of a user given that user's username and the new
	 * 		middle name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param lastName is the new last name for the user
	 *  
	 */
	// update the last name
	public void updateLastName(String username, String lastName) {
	    String query = "UPDATE userDB SET lastName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, lastName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentLastName = lastName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getPreferredFirstName(String username) </p>
	 * 
	 * <p> Description: Get the preferred first name of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the preferred first name of a user given that user's username 
	 *  
	 */
	// get the preferred first name
	public String getPreferredFirstName(String username) {
		String query = "SELECT preferredFirstName FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("firstName"); // Return the preferred first name if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updatePreferredFirstName(String username, String preferredFirstName) </p>
	 * 
	 * <p> Description: Update the preferred first name of a user given that user's username and
	 * 		the new preferred first name.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param preferredFirstName is the new preferred first name for the user
	 *  
	 */
	// update the preferred first name of the user
	public void updatePreferredFirstName(String username, String preferredFirstName) {
	    String query = "UPDATE userDB SET preferredFirstName = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, preferredFirstName);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentPreferredFirstName = preferredFirstName;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: String getEmailAddress(String username) </p>
	 * 
	 * <p> Description: Get the email address of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return the email address of a user given that user's username 
	 *  
	 */
	// get the email address
	public String getEmailAddress(String username) {
		String query = "SELECT emailAddress FROM userDB WHERE userName = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();
	        
	        if (rs.next()) {
	            return rs.getString("emailAddress"); // Return the email address if user exists
	        }
			
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
		return null;
	}
	
	
	/*******
	 * <p> Method: void updateEmailAddress(String username, String emailAddress) </p>
	 * 
	 * <p> Description: Update the email address name of a user given that user's username and
	 * 		the new email address.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param emailAddress is the new preferred first name for the user
	 *  
	 */
	// update the email address
	public void updateEmailAddress(String username, String emailAddress) {
	    String query = "UPDATE userDB SET emailAddress = ? WHERE username = ?";
	    try (PreparedStatement pstmt = connection.prepareStatement(query)) {
	        pstmt.setString(1, emailAddress);
	        pstmt.setString(2, username);
	        pstmt.executeUpdate();
	        currentEmailAddress = emailAddress;
	    } catch (SQLException e) {
	        e.printStackTrace();
	    }
	}
	
	
	/*******
	 * <p> Method: boolean getUserAccountDetails(String username) </p>
	 * 
	 * <p> Description: Get all the attributes of a user given that user's username.</p>
	 * 
	 * @param username is the username of the user
	 * 
	 * @return true of the get is successful, else false
	 *  
	 */
	// get the attributes for a specified user
	public boolean getUserAccountDetails(String username) {
		String query = "SELECT * FROM userDB WHERE username = ?";
		try (PreparedStatement pstmt = connection.prepareStatement(query)) {
			pstmt.setString(1, username);
	        ResultSet rs = pstmt.executeQuery();			
			rs.next();
	    	currentUsername = rs.getString(2);
	    	currentPassword = rs.getString(3);
	    	currentFirstName = rs.getString(4);
	    	currentMiddleName = rs.getString(5);
	    	currentLastName = rs.getString(6);
	    	currentPreferredFirstName = rs.getString(7);
	    	currentEmailAddress = rs.getString(8);
	    	currentAdminRole = rs.getBoolean(9);
	    	currentNewRole1 = rs.getBoolean(10);
	    	currentNewRole2 = rs.getBoolean(11);
			return true;
	    } catch (SQLException e) {
			return false;
	    }
	}
	
	
	/*******
	 * <p> Method: boolean updateUserRole(String username, String role, String value) </p>
	 * 
	 * <p> Description: Update a specified role for a specified user's and set and update all the
	 * 		current user attributes.</p>
	 * 
	 * @param username is the username of the user
	 *  
	 * @param role is string that specifies the role to update
	 * 
	 * @param value is the string that specified TRUE or FALSE for the role
	 * 
	 * @return true if the update was successful, else false
	 *  
	 */
	// Update a users role
	public boolean updateUserRole(String username, String role, String value) {
		if (role.compareTo("Admin") == 0) {
			String query = "UPDATE userDB SET adminRole = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentAdminRole = true;
				else
					currentAdminRole = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Role1") == 0) {
			String query = "UPDATE userDB SET newRole1 = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentNewRole1 = true;
				else
					currentNewRole1 = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		if (role.compareTo("Role2") == 0) {
			String query = "UPDATE userDB SET newRole2 = ? WHERE username = ?";
			try (PreparedStatement pstmt = connection.prepareStatement(query)) {
				pstmt.setString(1, value);
				pstmt.setString(2, username);
				pstmt.executeUpdate();
				if (value.compareTo("true") == 0)
					currentNewRole2 = true;
				else
					currentNewRole2 = false;
				return true;
			} catch (SQLException e) {
				return false;
			}
		}
		return false;
	}
	
	
	// Attribute getters for the current user
	/*******
	 * <p> Method: String getCurrentUsername() </p>
	 * 
	 * <p> Description: Get the current user's username.</p>
	 * 
	 * @return the username value is returned
	 *  
	 */
	public String getCurrentUsername() { return currentUsername;};

	
	/*******
	 * <p> Method: String getCurrentPassword() </p>
	 * 
	 * <p> Description: Get the current user's password.</p>
	 * 
	 * @return the password value is returned
	 *  
	 */
	public String getCurrentPassword() { return currentPassword;};

	
	/*******
	 * <p> Method: String getCurrentFirstName() </p>
	 * 
	 * <p> Description: Get the current user's first name.</p>
	 * 
	 * @return the first name value is returned
	 *  
	 */
	public String getCurrentFirstName() { return currentFirstName;};

	
	/*******
	 * <p> Method: String getCurrentMiddleName() </p>
	 * 
	 * <p> Description: Get the current user's middle name.</p>
	 * 
	 * @return the middle name value is returned
	 *  
	 */
	public String getCurrentMiddleName() { return currentMiddleName;};

	
	/*******
	 * <p> Method: String getCurrentLastName() </p>
	 * 
	 * <p> Description: Get the current user's last name.</p>
	 * 
	 * @return the last name value is returned
	 *  
	 */
	public String getCurrentLastName() { return currentLastName;};

	
	/*******
	 * <p> Method: String getCurrentPreferredFirstName( </p>
	 * 
	 * <p> Description: Get the current user's preferred first name.</p>
	 * 
	 * @return the preferred first name value is returned
	 *  
	 */
	public String getCurrentPreferredFirstName() { return currentPreferredFirstName;};

	
	/*******
	 * <p> Method: String getCurrentEmailAddress() </p>
	 * 
	 * <p> Description: Get the current user's email address name.</p>
	 * 
	 * @return the email address value is returned
	 *  
	 */
	public String getCurrentEmailAddress() { return currentEmailAddress;};

	
	/*******
	 * <p> Method: boolean getCurrentAdminRole() </p>
	 * 
	 * <p> Description: Get the current user's Admin role attribute.</p>
	 * 
	 * @return true if this user plays an Admin role, else false
	 *  
	 */
	public boolean getCurrentAdminRole() { return currentAdminRole;};

	
	/*******
	 * <p> Method: boolean getCurrentNewRole1() </p>
	 * 
	 * <p> Description: Get the current user's Student role attribute.</p>
	 * 
	 * @return true if this user plays a Student role, else false
	 *  
	 */
	public boolean getCurrentNewRole1() { return currentNewRole1;};

	
	/*******
	 * <p> Method: boolean getCurrentNewRole2() </p>
	 * 
	 * <p> Description: Get the current user's Reviewer role attribute.</p>
	 * 
	 * @return true if this user plays a Reviewer role, else false
	 *  
	 */
	public boolean getCurrentNewRole2() { return currentNewRole2;};

	
	/*******
	 * <p> Debugging method</p>
	 * 
	 * <p> Description: Debugging method that dumps the database of the console.</p>
	 * 
	 * @throws SQLException if there is an issues accessing the database.
	 * 
	 */
	// Dumps the database.
	public void dump() throws SQLException {
		String query = "SELECT * FROM userDB";
		ResultSet resultSet = statement.executeQuery(query);
		ResultSetMetaData meta = resultSet.getMetaData();
		while (resultSet.next()) {
		for (int i = 0; i < meta.getColumnCount(); i++) {
		System.out.println(
		meta.getColumnLabel(i + 1) + ": " +
				resultSet.getString(i + 1));
		}
		System.out.println();
		}
		resultSet.close();
	}


	/*******
	 * <p> Method: void closeConnection()</p>
	 * 
	 * <p> Description: Closes the database statement and connection.</p>
	 * 
	 */
	// Closes the database statement and connection.
	public void closeConnection() {
		try{ 
			if(statement!=null) statement.close(); 
		} catch(SQLException se2) { 
			se2.printStackTrace();
		} 
		try { 
			if(connection!=null) connection.close(); 
		} catch(SQLException se){ 
			se.printStackTrace(); 
		} 
	}
}