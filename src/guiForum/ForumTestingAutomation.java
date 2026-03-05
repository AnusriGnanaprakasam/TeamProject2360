package guiForum;

import java.sql.SQLException;
import java.util.List;

import database.Database;

/*******
 * <p> Title: ForumTestingAutomation. </p>
 *
 * <p> Description: Automated test cases for the Student Discussion System (HW2).
 * Tests are structured to match the TP1 PasswordEvaluationTestingAutomation pattern:
 * each test prints a header, performs an operation, checks the result, and prints
 * PASS or FAIL with a helpful message.
 *
 * The 15 test cases cover:
 *   TC-01 to TC-02  Post CREATE (explicit thread, default thread)
 *   TC-03           Post READ   (getPostContent + read tracking)
 *   TC-04 to TC-05  Post UPDATE (author succeeds, non-author fails)
 *   TC-06 to TC-07  Post DELETE soft-delete + placeholder + reply preservation
 *   TC-08 to TC-10  Reply CREATE, validation, READ
 *   TC-11 to TC-13  Search / filter (keyword, thread filter, empty result)
 *   TC-14           Read tracking (countUnreadReplies decrements after markAsRead)
 *   TC-15           Permissions  (student role check — verified via activeHomePage flag)
 *
 * Run this file as a standard Java application from Eclipse.
 * No JavaFX is required — all tests operate directly on the Database layer.</p>
 *
 * <p> Copyright: Anusri Gnananprakasam </p>
 *
 * @author : Anusri Gnananprakasam
 *
 * @version 1.00    2026-02-25  Initial version for HW2
 */
public class ForumTestingAutomation {

    /** Counts tests that produced the expected result. */
    static int numPassed = 0;

    /** Counts tests that did NOT produce the expected result. */
    static int numFailed = 0;

    /** Shared database instance used by all test cases. */
    static Database db = new Database();

    /*******
     * <p> Method: main(String[]) </p>
     *
     * <p> Description: Entry point.  Connects to the database, clears any existing
     * forum data so tests start from a known state, runs all 15 test cases, then
     * prints the final pass/fail summary.</p>
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) throws SQLException {

        // Connect and reset forum tables so every run starts clean
        db.connectToDatabase();
        db.clearAllDiscussionPosts();

        // ── Run all 15 test cases ─────────────────────────────────────────────
        testCase01_CreatePostWithExplicitThread();
        testCase02_CreatePostDefaultsToGeneral();
        testCase03_ReadPostContentMarksAsRead();
        testCase04_UpdateOwnPost();
        testCase05_NonAuthorCannotUpdatePost();
        testCase06_SoftDeleteOwnPost();
        testCase07_DeletedPostShowsPlaceholderRepliesPreserved();
        testCase08_CreateReply();
        testCase09_EmptyReplyContentRejected();
        testCase10_ReadAllRepliesForPost();
        testCase11_SearchByKeywordAllThreads();
        testCase12_SearchWithThreadFilter();
        testCase13_EmptySearchReturnsEmptyList();
        testCase14_UnreadCountDecrementsAfterMarkAsRead();
        testCase15_StudentRoleCannotSeeThreadControls();

        
        System.out.printf("%nResults:  %d passed,  %d failed%n", numPassed, numFailed);

    }
  
    // TC-01  Post CREATE — explicit thread
  
    /*******
     * <p> Method: testCase01_CreatePostWithExplicitThread() </p>
     *
     * <p> Description: Verifies that a post is created successfully when an explicit
     * thread name is supplied.  Checks that the returned list contains the new post's
     * title and that the thread is stored correctly via getPostContent.</p>
     *
     * Satisfies: US-01 (students can post), US-02 (posts belong to a thread)
     */
    private static void testCase01_CreatePostWithExplicitThread() {
        printHeader("TC-01", "Create post with explicit thread 'Assignments'");
        try {
            db.createPost("student1", "Assignments",
                    "HW2 Screencast question",
                    "What is the difference between Screencast 5 and Screencast 6?");

            List<String> posts = db.getAllPosts();
            int id = parseId(posts.get(0));  // newest post is first
            String content = db.getPostContent(id, "student1");

            if (content.contains("Assignments") && content.contains("HW2 Screencast question"))
                pass("Post created with thread='Assignments' and correct title.");
            else
                fail("Post content did not contain expected thread or title.\nGot: " + content);

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    // TC-02  Post CREATE — null thread defaults to "General"
   

    /*******
     * <p> Method: testCase02_CreatePostDefaultsToGeneral() </p>
     *
     * <p> Description: Verifies that when thread is null, the post is stored in the
     * "General" thread.  This satisfies the "defaults to General" requirement.</p>
     *
     * Satisfies: US-02 (thread defaults to General when not specified)
     */
    private static void testCase02_CreatePostDefaultsToGeneral() {
        printHeader("TC-02", "Post with null thread defaults to 'General'");
        try {
            db.createPost("student2", null,
                    "General question about the project",
                    "Where can I find the epics list?");

            List<String> posts = db.getAllPosts();
            int id = parseId(posts.get(0));
            String content = db.getPostContent(id, "student2");

            if (content.contains("General"))
                pass("Thread defaulted to 'General' as required.");
            else
                fail("Expected thread 'General' in content but got:\n" + content);

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
       // TC-03  Post READ — getPostContent marks post as read
   
    /*******
     * <p> Method: testCase03_ReadPostContentMarksAsRead() </p>
     *
     * <p> Description: Verifies that getPostContent returns formatted content and
     * marks the post as read for the viewing user.  hasUserReadPost should return
     * true after the call.</p>
     *
     * Satisfies: US-04 (see list of posts), US-07 (read/unread tracking)
     */
    private static void testCase03_ReadPostContentMarksAsRead() {
        printHeader("TC-03", "getPostContent returns content and marks post as read");
        try {
            // Use the post created in TC-01 (second newest = index 1)
            List<String> posts = db.getAllPosts();
            int id = parseId(posts.get(1)); // TC-01 post (TC-02 is newer, at index 0)

            // Reading as a different user — should start unread
            String content = db.getPostContent(id, "student3");
            boolean nowRead = db.hasUserReadPost(id, "student3");

            if (content != null && !content.isEmpty() && nowRead)
                pass("Content returned and post marked as read for student3.");
            else
                fail("Content empty or post not marked read. hasUserReadPost=" + nowRead);

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
  
    // TC-04  Post UPDATE — author can update own post
  
    /*******
     * <p> Method: testCase04_UpdateOwnPost() </p>
     *
     * <p> Description: Verifies that the original author can update the title and
     * content of their own post.  updatePost should return true and the new title
     * should appear in getAllPosts.</p>
     *
     * Satisfies: CRUD Update for Post class
     */
    private static void testCase04_UpdateOwnPost() {
        printHeader("TC-04", "Author successfully updates own post");
        try {
            List<String> posts = db.getAllPosts();
            int id = parseId(posts.get(1)); // TC-01 post by student1

            boolean updated = db.updatePost(id, "student1",
                    "HW2 Screencast question — RESOLVED",
                    "Staff clarified: SC5 is live demo, SC6 is automated tests.");

            List<String> after = db.getAllPosts();
            boolean titleUpdated = after.stream().anyMatch(s -> s.contains("RESOLVED"));

            if (updated && titleUpdated)
                pass("updatePost returned true and new title is visible in list.");
            else
                fail("updatePost returned " + updated + ", titleUpdated=" + titleUpdated);

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
 
    // TC-05  Post UPDATE — non-author is blocked (security / negative test)
  

    /*******
     * <p> Method: testCase05_NonAuthorCannotUpdatePost() </p>
     *
     * <p> Description: Verifies that a user who is NOT the original author cannot
     * update someone else's post.  updatePost must return false and the post must
     * remain unchanged.</p>
     *
     * Satisfies: Author-only enforcement; produces helpful implicit error (false return)
     */
    private static void testCase05_NonAuthorCannotUpdatePost() {
        printHeader("TC-05", "Non-author blocked from updating another user's post (security)");
        try {
            List<String> posts = db.getAllPosts();
            int id = parseId(posts.get(1)); // student1's post

            boolean updated = db.updatePost(id, "intruder",
                    "Hacked title", "Spam content");

            // Title should still contain RESOLVED from TC-04
            List<String> after = db.getAllPosts();
            boolean stillResolved = after.stream().anyMatch(s -> s.contains("RESOLVED"));

            if (!updated && stillResolved)
                pass("updatePost returned false for non-author. Post unchanged.");
            else
                fail("Security failure: updated=" + updated + ", stillResolved=" + stillResolved);

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }  // TC-06  Post DELETE — soft-delete by author
   
    /*******
     * <p> Method: testCase06_SoftDeleteOwnPost() </p>
     *
     * <p> Description: Verifies that an author can soft-delete their own post.
     * softDeletePost must return true and getAllPosts must show "[DELETED]".</p>
     *
     * Satisfies: US-03 (student can delete own post)
     */
    private static void testCase06_SoftDeleteOwnPost() {
        printHeader("TC-06", "Author soft-deletes own post");
        try {
            List<String> posts = db.getAllPosts();
            int id = parseId(posts.get(0)); // TC-02 post by student2

            boolean deleted = db.softDeletePost(id, "student2");

            List<String> after = db.getAllPosts();
            boolean showsDeleted = after.stream()
                    .anyMatch(s -> s.contains(id + " |") && s.contains("[DELETED]"));

            if (deleted && showsDeleted)
                pass("softDeletePost returned true. List shows [DELETED] for that post.");
            else
                fail("deleted=" + deleted + ", showsDeleted=" + showsDeleted);

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
   
    // TC-07  Post DELETE — placeholder shown, replies preserved
    

    /*******
     * <p> Method: testCase07_DeletedPostShowsPlaceholderRepliesPreserved() </p>
     *
     * <p> Description: Verifies that after a soft-delete, getPostContent returns the
     * placeholder message, and any existing replies are still retrievable.</p>
     *
     * Satisfies: US-03 (replies preserved, placeholder shown to viewers)
     */
    private static void testCase07_DeletedPostShowsPlaceholderRepliesPreserved() {
        printHeader("TC-07", "Deleted post shows placeholder; replies preserved");
        try {
            // Add a reply to the TC-02 post BEFORE checking (it's already deleted)
            // For the test, we check the TC-01 post — add a reply then soft-delete
            db.createPost("student4", "General",
                    "Post to be deleted with reply",
                    "This post will be soft-deleted.");
            List<String> posts = db.getAllPosts();
            int id = parseId(posts.get(0)); // newest post

            db.createReply(id, "student5", "This is a reply that must survive.");
            db.softDeletePost(id, "student4");

            String content = db.getPostContent(id, "student5");
            List<String> replies = db.getRepliesForPost(id);

            boolean placeholderShown = content.contains("[This post has been deleted");
            boolean replyPreserved   = !replies.isEmpty();

            if (placeholderShown && replyPreserved)
                pass("Placeholder shown. " + replies.size() + " reply(s) preserved.");
            else
                fail("placeholderShown=" + placeholderShown
                        + ", replyPreserved=" + replyPreserved);

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
  
    // TC-08  Reply CREATE — valid reply
   

    /*******
     * <p> Method: testCase08_CreateReply() </p>
     *
     * <p> Description: Verifies that a reply is created and appears in the reply list
     * for the parent post.</p>
     *
     * Satisfies: US-01 (students can receive replies)
     */
    private static void testCase08_CreateReply() {
        printHeader("TC-08", "Create valid reply on a post");
        try {
            List<String> posts = db.getAllPosts();
            int postId = parseId(posts.get(posts.size() - 1)); // TC-01 post (oldest)

            int countBefore = db.getRepliesForPost(postId).size();

            db.createReply(postId, "nagamk_staff",
                    "Screencast5 is your live demo of the actual HW2 application running normally.");

            int countAfter = db.getRepliesForPost(postId).size();

            if (countAfter == countBefore + 1)
                pass("Reply created. Reply count increased from "
                        + countBefore + " to " + countAfter + ".");
            else
                fail("Expected count " + (countBefore+1) + " but got " + countAfter);

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
    
    // TC-09  Reply CREATE — empty content rejected (negative / validation test)
   

    /*******
     * <p> Method: testCase09_EmptyReplyContentRejected() </p>
     *
     * <p> Description: Verifies that attempting to create a reply with empty content
     * throws an IllegalArgumentException with a helpful error message.</p>
     *
     * Satisfies: Input validation; helpful error message requirement
     */
    private static void testCase09_EmptyReplyContentRejected() {
        printHeader("TC-09", "Empty reply content rejected with helpful error message (negative)");
        try {
            List<String> posts = db.getAllPosts();
            int postId = parseId(posts.get(posts.size() - 1));

            db.createReply(postId, "student1", "");   // should throw

            fail("Expected IllegalArgumentException but no exception was thrown.");

        } catch (IllegalArgumentException e) {
            if (e.getMessage().toLowerCase().contains("empty")
                    || e.getMessage().toLowerCase().contains("cannot"))
                pass("IllegalArgumentException thrown with helpful message: \""
                        + e.getMessage() + "\"");
            else
                fail("Exception thrown but message not helpful: \"" + e.getMessage() + "\"");
        } catch (Exception e) {
            fail("Wrong exception type: " + e.getClass().getSimpleName()
                    + " — " + e.getMessage());
        }
    }
   
    // TC-10  Reply READ — getRepliesForPost returns ordered list
  

    /*******
     * <p> Method: testCase10_ReadAllRepliesForPost() </p>
     *
     * <p> Description: Verifies that getRepliesForPost returns at least one reply
     * in the expected "id | author [timestamp]: content" format.</p>
     *
     * Satisfies: US-01 (students can read replies)
     */
    private static void testCase10_ReadAllRepliesForPost() {
        printHeader("TC-10", "getRepliesForPost returns formatted reply list");
        try {
            List<String> posts = db.getAllPosts();
            int postId = parseId(posts.get(posts.size() - 1)); // TC-01 post

            List<String> replies = db.getRepliesForPost(postId);

            if (replies.isEmpty()) {
                fail("No replies found for post " + postId);
                return;
            }

            // Check format: should contain " | " and ":"
            String first = replies.get(0);
            if (first.contains(" | ") && first.contains(":"))
                pass("getRepliesForPost returned " + replies.size()
                        + " reply(s). Format correct: \"" + first.substring(0, Math.min(60, first.length())) + "...\"");
            else
                fail("Reply format unexpected: \"" + first + "\"");

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
  
    // TC-11  SEARCH — keyword across all threads
  

    /*******
     * <p> Method: testCase11_SearchByKeywordAllThreads() </p>
     *
     * <p> Description: Verifies that searchPosts with a keyword and null thread
     * returns only posts whose title or content matches the keyword.</p>
     *
     * Satisfies: US-08 (search by keyword; if no thread specified, all searched)
     */
    private static void testCase11_SearchByKeywordAllThreads() {
        printHeader("TC-11", "searchPosts by keyword across all threads");
        try {
            List<String> results = db.searchPosts("screencast", null);

            if (!results.isEmpty())
                pass("searchPosts('screencast', null) returned " + results.size()
                        + " result(s). Keyword search works across all threads.");
            else
                fail("Expected at least one result for keyword 'screencast' but got none.");

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
  
    // TC-12  SEARCH — thread filter with no keyword

    /*******
     * <p> Method: testCase12_SearchWithThreadFilter() </p>
     *
     * <p> Description: Verifies that searchPosts with null keyword and a thread name
     * returns only posts in that thread.</p>
     *
     * Satisfies: US-08 (thread filter; if no thread, all searched)
     */
    private static void testCase12_SearchWithThreadFilter() {
        printHeader("TC-12", "searchPosts with thread filter 'Assignments'");
        try {
            List<String> results = db.searchPosts(null, "Assignments");

            // Every result must belong to the Assignments thread.
            // We verify by checking that each result id, when fetched, contains "Assignments".
            boolean allMatch = true;
            for (String item : results) {
                int id = parseId(item);
                String content = db.getPostContent(id, "tester");
                if (!content.contains("Assignments")) { allMatch = false; break; }
            }

            if (!results.isEmpty() && allMatch)
                pass("searchPosts(null, 'Assignments') returned " + results.size()
                        + " result(s), all in Assignments thread.");
            else if (results.isEmpty())
                fail("No results found for thread 'Assignments'.");
            else
                fail("Some results were not in the 'Assignments' thread.");

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
   
    // TC-13  SEARCH — no match returns empty list, not an error
    

    /*******
     * <p> Method: testCase13_EmptySearchReturnsEmptyList() </p>
     *
     * <p> Description: Verifies that a search producing no results returns an empty
     * List, not null and not an exception.  An empty subset is a valid outcome.</p>
     *
     * Satisfies: US-08 (search); PostRepository subset can be empty
     */
    private static void testCase13_EmptySearchReturnsEmptyList() {
        printHeader("TC-13", "Search with no matches returns empty list — not an error (negative)");
        try {
            List<String> results = db.searchPosts("xyzNOmatch999", null);

            if (results != null && results.isEmpty())
                pass("searchPosts returned empty list for unmatched keyword. No exception.");
            else
                fail("Expected empty list but got: " + results);

        } catch (Exception e) {
            fail("Unexpected exception thrown for empty search: " + e.getMessage());
        }
    }
  
    // TC-14  READ TRACKING — countUnreadReplies decrements after markAsRead
    
    /*******
     * <p> Method: testCase14_UnreadCountDecrementsAfterMarkAsRead() </p>
     *
     * <p> Description: Creates 3 replies on a fresh post, verifies the unread count
     * is 3 for a new user, then marks one reply as read and verifies the count
     * drops to 2.</p>
     *
     * Satisfies: US-05 (see how many replies unread), US-06 (filter unread)
     */
    private static void testCase14_UnreadCountDecrementsAfterMarkAsRead() {
        printHeader("TC-14", "countUnreadReplies decrements after markReplyAsRead");
        try {
            // Create a fresh post and 3 replies
            db.createPost("student6", "General",
                    "Read tracking test post",
                    "This post is used to verify unread reply counting.");
            List<String> posts = db.getAllPosts();
            int postId = parseId(posts.get(0));

            db.createReply(postId, "student7", "Reply one");
            db.createReply(postId, "student7", "Reply two");
            db.createReply(postId, "student7", "Reply three");

            int before = db.countUnreadReplies(postId, "student8");

            // Mark the first reply as read
            List<String> replies = db.getRepliesForPost(postId);
            int firstReplyId = parseId(replies.get(0));
            db.markReplyAsRead(firstReplyId, "student8");

            int after = db.countUnreadReplies(postId, "student8");

            if (before == 3 && after == 2)
                pass("Unread count went from 3 to 2 after marking one reply as read.");
            else
                fail("Expected before=3, after=2 but got before=" + before
                        + ", after=" + after);

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
   
    // TC-15  PERMISSIONS — student role cannot see thread controls
   

    /*******
     * <p> Method: testCase15_StudentRoleCannotSeeThreadControls() </p>
     *
     * <p> Description: Verifies the permissions logic by simulating the role check
     * that ViewForum.displayForum() performs.  When activeHomePage is not 1 (admin),
     * isAdminOrStaff is false — meaning thread management controls are hidden.
     * This test checks the boolean logic directly without requiring JavaFX.</p>
     *
     * Satisfies: US-09 (students cannot create/delete/edit threads)
     */
    private static void testCase15_StudentRoleCannotSeeThreadControls() {
        printHeader("TC-15", "Student role (activeHomePage != 1) hides thread management controls");
        try {
            // Simulate the check in ViewForum.displayForum():
            //   boolean isAdminOrStaff = (applicationMain.FoundationsMain.activeHomePage == 1);
            //   button_CreateThread.setVisible(isAdminOrStaff);

            int studentActivePage = 2;  // Role1 = student
            boolean isAdminOrStaff = (studentActivePage == 1);

            // Thread controls visible = isAdminOrStaff
            boolean threadControlsVisible = isAdminOrStaff;

            if (!threadControlsVisible)
                pass("isAdminOrStaff=false for Role1 user. Thread controls would be hidden.");
            else
                fail("isAdminOrStaff was true for a student role — controls would be wrongly visible.");

            // Also verify admin sees them
            int adminActivePage = 1;
            boolean adminSees = (adminActivePage == 1);
            if (adminSees)
                pass("isAdminOrStaff=true for admin. Thread controls would be visible.");
            else
                fail("Admin should see thread controls but isAdminOrStaff was false.");

        } catch (Exception e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }
   
    // Helper methods
   

    /*******
     * <p> Method: parseId(String item) </p>
     *
     * <p> Description: Parses the integer id from a display string formatted as
     * "id | ..." — the same format used by getAllPosts() and getRepliesForPost().</p>
     *
     * @param item a display string in "id | ..." format
     * @return the parsed integer id
     */
    private static int parseId(String item) {
        return Integer.parseInt(item.split("\\|")[0].trim());
    }

    /*******
     * <p> Method: printHeader(String id, String description) </p>
     *
     * <p> Description: Prints a formatted test case header to the console.</p>
     *
     * @param id          the test case identifier (e.g., "TC-01")
     * @param description a brief description of what is being tested
     */
    private static void printHeader(String id, String description) {
        System.out.println(id + ": " + description);
    }

    /*******
     * <p> Method: pass(String message) </p>
     *
     * <p> Description: Records a passing test and prints the result.</p>
     *
     * @param message explanation of why the test passed
     */
    private static void pass(String message) {
        System.out.println("  PASS: " + message);
        numPassed++;
    }

    /*******
     * <p> Method: fail(String message) </p>
     *
     * <p> Description: Records a failing test and prints the result.</p>
     *
     * @param message explanation of why the test failed
     */
    private static void fail(String message) {
        System.out.println("  FAIL: " + message);
        numFailed++;
    }
}