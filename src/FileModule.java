import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Vector;

public class FileModule {
    public static boolean saveFile(String filename, Vector<Shape> shapes, ImageObserver imageObserver) {
        try {
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            int shapeNum = shapes.size();
            out.writeInt(shapeNum);
            for (Shape shape: shapes) {
                shape.bi = null;
                shape.imageObserver = null;
                out.writeObject(shape);
                shape.bi = new BufferedImage(1600,1300,BufferedImage.TYPE_INT_ARGB);
                shape.imageObserver = imageObserver;
            }
            out.close();
            fileOut.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static boolean readFile(String filename, Vector<Shape> shapes, ImageObserver imageObserver) {
        try {
            FileInputStream fileIn = new FileInputStream(filename);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            int shapeNum = in.readInt();
            while ((--shapeNum) >= 0) {
                Shape newShape = (Shape)in.readObject();
                newShape.bi = new BufferedImage(1600,1300,BufferedImage.TYPE_INT_ARGB);
                newShape.imageObserver = imageObserver;
                shapes.add(newShape);
            }
            in.close();
            fileIn.close();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
