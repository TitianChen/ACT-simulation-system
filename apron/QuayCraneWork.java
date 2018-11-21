/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apron;

import CYT_model.Container;
import CYT_model.Port;
import Vehicle.AGV;
import static algorithm.formater.format;
import ship.Ship;
import java.rmi.RemoteException;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.Resource;
import nl.tudelft.simulation.dsol.formalisms.ResourceRequestorInterface;
import nl.tudelft.simulation.dsol.formalisms.process.Process;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import parameter.InputParameters;
import static parameter.InputParameters.animationTimeUnit;
import static parameter.InputParameters.getRandom;
import static parameter.InputParameters.getSimulationSpeed;
import parameter.OutputParameters;
import static parameter.OutputParameters.simulationRealTime;
import parameter.StaticName;
/**
 * @author YutingChen, yuting.chen17@imperial.ac.uk
 * Dalian University of Technology
 */
public class QuayCraneWork extends Process implements ResourceRequestorInterface{

    private static final long serialVersionUID = 1L;
    //final parameters;
    private final int trolleyType;//1：主小车; 2：副小车;   
    private final QuayCrane QC;//岸桥作业所在岸桥;
    //the description of the Ship
    private String description;
    //    
    protected QuayCraneWork myself = null;
    private Port port = null;
    private Ship ship = null; 
    private int number = 0;//作业线编号;
    private String workType;//QCWorkLineType;UNLOADING LOADING
    public double startTime;//该作业线开始时间;
    public double endTime;//该作业线结束时间;
    private double NeedTime;//该作业线作业仿真所需总时间;
    // 
    private final static double WAITINGTIMEUNIT = 0.5; 
    //
    private String twWorkType;
    private double twStartTime = simulationRealTime;
    private double twWaitExchangT = 0;
    private double twWaitingAGVT = 0;
    private int twContainerTEU = 0;
    private double twEndTime;
    private StringBuffer strb;
    
    public QuayCraneWork(final DEVSSimulatorInterface simulator,QuayCrane QC, 
            int trolleyType, int number) 
    {  
        super(simulator);
        this.myself = this;
        this.ship = null;
        this.port = QC.getPort();
        this.QC = QC;//该作业线岸桥;
        this.trolleyType = trolleyType;
        this.workType = null;
        this.number = number;//QC编号;
        if(trolleyType == 1){
            this.description = this.QC.toString() + " 主小车";//QCNo**+主小车
        }else{
            this.description = this.QC.toString() + " 门架小车";
        }
        this.QC.addQuayCraneWork(this.myself);
        this.strb = new StringBuffer();
        
    }
    /**
     * 装卸子系统进程;
     * @throws RemoteException
     * @throws SimRuntimeException 
     */    
    @Override
    public void process() throws RemoteException, SimRuntimeException{          
        if(this.ship != null && this.ship.shipState.equals(StaticName.LEAVING)==true){
            //该船已准备驶离，QCwork解除与Ship的关联;
            this.ship = null;
            this.QC.setNumOnExchangePlatform(0);
        }
        if(this.ship == null){
            this.QC.setNumOnExchangePlatform(0);
        }
        //空闲,等待QC获得Ship任务
        while(this.QC.getServiceShip() == null){
            if (this.getQC().isNeedToMoveForOtherQC() == true && this.getTrolleyType() == 1) {
                this.getQC().setNeedToMoveForOtherQC(false);
                this.holdMoveTime(QC.getLocation().getY(), QC.getServiceLocation().getY());
//System.out.println(this.toString() + " 该岸桥移动完毕！" + simulationRealTime);
            }
            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
        }
        this.setShip(this.QC.getServiceShip());
        //等待serviceShip获得装卸信号;
        while(((this.QC.getServiceShip().shipState.equals(StaticName.LOADING)==false) && 
                (this.QC.getServiceShip().shipState.equals(StaticName.UNLOADING))==false)){
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
            //岸桥移动到装卸点;
            if (this.QC.getServiceShip() != null  && this.QC.getLocation().equals(this.QC.getServiceLocation()) == false) {
                this.holdMoveTime(QC.getLocation().getY(), QC.getServiceLocation().getY());      
//System.out.println(this.toString() + " 岸桥成功移动到装卸位置！" + simulationRealTime);
                if (this.QC.getServiceShip() != null) {
                    this.QC.getServiceShip().setQuayCraneFinishToMoveTime(simulationRealTime);
                }
            }
            if(this.ship.toString().equals(this.QC.getServiceShip().toString()) == false){
                this.ship = this.QC.getServiceShip();
                this.QC.setNumOnExchangePlatform(0);
                this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                this.process();
            }
        }
        if(QC.getServiceShip().shipState.equals(StaticName.UNLOADING) == true && QC.getServiceShip().getShipContainers().getTotal_unload_num() != 0){
            this.workType = StaticName.UNLOADING;
        }else if(QC.getServiceShip().shipState.equals(StaticName.LOADING) == true && QC.getServiceShip().getShipContainers().getTotal_load_num() != 0){
            this.workType = StaticName.LOADING;
        }else{
            //该船舶没有装/卸作业的情况;
            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
            this.process();
        }
        if(trolleyType == 1){
            this.description = this.workType + this.QC.toString() + " 主小车";//Unloading+QCNo** 主小车;
        }else{
            this.description = this.workType + this.QC.toString() + " 门架小车";
        }
        //开始装卸;
        this.startTime = simulationRealTime;
        if(this.workType.equals(StaticName.UNLOADING) == true && 
                QC.getServiceShip().getShipContainers().getPresent_needunload_num()>0){
            //卸船进程;
//System.out.println("-------"+this.getWorkType()+" "+this.toString()+" 开始卸船工作--------:"+simulationRealTime);
            if (this.QC.getLocation().equals(this.QC.getServiceLocation()) == false) {
                this.holdMoveTime(QC.getLocation().getY(), QC.getServiceLocation().getY());
//System.out.println(this.toString() + " 岸桥成功移动到装卸位置！" + simulationRealTime);
                if (this.QC.getServiceShip() != null) {
                    this.QC.getServiceShip().setQuayCraneFinishToMoveTime(simulationRealTime);
                }
            }
            this.process_Unloading();
        }else if(this.workType.equals(StaticName.LOADING) == true && 
                QC.getServiceShip().getShipContainers().getPresent_needload_num()>0){
            //装船进程;
//System.out.println("-------"+this.getWorkType()+" "+this.toString()+" 开始装船工作--------:" + simulationRealTime);
            this.process_loading();
        }
        //完成该Ship的装船或卸船进程;回到主进程中;
    //    this.ship = null;
        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
        this.process();
    }
    /**
     * 卸船进程;
     * @throws SimRuntimeException
     * @throws RemoteException 
     */
    public void process_Unloading() throws SimRuntimeException, RemoteException{
        //对一个车子而言的。
        this.twWorkType = StaticName.UNLOADING;
        this.twStartTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
        this.twWaitExchangT = 0;
        this.twWaitingAGVT = 0;
        this.twContainerTEU = 0;
        if(this.getQC().getQCType() == 2){
                //默认为双小车岸桥;    
                if(this.getTrolleyType() == 1){
                    //-----------------------主小车进程------------------------//
                    //判断:主小车空闲？
                    while(this.getQC().isMainTrolleyFree() == false){
                        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                        System.out.println("!!检查!!"+this.toString()+" waiting "+ "for MainTrolley free@"+simulationRealTime);
                        break;
                    }       
                    this.QC.setMainTrolleyFree(false);//mainTrolley开始作业;
                    if (this.QC.getLocation().equals(this.QC.getServiceLocation()) == false) {
                        this.holdMoveTime(QC.getLocation().getY(), QC.getServiceLocation().getY());
//System.out.println(this.toString() + " 岸桥成功移动到装卸位置！" + simulationRealTime);
                    }
                    //先卸40ft;
                    while(this.QC.getServiceShip().getShipContainers().getPresent_needunload_40ft() > 0){
                        this.twWorkType = StaticName.UNLOADING;
                        this.twStartTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
                        this.twWaitExchangT = 0;
                        this.twWaitingAGVT = 0;
                        this.twContainerTEU = 0;
                        //判断：中转平台有空间?
                        while(this.QC.getNumOnExchangePlatform() + 0.5 >= this.getQC().getMaxOnExchangePlatform()){
                            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());//
                            this.twWaitExchangT += WAITINGTIMEUNIT;
//System.out.println(this.toString()+" waiting for Enough Space on ExchangePlatform@" +simulationRealTime);
                            if(this.QC.getServiceShip().getShipContainers().getPresent_needunload_40ft() == 0){
                                break;
                            }
                        }
                        if (this.QC.getServiceShip().getShipContainers().getPresent_needunload_40ft() == 0) {
                            break;
                        }
                        //事件1：卸箱至中转平台;   
//System.out.println(this.toString()+" 事件1：" + "卸40ft至中转平台@"+simulationRealTime);
                        //活动1：卸箱;
                        if (this.holdShip_To_ExchangeP(40, 1) == true) {
                            this.QC.setNumOnExchangePlatform(+1);//+1:1*40ft or 2*20ft
                            this.QC.getServiceShip().getShipContainers().setPresent_needunload_num(-1);
                            this.QC.getServiceShip().getShipContainers().setPresent_needunload_40ft(-1);
                            this.twContainerTEU = 2;
                            //活动：回到Ship 
                            this.holdExchangeP_To_Ship(0, 0);
                            this.QC.setFinished_unload_40FTassignment(1);
                        } else {
                            this.holdExchangeP_To_Ship(0, 0);
                        }
                        this.twEndTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
                        this.addOneOutputQCWorkList();
                    }
                    //再卸20ft;
                    while(this.QC.getServiceShip().getShipContainers().getPresent_needunload_20ft() > 0){
                        this.twWorkType = StaticName.UNLOADING;
                        this.twStartTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
                        this.twWaitExchangT = 0;
                        this.twWaitingAGVT = 0;
                        this.twContainerTEU = 0;
                        //判断：中转平台有空间?
                        while(this.QC.getNumOnExchangePlatform() == this.getQC().getMaxOnExchangePlatform()){
                            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                            this.twWaitExchangT += WAITINGTIMEUNIT;
//System.out.println(this.toString()+" waiting for Enough Space on ExchangePlatform@" +simulationRealTime);
                            if(this.QC.getServiceShip().getShipContainers().getPresent_needunload_20ft() == 0){
                                break;
                            }
                        }
                        if(this.QC.getServiceShip().getShipContainers().getPresent_needunload_20ft() == 0){
                            break;
                        }else if(this.QC.getNumOnExchangePlatform()+0.5 == this.getQC().getMaxOnExchangePlatform() ||
                            this.QC.getServiceShip().getShipContainers().getPresent_needunload_20ft() == 1){
//System.out.println(this.toString()+" UnloadNum余量：" +this.QC.getServiceShip().getShipContainers().getPresent_needunload_num());
                            //事件：提一个20ft箱;
//System.out.println(this.toString()+" 事件1:" + "卸1*20ft至中转平台@"+simulationRealTime);
                            if (this.holdShip_To_ExchangeP(20, 1) == true) {
//System.out.println(this.toString() + " 事件1:" + "卸1*20ft至中转平台 true@" + simulationRealTime);
                                this.QC.setNumOnExchangePlatform(+0.5);//1*20ft
                                this.QC.getServiceShip().getShipContainers().setPresent_needunload_num(-1);
                                this.QC.getServiceShip().getShipContainers().setPresent_needunload_20ft(-1);
                                this.twContainerTEU = 1;
                                //小车移回ship;
                                this.holdExchangeP_To_Ship(0, 0);
                                //释放资源;
                                this.QC.setFinished_unload_20FTassignment(1);
                            } else {
//System.out.println(this.toString() + " 事件1:" + "卸1*20ft至中转平台 false@" + simulationRealTime);
                                //小车移回ship;
                                this.holdExchangeP_To_Ship(0, 0);
                            }
                            if(this.ship.getShipContainers().haveUnloadingContainersOnShip() == false){
                                break;
                            }
                        }else{
                            //中转平台空间允许提两个20ft箱;且需卸20ft>=2
                            //事件：提2个20ft箱; 
                            this.QC.getServiceShip().getShipContainers().setPresent_needunload_num(-2);
                            this.QC.getServiceShip().getShipContainers().setPresent_needunload_20ft(-2);
//System.out.println(this.toString()+" UnloadNum余量：" +this.QC.getServiceShip().getShipContainers().getPresent_needunload_num());
//System.out.println(this.toString()+" 事件1："+ "卸2*20ft至中转平台@"+simulationRealTime);
                            //活动：
                            if (this.holdShip_To_ExchangeP(20, 2) == true) {
                                //事件：提2个20ft箱; 
                                this.QC.setNumOnExchangePlatform(+1);//2*20ft
                                this.twContainerTEU = 2;
                                //小车移回ship;
                                this.holdExchangeP_To_Ship(0, 0);
                                this.QC.setFinished_unload_20FTassignment(2);
                            } else {
                                //小车移回ship;
                                this.QC.getServiceShip().getShipContainers().setPresent_needunload_num(2);
                                this.QC.getServiceShip().getShipContainers().setPresent_needunload_20ft(2);
                                this.holdExchangeP_To_Ship(0, 0);
                            }
                        }
                        this.twEndTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
                        this.addOneOutputQCWorkList();
                    }
                    //20ft,40ft均装卸完毕;-----该QCWorkLine结束本次任务;
                    this.QC.setMainTrolleyFree(true);//mainTrolley任务结束;
                    this.endTime = simulationRealTime;
                    if(this.startTime != this.endTime){ 
                        System.out.println(this.toString()+"----start at time=" + this.startTime
                                + " and end at time=" + this.endTime + ". ProcessTime = " + (this.endTime-this.startTime));
                    }
                    while(this.allQCForThisShipAreFree() == false){
                        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                    }              
                }else{
                    //---------------------门架小车进程------------------------//
                    //门架小车是否空闲;
                    while(this.getQC().isGantryTrolleyFree() == false){
                        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                        System.out.println("!!检查!!"+this.toString()+" waiting "+ "for GantryTrolley free@"+simulationRealTime);
                        break;
                    }
                    this.QC.setGantryTrolleyFree(false);
                    if (this.QC.getLocation().equals(this.QC.getServiceLocation()) == false) {
                        this.holdMoveTime(QC.getLocation().getY(), QC.getServiceLocation().getY());
//System.out.println(this.toString() + " 岸桥成功移动到装卸位置！" + simulationRealTime);
                    }
                    this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                    while(QC.getServiceShip().getShipContainers().getPresent_needunload_num()>0
                            || this.QC.isMainTrolleyFree() == false || this.QC.getNumOnExchangePlatform() > 0){
                        //mainTrolley任务未完 或 中转平台有箱;
                        this.twWorkType = StaticName.UNLOADING;
                        this.twStartTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
                        this.twWaitExchangT = 0;
                        this.twWaitingAGVT = 0;
                        this.twContainerTEU = 0;
                        //判断：中转平台有箱？？
                        while(this.QC.getNumOnExchangePlatform() == 0){
                            //无箱;    
//System.out.println(this.toString()+" waiting for Container on ExchangePlatform@" +simulationRealTime);
                            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                            this.twWaitExchangT += WAITINGTIMEUNIT;
                            if(this.QC.getServiceShip().getShipContainers().getPresent_needunload_num() == 0 &&
                                    this.QC.isMainTrolleyFree() == true && this.QC.getNumOnExchangePlatform() == 0){
                                this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                                break;
                            }
                            if(this.QC.getServiceShip().getShipContainers().getPresent_needunload_num() == 1 &&
                                    this.twWaitExchangT>20 && this.QC.getNumber() == 
                                    this.QC.getServiceShip().getAssignedQC()[0].getNumber()){
                                //主小车可能出问题了，直接把箱子弄过来吧;
                                if (this.QC.getServiceShip().getShipContainers().SetUnloadingContainersOnShipToQuayCrane(20, 1, this.QC) != null) {
                                    this.QC.setNumOnExchangePlatform(+0.5);
                                    this.QC.getServiceShip().getShipContainers().setPresent_needunload_num(-1);
                                    this.QC.getServiceShip().getShipContainers().setPresent_needunload_20ft(-1);
                                }else{
                                    this.QC.getServiceShip().getShipContainers().setPresent_needunload_num(-1);
                                    this.QC.getServiceShip().getShipContainers().setPresent_needunload_20ft(-1);
                                    break;
                                }
                            }
                        }
                        if(this.QC.getServiceShip().getShipContainers().getPresent_needunload_num() == 0 &&
                                this.QC.isMainTrolleyFree() == true && this.QC.getNumOnExchangePlatform() == 0){
                            break;
                        }
                        //有箱;
                        if(this.QC.getNumOnExchangePlatform() == 0.5){
                            //中转平台上有一个20ft箱;
                            //判断：是否有空闲AGV;
                            while(this.AGVfree() == false){
                                this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                                twWaitingAGVT += WAITINGTIMEUNIT;
//System.out.println(this.toString()+" waiting for free AGV@" +simulationRealTime);
                            }
                            //有空闲AGV;
                            //判断：是否已到达岸桥底下等待;
                            while(this.freeAGV_arrived() == false){
                                this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                                twWaitingAGVT += WAITINGTIMEUNIT;
//System.out.println(this.toString()+" waiting for AGV arrived@" +simulationRealTime);
                            }
                            //已有AGV到达;
                            //事件1：门架小车提箱装至AGV;
//System.out.println(this.toString()+" 事件2：" + "drag One 20ft from exchange to AGV@"+simulationRealTime);
                            //活动1：
                            while(this.QC.getServiceShip().getShipContainers().ObtainUnloadingContainersOnQC(20, 1, this.QC) == null){
                                this.hold(1.0/getSimulationSpeed());
//System.out.println(this.toString()+"等待主小车吊过来");
//                                if(this.QC.getServiceShip().getShipContainers().haveUnloadingContainersOnShip() == false){
//                                    break;
//                                }
                            }
                            //container到AGV上; 更新AGV上信息;
                            this.QC.setNumOnExchangePlatform(-0.5);
                            this.twContainerTEU = 1;
                            this.holdExchange_To_AGV(20,1,getFirstArrivedAGVForUnloadingShip());
//System.out.println("1*20ft container成功到AGV上");
                            //事件2：门架小车移回exchange;
                            //活动2
                            this.holdAGV_To_ExchangeP(null,null);
                        }else if(this.QC.getNumOnExchangePlatform() >= 1){
                            //中转平台上有足够的2*20ft或1*40ft;
                           //判断：是否有空闲AGV;
                            while(this.AGVfree() == false){
//System.out.println(this.toString()+" waiting for free AGV@"+simulationRealTime);
                                this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                                this.twWaitingAGVT += WAITINGTIMEUNIT;
                            }
                            //有空闲AGV;
                            //判断：是否已到达岸桥底下等待;
                            while(this.freeAGV_arrived() == false){
//System.out.println(this.toString()+" waiting for AGV arrived@"+simulationRealTime);
                                this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                                this.twWaitingAGVT += WAITINGTIMEUNIT;
                            }
                            //已有AGV到达;
                            //事件1：门架小车提箱装至AGV;
                            this.QC.setNumOnExchangePlatform(-1);
//System.out.println(this.toString()+" 事件2：drag Two 20ft/One 40ft from exchange to AGV@"+simulationRealTime);
                            //活动1:
                            //container成功到AGV上; 更新AGV上信息;
                            AGV agv = this.getFirstArrivedAGVForUnloadingShip();
                            while(1==1){
                                if(this.QC.getServiceShip().getShipContainers().ObtainUnloadingContainersOnQC(40,1,this.QC)!=null){
                                    this.holdExchange_To_AGV(40, 1, agv);
                                    this.twContainerTEU = 2;
                                    break;
                                }else if(this.QC.getServiceShip().getShipContainers().ObtainUnloadingContainersOnQC(20, 2, this.QC)!=null){
                                    this.holdExchange_To_AGV(20, 2, agv);
                                    this.twContainerTEU = 2;
                                    break;
                                }else{
                                    this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                                }
                            }
                            //事件2：门架小车移回exchange;
//System.out.println(this.toString()+" 事件2："+ "门架小车移回exchange@"+simulationRealTime);
                            //活动2：
                            this.holdAGV_To_ExchangeP(null,null);
//System.out.println(this.toString()+" 门架小车结束该轮@" +simulationRealTime);
                            this.twEndTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
                            this.addOneOutputQCWorkList();
                        }
                        if(this.QC.getServiceShip() != null &&
                                this.QC.getServiceShip().getNumber() != this.ship.getNumber()){
                            this.ship = this.QC.getServiceShip();
                        }
                    }
                    //即卸货完毕;提箱完成;
                    this.QC.setGantryTrolleyFree(true);
                    //mainTrolley没有装卸任务，中转平台没有箱;
                    this.endTime = simulationRealTime;
                    if(this.endTime != this.startTime){ 
                        System.out.println(this.toString()+"----start at time=" + this.startTime
                                + " and end at time=" + this.endTime + ". ProcessTime = " + (this.endTime-this.startTime)); 
                    }
                    while(this.allQCForThisShipAreFree() == false){
                        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                    }
                 }
            }else{
                //单小车岸桥;
            }   
    }
    /**
     * 装船流程
     * @return boolean
     * @throws RemoteException
     * @throws SimRuntimeException 
     */
    public boolean process_loading() throws RemoteException, SimRuntimeException{ 
        if (this.ship == null || (this.ship != null && this.ship.getShipContainers().allLoadingContainersOnShip() == true)) {
            this.hold(WAITINGTIMEUNIT / getSimulationSpeed());
            return true;
        }
        this.startTime = simulationRealTime; 
        if(this.workType.equals(StaticName.LOADING) == true){
            if (this.QC.getServiceShip() != null
                    && this.QC.getServiceShip().getNumber() != this.ship.getNumber()) {
                this.ship = this.QC.getServiceShip();
            }
            if(this.getQC().getQCType() == 2){
                //双小车岸桥;           
                if(this.getTrolleyType() == 1){
                    //-----------------------主小车进程------------------------//
                    //判断:主小车空闲？
                    while(this.getQC().isMainTrolleyFree() == false){
                        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                        System.out.println("!!检查!!"+this.toString()+" waiting for MainTrolley free@"+simulationRealTime);
                        break;
                    }       
                    this.QC.setMainTrolleyFree(false);//mainTrolley开始作业;
                    if (this.QC.getLocation().equals(this.QC.getServiceLocation()) == false) {
                        this.holdMoveTime(QC.getLocation().getY(), QC.getServiceLocation().getY());
//System.out.println(this.toString() + " 岸桥成功移动到装卸位置！" + simulationRealTime);
                    }
                    //循环条件：门架小车出于工作状态 or 中转平台上有箱;
                    while(this.QC.isGantryTrolleyFree() == false || this.QC.getNumOnExchangePlatform() > 0
                            || (this.ship != null && this.ship.getShipContainers().allLoadingContainersOnShip() == false 
                            || this.AGVHaveLoadingShipWork() == true)){
                        this.twWorkType = StaticName.LOADING;
                        this.twStartTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
                        this.twWaitExchangT = 0;
                        this.twWaitingAGVT = 0;
                        this.twContainerTEU = 0;
                        //判断中转平台有箱？
                        while(this.ship != null && this.ship.getShipContainers().ObtainLoadingContainersOnQC(20, 1, this.QC) == null
                                && this.ship.getShipContainers().ObtainLoadingContainersOnQC(40, 1, this.QC) == null){
                            //中转平台没有箱;
                            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                            this.twWaitExchangT += WAITINGTIMEUNIT;
//System.out.println(this.toString()+" 等中转平台有箱");
//System.out.print(this.toString()+"Num:::::"+this.ship.getShipContainers().getPresent_needload_num());
//System.out.print(this.toString()+"ExchangeNum:::::"+this.QC.getNumOnExchangePlatform());
                            if(this.ship == null || (this.ship != null && this.ship.getShipContainers().allLoadingContainersOnShip() == true)){
                                //门架小车已停止工作，但中转平台上仍然没有箱时break循环;
//System.out.println(this.toString()+" break@"+simulationRealTime);
                                this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                                break;
                            }
//                            if(this.twWaitExchangT>150 && this.ship.getShipContainers().getPresent_needload_num()<5 &&
//                                    this.AGVfreeInBuffer()==true){
//                                this.ship.getShipContainers().setAllLoadingOnShip();
//                                this.ship.getShipContainers().clearNeedLoadNum();
//                                break;
//                            }
                        }
                        if((this.ship != null && this.ship.getShipContainers().allLoadingContainersOnShip() == true) ||
                                this.ship == null){
                            //已装完箱,门架小车已停止工作，但中转平台上仍然没有箱时break循环;
//System.out.println(this.toString()+" break@"+simulationRealTime);
                            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                            break;
                        }
//this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                        if (this.ship.getShipContainers().ObtainLoadingContainersOnQC(20, 1, this.QC) != null
                                || this.ship.getShipContainers().ObtainLoadingContainersOnQC(40, 1, this.QC) != null) {
//this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                            //有箱;
                            //事件1：提箱装船;
//System.out.println(this.toString() + " 事件1：" + "主小车提箱 from 中转平台 to ship@" + simulationRealTime);
                            //活动1：装船;
                            double space = 0;
                            if (this.ship != null && this.ship.getShipContainers().ObtainLoadingContainersOnQC(40, 1, this.QC) != null) {
                                space = 1;
                                this.holdExchangeP_To_Ship(40, 1);
                                this.QC.setNumOnExchangePlatform(-space);//1单位：40ft or 2*20ft
                                this.twContainerTEU = 2;
//System.out.println(this.toString() + "1*40ft container成功到船上!!!");
                            } else if (this.ship != null && this.ship.getShipContainers().ObtainLoadingContainersOnQC(20, 2, this.QC) != null) {
                                space = 1;
                                this.holdExchangeP_To_Ship(20, 2);
                                this.QC.setNumOnExchangePlatform(-space);//1单位：40ft or 2*20ft
                                this.twContainerTEU = 2;
//System.out.println(this.toString() + "2*20ft container成功到船!!!");
                            } else if (this.ship != null && this.ship.getShipContainers().ObtainLoadingContainersOnQC(20, 1, this.QC) != null) {
                                space = 0.5;
                                this.holdExchangeP_To_Ship(20, 1);
                                this.QC.setNumOnExchangePlatform(-space);//1单位：40ft or 2*20ft
                                this.twContainerTEU = 1;
//System.out.println(this.toString() + "1*20ft container成功到船上!!!");
                            } else if (this.ship == null) {
                                this.holdExchangeP_To_Ship(0, 0);
                                break;
                            } else {
//System.out.println(this.toString() + "!!!!!注意: QCWork.Loading.主小车提箱.没有可以提的箱了!!!!");
                                //throw new UnsupportedOperationException("!!!!!Error: QCWork.Loading.主小车提箱.没有可以提的箱了!!!!");
                            }
                            //事件2：返回;
                            //活动2：
                            this.holdShip_To_ExchangeP(0, 0);
                            //主小车该轮装船任务结束;
                            this.twEndTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
                            this.addOneOutputQCWorkList();
                        }
                    }
                    //该QC已无装船任务;
                    //-----该QCWorkLine结束本次任务;
                    this.QC.setMainTrolleyFree(true);//mainTrolley任务结束;
                    if(super.simulator.getSimulatorTime() != this.startTime){
                        this.endTime = simulationRealTime;
                        System.out.println(this.toString()+"----start at time=" + this.startTime
                                + " and end at time=" + this.endTime + ". ProcessTime = " + (this.endTime-this.startTime));
                    }
//                    while(this.allQCForThisShipAreFree() == false){
//                        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
//                    }
                    return true;
                }else{
                    //---------------------门架小车进程------------------------//
                    //门架小车是否空闲;
                    while(this.getQC().isGantryTrolleyFree() == false){
                        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                        System.out.println("!!检查!!"+this.toString()+" is waiting "+ "for GantryTrolley free@"+simulationRealTime);
                        break;
                    }
                    this.QC.setGantryTrolleyFree(false);//门架小车开始工作;
                    if(this.QC.isGantryTrolleyFree() == false && this.QC.isMainTrolleyFree() == false){
                        //说明QC已经到达装卸点;改变QC当前位置;
                        this.QC.setLocation(this.QC.getServiceLocation());
                    }
                    while(this.ship != null && this.ship.getShipContainers().getPresent_needload_num()>0){
                        //有剩余的未分配岸桥的装船任务;
                        this.twWorkType = StaticName.LOADING;
                        this.twStartTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
                        this.twWaitExchangT = 0;
                        this.twWaitingAGVT = 0;
                        this.twContainerTEU = 0;
                        //判断：该作业线上有 AGV getting ContainerWork
                        while(this.AGVHaveLoadingShipWork() == false){
                            //该作业线上所有AGV均没有获得装船集装箱;
                            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                            this.twWaitingAGVT += WAITINGTIMEUNIT;
                            if(this.ship == null || (this.ship!= null && this.ship.getShipContainers().allLoadingContainersOnShip() == true)){
                                break;
                            }
                        }
                        if(this.ship == null || (this.ship!= null && this.ship.getShipContainers().allLoadingContainersOnShip() == true)){
                            /////该成AGV 全在停放区,堆场作业列表里没有作业了;
                            //装船任务已分配完毕;
//System.out.println(this.toString()+"装船任务已经分配完毕  break");
                            break;
                        }
                        //存在获得装船任务的AGV;
                        //判断：装船AGV已到达？？
                        while(this.loadingShipAGV_Arrived() == false){
                            //未到达;
                            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
//System.out.println(this.toString()+"等AGV到达");
                            this.twWaitingAGVT  += WAITINGTIMEUNIT;
                            if(this.ship.getShipContainers().getPresent_needload_num() == 0){
                                break;
                            }
                        }
                        if(this.ship == null || (this.ship!= null && this.ship.getShipContainers().allLoadingContainersOnShip() == true)){
                            //装船任务已分配完毕;
//System.out.println(this.toString()+"装船任务已经分配完毕  break");
                            break;
                        }
                        //AGV已到达;
                        //获得AGV上container信息;
                        AGV agv = this.getFirstArrivedAGVForLoadingShip();
                        double ft = agv.getContainerType(); 
                        double containerNum = agv.getContainerNum();
                        //减少还未分配的任务量;           
                        double spaceNeeded = containerNum*(ft/40);//中转平台上：1*40ft认为是1单位的Space;
                        this.twContainerTEU = (int)(spaceNeeded*2);
                        //装船任务分配成功;减少需分配量;
                        this.ship.getShipContainers().setPresent_needload_num(-containerNum);
                        if(ft == 20){
                            this.ship.getShipContainers().setPresent_needload_20ft(-containerNum);
                            
                        }else{
                            this.ship.getShipContainers().setPresent_needload_40ft(-containerNum);
                        }
                        //判断：中转平台位置足够？
                        while(this.QC.getNumOnExchangePlatform()+spaceNeeded > this.QC.getMaxOnExchangePlatform()){
                            //位置不够
                            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                            this.twWaitExchangT += WAITINGTIMEUNIT;
                        }
                        //位置够;
                        //事件1：门架小车装箱;
//System.out.println(this.toString()+" 事件1：门架小车开始提箱@" +simulationRealTime);
                        //活动1;装箱至中转平台
                        //container转移到ship上;AGV.container[]变为空，AGV进程进行驶离事件;
                        
                        Container[] currentContainers = new Container[(int)agv.getContainerNum()];
                        System.arraycopy(agv.getContainersOnAGV(), 0, currentContainers, 0, currentContainers.length);
                        agv.clearContainers();
                        this.holdAGV_To_ExchangeP(currentContainers,agv);
                        this.QC.setNumOnExchangePlatform(+spaceNeeded);
                        //事件2：门架小车返回;
//System.out.println(this.toString()+" 事件2：" + "门架小车准备返回@"+simulationRealTime);
                        //活动2：门架小车回到提箱装船处;
                        this.holdExchange_To_AGV(0,0,null);
                        //该轮装船任务结束;
                        if(ft == 20){
                            this.QC.setFinished_load_20FTassignment(containerNum);
                        }else{
                            this.QC.setFinished_load_40FTassignment(containerNum);
                        }
                        this.twEndTime = Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed();
                        this.addOneOutputQCWorkList();
                    }
                    //ship上所有装船任务已完成;
                    this.QC.setGantryTrolleyFree(true);
                    //即门架小车没有提箱装船任务;
                    this.endTime = simulationRealTime;
                    if(this.endTime != this.startTime){
                        System.out.println(this.toString()+"----start at time=" + this.startTime
                                + " and end at time=" + this.endTime + ". ProcessTime = " + (this.endTime-this.startTime));
                    }
//                    while(this.allQCForThisShipAreFree() == false){
//                        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
//                    } 
                    return true;
                 }
            }else{
                System.out.println("目前未实现单小车岸桥");
                return false;
                //单小车岸桥;
            }
        }
        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
        this.process();
        return false;
    }  
    
    /**
     * @return 所有QC均完成工作;
     */
    public boolean allQCForThisShipAreFree(){
        boolean i = true;
        if(this.ship != null && this.ship.getAssignedQC() == null){
            return true;
        }
        for(QuayCrane QC1:this.ship.getAssignedQC()){
            if(QC1.isGantryTrolleyFree() == false || QC1.isMainTrolleyFree() == false){
                if(this.trolleyType == 1 && QC1.isGantryTrolleyFree() == true &&
                        QC1.getName().equals(this.QC.getName())){
                    ///说明这个QC主副小车都空闲了;
                }else if(this.trolleyType == 2 && QC1.isMainTrolleyFree() ==  true &&
                        QC1.getName().equals(this.QC.getName())){     
                    //说明这个QC主副小车都空闲了;
                }else{
                    i = false;
                }
            }
        }
        return i;
    }

    /**
     * @return the NeedTime
     */
    public double getNeedTime() {
        return NeedTime;
    }
    
    @Override
    public void receiveRequestedResource(final double requestedCapacity,
            final Resource resource)
    {
        this.resume();
    }
    @Override
    public String toString()
    {
        return this.description;
    }

    /**
     * 1：主小车; 2：副小车;
     * @return the trolleyType
     */
    public int getTrolleyType() {
        return trolleyType;
    }

    /**
     * @return the QC
     */
    public QuayCrane getQC() {
        return QC;
    }

    /**
     * @return the number
     */
    public int getNumber() {
        return number;
    }

    /**
     * @return the workType
     */
    public String getWorkType() {
        return workType;
    }

    /**
     * 搜索该QC的水平运输作业线上是否有获得Container的AGV
     * 可以是到达的，也可以是没有到达;
     * @return boolean true:有集装箱任务;false:没有集装箱任务;
     */
    private boolean AGVHaveLoadingShipWork() {
        int AGVTotalNumOfQC = this.QC.getServiceAGV().length;
        for(int i = 0;i<AGVTotalNumOfQC;i++){
            if(this.QC.getServiceAGV()[i].getContainerNum()>0){
                if(this.QC.getServiceAGV()[i].getAGVstate().equals(StaticName.LOADING)==true){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return boolean whether 该QC下已有装船AGV到达;
     * 到达：AGV位置与QC位置相同;
     */
    private boolean loadingShipAGV_Arrived() {
        int AGVTotalNumOfQC = this.QC.getServiceAGV().length;
        for(int i = 0;i<AGVTotalNumOfQC;i++){
            if(this.QC.getServiceAGV()[i].getContainerNum()>0){
                if(this.QC.getServiceAGV()[i].isArrivedUnderQC() == true){
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * 找到等待时间最长的AGV
     * @return AGV first arrived for loading ship at this.QC
     */
    private AGV getFirstArrivedAGVForLoadingShip() {
        int AGVTotalNumOfQC = this.QC.getServiceAGV().length;
        double wtUnderQC = -1;
        int num = -1;
        for(int i = 0;i<AGVTotalNumOfQC;i++){
            if(this.QC.getServiceAGV()[i].getContainerNum()>0){
                if(this.QC.getServiceAGV()[i].isArrivedUnderQC() == true &&
                        this.QC.getServiceAGV()[i].getTwWaitingQC()> wtUnderQC){
                    num = i;
                    wtUnderQC = this.QC.getServiceAGV()[i].getTwWaitingQC();
                }
            }
        }
        if (num != -1) {
            return this.QC.getServiceAGV()[num];
        } else {
            System.out.println("!!! Error: QuayCraneWork.getFirstArrivedAGVForLoadingShip() !!!");
            throw new UnsupportedOperationException("!!! Error: QuayCraneWork.getFirstArrivedAGVForLoadingShip() !!!");
        }
    }
    
    /**
     * 搜索该QC的水平运输作业线上是否有空闲的AGV
     * 空闲AGV：指没有箱子。
     * @return boolean
     */
    private boolean AGVfree() {
        double agvTotalNumPerQC = this.port.getNumOfAGVPerQC();
        for(int i = 0;i<agvTotalNumPerQC;i++){
            if(this.QC.getServiceAGV()[i].getContainerNum() == 0){
                return true;
            }
        }
        return false;
    }
    
    /**
     * @return AGV first arrived for unloading ship at this.QC
     */
    private AGV getFirstArrivedAGVForUnloadingShip() {
        double agvTotalNumPerQC = this.port.getNumOfAGVPerQC();
        double wtUnderQC = -1;
        int num = -1;
        for(int i = 0;i<agvTotalNumPerQC;i++){
            if(this.QC.getServiceAGV()[i].getContainerNum() == 0){
                if(this.QC.getServiceAGV()[i].isArrivedUnderQC() == true &&
                        this.QC.getServiceAGV()[i].getTwWaitingQC()> wtUnderQC){
                    num = i;
                    wtUnderQC = this.QC.getServiceAGV()[i].getTwWaitingQC();
                }
            }
        }
        if (num != -1) {
            return this.QC.getServiceAGV()[num];
        } else {
            System.out.println("getFirstArrivedAGVForUnloadingShip()失败");
            throw new UnsupportedOperationException("!!!Error: in QuayCraneWork.getFirstArrivedAGVForUnloadingShip() !!!");
        }
    }

    /**
     * 是否有空载状态下的AGV到达前沿QC
     * @return true false
     */
    private boolean freeAGV_arrived() {
        double agvTotalNumPerQC = this.port.getNumOfAGVPerQC();
        for(int i = 0;i<agvTotalNumPerQC;i++){
            if(this.QC.getServiceAGV()[i].getContainerNum() == 0){
                if(this.QC.getServiceAGV()[i].isArrivedUnderQC() == true){
//System.out.println(this.toString()+"with No Container"+"ArrivedUnderQC");
                    return true;
                }
            }
        }
        //AGV还没有到达QC下面;
        return false;
    }

    /**
     * @param ship the ship to set
     */
    public void setShip(Ship ship) {
        if(ship == null){
            this.ship = null;
//System.out.println("QuayCraneWork.setShip():NULL");
        }else{
            this.ship = ship;
        }
    }

    /**
     * 主小车进程进行移动;
     * 门架小车进程只做判断;
     * @param startY
     * @param endY 
     */
    private void holdMoveTime(double startY, double endY) throws SimRuntimeException, RemoteException {
        if (this.trolleyType == 2) {
            while (this.QC.getLocation().getY() != endY) {
                this.hold(animationTimeUnit);
                if(this.QC.isMainTrolleyFree() == false){
                    break;
                }
            }
            return;
        } else if(this.trolleyType == 1){
            double pathLength = this.QC.pathLength(startY, endY);
            double minute = this.QC.calculateNeededMoveTime(pathLength);
            double minuteBefore = simulationRealTime;
            double nowSimulationSpeed = getSimulationSpeed();
            double time = minute / nowSimulationSpeed;
            int num = (int) (time / animationTimeUnit);
            if (InputParameters.isNeedAnimation() == false) {
                num = 0;
            }
            //int i = 0; or = 1????
            for (int i = 0; i < num; i++) {
                if (num <= 1) {
                    num = 0;
                    break;
                }
                if (startY < endY) {
                    this.QC.setLocation(this.QC.getLocation().getX(), startY + nowSimulationSpeed
                            * QC.getMoveSpeed_Crane() * 60 * i * animationTimeUnit);
                } else {
                    this.QC.setLocation(this.QC.getLocation().getX(), startY - nowSimulationSpeed
                            * QC.getMoveSpeed_Crane() * 60 * i * animationTimeUnit);
                }
                this.hold(InputParameters.animationTimeUnit);
                if (InputParameters.getSimulationSpeed() != nowSimulationSpeed) {
                    //说明此刻有改变仿真速度;
                    time = ((minute - (i + 1) * animationTimeUnit * nowSimulationSpeed) / getSimulationSpeed()) + (i + 1) * animationTimeUnit;
                    num = (int) ((time / animationTimeUnit) - (i + 1) * (getSimulationSpeed() / nowSimulationSpeed));
                    i = (int) (nowSimulationSpeed * QC.getMoveSpeed_Crane() * 60 * i * animationTimeUnit / getSimulationSpeed());
                    nowSimulationSpeed = getSimulationSpeed();
                    if (startY < endY) {
                        this.QC.setLocation(this.QC.getLocation().getX(), startY + nowSimulationSpeed
                                * QC.getMoveSpeed_Crane() * 60 * i * animationTimeUnit);
                    } else {
                        this.QC.setLocation(this.QC.getLocation().getX(), startY - nowSimulationSpeed
                                * QC.getMoveSpeed_Crane() * 60 * i * animationTimeUnit);
                    }
                    if (simulationRealTime > minute + minuteBefore) {
                        break;
                    }
                }
                if (time > ((num) * animationTimeUnit)) {
                    this.hold(format(time - ((num) * animationTimeUnit)));
                }
            }
            this.QC.setLocation(this.QC.getLocation().getX(), endY);
        }
    }

    /**
     * mainTrolley
     * 从船舶到中转平台上;
     * @param ftSize
     * @param num 
     */
    private boolean holdShip_To_ExchangeP(int ftSize, int containerNum) throws SimRuntimeException, RemoteException {
        if(containerNum != 0){
            this.QC.setMainTrolleyHaveContainer(true);
        }
        //起升;
        this.hold(format((this.QC.getAverageVerticalMove() / this.QC.getMainTrolleyVerticalSpeed()) / getSimulationSpeed()));
        //水平移动;
        double startX = this.QC.getMainTrolleyX();
        double endX = this.QC.getExchangePlatformX();
        double pathLength = Math.abs(startX-endX);
        double minute = pathLength/this.QC.getMainTrolleySpeed();
        double minuteBefore = OutputParameters.simulationRealTime;
        double nowSimulationSpeed = InputParameters.getSimulationSpeed();
        double totalTime = minute/nowSimulationSpeed;
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
            double nowPathLength = nowSimulationSpeed*QC.getMainTrolleySpeed()*i*animationTimeUnit;
            if(startX<endX){
                this.QC.setMainTrolleyX(startX+nowPathLength);
            }else{
                this.QC.setMainTrolleyX(startX-nowPathLength);
            }
            this.hold(InputParameters.animationTimeUnit);
            if(InputParameters.getSimulationSpeed() != nowSimulationSpeed){
                //说明此刻有改变仿真速度;
                totalTime = ((minute-(i+1)*InputParameters.animationTimeUnit*nowSimulationSpeed)/getSimulationSpeed())+
                   (i+1)*InputParameters.animationTimeUnit;
                num = (int)((totalTime/InputParameters.animationTimeUnit)-(i+1)*(getSimulationSpeed()/nowSimulationSpeed));
                i = (int) (nowSimulationSpeed*QC.getMainTrolleySpeed()*i*animationTimeUnit / getSimulationSpeed());
                nowSimulationSpeed = getSimulationSpeed();
                if (startX < endX) {
                    this.QC.setMainTrolleyX(startX + nowSimulationSpeed*QC.getMainTrolleySpeed()*i*animationTimeUnit);
                } else {
                    this.QC.setMainTrolleyX(startX - nowSimulationSpeed*QC.getMainTrolleySpeed()*i*animationTimeUnit);
                }
            }
            if(simulationRealTime>minuteBefore+minute){
                break;
            }
        }
        if (totalTime > ((num) * InputParameters.animationTimeUnit)) {
            this.hold(format(totalTime - ((num) * InputParameters.animationTimeUnit)));
        }
        this.QC.setMainTrolleyX(endX);
        //下降;
        this.hold(format((this.QC.getAverageVerticalMove()/this.QC.getMainTrolleyVerticalSpeed())/getSimulationSpeed()));
        //更新集装箱信息：      
        if (ftSize > 0 && containerNum > 0) {
            this.hold(getRandom(1,3)*animationTimeUnit);
            if(this.QC.getServiceShip().getShipContainers().SetUnloadingContainersOnShipToQuayCrane(ftSize, containerNum, this.QC) == null){
                this.QC.setMainTrolleyHaveContainer(false);  
                return false;
            }
        }
        this.QC.setMainTrolleyHaveContainer(false);
        return true;
    }
    /**
     * MainTrolley 从中转平台到船舶上;
     * @param ftSize
     * @param containerNum
     * @throws SimRuntimeException
     * @throws RemoteException
     */
    private void holdExchangeP_To_Ship(int ftSize, int containerNum) throws SimRuntimeException, RemoteException {
        if (containerNum != 0) {
            this.QC.setMainTrolleyHaveContainer(true);
        }
        //起升;
        this.hold(format((this.QC.getAverageVerticalMove()/this.QC.getMainTrolleyVerticalSpeed())/getSimulationSpeed()));
        //水平移动;
        double startX = this.QC.getMainTrolleyX();
        double endX;
        if(this.QC.getServiceShip() != null){
            endX = this.QC.getLocation().getX()-(0.5*this.QC.getServiceShip().getShipWidth());
        }else{
            endX = this.QC.getLocation().getX()-(0.5*this.QC.getOutReach());
        }
        double pathLength = Math.abs(startX-endX);
        double minute = pathLength/this.QC.getMainTrolleySpeed();
        double minuteBefore = OutputParameters.simulationRealTime;
        double nowSimulationSpeed = InputParameters.getSimulationSpeed();
        double totalTime = minute/nowSimulationSpeed;
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
            double nowPathLength = nowSimulationSpeed*QC.getMainTrolleySpeed()*i*animationTimeUnit;
            if(startX<endX){
                this.QC.setMainTrolleyX(startX+nowPathLength);
            }else{
                this.QC.setMainTrolleyX(startX-nowPathLength);
            }
            this.hold(InputParameters.animationTimeUnit);
            if(InputParameters.getSimulationSpeed() != nowSimulationSpeed){
                //说明此刻有改变仿真速度;
                totalTime = ((minute-(i+1)*InputParameters.animationTimeUnit*nowSimulationSpeed)/getSimulationSpeed())+
                   (i+1)*InputParameters.animationTimeUnit;
                num = (int)((totalTime/InputParameters.animationTimeUnit)-(i+1)*(getSimulationSpeed()/nowSimulationSpeed));
                i = (int) (nowSimulationSpeed*QC.getMainTrolleySpeed()*i*animationTimeUnit / getSimulationSpeed());
                nowSimulationSpeed = getSimulationSpeed();
                if (startX < endX) {
                    this.QC.setMainTrolleyX(startX + nowSimulationSpeed*QC.getMainTrolleySpeed()*i*animationTimeUnit);
                } else {
                    this.QC.setMainTrolleyX(startX - nowSimulationSpeed*QC.getMainTrolleySpeed()*i*animationTimeUnit);
                }
            }
            if(simulationRealTime>minuteBefore+minute){
                break;
            }
        }
        if (totalTime > ((num) * InputParameters.animationTimeUnit)) {
            this.hold(format(totalTime - ((num) * InputParameters.animationTimeUnit)));
        }
        this.QC.setMainTrolleyX(endX);
        //下降;
        this.hold(format((this.QC.getAverageVerticalMove()/this.QC.getMainTrolleyVerticalSpeed())/getSimulationSpeed()));
        double wT = 0;
        //更新集装箱信息：
        if (ftSize > 0 && containerNum > 0) {
            while(this.QC.getServiceShip().getShipContainers().SetLoadingContainersOnQuayCraneToShip(ftSize,containerNum, QC, 
                    QC.getServiceShip()) == false){
                System.out.println("wait !!!!!! QCWork.holdExchangeP_To_Ship");
                this.hold(format(1/getSimulationSpeed()));
                wT += 1;
                if(wT>20){
                    this.QC.setNumOnExchangePlatform(containerNum*(ftSize/40));
                    break;
                }
            }
            this.QC.setMainTrolleyHaveContainer(false);
        }
    }
    /**
     * gantryTrolley从中转平台到AGV上;
     * @param ftSize
     * @param containerNum
     * @param agv
     * @throws SimRuntimeException
     * @throws RemoteException 
     */
    private void holdExchange_To_AGV(int ftSize, int containerNum, AGV agv) throws SimRuntimeException, RemoteException {
        if(containerNum != 0){
            this.QC.setGantryTrolleyHaveContainer(true);
        }
        //起升;
        this.hold(format((this.QC.getAverageVerticalMove()/this.QC.getGantryTrolleyVerticalSpeed())/getSimulationSpeed()));
        //水平移动;
        double startX = this.QC.getGantryTrolleyX();
        double endX;
        if(agv != null){
            endX = agv.getPresentPosition().getX();
        }else{
            endX = port.getRoadNetWork().getagvUnderQCRoad().CentralAxis(1).getX();
        }
        double pathLength = Math.abs(startX-endX);
        double minute = pathLength/this.QC.getGantryTrolleySpeed();
        double minuteBefore = OutputParameters.simulationRealTime;
        double nowSimulationSpeed = InputParameters.getSimulationSpeed();
        double totalTime = minute/nowSimulationSpeed;
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
            double nowPathLength = nowSimulationSpeed*QC.getGantryTrolleySpeed()*i*animationTimeUnit;
            if(startX<endX){
                this.QC.setGantryTrolleyX(startX+nowPathLength);
            }else{
                this.QC.setGantryTrolleyX(startX-nowPathLength);
            }
            this.hold(InputParameters.animationTimeUnit);
            if(InputParameters.getSimulationSpeed() != nowSimulationSpeed){
                //说明此刻有改变仿真速度;
                totalTime = ((minute-(i+1)*InputParameters.animationTimeUnit*nowSimulationSpeed)/getSimulationSpeed())+
                   (i+1)*InputParameters.animationTimeUnit;
                num = (int)((totalTime/InputParameters.animationTimeUnit)-(i+1)*(getSimulationSpeed()/nowSimulationSpeed));
                i = (int) (nowSimulationSpeed*QC.getGantryTrolleySpeed()*i*animationTimeUnit / getSimulationSpeed());
                nowSimulationSpeed = getSimulationSpeed();
                if (startX < endX) {
                    this.QC.setGantryTrolleyX(startX + nowSimulationSpeed*QC.getGantryTrolleySpeed()*i*animationTimeUnit);
                } else {
                    this.QC.setGantryTrolleyX(startX - nowSimulationSpeed*QC.getGantryTrolleySpeed()*i*animationTimeUnit);
                }
            }
            if(simulationRealTime>minuteBefore+minute){
                break;
            }
        }
        if (totalTime > ((num) * InputParameters.animationTimeUnit)) {
            this.hold(format(totalTime - ((num) * InputParameters.animationTimeUnit)));
        }
        this.QC.setGantryTrolleyX(endX);
        //下降;
        this.hold(format((this.QC.getAverageVerticalMove()/this.QC.getGantryTrolleyVerticalSpeed())/getSimulationSpeed()));
        //更新AGV
        if (ftSize > 0 && containerNum > 0) {
            while(ship.getShipContainers().ObtainUnloadingContainersOnQC(ftSize, containerNum, this.QC) == null){
                System.out.println("wait !!!!!! QCWork.holdExchangeP_To_AGV");
                this.hold(1/getSimulationSpeed());
            }
            agv.obtainContainersFromQC(ship.getShipContainers().ObtainUnloadingContainersOnQC(ftSize, containerNum, this.QC));
            this.QC.setGantryTrolleyHaveContainer(false);
        }
    }

    /**
     * GantryTrolley 从AGV到中转平台上;
     * @param containers
     * @param agv
     * @throws SimRuntimeException
     * @throws RemoteException 
     */
    private void holdAGV_To_ExchangeP(Container[] containers, AGV agv) throws SimRuntimeException, RemoteException {
        if (containers != null) {
            this.QC.setGantryTrolleyHaveContainer(true);
        }
        //起升;
        this.hold(format((this.QC.getAverageVerticalMove()/this.QC.getGantryTrolleyVerticalSpeed())/getSimulationSpeed()));
        //水平移动;
        double startX = this.QC.getGantryTrolleyX();
        double endX = this.QC.getExchangePlatformX();
        double pathLength = Math.abs(startX-endX);
        double minute = pathLength/this.QC.getGantryTrolleySpeed();
        double minuteBefore = OutputParameters.simulationRealTime;
        double nowSimulationSpeed = InputParameters.getSimulationSpeed();
        double totalTime = minute/nowSimulationSpeed;
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
            double nowPathLength = nowSimulationSpeed*QC.getGantryTrolleySpeed()*i*animationTimeUnit;
            if(startX<endX){
                this.QC.setGantryTrolleyX(startX+nowPathLength);
            }else{
                this.QC.setGantryTrolleyX(startX-nowPathLength);
            }
            this.hold(InputParameters.animationTimeUnit);
            if(InputParameters.getSimulationSpeed() != nowSimulationSpeed){
                //说明此刻有改变仿真速度;
                totalTime = ((minute-(i+1)*InputParameters.animationTimeUnit*nowSimulationSpeed)/getSimulationSpeed())+
                   (i+1)*InputParameters.animationTimeUnit;
                num = (int)((totalTime/InputParameters.animationTimeUnit)-(i+1)*(getSimulationSpeed()/nowSimulationSpeed));
                i = (int) (nowSimulationSpeed*QC.getGantryTrolleySpeed()*i*animationTimeUnit / getSimulationSpeed());
                nowSimulationSpeed = getSimulationSpeed();
                if (startX < endX) {
                    this.QC.setGantryTrolleyX(startX + nowSimulationSpeed*QC.getGantryTrolleySpeed()*i*animationTimeUnit);
                } else {
                    this.QC.setGantryTrolleyX(startX - nowSimulationSpeed*QC.getGantryTrolleySpeed()*i*animationTimeUnit);
                }
            }
            if(simulationRealTime>minuteBefore+minute){
                break;
            }
        }
        if (totalTime > ((num) * InputParameters.animationTimeUnit)) {
            this.hold(format(totalTime - ((num) * InputParameters.animationTimeUnit)));
        }
        this.QC.setGantryTrolleyX(endX);
        //下降;
        this.hold(format((this.QC.getAverageVerticalMove()/this.QC.getGantryTrolleyVerticalSpeed())/getSimulationSpeed()));
        //更新集装箱信息;
        if (containers != null) {
            this.QC.getServiceShip().getShipContainers().setLoadingContainersOnAGVToQuayCrane(containers,agv);
            this.QC.setGantryTrolleyHaveContainer(false);
        }
    }

    private void addOneOutputQCWorkList() {
        String res = this.twWorkType+"\t"+this.getTwStartTime()+"\t"+this.twWaitExchangT+"\t"+
                this.twWaitingAGVT+"\t"+this.twContainerTEU+"\t"+(this.twEndTime-this.getTwStartTime())+"\t"+
                this.trolleyType;
        if(this.ship != null){
            res += "\t"+this.ship.getNumber();
        }else{
            res += "\t-1";
        }
        getStrb().append(res);
        if(getStrb().length()>20){
            OutputParameters.addRowsToFile("WorkList_QC/"+"QC"+this.QC.getNumber(), getStrb());
            strb = new StringBuffer();
        }
        
    }

    //所有AGV均空闲且在AGVBuffer里
    private boolean AGVfreeInBuffer() {
        //
        for (AGV agv : this.QC.getServiceAGV()) {
            if(port.getAGVBufferArea().isParking(agv) == false ||
                    agv.getContainerNum()!= 0 || agv.getGoalLoadingContainers()!=null){
                return false;
            }
        }
        return true;
    }

    /**
     * @return the twStartTime
     */
    public double getTwStartTime() {
        return twStartTime;
    }

    /**
     * @return the strb
     */
    public StringBuffer getStrb() {
        return strb;
    }
}
