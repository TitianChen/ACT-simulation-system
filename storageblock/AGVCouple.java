/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storageblock;

import CYT_model.Container;
import CYT_model.Point2D;
import parameter.InputParameters;
import parameter.StaticName;
/**
// * AGV伴侣-----不单独弄进程了！！！和AGVprocess()放一起处理？？？？？？？？？？？？？？？？？？？？？？、、
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 */
public class AGVCouple {
    private final Block serviceBlock;
    private final String name;
    private Container[] currentContainers;
    public static double Time_dragContainers = 1;
    public final Point2D centralPosition;
    public final double width;
    public final double length;
    public AGVCouple(Block block,int point){
        this.serviceBlock = block;
        this.name = this.serviceBlock.toString()+"AGVCouple"+"-TP-"+point;
        this.currentContainers = null;
        this.centralPosition = new Point2D.Double(0,0);
        if (point == 3) {
            this.centralPosition.setLocation(block.getWaterSideArea().getTransferPoint3());
        } else {
            this.centralPosition.setLocation(block.getWaterSideArea().getTransferPoint2());
        }
        this.width = InputParameters.getWidthOfContainer()+2;///////////////////这里可以改！
        this.length = InputParameters.getLengthOfTransferPoint()-2;/////////////尺寸可以改
    }
    /**
     * 获得集装箱;
     * 改变AGVcouple状态;
     * @param containers 
     */
    public void obtainContainers(Container[] containers){
        if(this.getCurrentContainers() != null){
            System.out.println("!!!Error:AGVCouple.obtainContainers(Container[] containers)出错！！");
            throw new UnsupportedOperationException("!!!Error:AGVCouple.obtainContainers(Container[] containers)出错！！");
        }else{
            this.currentContainers = new Container[containers.length];
            for(int i = 0;i<this.getCurrentContainers().length;i++){
                this.currentContainers[i] = containers[i];
                this.currentContainers[i].setState(StaticName.ONAGVCOUPLE);
            }
        }
    }
    /**
     * @param YC
     */
    public void clearContainersToYC(YardCrane YC){
        int length = this.currentContainers.length;
        for(int i = 0;i<length;i++){
//System.out.println("0------------AGVCouple"+ "!!!Error:"+YC.getFirstYCTask().getContainer()[0]+"----"+this.currentContainers[0]);
            if(this.currentContainers[i].equals(YC.getFirstYCTask().getContainer()[i]) == false){
                System.out.println("!!!Error:AGVCouple.clearContainersToYC(YardCrane YC)出错！！");
                throw new UnsupportedOperationException("Error:AGVCouple.clearContainersToYC(YardCrane YC)出错！！");
            }
        }
        this.currentContainers = null;
    }
    
    
    /**
     * 是否有集装箱;
     * @return true有箱， false无箱
     */
    public boolean haveContainers(){
        if(this.getCurrentContainers() == null){
            return false;
        }else{
            return true;
        }
    }

    /**
     * @return the currentContainers
     */
    public Container[] getCurrentContainers() {
        if(this.currentContainers == null){
            return null;
        }
        return currentContainers;
    }
    
}
