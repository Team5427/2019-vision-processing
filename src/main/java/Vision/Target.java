package Vision;



import org.opencv.core.*;

public class Target {

    /**
     * The left and right pieces of retroreflective tape
     */
    HalfTarget left, right;
    /**
     * The average of both halves' centers
     */
    Point center = new Point();
    
    public boolean isHatch;

    /**
     * From Manual - In inches
     */
    public static final double HEIGHT_HATCH         = 39.125;
    public static final double HEIGHT_BALL          = 31.5;
    public static final double HEIGHT_CAMERA        = 10;
    public static final double TARGET_SEPERATION    = 8;

    public Target(HalfTarget l, HalfTarget r, boolean isHatch) {
        left = l;
        right = r;
        this.isHatch = isHatch;
        center.x = (left.center.x+right.center.x)/2;
        center.y = (left.center.y+left.center.y)/2; 
    }

    public double getAvgWidth()
    {
        return (left.width+right.width)/2;
    }

    public double getAvgHeight()
    {
        return (left.height+right.height)/2;
    }

    public double getTapeDist()
    {
        double diffX = right.topLeft.x - left.topLeft.x;
        double diffY = right.topLeft.y - left.topLeft.y;
        double pixDist = (right.topLeft.y != left.topLeft.y)? Math.sqrt(Math.pow(diffX, 2) + Math.pow(diffY, 2)) : diffX;
        return pixDist;
    }

    public double getXOverZ() {
        return (center.x - GraphicsPanel.imageCenterX)/GraphicsPanel.focalLen;
    }
    public double getYOverZ() {
        return (center.y - GraphicsPanel.imageCenterY)/GraphicsPanel.focalLen;
    }
    public double getConstant3() {
        return getYOverZ()/getXOverZ();
    }
    public double getConstant4() {
        return 9.4; //inches, height of target - height of camera
    }
    public double solveForX() {
        return getConstant4()/getConstant3();
    }
    public double solveForZ() {
        //double d =  solveForX()/getXOverZ();
        return getConstant4()/(Math.tan(getVertAngle()));
    }
    public double getHorAngle() {
        double a =  Math.atan(getXOverZ());

        return a;
    }
    public double getVertAngle() {
        double a =  Math.atan(getYOverZ());

        return a;
    }


}