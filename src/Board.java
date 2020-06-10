import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;

import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;

import javafx.scene.control.TextArea;

import javafx.scene.control.ListView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import javafx.stage.FileChooser;

/** Board class which represents code for playable action on a board interface.
 * @author Kevin Moy**/
public class Board{
    /* --- Public attributes --- */
    public Stage s = new Stage();
    public Game g = null;
    ListView<String> list = new ListView<String>();
    public int turn;
    public int fullmove_number;
    public Boolean flip;
    /* --- Private attributes --- */
    private FileChooser f = new FileChooser();
    private Boolean with_gui;
    private static Hashtable translit_light;
    private static Hashtable translit_dark;
    private static InputStream stream = Main.class.getResourceAsStream("fonts/MERIFONTNEW.TTF");
    private static Font pieceFont;

    private String castling_rights;
    private String ep_square_algeb;
    private int halfmove_clock;
    private String rep;
    private char[][] board=new char[8][8];
    private char[][] fonts=new char[8][8];


    /* --- For piece drag/drop --- */
    // drag and drop
    private Boolean is_drag_going;
    private char drag_piece;
    private char orig_drag_piece;
    private char orig_piece;
    private char orig_empty;
    private int drag_from_i;
    private int drag_from_j;
    private int drag_to_i;
    private int drag_to_j;
    private int drag_to_x;
    private int drag_to_y;
    private int drag_from_x;
    private int drag_from_y;
    private int drag_dx;
    private int drag_dy;

    /* --- GC Attributes --- */
    private Group canvas_group = new Group();

    public HBox main_box=new HBox(2);
    public VBox vertical_box=new VBox(2);
    private HBox game_controls_box=new HBox(2);
    private HBox controls_box=new HBox(2);

    private TextField fen_text = new TextField();

    public Canvas canvas;
    public Canvas highlight_canvas;
    public Canvas upper_canvas;

    private GraphicsContext gc;
    private GraphicsContext highlight_gc;
    private GraphicsContext upper_gc;

    private Color board_color;
    private Color piece_color;

    /* --- Attributes for Pieces --- */
    private static int padding;
    private static int piece_size;
    private static int margin;
    private static int board_size;
    private static int font_size;

    /* --- UCI OUT --- */
    Move makemove=new Move();

    /* --- MOVE GENERATION ATTRIBUTES --- */
    final static int move_table_size = 20000;
    static MoveDescriptor move_table[] = new MoveDescriptor[move_table_size];

    static int move_table_ptr[][][]=new int[8][8][64];

    /* ---- Piece Representation ---- */
    //We use a 6-bit system to inherently give the encoding of a piece (multiple) move capabilities.
    // If a piece has bit 5, it has SLIDING capabilities, i.e. can move any number of steps in an allowed direction (diagonal/lat/long).
    final static int SLIDING = 32;
    // If a piece has bit 4, it has STRAIGHT capabilities, i.e. can move laterally.
    final static int STRAIGHT = 16;
    // If a piece has bit 3, it has DIAGONAL capabilities, can move in a diagonal.
    final static int DIAGONAL = 8;
    // If a piece has bit 2, it has SINGLE capabilities, i.e. it can't do shit.
    final static int SINGLE = 4;
    // bit 1 indicates a pawn.
    final static int IS_PAWN = 2;
    // bit 0 is for color

    final static int QUEEN = SLIDING|STRAIGHT|DIAGONAL;
    final static int ROOK = SLIDING|STRAIGHT;
    final static int BISHOP = SLIDING|DIAGONAL;
    final static int KING = SINGLE|STRAIGHT|DIAGONAL;
    final static int KNIGHT = SINGLE;
    final static int PAWN = SINGLE|IS_PAWN;

    final static int all_pieces[] = {KING, QUEEN, ROOK, BISHOP, KNIGHT, PAWN};
    final static char promotion_pieces[] = {'q', 'r', 'b', 'n'};

    final static int PIECE_TYPE = 62;
    final static int PIECE_COLOR = 1;

    final static int WHITE = 1;
    final static int BLACK = 0;

    private int curr_i = 0;
    private int curr_j = 0;

    private int move_gen_curr_ptr = 0;
    private char current_move_gen_piece = ' ';
    private int current_move_gen_piece_code = 0;
    private int current_move_gen_piece_type = 0;
    private Boolean is_current_move_gen_piece_sliding = false;
    private int current_move_gen_piece_color = 0;
    private Move current_move;

    //Indicators 1 and -1 indicate color to move on current board.
    final static int TURN_WHITE = 1;
    final static int TURN_BLACK = -1;
    public boolean deep_going = false;

    /** Create a board instance, in particular a chain of HBoxes and VBoxes to be shown in the GUI class. **/
    public Board(boolean b) throws Exception{
        with_gui = b;
        if (with_gui) {
            //TODO (LATER): Implement engine controls
            flip = false;
            //Draw board GUI canvases.
            canvas = new Canvas(board_size,board_size);
            highlight_canvas = new Canvas(board_size, board_size);
            upper_canvas = new Canvas(board_size, board_size);
            //Add canvases to group layout.
            canvas_group.getChildren().add(canvas);
            canvas_group.getChildren().add(highlight_canvas);
            canvas_group.getChildren().add(upper_canvas);
            //Create button below board.

            //Add board to VBox (at the very top)
            vertical_box.getChildren().add(canvas_group);

            //SET BUTTON TO REVERT TO STARTING POSITION (moves saved).
            Button to_begin_button = new Button();
            to_begin_button.setText("Start");
            to_begin_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    g.to_begin();
                }
            });

            FileInputStream img = new FileInputStream("src/images/left.png");
            Image leftArrow = new Image(img);
            ImageView leftView = new ImageView(leftArrow);
            leftView.setFitHeight(50);
            leftView.setFitWidth(50);
            Button back_button = new Button();
            back_button.setGraphic(leftView);
            back_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    g.back();
                }
            });

            //TODO: Add Color to Move button + last move played textbox

            img = new FileInputStream("src/images/right.png");
            Image rightArrow = new Image(img);
            ImageView rightView = new ImageView(rightArrow);
            rightView.setFitHeight(50);
            rightView.setFitWidth(50);
            Button forward_button = new Button();
            forward_button.setGraphic(rightView);
            forward_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    g.forward();
                }
            });

            //SET BUTTON TO FORWARD TO END OF GAME.
            Button to_end_button = new Button();
            to_end_button.setText("End");
            to_end_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    g.to_end();
                }
            });

            //Add buttons to game controls HBox.
            game_controls_box.getChildren().add(to_begin_button);
            game_controls_box.getChildren().add(back_button);
            game_controls_box.getChildren().add(forward_button);
            game_controls_box.getChildren().add(to_end_button);

            //Add game controls hbox to board's vertical box, now below the board.
            vertical_box.getChildren().add(game_controls_box);

            //TODO: Set up commentary box for each move and add to vertical_box.
            //TODO: Set up homescreen link AND popup menu for moves list.

            vertical_box.getChildren().add(fen_text);
            Button flip_button = new Button();
            flip_button.setText("Flip");
            flip_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    flip();
                }
            });

            Button set_fen_button = new Button();
            set_fen_button.setText("Set Fen");
            set_fen_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    set_from_fen(fen_text.getText());
                }
            });

            Button report_fen_button=new Button();
            report_fen_button.setText("Report Fen");
            report_fen_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    drawBoard();
                }
            });

            Button reset_button = new Button();
            reset_button.setText("Reset");
            reset_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    reset();
                }
            });

            Button delete_button = new Button();
            delete_button.setText("Delete");
            delete_button.setOnAction(new EventHandler<ActionEvent>() {
                @Override public void handle(ActionEvent e) {
                    set_from_fen_inner(g.delete_move(),false);
                    make_move_show(null);
                }
            });

            controls_box.getChildren().add(flip_button);
            controls_box.getChildren().add(set_fen_button);
            controls_box.getChildren().add(report_fen_button);
            controls_box.getChildren().add(reset_button);
            controls_box.getChildren().add(delete_button);
            vertical_box.getChildren().add(controls_box);

            //Finally, add VBox with board + controls to HBox.
            main_box.getChildren().add(vertical_box);
            list.setMaxWidth(80);
//            main_box.getChildren().add(list);
            //TODO: Add commentary textarea to verticalbox here.

            //Set mouse control logic for moving pieces on board.
            upper_canvas.setOnMouseDragged(mouseHandler);
            upper_canvas.setOnMouseClicked(mouseHandler);
            upper_canvas.setOnMouseReleased(mouseHandler);
            //Set colors for board.
            gc = canvas.getGraphicsContext2D();
            highlight_gc = highlight_canvas.getGraphicsContext2D();
            highlight_canvas.setOpacity(0.2);
            highlight_gc.setFill(Color.rgb(255,255,0));
            upper_gc = upper_canvas.getGraphicsContext2D();
            board_color = Color.rgb(160, 82, 45);
            piece_color = Color.rgb(0, 0, 0);
        }
        reset();
    }

    private void make_move_show(Object o) {
    }

    /** Set board to starting position and set necessary
     * attribute controls for a valid game.**/
    public void reset() {
        rep = "rnbqkbnrpppppppp                                PPPPPPPPRNBQKBNR";
        setPosition(rep);
        castling_rights = "KQkq";
        ep_square_algeb = "-";
//        halfmove_clock = 0;
//        fullmove_number = 1;
        turn = TURN_WHITE;
        if(with_gui) {
            is_drag_going=false;
            drawBoard();
            reset_game();
        }
    }

    private void reset_game() {
        if(g != null) g.reset(getFEN());
    }

    /** Set our board 2D array to the pieces specified by REP.**/
    private void setPosition(String rep) {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                board[i][j] = rep.charAt(i + j*8);
            }
        }
    }

    /** KEY METHOD: Draw the board based on current position attributes.
     * Assume GraphicsContext gc has been set properly.**/
    private void drawBoard() {
        setPieceFonts(board); //Populate squares.
        gc.setFont(pieceFont);
        //Fill color of board (brown)
        gc.setFill(board_color);
        gc.fillRect(0, 0, board_size, board_size);
        //Fill bar below board.
        //Fill in piece colors.
        gc.setFill(piece_color);
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                gc.fillText(Character.toString(fonts[i][j]), gc_x(i),gc_y(j)+piece_size+padding);
            }
        }

        gc.setFont(Font.font("Courier New",font_size));

        gc.strokeRect(0, 0, board_size, board_size);
        fen_text.setText(getFEN());
    }

    /** Return true if TURN is in check on the current board position. **/
    private boolean inCheck(int turn) {
        Boolean found = false;
        char search_king = turn == TURN_WHITE? 'K':'k';
        int king_i = 0;
        int king_j = 0;
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                if(board[i][j] == search_king) {
                    king_i = i;
                    king_j = j;
                    found = true;
                    break;
                }
                if(found){break;}
            }
        }
        return is_square_in_check(king_i,king_j,turn==TURN_WHITE?WHITE:BLACK);
    }

    private boolean is_square_in_check(int i, int j, int color) {
        int attacker_color = color == WHITE? BLACK:WHITE;
        Boolean is_check=false;
        for(int p = 0; p < all_pieces.length; p++) {
            int piece_code = all_pieces[p];
            int piece_type = piece_code & PIECE_TYPE;
            int check_ptr=move_table_ptr[i][j][piece_code|color];
            char test_piece = piece_of_code(piece_code|attacker_color);
            MoveDescriptor md;
            do {
                md = move_table[check_ptr];
                if (md.castling) {
                    check_ptr++;
                }
                else if (!md.end_piece) {
                    int to_i = md.to_i;
                    int to_j = md.to_j;
                    char to_piece=board[to_i][to_j];
                    if(piece_type==PAWN) {
                        if(to_i==i) {
                            // pawn cannot check forward
                            to_piece=' ';
                        }
                    }
                    if(to_piece==test_piece) {
                        is_check=true;
                    }
                    else {
                        if(to_piece==' ') {
                            check_ptr++;
                        }
                        else {
                            check_ptr=md.next_vector;
                        }
                    }
                }
            }while((!md.end_piece)&&(!is_check));

            if(is_check) {
                break;
            }
        }
        return is_check;
    }

    private char piece_of_code(int code) {
        if(code==(WHITE|KING)){return 'K';}
        if(code==(BLACK|KING)){return 'k';}
        if(code==(WHITE|QUEEN)){return 'Q';}
        if(code==(BLACK|QUEEN)){return 'q';}
        if(code==(WHITE|ROOK)){return 'R';}
        if(code==(BLACK|ROOK)){return 'r';}
        if(code==(WHITE|BISHOP)){return 'B';}
        if(code==(BLACK|BISHOP)){return 'b';}
        if(code==(WHITE|KNIGHT)){return 'N';}
        if(code==(BLACK|KNIGHT)){return 'n';}
        if(code==(WHITE|PAWN)){return 'P';}
        if(code==(BLACK|PAWN)){return 'p';}
        return ' ';
    }

    /** Make square colors show on board canvas by populating fonts array. **/
    private void setPieceFonts(char[][] board) {
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                fonts[i][j]= isDarkSqr(i,j)? (char) translit_dark.get(board[i][j]) : (char) translit_light.get(board[i][j]);
            }
        }
    }

    /** Check if (x,y) is a dark square on the chessboard.**/
    private boolean isDarkSqr(int x, int y) {
        return(x+y) % 2 == 1;
    }

    /** Flipping board action upon mouseclick. **/
    public void flip() {
        flip = !flip;
        drawBoard();
        g.update();
    }

    public Boolean set_from_fen(String fen) {
        return set_from_fen_inner(fen,true);
    }

    public Boolean set_from_fen_inner(String fen,Boolean do_reset_game) {
        rep="";
        String[] fen_parts=fen.split(" ");
        fen=fen_parts[0];
        for(int i=0;i<fen.length();i++) {
            char current=fen.charAt(i);
            if(rep.length()<64) {
                if(current=='/') { }
                else {
                    if((current>='1')&&(current<='8')) {
                        for(int j=0;j<Integer.parseInt(""+current);j++) {
                            rep+=" ";
                        }
                    }
                    else {
                        rep+=current;
                    }
                }
            }
            else {
                break;
            }
        }

        if(rep.length()<64) {
            board_to_rep();
            return false;
        }
        setPosition(rep);
        if(fen_parts.length>=2) {
            String turn_part=fen_parts[1];
            if(turn_part.charAt(0)=='w') {
                turn=TURN_WHITE;
            }
            else {
                turn=TURN_BLACK;
            }
        }
        if(fen_parts.length>=3) {
            String castling_rights_part=fen_parts[2];
            castling_rights=castling_rights_part;
        }
        if(fen_parts.length>=4) {
            String ep_square_algeb_part=fen_parts[3];
            ep_square_algeb=ep_square_algeb_part;
        }
        if(fen_parts.length>=5) {
            String halfmove_clock_part=fen_parts[4];
            halfmove_clock=Integer.parseInt(halfmove_clock_part);
        }
        if(fen_parts.length>=6) {
            String fullmove_number_part=fen_parts[5];
            fullmove_number=Integer.parseInt(fullmove_number_part);
        }
        if((with_gui)&&(!deep_going)) {
            drawBoard();
            if(do_reset_game) {
                reset_game();
            }
        }
        return true;
    }

    public static void init_class() {
        //Set dimensions for board GUI
        piece_size = 52;
        padding = 5;
        margin = 10;
        font_size = 15;

        board_size = (piece_size + padding) * 8 + (2 * margin);
//        info_bar_size = font_size + (2 * padding) + margin;

        pieceFont = Font.loadFont(stream, piece_size); //set piece font (how the pieces look) to alpha.ttf as loaded.

        // Create maps to white pieces.

        translit_light=new Hashtable();

        translit_light.put(' ',' ');
        translit_light.put('P','p');
        translit_light.put('N','n');
        translit_light.put('B','b');
        translit_light.put('R','r');
        translit_light.put('Q','q');
        translit_light.put('K','k');
        translit_light.put('p','o');
        translit_light.put('n','m');
        translit_light.put('b','v');
        translit_light.put('r','t');
        translit_light.put('q','w');
        translit_light.put('k','l');

        // Maps to black pieces.

        translit_dark=new Hashtable();

        translit_dark.put(' ','+');
        translit_dark.put('P','P');
        translit_dark.put('N','N');
        translit_dark.put('B','B');
        translit_dark.put('R','R');
        translit_dark.put('Q','Q');
        translit_dark.put('K','K');
        translit_dark.put('p','O');
        translit_dark.put('n','M');
        translit_dark.put('b','V');
        translit_dark.put('r','T');
        translit_dark.put('q','W');
        translit_dark.put('k','L');

        //For this given position, create all possible move descriptors, i.e. all possible moves, disregarding turn.
        int move_table_curr_ptr = 0;
        for(int x = 0; x < 8; x++) {
            for(int y = 0; y < 8; y++) {
                //6-bit piece encoding goes up to 2^5 - 1 = 63.
                for(int p = 0; p < 64; p++) {
                    int piece_type = p & PIECE_TYPE;
                    int piece_color = p & PIECE_COLOR;
                    if(isPiece(piece_type)) {
                        boolean is_single = ((piece_type & SINGLE) != 0);
                        move_table_ptr[x][y][p] = move_table_curr_ptr;
                        //NOTE: top left corner is (0,0), so Black's back rank is 0 and White's is 7.
                        //Left column is 0, right column is 7.
                        for(int dx = -2; dx <= 2; dx++) {
                            for(int dy = -2; dy <= 2; dy++) {
                                Boolean is_castling = isCastlingHelper(p,x,y,dx,dy);
                                if (moves(dx,dy) && ((is_castling) || diagPieceMoved(piece_type, dx, dy)
                                                                || straightPieceMoved(piece_type, dx, dy)
                                                                || knightMoved(piece_type, dx, dy)
                                                                || pawnCaptures(piece_type, piece_color, x, y, dx, dy)))
                                {
                                    int start_vector = move_table_curr_ptr;
                                    int possible_dest_x = x;
                                    int possible_dest_y = y;
                                    Boolean square_ok;
                                    do {
                                        possible_dest_x += dx;
                                        possible_dest_y += dy;
                                        square_ok = inbounds(possible_dest_x, possible_dest_y);
                                        if(square_ok) {
                                            if(isPromotionMove(p, possible_dest_x, possible_dest_y)) {
                                                for(int prom = 0; prom < promotion_pieces.length; prom++) {
                                                    MoveDescriptor md = new MoveDescriptor();
                                                    md.to_i=possible_dest_x;
                                                    md.to_j=possible_dest_y;
                                                    md.castling=false;
                                                    md.promotion=true;
                                                    md.prom_piece=promotion_pieces[prom];
                                                    move_table[move_table_curr_ptr++]=md;
                                                }
                                            }
                                            else {
                                                MoveDescriptor md = new MoveDescriptor();
                                                md.to_i = possible_dest_x;
                                                md.to_j = possible_dest_y;
                                                md.castling = is_castling;
                                                move_table[move_table_curr_ptr++]=md;
                                            }
                                        }
                                    }while(square_ok && (!is_single));

                                    for (int ptr = start_vector; ptr < move_table_curr_ptr; ptr++) {
                                        move_table[ptr].next_vector = move_table_curr_ptr;
                                    }
                                }
                            }
                        }
                        //Update move table.
                        move_table[move_table_curr_ptr] = new MoveDescriptor();
                        move_table[move_table_curr_ptr++].end_piece = true;
                    }
                }
            }
        }
    }

    /** Return true if cartesian coordinate (x,y) is a valid coordinate
     * in a zero-indexed 8x8 graph, i.e. our board **/
    private static Boolean inbounds(int x, int y) {
        if((x >= 0) && (x <= 7) && (y >= 0) && (y <= 7)) {
            return true;
        }
        return false;
    }

    /** Helper method that returns true if a piece actually has moved, i.e. its overall displacement (dx+dy) is positive**/
    private static Boolean moves(int dx, int dy) {
        return Math.abs(dx) + Math.abs(dy) > 0;
    }

    /** Helper method that returns true if a diagonal piece (bishop, king, queen) has positive displacement. **/
    private static Boolean diagPieceMoved(int piece_type, int dx, int dy) {
        return ((dx * dy) != 0) &&
                ((Math.abs(dx) != 2) && (Math.abs(dy) != 2))
                &&
                ((piece_type & DIAGONAL) != 0);
    }

    /** Helper method that returns true if a STRAIGHT-moving piece (queen, rook, king) has positive displacement. **/
    private static Boolean straightPieceMoved(int piece_type, int dx, int dy) {
        return ((dx*dy)==0)
                        && ((Math.abs(dx) != 2) && (Math.abs(dy) != 2))
                        && ((piece_type & STRAIGHT) != 0);
    }

    /** Helper method that returns true if a KNIGHT has moved **/
    private static Boolean knightMoved(int piece_type, int dx, int dy) {
        return ((Math.abs(dx * dy) == 2) && (piece_type == KNIGHT));
    }

    /** Helper method that returns true if a pawn has made a capture.**/
    private static Boolean pawnCaptures(int piece_type, int piece_color, int i, int j, int dx, int dy) {
        return ((piece_type == PAWN) && (Math.abs(dx) < 2) && (Math.abs(dy) > 0) &&
                (((piece_color==WHITE) && (dy < 0) && ((Math.abs(dy) == 1)
                        || ((j==6) && (dx==0))))  //enpassant for black
                        || ((piece_color==BLACK) && (dy>0) && ((Math.abs(dy)==1) || ((j==1) && (dx==0))))) //enpassant for white.
        );
    }

    /** Return true if a castling move, i.e.
     * if the move p -> (x,y) with displacement (dx,dy) specifies castling. **/
    private static boolean isCastlingHelper(int p, int x, int y, int dx, int dy) {
        return ((p==(WHITE|KING)) && (((x==4)&&(y==7)&&(dx==2)&&(dy==0))
                ||
                        ((x==4)&&(y==7)&&(dx==-2)&&(dy==0)))
                ||
                ((p==(BLACK|KING)) && (((x==4)&&(y==0)&&(dx==2)&&(dy==0))
                        || ((x==4)&&(y==0)&&(dx==-2)&&(dy==0)))
                ));
    }
    /** Helper function for init_class() that returns true iff
     * the move specified by p -> (x,y) is a promotion or not **/
    private static boolean isPromotionMove(int p, int x, int y) {
        return ((p==(WHITE|PAWN))&&(y==0)) || ((p==(BLACK|PAWN))&&(y==7));
    }

    /** Return true if piece_type is 0-5, specifying piece enumeration. **/
    private static boolean isPiece(int piece_type) {
        return (piece_type==PAWN)||(piece_type==KNIGHT)||(piece_type==BISHOP)
                ||(piece_type==ROOK)||(piece_type==QUEEN)||(piece_type==KING);
    }

    /** Initialize move generator. **/
    private void init_move_generator() {
        curr_i=-1;
        curr_j=0;
        next_square(-1,0);
    }

    /** Set instance variables for piece being moved.**/
    private void next_square(int i, int j) {
        boolean stop = false;
        do {
            curr_i++;
            if(curr_i > 7) {
                curr_i = 0;
                curr_j++;
            }
            if(curr_j > 7) {
                stop=true;
            }
            else {
                char gen_piece=board[curr_i][curr_j];
                stop=((gen_piece!=' ') && (turn_of(gen_piece)==turn));
            }
        }
        while(!stop);

        if(curr_j < 8) {
            current_move_gen_piece = board[curr_i][curr_j];
            current_move_gen_piece_code=code_of(current_move_gen_piece);
            current_move_gen_piece_type=current_move_gen_piece_code&PIECE_TYPE;
            current_move_gen_piece_color=color_of(current_move_gen_piece);

            is_current_move_gen_piece_sliding=((current_move_gen_piece_code&SLIDING)!=0);

            move_gen_curr_ptr=move_table_ptr[curr_i][curr_j][current_move_gen_piece_code];
        }
    }

    /** Given character of piece SELECTED, return the indicator for which color to move. White pieces have uppercase
     * letters while Black pieces have lowercase. **/
    private int turn_of(char piece) {
        //Black pieces
        if(Character.isLowerCase(piece)) {
            return TURN_BLACK;
        }
        return TURN_WHITE;
    }

    /** Given character of piece, return that piece's color. White pieces have uppercase
     * letters while Black pieces have lowercase. **/
    private int color_of(char piece) {
        if(Character.isLowerCase(piece)) {
            return BLACK;
        }
        return WHITE;
    }

    /** Given a character, return its 6-bit encoding. **/
    private int code_of(char piece) {
        switch(piece) {
            case 'p':
                return BLACK|PAWN;
            case 'P':
                return WHITE|PAWN;
            case 'n':
                return BLACK|KNIGHT;
            case 'N':
                return WHITE|KNIGHT;
            case 'b':
                return BLACK|BISHOP;
            case 'B':
                return WHITE|BISHOP;
            case 'r':
                return BLACK|ROOK;
            case 'R':
                return WHITE|ROOK;
            case 'q':
                return BLACK|QUEEN;
            case 'Q':
                return WHITE|QUEEN;
            case 'k':
                return BLACK|KING;
            case 'K':
                return WHITE|KING;
            default: //Should not reach here.
                return 0;
        }
    }

    public String getFEN() {
        String fen = "";
        board_to_rep();
        for(int j=0;j<8;j++) {
            int empty_cnt=0;
            for(int i=0;i<8;i++)
            {
                int index=i+j*8;
                char current=rep.charAt(index);
                if(current==' ')
                {
                    empty_cnt++;
                }
                else
                {
                    if(empty_cnt>0)
                    {
                        fen+=empty_cnt;
                        empty_cnt=0;
                    }
                    fen+=current;
                }
            }
            if(empty_cnt>0)
            {
                fen+=empty_cnt;
            }
            if(j<7)
            {
                fen+="/";
            }
        }

        fen += " " +(turn==TURN_WHITE?"w":"b") +" "
                        +castling_rights
                        +" "
                        +ep_square_algeb
                        +" "
                        +halfmove_clock
                        +" "
                        +fullmove_number;

        return(fen);
    }

    private String board_to_rep() {
        rep = "";
        for(int i = 0; i < 8; i++) {
            for(int j = 0; j < 8; j++) {
                rep += board[j][i];
            }
        }
        return rep;
    }

    // convert board coordinates to screen coordinates
    private int gc_x(int i) {
        return(margin+(flip?(7-i):i)*(piece_size+padding));
    }

    private int gc_y(int j) {
        return(margin+((flip?(7-j):j)*(piece_size+padding)));
    }

    // convert screen coordinates to board coordinates
    private int gc_i(int x)
    {
        int i=(int)((x-margin)/(piece_size+padding));
        return(flip?(7-i):i);
    }

    private int gc_j(int y)
    {
        int j=(int)((y-margin)/(piece_size+padding));
        return(flip?(7-j):j);
    }

    private EventHandler<MouseEvent> mouseHandler = new EventHandler<MouseEvent>() {
        @Override
        public void handle(MouseEvent mouseEvent) {
            int x=(int)mouseEvent.getX();
            int y=(int)mouseEvent.getY();
            String type=mouseEvent.getEventType().toString();
            if(type.equals("MOUSE_RELEASED")) {
                if(is_drag_going) {
                    upper_gc.clearRect(0,0,board_size,board_size);
                    is_drag_going=false;
                    drag_to_i = gc_i(x);
                    drag_to_j = gc_i(y);
                    // same square
                    if((drag_to_i==drag_from_i)&&(drag_to_j==drag_from_j)) {
                        drawBoard();
                        return;
                    }
                    // wrong turn
                    if(turn_of(orig_piece)!=turn) {
                        drawBoard();
                        return;
                    }
                    if((drag_to_i>=0)&&(drag_to_j>=0)&&(drag_to_i<=7)&&(drag_to_j<=7)){
                        drag_to_x = gc_x(drag_to_i);
                        drag_to_y = gc_y(drag_to_j);
                        makemove.source_X = drag_from_i;
                        makemove.source_Y = drag_from_j;
                        makemove.dest_X = drag_to_i;
                        makemove.dest_Y = drag_to_j;
                        makemove.piecePromotedTo = ' ';
                        try {
                            if(is_move_legal(makemove)) {
                                make_move_show(makemove);
                            }
                            else {
                                //Illegal move (?)
                                drawBoard();
                                return;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    else {
                        drawBoard();
                        return;
                    }
                }
            }
            if(type.equals("MOUSE_DRAGGED")) {
                if(is_drag_going) {
                    upper_gc.clearRect(0,0,board_size,board_size);
                    put_piece_xy(upper_gc,x+drag_dx,y+drag_dy,drag_piece);
                }
                else {
                    is_drag_going=true;
                    drag_from_i=gc_i(x);
                    drag_from_j=gc_j(y);
                    drag_from_x=gc_x(drag_from_i);
                    drag_from_y=gc_y(drag_from_j);
                    drag_dx=drag_from_x-x;
                    drag_dy=drag_from_y-y;
                    orig_drag_piece=fonts[drag_from_i][drag_from_j];
                    orig_piece=board[drag_from_i][drag_from_j];
                    drag_piece=(char)translit_light.get(orig_piece);
                    orig_empty= isDarkSqr(drag_from_i,drag_from_j)?'+':' ';
                    put_piece_xy(gc,drag_from_x,drag_from_y,orig_empty);
                }
            }
        }
    };

    private void put_piece_xy(GraphicsContext select_gc, int x, int y, char piece) {
        if(select_gc==gc) {
            select_gc.setFill(board_color);
            select_gc.fillRect(x, y, piece_size + padding, piece_size + padding);
        }
        select_gc.setFill(piece_color);
        select_gc.setFont(pieceFont);
        select_gc.fillText(Character.toString(piece), x,y + piece_size + padding);
    }

    /** Check if move M is legal on current position. **/
    private boolean is_move_legal(Move m) throws Exception {
        Boolean is_legal = false;
        String algeb = m.to_algeb_inner(false);
        init_move_generator();
        while((!is_legal)&&(next_pseudo_legal_move())) {
            String test_algeb=current_move.to_algeb_inner(false);
            if(test_algeb.equals(algeb)) {
                Board dummy = new Board(false);
                dummy.set_from_fen(getFEN());
                dummy.make_move(current_move);
                if(!dummy.inCheck(turn)) {
                    is_legal=true;
                }
            }
        }
        return is_legal;
    }

    private void make_move(Move current_move) {
    }

    private boolean next_pseudo_legal_move() {
        while(curr_j<8)
        {

            while(!move_table[move_gen_curr_ptr].end_piece)
            {

                MoveDescriptor md=move_table[move_gen_curr_ptr];

                int to_i=md.to_i;
                int to_j=md.to_j;

                char to_piece=board[to_i][to_j];

                int to_piece_code=code_of(to_piece);
                int to_piece_color=color_of(to_piece);

                current_move=new Move();

                current_move.source_X=curr_i;
                current_move.source_Y=curr_j;
                current_move.dest_X=to_i;
                current_move.dest_Y=to_j;
                current_move.piecePromotedTo=md.prom_piece;

                if(md.castling)
                {

                    move_gen_curr_ptr++;

                    if((curr_j==0)&&(to_i==6))
                    {
                        // black kingside
                        if(
                                (board[6][0]==' ')
                                        &&
                                        (board[5][0]==' ')
                                        &&
                                        (castling_rights.indexOf('k')>=0)
                                        &&
                                        (!is_square_in_check(4,0,BLACK))
                                        &&
                                        (!is_square_in_check(5,0,BLACK))
                        )
                        {
                            return true;
                        }
                    }

                    if((curr_j==0)&&(to_i==2))
                    {
                        // black queenside
                        if(
                                (board[3][0]==' ')
                                        &&
                                        (board[2][0]==' ')
                                        &&
                                        (board[1][0]==' ')
                                        &&
                                        (castling_rights.indexOf('q')>=0)
                                        &&
                                        (!is_square_in_check(4,0,BLACK))
                                        &&
                                        (!is_square_in_check(3,0,BLACK))
                        )
                        {
                            return true;
                        }
                    }

                    if((curr_j==7)&&(to_i==6))
                    {
                        // white kingside
                        if(
                                (board[6][7]==' ')
                                        &&
                                        (board[5][7]==' ')
                                        &&
                                        (castling_rights.indexOf('K')>=0)
                                        &&
                                        (!is_square_in_check(4,7,WHITE))
                                        &&
                                        (!is_square_in_check(5,7,WHITE))
                        )
                        {
                            return true;
                        }
                    }

                    if((curr_j==7)&&(to_i==2))
                    {
                        // white queenside
                        if(
                                (board[3][7]==' ')
                                        &&
                                        (board[2][7]==' ')
                                        &&
                                        (board[1][7]==' ')
                                        &&
                                        (castling_rights.indexOf('Q')>=0)
                                        &&
                                        (!is_square_in_check(4,7,WHITE))
                                        &&
                                        (!is_square_in_check(3,7,WHITE))
                        )
                        {
                            return true;
                        }
                    }

                }
                else if((to_piece!=' ')&&(to_piece_color==current_move_gen_piece_color))
                {

                    // own piece
                    if(is_current_move_gen_piece_sliding)
                    {
                        move_gen_curr_ptr=md.next_vector;
                    }
                    else
                    {
                        move_gen_curr_ptr++;
                    }
                }
                else
                {

                    Boolean is_capture=to_piece!=' ';

                    if(is_capture)
                    {

                        // capture
                        if(is_current_move_gen_piece_sliding)
                        {
                            move_gen_curr_ptr=md.next_vector;
                        }
                        else
                        {
                            move_gen_curr_ptr++;
                        }

                    }
                    else
                    {
                        move_gen_curr_ptr++;
                    }

                    if(current_move_gen_piece_type==PAWN)
                    {

                        if(curr_i!=to_i)
                        {
                            // sidewise move may be ep capture
                            String test_algeb = Move.squareHelper(to_i, to_j);
                            if(test_algeb.equals(ep_square_algeb))
                            {
                                is_capture=true;
                            }
                        }

                        if(is_capture)
                        {
                            // pawn captures only to the sides
                            if(curr_i!=to_i)
                            {
                                return true;
                            }
                        }
                        else
                        {
                            // pawn moves only straight ahead
                            if(curr_i==to_i)
                            {
                                if(Math.abs(to_j-curr_j)<2)
                                {
                                    // can always move one square forward
                                    return true;
                                }
                                else
                                {
                                    if(board[curr_i][curr_j+(to_j-curr_j)/2]==' ')
                                    {
                                        // push by two requires empty passing square
                                        return true;
                                    }
                                }
                            }
                        }
                    }
                    else {
                        return true;
                    }
                }
            }
            next_square(curr_i, curr_j);
        }
        return false;
    }
}