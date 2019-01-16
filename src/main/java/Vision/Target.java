package Vision;

import static org.junit.Assert.assertNotNull;

import javax.lang.model.util.ElementScanner6;

import org.opencv.core.*;

public class Target {
    HalfTarget left, right;
    Point center = new Point();
    HalfTarget largerTarget, smallerTarget;
    double differenceRatio;


    public static final double FOCAL_WIDTH = 284.75;
    public static final double TARGET_SEPERATION = 8;//inches

    public Target(HalfTarget l, HalfTarget r) {
        left = l;
        right = r;
        center.x = (left.center.x+right.center.x)/2;
        center.y = (left.center.y+left.center.y)/2;
        if(left.height > right.height)
        {
            largerTarget = left;
            smallerTarget = right;
        }
        else if(left.height < right.height)
        {
            largerTarget = right;
            smallerTarget = left;    
        }
        else
        {
            largerTarget = smallerTarget = null;
        }


        differenceRatio = (largerTarget != null)? (smallerTarget.height/largerTarget.height) : 1.00;
            
    }


    public double distanceFromRobot()
    {
        double dist;

        double pixDist = right.topLeft.x - left.topRight.x;

        dist = FOCAL_WIDTH*TARGET_SEPERATION/pixDist;
        
        return dist;
    }
}