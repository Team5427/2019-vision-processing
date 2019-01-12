package Vision;

import org.opencv.core.*;

public class HalfTarget {
    enum TargetSide {Left,Right}
    Point[] points;
    public Point center = new Point();
    public TargetSide side;

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
    }
}