package Vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.util.ArrayList;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.opencv.core.*;

import Vision.HalfTarget.TargetSide;

public class GraphicsPanel extends JPanel implements Runnable {

    BufferedImage image;
    MatOfPoint[] contours;
    BufferedImage contourImage;
    GripPipeline pipeline = new GripPipeline();
    URL url;

    public GraphicsPanel(int w, int h) {
        super();
        setSize(w, h);
        try {
            this.image = ImageIO.read(new File("Images/VisionImages2019/RocketPanelStraightDark24in.jpg"));

        } catch (Exception e) {
            e.printStackTrace();
        }
        imageToContours(image);
        repaint();
        try{
        url = new URL("http://169.254.101.225/axis-cgi/jpg/image.cgi");
        }catch(Exception e) {
            e.printStackTrace();
        }
        new Thread(this).start();
    }

    public void run() {
        while(true) {
            try{
            Thread.sleep(1000/20);
            image = ImageIO.read(url);
            imageToContours(image);
            repaint();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static final int LeftColor = -15340065;  //Purple
    public static final int RightColor = -2222610;  //Cyan

    public void imageToContours(BufferedImage image) {
        pipeline.process(bufferedImageToMat(image));
        ArrayList<MatOfPoint> pointsList = pipeline.filterContoursOutput();
        Object[] contours = pointsList.toArray();
        ArrayList<HalfTarget> halfTargetsInFrame = new ArrayList<>();

        BufferedImage contour = new BufferedImage(320, 240, BufferedImage.TYPE_4BYTE_ABGR);
        for (Object currentContour : contours) {
            Point[] points = ((MatOfPoint) currentContour).toArray();
            HalfTarget currentHalfTarget = new HalfTarget(points);
            halfTargetsInFrame.add(currentHalfTarget);
            //All this loop does is draw the current points to the panel. No calculations
            for (Point p : points) {
                contour.setRGB((int) p.x, (int) p.y, (currentHalfTarget.side==TargetSide.Right)?RightColor:LeftColor);
            }
        }

        ArrayList<HalfTarget> leftTargets = (ArrayList<HalfTarget>)halfTargetsInFrame.clone();
        for(int x = 0;x<leftTargets.size();x++) {
            if(leftTargets.get(x).side==TargetSide.Right) {
                leftTargets.remove(x);
                x--;
            }
        }
        ArrayList<HalfTarget> rightTargets = (ArrayList<HalfTarget>)halfTargetsInFrame.clone();
        for(int x = 0;x<rightTargets.size();x++) {
            if(rightTargets.get(x).side==TargetSide.Left) {
                rightTargets.remove(x);
                x--;
            }
        }

        if(leftTargets.isEmpty())
            return;
        HalfTarget leftmostLeftTarget = leftTargets.get(0);
        for(HalfTarget h : leftTargets) {
            if(h.center.x<leftmostLeftTarget.center.x&&h.side==TargetSide.Left)
                leftmostLeftTarget = h;
        }

        this.contours = new MatOfPoint[contours.length];
        this.contourImage = contour;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.RED);
        g.drawLine(0, 120, 320, 120);
        g.drawLine(160, 0, 160, 240);
        g.drawImage(contourImage, 0, 0, null);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocus();
    }

    public static Mat bufferedImageToMat(BufferedImage bi) {
        Mat mat = new Mat(bi.getHeight(), bi.getWidth(), CvType.CV_8UC3);
        byte[] data = ((DataBufferByte) bi.getRaster().getDataBuffer()).getData();
        mat.put(0, 0, data);
        return mat;
    }
}