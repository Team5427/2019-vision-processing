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
        url = new URL("http://169.254.101.224/axis-cgi/jpg/image.cgi");
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

    public static final int LEFT_COLOR = -15340065;  //Purple
    public static final int RIGHT_COLOR = -15418960; //Cyan
    
    public static ArrayList<Target> targetsInFrame;

    public void imageToContours(BufferedImage image) {
        pipeline.process(bufferedImageToMat(image));
        ArrayList<MatOfPoint> pointsList = pipeline.filterContoursOutput();
        Object[] contours = pointsList.toArray();
        ArrayList<HalfTarget> halfTargetsInFrame = new ArrayList<>();
        targetsInFrame = new ArrayList<>();
        BufferedImage contour = new BufferedImage(320, 240, BufferedImage.TYPE_4BYTE_ABGR);
        for (Object currentContour : contours) {
            Point[] points = ((MatOfPoint) currentContour).toArray();
            HalfTarget currentHalfTarget = new HalfTarget(points);
            if(currentHalfTarget.height<(currentHalfTarget.width*1.5))
                break;
            halfTargetsInFrame.add(currentHalfTarget);
            //All this loop does is draw the current points to the panel. No calculations
            for (Point p : points) {
                contour.setRGB((int) p.x, (int) p.y, (currentHalfTarget.side==TargetSide.Right)?RIGHT_COLOR:LEFT_COLOR);
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
        
        while(!(leftTargets.isEmpty()||rightTargets.isEmpty())) {
            HalfTarget leftmostLeftTarget = leftTargets.get(0);
            for(HalfTarget h : leftTargets) {
                if(h.center.x<leftmostLeftTarget.center.x&&h.side==TargetSide.Left)
                    leftmostLeftTarget = h;
            }
            HalfTarget leftmostRightTarget = rightTargets.get(0);
            while(leftmostRightTarget.center.x<leftmostLeftTarget.center.x) {
                rightTargets.remove(leftmostRightTarget);
                if(rightTargets.isEmpty())
                    return;
                leftmostRightTarget = rightTargets.get(0);
            }
            for(HalfTarget h : rightTargets) {
                if(h.center.x<leftmostRightTarget.center.x)
                    leftmostRightTarget = h;
            }
            
            targetsInFrame.add(new Target(leftmostLeftTarget, leftmostRightTarget));
            leftTargets.remove(leftmostLeftTarget);
            rightTargets.remove(leftmostRightTarget);
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
        // g.drawImage(contourImage, 0, 0, null);
        for(Target t:targetsInFrame)
        {
            HalfTarget left = t.left;
            HalfTarget right = t.right;
            g.setColor(new Color(this.LEFT_COLOR));
            for(Point p : left.points)
            {
                g.drawLine((int)p.x, (int)p.y, (int)p.x, (int)p.y);
            }
            g.setColor(new Color(this.RIGHT_COLOR));
            for(Point p : right.points)
            {
                g.drawLine((int)p.x, (int)p.y, (int)p.x, (int)p.y);
            }
            System.out.println(t.distanceFromRobot());
        }
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