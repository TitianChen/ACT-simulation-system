/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storageblock;

import CYT_model.Container;
import CYT_model.Point2D;
import Vehicle.AGV;

/**
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 */
public class WaterSideYardTask {

    /**
     * @return the agv
     */
    public AGV getAgv() {
        return agv;
    }

    /**
     * @return the transferPoint
     */
    public Point2D getTransferPoint() {
        return transferPoint;
    }
    private final Container[] container;//container of this assignment;
    private final String workType;//Unloading卸船放箱入堆场 or Loading装船从堆场取箱;
    private Slot slot;//Unloading：该Container计划在箱区中储存的位置，Loading：该Container目前在箱区中储存的位置;
    private final AGV agv;
    private final Point2D transferPoint;
    public WaterSideYardTask(Container[] container,String workType,Slot slot,AGV agv,Point2D transferPoint){
        this.container = new Container[container.length];
        for(int i = 0;i<this.container.length;i++){
            this.container[i] = container[i];
        }
        this.workType = workType;
        if (slot != null) {
            this.slot = new Slot(slot);
        } else {
            this.slot = null;
        }
        this.agv = agv;
        this.transferPoint = new Point2D.Double(0, 0);
        this.transferPoint.setLocation(transferPoint);
    }
    @Override
    public String toString(){
        String res = this.agv.toString()+"*container:"+this.container.length+"*"+
                this.workType+"*"+this.transferPoint;
        String containerInformation  = "container::"+this.container[0].getState();
        return res+containerInformation;
    } 

    /**
     * @return the container
     */
    public Container[] getContainer() {
        return container;
    }

    /**
     * @return the workType
     */
    public String getWorkType() {
        return workType;
    }

    /**
     * @return the slot
     */
    public Slot getSlot() {
        if (slot != null) {
            return slot;
        }else{
            if (container[0].getPresentSlot() != null) {
                if (container.length == 1) {
                    this.slot = new Slot(container[0].getPresentSlot());
                } else {
                    this.slot = new Slot(container[0].getPresentSlot(), container[1].getPresentSlot());
                }
            }
            return slot;
        }
    }
}
