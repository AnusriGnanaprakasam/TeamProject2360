package entityClasses;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*******
 * <p> Title: ReplyRepository Class. </p>
 *
 * <p> Description: In-memory repository that stores all Reply objects across all posts
 * as well as any arbitrary subset of those replies (e.g., the unread replies for a
 * specific post and user).
 *
 * The class acts as the bridge between the database layer (Database.java) and the
 * controller layer (ControllerForum.java).  Read-tracking (which replies a given user
 * has already seen) is maintained in the readReceipts set so the GUI can instantly
 * compute unread counts and filter to unread-only without a database round-trip.
 *
 * The subset is always a filtered view of allReplies and can be empty, contain one
 * element, or contain an arbitrarily large number of elements — there is no fixed
 * upper limit.</p>
 *
 *  <p> Copyright: Anusri Gnanaprakasam © 2026 </p>
 *
 * @author Anusri Gnanaprakasam
 *
 *
 * @version 1.00   2026-02-25  Initial version for HW2
 */
public class ReplyRepository {

    /*-*************************************************************************************
    Attributes
    ***************************************************************************************/

    /** Master list of all Reply objects currently known to the application. */
    private List<Reply> allReplies;

    /** Current working subset — may be empty, may equal allReplies for a given post,
     *  or may be any filtered view.  Set by filter operations; cleared by
     *  clearSubset(). */
    private List<Reply> currentSubset;

    /** Tracks which (replyId, username) pairs have been read.
     *  Stored as "replyId:username" strings for fast lookup.
     *  Mirrors the forumReplyReads table in the database. */
    private Set<String> readReceipts;

    /*-*************************************************************************************
    Constructor
    ***************************************************************************************/

    /*******
     * <p> Method: ReplyRepository() </p>
     *
     * <p> Description: Creates an empty repository.  All three collections start empty.
     * The controller populates allReplies by calling addReply() once for each reply
     * loaded from the database at startup.</p>
     */
    public ReplyRepository() {
        allReplies    = new ArrayList<>();
        currentSubset = new ArrayList<>();
        readReceipts  = new HashSet<>();
    }

    /*-*************************************************************************************
    CREATE — add a reply to the repository
    ***************************************************************************************/

    /*******
     * <p> Method: addReply(Reply reply) </p>
     *
     * <p> Description: Adds a Reply to the master list.  Called by the controller after
     * a successful database insert.  The Reply's id must have been set by setId()
     * before this is called.</p>
     *
     * @param reply the Reply to add (must not be null; id must be set)
     */
    public void addReply(Reply reply) {
        if (reply == null) return;
        allReplies.add(reply);
    }

    /*-*************************************************************************************
    READ — retrieve replies from the repository
    ***************************************************************************************/

    /*******
     * <p> Method: getReply(int id) </p>
     *
     * <p> Description: Finds and returns the Reply with the given id from the master
     * list.  Returns null if no matching reply is found.</p>
     *
     * @param id the database-assigned integer primary key
     * @return the matching Reply, or null if not found
     */
    public Reply getReply(int id) {
        for (Reply r : allReplies)
            if (r.getId() == id) return r;
        return null;
    }

    /*******
     * <p> Method: getRepliesForPost(int postId) </p>
     *
     * <p> Description: Returns a list of all replies attached to the specified post,
     * in chronological (insertion) order.  This list represents ALL replies regardless
     * of read status.  The list may be empty if the post has no replies.</p>
     *
     * @param postId the id of the parent Post
     * @return ordered List of Reply objects for that post (may be empty)
     */
    public List<Reply> getRepliesForPost(int postId) {
        return allReplies.stream()
            .filter(r -> r.getPostId() == postId)
            .collect(Collectors.toList());
    }

    /*******
     * <p> Method: getSubset() </p>
     *
     * <p> Description: Returns the current working subset.  The subset is populated
     * by filterUnread() or any other filter method.  It is cleared by clearSubset().
     * The subset may be empty, contain one element, or be arbitrarily large.</p>
     *
     * @return the current subset List (may be empty)
     */
    public List<Reply> getSubset() {
        return new ArrayList<>(currentSubset);
    }

    /*-*************************************************************************************
    UPDATE — modify an existing reply in the repository
    ***************************************************************************************/

    /*******
     * <p> Method: updateReply(int id, String requestingUser, String newContent) </p>
     *
     * <p> Description: Updates the content of the reply with the given id.  Returns
     * false if the reply is not found or if the requestingUser is not the original
     * author.  The in-memory Reply object is updated; the caller is responsible for
     * also calling Database.updateReply() to persist the change.</p>
     *
     * @param id             database id of the reply to update
     * @param requestingUser must match the reply's author
     * @param newContent     replacement content (non-blank)
     * @return true if the update succeeded, false otherwise
     */
    public boolean updateReply(int id, String requestingUser, String newContent) {
        Reply r = getReply(id);
        if (r == null) return false;
        if (!r.getAuthor().equals(requestingUser)) return false;
        r.setContent(newContent);
        return true;
    }

    /*-*************************************************************************************
    DELETE — permanently remove a reply from the repository
    ***************************************************************************************/

    /*******
     * <p> Method: deleteReply(int id, String requestingUser) </p>
     *
     * <p> Description: Permanently removes the reply with the given id from the master
     * list.  Returns false if the reply is not found or if the requestingUser is not
     * the original author.  Also removes any read receipts for this reply.  The caller
     * is responsible for also calling Database.deleteReply() to persist the removal.</p>
     *
     * @param id             database id of the reply to delete
     * @param requestingUser must match the reply's author
     * @return true if the deletion succeeded, false otherwise
     */
    public boolean deleteReply(int id, String requestingUser) {
        Reply r = getReply(id);
        if (r == null) return false;
        if (!r.getAuthor().equals(requestingUser)) return false;
        allReplies.remove(r);
        // Remove all read receipts for this reply so counters stay accurate
        readReceipts.removeIf(key -> key.startsWith(id + ":"));
        return true;
    }

    /*-*************************************************************************************
    READ TRACKING
    ***************************************************************************************/

    /*******
     * <p> Method: markAsRead(int replyId, String username) </p>
     *
     * <p> Description: Records that the given user has read the given reply.  Calling
     * this multiple times for the same pair is safe (idempotent).  The caller is
     * responsible for also calling Database.markReplyAsRead() to persist the record.</p>
     *
     * @param replyId  the id of the reply that was read
     * @param username the username of the user who read it
     */
    public void markAsRead(int replyId, String username) {
        readReceipts.add(replyId + ":" + username);
    }

    /*******
     * <p> Method: isRead(int replyId, String username) </p>
     *
     * <p> Description: Returns true if the given user has already read the given
     * reply, based on the in-memory read-receipt set.</p>
     *
     * @param replyId  the id of the reply to check
     * @param username the username to check
     * @return true if the user has read this reply
     */
    public boolean isRead(int replyId, String username) {
        return readReceipts.contains(replyId + ":" + username);
    }

    /*-*************************************************************************************
    SUBSET / FILTER operations
    ***************************************************************************************/

    /*******
     * <p> Method: filterUnread(int postId, String username) </p>
     *
     * <p> Description: Builds and stores a subset of replies for the given post that
     * the specified user has NOT yet read.  The subset replaces the previous
     * currentSubset.  The subset may be empty (all replies have been read), may contain
     * one element, or may be arbitrarily large.</p>
     *
     * @param postId   id of the parent Post
     * @param username username of the user whose unread replies are returned
     * @return the resulting unread subset List (also stored as currentSubset)
     */
    public List<Reply> filterUnread(int postId, String username) {
        currentSubset = allReplies.stream()
            .filter(r -> r.getPostId() == postId)
            .filter(r -> !isRead(r.getId(), username))
            .collect(Collectors.toList());
        return new ArrayList<>(currentSubset);
    }

    /*******
     * <p> Method: countUnread(int postId, String username) </p>
     *
     * <p> Description: Returns the number of replies for the given post that the
     * specified user has not yet read, using the in-memory read-receipt set.  This
     * is used to populate the "Unread: N" label in the post list.</p>
     *
     * @param postId   id of the parent Post
     * @param username username to check
     * @return count of unread replies for that user on that post
     */
    public int countUnread(int postId, String username) {
        return (int) allReplies.stream()
            .filter(r -> r.getPostId() == postId)
            .filter(r -> !isRead(r.getId(), username))
            .count();
    }

    /*******
     * <p> Method: countAll(int postId) </p>
     *
     * <p> Description: Returns the total number of replies for the given post
     * regardless of read status.  Used to populate the "Replies: N" label.</p>
     *
     * @param postId id of the parent Post
     * @return total reply count for that post
     */
    public int countAll(int postId) {
        return (int) allReplies.stream()
            .filter(r -> r.getPostId() == postId)
            .count();
    }

    /*******
     * <p> Method: clearSubset() </p>
     *
     * <p> Description: Resets the current subset to an empty list.  Called when the
     * user switches from "Unread Only" back to "All Replies".</p>
     */
    public void clearSubset() {
        currentSubset = new ArrayList<>();
    }

    /*-*************************************************************************************
    Utility
    ***************************************************************************************/

    /*******
     * <p> Method: size() </p>
     *
     * <p> Description: Returns the total number of replies in the master list.</p>
     *
     * @return number of replies in allReplies
     */
    public int size() { return allReplies.size(); }

    /*******
     * <p> Method: clear() </p>
     *
     * <p> Description: Empties the master list, the current subset, and all read
     * receipts.  Used when reloading the repository from the database.</p>
     */
    public void clear() {
        allReplies.clear();
        currentSubset.clear();
        readReceipts.clear();
    }
}