import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.*;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.Vector;

public class FileModule {
    public static boolean saveFile(String filename, Vector<Shape> shapes, ImageObserver imageObserver) throws FileAlreadyExistsException, FileSystemException {
        File file = new File(filename);
        if (file.exists()) {
            throw new FileAlreadyExistsException("文件"+file.getPath()+"已存在！请修改保存路径！");
        }
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
            throw new FileSystemException("保存文件出错！请检查文件路径！");
        }
        return true;
    }

    public static boolean readFile(String filename, Vector<Shape> shapes, ImageObserver imageObserver) throws FileNotFoundException {
        String suffix = filename.substring(filename.lastIndexOf(".") + 1);
        if (!suffix.toUpperCase().equals("paint".toUpperCase())) {
            throw new FileNotFoundException("文件类型错误！");
        }
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
            throw new FileNotFoundException("读取文件失败，请检查错误！");
        }
        return true;
    }
}
