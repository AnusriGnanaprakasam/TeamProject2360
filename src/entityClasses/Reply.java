package entityClasses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*******
 * <p> Title: Reply Class. </p>
 *
 * <p> Description: Entity class representing a single reply to a forum post in the
 * Student Discussion System.  Every Reply is linked to a parent Post via postId.
 * Unlike posts, replies are permanently deleted (not soft-deleted) when removed.
 * Content is required and non-blank.</p>
 *
 *  <p> Copyright: Anusri Gnanaprakasam © 2026 </p>
 *
 * @author Anusri Gnanaprakasam
 *
 * @version 1.00   2026-02-25  Initial version for HW2
 */
public class Reply {

    /*-*************************************************************************************
    Attributes
    ***************************************************************************************/

    /** Database-assigned primary key. -1 until this reply has been persisted. */
    private int id;

    /** Primary key of the parent Post this reply is attached to. */
    private int postId;

    /** Username of the student or staff member who wrote this reply. */
    private String author;

    /** Full text of the reply (required, non-blank). */
    private String content;

    /** Human-readable creation timestamp. */
    private String timestamp;

    /** Formatter shared by all Reply instances for consistent timestamp display. */
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /*-*************************************************************************************
    Constructors
    ***************************************************************************************/

    /*******
     * <p> Method: Reply(postId, author, content) </p>
     *
     * <p> Description: Constructor used when creating a brand-new reply before it is
     * persisted.  The id is set to -1; ReplyRepository assigns the real id after the
     * database insert.  Throws IllegalArgumentException if content is blank.</p>
     *
     * @param postId  the id of the parent Post (must already exist in the database)
     * @param author  username of the reply author (required)
     * @param content reply body text (required, non-blank)
     */
    public Reply(int postId, String author, String content) {
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("Reply content cannot be empty.");

        this.id        = -1;
        this.postId    = postId;
        this.author    = author;
        this.content   = content.trim();
        this.timestamp = LocalDateTime.now().format(FMT);
    }

    /*******
     * <p> Method: Reply(id, postId, author, content, timestamp) </p>
     *
     * <p> Description: Reconstruction constructor used when loading an existing reply
     * back from the database.  All fields are supplied directly from the ResultSet.</p>
     *
     * @param id        database-assigned integer primary key
     * @param postId    id of the parent Post
     * @param author    username of the reply author
     * @param content   reply body text
     * @param timestamp original creation timestamp as a string
     */
    public Reply(int id, int postId, String author, String content, String timestamp) {
        this.id        = id;
        this.postId    = postId;
        this.author    = author;
        this.content   = content;
        this.timestamp = timestamp;
    }

    /*-*************************************************************************************
    Getters
    ***************************************************************************************/

    /** @return database-assigned id (-1 if not yet persisted) */
    public int    getId()        { return id;        }

    /** @return id of the parent Post this reply belongs to */
    public int    getPostId()    { return postId;    }

    /** @return username of the reply author */
    public String getAuthor()    { return author;    }

    /** @return reply body text */
    public String getContent()   { return content;   }

    /** @return creation timestamp as a formatted string */
    public String getTimestamp() { return timestamp; }

    /*-*************************************************************************************
    Setters
    ***************************************************************************************/

    /*******
     * <p> Method: setId(int id) </p>
     *
     * <p> Description: Called by ReplyRepository after the database insert to store
     * the generated primary key back into this object.</p>
     *
     * @param id the database-assigned integer primary key
     */
    public void setId(int id) { this.id = id; }

    /*******
     * <p> Method: setContent(String newContent) </p>
     *
     * <p> Description: Replaces the reply body text.  Author-only enforcement is
     * handled at the ReplyRepository level; this method only validates non-blank.</p>
     *
     * @param newContent replacement body text (required, non-blank)
     */
    public void setContent(String newContent) {
        if (newContent == null || newContent.isBlank())
            throw new IllegalArgumentException("Reply content cannot be empty.");
        this.content = newContent.trim();
    }

    /*-*************************************************************************************
    Object overrides
    ***************************************************************************************/

    /*******
     * <p> Method: toString() </p>
     *
     * <p> Description: Returns the display string used by the reply ListView:
     * "id | author [timestamp]: content".  This is the format expected by
     * ControllerForum.parseIdFromListItem().</p>
     *
     * @return "id | author [timestamp]: content"
     */
    @Override
    public String toString() {
        return id + " | " + author + " [" + timestamp + "]: " + content;
    }
}