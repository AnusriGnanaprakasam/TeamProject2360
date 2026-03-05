/**
 * 
 */
/**
 * 
 */
module HW2 {
	requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
	requires javafx.graphics;

    opens applicationMain to javafx.graphics, javafx.fml;
}
