import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.Serializable;
import java.util.Vector;

public interface Drawable extends Serializable {
    void draw(Graphics g);
}

abstract class Shape implements Drawable {
    public Point initPoint; //location when mouse is pressed
    public Point lastPoint; //location when mouse is released
    public Color borderColor; //border color
    public Color bgColor; //background color
    public Vector<Point> mask; //eraser, different segment is separated by point(-1,-1)
    public int lineWidth = 2;
    BufferedImage bi = new BufferedImage(1600, 800, BufferedImage.TYPE_INT_ARGB);
    ImageObserver imageObserver;

    //move shape by dimension(x,y)
    public void move(int x, int y) {
        setInitPoint(new Point(initPoint.x+x, initPoint.y+y));
        setLastPoint(new Point(lastPoint.x+x, lastPoint.y+y));
        for (Point maskPoint: mask) {
            if (maskPoint.x > -9000 && maskPoint.y > -9000) {
                maskPoint.x = maskPoint.x+x;
                maskPoint.y = maskPoint.y+y;
            }
        }
    }

    public void setInitPoint(Point initPoint) {
        this.initPoint = initPoint;
    }

    public void setLastPoint(Point lastPoint) {
        this.lastPoint = lastPoint;
    }

    public void setBorderColor(Color borderColor) {
        this.borderColor = borderColor;
    }

    public void setBgColor(Color bgColor) {
        this.bgColor = bgColor;
    }

    public int getLineWidth() {
        return this.lineWidth;
    }

    public void setLineWidth(int lineWidth) {
        this.lineWidth = lineWidth;
    }

    Shape(Color borderColor, ImageObserver imageObserver) {
        this.borderColor = borderColor;
        this.bgColor = null;
        this.mask = new Vector<>();
        this.imageObserver = imageObserver;
    }

    //make initPoint to be the top left point (invalidate in Line)
    protected void sortPoint() {
        Point[] points = innerSortPoint(this.initPoint, this.lastPoint);
        this.initPoint = points[0];
        this.lastPoint = points[1];
    }

    private Point[] innerSortPoint(Point p1, Point p2) {
        Point realp1 = new Point(Math.min(p1.x, p2.x), Math.min(p1.y, p2.y));
        Point realp2 = new Point(Math.max(p1.x, p2.x), Math.max(p1.y, p2.y));
        return new Point[]{realp1, realp2};
    }

    protected void addMask(Point point, boolean isFirstPoint) {
        if (isFirstPoint)
            this.mask.add(new Point(-9999, -9999));
        this.mask.add(point);
    }

    protected void maskRemoveRedundancy() {
        //TODO: not finished
        mask.removeIf(maskPoint -> (!(maskPoint.x < -9000 && maskPoint.y < -9000) && (maskPoint.x < initPoint.x && maskPoint.y < initPoint.y) || (maskPoint.x > lastPoint.x && maskPoint.y > lastPoint.y)));
    }

    @Override
    public void draw(Graphics g) {
        for (int i=0; i<mask.size()-1; ++i) {
            Point maskPoint = mask.elementAt(i);
            Point nextPoint = mask.elementAt(i+1);
            if (maskPoint.x < -9000 || nextPoint.x < -9000) {
                continue;
            } else {
                Point[] points = innerSortPoint(maskPoint, nextPoint);
//                g.fillRect(points[0].x, points[0].y, points[1].x-points[0].x+5, points[1].y-points[0].y+5);
//                int rgb = bi.getRGB(maskPoint.x, maskPoint.y);
//                int rgb = ( (0 + 0) << 24) | (0 & 0x00ffffff);
//                if (points[0].x < 0) points[0].x = 0;
//                if (points[0].y < 0) points[0].y = 0;
//                if (points[1].x < 0) points[1].x = -6;
//                if (points[1].y < 0) points[1].y = -6;
//
//                for (int j=points[0].x; j<= points[1].x+5 && j>=0; ++j)
//                    for (int k= points[0].y; k<= points[1].y+5 && k>=0; ++k)
//                        bi.setRGB(j,k,rgb);
                Graphics2D ig2 = (Graphics2D)bi.getGraphics();
                ig2.setBackground(new Color(255,255,255,0));
                ig2.clearRect(points[0].x, points[0].y, points[1].x-points[0].x+5, points[1].y-points[0].y+5);
                ig2.dispose();
            }
        }
    }
}

class Circle extends Shape {
    Circle(Color borderColor, ImageObserver imageObserver) {
        super(borderColor, imageObserver);
    }

    @Override
    public void draw(Graphics g) {
//        bi = new BufferedImage(1600, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = bi.createGraphics();
        ig2.setBackground(new Color(255,255,255,0));
        ig2.clearRect(0,0,1600,800);
        ig2.setColor(this.borderColor);
        ig2.setStroke(new BasicStroke(this.lineWidth));
        ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,(RenderingHints.VALUE_ANTIALIAS_ON));
        ig2.drawOval(Math.min(initPoint.x, lastPoint.x), Math.min(initPoint.y, lastPoint.y), Math.abs(lastPoint.x - initPoint.x), Math.abs(lastPoint.x - initPoint.x));
        if (this.bgColor != null) {
            ig2.setColor(this.bgColor);
            ig2.fillOval(Math.min(initPoint.x, lastPoint.x)+1, Math.min(initPoint.y, lastPoint.y)+1, Math.abs(lastPoint.x - initPoint.x)-2, Math.abs(lastPoint.x - initPoint.x)-2);
        }
        super.draw(ig2);
        ig2.dispose();

//        g.setColor(this.borderColor);
//        g.drawOval(Math.min(initPoint.x, lastPoint.x), Math.min(initPoint.y, lastPoint.y), Math.abs(lastPoint.x - initPoint.x), Math.abs(lastPoint.x - initPoint.x));
//        if (this.bgColor != null) {
//            g.setColor(this.bgColor);
//            g.fillOval(Math.min(initPoint.x, lastPoint.x)+1, Math.min(initPoint.y, lastPoint.y)+1, Math.abs(lastPoint.x - initPoint.x)-2, Math.abs(lastPoint.x - initPoint.x)-2);
//        }
//        super.draw(g);

        g.drawImage(bi, 0, 0, bi.getWidth(), bi.getHeight(), imageObserver);
    }

    @Override
    public String toString() {
        return "圆"+super.toString();
    }
}

class Rect extends Shape {
    Rect(Color borderColor, ImageObserver imageObserver) {
        super(borderColor, imageObserver);
    }

    @Override
    public void draw(Graphics g) {
//        bi = new BufferedImage(1600, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = bi.createGraphics();
        ig2.setBackground(new Color(255,255,255,0));
        ig2.clearRect(0,0,1600,800);
        ig2.setColor(this.borderColor);
        ig2.setStroke(new BasicStroke(this.lineWidth));
        ig2.drawRect(Math.min(initPoint.x, lastPoint.x), Math.min(initPoint.y, lastPoint.y), Math.abs(lastPoint.x - initPoint.x), Math.abs(lastPoint.y - initPoint.y));
        if (this.bgColor != null) {
            ig2.setColor(this.bgColor);
            ig2.fillRect(Math.min(initPoint.x, lastPoint.x)+1, Math.min(initPoint.y, lastPoint.y)+1, Math.abs(lastPoint.x - initPoint.x)-2, Math.abs(lastPoint.y - initPoint.y)-2);
        }
        super.draw(ig2);
        ig2.dispose();
        g.drawImage(bi, 0, 0, bi.getWidth(), bi.getHeight(), imageObserver);
    }

    @Override
    public String toString() {
        return "矩形"+super.toString();
    }
}

class Line extends Shape {
    Line(Color borderColor, ImageObserver imageObserver) {
        super(borderColor, imageObserver);
    }

    @Override
    public void draw(Graphics g) {
//        bi = new BufferedImage(1600, 1000, BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = (Graphics2D) bi.getGraphics();
        ig2.setBackground(new Color(255,255,255,0));
        ig2.clearRect(0,0,1600,800);
        ig2.setColor(this.borderColor);
        ig2.setStroke(new BasicStroke(this.lineWidth));
        ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,(RenderingHints.VALUE_ANTIALIAS_ON));
        ig2.drawLine(initPoint.x, initPoint.y, lastPoint.x, lastPoint.y);
        super.draw(ig2);
        ig2.dispose();
        g.drawImage(bi, 0, 0, imageObserver);
    }

    @Override
    protected void sortPoint() {
        //do nothing
    }

    @Override
    public String toString() {
        return "直线"+super.toString();
    }
}

class Ellipse extends Shape {
    Ellipse(Color borderColor, ImageObserver imageObserver) {
        super(borderColor, imageObserver);
    }

    @Override
    public void draw(Graphics g) {
//        bi = new BufferedImage(1600,1000,BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = (Graphics2D)bi.getGraphics();
        ig2.setBackground(new Color(255,255,255,0));
        ig2.clearRect(0,0,1600,800);
        ig2.setColor(this.borderColor);
        ig2.setStroke(new BasicStroke(this.lineWidth));
        ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,(RenderingHints.VALUE_ANTIALIAS_ON));
        ig2.drawOval(Math.min(initPoint.x, lastPoint.x), Math.min(initPoint.y, lastPoint.y), Math.abs(lastPoint.x - initPoint.x), Math.abs(lastPoint.y - initPoint.y));
        if (this.bgColor != null) {
            ig2.setColor(this.bgColor);
            ig2.fillOval(Math.min(initPoint.x, lastPoint.x)+1, Math.min(initPoint.y, lastPoint.y)+1, Math.abs(lastPoint.x - initPoint.x)-2, Math.abs(lastPoint.y - initPoint.y)-2);
        }
        super.draw(ig2);
        ig2.dispose();
        g.drawImage(bi,0,0,imageObserver);
    }

    @Override
    public String toString() {
        return "椭圆"+super.toString();
    }
}

class Curve extends Shape {
    Vector<Point> points = new Vector<>();

    Curve(Color borderColor, ImageObserver imageObserver) {
        super(borderColor, imageObserver);
    }

    @Override
    public void setLastPoint(Point lastPoint) {
        super.setLastPoint(lastPoint);
        this.points.add((Point) lastPoint.clone());
    }

    @Override
    public void move(int x, int y) {
        setInitPoint(new Point(initPoint.x+x, initPoint.y+y));

        for (Point point: points) {
            point.x = point.x + x;
            point.y = point.y + y;
        }
    }

    @Override
    public void draw(Graphics g) {
//        bi = new BufferedImage(1600,1000,BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = (Graphics2D)bi.getGraphics();
        ig2.setBackground(new Color(255,255,255,0));
        ig2.clearRect(0,0,1600,800);
        ig2.setColor(this.borderColor);
        ig2.setStroke(new BasicStroke(this.lineWidth));
        ig2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,(RenderingHints.VALUE_ANTIALIAS_ON));
        for (int i=1; i<points.size(); ++i) {
            Point currentPoint = points.elementAt(i);
            Point pastPoint = points.elementAt(i-1);
            ig2.drawLine(pastPoint.x, pastPoint.y, currentPoint.x, currentPoint.y);
        }
        super.draw(ig2);
        ig2.dispose();
        g.drawImage(bi,0,0,imageObserver);
    }

    @Override
    public String toString() {
        return "曲线"+super.toString();
    }
}

class Text extends Shape {
    Text(Color borderColor, ImageObserver imageObserver) {
        super(borderColor, imageObserver);
        text = "文字";
    }

    String text;
    String font = "宋体";
    int size = 15;
    int style = Font.PLAIN;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return "文字"+super.toString();
    }

    @Override
    public void setLastPoint(Point lastPoint) {
        super.setLastPoint(lastPoint);
    }

    @Override
    public void draw(Graphics g) {
//        bi = new BufferedImage(1600,1000,BufferedImage.TYPE_INT_ARGB);
        Graphics2D ig2 = (Graphics2D)bi.getGraphics();
        ig2.setBackground(new Color(255,255,255,0));
        ig2.clearRect(0,0,1600,800);
        ig2.setColor(borderColor);
        ig2.setFont(new Font(font, style, size));
        ig2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,(RenderingHints.VALUE_TEXT_ANTIALIAS_GASP));
        ig2.drawString(text, initPoint.x, initPoint.y);
        super.draw(ig2);
        ig2.dispose();
        g.drawImage(bi,0,0,imageObserver);
    }
}
