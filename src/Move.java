public class Move {

    public int source_X, source_Y;
    public int dest_X, dest_Y;
    char piece;
    char piecePromotedTo; //Account for possible promotion. Usually null.

    public Move() {
        source_X = 0;
        source_Y = 0;
        dest_X = 0;
        dest_Y = 0;
        piece = ' ';
        piecePromotedTo = ' ';
    }

    /** Convert **/
    public String to_algeb() {
        return to_algeb_inner(true);
    }

    public String to_algeb_inner(boolean full) {
        String notation = "";
        notation += squareHelper(source_X, source_Y);
        notation += squareHelper(dest_X, dest_Y);
        if(promotionMove() && (full)) {
            notation += piecePromotedTo;
        }
        return notation;
    }

    /** Convert cartesian coordinates to algebraic notation. For example, (0,0) would map to "a1",
     * the bottom left square, and (7,7) would map to "h8", the top right square. **/
    public static String squareHelper(int i, int j) {
        String algeb = "";
        algeb += (char)(i+(int)'a');
        algeb += (char)((7-j)+(int)'1');
        return algeb;
    }

    /** Return true if move is a promotion or not. **/
    public boolean promotionMove() {
        return piecePromotedTo != ' ';
    }

    public void copy(Move m) {
        source_X = m.source_X;
        source_Y = m.source_Y;
        dest_X = m.dest_X;
        dest_Y = m.dest_Y;
        piece = m.piece;
        piecePromotedTo = m.piecePromotedTo;
    }

    /** Set a move object's attributes given algebraic notation.
     * @param moveNotation Move in algebraic notation, for example g1f3**/
    public void setMove(String moveNotation) {
        if (moveNotation.length() < 2) return;

        source_X = moveNotation.charAt(0) - 'a';
        source_Y = '8' - moveNotation.charAt(1);

        if (moveNotation.length() < 4) return;

        dest_X = moveNotation.charAt(2) - 'a';
        dest_Y = '8' - moveNotation.charAt(3);

        piecePromotedTo = ' ';

        if (moveNotation.length() >= 5) {
            piecePromotedTo = moveNotation.charAt(4);
        }
    }
}
