import java.awt.image.*;
import java.io.File;

import javax.imageio.ImageIO;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;

public class Main
{
    public static void main(String[] args)throws Exception
    {
        System.out.print("Hello World!");
        GripPipeline pipeline = new GripPipeline();
        BufferedImage image = ImageIO.read(new File("Images/Sample Panel.png"));
        pipeline.process(bufferedImageToMat(image));
        //MatOfPoint[] points = (MatOfPoint[])(pipeline.filterContoursOutput().toArray());
    }
    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte)bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
      }
}