package entityClasses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*******
 * <p> Title: Post Class. </p>
 *
 * <p> Description: Entity class representing a single forum post in the Student
Discussion System.  A Post has an author, an optional thread (defaults to
"General"), a required title, and non empty content.

Posts support soft delete function called softDelete(), which marks the post as deleted and replaces
its content with a placeholder, but the row and all replies are preserved in the
database so readers see "this post was deleted" rather than nothing..</p>
 *
 *  <p> Copyright: Anusri Gnanaprakasam © 2026 </p>
 *
 * @author Anusri Gnanaprakasam
 *
 *
 * @version 1.00   2026-02-25  Initial version for TP2
 */
public class Post {

    /*
    Attributes
    */

    /** Database assigned primary key. -1 until this post has been persisted. */
    private int id;

    /** Username of the student or staff member who created this post. */
    private String author;

    /** Thread this post belongs to. Defaults to "General" if not specified. 
     Threads can only be created, deleted and updated by 
     Staff. */
    private String thread;

    /** Short descriptive title that has to be nonblank. */
    private String title;

    /** Full body text of the post which has to also be nonblank. */
    private String content;

    /** True when the post has been soft deleted by its author. The post replies will still be seen.
     */
    private boolean deleted;

    /** readable creation timestamp. */
    private String timestamp;

    /** Formatter shared by all Post instances for consistent timestamp display. */
    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /** Placeholder text shown to viewers when a post has been soft deleted. */
    public static final String DELETED_PLACEHOLDER =
            "[This post has been deleted by the author]";

    /* 
    Constructors
    */

    /*******
     * <p> Method: Post(author, thread, title, content) </p>
     *
     * <p> Description: Constructor used when creating a brand new post before it is
     * persisted.  The id is set to -1 and the PostRepository class assigns the real id after inserting in
     * the database.  Thread  always defaults to "General" when the user leaves the thread section blank.
     * Throws IllegalArgumentException for blank title or content saying that the post content or title cannot
     * be empty.</p>
     *
     * @param author  username of the post author (required)
     * @param thread  thread name which defaults to "General" when blank
     * @param title   post title that has to be nonblank
     * @param content post body text that has to be nonblank
     */
    public Post(String author, String thread, String title, String content) {
        if (title   == null || title.isBlank())
            throw new IllegalArgumentException("Post title cannot be empty.");
        if (content == null || content.isBlank())
            throw new IllegalArgumentException("Post content cannot be empty.");

        this.id        = -1;
        this.author    = author;
        this.thread    = (thread == null || thread.isBlank()) ? "General" : thread.trim();
        this.title     = title.trim();
        this.content   = content.trim();
        this.deleted   = false;
        this.timestamp = LocalDateTime.now().format(FMT);
    }

    /*******
     * <p> Method: Post(id, author, thread, title, content, deleted, timestamp) </p>
     *
     * <p> Description: Reconstruction constructor used when loading an existing post
     * back from the database.  All fields are supplied directly from the database.</p>
     *
     * @param id        database assigned integer primary key
     * @param author    username of the post author
     * @param thread    thread name
     * @param title     post title
     * @param content   post body which may have a deleted placeholder if soft deleted
     * @param deleted   true if this post has been soft deleted
     * @param timestamp original creation timestamp as a string
     */
    public Post(int id, String author, String thread, String title,
                String content, boolean deleted, String timestamp) {
        this.id        = id;
        this.author    = author;
        this.thread    = thread;
        this.title     = title;
        this.content   = content;
        this.deleted   = deleted;
        this.timestamp = timestamp;
    }

    /*
    Getters
    */

    /** @return database assigned id (-1 if not yet inserted into the database) */
    public int     getId()        { return id;        }

    /** @return username of the post author */
    public String  getAuthor()    { return author;    }

    /** @return thread name this post belongs to */
    public String  getThread()    { return thread;    }

    /** @return post title */
    public String  getTitle()     { return title;     }

    /** @return post body text, or deleted placeholder if soft deleted */
    public String  getContent()   { return content;   }

    /** @return true if this post has been soft deleted */
    public boolean isDeleted()    { return deleted;   }

    /** @return creation timestamp as a formatted string */
    public String  getTimestamp() { return timestamp; }

    /*
    Setters
    */

    /*******
     * <p> Method: setId(int id) </p>
     *
     * <p> Description: Called by PostRepository after the database insert to store
     * the generated primary key in the object.</p>
     *
     * @param id the database assigned integer primary key
     */
    public void setId(int id) { this.id = id; }

    /*******
     * <p> Method: setTitle(String newTitle) </p>
     *
     * <p> Description: Replaces the post title.  Author enforcement is handled
     * at the PostRepository level and this method validates to check if the title is nonblank.</p>
     *
     * @param newTitle replacement title is required and nonblank
     */
    public void setTitle(String newTitle) {
        if (newTitle == null || newTitle.isBlank())
            throw new IllegalArgumentException("Post title cannot be empty.");
        this.title = newTitle.trim();
    }

    /*******
     * <p> Method: setContent(String newContent) </p>
     *
     * <p> Description: Replaces the post body text. Author only enforcement is
     * handled at the PostRepository level and this method only validates nonblank cases.</p>
     *
     * @param newContent replacement body text is required and nonblank
     */
    public void setContent(String newContent) {
        if (newContent == null || newContent.isBlank())
            throw new IllegalArgumentException("Post content cannot be empty.");
        this.content = newContent.trim();
    }

    /*
    Soft Deletion
    */

    /*******
     * <p> Method: softDelete() </p>
     *
     * <p> Description: Marks this post as soft deleted by setting deleted equal to true and
     * replacing the content with DELETED_PLACEHOLDER.  The post row and all replies
     * are preserved in the database.  Author only enforcement is handled at the
     * repository level.</p>
     */
    public void softDelete() {
        this.deleted = true;
        this.content = DELETED_PLACEHOLDER;
    }

    /*
    Object overrides
    */

    /*******
     * <p> Method: toString() </p>
     *
     * <p> Description: Returns the display string used by ListView widgets:
     * "id | title" for live posts, "id | [DELETED]" for soft deleted posts.
     * This is the format expected by ControllerForum.parseIdFromListItem() function</p>
     *
     * @return "id | title"  or  "id | [DELETED]"
     */
    @Override
    public String toString() {
        return id + " | " + (deleted ? "[DELETED]" : title);
    }
}