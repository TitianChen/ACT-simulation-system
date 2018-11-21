/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storageblock;

import CYT_model.Container;
import CYT_model.Point2D;
import Vehicle.AGV;
import Vehicle.VehicleInterface;
import parameter.OutputParameters;

/**
 * Unloading:AGV到达Block入口后加入YardTaskArray中
 * Loading：一分配就加入YardTaskArray中
 * 由YardTask统一进行分配;
 * YC一次作业流程中的基本信息;
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 */
public class YCTask {
    private final Container[] containers;//container of this assignment;
    private final String workType;//Unloading卸船放箱入堆场 or Loading装船从堆场取箱;
    private final Point2D startPosition;//岸桥开始作业的位置(开始获得箱);
    private final Point2D goalPosition;//岸桥结束作业的位置(开始放箱);
    private double startT;
    private double endT;
    private final VehicleInterface car;
    public YCTask(Container[] containers,String workType,Point2D startPosition,Point2D goalPosition,VehicleInterface car){
        if(containers == null){
            System.out.println("!!!Error:YCTask()Construct!containers == null!!");
            throw new UnsupportedOperationException("!!!Error:YCTask()Construct!!!");
        }
        this.containers = new Container[containers.length];
        for(int i = 0;i<this.containers.length;i++){
            this.containers[i] = containers[i];
        }
        this.workType = workType;
        this.startPosition = new Point2D.Double(0, 0);
        this.startPosition.setLocation(startPosition);
        this.goalPosition = new Point2D.Double(0, 0);
        this.goalPosition.setLocation(goalPosition);
//System.out.println("YCTask构造成功");
        if(startPosition == null){
            System.out.println("!!!Error:YCTask()Construct!!!");
            throw new UnsupportedOperationException("!!!Error:YCTask()Construct!!!");
        }
        this.car = car;
    }

    /**
     * @return the container[]
     */
    public Container[] getContainer() {
        return containers;
    }

    /**
     * Unloading卸船放箱入堆场; Loading装船从堆场取箱;
     * @return the workType
     */
    public String getWorkType() {
        return workType;
    }

    /**
     * @return the goalPosition
     */
    public Point2D getGoalPosition() {
        return goalPosition;
    }

    /**
     * @return the startPosition
     */
    public Point2D getStartPosition() {
        return startPosition;
    }

    @Override
    public String toString(){
        String str = "";
        if(this.car == null){
            str += this.workType+" "+"Truck"+""+this.containers.length+""+
                this.startPosition+""+this.goalPosition;
        } else {
            str += this.workType + " " + this.car.getName() + "" + this.containers.length + ""
                    + this.startPosition + "" + this.goalPosition;
        }
        return str;
    }
    /**
     * @return the car
     */
    public VehicleInterface getCar() {
        return car;
    }

    /**
     * @return the startT
     */
    public double getStartT() {
        return startT;
    }

    /**
     * @return the endT
     */
    public double getEndT() {
        return endT;
    }

    /**
     * @param startT the startT to set
     */
    public void setStartT(double startT) {
        this.startT = startT;
    }

    /**
     * @param endT the endT to set
     */
    public void setEndT(double endT) {
        this.endT = endT;
    }
    
    
}
