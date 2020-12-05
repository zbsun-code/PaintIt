import java.awt.*;
import java.util.Vector;

public class History {
    public static Vector<History> histories = new Vector<>();
    public static void setDrawBoard(DrawBoard drawBoard) {
        History.drawBoard = drawBoard;
    }
    public static DrawBoard drawBoard;
    public enum ActionMode {CREATE, ERASE, MOVE, DELETE, LINECOLOR, FILLCOLOR, TEXTCHANGE, LINESTROKE} //last2: change line color, fill/clean new color

    ActionMode actionMode = null;
    Shape srcShape = null;
    Vector<Point> mask = null;
    Point distance = null; //moved distance from src to dst
    Color srcColor = null; //before change color
    int srcLineWidth = -1; //before change stroke
    String srcText = null; //before change text
    String srcFont = null;
    int srcSize = -1;
    int srcStyle = -1;

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

    //for LINESTROKE
    History(ActionMode actionMode, Shape srcShape, int srcLineWidth) {
        this.actionMode = actionMode;
        this.srcShape = srcShape;
        this.srcLineWidth = srcLineWidth;
    }


    //for TEXTCHANGE
    History(ActionMode actionMode, Shape srcShape, String srcText, String srcFont, int srcSize, int srcStyle) {
        this.actionMode = actionMode;
        this.srcShape = srcShape;
        this.srcText = srcText;
        this.srcFont = srcFont;
        this.srcSize = srcSize;
        this.srcStyle = srcStyle;
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
            case LINESTROKE -> record.srcShape.setLineWidth(record.srcLineWidth);
            case TEXTCHANGE -> {
                if (record.srcText != null)
                    ((Text)record.srcShape).setText(record.srcText);
                if (record.srcFont != null)
                    ((Text)record.srcShape).font = record.srcFont;
                if (record.srcSize != -1)
                    ((Text)record.srcShape).size = record.srcSize;
                if (record.srcStyle != -1)
                    ((Text)record.srcShape).style = record.srcStyle;
            }
        }
        drawBoard.actionMenu.currentShape.setSelectedIndex(-1);
//      System.out.println(drawBoard.actionMenu.currentShape.getSelectedIndex());
        histories.remove(record);
        drawBoard.repaint();
    }
}
