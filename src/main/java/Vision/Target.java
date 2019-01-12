package Vision;

import org.opencv.core.*;

public class Target {
    HalfTarget left, right;
    Point center = new Point();

    public Target(HalfTarget l, HalfTarget r) {
        left = l;
        right = r;
        center.x = (left.center.x+right.center.x)/2;
        center.y = (left.center.y+left.center.y)/2;
    }
}