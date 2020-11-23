import java.awt.*;
import java.util.Vector;

public class History {
    //TODO: add more text edit history (font, style, size)
    public static Vector<History> histories = new Vector<>();
    public static void setDrawBoard(DrawBoard drawBoard) {
        History.drawBoard = drawBoard;
    }
    public static DrawBoard drawBoard;
    public enum ActionMode {CREATE, ERASE, MOVE, DELETE, LINECOLOR, FILLCOLOR, TEXTCHANGE} //last2: change line color, fill/clean new color

    ActionMode actionMode = null;
    Shape srcShape = null;
    Vector<Point> mask = null;
    Point distance = null; //moved distance from src to dst
    Color srcColor = null; //before change color
    String srcText = null; //before change text

    //for MOVE
    History(ActionMode actionMode, Shape srcShape, Point srcPosition) {
        this.actionMode = actionMode;
        this.srcShape = srcShape;
        this.distance = srcPosition;
    }

    //for CREATE and DELETE
    History(ActionMode actionMode, Shape srcShape) {
        this.actionMode = actionMode;
        this.srcShape = srcShape;
    }

    //for ERASE
    History(ActionMode actionMode, Shape srcShape, Vector<Point> mask) {
        this.actionMode = actionMode;
        this.srcShape = srcShape;
        this.mask = (Vector)mask.clone();
    }

    //for LINECOLOR and FILLCOLOR
    History(ActionMode actionMode, Shape srcShape, Color srcColor) {
        this.actionMode = actionMode;
        this.srcShape = srcShape;
        this.srcColor = srcColor;
    }


    //for TEXTCHANGE
    History(ActionMode actionMode, Shape srcShape, String srcText) {
        this.actionMode = actionMode;
        this.srcShape = srcShape;
        this.srcText = srcText;
    }

    public static void undo() {
        if (histories.isEmpty()) return;
        History record = histories.lastElement();
        switch (record.actionMode) {
            case CREATE -> drawBoard.getShapes().remove(record.srcShape);
            case MOVE -> record.srcShape.move(-record.distance.x, -record.distance.y);
            case DELETE -> drawBoard.getShapes().add(record.srcShape);
            case ERASE -> record.srcShape.mask = record.mask;
            case LINECOLOR -> record.srcShape.setBorderColor(record.srcColor);
            case FILLCOLOR -> record.srcShape.setBgColor(record.srcColor);
            case TEXTCHANGE -> {
                ((Text)record.srcShape).setText(record.srcText);
            }
        }
        drawBoard.actionMenu.currentShape.setSelectedIndex(-1);
        System.out.println(drawBoard.actionMenu.currentShape.getSelectedIndex());
        histories.remove(record);
        drawBoard.repaint();
    }
}
