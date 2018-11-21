/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vehicle;

import CYT_model.Container;
import CYT_model.Point2D;
import CYT_model.Port;
import static algorithm.formater.format;
import storageblock.AGVCouple;
import apron.QuayCrane;
import java.rmi.RemoteException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.Resource;
import nl.tudelft.simulation.dsol.formalisms.ResourceRequestorInterface;
import nl.tudelft.simulation.dsol.formalisms.process.Process;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import parameter.InputParameters;
import parameter.StaticName;
import storageblock.Yard;
import storageblock.Block;
import static parameter.InputParameters.getRandom;
import static parameter.InputParameters.getSimulationSpeed;
import parameter.OutputParameters;
import static parameter.OutputParameters.outputSingleParameters;
import static parameter.OutputParameters.simulationRealTime;
import ship.Ship;

/**
 * 水平运输系统--主动实体：AGV 主要属性：载箱情况，服务岸桥，位置信息
 *
 * @author YutingChen, 760698296@qq.com Dalian University of Technology
 */
public class AGV extends Process implements ResourceRequestorInterface, VehicleInterface {

    private static final long serialVersionUID = 1L;

    private final Port port;
    private final AGV myself;
    private final String name;
    private final int number;
    private final QuayCrane serviceQC;
    private final String AGVType;
    private final double length = 15;
    private final double width = 3;
    private final double height = 2.3;
    private final double speed0TEU = 5.833333;
    private final double speed2TEU = 3.5;
    private String AGVstate;//Unloading:AGV上有箱，且是卸船箱;//Loading:AGV上有箱，且是装船箱;//Free:AGV上无箱;
    private Point2D presentPosition;//
    private Container[] goalLoadingContainers;//目标Loading装船集装箱;//只在道路至Stocking部分有效
    private int currentCarLine = -1;//当前行驶车道,初始化默认-1,即不在车道上;
    private Yard goalYard;//水平运输过程中目标Block;
    private Block goalBlock;//水平运输过程中目标YardSection;
    private Container[] containersOnAGV;
    
    private int laneNumUnderQC;

    private final static double WAITINGTIMEUNIT = 1;
    //评价指标;
    private String twWorkType;
    private double twTEU;
    private double twPath;
    private double twST;
    private double twET;
    private double twWaitingQCFreeLane;
    private double twWaitingBlockAvailable;
    private double twWaitingQC;
    private double twWaitingYC; 
    private double faiPath;//考虑路段拥堵的修正系数;
    
    private StringBuffer strb;

    /**
     * construct a AGV
     *
     * @param simulator
     * @param name AGV编号; eg.AGVNo.1
     * @param quayCrane 服务岸桥;
     * @param no AGV在QC serviceAGV[]中的编号;
     * @param AGVtype AGV型号;决定AGV参数性能等;
     */
    public AGV(final DEVSSimulator simulator, String name, QuayCrane quayCrane, int no, String AGVtype) {
        super(simulator);
        this.name = name;
        this.myself = this;
        this.AGVstate = StaticName.FREE;
        this.number = this.readNumber();
        this.serviceQC = quayCrane;
        this.port = this.serviceQC.getPort();
        this.AGVType = AGVtype;////决定性能参数;
        this.presentPosition = null;//默认初始位置在服务岸桥下 or 停放区？？
        this.setAGVPostion();//初始化AGV位置;
        this.containersOnAGV = null;//默认初始化时AGV上无箱;
        this.goalLoadingContainers = null;
        this.serviceQC.addServiceAGV(this.myself, no);
        this.port.addSeriveAGV(this.myself);
        this.laneNumUnderQC = 0;
        this.faiPath = 1.1;
        double[] parameters = {this.length, this.width, this.height, this.speed0TEU, this.speed2TEU};
        this.strb = new StringBuffer();
        outputSingleParameters("AGVInput", parameters);
    }

    /**
     * AGV Process including 装船进程 & 卸船进程
     *
     * @throws RemoteException
     * @throws SimRuntimeException
     */
    @Override
    public void process() throws RemoteException, SimRuntimeException, NullPointerException {

        this.goalYard = null;
        this.goalLoadingContainers = null;
        this.goalBlock = null;

        this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
        if (simulationRealTime == WAITINGTIMEUNIT) {
//System.out.println(this.toString() + "AGV_process() 开始！！" + simulationRealTime);
        }
        /**
         * AGV无任务时默认进入停放区域; 收到任务后再进行任务;
         */
        //服务岸桥没有任务,AGV不在指定缓冲停放区;
        if (this.serviceQC.getCraneState() == 0//该岸桥未被分配任务;
                && this.isOnBufferArea() == false) {
            //---------AGV水平移动至停放区域---------
            Point2D startPoint = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
            this.hideAGVPosition();
            this.holdMoveToBufferArea(startPoint);
            //System.out.println(this.toString() + "成功移至AGV停放区" + simulationRealTime);
        }
        //等待服务岸桥被分配;
        while (this.serviceQC.getCraneState() == 0) {
            this.hold(3.0 / getSimulationSpeed());//等待岸桥被分配;
        }
        //岸桥被分配成功(craneState = 1)
        //判断:岸桥是否已开始装卸作业？
        while (this.serviceQC.getServiceShip().shipState.equals(StaticName.FREE) == true) {
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());//等待船舶开始进行装/卸作业;
        }
        if (this.serviceQC.getServiceShip().shipState.equals(StaticName.UNLOADING) == true
                && this.serviceQC.getServiceShip().getShipContainers().getTotal_unload_num() > 0) {
            //卸船进程--从QC至堆场;
            this.process_Unloading();
        } else if (this.serviceQC.getServiceShip().shipState.equals(StaticName.LOADING) == true
                && this.serviceQC.getServiceShip().getShipContainers().getTotal_load_num() > 0) {
            //装船进程--从堆场至QC;
            this.process_Loading();
//System.out.println(this.toString() + " AGV装船任务结束 return true end" + simulationRealTime);
        } else {
            //服务岸桥与船舶已解除关联 或 服务岸桥的停靠船舶目前没有装卸任务;
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
            this.process();
        }
        //服务岸桥的停靠船舶装/卸作业水平运输任务已结束 或根本没有装卸任务;
//System.out.println(this.toString() + " 该AGV任务结束！" + simulationRealTime);
        if (this.serviceQC.getServiceShip() != null
                && (this.serviceQC.getServiceShip().shipState.equals(StaticName.UNLOADING) == true
                || this.serviceQC.getServiceShip().shipState.equals(StaticName.LOADING) == true)) {
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
        }
        //该轮任务结束;默认先回到缓冲区;
        if (this.isOnBufferArea() == false) {
            //事件：AGV准备移至QC缓冲区;
//System.out.println(this.toString() + " AGV开始驶向QC缓冲区！" + simulationRealTime);
            //活动：---------------AGV移至岸桥缓冲区域-----------------
            Point2D startPoint1 = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
            this.hideAGVPosition();
            this.holdMoveToBufferArea(startPoint1);
        }
        //重新回到进程中;
        this.process();
    }

    /**
     * AGV卸船流程; 完成该服务船舶卸船后回到主进程中; return to AGV.process();
     */
    private boolean process_Unloading() throws SimRuntimeException, RemoteException {
        this.goalYard = null;
        this.goalLoadingContainers = null;
        this.goalBlock = null;
        if (this.isOnBufferArea() == false) {
            //事件：AGV准备移至QC缓冲区;
            //System.out.println(this.toString() + " AGV开始驶向QC缓冲区！" + simulationRealTime);
            //活动：---------------AGV移至岸桥缓冲区域-----------------
            Point2D startPoint1 = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
            this.hideAGVPosition();
            this.holdMoveToBufferArea(startPoint1);
        }
        this.twWorkType = StaticName.UNLOADING;
        this.twST = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
        this.twPath = 0;
        this.twWaitingQC = 0;
        this.twWaitingQCFreeLane = 0;
        this.twWaitingBlockAvailable = 0;
        this.twWaitingYC = 0;
        //判断:QC下位置是否足够？
        while (this.serviceQC.getPresentWorkingOrToWorkAGVNum() == InputParameters.getMaxPresentWorkingAGV()) {
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
            this.twWaitingQCFreeLane += WAITINGTIMEUNIT;
        }
        this.setLaneNumUnderQC(1);
        //事件：AGV准备移至QC装卸区;
//System.out.println(this.toString() + " AGV开始驶向QC装卸区！" + simulationRealTime);
        //活动：---------------AGV移至岸桥装卸区域-----------------
        Point2D startPoint = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
        if (port.getAGVBufferArea().isParking(this.myself) == true) {
            port.getAGVBufferArea().releaseOneParking(this.myself);
        }else{
//System.out.println(this.toString() + " AGVUnloading不在buffer区！" + simulationRealTime);
        }
        this.hideAGVPosition();
        this.holdMoveToUnderQC(startPoint);
        
        //事件：AGV到达岸桥下面;
//System.out.println(this.toString() + " AGV到QC下面啦！" + simulationRealTime);
        //活动：等待门架小车装箱至AGV;
        while (this.getContainersOnAGV() == null) {
//            System.out.println(this.toString()+" 等待门架小车成功装箱至AGV！"+super.simulator.getSimulatorTime());
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());//等待;
            this.twWaitingQC += WAITINGTIMEUNIT;
            if(this.isArrivedUnderQC() == false){
                this.presentPosition.setLocation(this.presentPosition.getX(),
                        this.serviceQC.getServiceLocation().getY());
            }
            if (this.serviceQC.getServiceShip() == null
                    || this.serviceQC.getServiceShip().shipState.equals(StaticName.UNLOADING) == false) {
//System.out.println(this.toString() + "没有卸船任务,回到原始进程中");
                this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
                this.setLaneNumUnderQC(0);
//System.out.println("!!!!!________"+this.toString()+this.laneNumUnderQC);
                return true;
            }
        }
        //事件：门架小车成功装箱至AGV
        this.setAGVstate(StaticName.UNLOADING);
//System.out.println(this.toString() + " AGV有箱啦!开始水平运输啦!" + simulationRealTime);
        this.setLaneNumUnderQC(0);
        this.twTEU = this.getContainerTEU();
        //活动：----------------水平运输----From 前沿 To 堆场---------------------
        //前沿-缓冲区;
        Point2D startPoint1 = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
        this.hideAGVPosition();
        this.holdMoveToBufferArea(startPoint1);
        port.getAGVBufferArea().releaseOneParking(this.myself);
        //缓冲区-堆场入口;
        startPoint = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
        this.hideAGVPosition();
        this.holdMoveToStockingArea(startPoint);
//System.out.println(this.toString() + " AGV运到堆场区域！堆场开始分配Block位置让AGV放箱！" + simulationRealTime);
        while (this.assignUnloadingBlock() == false) {
//System.out.println(this.toString() + "----------Unloading堆场block分配位置失败，继续分配!" + simulationRealTime);
            //堆场分配位置失败;
            this.hold(WAITINGTIMEUNIT);//等待;
            this.twWaitingBlockAvailable += WAITINGTIMEUNIT;
        }
        //堆场分配block成功;
//System.out.println(this.toString() + "----------Unloading堆场block分配位置成功!" + this.goalBlock.toString() + simulationRealTime);
        //驶向指定位置;
        //活动：----------------水平运输----From 堆场入口 To 指定Block-------------
        while (this.goalBlock.getWaterSideArea().getOneFreeTransferPoint(this.myself) == null) {
//System.out.println(this.toString() + "-----------等待空闲TransferPoint!" + this.goalBlock.toString() + simulationRealTime);
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
            this.twWaitingBlockAvailable += WAITINGTIMEUNIT;
        }
        startPoint = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
        this.hideAGVPosition();
        Point2D goalPoint = new Point2D.Double(0, 0);
        goalPoint.setLocation(this.goalBlock.getWaterSideArea().getAndBookOneFreeTransferPoint(this.myself));
        //Book失败时返回Point2D(0,0);
        while(goalPoint.getX() == 0 && goalPoint.getY() == 0){
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
            this.twWaitingBlockAvailable += WAITINGTIMEUNIT;
            goalPoint.setLocation(this.goalBlock.getWaterSideArea().getAndBookOneFreeTransferPoint(this.myself));
        }
        //block添加任务成功;
        this.holdMoveToTransferPoint(startPoint,goalPoint);
        //获取transferPoint;
        this.goalBlock.getWaterSideArea().obtainThisTransferPoint(this.myself);
        this.addUnloadingTaskOfRelativeBlock(this, goalPoint);
        this.setAGVPostion(goalPoint);
//System.out.println(this.toString() + " AGV运到堆场SectionTransferPoint区域!" + simulationRealTime);
        //根据指定位置不同来进行不同活动;
        //等待场桥提箱or等待AGV伴侣提箱;
        while (this.getContainerNum() != 0) {
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
            this.twWaitingYC += WAITINGTIMEUNIT;
            //若在AGV伴侣下： 
            if (this.goalBlock.getWaterSideArea().findTransferPointNum(this.myself) == 3) {
                //在有AGV伴侣的位置;
                if (this.goalBlock.getAGVCouple()[0].getCurrentContainers() == null) {
                    //AGV伴侣上无箱，可以从AGV直接提箱;
                    this.hold(AGVCouple.Time_dragContainers / getSimulationSpeed());
                    this.twWaitingYC += AGVCouple.Time_dragContainers;
//System.out.println(this.toString() + "AGV伴侣hold结束!" + simulationRealTime);
                    this.goalBlock.getAGVCouple()[0].obtainContainers(containersOnAGV);
                    //事件：放箱成功,AGV Containers变为空;
                    this.clearContainers();
                    this.containersOnAGV = null;
//System.out.println(this.toString() + "AGV伴侣提箱成功!" + simulationRealTime);
                }
            }else if (this.goalBlock.getWaterSideArea().findTransferPointNum(this.myself) == 2) {
                //在有AGV伴侣的位置;
                if (this.goalBlock.getAGVCouple()[1].getCurrentContainers() == null) {
                    //AGV伴侣上无箱，可以从AGV直接提箱;
                    this.hold(AGVCouple.Time_dragContainers / getSimulationSpeed());
                    this.twWaitingYC += AGVCouple.Time_dragContainers;
//System.out.println(this.toString() + "AGV伴侣hold结束!" + simulationRealTime);
                    this.goalBlock.getAGVCouple()[1].obtainContainers(containersOnAGV);
                    //事件：放箱成功,AGV Containers变为空;
                    this.clearContainers();
                    this.containersOnAGV = null;
//System.out.println(this.toString() + "AGV伴侣提箱成功!" + simulationRealTime);
                }
            }
//System.out.println(this.toString() + " 等待放箱成功");
        }
        //transferpoint解除锁定，AGV准备驶离堆场
        this.goalBlock.setVehicleLeave(this.myself);
        this.goalYard = null;
        this.goalBlock = null;
        this.setAGVstate(StaticName.FREE);
//System.out.println(this.toString() + " 放箱成功啦！" + simulationRealTime);
        //回到bufferArea
        if (this.isOnBufferArea() == false || this.port.getAGVBufferArea().isInThisRectangle(presentPosition) == false) {
            //事件：AGV准备移至QC缓冲区;
            //System.out.println(this.toString() + " AGV开始驶向QC缓冲区！" + simulationRealTime);
            //活动：---------------AGV移至岸桥缓冲区域-----------------
            Point2D startPoint2 = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
            this.hideAGVPosition();
            this.holdMoveToBufferArea(startPoint2);
        }
        this.twET = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
        this.addOneOutputAGVWorklist();
        
        //-------------------结束该次循环，判断是否继续循环-----------------------
        //判断:还有卸船任务？？
        if (this.serviceQC.getServiceShip() != null
                && this.serviceQC.getServiceShip().shipState.equals(StaticName.UNLOADING) == true
                && this.serviceQC.isGantryTrolleyFree() == false) {
            //还有卸船任务，继续卸船进程的循环;
//System.out.println(this.toString() + "有卸船任务");         
            this.process_Unloading();
        } else {
            //没有卸船任务;
//System.out.println(this.toString() + "没有卸船任务,回到原始进程中");
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
        }
        //结束循环，回到主进程中
        return true;
    }

    /**
     * AGV装船流程; From堆场To前沿 （&Come back to 堆场） 完成装船任务后回到主进程中
     */
    private boolean process_Loading() throws SimRuntimeException, RemoteException {
        this.goalYard = null;
        this.goalLoadingContainers = null;
        this.goalBlock = null;
        if (this.isOnBufferArea() == false) {
            //事件：AGV准备移至QC缓冲区;
//System.out.println(this.toString() + " AGV开始驶向QC缓冲区！" + simulationRealTime);
            //活动：---------------AGV移至岸桥缓冲区域-----------------
            Point2D startPoint1 = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
            this.hideAGVPosition();
            this.holdMoveToBufferArea(startPoint1);
        }
        //判断：分配箱子及目的地成功？
        while (this.goalLoadingContainers == null && this.assignLoadingConatainer() == false) {
            //分配失败,回到主进程中;
            if (this.serviceQC.getServiceShip().getShipContainers().allLoadingContainersOnShip() == false) {
                //还有装船任务但container还未在堆场里;
                //重新判断;
                this.hold(AGV.WAITINGTIMEUNIT / getSimulationSpeed());
            } else {
//System.out.println(this.toString() + " AGV装船任务结束！" + simulationRealTime);
                break;
            }
        }
        if (this.serviceQC.getServiceShip() == null ||
                this.serviceQC.getServiceShip().getShipContainers().allLoadingContainersOnShip() == true) {
            return true;
        }
        //目标箱分配成功;
        this.twWorkType = StaticName.LOADING;
        this.twST = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
        this.twPath = 0;
        this.twWaitingQC = 0;
        this.twWaitingQCFreeLane = 0;
        this.twWaitingBlockAvailable = 0;
        this.twWaitingYC = 0;
        //事件：AGV准备移至堆场：
//System.out.println(this.toString() + " AGV开始驶向堆场！" + simulationRealTime);
        //活动：---------------水平运输：AGV移至堆场TransferPoint-----------------
        while (this.goalBlock.getWaterSideArea().getOneFreeTransferPoint(this.myself) == null) {
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
            this.twWaitingBlockAvailable += WAITINGTIMEUNIT;
//System.out.println(this.toString() + " AGV等待堆场有位置！" + simulationRealTime); 
        }
        port.getAGVBufferArea().releaseOneParking(this.myself);
        Point2D startPoint = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
        this.hideAGVPosition();
        Point2D goalPoint = new Point2D.Double(0, 0);
        goalPoint.setLocation(this.goalBlock.getWaterSideArea().getAndBookOneFreeTransferPoint(this.myself));
        //Book失败时返回Point2D(0,0);
        while(goalPoint.getX() == 0 && goalPoint.getY() == 0){
            this.hold(AGV.WAITINGTIMEUNIT / getSimulationSpeed());
            this.twWaitingBlockAvailable += WAITINGTIMEUNIT;
//System.out.println(this.toString() + " AGV等待堆场Book成功！" + simulationRealTime);
            goalPoint.setLocation(this.goalBlock.getWaterSideArea().getAndBookOneFreeTransferPoint(this.myself));
        }
        this.holdMoveToTransferPoint(startPoint, goalPoint);
        //获取transferPoint;
        this.goalBlock.getWaterSideArea().obtainThisTransferPoint(this.myself);
        this.addLoadingTaskOfRelativeBlock(this.myself, goalPoint);
        //事件：AGV到达堆场装卸点;
//System.out.println(this.toString() + " AGV到堆场TransferPoint!等待场桥放箱至AGV" + simulationRealTime);
        //活动：---------------等待堆场装卸子系统装箱成功-------------------------
        //(包括场桥移动和AGV装箱 两部分)
        /**
         * 由场桥和AGV伴侣完成; this.containerOnAGV状态变化;
         */
        while (this.containersOnAGV == null
                || this.getContainerNum() != this.goalLoadingContainers.length) {
            //如果箱未装满也继续等待;
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());//等待;
            this.twWaitingYC += WAITINGTIMEUNIT;
            if(this.serviceQC.getServiceShip() == null){
                //说明该任务不用再判断了，回到原进程中去;
                return false;
            }
        }
//System.out.println(this.toString() + " YC/AGVCouple成功装箱至AGV！" + simulationRealTime);
        //事件：场桥/AGV伴侣 成功装箱至AGV，开始水平运输
        this.setAGVstate(StaticName.LOADING);
        this.goalBlock.setVehicleLeave(this.myself);
        this.goalYard = null;
        this.goalBlock = null;
        this.obtainContainersfromGoalContainers();
//System.out.println(this.toString() + " AGV开始驶向QC缓冲区！" + simulationRealTime);
        //活动：----------------------AGV移至前沿缓冲区域-------------------------
        Point2D startPoint1 = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
        this.hideAGVPosition();
        this.holdMoveToBufferArea(startPoint1);//移至QC下;AGV位置更新;
        this.twTEU = this.getContainerTEU();
        //判断:QC下位置是否足够？
        while (this.serviceQC.getPresentWorkingOrToWorkAGVNum() == InputParameters.getMaxPresentWorkingAGV()) {
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
            this.twWaitingQCFreeLane += WAITINGTIMEUNIT;
//System.out.println(this.toString() + " 等待服务岸桥下有空闲位置!当前workingAGV数量为："+ this.serviceQC.getPresentWorkingOrToWorkAGVNum() + "@" + simulationRealTime);
        }
        this.setLaneNumUnderQC(1);
        //事件：AGV准备移至QC装卸区;
//System.out.println(this.toString() + " AGV开始驶向QC装卸区！" + simulationRealTime);
        //活动：-----------------水平运输-From缓冲区ToQC装卸区-------------------- 
        startPoint = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
        port.getAGVBufferArea().releaseOneParking(this.myself);
        this.hideAGVPosition();
        this.holdMoveToUnderQC(startPoint);
        //事件：AGV到达岸桥下
//System.out.println(this.toString() + " AGV到QC下啦！等待门架小车提箱！" + simulationRealTime);
        //等待门架小车提箱;
        while (this.getContainersOnAGV() != null) {
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());//等待;
            this.twWaitingQC += WAITINGTIMEUNIT;
            if(this.isArrivedUnderQC() == false){
                this.presentPosition.setLocation(this.presentPosition.getX(),
                        this.serviceQC.getServiceLocation().getY());
            }
        }
        //事件：岸桥成功提走箱，AGV载箱状态变为空;
        //(已经在QCWork部分clearContainer())
        this.setAGVstate(StaticName.FREE);
//System.out.println(this.toString() + " 装船成功啦！" + simulationRealTime);
        this.setLaneNumUnderQC(0);
        //Move To Buffer Area
        if (this.isOnBufferArea() == false || this.port.getAGVBufferArea().isInThisRectangle(presentPosition) == false) {
            //事件：AGV准备移至QC缓冲区;
            //System.out.println(this.toString() + " AGV开始驶向QC缓冲区！" + simulationRealTime);
            //活动：---------------AGV移至岸桥缓冲区域-----------------
            Point2D startPoint2 = new Point2D.Double(this.presentPosition.getX(), this.presentPosition.getY());
            this.hideAGVPosition();
            this.holdMoveToBufferArea(startPoint2);
        }
        //结束该次循环,判断是否进行卸船循环;
        this.twET = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
        this.addOneOutputAGVWorklist();
        //判断:是否还有装船任务，决定要不要继续卸船流程的循环;
        this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
        if (this.serviceQC.getServiceShip() != null) {
            this.process_Loading();
        } else {
//System.out.println(this.toString() + "结束装船任务！回到原始进程中" + simulationRealTime);
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
        }
        return true;
    }

    /**
     * @see nl.tudelft.simulation.dsol.formalisms.ResourceRequestorInterface
     * #receiveRequestedResource(double,
     * nl.tudelft.simulation.dsol.formalisms.Resource)
     */
    @Override
    public void receiveRequestedResource(final double requestedCapacity,
            final Resource resource) {
        this.resume();
    }

    /**
     * 若空载，则throw Exception
     *
     * @return [double] 20--20ftContainer 40--40ftContainer
     */
    public double getContainerType() {
        if (this.getContainersOnAGV() == null) {
            System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!No container On " + this.toString());
            throw new UnsupportedOperationException("!!!!! AGV.getContainerType Not supported yet.");
        } else {
            return this.getContainersOnAGV()[0].getFTsize();
        }
        
    }

    /**
     * 若空载，return 0;
     *
     * @return double get Number of container on AGV at present
     */
    public double getContainerNum() {
        if (this.getContainersOnAGV() == null) {
            return 0;
        } else {
            return this.getContainersOnAGV().length;
        }
    }
    /**
     * 变为空载状态; 清空ContainerOnAGV信息;及ContainerOnAGV中的AGV信息; 原本就是空载状态时throw
     * Exception
     */
    public void clearContainers() {
        for (int i = 0; i < this.getContainerNum(); i++) {
            this.containersOnAGV[i].setServiceAGV(null);
            if (this.getAGVstate().equals(StaticName.LOADING) == true) {
                //装船进程：在前沿时变为空载;
                this.containersOnAGV[i].setState(StaticName.ONQC);
                this.containersOnAGV[i].setServiceAGV(null);
                this.containersOnAGV[i].setServiceQC(this.serviceQC);
            } else if (this.containersOnAGV[i].getState().equals(StaticName.ONYC) == true) {
                //卸船进程;container到YC上了，变为空载;
                this.containersOnAGV[i].setServiceAGV(null);
            } else if (this.containersOnAGV[0].getState().equals(StaticName.ONAGVCOUPLE) == true) {
                //卸船进程;container到AGVCouple上了，变为空载;
                this.containersOnAGV[i].setServiceAGV(null);
            } else {
                throw new UnsupportedOperationException("!!!!!Error: AGV.clearContainer()!!!!");
            }
        }
        //清空载箱信息;
        this.containersOnAGV = null;
    }

    /**
     * @return AGVNo.**
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @return AGVNo.** QCNO.**
     */
    @Override
    public String toString() {
        return this.getName() + this.serviceQC.toString();
    }

    /**
     * @return 浅复制当前位置;
     */
    @Override
    public Point2D getPresentPosition() {
        return this.presentPosition;
    }

    /**
     * @return 浅复制服务岸桥
     */
    public QuayCrane getServiceQC() {
        return serviceQC;
    }

    /**
     * @return String AGVType 型号;
     */
    public String getAGVType() {
        return AGVType;
    }

    /**
     * private 初始化AGV位置在缓冲区;
     */
    private void setAGVPostion() {
        //初始位置在停放区:(2,****)
        this.presentPosition = new Point2D.Double(0, 0);
        this.presentPosition.setLocation(port.getAGVBufferArea().bookedOneParking(this.myself));
        this.presentPosition.setLocation(port.getAGVBufferArea().haveOneParking(this.myself));
//System.out.println("!!!!!!AGV初始Position认为在服务岸桥的缓冲区！！！！");
    }

    /**
     * change AGV presentposition into destination
     * deep copy
     * @param destination 目的地
     */
    public void setAGVPostion(Point2D destination) {
        this.presentPosition = new Point2D.Double(destination.getX(), destination.getY());
    }

    /**
     * private 从AGVname中读取AGVNumber;
     *
     * @return int number
     */
    private int readNumber() {
        int num;
        String str = this.getName();
        str = str.trim();
        String str2 = "";
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) >= 48 && str.charAt(i) <= 57) {
                str2 += str.charAt(i);
            }
        }
        num = Integer.parseInt(str2);
        return num;
    }

    public int getNumber() {
        return number;
    }

    /**
     * 判断到达服务QC下面的装卸点;
     *
     * @return arrived:true notArrived:false
     */
    public boolean isArrivedUnderQC() {
        for (int i = 1; i <= InputParameters.getMaxPresentWorkingAGV(); i++) {
            if (this.presentPosition.getY() == this.serviceQC.getServiceLocation().getY()) {
                if (this.presentPosition.getX() == this.port.getAGVUnderQCAreaPositionX(i)) {
                    return true;
                }
            }
        }
        return false;
    }
    /**
     * 判断AGV是否在停放区;
     *
     * @return true:在停放区; false:不在停放区;
     */
    public boolean isOnBufferArea() {
        for (AGV haveCar : port.getAGVBufferArea().getHaveCar()) {
            if(haveCar != null && haveCar.equals(this.myself) == true){
                return true;
            }
        }
        return false;
    }

    /**
     * AGV状态：载有卸船箱，载有装船箱，空载
     *
     * @return the AGVstate UNLOADING:AGV上有箱，且是卸船箱; LOADING:AGV上有箱，且是装船箱;
     * FREE:AGV上无箱;
     */
    public String getAGVstate() {
        return this.AGVstate;
    }

    /**
     * @param AGVstate the AGVstate to set UNLOADING:AGV上有箱，且是卸船箱;
     * LOADING:AGV上有箱，且是装船箱; FREE:AGV上无箱;
     */
    private void setAGVstate(String AGVstate) {
        this.AGVstate = AGVstate;
    }

    /**
     * AGV获得QC上的集装箱; 改变集装箱状态; 改变AGV载箱信息;
     *
     * @param containers:获取的集装箱数组;
     */
    public void obtainContainersFromQC(Container[] containers) {
        int num = containers.length;
        if (num <= 0) {
            System.out.println("!!!!!!Error:AGV.obtainContainersFromQC()!!!!!!");
            throw new UnsupportedOperationException("!!!!!!Error:AGV.obtainContainersFromQC()!!!!!!");
        }
        this.containersOnAGV = new Container[num];
        for (int i = 0; i < num; i++) {
            this.containersOnAGV[i] = containers[i];
            containers[i].setState(StaticName.ONAGV);
            containers[i].setServiceQC(null);
            containers[i].setServiceAGV(this.myself);
        }
    }

    /**
     * 获得YC上的集装箱 改变集装箱状态 改变AGV载箱信息
     *
     * @param containers:获取的集装箱数组;
     * @throws java.rmi.RemoteException
     */
    //加container,不删除AGV原有的container;
    public void obtainContainersFromYC(Container[] containers) throws RemoteException {
        int num = containers.length;
        if (num <= 0) {
            System.out.println("!!!!!!Error:AGV.obtainContainersFromYC()!!!!!!");
            throw new UnsupportedOperationException("!!!!!!Error:AGV.obtainContainersFromYC()!!!!!!");
        }
        if (this.containersOnAGV != null
                && this.containersOnAGV.length == 1) {
            Container containerBefore = this.containersOnAGV[0];
            this.containersOnAGV = new Container[2];
            this.containersOnAGV[0] = containerBefore;
            this.containersOnAGV[1] = containers[0];
            if (containers.length > 1
                    || this.containersOnAGV[0].getFTsize() == 40
                    || containers[0].getFTsize() == 40) {
                System.out.println("!!!!!!Error:AGV.obtainContainersFromYC()!!!!!!");
                System.out.println("!!!!!!Error:AGV.obtainContainersFromYC()!!!!!!");
                throw new UnsupportedOperationException("!!!!!!Error:AGV.obtainContainersFromYC()!!!!!!");
            } else {
                return;
            }
        }
        this.containersOnAGV = new Container[num];
        for (int i = 0; i < num; i++) {
            this.containersOnAGV[i] = containers[i];
            containers[i].setState(StaticName.ONAGV);
            containers[i].setServiceQC(null);
            containers[i].setServiceYC(null);
            containers[i].setServiceAGV(this.myself);
            containers[i].setTime_LeavingBlock(simulationRealTime);
        }
    }

    /**
     * AGV移动至缓冲区;
     * @param startPoint
     * @throws nl.tudelft.simulation.dsol.SimRuntimeException
     * @throws java.rmi.RemoteException
     */
    public void holdMoveToBufferArea(Point2D startPoint) throws SimRuntimeException, RemoteException {
        //System.out.println("---------------------------------------------------------------------------");
        Point2D endPoint = new Point2D.Double(0, 0);
        if(port.getAGVBufferArea().haveFreeParking() == false){
            this.hold(format(WAITINGTIMEUNIT/InputParameters.getSimulationSpeed()));
            this.twWaitingBlockAvailable += WAITINGTIMEUNIT;
//System.out.println("等空闲");
        }
        //Book one parking
        endPoint.setLocation(port.getAGVBufferArea().bookedOneParking(this.myself));
        double pathLength = port.getRoadNetWork().minPathLength(startPoint, endPoint, StaticName.AGVCAR);
        this.twPath += pathLength;       
//System.out.print("-------MoveToBufferArea-pathLength " + pathLength);
        double minute = pathLength/(this.getSpeed_now()*60);
        double minuteAfter = simulationRealTime + minute;
        double nowSimulationSpeed = InputParameters.getSimulationSpeed();
        double totalTime = minute/nowSimulationSpeed;
//System.out.println("--------MoveToBufferArea-minute " + minute);
        int num = (int) (totalTime / InputParameters.animationTimeUnit);
        //System.out.println("num:"+num);
        if(InputParameters.isNeedAnimation() == false){
            num = 0;
        }
        //int i = 0; or = 1????
        for (int i = 0; i < num; i++) {
            if(num <= 1){
                num = 0;
                break;
            }
            Point2D nowPoint = port.getRoadNetWork().calculateNowPoint(startPoint, endPoint, this.myself,
                    pathLength, nowSimulationSpeed*(this.getSpeed_now() * 60)*i*InputParameters.animationTimeUnit);  
            this.setAGVPostion(nowPoint);
            //System.out.println("i:"+i+"---nowPoint:"+nowPoint);
            this.hold(InputParameters.animationTimeUnit);
            if(InputParameters.getSimulationSpeed() != nowSimulationSpeed){
                //说明此刻有改变仿真速度;
                totalTime = ((minute-(i+1)*InputParameters.animationTimeUnit*nowSimulationSpeed)/getSimulationSpeed())+
                   (i+1)*InputParameters.animationTimeUnit;
                num = (int)((totalTime/InputParameters.animationTimeUnit)-(i+1)*(getSimulationSpeed()/nowSimulationSpeed));
                i = (int) (nowSimulationSpeed*(this.getSpeed_now() * 60)*i*InputParameters.animationTimeUnit / getSimulationSpeed());
                nowSimulationSpeed = getSimulationSpeed();
                this.setAGVPostion(port.getRoadNetWork().calculateNowPoint(startPoint, endPoint, this.myself,
                    pathLength, nowSimulationSpeed*(this.getSpeed_now() * 60)*i*InputParameters.animationTimeUnit));
            }
            if(simulationRealTime>minuteAfter){
                break;
            }
        }
        if (totalTime > ((num) * InputParameters.animationTimeUnit)) {
            this.hold(format(totalTime - ((num) * InputParameters.animationTimeUnit)));
        }
        port.getAGVBufferArea().haveOneParking(this.myself);
        this.setAGVPostion(endPoint);
    }
    /**
     * @return 移动到指定装卸点所需时间;
     */

    /**
     * 给AGV分配该轮装船任务中还未分配的Container
     * ----------------------------------------------------------------先分配堆场里的;
     * ----------------------------------------------------------------2*20ft时：必须分配在同一个YardSection里;
     * 改变Container.service AGV值 改变AGV.GoalContainer值
     *
     * @return true 分配成功，关联成功;
     * @return false 分配失败,没有关联;
     */
    private boolean assignLoadingConatainer() {
        Ship ship = this.serviceQC.getServiceShip();
        if (ship != null) {
            if (ship.getShipContainers().getLoadingContainersOnStocking() != null) {
//System.out.println("COnStocking:" + this.serviceQC.getServiceShip(). getShipContainers().getLoadingContainersOnStocking().length);
                //还存在需要装船的，已在堆场中的，并且还未被分配的container;
                Container[] containerOnYard = new Container[ship.
                        getShipContainers().getLoadingContainersOnStocking().length];
                for (int i = 0; i < containerOnYard.length; i++) {
                    containerOnYard[i] = ship.getShipContainers().
                            getLoadingContainersOnStocking()[i];
                }
                int num20FT = 0;
                int[] no20FT = new int[2];
                for (int i = 0; i < containerOnYard.length; i++) {
                    //找出最先的集装箱;1个40ft或2*20ft;实在没有再找20ft;
                    if (containerOnYard[i].getServiceAGV() == null
                            && containerOnYard[i].getFTsize() == 40) {
                        //尚未被分配的需要装船的40ftContainer;
                        containerOnYard[i].setServiceAGV(this.myself);//标记Conatainer
                        this.goalLoadingContainers = new Container[1];
                        this.goalLoadingContainers[0] = containerOnYard[i];//标记AGV.goalLoadingContainer;
                        this.goalYard = this.goalLoadingContainers[0].getPresentSlot().getYard();
                        this.goalBlock = this.goalLoadingContainers[0].getPresentSlot().getBlock();
//System.out.println(this.toString() + "分配成功:1*40ft Container");
                        return true;
                    } else if (containerOnYard[i].getServiceAGV() == null
                            && containerOnYard[i].getFTsize() == 20) {
                        //尚未被分配的20ftContainer;
                        no20FT[num20FT] = i;
                        num20FT++;
                        if (num20FT == 2) {
                            if (containerOnYard[no20FT[0]].getPresentSlot().getBlock().getP1().equals(
                                    containerOnYard[no20FT[1]].getPresentSlot().getBlock().getP1()) == true) {
                                //在一个区段里;
                                this.goalLoadingContainers = new Container[2];
                                this.goalLoadingContainers[0] = containerOnYard[no20FT[0]];
                                this.goalLoadingContainers[1] = containerOnYard[no20FT[1]];//标记AGV.goal;
                                containerOnYard[no20FT[0]].setServiceAGV(this.myself);//标记container;
                                containerOnYard[no20FT[1]].setServiceAGV(this.myself);
                                this.goalYard = this.goalLoadingContainers[0].getPresentSlot().getYard();
                                this.goalBlock = this.goalLoadingContainers[0].getPresentSlot().getBlock();
//System.out.println(this.toString() + "分配成功:2*20ft Container");
                                return true;
                            } else {
                                //不在一个section里;
                                num20FT--;
                            }
                        }
                    } else {
                    }
                }
                if (num20FT == 1) {
                    containerOnYard[no20FT[0]].setServiceAGV(this.myself);//标记Conatainer
                    this.goalLoadingContainers = new Container[1];
                    this.goalLoadingContainers[0] = containerOnYard[no20FT[0]];//标记AGV.goalContainers;
                    this.goalYard = this.goalLoadingContainers[0].getPresentSlot().getYard();
                    this.goalBlock = this.goalLoadingContainers[0].getPresentSlot().getBlock();
//System.out.println(this.toString() + "分配成功:1*20ft Container");
                    return true;
                } else {
                    //待装船的container都已经分配完毕;
                    return false;
                }
            } else {
//System.out.println(this.toString()+"堆场里面没有需要装船的Container");
                return false;//堆场里面已经没有需要装船的Container了;
            }
        } else {
            return false;
            //throw new UnsupportedOperationException("!!!Error:AGV.assignLoadingConatainer()!!!");
        }
    }

    /**
     * @return the containersOnAGV
     */
    public Container[] getContainersOnAGV() {
        return containersOnAGV;
    }

    /**
     * private 装船阶段 成功获取目标箱 改变ContainerOnAGV状态 将目标箱数组重置为null
     *
     * @param goalContainers
     */
    private void obtainContainersfromGoalContainers() {
        int num = this.getGoalLoadingContainers().length;
        if (num <= 0) {
            System.out.println("!!Error:AGV.obtainContainersFromGoadContainers()!!失败！！！");
            return;
        }
        this.containersOnAGV = new Container[num];
        for (int i = 0; i < num; i++) {
            this.containersOnAGV[i] = this.getGoalLoadingContainers()[i];
            this.getGoalLoadingContainers()[i].setState(StaticName.ONAGV);
            this.getGoalLoadingContainers()[i].setServiceYC(null);
            this.getGoalLoadingContainers()[i].setServiceAGV(this.myself);
        }
        this.goalLoadingContainers = null;
//System.out.println(this.toString() + "obtain目标containers");
    }

    /**
     * private AGV出于水平运输行驶阶段;隐藏AGV位置;
     */
    public void hideAGVPosition() {
        this.presentPosition.setLocation(-2000, -20000);
    }

    /**
     * //////////////////////////////////////////////////////////////这部分要改哈！
     * 分配卸船任务的集装箱在堆场中的放箱位置(先具体到Block);
     *
     * @return true:分配成功,对应Block.Array添加任务; false:分配失败，不做任何改变;
     */
    private boolean assignUnloadingBlock() {
        int section_i = -1;
        //优先找:transferPoint3没有被占用 && 没有被预定的 && 装箱量满足要求的 && YC没有任务
        double contNum = 10000;
        for (int i = 0; i < this.goalYard.getBlock().length; i++) {
            if ((this.goalYard.getBlock()[i].getContainersTEU() < InputParameters.getTotalBayNum()
                    * InputParameters.getPerYCRowNum() * InputParameters.getYCContainerHeightNum())
                    && this.goalYard.getBlock()[i].getWaterSideArea().getCar3() == null
                    && this.goalYard.getBlock()[i].getWaterSideArea().isTransPoint3IsBooked() == false
                    && this.goalYard.getBlock()[i].assignmentNumNow() == 0
                    && this.goalYard.getBlock()[i].getContainersTEU()<contNum) {
                section_i = i;
                contNum = this.goalYard.getBlock()[i].getContainersTEU();
            }
        }
        //找2空闲 && 箱量最小的一个Block;
        if (section_i == -1) {
            double containerNum = 10000;
            for (int i = 0; i < this.goalYard.getBlock().length; i++) {
                if ((this.goalYard.getBlock()[i].getContainersTEU() < InputParameters.getTotalBayNum()
                        * InputParameters.getPerYCRowNum() * InputParameters.getYCContainerHeightNum())
                        && this.goalYard.getBlock()[i].getWaterSideArea().getCar2() == null
                        && this.goalYard.getBlock()[i].getWaterSideArea().isTransPoint2IsBooked() == false
                        && this.goalYard.getBlock()[i].getContainersTEU()<containerNum) {
                    section_i = i;
                    containerNum = this.goalYard.getBlock()[i].getContainersTEU();
                }
            }
        }
        //3,2都不行;找1空闲 && 箱量最小的一个Block
        if (section_i == -1) {
            double containerNum = 10000;
            for (int i = 0; i < this.goalYard.getBlock().length; i++) {
                if ((this.goalYard.getBlock()[i].getContainersTEU() < InputParameters.getTotalBayNum()
                        * InputParameters.getPerYCRowNum() * InputParameters.getYCContainerHeightNum())
                        && this.goalYard.getBlock()[i].getWaterSideArea().getCar1() == null
                        && this.goalYard.getBlock()[i].getWaterSideArea().isTransPoint1IsBooked() == false 
                        &&  this.goalYard.getBlock()[i].getContainersTEU()<containerNum) {
                    section_i = i;
                    containerNum = this.goalYard.getBlock()[i].getContainersTEU();
                }
            }
        }
        if (section_i != -1) {
            this.goalBlock = this.goalYard.getBlock()[section_i];
            return true;
        } else {
            return false;
        }
    }
    private synchronized void addUnloadingTaskOfRelativeBlock(AGV agv, Point2D point) {
        //true:分配成功,对应YardSection.Array添加任务;
        if(this.containersOnAGV == null){
            System.out.println("!!!Error:AGV.addUnloadingTaskOfRelativeYardSection()--NULL");
            throw new UnsupportedOperationException("!!!Error:AGV.addUnloadingTaskOfRelativeYardSection()");
        }
        if (this.getContainerNum() == 1) {
            //40ft or 20ft;
            this.goalBlock.obtainOneTask(this.containersOnAGV,
                    this.getAGVstate(), null, agv, point);
        } else if (this.getContainerNum() == 2) {
            //2个20ft箱;
            this.goalBlock.obtainOneTask(this.containersOnAGV,
                    this.getAGVstate(), null, agv, point);
            //YC将两个箱一起取;
        } else {
            System.out.println("!!!Error:AGV.addUnloadingTaskOfRelativeYardSection(AGV agv,Point2D point");
            throw new UnsupportedOperationException("!!!Error:AGV.addUnloadingTaskOfRelativeYardSection(AGV agv,Point2D point");
        }
    }

    /**
     * 获得AGV车道，或计划行驶的车道;
     *
     * @return
     */
    public int getVehicleLine() {
        if (this.currentCarLine != -1) {
            return this.currentCarLine;
        } else {
            /**
             * 默认1车道; 如果要做分车道的动画，此处必须做修改
             */
            this.currentCarLine = 1;
            return 1;
        }
    }

    /**
     * 添加goalContainers[](默认在箱区中;)相关YardSection的任务;
     * @return void
     */
    private synchronized void addLoadingTaskOfRelativeBlock(AGV agv, Point2D point) {
        switch (this.getGoalLoadingContainers().length) {
            case 1:
                //一个箱;
                Block thisYBlock = this.getGoalLoadingContainers()[0].getPresentSlot().getBlock();
                thisYBlock.obtainOneTask(this.getGoalLoadingContainers(), StaticName.LOADING,
                        this.getGoalLoadingContainers()[0].getPresentSlot(), agv, point);
                break;
            case 2:
                //两个20ft箱;分配时必须保证在同一个section;
                Block thisYBlock1 = this.getGoalLoadingContainers()[0].getPresentSlot().getBlock();
                Container[] container = new Container[1];
                container[0] = this.getGoalLoadingContainers()[0];
                thisYBlock1.obtainOneTask(container, StaticName.LOADING,
                        this.getGoalLoadingContainers()[0].getPresentSlot(), agv, point);
                container[0] = this.getGoalLoadingContainers()[1];
                thisYBlock1.obtainOneTask(container, StaticName.LOADING,
                        this.getGoalLoadingContainers()[1].getPresentSlot(), agv, point);
                break;
            default:
                System.out.println("!!!Error:AGV.addTaskOfRelativeYardSection().");
                throw new UnsupportedOperationException("Error:AGV.addTaskOfRelativeYardSection().");
        }
    }

    /**
     * @return the goalLoadingContainers
     */
    public Container[] getGoalLoadingContainers() {
        return goalLoadingContainers;
    }

    /**
     * @return the length
     */
    public double getLength() {
        return length;
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    public void holdMoveToTransferPoint(Point2D startP,Point2D goalP) throws SimRuntimeException, RemoteException {
        Point2D startPoint = new Point2D.Double(0,0);
        startPoint.setLocation(startP);
        Point2D goalPoint = new Point2D.Double(0,0);
        goalPoint.setLocation(goalP);
        //System.out.print("startP:" + startPoint+"-endP:"+goalPoint);
        double pathLength = port.getRoadNetWork().minPathLength(startPoint, goalPoint, StaticName.AGVCAR);
        this.twPath += pathLength;
        //System.out.print("MoveToTransferPoint-pathLength " + pathLength);
        double totalMinute = (pathLength/(this.getSpeed_now()*60));
        double minuteAfter = totalMinute + simulationRealTime;
        double nowSimulationSpeed = InputParameters.getSimulationSpeed();
        double totalTime = totalMinute/nowSimulationSpeed;
        //System.out.println(" MoveToTransferPoint-minute " + totalMinute);
        int num = (int)(totalTime/InputParameters.animationTimeUnit);
        if (InputParameters.isNeedAnimation() == false) {
            num = 0;
        }
        //int i = 0; or = 1????
        for (int i = 0; i < num; i++) {
            if (num <= 1) {
                num = 0;
                break;
            }
            Point2D nowPoint = port.getRoadNetWork().calculateNowPoint(startPoint, goalPoint, this.myself, 
                    pathLength,nowSimulationSpeed*(this.getSpeed_now()*60)*i*InputParameters.animationTimeUnit);
            this.setAGVPostion(nowPoint);
            //System.out.println(this.toString()+"i:"+i+"nowPoint:"+nowPoint);
            this.hold(InputParameters.animationTimeUnit);
            if(InputParameters.getSimulationSpeed() != nowSimulationSpeed){
                //说明此刻有改变仿真速度;
                totalTime = ((totalMinute-(i+1)*InputParameters.animationTimeUnit*nowSimulationSpeed)/getSimulationSpeed())+
                   (i+1)*InputParameters.animationTimeUnit;
                num = (int)((totalTime/InputParameters.animationTimeUnit)-(i+1)*(getSimulationSpeed()/nowSimulationSpeed));
                i = (int) (nowSimulationSpeed*(this.getSpeed_now() * 60)*i*InputParameters.animationTimeUnit / getSimulationSpeed());
                nowSimulationSpeed = getSimulationSpeed();
                this.setAGVPostion(port.getRoadNetWork().calculateNowPoint(startPoint, goalPoint, this.myself,
                    pathLength, nowSimulationSpeed*(this.getSpeed_now() * 60)*i*InputParameters.animationTimeUnit));
            }
            if(simulationRealTime > minuteAfter){
                break;
            }
        }
        if (totalTime > ((num) * InputParameters.animationTimeUnit)) {
            this.hold(format(totalTime - ((num) * InputParameters.animationTimeUnit)));
        }
        this.setAGVPostion(goalPoint);
    }

    private void holdMoveToStockingArea(Point2D startPoint) throws SimRuntimeException, RemoteException {
        double minute;
        if (this.getAGVstate().equals(StaticName.UNLOADING) == true) {
            //卸船任务;有箱状态;从前沿到StockingArea放箱;
            //random Yard箱区;
            //System.out.println("-----------------------------------------------------------");
            this.goalYard = port.getYards()[(int) getRandom(0, port.getYards().length - 1)];/////Yard随机
            //System.out.println(this.toString() + "Unloading Contaienrs箱区指定成功" + this.goalYard.getAreaNum());
        } else if (this.getAGVstate().equals(StaticName.FREE) == true) {
            if (this.serviceQC.getServiceShip().shipState.equals(StaticName.LOADING) == true) {
                //装箱任务;空箱,已分配目标相状态;
                minute = 3;
                //random Yard箱区;
                //System.out.println("-----------------------------------------------------------");
                this.goalYard = port.getYards()[(int) getRandom(0, port.getYards().length - 1)];/////Yard随机;
                //System.out.println(this.toString() + "装箱任务-箱区指定成功" + this.goalYard.getAreaNum()); 
            }else {
                System.out.println("!!!!!!Error:AGV.holdMoveToStockingArea()!!!!!");
                throw new UnsupportedOperationException("!!!!!!Error:AGV.holdMoveToStockingArea()!!!!!");
            }
        } else {
            System.out.println("!!!!!!Error:AGV.holdMoveToStockingArea():AGVstate!!!!!"+this.AGVstate);
            throw new UnsupportedOperationException("!!!!!Error:AGV.holdMoveToStockingArea()!!!!!");
        }
        Point2D endPoint = new Point2D.Double(0, 0);
        endPoint.setLocation(this.goalYard.getEntranceRoadOfAGV(this.myself));
        double pathLength = port.getRoadNetWork().minPathLength(startPoint, endPoint, StaticName.AGVCAR);
        this.twPath += pathLength;
        //System.out.print("-------MoveToStockingArea-pathLength " + pathLength);
        minute = pathLength / (this.getSpeed_now() * 60);
        double minuteAfter = minute + simulationRealTime;
        //System.out.println(this.toString() + "Unloading Contaienrs MoveToStockingAreaTime()成功" + minute);
        double nowSimulationSpeed = InputParameters.getSimulationSpeed();
        double totalTime = minute / nowSimulationSpeed;
        int num = (int) (totalTime / InputParameters.animationTimeUnit);
        if (InputParameters.isNeedAnimation() == false) {
            num = 0;
        }
        //int i = 0; or = 1????
        for (int i = 0; i < num; i++) {
            if (num <= 1) {
                num = 0;
                break;
            }
            Point2D nowPoint = new Point2D.Double(0,0);
            nowPoint.setLocation(port.getRoadNetWork().calculateNowPoint(startPoint, endPoint, this.myself,
                    pathLength, nowSimulationSpeed * (this.getSpeed_now() * 60) * i * InputParameters.animationTimeUnit));
            this.setAGVPostion(nowPoint);
            //System.out.println(this.toString() + "i:" + i + "nowPoint:" + nowPoint);
            this.hold(InputParameters.animationTimeUnit);
            if(InputParameters.getSimulationSpeed() != nowSimulationSpeed){
                //说明此刻有改变仿真速度;
                totalTime = ((minute-(i+1)*InputParameters.animationTimeUnit*nowSimulationSpeed)/getSimulationSpeed())+
                   (i+1)*InputParameters.animationTimeUnit;
                num = (int)((totalTime/InputParameters.animationTimeUnit)-(i+1)*(getSimulationSpeed()/nowSimulationSpeed));
                i = (int) (nowSimulationSpeed*(this.getSpeed_now() * 60)*i*InputParameters.animationTimeUnit / getSimulationSpeed());
                nowSimulationSpeed = getSimulationSpeed();
                this.setAGVPostion(port.getRoadNetWork().calculateNowPoint(startPoint, endPoint, this.myself,
                    pathLength, nowSimulationSpeed*(this.getSpeed_now() * 60)*i*InputParameters.animationTimeUnit));
            }
            if(simulationRealTime>minuteAfter){
                break;
            }
        }
        if (totalTime > ((num) * InputParameters.animationTimeUnit)) {
            this.hold(format(totalTime - ((num) * InputParameters.animationTimeUnit)));
        }
        this.setAGVPostion(endPoint);
    }
    /**
     * 已经判断了UnderQC下面有位子;
     * @param startPoint
     * @throws SimRuntimeException
     * @throws RemoteException 
     */
    private void holdMoveToUnderQC(Point2D startPoint) throws SimRuntimeException, RemoteException {
        //////AGV移至前沿服务岸桥下的装卸点需要的时间(从缓冲区移动) 注意：不要在此处改变AGV位置信息
        //////这一部分不要直接用路网的minPathLength!!!()
        if(port.getAGVBufferArea().isInThisRectangle(startPoint)){
            //AGV现在在AGVBuffer里面;
            //先移动至RoadUnderQC RoadSection;
            Point2D endPoint = new Point2D.Double(0,0);
            endPoint.setLocation(port.getRoadNetWork().getagvUnderQCRoad().findNearestPoint(startPoint));
            double pathOnBuffer = endPoint.distance(startPoint);
            this.twPath += pathOnBuffer;
            if(endPoint.getY() != startPoint.getY()){
                System.out.println("!!!!!!Error:AGV.holdMoveToUnderQC()-Buffer里面 endPoint:"+endPoint+"startP:"+startPoint);
                throw new UnsupportedOperationException("!!!!!Error:AGV.holdMoveToUnderQC()!!!!!");
            }
            double minuteOnBuffer = pathOnBuffer/ (this.getSpeed_now() * 60);
            double minuteAfter = simulationRealTime + minuteOnBuffer;
            double nowSimulationSpeed = getSimulationSpeed();
            double timeOnBuffer = minuteOnBuffer / nowSimulationSpeed;
            int num = (int) (timeOnBuffer / InputParameters.animationTimeUnit);
            if (InputParameters.isNeedAnimation() == false) {
                num = 0;
            }
            //int i = 0; or = 1????
            for (int i = 0; i < num; i++) {
                if (num <= 1) {
                    num = 0;
                    break;
                }
                Point2D nowPoint = this.port.getAGVBufferArea().calculateNowPoint(startPoint, endPoint,
                        this, nowSimulationSpeed * (this.getSpeed_now() * 60) * i * InputParameters.animationTimeUnit);
                this.setAGVPostion(nowPoint);
                //System.out.println(this.toString() + "i:" + i + "nowPoint:" + nowPoint);
                this.hold(InputParameters.animationTimeUnit);
                if (InputParameters.getSimulationSpeed() != nowSimulationSpeed) {
                    //说明此刻有改变仿真速度;
                    timeOnBuffer = ((minuteOnBuffer - (i + 1) * InputParameters.animationTimeUnit * nowSimulationSpeed) / getSimulationSpeed())
                            + (i + 1) * InputParameters.animationTimeUnit;
                    num = (int) ((timeOnBuffer / InputParameters.animationTimeUnit) - (i + 1) * (getSimulationSpeed() / nowSimulationSpeed));
                    i = (int) (nowSimulationSpeed * (this.getSpeed_now() * 60) * i * InputParameters.animationTimeUnit / getSimulationSpeed());
                    nowSimulationSpeed = getSimulationSpeed();
                    this.setAGVPostion(this.port.getAGVBufferArea().calculateNowPoint(startPoint, endPoint,
                        this, nowSimulationSpeed * (this.getSpeed_now() * 60) * i * InputParameters.animationTimeUnit));
                }
                if (simulationRealTime > minuteAfter) {
                    break;
                }
            }
            if (timeOnBuffer > ((num) * InputParameters.animationTimeUnit)) {
                this.hold(format(timeOnBuffer - ((num) * InputParameters.animationTimeUnit)));
            }
            this.setAGVPostion(endPoint);
            //RoadSection内：移动至serviceQCWorkingLocation;
            startPoint.setLocation(endPoint);
            endPoint.setLocation(new Point2D.Double(this.port.getAGVUnderQCAreaPositionX(getLaneNumUnderQC()), 
                    this.serviceQC.getServiceLocation().getY()));
            double pathUnderQC = port.getRoadNetWork().getagvUnderQCRoad().PathLengthOnThisRoad(startPoint, endPoint);
            double minuteUnderQC = pathUnderQC/(this.getSpeed_now()*60);
            this.twPath += pathUnderQC;
            minuteAfter = simulationRealTime + minuteUnderQC;
            nowSimulationSpeed = getSimulationSpeed();
            double timeUnderQC = minuteUnderQC/nowSimulationSpeed;
            num = (int) (timeUnderQC / InputParameters.animationTimeUnit);
            if (InputParameters.isNeedAnimation() == false) {
                num = 0;
            }
            //int i = 0; or = 1????
            for (int i = 0; i < num; i++) {
                if (num <= 1) {
                    num = 0;
                    break;
                }
                Point2D nowPoint = port.getRoadNetWork().getagvUnderQCRoad().calculateNowPoint(startPoint, endPoint, 
                        this.myself,nowSimulationSpeed * (this.getSpeed_now() * 60) * i * InputParameters.animationTimeUnit);
                this.setAGVPostion(nowPoint);
                //System.out.println(this.toString() + "i:" + i + "nowPoint:" + nowPoint);
                this.hold(InputParameters.animationTimeUnit);
                if (InputParameters.getSimulationSpeed() != nowSimulationSpeed) {
                    //说明此刻有改变仿真速度;
                    timeUnderQC = ((minuteUnderQC - (i + 1) * InputParameters.animationTimeUnit * nowSimulationSpeed) / getSimulationSpeed())
                            + (i + 1) * InputParameters.animationTimeUnit;
                    num = (int) ((timeUnderQC / InputParameters.animationTimeUnit) - (i + 1) * (getSimulationSpeed() / nowSimulationSpeed));
                    i = (int) (nowSimulationSpeed * (this.getSpeed_now() * 60) * i * InputParameters.animationTimeUnit / getSimulationSpeed());
                    nowSimulationSpeed = getSimulationSpeed();
                    this.setAGVPostion(port.getRoadNetWork().getagvUnderQCRoad().calculateNowPoint(startPoint, endPoint, 
                        this.myself,nowSimulationSpeed * (this.getSpeed_now() * 60) * i * InputParameters.animationTimeUnit));
                }
                if(simulationRealTime > minuteAfter){
                    break;
                }
            }
            if (timeUnderQC > ((num) * InputParameters.animationTimeUnit)) {
                this.hold(format(timeUnderQC - ((num) * InputParameters.animationTimeUnit)));
            }
            this.setAGVPostion(endPoint);
        }else{
            System.out.println("!!!!!!Error:AGV.holdMoveToUnderQC()-StartPoint:"+startPoint);
//避免throw先暂时删掉吧;throw new UnsupportedOperationException("!!!!!Error:AGV.holdMoveToUnderQC()!!!!!");
        }
    }

    /**
     * 修正系数放在速度里面，最方便;
     * @return the speed_now
     */
    public double getSpeed_now() {
        if(this.containersOnAGV == null){
            return this.speed0TEU*this.faiPath;
        }else{
            return this.speed2TEU*this.faiPath;
        }
    }

    /**
     * 
     * @param itag +1:预定一个位子;0:释放一个位子; 
     */
    public void setLaneNumUnderQC(int itag) {
        if(this.laneNumUnderQC != 0 && itag == 0){
            this.serviceQC.setLaneIsBookedOrOccupied(this.laneNumUnderQC,0);
            this.laneNumUnderQC = 0;
        }else if(this.laneNumUnderQC == 0 && itag == 0){
            throw new UnsupportedOperationException("!!!Error!!!setLaneNumUnderQC");  
        }else{
            for (int i = 0; i < this.serviceQC.getLaneIsBookedOrOccupied().length; i++) {
                if (serviceQC.getLaneIsBookedOrOccupied()[i] == 0) {
                    serviceQC.setLaneIsBookedOrOccupied(i + 1, 1);
                    this.laneNumUnderQC = i + 1;
                    return;
                }
            }
            if (this.laneNumUnderQC == 0) {
                System.out.println("!!!!!Error!!!!!AGV.setLaneNumUnderQC()");
                throw new UnsupportedOperationException("!!!Error!!!setLaneNumUnderQC");  
            }
        }
    }

    /**
     * @return the laneNumUnderQC
     */
    public int getLaneNumUnderQC() {
        return laneNumUnderQC;
    }

    private int getContainerTEU() {
        if(this.containersOnAGV == null){
            return 0;
        }else{
            int num = this.containersOnAGV.length;
            double size = this.containersOnAGV[0].getFTsize();
            int teu = (int)(size/20);
            return num*teu;
        }
    }

    private void addOneOutputAGVWorklist() {
        String res = this.twWorkType+"\t"+this.twST+"\t"+this.twET+"\t"+this.twPath+"\t"+
                this.twWaitingQCFreeLane+"\t"+this.getTwWaitingQC()+"\t"+this.twWaitingBlockAvailable+"\t"+
                this.twWaitingYC+"\t"+this.twTEU+"\t"+this.serviceQC.getNumber();
        getStrb().append(res);
        if(getStrb().length()>20){
            OutputParameters.addRowsToFile("WorlList_AGV/AGV"+this.number, getStrb());     
            strb = new StringBuffer();
        }
    }

    /**
     * @return the twWaitingQC
     */
    public double getTwWaitingQC() {
        return twWaitingQC;
    }

    /**
     * @return the strb
     */
    public StringBuffer getStrb() {
        return strb;
    }
}
