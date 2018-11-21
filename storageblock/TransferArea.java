/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storageblock;

import CYT_model.Point2D;
import Vehicle.AGV;
import Vehicle.VehicleInterface;
import parameter.InputParameters;
import roadnet.Rectangle;

/**
 *
 * @author Administrator
 */
public class TransferArea extends Rectangle{
    private final Block block;
    private final String areaName;
    private final Point2D transferPoint1;
    private final Point2D transferPoint2;
    private final Point2D transferPoint3;
    private boolean transPoint1IsBooked = false;
    private boolean transPoint2IsBooked = false;
    private boolean transPoint3IsBooked = false;
    private VehicleInterface car1 = null;
    private VehicleInterface car2 = null;
    private VehicleInterface car3 = null;//waterSide里此处有AGV伴侣,下面可以有AGV;
    
    public TransferArea(Point2D p1, Point2D p2, String areaName,Block block) {
        super(p1, p2);
        this.areaName = areaName;
        this.block = block;
        this.transferPoint1 = new Point2D.Double(getP1().getX()+InputParameters.getRMGtrackGuage()/6, 
                0.5*(getP1().getY()+getP2().getY()));
        this.transferPoint2 = new Point2D.Double(getP1().getX()+InputParameters.getRMGtrackGuage()/2, 
                0.5*(getP1().getY()+getP2().getY()));
        this.transferPoint3 = new Point2D.Double(getP2().getX()-InputParameters.getRMGtrackGuage()/6, 
                0.5*(getP1().getY()+getP2().getY()));
    }
    @Override
    public final Point2D getP1(){
        return this.P1;
    }
    @Override
    public final Point2D getP2(){
        return this.P2;
    }

    /**
     * @return the car1
     */
    public VehicleInterface getCar1() {
        return car1;
    }

    /**
     * @return the car2
     */
    public VehicleInterface getCar2() {
        return car2;
    }

    /**
     * @return the car3
     */
    public VehicleInterface getCar3() {
        return car3;
    }
    /**
     * 优先级：Point3>Point2>Point1
     * @param agv
     * @return 返回空闲的装卸点;若没有空闲，则返回null;
     */
    public Point2D getOneFreeTransferPoint(AGV agv){
        if(this.getCar3() == null && this.isTransPoint3IsBooked() == false){
            //配备有AGV伴侣的装卸点;
            if(agv.getContainerNum() == 0){
                //AGV-Loading;
                if(this.block.getAGVCouple()[0].haveContainers() == false){
                    return this.getTransferPoint3();
                }
            }else{
                //AGV-Unloading;
                if(this.block.getAGVCouple()[0].haveContainers() == false){
                    return this.getTransferPoint3();
                }
            }
        }
        if(this.getCar2() == null && this.isTransPoint2IsBooked() == false){
            //配备有AGV伴侣的装卸点;
            if(agv.getContainerNum() == 0){
                //AGV-Loading;
                if(this.block.getAGVCouple()[1].haveContainers() == false){
                    return this.getTransferPoint2();
                }
            }else{
                //AGV-Unloading;
                if(this.block.getAGVCouple()[1].haveContainers() == false){
                    return this.getTransferPoint2();
                }
            }
        }
        if(this.getCar1() == null && this.isTransPoint1IsBooked() == false){
            return this.getTransferPoint1();
        }
        return null;
    }
    /**
     * 优先级：Point1<Point2<Point3
     * @param agv
     * @return 返回空闲的装卸点;若没有空闲，则返回null;
     */
    public Point2D getAndBookOneFreeTransferPoint(AGV agv){  
        if(this.isTransPoint3IsBooked() == false && this.getCar3() == null){
            //配备有AGV伴侣的装卸点;
            if(agv.getContainerNum() == 0){
                //AGV-Loading;
                if(this.block.getAGVCouple()[0].haveContainers() == false){
                    if(this.setTransPoint3Booked(true, agv) == true){
//System.out.println(this.areaName + "Loading--getAndBook--Car3" + agv.toString());
                        return this.getTransferPoint3();
                    }
//System.out.println("false:---"+this.areaName + "Loading--getAndBook--Car3" + agv.toString());
                }
            }else{
                //AGV-Unloading;
                if(this.block.getAGVCouple()[0].haveContainers() == false){
                    if(this.setTransPoint3Booked(true,agv) == true){
//System.out.println(this.areaName + "Unloading--getAndBook--Car3" + agv.toString());
                        return this.getTransferPoint3();
                    }
//System.out.println("false:---"+this.areaName + "UnLoading--getAndBook--Car3" + agv.toString());
                }
            }
        }
        if(this.isTransPoint2IsBooked() == false && this.getCar2() == null){
            if(agv.getContainerNum() == 0){
                //AGV-Loading;
                if(this.block.getAGVCouple()[1].haveContainers() == false){
                    if(this.setTransPoint2Booked(true, agv) == true){
//System.out.println(this.areaName + "Loading--getAndBook--Car2" + agv.toString());
                        return this.getTransferPoint2();
                    }
//System.out.println("false:---"+this.areaName + "Loading--getAndBook--Car2" + agv.toString());
                }
            }else{
                //AGV-Unloading;
                if(this.block.getAGVCouple()[1].haveContainers() == false){
                    if(this.setTransPoint2Booked(true,agv) == true){
//System.out.println(this.areaName + "Unloading--getAndBook--Car2" + agv.toString());
                        return this.getTransferPoint2();
                    }
//System.out.println("false:---"+this.areaName + "UnLoading--getAndBook--Car2" + agv.toString());
                }
            }
        }
        if(this.getCar1() == null && this.isTransPoint1IsBooked() == false){
            if (this.setTransPoint1Booked(true, agv) == true) {
//System.out.println(this.areaName + "Unloading--getAndBook--Car1" + agv.toString());
                return this.getTransferPoint1();
            }
//System.out.println("false:---"+this.areaName + "Unloading--getAndBook--Car1" + agv.toString());
        }
        return new Point2D.Double(0,0);
    }
    public boolean obtainThisTransferPoint(VehicleInterface car){
        if(this.getTransferPoint1().equals(car.getPresentPosition()) == true){
            if(this.getCar1() == null){
                this.setCar1(car);
                this.transPoint1IsBooked = false;
                return true;
            }else{
                System.out.println(this.areaName+"!!Error:obtainThisTransferPoint1"+car.toString());
                throw new UnsupportedOperationException("!!!!!Error:TransferArea.obtainThisTransferPoint(car)"); 
            }
        }else if(this.getTransferPoint2().equals(car.getPresentPosition()) == true){
            if(this.getCar2() == null){
                this.setCar2(car);
                this.transPoint2IsBooked = false;
                return true;
            }else{
                System.out.println(this.areaName+"!!Error:obtainThisTransferPoint2"+car.toString());
                throw new UnsupportedOperationException("!!!!!Error:TransferArea.obtainThisTransferPoint(car)"); 
            }
        }else if(this.getTransferPoint3().equals(car.getPresentPosition()) == true){
            if(this.getCar3() == null){
                this.setCar3(car);
                this.transPoint3IsBooked = false;
                return true;
            }else{
                System.out.println(this.areaName+"!!Error:obtainThisTransferPoint3"+car.toString());
                throw new UnsupportedOperationException("!!!!!Error:TransferArea.obtainThisTransferPoint(car)"); 
            }
        }else{
            System.out.println(this.areaName+"!!Error:obtainThisTransferPoint--null"+car.toString());
            throw new UnsupportedOperationException("!!!!!Error:TransferArea.obtainThisTransferPoint(car)"); 
        }
    }

    /**
     * 获取装卸点编号;
     * @param thisCar
     * @return 没有找到时返回0
     */
    public int findTransferPointNum(VehicleInterface thisCar){
        if(this.transferPoint1.equals(thisCar.getPresentPosition()) == true){
            return 1;
        }else if(this.transferPoint2.equals(thisCar.getPresentPosition()) == true){
            return 2;
        }else if(this.transferPoint3.equals(thisCar.getPresentPosition()) == true){
            return 3;
        }else{
//System.out.println(this.areaName+"!!Error:TransferArea.findTrasferPointNum()"+thisCar.toString());
            return 0;
        }
    }
    /**
     * @return the transferPoint1
     */
    public Point2D getTransferPoint1() {
        return transferPoint1;
    }

    /**
     * @return the transferPoint2
     */
    public Point2D getTransferPoint2() {
        return transferPoint2;
    }

    /**
     * @return the transferPoint3
     */
    public Point2D getTransferPoint3() {
        return transferPoint3;
    }

    /**
     * @param car1 the car1 to set
     */
    public void setCar1(VehicleInterface car1) {
        if(car1 == null && this.car1 != null){
            this.car1 = car1;
        }else if(car1 != null && this.car1 == null){
            this.car1 = car1;
        }else{
            System.out.print(this.areaName + "!!Error:TransferArea.setCar1");
            System.out.println(car1 + "" + this.car1);
            throw new UnsupportedOperationException("!!!!!Error:TransferArea.setCar1"); 
        }
    }

    /**
     * @param car2 the car2 to set
     */
    public void setCar2(VehicleInterface car2) {
        if(car2 == null && this.car2 != null){
            this.car2 = car2;
        }else if(car2 != null && this.car2 == null){
            this.car2 = car2;
        }else{
            System.out.print(this.areaName + "!!Error:TransferArea.setCar2");
            System.out.println(car2 + "" + this.car2);
            throw new UnsupportedOperationException("!!!!!Error:TransferArea.setCar2"); 
        }
    }

    /**
     * @param car3 the car3 to set
     */
    public void setCar3(VehicleInterface car3) {
        if(car3 == null && this.car3 != null){
            this.car3 = car3;
        }else if(car3 != null && this.car3 == null){
            this.car3 = car3;
        }else{
            System.out.print(this.areaName + "!!Error:TransferArea.setCar3");
            System.out.println(car3 + "" + this.car3);
            throw new UnsupportedOperationException("!!!!!Error:TransferArea.setCar3"); 
        }
    }

    /**
     * @return the transPoint1IsBooked
     */
    public boolean isTransPoint1IsBooked() {
        return transPoint1IsBooked;
    }

    /**
     * @return the transPoint2IsBooked
     */
    public boolean isTransPoint2IsBooked() {
        return transPoint2IsBooked;
    }

    /**
     * @return the transPoint3IsBooked
     */
    public boolean isTransPoint3IsBooked() {
        return transPoint3IsBooked;
    }

    /**
     * 返回是否预订成功
     * set true这么复杂的原因是由于多线程对set的影响!!!
     * @param state
     * @param agv
     * @return 
     */
    public boolean setTransPoint3Booked(boolean state,AGV agv) {
        if(state == true){
            if (this.transPoint3IsBooked == false && this.car3 == null) {
                this.transPoint3IsBooked = true;
                return true;
            } else {
//System.out.println(this.areaName + "!!Error:setTransPoint3IsBooked" + agv.toString());
                return false;
            }
        }else{
            System.out.println(this.areaName + "!!Error:setTransPoint3IsBooked" + agv.toString());
            throw new UnsupportedOperationException("!!!!!Error:TransferArea.setTransPoint3IsBooked"); 
        }
    }

    /**
     * 只能book，set false不要用这个哈
     * @param state
     * @param agv
     * @return 
     */
    public boolean setTransPoint1Booked(boolean state, AGV agv) {
        if(state == true){
            if (this.transPoint1IsBooked == false && this.car1 == null) {
                this.transPoint1IsBooked = true;
                return true;
            } else {
//System.out.println(this.areaName + "!!Error:setTransPoint1Booked" + agv.toString());
                return false;
            }
        }else{
            System.out.println(this.areaName + "!!Error:setTransPoint1Booked" + agv.toString());
            throw new UnsupportedOperationException("!!!!!Error:TransferArea.setTransPoint1Booked"); 
        }
    }

    /**
     * 只能book，set false不要用这个哈
     * @param state
     * @param agv
     * @return 
     */
    public boolean setTransPoint2Booked(boolean state, AGV agv) {
        if(state == true){
            if (this.transPoint2IsBooked == false && this.car2 == null) {
                this.transPoint2IsBooked = true;
                return true;
            } else {
//System.out.println(this.areaName + "!!Error:setTransPoint2Booked" + agv.toString());
                return false;
            }
        }else{
            System.out.println(this.areaName + "!!Error:setTransPoint2Booked" + agv.toString());
            throw new UnsupportedOperationException("!!!!!Error:TransferArea.setTransPoint2Booked"); 
        }
    }

}
