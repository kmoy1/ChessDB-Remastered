import javafx.application.Application;
import javafx.scene.*;
import javafx.stage.*;
import javafx.scene.shape.Circle;


import java.io.*;
import javafx.application.Platform;

import javafx.scene.control.TextArea;

/** Main class to start the ChessDB-Remastered Application
 * @author Kevin Moy **/
public class Main extends Application {
    /*------------Attributes-------------- */
    public GUI gui;
    public static TextArea message_text = new TextArea(); //For overlaying on-screen messages.
    /*------------End Attributes-------------- */
    public static void main(String[] args) {
        Application.launch(args); //Launch JavaFX application.
    }

    @Override
    /** JavaFX application entry point. Runs after init(), which we do nothing to.
     * @param primaryStage stage where application SCENE can be set.
     *                     If applet, primaryStage embedded into browser.
     */
    public void start(Stage primaryStage) {
        try {
            new File("book").mkdir(); //Create "book" folder in same dir as src folder
        }
        catch(Exception e) {}

        init_app(); //Initialize application.
        primaryStage.setTitle("ChessDB-Remastered");
        //Set (x,y) position of stage to (0,0): take up entirety of (left corner) of window.
        //TODO: Set width, height of primaryStage (?)
        primaryStage.setX(0);
        primaryStage.setY(0);

        Group root = new Group(); //COMPONENT with no layout. All nodes at (0,0).

        gui = new GUI(primaryStage);

        root.getChildren().add(gui.horizontal_box);

        message_text.setWrapText(true);
        message_text.setTranslateX(30);
        message_text.setTranslateY(30);
        // Message Style Settings
        message_text.setStyle("-fx-opacity: 0;" + "-fx-border-width: 10px;" + "-fx-border-radius: 10px;"
                                                + "-fx-border-style: solid;"
                                                + "-fx-control-inner-background: #efefff;"
                                                + "-fx-border-color: #afafff;");
        root.getChildren().add(message_text);

        Scene scene = new Scene(root);
        primaryStage.setScene(scene);

        primaryStage.show(); //Display app.

        system_message("Welcome to ChessDB-Remastered!", 2000);
        System.out.println("ChessDB-Remastered Start");
    }

    private void system_message(String s, int i) {
    }

    /** Initialize ChessDB app **/
    private void init_app() {

    }

}