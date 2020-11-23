import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.rmi.NoSuchObjectException;
import java.util.Vector;

public class Ui {
    public static void main(String[] args) {
        JFrame frame = new JFrame("PaintIt");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //make menu
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("文件");
        JMenuItem loadFile = new JMenuItem("加载文件");
        JMenuItem saveFile = new JMenuItem("保存文件");

        fileMenu.add(loadFile);
        fileMenu.add(saveFile);
        menuBar.add(fileMenu);
        frame.setJMenuBar(menuBar);

        frame.setMinimumSize(new Dimension(800, 600));
        frame.setLayout(new BorderLayout(10, 5));

        DrawBoard drawBoard = new DrawBoard();
        History.drawBoard = drawBoard;
        Palette palette = new Palette(drawBoard);
        ActionMenu actionMenu = new ActionMenu(drawBoard, palette);
        actionMenu.setPreferredSize(new Dimension(800,70));

        frame.add(actionMenu, BorderLayout.NORTH);
        frame.add(drawBoard, BorderLayout.CENTER);
        frame.add(palette, BorderLayout.WEST);

        loadFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("绘图文件(.paint)", "paint"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.showDialog(frame, "加载文件");
                String filePath = fileChooser.getSelectedFile().getPath();
                try {
                    drawBoard.getShapes().removeAllElements();
                    FileModule.readFile(filePath, drawBoard.getShapes(), drawBoard);
                    drawBoard.repaint();
                    actionMenu.currentShape.updateData();
                } catch (FileNotFoundException exception) {
                    JOptionPane.showMessageDialog(frame, exception.getMessage());
                } catch (NullPointerException exception) {
                    return;
                }
            }
        });

        saveFile.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileFilter(new FileNameExtensionFilter("绘图文件(.paint)", "paint"));
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.showSaveDialog(frame);
                String filePath = fileChooser.getSelectedFile().getPath();
                if (!fileChooser.getFileFilter().getClass().equals(fileChooser.getAcceptAllFileFilter().getClass())
                        && !filePath.toUpperCase().endsWith("paint".toUpperCase())) {
                    filePath = filePath + ".paint";
                }
                try {
                    FileModule.saveFile(filePath, drawBoard.getShapes(), drawBoard);
                } catch (FileAlreadyExistsException exception) {
                    JOptionPane.showMessageDialog(frame, exception.getMessage());
                } catch (FileSystemException exception) {
                    JOptionPane.showMessageDialog(frame, exception.getMessage());
                } catch (NullPointerException exception) {
                    return;
                }
            }
        });

        frame.setVisible(true);
    }
}

class ActionMenu extends JPanel {
    private DrawBoard dwb;
    private Palette palette;
    private Vector<Shape> shapes;

    JButton btnCircle = new JButton("圆");
    JButton btnEllipse = new JButton("椭圆");
    JButton btnRect = new JButton("矩形");
    JButton btnLine = new JButton("直线");
    JButton btnCurve = new JButton("曲线");
    JButton btnEraser = new JButton("橡皮");
    JButton btnFill = new JButton("填充颜色模式");
    JButton btnUnFill = new JButton("去除填充");
    JButton btnAddText = new JButton("添加文字");
    JButton btnMove = new JButton("移动");
    JButton btnDelete = new JButton("删除");
    JButton btnUndo = new JButton("撤销");
    JLabel label = new JLabel("当前选中形状");
    JTextField textField = new JTextField();
    JComboBox<String> fontSelector = new JComboBox(new String[]{"宋体", "微软雅黑", "仿宋", "Times New Roman"});
    JComboBox<Integer> fontSizeSelector = new JComboBox<Integer>(new Integer[]{5,10,15,20,30,40,50,60,70,80,90});
    JComboBox<String> fontStyleSelector = new JComboBox<String>(new String[]{"PLAIN", "BOLD", "ITALIC"});

    Vector<JButton> buttons = new Vector<>();

    boolean doNotRecordHistory = false;

    public class UpdatableCombobox extends JComboBox {
        public void updateData() {
            this.removeAllItems();
            for(int i=0; i<shapes.size(); ++i) {
                this.addItem(i+":"+shapes.elementAt(i));
            }
        }

        @Override
        public void setSelectedIndex(int anIndex) {
            super.setSelectedIndex(anIndex);
            dwb.selectedShapeIndex = anIndex;
        }
    }

    UpdatableCombobox currentShape = new UpdatableCombobox();

    ActionMenu(DrawBoard drawBoard, Palette p) {
        dwb = drawBoard;
        palette = p;
        this.shapes = dwb.getShapes();
        dwb.setActionMenu(this);
        buttons.add(btnCircle);
        buttons.add(btnEllipse);
        buttons.add(btnRect);
        buttons.add(btnLine);
        buttons.add(btnCurve);
        buttons.add(btnFill);
        buttons.add(btnUnFill);
        buttons.add(btnAddText);
        buttons.add(btnEraser);
        buttons.add(btnMove);
        buttons.add(btnDelete);
        buttons.add(btnUndo);

        currentShape.setMinimumSize(new Dimension(200,25));
        currentShape.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                if (e.getStateChange() == ItemEvent.SELECTED) {
                    dwb.selectedShapeIndex = currentShape.getSelectedIndex();
                    System.out.println(currentShape.getSelectedIndex());
                    System.out.println(shapes.elementAt(currentShape.getSelectedIndex()).getClass());
                    if (shapes.elementAt(currentShape.getSelectedIndex()).getClass().toString().equals("class Text")) {
                        //TODO: add visible components
                        textField.setVisible(true);
                        fontSelector.setVisible(true);
                        fontSizeSelector.setVisible(true);
                        fontStyleSelector.setVisible(true);
                        doNotRecordHistory = true;
                        textField.setText(((Text)shapes.elementAt(currentShape.getSelectedIndex())).getText());
                        doNotRecordHistory = false;
                    } else {
                        textField.setVisible(false);
                        fontSelector.setVisible(false);
                        fontSizeSelector.setVisible(false);
                        fontStyleSelector.setVisible(false);
                    }
                } else {
                    textField.setVisible(false);
                    fontSelector.setVisible(false);
                    fontSizeSelector.setVisible(false);
                    fontStyleSelector.setVisible(false);
                }
            }
        });

        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
//                super.mouseClicked(e);
                dwb.unsetMoveMode();
                dwb.unsetEraserMode();
                if (e.getSource().equals(btnCircle)) {
                    System.out.println("circle");
                    dwb.setDrawMode(DrawBoard.DrawMode.WRITEABLE);
                    dwb.setDrawShape(DrawBoard.ShapeMode.CIRCLE);
                }
                else if (e.getSource().equals(btnEllipse)) {
                    System.out.println("ellipse");
                    dwb.setDrawMode(DrawBoard.DrawMode.WRITEABLE);
                    dwb.setDrawShape(DrawBoard.ShapeMode.ELLIPSE);
                }
                else if (e.getSource().equals(btnRect)) {
                    System.out.println("rect");
                    dwb.setDrawMode(DrawBoard.DrawMode.WRITEABLE);
                    dwb.setDrawShape(DrawBoard.ShapeMode.RECT);
                }
                else if (e.getSource().equals(btnLine)) {
                    System.out.println("line");
                    dwb.setDrawMode(DrawBoard.DrawMode.WRITEABLE);
                    dwb.setDrawShape(DrawBoard.ShapeMode.LINE);
                }
                else if (e.getSource().equals(btnCurve)) {
                    System.out.println("curve");
                    dwb.setDrawMode(DrawBoard.DrawMode.WRITEABLE);
                    dwb.setDrawShape(DrawBoard.ShapeMode.CURVE);
                }
                else if (e.getSource().equals(btnFill)) {
                    System.out.println("fill");
                    dwb.setDrawMode(DrawBoard.DrawMode.UNWRITEABLE);
                    if (palette.isFillMode) {
                        palette.setLineColorMode();
                        ((JButton)e.getSource()).setText("填充颜色模式");
                    } else {
                        palette.setFillColorMode();
                        ((JButton)e.getSource()).setText("线条颜色模式");
                    }
                }
                else if (e.getSource().equals(btnUnFill)) {
                    System.out.println("unfill");
                    dwb.setDrawMode(DrawBoard.DrawMode.UNWRITEABLE);
                    shapes.elementAt(dwb.selectedShapeIndex).bgColor = null;
                    dwb.repaint();
                }
                else if (e.getSource().equals(btnAddText)) {
                    System.out.println("text");
                    dwb.setDrawMode(DrawBoard.DrawMode.WRITEABLE);
                    dwb.setDrawShape(DrawBoard.ShapeMode.TEXT);
                }
                else if (e.getSource().equals(btnEraser)) {
                    System.out.println("eraser");
                    dwb.setDrawMode(DrawBoard.DrawMode.UNWRITEABLE);
                    dwb.setEraserMode();
                }
                else if (e.getSource().equals(btnMove)) {
                    System.out.println("move");
                    dwb.setDrawMode(DrawBoard.DrawMode.UNWRITEABLE);
                    dwb.setMoveMode();
                }
                else if (e.getSource().equals(btnDelete)) {
                    System.out.println("delete");
                    dwb.setDrawMode(DrawBoard.DrawMode.UNWRITEABLE);
                    if (dwb.delShapeAt(dwb.selectedShapeIndex)){
                        History.histories.add(new History(History.ActionMode.DELETE, shapes.elementAt(dwb.selectedShapeIndex)));
                    }
                    currentShape.updateData();
                }
                else if (e.getSource().equals(btnUndo)) {
                    System.out.println("undo");
                    dwb.setDrawMode(DrawBoard.DrawMode.UNWRITEABLE);
                    History.undo();
                    currentShape.updateData();
                    currentShape.setSelectedIndex(-1);
                }
                if (dwb.getCurrentDrawMode() == DrawBoard.DrawMode.WRITEABLE)
                    currentShape.setSelectedIndex(-1);
            }
        };

        {
            int left = 0;
            for (JButton btn : buttons) {
                btn.setBounds(left, 10, 80,25);
                this.add(btn);
                btn.addMouseListener(mouseAdapter);
                left += 100;
            }
        }

        this.add(label);
        this.add(currentShape);
        this.add(textField);
        this.add(fontSelector);
        this.add(fontSizeSelector);
        this.add(fontStyleSelector);
        textField.setPreferredSize(new Dimension(200,25));
        textField.setVisible(false);
        fontSelector.setVisible(false);
        fontSizeSelector.setVisible(false);
        fontStyleSelector.setVisible(false);

        textField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) {
                changedUpdate(e);
            }

            public void removeUpdate(DocumentEvent e) {
                changedUpdate(e);
            }


            public void changedUpdate(DocumentEvent e) {
                if (!doNotRecordHistory) {
                    System.out.println("changedUpdate");
                    History.histories.add(new History(History.ActionMode.TEXTCHANGE, shapes.elementAt(currentShape.getSelectedIndex()), ((Text)shapes.elementAt(currentShape.getSelectedIndex())).text));
                    ((Text)shapes.elementAt(currentShape.getSelectedIndex())).setText(textField.getText());
                    dwb.repaint();
                }
            }
        });

        fontSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ((Text)shapes.elementAt(currentShape.getSelectedIndex())).font = (String)fontSelector.getSelectedItem();
                dwb.repaint();
            }
        });

        fontSizeSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ((Text)shapes.elementAt(currentShape.getSelectedIndex())).size = (int)fontSizeSelector.getSelectedItem();
                dwb.repaint();
            }
        });

        fontStyleSelector.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                ((Text)shapes.elementAt(currentShape.getSelectedIndex())).style = fontStyleSelector.getSelectedIndex();
                dwb.repaint();
            }
        });
    }
}

class DrawBoard extends JPanel {
    DrawBoard() {
        this.setBackground(new Color(255,255,255));
        this.addMouseListener(dbDrawMouseAdapter);
        this.addMouseMotionListener(dbDrawMouseAdapter);
    }
    public enum ShapeMode {BLANK, CIRCLE, RECT, LINE, ELLIPSE, CURVE, TEXT}
    public enum DrawMode {WRITEABLE, UNWRITEABLE}

    public ActionMenu actionMenu;
    public int selectedShapeIndex = -1;
    public Color foregroundColor = new Color(0,0,0);
    private ShapeMode currentShapeMode = ShapeMode.BLANK;
    private DrawMode currentDrawMode = DrawMode.UNWRITEABLE;
    private int iShapeCount = 0;
    private final Vector<Shape> shapes = new Vector<>();

    private final MouseAdapter dbDrawMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            if (DrawBoard.this.currentDrawMode == DrawMode.UNWRITEABLE) return;
            else if (DrawBoard.this.currentShapeMode == ShapeMode.BLANK) return;
            else {
                ++iShapeCount;
                try {
                    shapes.add(addShape(DrawBoard.this.currentShapeMode, DrawBoard.this.foregroundColor));
                } catch (NoSuchObjectException exception) {
                    exception.printStackTrace();
                }
                Point mouseInitialLocation = e.getPoint();
                System.out.println("pressed");
                shapes.lastElement().setInitPoint(mouseInitialLocation);
                shapes.lastElement().setLastPoint(mouseInitialLocation);
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (DrawBoard.this.currentDrawMode == DrawMode.UNWRITEABLE) return;
            else if (DrawBoard.this.currentShapeMode == ShapeMode.BLANK) return;
            else {
                Point mouseLastLocation = e.getPoint();
                System.out.println("released");
                shapes.lastElement().setLastPoint(mouseLastLocation);
                shapes.lastElement().sortPoint();
                DrawBoard.this.repaint();
                DrawBoard.this.actionMenu.currentShape.updateData();
                DrawBoard.this.actionMenu.currentShape.setSelectedIndex(shapes.size()-1);
                History.histories.add(new History(History.ActionMode.CREATE, shapes.lastElement()));
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (DrawBoard.this.currentDrawMode == DrawMode.UNWRITEABLE) return;
            else if (DrawBoard.this.currentShapeMode == ShapeMode.BLANK) return;
            else {
                Point ptMouseLocation = e.getPoint();
//                System.out.println("dragged");
                shapes.lastElement().setLastPoint(ptMouseLocation);
            }
            DrawBoard.this.repaint();
        }
    };

    private final MouseAdapter dbMoveMouseAdapter = new MouseAdapter() {
        Point initPoint, lastPoint;

        @Override
        public void mousePressed(MouseEvent e) {
            initPoint = e.getPoint();
            lastPoint = e.getPoint();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point currentPoint = e.getPoint();
            System.out.println(lastPoint.x - currentPoint.x);
            if (selectedShapeIndex != -1) {
                shapes.elementAt(selectedShapeIndex).move(currentPoint.x- lastPoint.x, currentPoint.y- lastPoint.y);
            }
            DrawBoard.this.repaint();
            lastPoint = currentPoint;
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (selectedShapeIndex != -1) {
                int x = lastPoint.x - initPoint.x;
                int y = lastPoint.y - initPoint.y;
                History.histories.add(new History(History.ActionMode.MOVE, shapes.elementAt(selectedShapeIndex), new Point(x, y)));
            }
        }
    };

    private final MouseAdapter dbEraserMouseAdapter = new MouseAdapter() {
        @Override
        public void mouseReleased(MouseEvent e) {
            try {
                shapes.elementAt(selectedShapeIndex).maskRemoveRedundancy();
                DrawBoard.this.repaint();
            } catch (Exception exception) {
//                exception.printStackTrace();
            }
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            try {
                shapes.elementAt(selectedShapeIndex).addMask(e.getPoint(), false);
                DrawBoard.this.repaint();
            } catch (Exception exception) {
//                exception.printStackTrace();
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            try {
                History.histories.add(new History(History.ActionMode.ERASE, shapes.elementAt(selectedShapeIndex), shapes.elementAt(selectedShapeIndex).mask));
                shapes.elementAt(selectedShapeIndex).addMask(e.getPoint(), true);
                DrawBoard.this.repaint();
            } catch (Exception exception) {
//                exception.printStackTrace();
            }
        }
    };

    public Shape addShape(ShapeMode mode, Color color) throws NoSuchObjectException {
        Shape newShape = switch (mode) {
            case RECT -> new Rect(color, this);
            case LINE -> new Line(color, this);
            case CIRCLE -> new Circle(color, this);
            case ELLIPSE -> new Ellipse(color, this);
            case CURVE -> new Curve(color, this);
            case TEXT -> new Text(color, this);
            default -> throw new NoSuchObjectException("Shape Mode Error!");
        };
        return newShape;
    }

    public void setDrawMode(DrawMode drawMode) {
        switch (drawMode) {
            case WRITEABLE:
                currentDrawMode = DrawMode.WRITEABLE;
                break;
            case UNWRITEABLE:
                currentDrawMode = DrawMode.UNWRITEABLE;
                break;
            default:
                break;
        }
    }

    public DrawMode getCurrentDrawMode() {
        return currentDrawMode;
    }

    public void setDrawShape(ShapeMode shapeMode) {
        this.currentShapeMode = shapeMode;
    }

    public void delLastShape() {
        try {
            shapes.removeElementAt(iShapeCount-1);
            --iShapeCount;
            repaint();
            actionMenu.currentShape.updateData();
        } catch (Exception e) {
            return;
        }
    }

    public boolean delShapeAt(int index) {
        try {
            shapes.removeElementAt(index);
            --iShapeCount;
            repaint();
            actionMenu.currentShape.updateData();
            actionMenu.currentShape.setSelectedIndex(-1);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        for (Drawable drawable: shapes) {
            drawable.draw(g);
        }
    }

    public Vector<Shape> getShapes() {
        return shapes;
    }

    public void setActionMenu(ActionMenu actionMenu) {
        this.actionMenu = actionMenu;
    }

    public void setMoveMode() {
        this.addMouseListener(dbMoveMouseAdapter);
        this.addMouseMotionListener(dbMoveMouseAdapter);
    }

    public void unsetMoveMode() {
        this.removeMouseListener(dbMoveMouseAdapter);
        this.removeMouseMotionListener(dbMoveMouseAdapter);
    }

    public void setEraserMode() {
        this.addMouseListener(dbEraserMouseAdapter);
        this.addMouseMotionListener(dbEraserMouseAdapter);
    }

    public void unsetEraserMode() {
        this.removeMouseListener(dbEraserMouseAdapter);
        this.removeMouseMotionListener(dbEraserMouseAdapter);
    }
}

class Palette extends JPanel{
    DrawBoard dwb;
    public boolean isFillMode = false;

    private class ColorBlock extends JPanel {
        Color color;

        ColorBlock(Dimension size, Color color) {
            this.color = color;
            this.setPreferredSize(size);
            this.setBackground(color);
        }
    }

    MouseAdapter lineColorMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            ColorBlock colorBlock = (ColorBlock) e.getSource();
            dwb.foregroundColor = colorBlock.color;
            Palette.this.currentColorblk.color = colorBlock.color;
            Palette.this.currentColorblk.setBackground(colorBlock.color);
            Palette.this.currentColorblk.repaint();
            if (dwb.selectedShapeIndex != -1) {
                History.histories.add(new History(History.ActionMode.LINECOLOR, dwb.getShapes().elementAt(dwb.selectedShapeIndex), dwb.getShapes().elementAt(dwb.selectedShapeIndex).borderColor));
                dwb.getShapes().elementAt(dwb.selectedShapeIndex).borderColor = colorBlock.color;
            }
            dwb.repaint();
        }
    };

    MouseAdapter fillColorMouseAdapter = new MouseAdapter() {
        @Override
        public void mousePressed(MouseEvent e) {
            ColorBlock colorBlock = (ColorBlock) e.getSource();
            dwb.foregroundColor = colorBlock.color;
            Palette.this.currentColorblk.color = colorBlock.color;
            Palette.this.currentColorblk.setBackground(colorBlock.color);
            Palette.this.currentColorblk.repaint();
            if (dwb.selectedShapeIndex != -1) {
                History.histories.add(new History(History.ActionMode.FILLCOLOR, dwb.getShapes().elementAt(dwb.selectedShapeIndex), dwb.getShapes().elementAt(dwb.selectedShapeIndex).bgColor));
                dwb.getShapes().elementAt(dwb.selectedShapeIndex).bgColor = colorBlock.color;
            }
            dwb.repaint();
        }
    };

    ColorBlock colorblkBlack = new ColorBlock(new Dimension(30,30), Color.BLACK);
    ColorBlock colorblkRed = new ColorBlock(new Dimension(30,30), Color.RED);
    ColorBlock colorblkBlue = new ColorBlock(new Dimension(30,30), Color.BLUE);
    ColorBlock colorblkGreen = new ColorBlock(new Dimension(30,30), Color.GREEN);
    Vector<ColorBlock> colorBlocks = new Vector<>();
    JLabel label = new JLabel("当前颜色");
    ColorBlock currentColorblk = new ColorBlock(new Dimension(50,50), Color.BLACK);

    Palette(DrawBoard drawBoard) {
        this.dwb = drawBoard;
        this.setPreferredSize(new Dimension(60, 500));

        colorBlocks.add(colorblkBlack);
        colorBlocks.add(colorblkRed);
        colorBlocks.add(colorblkBlue);
        colorBlocks.add(colorblkGreen);

        for (ColorBlock colorBlock: colorBlocks) {
            this.add(colorBlock);
        }
        this.setLineColorMode();

        this.add(label);
        this.add(currentColorblk);
        System.out.println(colorblkBlack.getSize());
    }

    public void setFillColorMode() {
        for (ColorBlock colorBlock: colorBlocks) {
            colorBlock.removeMouseListener(lineColorMouseAdapter);
            colorBlock.addMouseListener(fillColorMouseAdapter);
        }
        isFillMode = true;
    }

    public void setLineColorMode() {
        for (ColorBlock colorBlock: colorBlocks) {
            colorBlock.removeMouseListener(fillColorMouseAdapter);
            colorBlock.addMouseListener(lineColorMouseAdapter);
        }
        isFillMode = false;
    }
}
