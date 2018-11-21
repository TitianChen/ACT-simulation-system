/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roadnet;

import CYT_model.Point2D;
import Vehicle.AGV;
import parameter.InputParameters;

/**
 * AGV缓冲区;
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 */
public class AGVBufferArea extends FunctionArea {
    private final double totalwidth;
    private final double length;
    private final Point2D[] parkingAreas;//停车位中心点位置;
    private final AGV[] haveCar;//停车位是否有车;
    private final AGV[] isBooked;//停车位是否被预定;
    private final double widthOfOneParking;//一个停车位的宽度;
    
    public AGVBufferArea(Point2D p1, Point2D p2,String areanum,String areaname)
    {
        super(p1,p2,areanum,areaname);
        //System.out.println("UUU11");
        super.P1.setLocation(super.P1.getX(),super.P1.getY());
        super.P2.setLocation(super.P2.getX(), super.P2.getY());
        length = Math.abs(p1.getX()-p2.getX());
        totalwidth = Math.abs(p1.getY()-p2.getY());
        widthOfOneParking = InputParameters.getSingleRoadWidth()*2;
        int parkingNum = (int)(totalwidth/widthOfOneParking);
        parkingAreas = new Point2D[parkingNum];
        haveCar = new AGV[parkingNum];
        isBooked = new AGV[parkingNum];
        for(int i = 0;i<parkingNum;i++){
            haveCar[i] = null;
            isBooked[i] = null;
        }
        parkingAreas[0] = new Point2D.Double(0.5*(p1.getX()+p2.getX()), p1.getY()+0.5*widthOfOneParking);
        parkingAreas[0].setLocation(parkingAreas[0].getX(), parkingAreas[0].getY());
        for (int i = 1;i<parkingAreas.length;i++) {
            parkingAreas[i] = new Point2D.Double(parkingAreas[i-1].getX(), parkingAreas[i-1].getY()+widthOfOneParking);
        }
    }

    /**
     * @return the length
     */
    public double getLength() {
        return length;
    }

    /**
     * @return the parkingAreas
     */
    public Point2D[] getParkingAreas() {
        return parkingAreas;
    }

    /**
     * @return the widthOfOneParking
     */
    public double getWidthOfOneParking() {
        return widthOfOneParking;
    }
    /**
     * 是否有空闲位置;
     * @return 
     */

    public boolean haveFreeParking(){
        for(int i = 0;i<parkingAreas.length;i++){
            if(isBooked[i] == null && haveCar[i] == null){
                return true;
            }
        }
        return false;
    }
    /**
     * @param agv
     * @return 
     */
    public Point2D getBookedArea(AGV agv){
        for(int i = 0;i<parkingAreas.length;i++){
            if(isBooked[i] != null && isBooked[i] == agv){
                return new Point2D.Double(parkingAreas[i].getX(),parkingAreas[i].getY());
            }
        }
        System.out.println("!!!!Error.getBooekdArea(AGV agv)!!!:");
        System.out.println("!!!!Error.getBooekdArea(AGV agv)!!!:");
        throw new UnsupportedOperationException("Error:Error:AGVBufferArea.getBooekdArea(AGV agv)!!!");
        
    }
    public Point2D bookedOneParking(AGV agv) {
        double distance = 2000;
        int num = -1;
        for(int i = 0;i<parkingAreas.length;i++){
            if((isBooked[i] != null && isBooked[i] == agv) ||
                    haveCar[i] != null && haveCar[i] == agv){
                System.out.println("!!!!Error!!!:"+agv+"i:"+i+isBooked[i]+haveCar[i]+agv.getAGVstate()+agv.getPresentPosition());
                System.out.println("!!!!Error!!!:"+parkingAreas[i].toString());
                throw new UnsupportedOperationException("Error:Error:AGVBufferArea.bookedOneParking(AGV agv)!!!");
            }
        }
        for(int i = 0;i<parkingAreas.length;i++){
            double d = agv.getServiceQC().getLocation().distance(parkingAreas[i]);
            //System.out.println("i:"+i+"d:"+d+" "+getIsBooked()[i]+" "+getHaveCar()[i]);
            if(isBooked[i] == null && haveCar[i] == null && d<distance){
                num = i;
                distance = d;
            }
        }
        if (num == -1) {
            System.out.println("!!!Error:AGVBufferArea.bookedOneParking(AGV agv)!!!");
            throw new UnsupportedOperationException("Error:Error:AGVBufferArea.bookedOneParking(AGV agv)!!!");
        }
        isBooked[num] = agv;
        return new Point2D.Double(parkingAreas[num].getX(),parkingAreas[num].getY());
    }

    public Point2D haveOneParking(AGV agv) {
        int num = -1;
        for(int i = 0;i<parkingAreas.length;i++){
            if(isBooked[i]!= null && isBooked[i] == agv){
                num = i;
                isBooked[num] = null;
                haveCar[num] = agv;
                return new Point2D.Double(parkingAreas[num].getX(),parkingAreas[num].getY());
            }
        }
        System.out.println("!!!Error:AGVBufferArea.bookedOneParking(AGV agv)!!!");
        throw new UnsupportedOperationException("Error:Error:AGVBufferArea.bookedOneParking(AGV agv)!!!");
    }

    public void releaseOneParking(AGV agv) {
        for(int i = 0;i<parkingAreas.length;i++){
            if(haveCar[i] != null && haveCar[i] == agv){
                this.haveCar[i] = null;
                if (isBooked[i] != null && isBooked[i] == agv) {
                    System.out.println("!!!Error:AGVBufferArea.releaseOneParking(AGV agv)!!!");
                    throw new UnsupportedOperationException("Error:Error:AGVBufferArea.releaseOneParking(AGV agv)!!!");
                }
                return;
            }
        }
        System.out.println("!!!Error:AGVBufferArea.releaseOneParking(point)!!!");
        throw new UnsupportedOperationException("Error:Error:AGVBufferArea.releaseOneParking(point)!!!");
    }

    /**
     * @return the haveCar
     */
    public AGV[] getHaveCar() {
        return haveCar;
    }

    /**
     * @return the isBooked
     */
    public AGV[] getIsBooked() {
        return isBooked;
    }

    public boolean isParking(AGV myself) {
        for (AGV havecar : haveCar) {
            if(havecar != null && havecar.equals(myself)){
                return true;
            }
        }
        return false;
    }

    public Point2D calculateNowPoint(Point2D startPoint, Point2D endPoint, AGV agv, double nowLength) {
        if (Math.abs(startPoint.getY() - endPoint.getY()) > 1) {
            System.out.println("!!!Error:AGVBufferArea.calculateNowPoint()!!!");
            System.out.println("!!!Error:!!!"+startPoint+" "+endPoint);
            throw new UnsupportedOperationException("Error:Error:AGVBufferArea.calculateNowPoint()!!!");
        }
        if(startPoint.getX() >= endPoint.getX()){
            return new Point2D.Double(startPoint.getX()-nowLength,startPoint.getY());
        }else{
            return new Point2D.Double(startPoint.getX()+nowLength,startPoint.getY());
        }
        
    }
}
