/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storageblock;

import CYT_model.Container;
import CYT_model.Point2D;
import Vehicle.Truck;

/**
 *
 * @author Administrator
 */
public class LandSideYardTask {
    private final Container[] container;//container of this assignment;
    private final String workType;//ToPort集港堆箱任务 or LeavingPort出港提箱任务;
    private Slot slot;//ToPort：该Container计划在箱区中储存的位置，LeavingPort：该Container目前在箱区中储存的位置;
    private final Truck truck;
    private final Point2D transferPoint;
    public LandSideYardTask(Container[] container,String workType,Slot slot,Truck truck,Point2D transferPoint){
        this.container = new Container[container.length];
        for(int i = 0;i<this.container.length;i++){
            this.container[i] = container[i];
        }
        this.workType = workType;
        if (slot != null) {
            this.slot = new Slot(slot);
        }else if(container[0].getPresentSlot() != null){
            if(container.length == 1){
                this.slot = new Slot(container[0].getPresentSlot());
            }else{
                this.slot = new Slot(container[0].getPresentSlot(),container[1].getPresentSlot());
            }
        }else{
            this.slot = null;
        }
        this.truck = truck;
        if(transferPoint == null){
            this.transferPoint = null;
        }else{
            this.transferPoint = new Point2D.Double(0, 0);
            this.transferPoint.setLocation(transferPoint);
        }
//System.out.println("YardTask构造成功");
    }

    /**
     * @return the transferPoint
     */
    public Point2D getTransferPoint() {
        return transferPoint;
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

    /**
     * @return the truck
     */
    public Truck getTruck() {
        return truck;
    }

    
}
