/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vehicle;

import CYT_model.Container;
import CYT_model.Point2D;
import storageblock.YCTask;


/**
 *
 * @author Administrator
 */
public class Truck implements VehicleInterface{
   
    private final Truck myself;
    private final String name;
    private final String TruckState;
    private final Point2D presentPosition;//
    private final Container[] containersOnTruck;
    private final YCTask serviceTask;
    
    public Truck(YCTask servicetask,Point2D nowPosition){
        this.serviceTask = servicetask;
        this.name = this.serviceTask.getWorkType()+" Truck";
        this.TruckState = this.serviceTask.getWorkType();
        this.presentPosition = new Point2D.Double(0, 0);
        this.presentPosition.setLocation(nowPosition);
        this.containersOnTruck = new Container[this.serviceTask.getContainer().length];
        System.arraycopy(this.serviceTask.getContainer(), 0, this.containersOnTruck, 0, this.containersOnTruck.length);
        this.myself = this;
    } 

    @Override
    public String getName() {
        return this.name;
    }
    @Override
    public String toString(){
        return this.name;
    }

    @Override
    public Point2D getPresentPosition() {
        return this.presentPosition;
    }

    /**
     * @return the TruckState
     */
    public String getTruckState() {
        return TruckState;
    }

    /**
     * @return the containersOnTruck
     */
    public Container[] getContainersOnTruck() {
        return containersOnTruck;
    }

    /**
     * @return the serviceTask
     */
    public YCTask getServiceTask() {
        return serviceTask;
    }

    /**
     * @return the myself
     */
    public Truck getMyself() {
        return myself;
    }
    
}
