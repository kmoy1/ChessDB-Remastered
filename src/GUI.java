import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class GUI {
    private Board board; //Board to display.
    private Game g; //Game interface implemented that enforces rules.
    private Stage stage; //Top-Level Container (Window): Like JFrame in Swing
    public HBox horizontal_box = new HBox(2);
    public GUI(Stage s) throws Exception{
        //Initialize stage, board, and create a game instance from the stage + board.
        stage = s;
        board = new Board(true);
        board.s = stage;
        g = new Game(stage, board);
        g.reset(board.getFEN());
        board.g = g;
        horizontal_box.getChildren().add(board.main_box);
        horizontal_box.getChildren().add(g.vertical_box);
    }

    /** Shut down GUI, used when closing app.**/
    public void shutdown() {
        //Do nothing, for now.
    }
}
