package Vision;

import org.opencv.core.*;

public class HalfTarget {
    public static final double ANGLE = 14.5;

    enum TargetSide {Left,Right}
    Point[] points;
    public Point center = new Point();
    public TargetSide side;
    public double height;
    public double width;

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
            if(p.y>topmostPoint.y)
                topmostPoint = p;
            else if(p.y<bottommostPoint.y)
                bottommostPoint = p;
        }
        side = (leftmostPoint.y>rightmostPoint.y)?TargetSide.Right:TargetSide.Left;
        center.x = (leftmostPoint.x+rightmostPoint.x)/2;
        center.y = (topmostPoint.y+bottommostPoint.y)/2;

        double longerHeight = (side == TargetSide.Left)? bottommostPoint.y - rightmostPoint.y: (int)leftmostPoint.y - (int)bottommostPoint.y;
        double longerWidth = rightmostPoint.x - leftmostPoint.y;
        height = longerHeight / Math.cos(ANGLE);
        width = longerWidth / Math.cos(ANGLE);
        
        if(side==TargetSide.Left) {
            topRight = rightmostPoint;
            topLeft = topmostPoint;
            bottomLeft = leftmostPoint;
            bottomRight = bottommostPoint;
        } else {
            topRight = topmostPoint;
            topLeft = leftmostPoint;
            bottomLeft = bottommostPoint;
            bottomRight = rightmostPoint;
        }
    }
}