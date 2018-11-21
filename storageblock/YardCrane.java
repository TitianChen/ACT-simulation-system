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
import Vehicle.Truck;
import static algorithm.formater.format;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.Resource;
import nl.tudelft.simulation.dsol.formalisms.ResourceRequestorInterface;
import nl.tudelft.simulation.dsol.formalisms.process.Process;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import parameter.InputParameters;
import static parameter.InputParameters.getSimulationSpeed;
import parameter.OutputParameters;
import static parameter.OutputParameters.simulationRealTime;
import parameter.StaticName;

/**
 *
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 */
public class YardCrane extends Process implements ResourceRequestorInterface{
    
    private static final long serialVersionUID = 1L;
    
    private final Port port;
    private final YardCrane myself;
    private Yard yard;
    private Block yardBlock;
    //尺寸参数;
    private final String YCtype;
    private final int maxLiftingHeightNum;//最大抬升层数;//如堆5过6，堆6过7等;
    private final int maxContainersHeightNum;//最高堆箱层数;
    private final double totalRow;//该section中行数;
    private final double trackGuage;
    private final double width;
    public String discription;
    public final String YCname;
    public final int yardNum;
    public final char blockNum;
    private final String number; //在该段的编号; 如 waterside,landside;
    //
    private final double horizontalSpeed_trolley;//场桥小车的水平移动速度;m/min
    private final double verticalSpeed_trolley;//场桥小车的竖向移动速度;m/min
    private final double horizontalSpeed_crane;//场桥在轨道上的移动速度;m/min
    
    //会改变的参数;
    public int currentBayPosition;//当前所在贝位;
    private int currentRowPosition;//当前小车吊具所在row;
    public int currentHeightNumPosition;//当前小车吊具所在层高数;
    public Point2D currentPosition;//当前位置----指中点所在位置;
    private Container[] currentServiceContainer;//当前服务集装箱;
    private YCTask firstYCTask = null;
    
    //
    private final static double WAITINGTIMEUNIT = 0.5;
    private final double Time_dragContainersFromBlock = 0.5;
    private final double Time_putContainersToBlock = 0.5;
    private final double Time_putContainersToAGV = 1;
    private final double Time_getContainersFromAGV = 0.5;
    private final double Time_getContainersFromAGVCouple = 0.5;
    private final double Time_trolleyReturnToLiftingHeight = 0.5;
    private final double Time_putContainersToTruck = 0.5;
    private StringBuffer strb;
    
    public YardCrane(final DEVSSimulator simulator,final Port port,int yardNum,char blockNum,String number){
        super(simulator);
        this.port = port;
        this.myself = this;
        this.YCtype = InputParameters.getYCtype();
        this.maxLiftingHeightNum = InputParameters.getYCLiftingNum();
        this.maxContainersHeightNum = InputParameters.getYCContainerHeightNum();
        this.totalRow  = InputParameters.getPerYCRowNum();
        this.trackGuage = InputParameters.getRMGtrackGuage();
        this.width = InputParameters.getLengthofContainer(20);//认为岸桥宽度和20ft箱一样,(动画片里都是这样的哦)
        this.yardNum = yardNum;
        this.blockNum = blockNum;
        this.number = number;
        this.YCname = this.YCtype+"-"+this.number+" of Y"+Integer.toString(yardNum)+"("+
                this.blockNum+")";
        this.discription = null;
        this.verticalSpeed_trolley = 60;//////满载时是30;
        this.horizontalSpeed_trolley = 90;
        this.horizontalSpeed_crane = 120;
        if((this.number.equals(StaticName.WATERSIDE)==true && this.yardNum==1)
                || (this.number.equals(StaticName.LANDSIDE)==true && this.yardNum==2)){
            this.currentBayPosition = 2;
            this.currentRowPosition = 2;
        }else if((this.number.equals(StaticName.LANDSIDE)==true && this.yardNum==1)
                ||(this.number.equals(StaticName.WATERSIDE)==true && this.yardNum==2)){
            this.currentBayPosition = InputParameters.getTotalBayNum()-2;
            this.currentRowPosition = 2;
        }else{
            System.out.println("!!Error:YardCrane.construct()!!");
            throw new UnsupportedOperationException("Error:YardCrane.construct()!!");
        }
        
        this.relatePort();
        this.relateYard();
        
        double x = 0.5*(yardBlock.getP1().getX()+yardBlock.getP2().getX());
        double y = this.yardBlock.getP1().getY()+0.5*this.currentBayPosition*InputParameters.getLengthOf20FTContainer()
                +InputParameters.getDistanceOfContainers()*0.5*(this.currentBayPosition-1);
        this.currentPosition = new Point2D.Double(x, y);
        this.currentServiceContainer = null;
        this.firstYCTask = null;
//System.out.println(this.YCname+"构造成功"); 
        
        this.strb = new StringBuffer();
    }   
        
    /**
     * @return the number
     */
    public String getNumber() {
        return number;
    }
    /**
     * YardCrane进程;
     * @throws SimRuntimeException
     * @throws RemoteException 
     */
    @Override
    public void process() throws SimRuntimeException, RemoteException{
//System.out.println("-----------"+this.YCname+"进程开始--------");   
        //判断:YC是否收到任务;
        while(this.getFirstYCTask() == null){
            this.hold(10*WAITINGTIMEUNIT/getSimulationSpeed());
            //System.out.println("          "+this.YCname+"等待获得任务"+super.simulator.getSimulatorTime());
        }
        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
        this.firstYCTask.setStartT(Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed());
        //检查任务是否正确;
        //讲道理是Vehicle到达之后再开始进行任务，故检查Vehicle是否已经到达;
        switch(this.firstYCTask.getWorkType()){
            case StaticName.UNLOADING:
                //卸船;
                if(((AGV)(this.firstYCTask.getCar())).getContainersOnAGV() == null ||
                        ((AGV)(this.firstYCTask.getCar())).getPresentPosition().distance(this.firstYCTask.getStartPosition())!=0){
                    if(this.firstYCTask.getContainer()[0].getState().equals(StaticName.ONAGVCOUPLE)){
                        break;
                    }else if(((AGV)(this.firstYCTask.getCar())).getContainerNum() == 0 || 
                            ((AGV)(this.firstYCTask.getCar())).getContainersOnAGV()[0].equals(this.firstYCTask.getContainer()[0]) == false){
                        OutputParameters.addOneRowToFile("ErrorYCTask", "Error:Unloading---" + this.toString() + " "
                                + this.firstYCTask.toString() + " " + port.getRoadNetWork().
                                        findRoadSectionName(this.firstYCTask.getCar().getPresentPosition(), 1) + ""+
                                this.firstYCTask.getContainer()[0].getState());
                        this.firstYCTask = null;
                        this.process();
                    }
                }
                break;
            case StaticName.LOADING:
                //装船;
                if(((AGV)(this.firstYCTask.getCar())).getPresentPosition().distance(this.firstYCTask.getGoalPosition())!=0){
                    if(((AGV)(this.firstYCTask.getCar())).getContainerNum() != 0 || 
                            ((AGV)(this.firstYCTask.getCar())).getGoalLoadingContainers() == null ||
                            ((AGV)(this.firstYCTask.getCar())).getGoalLoadingContainers()[0].equals(this.firstYCTask.getContainer()[0]) == false){
                        OutputParameters.addOneRowToFile("ErrorYCTask", "Error:Loading---" + this.toString() + " "
                                + this.firstYCTask.toString() + " " + port.getRoadNetWork().
                                        findRoadSectionName(this.firstYCTask.getCar().getPresentPosition(), 1) + ""+
                                this.firstYCTask.getContainer()[0].getState());
                        this.getYardBlock().move1Or2MaybeContainer(this.firstYCTask.getContainer());//在Block中移除问题箱;
                        this.firstYCTask = null;
                        this.process();
                    }
                }
                break;
            case StaticName.LEAVINGPORT:
                //离港
                if(this.firstYCTask.getContainer()[0].getState().equals(StaticName.ONSTOCKING) == false) {
                    OutputParameters.addOneRowToFile("ErrorYCTask", "Error:LeavingPort---" + this.toString() + " "
                            + this.firstYCTask.toString() + "" + this.firstYCTask.getContainer()[0].getState());
                    this.firstYCTask = null;
                    this.process();
                }
                break;
            case StaticName.TOPORT:
                if(this.firstYCTask.getContainer()[0].getState().equals(StaticName.ONTRUCK) == false && 
                        this.firstYCTask.getContainer()[0].getState().equals(StaticName.OUTSIDE) == false) {
                    OutputParameters.addOneRowToFile("ErrorYCTask", "Error:ToPort---" + this.toString() + " "
                            + this.firstYCTask.toString() + "" + this.firstYCTask.getContainer()[0].getState());
                    this.firstYCTask = null;
                    this.process();
                }else if(this.getNumber().equals(StaticName.LANDSIDE) && this.getYardBlock().getWatersideYC().isFree()==false &&
                        this.firstYCTask.getContainer()[0].equals(this.getYardBlock().getWatersideYC().firstYCTask.getContainer()[0])){
                    OutputParameters.addOneRowToFile("ErrorYCTask", "Error:ToPort---" + this.toString() + " "
                            + this.firstYCTask.toString() + "" + this.firstYCTask.getContainer()[0].getState());
                    this.firstYCTask = null;
                    this.process();
                }else if(this.getNumber().equals(StaticName.WATERSIDE) && this.getYardBlock().getLandsideYC().isFree()==false &&
                        this.firstYCTask.getContainer()[0].equals(this.getYardBlock().getLandsideYC().firstYCTask.getContainer()[0])){
                    OutputParameters.addOneRowToFile("ErrorYCTask", "Error:ToPort---" + this.toString() + " "
                            + this.firstYCTask.toString() + "" + this.firstYCTask.getContainer()[0].getState());
                    this.firstYCTask = null;
                    this.process();
                }
                break;
            default:               
        }
//System.out.println("          "+this.YCname+"获得任务"+simulationRealTime);
        //YC收到任务;
        //判断：装船/卸船/出港/进港;
        switch (this.firstYCTask.getWorkType()) {
            case StaticName.LOADING: //System.out.println("-----------"+this.YCname+"LOADING(取箱装船)进程开始--------");   
            {
                try {
                    this.process_Loading();
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(YardCrane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case StaticName.UNLOADING: //System.out.println("-----------"+this.YCname+"UNLOADING(卸船进场)进程开始--------");
            {
                try {
                    this.process_Unloading();
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(YardCrane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case StaticName.LEAVINGPORT: //System.out.println("-----------"+this.YCname+"--------LEAVINGPORT(取箱离港)进程开始--------");
            {
                try {
                    this.process_LeavingPort();
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(YardCrane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            case StaticName.TOPORT: //System.out.println("-----------"+this.YCname+"--------TOPORT(提箱进港)进程开始--------");
            {
                try {
                    this.process_ToPort();
                } catch (InvocationTargetException ex) {
                    Logger.getLogger(YardCrane.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
            default:
                System.out.println("-----------" + this.YCname + "switch出错了--------：" + this.firstYCTask.getWorkType());
        }
        //该次任务完成;
        this.firstYCTask = null;
//System.out.println(this.toString()+"--------------结束该次Work"+simulationRealTime);
        this.process();
    }
    /**
     * 装船提箱出堆场;
     * Bay To WaterSide;
     * 结束时：吊具移至liftingHeight;
     * @throws SimRuntimeException
     * @throws RemoteException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void process_Loading() throws SimRuntimeException, RemoteException,InvocationTargetException{
        //先移动至相应Bay位;
        Point2D startPoint = new Point2D.Double(this.currentPosition.getX(), this.currentPosition.getY());
        Point2D nextPoint = new Point2D.Double(firstYCTask.getStartPosition().getX(),firstYCTask.getStartPosition().getY());
//System.out.println(this.toString()+"MoveToThisBay");
        this.holdMoveToThisPosition(startPoint,nextPoint);
//System.out.println(this.toString()+"--------------成功移至Loading取箱Bay位"+simulationRealTime);
        //移动吊具至指定row;
        int startRowNum = this.currentRowPosition;
        this.holdHorizontalMove_Trolley(startRowNum, firstYCTask.getStartPosition());
//System.out.println(this.toString()+"--------------成功移至Loading取箱Row"+simulationRealTime);
        //移动吊具至指定height;
        this.holdVerticalMove_Trolley(currentHeightNumPosition, yard.findHeightNum(firstYCTask.getContainer(),yardBlock));
//System.out.println(this.toString()+"--------holdVerticalMove_Trolley完成------"+simulationRealTime);
        this.obtainContainersFromBlockToYC();
//System.out.println(this.toString()+"--------------成功移至Loading取箱HeightNum"+simulationRealTime);
        //提箱;
        this.hold(this.Time_dragContainersFromBlock/getSimulationSpeed());
//System.out.println(this.toString()+"--------------成功取箱"+simulationRealTime);
        //吊具收回
        this.holdVerticalMove_Trolley(currentHeightNumPosition,maxLiftingHeightNum);
//System.out.println(this.toString()+"--------------小车成功移至YardCrane移动高度"+simulationRealTime);
        //移动至goalPosition;
        startPoint = new Point2D.Double(this.currentPosition.getX(), this.currentPosition.getY());
        Point2D goalPoint = new Point2D.Double(firstYCTask.getGoalPosition().getX(),firstYCTask.getGoalPosition().getY());
        this.holdMoveToThisPosition(startPoint,goalPoint);
//System.out.println(this.toString()+"--------------YardCrane成功移至Loading GoalPosition Bay位"+simulationRealTime);
        //小车吊具水平移动至指定装卸点;
        this.holdHorizontalMove_Trolley(this.currentRowPosition, firstYCTask.getGoalPosition());
//System.out.println(this.toString()+"--------------trolley小车成功移至WaterSide指定点@"+simulationRealTime);
        //检查：AGV是否已经到达该位置;
        double wT = 0;
        while(this.haveRightLoadingAGVArriving(this.firstYCTask.getGoalPosition()) == null){
            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
            wT += WAITINGTIMEUNIT;
            if(wT>10){
                //如果10min了，说明这个任务可能是出bug了，直接不要了，进入下一个task;
                this.firstYCTask = null;
                return;
            }
            System.out.println("Error:检查AGV Loading Yard Task分配至YCTask的规则");
//throw new UnsupportedOperationException("Error:YardCrane.process_Loading()！！检查AGV Loading Yard Task分配至YCTask的规则！！");
        }
//System.out.println(this.toString()+"--------------AGV已经到达该位置@"+simulationRealTime);
        //AGV已在场桥吊具下
//System.out.println(this.toString()+"--------------开始放箱至AGV &"+simulationRealTime);
        this.hold(this.Time_putContainersToAGV/getSimulationSpeed());//放箱至AGV;
        AGV agv = this.haveRightLoadingAGVArriving(this.firstYCTask.getGoalPosition());
//System.out.println(this.toString()+"--------------开始放箱至"+agv.toString()+"@"+simulationRealTime);
        this.updateContainersFromYCToAGV(this.myself,agv);
        //System.out.println(this.toString()+"--------------成功放箱至AGV@"+simulationRealTime);
        //吊具竖直移回lifting高度;
        this.holdVerticalMove_Trolley(this.currentHeightNumPosition,this.maxLiftingHeightNum);
        //结束该次loadingWork任务,回到主进程获取下一个work任务;
        this.addOneOutputYCWorkList();
        this.firstYCTask = null;
    }
    
    
    ////CYT_model.Container@1145994a----CYT_model.Container@3b1c237a
    ////Error:CYT_model.Container@2a626382----CYT_model.Container@2a626382
    /**
     * 卸船入堆场;
     * WaterSide To Bay;
     * @throws SimRuntimeException
     * @throws RemoteException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void process_Unloading() throws SimRuntimeException, RemoteException,InvocationTargetException{
        //crane移至指定waterSide装卸点;
//System.out.println(this.toString()+"Point2D startPoint");
        Point2D startPoint = new Point2D.Double(this.currentPosition.getX(), this.currentPosition.getY());
        Point2D nextPoint = new Point2D.Double(firstYCTask.getStartPosition().getX(),firstYCTask.getStartPosition().getY());
        this.holdMoveToThisPosition(startPoint,nextPoint);
        //System.out.println(this.toString()+"YardCrane成功移至WaterSide Unloading装卸点"+simulationRealTime);
        //小车吊具水平移动至指定装卸Row;
        this.holdHorizontalMove_Trolley(currentRowPosition, this.firstYCTask.getStartPosition());
        //System.out.println(this.toString()+"trolley小车成功移至WaterSide指定Position"+simulationRealTime);
        //判断
        switch(this.firstYCTask.getContainer()[0].getState()){
            case StaticName.ONAGV:
                //判断：目标箱在AGV上且目标箱不在有AGV伴侣的位置;
                if(this.firstYCTask.getStartPosition().equals(this.yardBlock.getWaterSideArea().getTransferPoint3()) == true){
                    //在有AGV伴侣的位置;
                    while(this.firstYCTask.getContainer()[0].getState().equals(StaticName.ONAGV) == true){
                        //System.out.println(this.toString()+"等待AGV伴侣先从AGV上取箱"+simulationRealTime);
                        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                    }
                }else if(this.firstYCTask.getStartPosition().equals(this.yardBlock.getWaterSideArea().getTransferPoint2()) == true){
                    //在有AGV伴侣的位置;
                    while(this.firstYCTask.getContainer()[0].getState().equals(StaticName.ONAGV) == true){
                        //System.out.println(this.toString()+"等待AGV伴侣先从AGV上取箱"+simulationRealTime);
                        this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
                    }
                }else{
                   //System.out.println(this.toString()+"开始从AGV取箱"+simulationRealTime);
                    AGV agv = this.haveRightUnloadingAGVArriving(this.firstYCTask.getStartPosition());
                    this.hold(this.Time_getContainersFromAGV/getSimulationSpeed());
                    this.updateContainersFromAGVToYC(this.myself,agv);
                    //System.out.println(this.toString()+"成功从"+agv.getName()+simulationRealTime);
                    break;
                }
                //注意：此处特意没有break;
            case StaticName.ONAGVCOUPLE:
                //判断：目标箱在AGVCouple上
                this.hold(this.Time_getContainersFromAGVCouple/getSimulationSpeed());
                this.updateContainersFromAGVCoupleToYC(this.myself);
                //System.out.println(this.toString()+"成功从AGVCouple"+simulationRealTime);
                break;
            default:
                //判断：都不在？？检查
                System.out.println("!!!Error:YardCrane.process_Unloading()!!!");
                throw new UnsupportedOperationException("Error:YardCrane.process_Unloading()"); 
        }
        //取箱成功,吊具竖直移回lifting高度;
        this.hold(this.Time_trolleyReturnToLiftingHeight/getSimulationSpeed());
        this.currentHeightNumPosition = this.maxLiftingHeightNum;
        //crane水平移动至相应Bay位;
        startPoint = new Point2D.Double(this.currentPosition.getX(), this.currentPosition.getY());
        Point2D goalPoint = new Point2D.Double(firstYCTask.getGoalPosition().getX(),firstYCTask.getGoalPosition().getY());
        this.holdMoveToThisPosition(startPoint,goalPoint);
        //System.out.println(this.toString()+"成功移至Unloading放箱Bay位"+simulationRealTime);
        //移动吊具至指定row;
        this.holdHorizontalMove_Trolley(currentRowPosition, this.firstYCTask.getGoalPosition());
        //System.out.print(this.toString()+"成功移至Unloading放箱Row"+simulationRealTime);
        //移动吊具至指定height;
        this.holdVerticalMove_Trolley(currentHeightNumPosition,
                yardBlock.findCurrentMaxHeightNum(currentRowPosition,currentBayPosition)+1);;
//System.out.println(this.toString()+ "移至Unloading取箱HeightNum@"+simulationRealTime);
        //放箱;
        this.hold(this.Time_putContainersToBlock/getSimulationSpeed());
        this.obtainContainersFromYCToBlock(currentHeightNumPosition);
//System.out.println(this.toString()+"成功放箱"+simulationRealTime);
        //吊具收回
        this.holdVerticalMove_Trolley(currentHeightNumPosition,maxLiftingHeightNum);
//System.out.println(this.toString()+"小车成功移至YardCrane移动高度"+simulationRealTime);
        //结束该次loadingWork任务,回到主进程获取下一个work任务;
        this.addOneOutputYCWorkList();
        this.firstYCTask = null;
    }
    /**
     * 提箱出港
     * Bay To LandSide
     * @throws nl.tudelft.simulation.dsol.SimRuntimeException
     * @throws java.rmi.RemoteException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void process_LeavingPort() throws SimRuntimeException, RemoteException, InvocationTargetException{
        //先移动至相应Bay位;
        Point2D startPoint = new Point2D.Double(this.currentPosition.getX(), this.currentPosition.getY());
        Point2D nextPoint = new Point2D.Double(firstYCTask.getStartPosition().getX(),firstYCTask.getStartPosition().getY());
        this.holdMoveToThisPosition(startPoint, nextPoint);
//System.out.println(this.toString()+"移至LEAVINGPORT取箱Bay位"+simulationRealTime);
        //移动吊具至指定row;
        int startRowNum = this.currentRowPosition;
        this.holdHorizontalMove_Trolley(startRowNum, this.firstYCTask.getStartPosition());
//System.out.println(this.toString()+"移至LEAVINGPORT取箱Row @"+simulationRealTime);
//System.out.println(this.toString()+"  "+this.firstYCTask.getContainer()[0].getState()+simulationRealTime);        
        if(this.firstYCTask.getContainer()[0].getPresentSlot() == null || 
                this.firstYCTask.getContainer()[0].getState().equals(StaticName.ONSTOCKING) == false){
            OutputParameters.addOneRowToFile("ErrorYCTask", "Error:LeavingPort---" + this.toString() + " "
                            + this.firstYCTask.toString() + "" + this.firstYCTask.getContainer()[0].getState());
            //this.getYardBlock().move1Or2MaybeContainer(this.firstYCTask.getContainer());
            this.firstYCTask = null;
            return;
        }
        //移动吊具至指定height;
        this.holdVerticalMove_Trolley(currentHeightNumPosition, yard.findHeightNum(firstYCTask.getContainer(),this.yardBlock));
        if(this.firstYCTask.getContainer()[0].getPresentSlot() == null || 
                this.firstYCTask.getContainer()[0].getState().equals(StaticName.ONSTOCKING) == false){
//System.out.println("ErrorYCTask_LeavingPort");
            OutputParameters.addOneRowToFile("ErrorYCTask", "Error:LeavingPort---" + this.toString() + " "
                            + this.firstYCTask.toString() + "" + this.firstYCTask.getContainer()[0].getState());
            this.firstYCTask = null;
            this.hold(WAITINGTIMEUNIT);
            return;
        }
//System.out.println(this.toString()+"---obtainContainersFromBlockToYC"+simulationRealTime);
        this.obtainContainersFromBlockToYC();
//System.out.println(this.toString()+"移至LEAVINGPORT取箱HeightNum"+simulationRealTime);
        //提箱;
        this.hold(this.Time_dragContainersFromBlock/getSimulationSpeed());
//System.out.println(this.toString()+"--------------成功取箱"+simulationRealTime);
        //吊具收回
        this.holdVerticalMove_Trolley(currentHeightNumPosition, maxLiftingHeightNum);
//System.out.println(this.toString()+"小车移至YardCrane移动高度"+simulationRealTime);
        //移动至goalPosition;
        startPoint = new Point2D.Double(this.currentPosition.getX(), this.currentPosition.getY());
        Point2D goalPoint = new Point2D.Double(firstYCTask.getGoalPosition().getX(),firstYCTask.getGoalPosition().getY());
        this.holdMoveToThisPosition(startPoint,goalPoint);
//System.out.println(this.toString()+"移至集卡交换区"+simulationRealTime);
        //小车吊具水平移动至指定装卸点;
        this.holdHorizontalMove_Trolley(currentRowPosition, this.firstYCTask.getGoalPosition());
        //System.out.println(this.toString()+"trolley小车移至LandSide指定点@"+simulationRealTime);
        //检查：truck是否已经到达该位置;
        while(this.haveRightLoadingTruckArriving(this.firstYCTask.getGoalPosition()) == null){
            this.hold(WAITINGTIMEUNIT/getSimulationSpeed());
//System.out.println("Error:检查Truck LEAVINGPORT Yard Task分配至YCTask的规则");
        }
        //AGV已在场桥吊具下
        this.hold(this.Time_putContainersToTruck/getSimulationSpeed());//放箱至Truck;
        Truck truck = this.haveRightLoadingTruckArriving(this.firstYCTask.getGoalPosition());
        this.updateContainersFromYCToTruck(this.myself,truck);
        //System.out.println(this.toString()+"成功放箱至Truck@"+simulationRealTime);
        //吊具竖直移回lifting高度;
        this.hold(this.Time_trolleyReturnToLiftingHeight/getSimulationSpeed());
        this.currentHeightNumPosition = this.maxLiftingHeightNum;
        //结束该次loadingWork任务,回到主进程获取下一个work任务;
        this.addOneOutputYCWorkList();
        this.firstYCTask = null;
    }
    /**
     * 取箱进港
     * LandSide To Bay
     * @throws nl.tudelft.simulation.dsol.SimRuntimeException
     * @throws java.rmi.RemoteException
     * @throws java.lang.reflect.InvocationTargetException
     */
    public void process_ToPort() throws SimRuntimeException, RemoteException,InvocationTargetException{
        //crane移至指定landSide装卸点;
        Point2D startPoint = new Point2D.Double(this.currentPosition.getX(), this.currentPosition.getY());
        Point2D nextPoint = new Point2D.Double(firstYCTask.getStartPosition().getX(),firstYCTask.getStartPosition().getY());
        this.holdMoveToThisPosition(startPoint,nextPoint);
        //System.out.println(this.toString()+"YardCrane成功移至LandSide ToPort装卸点"+simulationRealTime);
        //小车吊具水平移动至指定装卸Row;
        this.holdHorizontalMove_Trolley(currentRowPosition, this.firstYCTask.getStartPosition());
        //System.out.println(this.toString()+"trolley小车成功移至LandSide指定row"+simulationRealTime);
        //判断
        //System.out.println(this.toString()+"!!!判断:YardCrane.process_ToPort()!!!"+this.firstYCTask.getContainer()[0].getState());
        switch(this.firstYCTask.getContainer()[0].getState()){
            case StaticName.ONTRUCK:
                this.updateContainersFromOUTSIDEToYC(this);
                
                /**
                 * /////////////////////////////////////////////////////////////若要做集疏运,则要加truck类;
                 */
                
//                Truck truck = this.haveRightToPortArriving(this.firstYCTask.getStartPosition());
//                this.hold(this.Time_getContainersFromOUTSIDE);
//                this.updateContainersFromOutsideToYC(this.myself,truck);
                //System.out.println(this.toString()+"成功从"+"truck取箱,集港"+simulationRealTime);
                break;
            default:
                //判断：检查
                System.out.println(this.toString()+"!!!Error:YardCrane.process_ToPort()!!!"+this.firstYCTask.getContainer()[0].getState());
                System.out.println("!!!Error:YardCrane.process_ToPort()!!!");
                throw new UnsupportedOperationException("Error:YardCrane.process_ToPort()"); 
        }

        //取箱成功,吊具竖直移回lifting高度;
        this.holdVerticalMove_Trolley(currentHeightNumPosition, maxLiftingHeightNum);
        this.currentHeightNumPosition = this.maxLiftingHeightNum;
        //crane水平移动至相应Bay位;
        startPoint = new Point2D.Double(this.currentPosition.getX(), this.currentPosition.getY());
        Point2D goalPoint = new Point2D.Double(firstYCTask.getGoalPosition().getX(),firstYCTask.getGoalPosition().getY());
        this.holdMoveToThisPosition(startPoint, goalPoint);
//        this.hold(this.MoveToThisBay(startPoint,goalPoint)/getSimulationSpeed());
//        this.setYCPositionAndBay(this.getFirstYCTask().getGoalPosition());
        //System.out.print(this.toString()+"移至ToPort放箱Bay位"+"@"+simulationRealTime);
        //移动吊具至指定row;
        this.holdHorizontalMove_Trolley(currentRowPosition, this.firstYCTask.getGoalPosition());
//System.out.print("  移至ToPort放箱Row:"+this.currentRowPosition+"@"+simulationRealTime);
        //移动吊具至指定height;
        this.holdVerticalMove_Trolley(currentHeightNumPosition,
                yardBlock.findCurrentMaxHeightNum(currentRowPosition,currentBayPosition)+1);
//System.out.println("  移至ToPort取箱HeightNum:"+currentHeightNumPosition+simulationRealTime);
        //放箱;
        this.hold(this.Time_putContainersToBlock/getSimulationSpeed());
        this.obtainContainersFromYCToBlock(currentHeightNumPosition);
//System.out.println(this.toString()+"成功放箱"+simulationRealTime);
        //吊具收回
        this.holdVerticalMove_Trolley(currentHeightNumPosition,maxLiftingHeightNum);
//System.out.println(this.toString()+"小车成功移至YardCrane移动高度"+simulationRealTime);
        //结束该次ToPorWork任务,回到主进程获取下一个work任务;
        this.addOneOutputYCWorkList();
        this.firstYCTask = null;
    }
    @Override
    public void receiveRequestedResource(final double requestedCapacity,
            final Resource resource)
    {
        this.resume();
    }
    
    /**
     * 成功抓取任务列表中的集装箱;
     * 并设置container的状态，服务场桥信息;
     */
    private void obtainContainersFromBlockToYC(){
        if(this.firstYCTask == null){
            return;
        }
        if(this.firstYCTask.getContainer() == null){
            return;
        }
        this.currentServiceContainer = new Container[this.firstYCTask.getContainer().length];
        for(int i=0;i<this.currentServiceContainer.length;i++){
            this.currentServiceContainer[i] = this.firstYCTask.getContainer()[i];
            this.currentServiceContainer[i].setServiceYC(this.myself);
            this.currentServiceContainer[i].setState(StaticName.ONYC);
            this.currentServiceContainer[i].setPresentSlot(null);
        }
        this.yardBlock.moveContainer(this.currentServiceContainer);
//System.out.println(this.toString()+"obtainContainersFromBlockToYC完成");
    }
    /**
     * containers从YC转移至箱区中;
     * 更新containers.slot
     * 更新containers.state
     * 更新containers.serviceYC
     * @param heightNum 
     */
    private void obtainContainersFromYCToBlock(int heightNum) throws RemoteException{
        for (Container currentServiceContainer1 : this.currentServiceContainer) {
            currentServiceContainer1.setPresentSlot(new Slot(this.yard,this.yardBlock,
                    this.currentRowPosition,this.currentBayPosition,heightNum));
            currentServiceContainer1.setState(StaticName.ONSTOCKING);
            currentServiceContainer1.setServiceYC(null);
        }
        this.yardBlock.addContainer(this.currentServiceContainer);
//System.out.println("YC.obtainContainersFromYCToBlock(this.currentServiceContainer成功");
        //判断是否为卸船集装箱;
        if(this.currentServiceContainer[0].getFormalShip() != null){
            //判断是否为需要出港集疏运的集装箱;
            //是卸船箱;
//System.out.print("是要出港进行集疏运的集装箱");
            for (int i = 0; i < this.currentServiceContainer.length; i++) {
                this.currentServiceContainer[i].setTime_StartInBlock(simulationRealTime);
                this.currentServiceContainer[0].setTime_LeavingBlock(StaticName.TRIANGULAR);
                if (i != 0) {
                    currentServiceContainer[i].setTime_LeavingBlock(currentServiceContainer[0].getTime_LeavingBlock());
                }
            }
            //加入离港作业列表中;
            this.getYardBlock().getLeavingPortList().add(new LandSideYardTask(this.currentServiceContainer,
                    StaticName.LEAVINGPORT, null, null, null));
        }else{
//System.out.println("不是卸船箱");
        }
        this.currentServiceContainer = null;
    }
    /**
     * 该LoadingYC当前所在装卸点是否有LoadingAGV到达;
     * @param position
     * @return AGV agv
     */
    private synchronized AGV haveRightLoadingAGVArriving(Point2D position) {
//System.out.println(this.toString() + "YardCrane.haveRightLoadingAGVArriving()开始");
        if (this.yardBlock.getWaterSideArea().getTransferPoint1().equals(position) == true) {
//System.out.println("-----TransferPoint1-----YardCrane.haveRightLoadingAGVArriving()开始------");
            if (this.yardBlock.getWaterSideArea().getCar1() != null) {
                AGV agv = (AGV) this.yardBlock.getWaterSideArea().getCar1();
//System.out.println("----TransferPoint1-0-----:" + agv.getName()+agv.toString());
                if (agv != null && this.firstYCTask.getCar() != null
                        && this.firstYCTask.getCar().getName().equals(agv.getName())) {
                    return agv;
                }
//                if (agv != null) {
//                    System.out.println("--" + agv.getServiceQC().getServiceShip());
//                    System.out.println("--" + Arrays.toString(agv.getGoalLoadingContainers()));
//                    System.out.println("--" + Arrays.toString(agv.getContainersOnAGV()));
//                }
//                if(agv != null && agv.getServiceQC().getServiceShip() != null && agv.getServiceQC().getServiceShip() ==
//                        ((AGV)(this.firstYCTask.getCar())).getServiceQC().getServiceShip()){
//                    if(agv.getAGVstate().equals(((AGV)(this.firstYCTask.getCar())).getAGVstate())){
//                        System.out.println("1-0:" + Arrays.toString(((AGV)(this.firstYCTask.getCar())).getGoalLoadingContainers()));
//                        System.out.println("1-0:" + Arrays.toString(((AGV)(this.firstYCTask.getCar())).getContainersOnAGV()));
//                        System.out.println("1-0:" + Arrays.toString(agv.getGoalLoadingContainers()));
//                    }
//                }  
            }
//            System.out.println("----TransferPoint1-0--final---:" + this.firstYCTask.getWorkType());
//            System.out.println("----TransferPoint1-0--final---:" + ((AGV)(this.firstYCTask.getCar())).toString());
//            System.out.println("--" + ((AGV)(this.firstYCTask.getCar())).getServiceQC().getServiceShip());
//            System.out.println("----TransferPoint3-0--final---:" + ((AGV)(this.firstYCTask.getCar())).getGoalLoadingContainers()[0]);
//            System.out.println("----TransferPoint1-0---final--:" + "null");
        }
        if (this.yardBlock.getWaterSideArea().getTransferPoint2().equals(position) == true) {
//System.out.println("----TransferPoint2-----YardCrane.haveRightLoadingAGVArriving()开始------");
            if (this.yardBlock.getWaterSideArea().getCar2() != null) {
                AGV agv = (AGV) this.yardBlock.getWaterSideArea().getCar2();
//System.out.println("----TransferPoint2-0-----:" + agv.toString());
                if (agv != null && this.firstYCTask.getCar() != null
                        && this.firstYCTask.getCar().getName().equals(agv.getName())) {
                    return agv;
                }
//                if (agv != null) {
//                    System.out.println("--" + agv.getServiceQC().getServiceShip());
//                    System.out.println("--" + Arrays.toString(agv.getGoalLoadingContainers()));
//                    System.out.println("--" + Arrays.toString(agv.getContainersOnAGV()));
//                }
//                if(agv != null && agv.getServiceQC().getServiceShip() != null && agv.getServiceQC().getServiceShip() ==
//                        ((AGV)(this.firstYCTask.getCar())).getServiceQC().getServiceShip()){
//                    if(agv.getAGVstate().equals(((AGV)(this.firstYCTask.getCar())).getAGVstate())){
//                        System.out.println("2-0:" + Arrays.toString(((AGV)(this.firstYCTask.getCar())).getGoalLoadingContainers()));
//                        System.out.println("2-0:" + Arrays.toString(((AGV)(this.firstYCTask.getCar())).getContainersOnAGV()));
//                        System.out.println("2-0:" + Arrays.toString(agv.getGoalLoadingContainers()));
//                    }
//                }  
            }
//            System.out.println("----TransferPoint2-0--final---:" + ((AGV)(this.firstYCTask.getCar())).toString());
//            System.out.println("--" + ((AGV)(this.firstYCTask.getCar())).getServiceQC().getServiceShip());
//            System.out.println("----TransferPoint2-0--final---:" + this.firstYCTask.getWorkType());
//            //System.out.println("----TransferPoint3-0--final---:" + ((AGV)(this.firstYCTask.getCar())).getGoalLoadingContainers()[0]);
//            System.out.println("----TransferPoint2-0--final---:" + "null");
        }
        if (this.yardBlock.getWaterSideArea().getTransferPoint3().equals(position) == true) {
//System.out.println("----TransferPoint3-----YardCrane.haveRightLoadingAGVArriving()开始！！！！！------");
            if (this.yardBlock.getWaterSideArea().getCar3() != null) {
                AGV agv = (AGV) this.yardBlock.getWaterSideArea().getCar3();
//System.out.println("----TransferPoint3-0-----:" + agv.toString());
                if (agv != null && this.firstYCTask.getCar() != null
                        && this.firstYCTask.getCar().getName().equals(agv.getName())) {
                    return agv;
                }
//                if (agv != null) {
//                    System.out.println("--" + agv.getServiceQC().getServiceShip());
//                    System.out.println("--" + Arrays.toString(agv.getGoalLoadingContainers()));
//                    System.out.println("--" + Arrays.toString(agv.getContainersOnAGV()));
//                }
//                if(agv != null && agv.getServiceQC().getServiceShip() != null && agv.getServiceQC().getServiceShip() ==
//                        ((AGV)(this.firstYCTask.getCar())).getServiceQC().getServiceShip()){
//                    if(agv.getAGVstate().equals(((AGV)(this.firstYCTask.getCar())).getAGVstate())){
//                        System.out.println("3-0:" + Arrays.toString(((AGV)(this.firstYCTask.getCar())).getGoalLoadingContainers()));
//                        System.out.println("3-0:" + Arrays.toString(((AGV)(this.firstYCTask.getCar())).getContainersOnAGV()));
//                        System.out.println("3-0:" + Arrays.toString(agv.getGoalLoadingContainers()));
//                        
//                    }
//                }  
            }
//            System.out.println("----TransferPoint3-0--final---:" + ((AGV)(this.firstYCTask.getCar())).toString());
//            System.out.println("----TransferPoint3-0--final---:" + this.firstYCTask.getWorkType());
//            System.out.println("----TransferPoint3-0--final---:" + ((AGV)(this.firstYCTask.getCar())).getGoalLoadingContainers()[0]);
//            System.out.println("--" + ((AGV)(this.firstYCTask.getCar())).getServiceQC().getServiceShip());
//            System.out.println("----TransferPoint3-0--final---:" + "null");       
        }
        return null;
    }

    /**
     * boolean：该UnloadingYC当前所在装卸点是否有UnloadingAGV到达;
     * AGV有箱待卸至堆场;
     * @param position
     * @return AGV agv
     */
    private AGV haveRightUnloadingAGVArriving(Point2D position){
        if(this.yardBlock.getWaterSideArea().getTransferPoint1().equals(position) == true){
            if(this.yardBlock.getWaterSideArea().getCar1() != null){
                AGV agv = (AGV)this.yardBlock.getWaterSideArea().getCar1();
                if(agv!= null && this.firstYCTask.getCar()!= null 
                        && agv.getName().equals(this.firstYCTask.getCar().getName())){
                    return agv;
                }
            }
            return null;
        }else if(this.yardBlock.getWaterSideArea().getTransferPoint2().equals(position) == true){
            if(this.yardBlock.getWaterSideArea().getCar2() != null){
                AGV agv = (AGV)this.yardBlock.getWaterSideArea().getCar2();
                if(agv!= null && this.firstYCTask.getCar()!= null 
                        && agv.getName().equals(this.firstYCTask.getCar().getName())){
                    return agv;
                }
            }
            return null;
        }else if(this.yardBlock.getWaterSideArea().getTransferPoint3().equals(position) == true){
            if(this.yardBlock.getWaterSideArea().getCar3() != null){
                AGV agv = (AGV)this.yardBlock.getWaterSideArea().getCar3();
                if(agv!= null && this.firstYCTask.getCar()!= null 
                        && agv.getName().equals(this.firstYCTask.getCar().getName())){
                    return agv;
                }
            }
            return null;
        }else{
            System.out.println("检查！！Error:YardCrane.haveRightLoadingAGVArriving(Point2D position)出错！！");
            throw new UnsupportedOperationException("Error:YardCrane.haveRightLoadingAGVArriving(Point2D position)出错！！");
        }
    }
    /**
     *该LoadingYC当前所在装卸点是否有LoadingTruck到达;
     * @param position
     * @return Truck truck
     */
    private Truck haveRightLoadingTruckArriving(Point2D position){
        if(this.firstYCTask.getWorkType().equals(StaticName.LEAVINGPORT)){
            Truck truck = new Truck(this.firstYCTask,position);
            return truck;
        }else{
            return null;
        }
    }
    /**
     * 关联YC和Port
     */
    private void relatePort() {
        this.port.setYardCranes(this.myself);
    }

    /**
     * 关联YC和block
     */
    private void relateYard(){
        this.yard = this.port.getRoadNetWork().getYardArea("Yard"+this.yardNum);
        this.port.getRoadNetWork().getYardArea("Yard"+this.yardNum).addCrane(this.myself);
    }
    @Override
    public String toString(){
        if(this.firstYCTask != null){
            return this.YCname+"-"+this.firstYCTask.getWorkType();
        }else{
            return this.YCname+"-Free";
        }
    }
    /**
     * @return the block
     */
    public Yard getYard() {
        return yard;
    }
    /**
     * @return the yardBlock
     */
    public Block getYardBlock() {
        return yardBlock;
    }
    /**
     * @param yardBlock the yardBlock to set
     */
    public void setYardBlock(Block yardBlock) {
        this.yardBlock = yardBlock;
    }
    /**
     * 没有时为null
     * @return the currentServiceContainer
     */
    public Container[] getCurrentServiceContainer() {
        return currentServiceContainer;
    }
    public boolean isFree(){
        return this.firstYCTask == null;
    }
    /**
     * @return the firstYCTask
     */
    public YCTask getFirstYCTask() {
        if(this.firstYCTask != null){
            return firstYCTask;
        }else{
            //YardTask向YC分配任务;若成功,则更新firstYCTask;
            this.yardBlock.assignTaskToThisYC(this.myself);
            if(this.firstYCTask == null){
//                this.holdTwoMinute();
//                this.yardBlock.assignOpositeTaskToThisYC(this.myself);
                return null;
            }else{
                return this.firstYCTask;
            }
        }
    }
    /**
     * @param firstYCTask the firstYCTask to set
     */
    public void setFirstYCTask(YCTask firstYCTask) {
        this.firstYCTask = firstYCTask;
    }
    /**
     * 设置YC当前bay位和坐标;
     * 坐标设为bay中心点坐标;
     * deep col
     */
    private void setYCPositionAndBay(Point2D position) {
        //find bayNum or Null of position;
        int bayNum = this.yardBlock.findBayNum(position);//没有贝位时findBayNum()返回-1;
        if(bayNum == -1){
//System.out.println(this.toString()+"注意：：bayNum == -1！！");
        }
        //set Bay：
        this.currentBayPosition = bayNum;
        //set Position：
        this.currentPosition.setLocation(this.currentPosition.getX(), position.getY());
    }
    /**
     * @return the currentRowPosition
     */
    public int getCurrentRowPosition() {
        return currentRowPosition;
    }
    /**
     * 计算YC小车的水平方向移动所需时间;在装卸区的移动;
     * @param startRowNum
     * @param goalPosition
     * @return double time;
     */
    private void holdHorizontalMove_Trolley(int startRowNum, Point2D goalPosition) throws SimRuntimeException, RemoteException {
        Point2D endP = new Point2D.Double(0,0);
        endP.setLocation(goalPosition);
        double startX = calculateRowPosition(startRowNum);
        double endX = endP.getX();     
        double pathLength = Math.abs(endX-startX);
        if(pathLength == 0){
            this.hold(InputParameters.animationTimeUnit);
            this.currentRowPosition = this.getYardBlock().findNearestRowNum(endP);
            return;
        }
        double minute = pathLength/this.horizontalSpeed_trolley;
        double minuteAfter = simulationRealTime + minute;
        double nowSimulationSpeed = getSimulationSpeed();
        double time = minute / nowSimulationSpeed;
        int num = (int) (time / InputParameters.animationTimeUnit);
        if (InputParameters.isNeedAnimation() == false) {
            num = 0;
        }
        //int i = 0; or = 1????
        for (int i = 0; i < num; i++) {
            if (num <= 1) {
                num = 0;
                break;
            }
            double nowLength = nowSimulationSpeed * this.horizontalSpeed_trolley * i * InputParameters.animationTimeUnit;
            Point2D nowPoint = new Point2D.Double(0,0);
            if(startX<endX){
                nowPoint.setLocation(startX+nowLength,endP.getY());
            }else{
                nowPoint.setLocation(startX-nowLength,endP.getY());
            }
            this.currentRowPosition = this.getYardBlock().findNearestRowNum(nowPoint);
            this.hold(InputParameters.animationTimeUnit);
            if (InputParameters.getSimulationSpeed() != nowSimulationSpeed) {
                //说明此刻有改变仿真速度;
                time = ((minute - (i + 1) * InputParameters.animationTimeUnit * nowSimulationSpeed) / getSimulationSpeed())
                        + (i + 1) * InputParameters.animationTimeUnit;
                num = (int) ((time / InputParameters.animationTimeUnit) - (i + 1) * (getSimulationSpeed() / nowSimulationSpeed));
                i = (int) (nowSimulationSpeed * this.horizontalSpeed_trolley * i * InputParameters.animationTimeUnit / getSimulationSpeed());
                nowSimulationSpeed = getSimulationSpeed();
                nowLength = nowSimulationSpeed * this.horizontalSpeed_trolley * i * InputParameters.animationTimeUnit;
                if (startX < endX) {
                    nowPoint.setLocation(startX + nowLength, endP.getY());
                } else {
                    nowPoint.setLocation(startX - nowLength, endP.getY());
                }
                this.currentRowPosition = this.getYardBlock().findNearestRowNum(nowPoint);
            }
            if(simulationRealTime > minuteAfter){
                break;
            }
        }
        if (time > ((num) * InputParameters.animationTimeUnit)) {
            this.hold(format(time - ((num) * InputParameters.animationTimeUnit)));
        }
        this.currentRowPosition = this.getYardBlock().findNearestRowNum(endP);
    }

    /**
     * 更新Container[]信息;
     * 更新YC信息;
     * 更新AGV信息;
     */
    private void updateContainersFromYCToAGV(YardCrane YC,AGV agv) throws RemoteException{
        agv.obtainContainersFromYC(currentServiceContainer);//更新AGV,containers[];
        YC.currentServiceContainer = null;
    }
    /**
     * 更新Container[]信息;
     * 更新YC信息;
     * 更新AGV信息;
     */
    private void updateContainersFromAGVToYC(YardCrane YC,AGV agv) throws RemoteException{
        if(agv.getContainersOnAGV() ==null){
            System.out.println("Error::::::"+agv.toString()+"没有container！！！！");
            throw new UnsupportedOperationException("Error!!!"+this.toString()+"Error::::::"+agv.toString()+"没有container！！！");
        }
        YC.currentServiceContainer = new Container[YC.firstYCTask.getContainer().length];
        for(int i = 0;i<YC.currentServiceContainer.length;i++){
            YC.currentServiceContainer[i] = agv.getContainersOnAGV()[i];
            YC.currentServiceContainer[i].setState(StaticName.ONYC);
            YC.currentServiceContainer[i].setServiceYC(YC);
            YC.currentServiceContainer[i].setTime_StartInBlock(simulationRealTime);
        }
        agv.clearContainers();//清空agv中Container[]信息;
    }
    /**
     * 更新YC和该次任务中的containers信息;
     * @param YC 
     */
    private void updateContainersFromOUTSIDEToYC(YardCrane YC) throws RemoteException{
        YC.currentServiceContainer = new Container[YC.firstYCTask.getContainer().length];
        for(int i = 0;i<YC.currentServiceContainer.length;i++){
            YC.currentServiceContainer[i] = YC.firstYCTask.getContainer()[i];
            YC.currentServiceContainer[i].setState(StaticName.ONYC);
            YC.currentServiceContainer[i].setServiceYC(YC);
            YC.currentServiceContainer[i].setTime_StartInBlock(simulationRealTime);
        }
        /**
         * /////////////////////////////////////////////////////////////////////如果加了truck，要记得清空truck上的消息;
         */
    }
    /**
     * @param YC
     * @param truck 
     */
    private void updateContainersFromYCToTruck(YardCrane YC,Truck truck) throws RemoteException{
        for(int i=0;i<YC.currentServiceContainer.length;i++){
            YC.currentServiceContainer[i].setState(StaticName.OUTSIDE);
            YC.currentServiceContainer[i].setTime_LeavingBlock(simulationRealTime);
        }
        YC.currentServiceContainer = null;
        /**
         * /////////////////////////////////////////////////////////////////////如果要加truck，此处应改;
         */
    }
    /**
     * Unloading
     * 更新Container[]信息;
     * 更新YC信息;
     * 更新AGV信息;
     */
    private void updateContainersFromAGVCoupleToYC(YardCrane YC) throws RemoteException{
        YC.currentServiceContainer = new Container[this.firstYCTask.getContainer().length];
        int num = 0;
        if(this.yardBlock.getAGVCouple()[0].centralPosition.distance(this.firstYCTask.getStartPosition())<1){
            num = 0;
        }else{
            num = 1;
        }
        for(int i = 0;i<YC.currentServiceContainer.length;i++){
            YC.currentServiceContainer[i] = this.firstYCTask.getContainer()[i];
            YC.currentServiceContainer[i].setState(StaticName.ONYC);
            YC.currentServiceContainer[i].setServiceYC(YC);
            YC.currentServiceContainer[i].setTime_StartInBlock(simulationRealTime);
        }
        this.yardBlock.getAGVCouple()[num].clearContainersToYC(this.myself);//清空agv中Container[]信息;
    }
    /**
     * @return the maxContainersHeightNum
     */
    public int getMaxContainersHeightNum() {
        return maxContainersHeightNum;
    }

    /**
     * @return the trackGuage
     */
    public double getTrackGuage() {
        return trackGuage;
    }

    /**
     * 计算该行所在X坐标
     * @param row
     * @return 
     */
    public double calculateRowPosition(int row) {
        double d = 0.5*(InputParameters.getRMGtrackGuage()-InputParameters.getPerYCRowNum()*InputParameters.getWidthOfContainer()
                -InputParameters.getDistanceOfContainers()*(InputParameters.getPerYCRowNum()-1));
        double x = this.getYardBlock().getP2().getX()-d-(0.5+(row-1))*InputParameters.getWidthOfContainer()
                -InputParameters.getDistanceOfContainers()*(row-1);
        return x;
    }

    /**
     * YC大架子从startPoint移动至Position
     * 哈哈哈我就叫你大门架子了
     * @param startPoint
     * @param endPoint
     * @return 
     */
    private void holdMoveToThisPosition(Point2D startPoint, Point2D endPoint) throws SimRuntimeException, RemoteException {
        double length = Math.abs(endPoint.getY()-startPoint.getY());
        if(length == 0){
            this.hold(InputParameters.animationTimeUnit);
            this.setYCPositionAndBay(endPoint);
            return;
        }
        double minute = length/this.horizontalSpeed_crane;/////这里speed是m/min
        double minuteAfter = simulationRealTime + minute;
        double nowSimulationSpeed = getSimulationSpeed();
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
            double nowLength = nowSimulationSpeed * this.horizontalSpeed_crane * i * InputParameters.animationTimeUnit;
            Point2D nowPoint = new Point2D.Double(0,0);
            if(startPoint.getY()<endPoint.getY()){
                nowPoint.setLocation(startPoint.getX(),startPoint.getY()+nowLength);
            }else{
                nowPoint.setLocation(startPoint.getX(),startPoint.getY()-nowLength);
            }
            this.setYCPosition(nowPoint);///在里面不变bay位;因为setP&Bay会计算准确贝位,不适用;
            this.hold(InputParameters.animationTimeUnit);
            if (InputParameters.getSimulationSpeed() != nowSimulationSpeed) {
                //说明此刻有改变仿真速度;
                totalTime = ((minute - (i + 1) * InputParameters.animationTimeUnit * nowSimulationSpeed) / getSimulationSpeed())
                        + (i + 1) * InputParameters.animationTimeUnit;
                num = (int) ((totalTime / InputParameters.animationTimeUnit) - (i + 1) * (getSimulationSpeed() / nowSimulationSpeed));
                i = (int) (nowSimulationSpeed * this.horizontalSpeed_crane * i * InputParameters.animationTimeUnit / getSimulationSpeed());
                nowSimulationSpeed = getSimulationSpeed();
                nowLength = nowSimulationSpeed * this.horizontalSpeed_crane * i * InputParameters.animationTimeUnit;
                if (startPoint.getY() < endPoint.getY()) {
                    nowPoint.setLocation(startPoint.getX(), startPoint.getY() + nowLength);
                } else {
                    nowPoint.setLocation(startPoint.getX(), startPoint.getY() - nowLength);
                }
                this.setYCPosition(nowPoint);
            }
            if(simulationRealTime > minuteAfter){
                break;
            }
        }
        if (totalTime > ((num) * InputParameters.animationTimeUnit)) {
            this.hold(format(totalTime - ((num) * InputParameters.animationTimeUnit)));
        }
        this.setYCPositionAndBay(endPoint);
    }
    
    private void holdVerticalMove_Trolley(int startHeightNum, int goalHeightNum) throws SimRuntimeException, RemoteException {
        double length = Math.abs((goalHeightNum-startHeightNum)*InputParameters.getHeightOfContainer());
        double minute = length/this.verticalSpeed_trolley;
        double minuteAfter = simulationRealTime + minute;
        double nowSimulationSpeed = getSimulationSpeed();
        double time = minute / nowSimulationSpeed;
        if(length == 0){
            this.hold(InputParameters.animationTimeUnit);
            this.currentHeightNumPosition = goalHeightNum;
            return;
        }
        int num = (int) (time / InputParameters.animationTimeUnit);
        if (InputParameters.isNeedAnimation() == false) {
            num = 0;
        }
        //int i = 0; or = 1????
        for (int i = 0; i < num; i++) {
            if (num <= 1) {
                num = 0;
                break;
            }
            double nowLength = nowSimulationSpeed * this.verticalSpeed_trolley * i * InputParameters.animationTimeUnit;
            if(startHeightNum<goalHeightNum){
                this.currentHeightNumPosition = (int)(startHeightNum+((nowLength)/InputParameters.getHeightOfContainer()));
            }else{
                this.currentHeightNumPosition = (int)(startHeightNum-((nowLength)/InputParameters.getHeightOfContainer()));
            }
            this.hold(InputParameters.animationTimeUnit);
            if (InputParameters.getSimulationSpeed() != nowSimulationSpeed) {
                //说明此刻有改变仿真速度;
                time = ((minute - (i + 1) * InputParameters.animationTimeUnit * nowSimulationSpeed) / getSimulationSpeed())
                        + (i + 1) * InputParameters.animationTimeUnit;
                num = (int) ((time / InputParameters.animationTimeUnit) - (i + 1) * (getSimulationSpeed() / nowSimulationSpeed));
                i = (int) (nowSimulationSpeed * this.verticalSpeed_trolley * i * InputParameters.animationTimeUnit / getSimulationSpeed());
                nowSimulationSpeed = getSimulationSpeed();
                nowLength = nowSimulationSpeed * this.verticalSpeed_trolley * i * InputParameters.animationTimeUnit;
                if (startHeightNum < goalHeightNum) {
                    this.currentHeightNumPosition = (int) (startHeightNum + ((nowLength) / InputParameters.getHeightOfContainer()));
                } else {
                    this.currentHeightNumPosition = (int) (startHeightNum - ((nowLength) / InputParameters.getHeightOfContainer()));
                }
                this.currentHeightNumPosition = goalHeightNum;
            }
            if(simulationRealTime > minuteAfter){
                break;
            }
        }
        if (time > ((num) * InputParameters.animationTimeUnit)) {
            this.hold(format(time - ((num) * InputParameters.animationTimeUnit)));
        }
        this.currentHeightNumPosition = goalHeightNum;
    }
    /**
     * deep copy;
     * @param nowPoint 
     */
    private void setYCPosition(Point2D nowPoint) {
        this.currentPosition = new Point2D.Double(nowPoint.getX(),nowPoint.getY());
    }

    /**
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    private void holdTwoMinute() {
        try {
            this.hold(2.0/getSimulationSpeed());
        } catch (SimRuntimeException ex) {
            Logger.getLogger(YardCrane.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(YardCrane.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addOneOutputYCWorkList() {
        firstYCTask.setEndT(Double.parseDouble(port.getSimulatorTime())*getSimulationSpeed());
        String res = this.number+"\t"+
                firstYCTask.getWorkType()+"\t"+
                firstYCTask.getContainer().length*(firstYCTask.getContainer()[0].getFTsize()/20)+"\t"+
                firstYCTask.getStartT()+"\t"+
                firstYCTask.getEndT()+"\t"+
                firstYCTask.getStartPosition().getX()+"\t"+
                firstYCTask.getStartPosition().getY()+"\t"+
                firstYCTask.getGoalPosition().getX()+"\t"+
                firstYCTask.getGoalPosition().getY();
        switch(this.firstYCTask.getWorkType()){
            case StaticName.UNLOADING:
                res += "\t"+"-100";
                break;
            case StaticName.LOADING:
                res += "\t"+(firstYCTask.getContainer()[0].getTime_LeavingBlock()-firstYCTask.getContainer()[0].getTime_StartInBlock());
                break;
            case StaticName.TOPORT:
                res += "\t"+"-100";
                break;
            case StaticName.LEAVINGPORT:
                res += "\t"+(firstYCTask.getContainer()[0].getTime_LeavingBlock()-firstYCTask.getContainer()[0].getTime_StartInBlock());
                break;
            default:
                res += "\t"+"-100";
        }
        strb.append(res);
        if(strb.length()>20){
            OutputParameters.addRowsToFile("WorlList_YC/Yard"+this.yardNum+"Block"+this.blockNum, strb);  
            strb = new StringBuffer();
        }
        double time = simulationRealTime;
        double num = this.getYardBlock().getContainersTEU();
        OutputParameters.addOneRowToFile("TEU_Block/Yard"+this.yardNum+"Block"+this.blockNum, time+"\t"+num);  
    }

    public String getWorkType() {
        if(this.firstYCTask != null){
            return this.firstYCTask.getWorkType();
        }else{
            return "空闲";
        }
    }
}
