/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CYT_model;
import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
public abstract class Point2D implements Cloneable {
    private static final DecimalFormat formater = new DecimalFormat("#.000000"); 
    /**
     * The <code>Double</code> class defines a point specified in
     * <code>double</code> precision.
     * @since 1.2
     */
    public static class Double extends Point2D implements Serializable {
        public double x;
        public double y;
        
        public Double() {
            formater.setRoundingMode(RoundingMode.UP); //模式  四舍五入   
        }
        public Double(double x, double y) {
            this.x = x;
            this.y = y;
            formater.setRoundingMode(RoundingMode.HALF_UP); //模式  四舍五入    
            this.x = java.lang.Double.parseDouble(formater.format(this.x));
            this.y = java.lang.Double.parseDouble(formater.format(this.y));
        }
        @Override
        public double getX() {
            return x;
        }
        @Override
        public double getY() {
            return y;
        }
        @Override
        public void setLocation(double x, double y) {
            this.x = x;
            this.y = y;
            formater.setRoundingMode(RoundingMode.HALF_UP); //模式  四舍五入    
            this.x = java.lang.Double.parseDouble(formater.format(this.x));
            this.y = java.lang.Double.parseDouble(formater.format(this.y));
        }
        @Override
        public String toString() {
            return "["+x+", "+y+"] ";
        }
        private static final long serialVersionUID = 6150783262733311327L;

        @Override
        public Object clone() throws CloneNotSupportedException {
            return super.clone(); //To change body of generated methods, choose Tools | Templates.
        }
    }
    protected Point2D() {
    }
    public abstract double getX();
    public abstract double getY();
    public abstract void setLocation(double x, double y);
    public void setLocation(Point2D p) {
        setLocation(p.getX(), p.getY());
    }
//    public static double distanceSq(double x1, double y1,
//                                    double x2, double y2)
//    {
//        x1 -= x2;
//        y1 -= y2;
//        return (x1 * x1 + y1 * y1);
//    }
//    public static double distance(double x1, double y1,
//                                  double x2, double y2)
//    {
//        x1 -= x2;
//        y1 -= y2;
//        double res = Math.sqrt(x1 * x1 + y1 * y1);
//        return java.lang.Double.parseDouble(formater.format(res));
//    }
//    public double distanceSq(double px, double py) {
//        px -= getX();
//        py -= getY();
//        return (px * px + py * py);
//    }
//    public double distanceSq(Point2D pt) {
//        double px = pt.getX() - this.getX();
//        double py = pt.getY() - this.getY();
//        return (px * px + py * py);
//    }
//    public double distance(double px, double py) {
//        px -= getX();
//        py -= getY();
//        return Math.sqrt(px * px + py * py);
//    }
    public double distance(Point2D pt) {
        double px = pt.getX() - this.getX();
        double py = pt.getY() - this.getY();
        double res = Math.sqrt(px * px + py * py);
        formater.setRoundingMode(RoundingMode.HALF_UP); //模式  四舍五入    
        res = java.lang.Double.parseDouble(formater.format(res));
        return res;
        //return java.lang.Double.parseDouble(formater.format(res));
    }
    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            // this shouldn't happen, since we are Cloneable
            throw new InternalError(e);
        }
    }
    @Override
    public int hashCode() {
        long bits = java.lang.Double.doubleToLongBits(getX());
        bits ^= java.lang.Double.doubleToLongBits(getY()) * 31;
        return (((int) bits) ^ ((int) (bits >> 32)));
    }
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Point2D) {
            Point2D p2d = (Point2D) obj;
            return (this.distance(p2d) == 0);
        }
        return super.equals(obj);
    }
}

