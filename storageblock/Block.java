/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storageblock;
//由YardSection给里面的两个岸桥分配任务。

import CYT_model.Container;
import CYT_model.Point2D;
import Vehicle.AGV;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import parameter.InputParameters;
import static parameter.InputParameters.getPerYCRowNum;
import static parameter.InputParameters.getProportionOf20;
import static parameter.OutputParameters.simulationRealTime;
import parameter.StaticName;

/**
 * 堆场中的一段,由2个岸桥组成;
 * @author Administrator
 */
public class Block{
    private final Block myself;
    //位置信息;
    private final Point2D P1;
    private final Point2D P2;
    private final double widthOfBlock;
    //Block规格信息;
    private final int containerHeightNum;
    private final int rowNumPerBlock;
    private final int bayNumPerRow;
    private final double distanceOfContainers;
    private final double lengthOfBlock;
    private final double lengthOfTransferPoint;
    private final double trackGuage;
    //
    private final Yard yard;
    private final TransferArea waterSideArea;
    private final TransferArea landSideArea;
    private final AGVCouple[] AGVCouple;//AGV伴侣; ////一个Block2个AGV伴侣;
    private final int line;//标记YardSection在Block中的位置;1开始
    //当前箱量;
    private Container[] currentContainers;//当前在该Section中的所有containers(不包括在transferPoint的)
    //设备信息;
    private final YardCrane[] YC;
    //任务信息;
    private final CopyOnWriteArrayList<WaterSideYardTask> blockWatersideAssignment = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<LandSideYardTask> blockLandsideAssignment = new CopyOnWriteArrayList<>();//陆侧任务列表;
    private final CopyOnWriteArrayList<LandSideYardTask> leavingPortList = new CopyOnWriteArrayList<>();//离港任务列表;
//    private final List<LandSideYardTask> leavingPortList = Collections.synchronizedList(new LinkedList<>());//离港任务列表;
//    private final List<WaterSideYardTask> blockWatersideAssignment = Collections.synchronizedList(new LinkedList<>());//海测任务列表;
//    private final List<LandSideYardTask> blockLandsideAssignment = Collections.synchronizedList(new LinkedList<>());//陆侧任务列表;
    
    //p1 x y 均小于 p2;(p1:左下,p2:右上)
    public Block(Yard yard, int i, double distanceYbetweenBlock, int liftingNum, 
            int containerHeightNum, int rowNumPerYC, int blockNumPerYard, int bayNumPerRow,
            double distanceOfContainers,double lengthOfYard,double lengthOfTransferPoint,double trackGuage) 
    {
        this.myself = this;
        this.yard = yard;
        this.line = i;//i从1开始;
        this.YC = new YardCrane[InputParameters.getYCNumPerBlock()];
        
        this.distanceOfContainers = distanceOfContainers;
        this.lengthOfBlock = lengthOfYard;//不包括交换区长度;
        this.lengthOfTransferPoint = lengthOfTransferPoint;
        this.trackGuage = trackGuage;
        this.containerHeightNum = containerHeightNum;
        this.rowNumPerBlock = rowNumPerYC;
        this.bayNumPerRow = bayNumPerRow;
        this.widthOfBlock = this.trackGuage;
        this.P1 = new Point2D.Double(0,0);//不包括交换区;
        this.P2 = new Point2D.Double(0,0);//不包括交换区;
        P1.setLocation(this.yard.P1.getX()+(widthOfBlock+distanceYbetweenBlock)*(i-1), this.yard.P1.getY());
        P2.setLocation(this.P1.getX()+widthOfBlock, this.yard.P2.getY());
        double sideType;
        ////陆侧，海测位置;
        //目前按两个block来;
        if(this.yard.getAreaNum().equals("Yard1") == true){
            sideType = 1;//左AGV右集卡;---左waterside右landside;
            waterSideArea = new TransferArea(new Point2D.Double(this.getP1().getX(),this.getP1().getY()-this.lengthOfTransferPoint),
                    new Point2D.Double(this.getP1().getX()+getWidthOfBlock(),this.getP1().getY()),
                    StaticName.WATERSIDE+"block"+this.line+this.yard.getAreaNum(),this.myself);
            landSideArea = new TransferArea(new Point2D.Double(this.getP2().getX()-getWidthOfBlock(),this.getP2().getY()),
                    new Point2D.Double(this.getP2().getX(),this.getP2().getY()+this.lengthOfTransferPoint),
                    StaticName.LANDSIDE+"block"+this.line+this.yard.getAreaNum(),this.myself);
        }else if(yard.getAreaNum().equals("Yard2") == true){
            sideType = 2;//左集卡右AGV;---左landside右waterside;
            landSideArea = new TransferArea(new Point2D.Double(this.getP1().getX(),this.getP1().getY()-this.lengthOfTransferPoint),
                    new Point2D.Double(this.getP1().getX()+getWidthOfBlock(),this.getP1().getY()),
                    StaticName.LANDSIDE+"block"+this.line+this.yard.getAreaNum(),this.myself);
            waterSideArea = new TransferArea(new Point2D.Double(this.getP2().getX()-getWidthOfBlock(),this.getP2().getY()),
                    new Point2D.Double(this.getP2().getX(),this.getP2().getY()+this.lengthOfTransferPoint),
                    StaticName.WATERSIDE+"block"+this.line+this.yard.getAreaNum(),this.myself);
        }else{
            System.out.println("!!! Error: Block.Block() !!!");
            throw new UnsupportedOperationException("!!! Error: Block.Block() !!!!!!");
        }
        this.AGVCouple = new AGVCouple[2];
        this.AGVCouple[0] = new AGVCouple(this.myself, 3);
        this.AGVCouple[1] = new AGVCouple(this.myself, 2);
        //初始化箱量;
        this.currentContainers = new Container[Math.round((float)(this.containerHeightNum*rowNumPerBlock
                *this.bayNumPerRow/InputParameters.getInitialProportionOfContainersOnYard()))];
//System.out.println(this.toString()+"------初始化箱量:"+this.currentContainers.length);
        String[] size = {StaticName.SIZE20FT,StaticName.SIZE40FT};
        for(i=0;i<this.currentContainers.length;i++){
            int randomI = (int)Math.floor(Math.random()*10/7);//////0:7/10,1:3/10
            this.currentContainers[i] = new Container(size[randomI],StaticName.ONSTOCKING,this.myself);
            //int rowNum,int bayNum,int heightNum
            int rowNum = 1;
            int bayNum = 1;
            int currentHeightNum = -1;
            while(currentHeightNum == -1){
                if(size[randomI].equals(StaticName.SIZE40FT) == true){
                    rowNum = (int)InputParameters.getRandom((int)(getProportionOf20()*InputParameters.getPerYCRowNum()),
                            InputParameters.getPerYCRowNum());//[6,7]
                    bayNum = InputParameters.getRandomIntTriangle(StaticName.TRIANGULAR,
                            2, InputParameters.getTotalBayNum() * 2 - 2);//[2,58]
                }else{
                    rowNum = (int)InputParameters.getRandom(1, (int)(getProportionOf20()*InputParameters.getPerYCRowNum()));//[1,5]
                    bayNum = InputParameters.getRandomIntTriangle(StaticName.TRIANGULAR,
                            1, InputParameters.getTotalBayNum() * 2 - 1);//[1,59]
                }
                currentHeightNum = this.findCurrentMaxHeightNum(rowNum,bayNum,i);
                if(currentHeightNum == -1){
                    //System.out.println("!!!:Block.findCurrentMaxHeightNum(int rowNum"+rowNum
                      //      +",int bayNum"+bayNum+")再来一次!!");
                }
            }
            this.currentContainers[i].presentSlot = new Slot(this.yard,this.myself,rowNum,bayNum,currentHeightNum+1);
        }
//OutputParameters.outputBlockCurrentContainer(this.myself);
    }
    private int findCurrentMaxHeightNum(int rowNum,int bayNum,int num){
        switch(bayNum%2){
            case 0:
                //40ft箱;
                if(this.findCurrentMaxHeightNum(rowNum, bayNum-1,num) != -1){
//                if(this.findCurrentMaxHeightNum(rowNum, bayNum-1,num) ==
//                        this.findCurrentMaxHeightNum(rowNum, bayNum+1,num) &&
//                        this.findCurrentMaxHeightNum(rowNum, bayNum-1,num) != -1){
                    return this.findCurrentMaxHeightNum(rowNum, bayNum-1,num);
                }else{
                    return -1;
                }
            case 1:
                //20ft箱;
                int heightNum = 0;
                if(num == 0){
                    return 0;
                }
                for(int i=0;i<num;i++){
                    if(this.currentContainers[i].getPresentSlot() != null &&
                            this.currentContainers[i].getPresentSlot().getRowNum()==rowNum &&
                            this.currentContainers[i].getPresentSlot().getBayNum()==bayNum){
                        heightNum++;
                    }else if(this.currentContainers[i].getPresentSlot() != null &&
                            this.currentContainers[i].getPresentSlot().getRowNum() == rowNum &&
                            (this.currentContainers[i].getPresentSlot().getBayNum() == (bayNum-1) ||
                            this.currentContainers[i].getPresentSlot().getBayNum() == (bayNum+1))){
                        heightNum++;
                    }
                }
                if(heightNum+1 >= InputParameters.getYCContainerHeightNum()){
                    return -1;
                }
                return heightNum;
            default:
                System.out.println("bayNum：："+bayNum+"!!:Block.findCurrentMaxHeightNum(int rowNum,int bayNum,int num) default!!!!");
                throw new UnsupportedOperationException("!!:Block.findCurrentMaxHeightNum(int rowNum,int bayNum,int num)出错!!");  
        }
    }
    /**
     * YardSection任务列表数量;
     * @return int num
     */
    public int assignmentNumNow(){
        if (this.blockWatersideAssignment.isEmpty() == false) {
            return this.blockWatersideAssignment.size();
        } else {
            return 0;
        }
    }
    public void setYC(YardCrane YC){
        if(YC.getNumber().equals(StaticName.WATERSIDE) == true){
            this.YC[0] = YC;
        }else if(YC.getNumber().equals(StaticName.LANDSIDE) == true){
            this.YC[1] = YC;
        } 
    }
    /**
     * 
     * @param containers 
     */
    public void addContainer(Container[] containers){
        int length = this.currentContainers.length;
        int addNum = containers.length;
        Container[] allContainers = new Container[length+addNum];
        for(int i=0;i<length;i++){
            allContainers[i] = this.currentContainers[i];
        }
        for(int i = length;i<allContainers.length;i++){
            allContainers[i] = containers[i-length];
            
        }
        this.currentContainers = new Container[allContainers.length];
        for(int i = 0;i<allContainers.length;i++){
            this.currentContainers[i] = allContainers[i];
        }
    }
    public void addContainer(Container container){
        int length = this.currentContainers.length;
        int addNum = 1;
        Container[] allContainers = new Container[length+addNum];
        for(int i=0;i<length;i++){
            allContainers[i] = this.currentContainers[i];
        }
        for(int i = length;i<allContainers.length;i++){
            allContainers[i] = container;
        }
        this.currentContainers = new Container[allContainers.length];
        for(int i = 0;i<allContainers.length;i++){
            this.currentContainers[i] = allContainers[i];
        }
    }
    public synchronized void move1Or2MaybeContainer(Container[] containers){
        if(this.currentContainers == null){
            return;   
        }
        int length = this.currentContainers.length;
        int j = 0;
        for (int i = 0; i < length; i++) {
            if (this.currentContainers[i].equals(containers[0]) == true) {
                j++;
            }
        }
        int num = 0;
        Container[] allContainers;
        if (containers.length == 1) {
            allContainers = new Container[length - j];
            for (int i = 0; i < length; i++) {
                if (this.currentContainers[i].equals(containers[0]) == false) {
                    allContainers[num] = this.currentContainers[i];
                    num++;
                }
            }
        }else{
            allContainers = new Container[length - j];
            for (int i = 0; i < length; i++) {
                if (this.currentContainers[i].equals(containers[0]) == false &&
                        this.currentContainers[i].equals(containers[1]) == false) {
                    allContainers[num] = this.currentContainers[i];
                    num++;
                }
            }
        }
        this.currentContainers = new Container[allContainers.length];
        System.arraycopy(allContainers, 0, this.currentContainers, 0, allContainers.length); 
    }
    /**
     * 从currentContainer里移除Container
     * @param containers 
     */
    public void moveContainer(Container[] containers){
        if(this.currentContainers == null){
            return;   
        }
        int length = this.currentContainers.length;
        int moveNum = containers.length;
        Container[] allContainers = new Container[length-moveNum];
        int j = 0;
        switch(moveNum){
            case 1:
                for(int i=0;i<length;i++){
                    if(this.currentContainers[i].equals(containers[0]) == false){
                        allContainers[j] = this.currentContainers[i];
                        j++;
                    }
                }
                break;
            case 2:
                for(int i=0;i<length;i++){
                    if(this.currentContainers[i].equals(containers[0]) == false &&
                            this.currentContainers[i].equals(containers[1]) == false){
                        allContainers[j] = this.currentContainers[i];
                        j++;
                    }
                }
                break;
            default:
                System.out.println("!!Error:Block.moveContainer(Container[] containers) switch()出错!!");
//throw new UnsupportedOperationException("Error:Block.moveContainer(Container[] containers) switch()出错！！");
        }
        if(j != allContainers.length){
            System.out.println("!!Error:Block.moveContainer(Container[] containers)出错!!");
//throw new UnsupportedOperationException("!!Error:Block.moveContainer(Container[] containers)出错!!");
        }
        this.currentContainers = new Container[allContainers.length];
        for(int i = 0;i<allContainers.length;i++){
            this.currentContainers[i] = allContainers[i];
        }
    }
    /**
     * 这部分最好改成找matrix
     * 找到当bayNum,rowNum的堆箱高度;
     * @param rowNum
     * @param bayNum
     * @return 
     */
    public int findCurrentMaxHeightNum(int rowNum,int bayNum){
        switch(bayNum%2){
            case 0:
                //40ft箱位---2~58
                if((this.findCurrentMaxHeightNum(rowNum, bayNum-1) != this.findCurrentMaxHeightNum(rowNum, bayNum+1)))
                {
//                    return 100;
                    return this.findCurrentMaxHeightNum(rowNum, bayNum-1);
                }else if(this.findCurrentMaxHeightNum(rowNum, bayNum-1) > InputParameters.getYCContainerHeightNum()){
                    return 100;
                }else{
                    return this.findCurrentMaxHeightNum(rowNum, bayNum-1);
                }
            case 1:
                //20ft箱位---1~59;
                int heightNum = 0;
                if(this.currentContainers == null){
                    return 0;
                }
                for(int i=0;i<this.currentContainers.length;i++){
                    if(this.currentContainers[i].getPresentSlot() != null &&
                            this.currentContainers[i].getPresentSlot().getRowNum()==rowNum &&
                            this.currentContainers[i].getPresentSlot().getBayNum()==bayNum){
                        heightNum++;
                    }else if(this.currentContainers[i].getPresentSlot() != null &&
                            this.currentContainers[i].getPresentSlot().getRowNum() == rowNum &&
                            (this.currentContainers[i].getPresentSlot().getBayNum() == (bayNum-1) ||
                            this.currentContainers[i].getPresentSlot().getBayNum() == (bayNum+1))){
                        heightNum++;
                    }
                }
                if(heightNum+1 > this.YC[0].getMaxContainersHeightNum()){
                    //System.out.println("!!:Block.findCurrentMaxHeightNum(int rowNum,int bayNum)不合适，继续找!!");
                    return 100;
                }
                if(heightNum > this.YC[0].getMaxContainersHeightNum()){
                    System.out.println("!!Error:Block.findCurrentMaxHeightNum(int rowNum,int bayNum)出错!!");
                    throw new UnsupportedOperationException("!!Error:Block.findCurrentMaxHeightNum(int rowNum,int bayNum)出错!!");
                }
                return heightNum;
            default:
                System.out.println("bayNum：："+bayNum+"!!Error:Block.findCurrentMaxHeightNum(int rowNum,int bayNum) default!!!!");
                throw new UnsupportedOperationException("!!Error:Block.findCurrentMaxHeightNum(int rowNum,int bayNum)出错!!");  
        }
    }
    /**
     * 用于储存堆场指定YardBlock的作业任务Array;
     * 与前沿：对于Unloading任务,是AGV到达Yard入口后再加入Array中，对于Loading任务，是一分配就加入Array中;
     * -----------与港外：对于取箱出港任务,是产生任务后加入Array中,---------？？
     * -----------与港外：对于进港放箱任务，是集卡到达Where？？后加入Array中;？？
     * 然后由YardTask分配该Block中YCs各自相应的任务;
     * @param container
     * @param workType
     * @param slotLocation
     * @param agv
     * @param point
     */
    public synchronized void obtainOneTask(Container[] container,String workType,
            Slot slotLocation,AGV agv,Point2D point){
        //获得任务;
        //任意一个YC_Process()，都会触发YC的getTask()函数，由场桥指派相应任务;
        this.blockWatersideAssignment.add(new WaterSideYardTask(container,workType,slotLocation,agv,point));
//System.out.println(agv.toString()+"AGV.addUnloadingTaskOfRelativeYardSection成功！！"+this.getYard().getAreaNum()+"Section"+this.getLine());
        //unloading作业先不指定startLocation;在从队列中提取该作业时再分配Location;
    }
    /**
     * @return the YC
     */
    public YardCrane[] getYC() {
        return YC;
    }
    /**
     * 
     * @return 
     */
    @Override
    public String toString(){
        return this.getYard().getAreaNum()+"Block"+this.getLine()+":";
    }
    /**
     * @return the block
     */
    public Yard getYard() {
        return yard;
    }
    /**
     * @return the waterSideArea
     */
    public TransferArea getWaterSideArea() {
        return waterSideArea;
    }
    /**
     * @return the landSideArea
     */
    public TransferArea getLandSideArea() {
        return landSideArea;
    }
    /**
     * @return the currentContainers
     */
    public Container[] getCurrentContainers() {
        if(this.currentContainers == null){
//System.out.println("YardSection.getCurrentContainers():NULL");
        }
        return currentContainers;
    }
    /**
     * 20ft为1个单位;
     * 40ft为2个num;
     * @return 
     */
    public double getContainersTEU(){
        int num = 0;
        if(this.currentContainers == null){
            return 0;
        }else{
            int length = this.currentContainers.length;
            for(int i = 0;i<length;i++){
                if (this.currentContainers[i] != null) {
                    if (this.currentContainers[i].size == 20) {
                        num++;
                    } else if (this.currentContainers[i].size == 40) {
                        num += 2;
                    }
                }
            }
        }
        return num;
    }
    /**
     * @return the P1
     */
    public Point2D getP1() {
        return P1;
    }
    /**
     * @return the P2
     */
    public Point2D getP2() {
        return P2;
    }  
    
    /**
     * 设置运输车辆驶离;
     * transferPoint解除占用;
     * @param agv 
     */
    public void setVehicleLeave(AGV agv){
        if(this.waterSideArea.getTransferPoint1().equals(agv.getPresentPosition()) == true){
            this.waterSideArea.setCar1(null);
        }else if(this.waterSideArea.getTransferPoint2().equals(agv.getPresentPosition()) == true){
            this.waterSideArea.setCar2(null);
        }else if(this.waterSideArea.getTransferPoint3().equals(agv.getPresentPosition()) == true){
            this.waterSideArea.setCar3(null);
        }else{
            System.out.println(this.getLine()+"!!Error:Block.setVehicleLeave(AGV agv)");
            throw new UnsupportedOperationException("!!!Error:Block.setVehicleLeave(AGV agv)!!!.");
        }
    }
    /**
     * 获得当前位置所在Bay位
     * @param point
     * @return int bayNum
     * 没有bayNum时返回-1;
     */
    public int findBayNum(Point2D point){
        int bayNum = -1;
        for(int i = 1;i<=this.getBayNumPerRow()*2-1;i++){
            double y = this.getP1().getY()+0.5*i*InputParameters.getLengthOf20FTContainer()
                +InputParameters.getDistanceOfContainers()*0.5*(i-1);
            if(Math.abs(point.getY()-y)<0.0001){
                bayNum = i;
                break;
            }
        }
        if(bayNum != -1){
            return bayNum;
        }else{
//System.out.println("!!!Block.findBayNum() = -1!!!");
            return -1;
            //throw new UnsupportedOperationException("!!!Error:Block.findBayNum()!!!.");            
        }
    }
    /**
     * 寻找对应Position所在的RowNum;
     * @param position 中心坐标;
     * @return rowNum
     */
    public int findRowNum(Point2D position){
        double d = 0.5*(InputParameters.getRMGtrackGuage()-InputParameters.getPerYCRowNum()*InputParameters.getWidthOfContainer()
                -InputParameters.getDistanceOfContainers()*(InputParameters.getPerYCRowNum()-1));
        double row = ((this.getP2().getX()-d-0.5*InputParameters.getWidthOfContainer()-position.getX())/
                (InputParameters.getWidthOfContainer()+InputParameters.getDistanceOfContainers()))+1;
        if(Math.abs(row-Math.round(row))>=0.001){
            System.out.println("row:"+row+"!!!Error:Block.findRowNum(Point2D position)!!!");
            throw new UnsupportedOperationException("Error:Block.findRowNum(Point2D position).");
        }
        if((int)Math.round(row)>InputParameters.getPerYCRowNum()){
            System.out.println("row:"+Math.round(row)+"!!!Error:>>>>>>>>>Block.findRowNum(Point2D position)!!!");
            throw new UnsupportedOperationException("Error:>>>>>>>>>>Block.findRowNum(Point2D position).");
        }
        return (int)Math.round(row);
    }
    /**
     * 寻找对应Position最近的RowNum;
     * 一般用在YC prosses()-transferPoint 
     * @param position 坐标;
     * @return nearest rowNum
     */
    public int findNearestRowNum(Point2D position){
        double d = 0.5*(InputParameters.getRMGtrackGuage()-InputParameters.getPerYCRowNum()*InputParameters.getWidthOfContainer()
                -InputParameters.getDistanceOfContainers()*(InputParameters.getPerYCRowNum()-1));
        double row = ((this.getP2().getX()-d-0.5*InputParameters.getWidthOfContainer()-position.getX())/
                (InputParameters.getWidthOfContainer()+InputParameters.getDistanceOfContainers()))+1;
        return (int)Math.round(row);
    }
    /**
     * @return the AGVCouple
     */
    public AGVCouple[] getAGVCouple() {
        return AGVCouple;
    }
    public YardCrane getWatersideYC(){
        for (YardCrane YC1 : this.YC) {
            if (YC1.getNumber().equals(StaticName.WATERSIDE) == true) {
                return YC1;
            }
        }
        return null;
    }
    
    public YardCrane getLandsideYC() {
        for (YardCrane YC1 : this.YC) {
            if (YC1.getNumber().equals(StaticName.LANDSIDE) == true) {
                return YC1;
            }
        }
        return null;
    }
    
    /**
     * YardTask向YC分配任务;若成功,则更新firstYCTask;
     * @param YC
     */
    public synchronized void assignTaskToThisYC(YardCrane YC) {
        //对于陆侧岸桥，优先分配陆侧任务，海测岸桥优先分配海测任务
        //存在海测陆侧任务不平衡时，再调配(assignOpositeTaskToThisYC(YardCrane YC))
        if (YC.getNumber().equals(StaticName.LANDSIDE) == true
                && this.blockLandsideAssignment.isEmpty()) {
            //创造新的任务;同一计划时间下，优先离港任务;再进港任务
            this.obtainLeavingPortAssignment();
            this.obtainGatheringPortAssignment();
            
        }
        if ((YC.getNumber().equals(StaticName.WATERSIDE) == true
                && this.blockWatersideAssignment.isEmpty() == false)) {
            WaterSideYardTask thisTask;
            thisTask = this.blockWatersideAssignment.remove(0);//.poll();
            switch (thisTask.getWorkType()) {
                case StaticName.LOADING:
                    //装船;
                    //要确保该AGV已经到达Block;
                    if (thisTask.getContainer().length == 2
                            && this.yard.getPort().isNeighbour20FT(thisTask.getContainer(), 2) == true) {
//System.out.println(YC.YCname + "get LOADING assignedTask 开始！！");
                        YC.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LOADING,
                                this.yard.getPort().getCentralSlotPosition(thisTask.getContainer(), 2),
                                thisTask.getTransferPoint(), thisTask.getAgv()));
                        return;
                    } else if (thisTask.getContainer().length == 1) {
//System.out.println(YC.YCname + "get LOADING assignedTask 开始");
                        YC.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LOADING,
                                thisTask.getContainer()[0].getPresentSlot().getCentralLocation(),
                                thisTask.getTransferPoint(), thisTask.getAgv()));
                        return;
                    } else if (thisTask.getContainer().length == 2
                            && this.yard.getPort().isNeighbour20FT(thisTask.getContainer(), 2) == false) {
//System.out.println(YC.YCname + "get LOADING assignedTask 开始！！");
                        YC.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LOADING,
                                thisTask.getContainer()[0].getPresentSlot().getCentralLocation(),
                                thisTask.getTransferPoint(), thisTask.getAgv()));
                        Container[] container = new Container[1];
                        container[0] = thisTask.getContainer()[1];
                        //Container[] container,String workType,Slot location,AGV agv,Point2D transferPoint
                        this.blockWatersideAssignment.add(0,new WaterSideYardTask(container, thisTask.getWorkType(),
                                thisTask.getContainer()[1].getPresentSlot(), thisTask.getAgv(), thisTask.getTransferPoint()));
                        return;
                    } else {
                        System.out.println("!!!Error:Block.assignTaskToThisYC(YardCrane YC)Loading!!!");
                        throw new UnsupportedOperationException("!!!Error:Block.assignTaskToThisYC(YardCrane YC)!!!");
                    }
                case StaticName.UNLOADING:
                    //卸船;
//System.out.println(YC.YCname + "get Unloading assignedTask 开始");
//System.out.println(YC.YCname + "thisTask.getContainer()");
                    if (thisTask.getContainer().length == 2) {
//System.out.println(YC.YCname + "length2 开始");
                        YC.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.UNLOADING,
                                thisTask.getTransferPoint(), this.findSuitableFreeSlot(thisTask.getContainer()), thisTask.getAgv()));
                        return;
                    } else if (thisTask.getContainer().length == 1) {
//System.out.println(YC.YCname + "length1 开始");
                        YC.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.UNLOADING,
                                thisTask.getTransferPoint(), this.findSuitableFreeSlot(thisTask.getContainer()), thisTask.getAgv()));
                        return;
                    } else {
                        System.out.println("!!!Error:Block.assignTaskToThisYC(YardCrane YC)Unloading!!!");
                        throw new UnsupportedOperationException("!!!Error:Block.assignTaskToThisYC(YardCrane YC)!!!");
                    }
            }
        }
        if ((YC.getNumber().equals(StaticName.LANDSIDE) == true
                && this.blockLandsideAssignment.isEmpty() == false)) {
            LandSideYardTask thisTask;
            thisTask = this.blockLandsideAssignment.remove(0);//.poll();
            switch (thisTask.getWorkType()) {
                case StaticName.LEAVINGPORT:
                    //进口箱离港;
                    if (thisTask.getContainer().length == 2
                            && this.yard.getPort().isNeighbour20FT(thisTask.getContainer(), 2) == true) {
//System.out.println(YC.YCname + "get LEAVINGPORT assignedTask 开始!!1---");
                        YC.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LEAVINGPORT,
                                this.yard.getPort().getCentralSlotPosition(thisTask.getContainer(), 2),
                                this.getLandSideArea().getTransferPoint1(), null));
                    } else if (thisTask.getContainer().length == 1) {
//System.out.println(YC.YCname + "get LEAVINGPORT assignedTask 开始!!2---");
                        if (thisTask.getContainer()[0].getPresentSlot() == null) {
//System.out.println(YC.YCname + "Block.assignTask.::::::getPresentSlot() == null:::::");
                            //System.out.println(YC.YCname + "::::::__:::::" + thisTask.getContainer()[0].getState());
                        } else {
                            YC.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LEAVINGPORT,
                                    thisTask.getContainer()[0].getPresentSlot().getCentralLocation(),
                                    this.getLandSideArea().getTransferPoint1(), null));
                        }
                    } else if (thisTask.getContainer().length == 2
                            && this.yard.getPort().isNeighbour20FT(thisTask.getContainer(), 2) == false) {
//System.out.println(YC.YCname + "get LEAVINGPORT assignedTask 开始!!3---");
                        YC.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LEAVINGPORT,
                                thisTask.getContainer()[0].getPresentSlot().getCentralLocation(),
                                this.getLandSideArea().getTransferPoint1(), null));
                        Container[] container = new Container[1];
                        container[0] = thisTask.getContainer()[1];
                        //Container[] container,String workType,Slot location,Truck truck,Point2D transferPoint
                        this.blockLandsideAssignment.add(0,new LandSideYardTask(container, thisTask.getWorkType(),
                                container[0].getPresentSlot(), thisTask.getTruck(), null));
                    } else {
                        System.out.println("!!!Error:Block.assignTaskToThisYC(YardCrane YC)LEAVINGPORT!!!");
                        throw new UnsupportedOperationException("!!!Error:Block.assignTaskToThisYC(YardCrane YC)!!!");
                    }
//System.out.println(YC.YCname + "get assignedTask 成功");
                    break;
                case StaticName.TOPORT:
                    //出口箱集港;
                    if (thisTask.getContainer().length == 2) {
                        YC.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.TOPORT,
                                thisTask.getTransferPoint(), this.findSuitableFreeSlot(thisTask.getContainer()), null));
                    } else if (thisTask.getContainer().length == 1) {
                        YC.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.TOPORT,
                                thisTask.getTransferPoint(), this.findSuitableFreeSlot(thisTask.getContainer()), null));
                    } else {
                        System.out.println("!!!Error:Block.assignTaskToThisYC(YardCrane YC)TOPORT!!!");
                        throw new UnsupportedOperationException("!!!Error:Block.assignTaskToThisYC(YardCrane YC)!!!");
                    }
//System.out.println(YC.YCname + "get TOPORT assignedTask 成功");
                    break;
            }
        }
    }
    /**
     * 当海测陆侧任务不均匀时，进行该调配;
     * @param yc
     */
    public synchronized void assignOpositeTaskToThisYC(YardCrane yc) {
        if(yc.getNumber().equals(StaticName.WATERSIDE) == true && 
                this.getLandsideYC().isFree() == true){
            return;
        }else if(yc.getNumber().equals(StaticName.LANDSIDE) == true && 
                this.getWatersideYC().isFree() == true){
            return;
        }
        //yc.holdTwoMinute();
        if (yc.getNumber().equals(StaticName.WATERSIDE) == true
                && this.blockLandsideAssignment.isEmpty()) {
            //创造新的任务;同一计划时间下，优先进港任务，再离港任务;
            this.obtainGatheringPortAssignment();
            this.obtainLeavingPortAssignment();
        }
        if ((yc.getNumber().equals(StaticName.LANDSIDE) == true
                && this.blockWatersideAssignment.size()>1)) {
            WaterSideYardTask thisTask;
            thisTask = this.blockWatersideAssignment.remove(1);//.poll();
            switch (thisTask.getWorkType()) {
                case StaticName.LOADING:
                    //装船;
                    //要确保该AGV已经到达Block;
                    if (thisTask.getContainer().length == 2
                            && this.yard.getPort().isNeighbour20FT(thisTask.getContainer(), 2) == true) {
                        System.out.println(yc.YCname + "get LOADING assignedTask 开始！！");
                        yc.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LOADING,
                                this.yard.getPort().getCentralSlotPosition(thisTask.getContainer(), 2),
                                thisTask.getTransferPoint(), thisTask.getAgv()));
                        return;
                    } else if (thisTask.getContainer().length == 1) {
                        System.out.println(yc.YCname + "get LOADING assignedTask 开始");
                        yc.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LOADING,
                                thisTask.getContainer()[0].getPresentSlot().getCentralLocation(),
                                thisTask.getTransferPoint(), thisTask.getAgv()));
                        return;
                    } else if (thisTask.getContainer().length == 2
                            && this.yard.getPort().isNeighbour20FT(thisTask.getContainer(), 2) == false) {
                        System.out.println(yc.YCname + "get LOADING assignedTask 开始！！");
                        yc.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LOADING,
                                thisTask.getContainer()[0].getPresentSlot().getCentralLocation(),
                                thisTask.getTransferPoint(), thisTask.getAgv()));
                        Container[] container = new Container[1];
                        container[0] = thisTask.getContainer()[1];
                        //Container[] container,String workType,Slot location,AGV agv,Point2D transferPoint
                        this.blockWatersideAssignment.add(0,new WaterSideYardTask(container, thisTask.getWorkType(),
                                thisTask.getContainer()[1].getPresentSlot(), thisTask.getAgv(), thisTask.getTransferPoint()));
                        return;
                    } else {
                        System.out.println("!!!Error:Block.assignTaskToThisYC(YardCrane YC)Loading!!!");
                        throw new UnsupportedOperationException("!!!Error:Block.assignTaskToThisYC(YardCrane YC)!!!");
                    }
                case StaticName.UNLOADING:
                    //卸船;
                    System.out.println(yc.YCname + "get Unloading assignedTask 开始");
                    System.out.println(yc.YCname + "thisTask.getContainer()");
                    if (thisTask.getContainer().length == 2) {
                        System.out.println(yc.YCname + "length2 开始");
                        yc.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.UNLOADING,
                                thisTask.getTransferPoint(), this.findSuitableFreeSlot(thisTask.getContainer()), thisTask.getAgv()));
                        return;
                    } else if (thisTask.getContainer().length == 1) {
                        System.out.println(yc.YCname + "length1 开始");
                        yc.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.UNLOADING,
                                thisTask.getTransferPoint(), this.findSuitableFreeSlot(thisTask.getContainer()), thisTask.getAgv()));
                        return;
                    } else {
                        System.out.println("!!!Error:Block.assignTaskToThisYC(YardCrane YC)Unloading!!!");
                        throw new UnsupportedOperationException("!!!Error:Block.assignTaskToThisYC(YardCrane YC)!!!");
                    }
            }
        }
        if ((yc.getNumber().equals(StaticName.WATERSIDE) == true
                && this.blockLandsideAssignment.size()>1)) {
            LandSideYardTask thisTask;
            thisTask = this.blockLandsideAssignment.remove(1);//.poll();
            switch (thisTask.getWorkType()) {
                case StaticName.LEAVINGPORT:
                    //进口箱离港;
                    if (thisTask.getContainer().length == 2
                            && this.yard.getPort().isNeighbour20FT(thisTask.getContainer(), 2) == true) {
                        System.out.println(yc.YCname + "get LEAVINGPORT assignedTask 开始!!1---");
                        yc.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LEAVINGPORT,
                                this.yard.getPort().getCentralSlotPosition(thisTask.getContainer(), 2),
                                this.getLandSideArea().getTransferPoint1(), null));
                    } else if (thisTask.getContainer().length == 1) {
                        System.out.println(yc.YCname + "get LEAVINGPORT assignedTask 开始!!2---");
                        if (thisTask.getContainer()[0].getPresentSlot() == null) {
                            System.out.println(yc.YCname + "::::::__:::::" + thisTask.getContainer()[0]);
                            System.out.println(yc.YCname + "::::::__:::::" + thisTask.getContainer()[0].getState());
                        } else {
                            yc.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LEAVINGPORT,
                                    thisTask.getContainer()[0].getPresentSlot().getCentralLocation(),
                                    this.getLandSideArea().getTransferPoint1(), null));
                        }
                    } else if (thisTask.getContainer().length == 2
                            && this.yard.getPort().isNeighbour20FT(thisTask.getContainer(), 2) == false) {
                        System.out.println(yc.YCname + "get LEAVINGPORT assignedTask 开始!!3---");
                        yc.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.LEAVINGPORT,
                                thisTask.getContainer()[0].getPresentSlot().getCentralLocation(),
                                this.getLandSideArea().getTransferPoint1(), null));
                        Container[] container = new Container[1];
                        container[0] = thisTask.getContainer()[1];
                        //Container[] container,String workType,Slot location,Truck truck,Point2D transferPoint
                        this.blockLandsideAssignment.add(0,new LandSideYardTask(container, thisTask.getWorkType(),
                                container[0].getPresentSlot(), thisTask.getTruck(), null));
                    } else {
                        System.out.println("!!!Error:Block.assignTaskToThisYC(YardCrane YC)LEAVINGPORT!!!");
                        throw new UnsupportedOperationException("!!!Error:Block.assignTaskToThisYC(YardCrane YC)!!!");
                    }
                    System.out.println(yc.YCname + "get assignedTask 成功");
                    break;
                case StaticName.TOPORT:
                    //出口箱集港;
                    if (thisTask.getContainer().length == 2) {
                        yc.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.TOPORT,
                                thisTask.getTransferPoint(), this.findSuitableFreeSlot(thisTask.getContainer()), null));
                    } else if (thisTask.getContainer().length == 1) {
                        yc.setFirstYCTask(new YCTask(thisTask.getContainer(), StaticName.TOPORT,
                                thisTask.getTransferPoint(), this.findSuitableFreeSlot(thisTask.getContainer()), null));
                    } else {
                        System.out.println("!!!Error:Block.assignTaskToThisYC(YardCrane YC)TOPORT!!!");
                        throw new UnsupportedOperationException("!!!Error:Block.assignTaskToThisYC(YardCrane YC)!!!");
                    }
                    System.out.println(yc.YCname + "get TOPORT assignedTask 成功");
                    break;
            }
        }
    }
    /**
     * 创建新的集港作业;
     */
    private synchronized boolean obtainGatheringPortAssignment() {
        if(this.yard.getTruckGatheringTask().isEmpty() == true){
            return false;
        }
        this.blockLandsideAssignment.add(new LandSideYardTask(this.yard.getTruckGatheringTask().poll().getContainer(),
                StaticName.TOPORT,null,null,this.landSideArea.getTransferPoint1()));
        return true;
    }
    /**
     * 搜索是否有离港作业;
     */
    private synchronized boolean obtainLeavingPortAssignment(){
        if(this.getLeavingPortList().isEmpty() == true){
            return false;
        }
        int itag = 0;
        int maxNum = 10;
        int itagNum = 0;
        for(int i = 0;i<this.getLeavingPortList().size();i++){
            if(this.getLeavingPortList().get(i).getContainer()[0].getTime_LeavingBlock()<=simulationRealTime){
                this.blockLandsideAssignment.add(new LandSideYardTask(this.getLeavingPortList().get(i).getContainer(),
                        StaticName.LEAVINGPORT,this.getLeavingPortList().get(i).getSlot(),null,this.landSideArea.getTransferPoint1()));
                this.getLeavingPortList().remove(i);
                itagNum++;
                if(itagNum>maxNum){
                    break;
                }
                i--;
                itag = 1;
            }
        }
        if(itag == 0){
            return false;
        }else{
            return true;
        } 
    }
    /**
     * 找到一个合适的箱位;
     * @param containers
     * @return Point2D;
     */
    public Point2D findSuitableFreeSlot(Container[] containers){
        if(containers.length == 1 && containers[0].getFTsize() == 20){
            //20ft箱*1;
            int rowNum = (int)InputParameters.getRandom(1,(int)(getProportionOf20()*InputParameters.getPerYCRowNum()));
            //int rowNum = (int)InputParameters.getRandom(1,InputParameters.getPerYCRowNum());
            int bayNum = (int)InputParameters.getRandom(1,InputParameters.getTotalBayNum()-1);//[1-30]
            //int bayNum = InputParameters.getRandomIntTriangle(StaticName.TRIANGULAR,1,InputParameters.getTotalBayNum());//[1-30]
            bayNum = bayNum*2-1;
            if(this.findCurrentMaxHeightNum(rowNum,bayNum) < this.YC[0].getMaxContainersHeightNum()){
//System.out.println("找到合适箱位："+"rowNum"+rowNum+"bayNum"+bayNum);
                return this.caculateLocation(rowNum,bayNum);
            }else{
//System.out.println("该1*20ft箱位不合适，继续找："+"rowNum"+rowNum+"bayNum"+bayNum);
                return this.findSuitableFreeSlot(containers);
            }
        }else if(containers.length == 1 && containers[0].getFTsize() == 40){
            //40ft箱*1
            int rowNum = (int)InputParameters.getRandom((int)(getProportionOf20()*InputParameters.getPerYCRowNum()),getPerYCRowNum());
            //int rowNum = (int)InputParameters.getRandom(1,InputParameters.getPerYCRowNum());
            int bayNum = (int)InputParameters.getRandom(1,InputParameters.getTotalBayNum()-1);//[1-30]
            //int bayNum = InputParameters.getRandomIntTriangle(StaticName.TRIANGULAR,1,InputParameters.getTotalBayNum()-1);//[1-30]
            bayNum = bayNum*2;
            if(this.findCurrentMaxHeightNum(rowNum,bayNum) < this.YC[0].getMaxContainersHeightNum()){
//System.out.println("找到合适箱位："+"rowNum"+rowNum+"bayNum"+bayNum);
                return this.caculateLocation(rowNum,bayNum);
            }else{
//System.out.println("该1*40箱位不合适，继续找："+"rowNum"+rowNum+"bayNum"+bayNum);
                return this.findSuitableFreeSlot(containers);
            }
        }else if(containers.length == 2){
            //2个20ft箱;
            int rowNum = (int)InputParameters.getRandom(1, InputParameters.getPerYCRowNum());
            //int rowNum = (int)InputParameters.getRandom(1,InputParameters.getPerYCRowNum());
            int bayNum = (int)InputParameters.getRandom(1,InputParameters.getTotalBayNum()-1);//[1-30]
            //int bayNum = InputParameters.getRandomIntTriangle(StaticName.TRIANGULAR,1,InputParameters.getTotalBayNum()-1);//[1-30]
            bayNum = bayNum*2;
            if(this.findCurrentMaxHeightNum(rowNum,bayNum) < this.YC[0].getMaxContainersHeightNum()){
//System.out.println("找到合适箱位："+"rowNum"+rowNum+"bayNum"+bayNum);
                return this.caculateLocation(rowNum,bayNum);
            }else{
//System.out.println("该2*20ft箱位不合适，继续找："+"rowNum"+rowNum+"bayNum"+bayNum);
                return this.findSuitableFreeSlot(containers);
            }
        }else{
            System.out.println("!!!Error:Block.findSuitableFreeSlot()!!!");
            throw new UnsupportedOperationException("!!!Error:Block.findSuitableFreeSlot()!!!"); 
        }
    }
    private Point2D caculateLocation(int rowNum,int bayNum){
        Point2D location = new Point2D.Double(0, 0);
        double d = 0.5*(InputParameters.getRMGtrackGuage()-InputParameters.getPerYCRowNum()*InputParameters.getWidthOfContainer()
                -InputParameters.getDistanceOfContainers()*(InputParameters.getPerYCRowNum()-1));
        double x = this.getP2().getX()-d-(0.5+(rowNum-1))*InputParameters.getWidthOfContainer()
                -InputParameters.getDistanceOfContainers()*(rowNum-1);
        double y = this.getP1().getY()+0.5*bayNum*InputParameters.getLengthOf20FTContainer()
                +InputParameters.getDistanceOfContainers()*0.5*(bayNum-1);
        location.setLocation(x, y);   
        return location;
    }

    /**
     * @return the line
     */
    public int getLine() {
        return line;
    }

    /**
     * @return the leavingPortList
     */
    public List<LandSideYardTask> getLeavingPortList() {
        return leavingPortList;
    }

    /**
     * @return the rowNumPerBlock
     */
    public int getRowNumPerBlock() {
        return rowNumPerBlock;
    }

    /**
     * @return the bayNumPerRow
     */
    public int getBayNumPerRow() {
        return bayNumPerRow;
    }

    /**
     * @return the lengthOfBlock
     */
    public double getLengthOfBlock() {
        return lengthOfBlock;
    }

    /**
     * @return the widthOfBlock
     */
    public double getWidthOfBlock() {
        return widthOfBlock;
    }
    
    public double getAllContainerWidthOfBlock() {
        return (this.rowNumPerBlock*InputParameters.getWidthOfContainer())+
                ((this.rowNumPerBlock-1)*this.distanceOfContainers);
    }

    /**
     * 对于状态并不在Onblock、OnYC的container,清空;
     * 清空了一个后返回false;
     * 没有需要清空的返回true;
     * @return  
     */
    public synchronized boolean moveProblemContainer() {
        if(this.currentContainers == null){
            return true;
        }
        for (Container currentContainer : this.currentContainers) {
            if (currentContainer.getState().equals(StaticName.ONSTOCKING) == false && 
                    currentContainer.getState().equals(StaticName.ONYC) == false) {
                Container[] c = new Container[1];
                c[0] = currentContainer;
                this.moveContainer(c);
                return false;
            }
            return true;
        }
        return true;
    }
}

    
