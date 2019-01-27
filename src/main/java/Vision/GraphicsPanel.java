package Vision;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.opencv.core.*;
import Networking.Server;
import Vision.HalfTarget.TargetSide;

public class GraphicsPanel extends JPanel implements Runnable {

    private static final long serialVersionUID = 1L;
    /**
     * Stores the initial image from the axis camera.
     */
    BufferedImage image;

    /**
     * An array that contains all of the contours in the image.
     */
    MatOfPoint[] contours;

    /**
     * An image of the contours that are printed to the screen.
     */
    BufferedImage contourImage;

    /**
     * An array list that stores every vision target that appears on-screen.
     */
    public static ArrayList<Target> targetsInFrame;
    
    public static ArrayList<HalfTarget> badTargets;
    public static ArrayList<HalfTarget> allTargets;

    /**
     * The filters that the image goes through to find the contours of the
     * target(s).
     */
    GripPipeline pipeline = new GripPipeline();

    /**
     * The url that connects to the Axis Camera.
     */
    URL url;

    /**
     * The focal length of the camera.
     */
    public static double focalLen;

    /**
     * The x value of the center of the image.
     */
    public static double imageCenterX;

    /**
     * The y value of the center of the image.
     */
    public static double imageCenterY;

    /**
     * The Field of View of the Axis Camera.
     */
    public static final double FOV = 67f;

    /**
     * The ratio of the average half target width to the pixel distance between the
     * two half targets
     */
    public static final double WIDTH_PIX_RATIO = 0.17;

    /**
     * The ratio of the average half target height to the pixel distance between the
     * two half targets
     */
    public static final double HEIGHT_PIX_RATIO = 0.52;

    /**
     * The tolerance, in pixels, for the calculated distance between the two vision
     * targets.
     */
    public static final double DISTANCE_TOLERANCE = 25;


    public GraphicsPanel(int w, int h) {
        super();
        setSize(w, h);
        // try {
        //     this.image = ImageIO.read(new File("Images/VisionImages2019/RocketPanelStraightDark24in.jpg"));

        // } catch (Exception e) {
        //     e.printStackTrace();
        // }
        // imageToContours(image);
        // repaint();
        Server.start();
        try{
        url = new URL("http://10.54.27.62/axis-cgi/jpg/image.cgi");
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

            focalLen = image.getWidth()/(2*Math.tan(Math.toRadians(FOV/2)));
            imageCenterX = image.getWidth()/2 - 0.5;
            imageCenterY = image.getHeight()/2 - 0.5;

            repaint();
            }catch(Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * The rgb colors used for the vision targets
     */
    public static final int LEFT_COLOR = -15340065;  //Purple
    public static final int RIGHT_COLOR = -15418960; //Cyan

    public void imageToContours(BufferedImage image) {

        // Converts the image from the Axis Camera to an array of contours.
        pipeline.process(bufferedImageToMat(image));
        ArrayList<MatOfPoint> pointsList = pipeline.filterContoursOutput();
        Object[] contours = pointsList.toArray();

        // The array list that stores all of the possible half targets
        ArrayList<HalfTarget> halfTargetsInFrame = new ArrayList<>();

        // Initializes the array list for targets.
        targetsInFrame = new ArrayList<>();
        badTargets = new ArrayList<>();
        allTargets = new ArrayList<>();

        BufferedImage contour = new BufferedImage(800, 600, BufferedImage.TYPE_4BYTE_ABGR);
        for (Object currentContour : contours) {

            // Stores all the points in the current contour.
            Point[] points = ((MatOfPoint) currentContour).toArray();

            // Creates a half target based on the array of points.
            HalfTarget currentHalfTarget = new HalfTarget(points);
            if(currentHalfTarget.height<(currentHalfTarget.width*2)||currentHalfTarget.height>(currentHalfTarget.width*4))
            {
                badTargets.add(currentHalfTarget);
                // System.out.println(currentHalfTarget.height/currentHalfTarget.width);
                // System.out.println("Invalid Halftarget!!!");
                continue;
            }
            halfTargetsInFrame.add(currentHalfTarget);
            // Adds the half target to the list of all half targets
        }

        // Splits the half targets into left and right targets.
        ArrayList<HalfTarget> leftTargets = (ArrayList<HalfTarget>) halfTargetsInFrame.clone();
        for (int x = 0; x < leftTargets.size(); x++) {
            if (leftTargets.get(x).side == TargetSide.Right) {
                leftTargets.remove(x);
                x--;
            }
        }
        ArrayList<HalfTarget> rightTargets = (ArrayList<HalfTarget>) halfTargetsInFrame.clone();
        for (int x = 0; x < rightTargets.size(); x++) {
            if (rightTargets.get(x).side == TargetSide.Left) {
                rightTargets.remove(x);
                x--;
            }
        }

        // Finds the left and right half targets that combine into a single vision
        // target from left to right, and gets rid of invalid targets.
        while (!(leftTargets.isEmpty() || rightTargets.isEmpty())) {

            // Finds the leftmost target that is also a left target.
            HalfTarget leftmostLeftTarget = leftTargets.get(0);
            for (HalfTarget h : leftTargets) {
                if (h.center.x < leftmostLeftTarget.center.x && h.side == TargetSide.Left)
                    leftmostLeftTarget = h;
            }

            HalfTarget leftmostRightTarget = rightTargets.get(0);
            while(leftmostRightTarget.center.x<leftmostLeftTarget.center.x) {
                allTargets.add(leftmostRightTarget);
                rightTargets.remove(leftmostRightTarget);
                if (rightTargets.isEmpty())
                    return;
                leftmostRightTarget = rightTargets.get(0);
            }

            // Finds the leftmost target that is also a right target.
            for (HalfTarget h : rightTargets) {
                if (h.center.x < leftmostRightTarget.center.x)
                    leftmostRightTarget = h;
            }

            // Checks if the target is a valid one.
            Target t = new Target(leftmostLeftTarget, leftmostRightTarget, true);
            if(isValidTarget(t)>0) {
                allTargets.add(leftmostLeftTarget);
                leftTargets.remove(leftmostLeftTarget);
            }
            else if(isValidTarget(t)<0) {
                allTargets.add(leftmostRightTarget);
                rightTargets.remove(leftmostRightTarget);
            }
            else {
                targetsInFrame.add(t);
                //System.out.println("\t\t" + targetOffset(t));
                leftTargets.remove(leftmostLeftTarget);
                rightTargets.remove(leftmostRightTarget);
            }

            double d = -t.solveForZ();
            double aH = -t.getHorAngle();
            double aV = -t.getVertAngle();

            System.out.println("******* z: "+d+"*************************");
            System.out.println("******* aH: "+Math.toDegrees(aH)+"*************************");
            System.out.println("******* aV: "+Math.toDegrees(aV)+"*************************\n\n");

            Server.send(d+" "+aH);
        }
    }

    public static final int tolerance = 10;

    public double targetOffset(Target t) {
        double offset = t.center.x - 160; // in pixels;

        return offset;
    }

    //returns -1 if too close, 1 if too far, 0 if valid target
    public int isValidTarget(Target t)
    {

        double idealTapeDist = ((t.getAvgHeight()/HEIGHT_PIX_RATIO)+(t.getAvgWidth()/WIDTH_PIX_RATIO))/2;
        if(Math.abs(idealTapeDist-t.getTapeDist())>DISTANCE_TOLERANCE)
            return 1;
        else if(Math.abs(idealTapeDist-t.getTapeDist())>DISTANCE_TOLERANCE)
            return -1;
        else
            return 0;
    }

    @Override
    public void paint(Graphics g) {
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(Color.RED);
        g.drawLine(0, 300, 800, 300);
        g.drawLine(400, 0, 400, 600);
        // g.drawImage(contourImage, 0, 0, null);
        for (Target t : targetsInFrame) {
            HalfTarget left = t.left;
            HalfTarget right = t.right;
            for (Point p : left.points) {
                g.setColor(new Color(GraphicsPanel.LEFT_COLOR));
                g.drawLine((int) p.x, (int) p.y, (int) p.x, (int) p.y);
                g.setColor(Color.BLACK);
                if (left.topLeft.x == p.x && left.topLeft.y == p.y)
                    g.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);
                if (left.topRight.x == p.x && left.topRight.y == p.y)
                    g.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);
                if (left.bottomLeft.x == p.x && left.bottomLeft.y == p.y)
                    g.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);
                if (left.bottomRight.x == p.x && left.bottomRight.y == p.y)
                    g.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);

            }
            for (Point p : right.points) {
                g.setColor(new Color(GraphicsPanel.RIGHT_COLOR));
                g.drawLine((int) p.x, (int) p.y, (int) p.x, (int) p.y);
                g.setColor(Color.BLACK);
                if (right.topLeft.x == p.x && right.topLeft.y == p.y)
                    g.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);
                if (right.topRight.x == p.x && right.topRight.y == p.y)
                    g.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);
                if (right.bottomLeft.x == p.x && right.bottomLeft.y == p.y)
                    g.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);
                if (right.bottomRight.x == p.x && right.bottomRight.y == p.y)
                    g.fillOval((int) p.x - 2, (int) p.y - 2, 4, 4);
            }
            // System.out.println("DifferenceRatio: "+t.differenceRatio);
        }

        for(HalfTarget h:badTargets)
        {
            for(Point p : h.points)
            {
                g.setColor(Color.RED);
                g.drawLine((int)p.x, (int)p.y, (int)p.x, (int)p.y);
                g.setColor(Color.BLACK);
                if(h.topLeft.x == p.x && h.topLeft.y == p.y)
                    g.fillOval((int)p.x-2, (int)p.y-2, 4, 4);
                if(h.topRight.x == p.x && h.topRight.y == p.y)
                    g.fillOval((int)p.x-2, (int)p.y-2, 4, 4);
                if(h.bottomLeft.x == p.x && h.bottomLeft.y == p.y)
                    g.fillOval((int)p.x-2, (int)p.y-2, 4, 4);
                if(h.bottomRight.x == p.x && h.bottomRight.y == p.y)
                    g.fillOval((int)p.x-2, (int)p.y-2, 4, 4);
            }
        }
        for(HalfTarget h:allTargets)
        {
            for(Point p : h.points)
            {
                g.setColor(Color.ORANGE);
                g.drawLine((int)p.x, (int)p.y, (int)p.x, (int)p.y);
                g.setColor(Color.BLACK);
                if(h.topLeft.x == p.x && h.topLeft.y == p.y)
                    g.fillOval((int)p.x-2, (int)p.y-2, 4, 4);
                if(h.topRight.x == p.x && h.topRight.y == p.y)
                    g.fillOval((int)p.x-2, (int)p.y-2, 4, 4);
                if(h.bottomLeft.x == p.x && h.bottomLeft.y == p.y)
                    g.fillOval((int)p.x-2, (int)p.y-2, 4, 4);
                if(h.bottomRight.x == p.x && h.bottomRight.y == p.y)
                    g.fillOval((int)p.x-2, (int)p.y-2, 4, 4);
            }
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