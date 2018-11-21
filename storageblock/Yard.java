/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storageblock;

import CYT_model.Container;
import CYT_model.Point2D;
import CYT_model.Port;
import Vehicle.AGV;
import java.util.ArrayDeque;
import java.util.Queue;
import parameter.InputParameters;
import parameter.OutputParameters;
import parameter.StaticName;
import roadnet.FunctionArea;
import ship.Ship;

/**
 * 堆场区域;在该部分进行yardSection位置的指定;
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 */
//一个Block;由多个section组成;
public class Yard extends FunctionArea {
    
    private final Yard myself;
    private Port port;
    //1.section
    private final Block[] yardBlock = new Block
            [InputParameters.getTotalBlockNumPerYard()];
    private final double distanceYbetweenBlock = InputParameters.getDistanceYOfBlock();
    private final int liftingNum = InputParameters.getYCLiftingNum();
    private final int containerHeightNum = InputParameters.getYCContainerHeightNum();
    private final int rowNumPerYC = InputParameters.getPerYCRowNum();
    private final int blockNumPerYard = InputParameters.getTotalBlockNumPerYard();
    private final int bayNumPerRow = InputParameters.getTotalBayNum();
    private final double distanceOfContainers = InputParameters.getDistanceOfContainers();//相邻集装箱之间的距离;
    private final double lengthOfYard = InputParameters.getLengthOfYard();//Block长度,不包括两端的车辆交换区;
    private final double lengthOfTransferPoint = InputParameters.getLengthOfTransferPoint();//交换区长度;
    private final double trackGuage = InputParameters.getRMGtrackGuage();
    private final String LayoutRule;//Parallel Vertical
    //
    private final Queue<LandSideYardTask> truckGatheringTask = new ArrayDeque<>();
    
    //Block里面要有
    //p1 p2均不包括交换区;
    public Yard(Point2D p1, Point2D p2, String areanum, String areaname, String layOutRule,Port port) {
        super(p1,p2,areanum, areaname);
        this.LayoutRule = layOutRule;
        this.myself = this;
        this.port = port;
        for(int i = 0;i<blockNumPerYard;i++){
            yardBlock[i] = new Block(this.myself,i+1,distanceYbetweenBlock,liftingNum,
                    containerHeightNum,rowNumPerYC,blockNumPerYard,bayNumPerRow,
                    distanceOfContainers,lengthOfYard,lengthOfTransferPoint,trackGuage);   
        }
    }
    /**
     * 添加Block中的crane;
     * @param yardCrane 
     */
    public void addCrane(YardCrane yardCrane){
        int i = yardCrane.blockNum-'A';
        getBlock()[i].setYC(yardCrane);
        yardCrane.setYardBlock(this.getBlock()[i]);
    }

    /**
     * @return the yardBlock
     */
    public Block[] getBlock() {
        return yardBlock;
    }
    /**
     * @param containers
     * @param block
     * @return container[]在yardSection的层数高度;
     */
    public int findHeightNum(Container[] containers,Block block){
        if(containers == null){
            return 1;
        }
        int length = containers.length;
        if(containers[0].getPresentSlot() == null){
            System.out.println("!!!Error:Block.findHeightNum()-containers[0].getPresentSlot()=null!!!");
            return 1;
            //避免报错，暂时去掉;throw new UnsupportedOperationException("Error:Block.findHeightNum()");
        }
        int heightNum = containers[0].getPresentSlot().getHeightNum();
        for(int i = 0;i<length;i++){
            if(heightNum != containers[i].getPresentSlot().getHeightNum()){
                System.out.println("!!!Error:Block.findHeightNum(Container[] containers,YardSection section)!!!");
                //避免报错，暂时去掉;throw new UnsupportedOperationException("Error:Block.findHeightNum(Container[] containers,YardSection section).");
            }
        }
        return heightNum;
    }
    /**
     * AGV刚驶入该Block的Point2D(由车道确定)
     * @param agv
     * @return 
     */
    public Point2D getEntranceRoadOfAGV(AGV agv) {
        if(this.getAreaNum().equals("Yard1") == true){
            return new Point2D.Double(port.getRoadNetWork().findRoadSection("Road3").CentralAxis(1).getX(),
                    port.getRoadNetWork().findRoadSection("Road3").CentralAxis(1).getY());
        }else if(this.getAreaNum().equals("Yard2") == true){
            return new Point2D.Double(port.getRoadNetWork().findRoadSection("Road7").CentralAxis(1).getX(),
                    port.getRoadNetWork().findRoadSection("Road7").CentralAxis(1).getY());
        }else{
            System.out.println("!!!Error:Yard.getEntranceRoadOfAGV(AGV agv).!!!");
            throw new UnsupportedOperationException("Error:Yard.getEntranceRoadOfAGV(AGV agv).");
        }
    }

    /**
     * @return the port
     */
    public Port getPort() {
        return port;
    }

    /**
     * @return the LayoutRule
     */
    public String getLayoutRule() {
        return LayoutRule;
    }

    /**
     * 该yard中加入一个该Ship的集港任务;
     * @param ship
     */
    public synchronized void addOneTruckGatheringTask(Ship ship) {
        if(ship.getShipContainers().getLoadingContainersOutside() == null){
            System.out.println("!!!!!Error!!!!Error:yard.addOneTruckGatheringTask(ship)");
            throw new UnsupportedOperationException("Error:yard.addOneTruckGatheringTask(ship)"); 
        }
        this.truckGatheringTask.add(new LandSideYardTask(ship.getShipContainers().
                getandSetOnTruck_Max2TEUOutsideContainers(),StaticName.TOPORT,null,null,null));
    }

    /**
     * @return the truckGatheringTask
     */
    public synchronized Queue<LandSideYardTask> getTruckGatheringTask() {
        return truckGatheringTask;
    }
}
