package entityClasses;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/*******
 * <p> Title: PostRepository Class. </p>
 *
 * <p> Description: In-memory repository that stores all current Post objects as well
 * as any arbitrary subset of those posts (e.g., the results of a keyword search or a
 * per-user filter).
 *
 * The class acts as the bridge between the database layer (Database.java) and the
 * controller layer (ControllerForum.java).  The database performs all persistence;
 * this class holds the in-memory list so the GUI has fast access without re-querying
 * the database on every keypress.
 *
 * The subset is always a filtered view of allPosts and can be empty, contain one
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
public class PostRepository {

    /*-*************************************************************************************
    Attributes
    ***************************************************************************************/

    /** Master list of all Post objects currently known to the application. */
    private List<Post> allPosts;

    /** Current working subset — may be empty, may equal allPosts, may be anything
     *  between.  Set by search / filter operations; cleared by showAllPosts(). */
    private List<Post> currentSubset;

    /*-*************************************************************************************
    Constructor
    ***************************************************************************************/

    /*******
     * <p> Method: PostRepository() </p>
     *
     * <p> Description: Creates an empty repository.  Both the master list and the
     * current subset start empty.  The controller populates allPosts by calling
     * addPost() once for each post loaded from the database at startup.</p>
     */
    public PostRepository() {
        allPosts      = new ArrayList<>();
        currentSubset = new ArrayList<>();
    }

    /*-*************************************************************************************
    CREATE — add a post to the repository
    ***************************************************************************************/

    /*******
     * <p> Method: addPost(Post post) </p>
     *
     * <p> Description: Adds a Post to the master list.  Called by the controller after
     * a successful database insert.  The Post's id must have been set by setId() before
     * this is called so the list holds the correct key.</p>
     *
     * @param post the Post to add (must not be null; id must be set)
     */
    public void addPost(Post post) {
        if (post == null) return;
        allPosts.add(post);
    }

    /*-*************************************************************************************
    READ — retrieve posts from the repository
    ***************************************************************************************/

    /*******
     * <p> Method: getPost(int id) </p>
     *
     * <p> Description: Finds and returns the Post with the given id from the master
     * list.  Returns null if no matching post is found.</p>
     *
     * @param id the database-assigned integer primary key
     * @return the matching Post, or null if not found
     */
    public Post getPost(int id) {
        for (Post p : allPosts)
            if (p.getId() == id) return p;
        return null;
    }

    /*******
     * <p> Method: getAllPosts() </p>
     *
     * <p> Description: Returns a copy of the master list containing every Post
     * currently in the repository, including soft-deleted ones.  The GUI uses the
     * Post.toString() representation to show "[DELETED]" for deleted posts.</p>
     *
     * @return a new List containing all Post objects (may be empty)
     */
    public List<Post> getAllPosts() {
        return new ArrayList<>(allPosts);
    }

    /*******
     * <p> Method: getSubset() </p>
     *
     * <p> Description: Returns the current working subset.  The subset is populated
     * by searchByKeyword(), filterByUser(), or any other filter method.  It is
     * cleared back to an empty list by clearSubset().  The subset may be empty,
     * contain one element, or be arbitrarily large.</p>
     *
     * @return the current subset List (may be empty)
     */
    public List<Post> getSubset() {
        return new ArrayList<>(currentSubset);
    }

    /*-*************************************************************************************
    UPDATE — modify an existing post in the repository
    ***************************************************************************************/

    /*******
     * <p> Method: updatePost(int id, String requestingUser, String newTitle,
     *                        String newContent) </p>
     *
     * <p> Description: Updates the title and content of the post with the given id.
     * Returns false if the post is not found, if the requestingUser is not the
     * original author, or if the post has already been soft-deleted.  The in-memory
     * Post object is updated; the caller is responsible for also calling
     * Database.updatePost() to persist the change.</p>
     *
     * @param id             database id of the post to update
     * @param requestingUser must match the post's author
     * @param newTitle       replacement title (non-blank)
     * @param newContent     replacement content (non-blank)
     * @return true if the update succeeded, false otherwise
     */
    public boolean updatePost(int id, String requestingUser,
                              String newTitle, String newContent) {
        Post p = getPost(id);
        if (p == null || p.isDeleted()) return false;
        if (!p.getAuthor().equals(requestingUser)) return false;
        p.setTitle(newTitle);
        p.setContent(newContent);
        return true;
    }

    /*-*************************************************************************************
    DELETE — soft-delete a post in the repository
    ***************************************************************************************/

    /*******
     * <p> Method: deletePost(int id, String requestingUser) </p>
     *
     * <p> Description: Soft-deletes the post with the given id by calling
     * Post.softDelete().  Returns false if the post is not found or if the
     * requestingUser is not the original author.  The in-memory object is updated;
     * the caller is responsible for also calling Database.softDeletePost() to persist
     * the change.  Replies are NOT removed from the database.</p>
     *
     * @param id             database id of the post to soft-delete
     * @param requestingUser must match the post's author
     * @return true if the soft-delete succeeded, false otherwise
     */
    public boolean deletePost(int id, String requestingUser) {
        Post p = getPost(id);
        if (p == null) return false;
        if (!p.getAuthor().equals(requestingUser)) return false;
        p.softDelete();
        return true;
    }

    /*-*************************************************************************************
    SUBSET / FILTER operations
    ***************************************************************************************/

    /*******
     * <p> Method: searchByKeyword(String keyword, String thread) </p>
     *
     * <p> Description: Builds and stores a subset of non-deleted posts whose title or
     * content contains the keyword (case-insensitive).  If thread is non-blank, only
     * posts in that thread are included.  If keyword is null or blank, all non-deleted
     * posts in the (optionally filtered) thread are returned.  The subset replaces the
     * previous currentSubset.  The subset may be empty.</p>
     *
     * @param keyword search term (case-insensitive); null or blank matches all
     * @param thread  restrict to this thread; null or blank searches all threads
     * @return the resulting subset List (also stored as currentSubset)
     */
    public List<Post> searchByKeyword(String keyword, String thread) {
        String kw = (keyword == null) ? "" : keyword.toLowerCase().trim();
        currentSubset = allPosts.stream()
            .filter(p -> !p.isDeleted())
            .filter(p -> (thread == null || thread.isBlank())
                         || p.getThread().equalsIgnoreCase(thread.trim()))
            .filter(p -> kw.isEmpty()
                         || p.getTitle().toLowerCase().contains(kw)
                         || p.getContent().toLowerCase().contains(kw))
            .collect(Collectors.toList());
        return new ArrayList<>(currentSubset);
    }

    /*******
     * <p> Method: filterByUser(String username) </p>
     *
     * <p> Description: Builds and stores a subset containing only posts authored by
     * the specified user, including their soft-deleted posts (so the student can see
     * their own deleted content).  The subset replaces the previous currentSubset.
     * The subset may be empty.</p>
     *
     * @param username the username whose posts should be returned
     * @return the resulting subset List (also stored as currentSubset)
     */
    public List<Post> filterByUser(String username) {
        currentSubset = allPosts.stream()
            .filter(p -> p.getAuthor().equals(username))
            .collect(Collectors.toList());
        return new ArrayList<>(currentSubset);
    }

    /*******
     * <p> Method: clearSubset() </p>
     *
     * <p> Description: Resets the current subset to an empty list.  Called when the
     * user clicks "All Posts" to return to the unfiltered master view.</p>
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
     * <p> Description: Returns the total number of posts in the master list.</p>
     *
     * @return number of posts in allPosts
     */
    public int size() { return allPosts.size(); }

    /*******
     * <p> Method: clear() </p>
     *
     * <p> Description: Empties both the master list and the current subset.  Used
     * when reloading the repository from the database.</p>
     */
    public void clear() {
        allPosts.clear();
        currentSubset.clear();
    }
}