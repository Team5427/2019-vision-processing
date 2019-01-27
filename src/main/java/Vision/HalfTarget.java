package Vision;

import org.opencv.core.*;

public class HalfTarget {
    /**
     * Identifies whether the HalfTarget is the left or right half of a full target
     */
    enum TargetSide {Left,Right}
    public TargetSide side;
    /**
     * All points condisered part of the HalfTarget - From GRIP contour
     */
    Point[] points;
    /**
     * Center of HalfTarget
     */
    public Point center = new Point();
    /**
     * Long edge of tape (pixels)
     */
    public double height;
    /**
     * Short edge of tape (pixels)
     */
    public double width;
    /**
     * The four corners of the tape (pixels)
     */
    public Point topRight, topLeft, bottomRight, bottomLeft;

    public HalfTarget(Point[] points) {
        this.points = points;
        Point leftmostPoint = points[0];
        Point rightmostPoint = points[0];
        Point topmostPoint = points[0];
        Point bottommostPoint = points[0];
        for (Point p : points) {
            if(p.x<leftmostPoint.x)
                leftmostPoint = p;
            else if(p.x>rightmostPoint.x)
                rightmostPoint = p;
            if(p.y<topmostPoint.y)
                topmostPoint = p;
            else if(p.y>bottommostPoint.y)
                bottommostPoint = p;
            }
            side = (leftmostPoint.y<rightmostPoint.y)?TargetSide.Right:TargetSide.Left;
            center.x = (leftmostPoint.x+rightmostPoint.x)/2;
            center.y = (topmostPoint.y+bottommostPoint.y)/2;
    
            if(side == TargetSide.Left)
            {
                height = Math.sqrt(Math.pow(bottommostPoint.y - rightmostPoint.y, 2) + Math.pow(bottommostPoint.x - rightmostPoint.x, 2));

                width = Math.sqrt(Math.pow(bottommostPoint.y - leftmostPoint.y, 2) + Math.pow(bottommostPoint.x - leftmostPoint.x, 2));
            }
            else if(side == TargetSide.Left)
            {
                width = Math.sqrt(Math.pow(bottommostPoint.y - rightmostPoint.y, 2) + Math.pow(bottommostPoint.x - rightmostPoint.x, 2));

                height = Math.sqrt(Math.pow(bottommostPoint.y - leftmostPoint.y, 2) + Math.pow(bottommostPoint.x - leftmostPoint.x, 2));
            }

            // double adjHeight = (side == TargetSide.Left)? bottommostPoint.y - rightmostPoint.y : leftmostPoint.y - bottommostPoint.y;
            // double adjWidth = (side == TargetSide.Left)? bottommostPoint.x - leftmostPoint.x : rightmostPoint.x - bottommostPoint.x;
            // height = Math.abs(adjHeight / Math.cos(Math.toRadians(ANGLE)));
            // width = Math.abs(adjWidth / Math.cos(Math.toRadians(ANGLE)));
            
            
        if(side==TargetSide.Left) {
            topRight = rightmostPoint;
            topLeft = topmostPoint;
            bottomLeft = leftmostPoint;
            bottomRight = bottommostPoint;
        } else if(side==TargetSide.Right) {
            topRight = topmostPoint;
            topLeft = leftmostPoint;
            bottomLeft = bottommostPoint;
            bottomRight = rightmostPoint;
        }
    }
}