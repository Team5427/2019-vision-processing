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

        //right.topLeft.x - left.topRight.x
        double diffX = right.topLeft.x - left.topLeft.x;
        double diffY = right.topLeft.y - left.topLeft.y;
        double pixDist = (right.topLeft.y != left.topLeft.y)? Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2)) : diffX;
        
        dist = FOCAL_WIDTH*TARGET_SEPERATION/pixDist;

        System.out.println("Left - ");
        System.out.println("\tWidth: "+left.width+ " Height: "+left.height);
        System.out.println("Right - ");
        System.out.println("\tWidth: "+right.width+ " Height: "+right.height);
        System.out.println("Distance between: " +pixDist);

        return dist;
    }
}