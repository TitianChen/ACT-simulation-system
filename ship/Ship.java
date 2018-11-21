
package ship;
import apron.Berth;
import CYT_model.Container;
import CYT_model.Point2D;
import CYT_model.Port;
import apron.QuayCrane;
import algorithm.QuickSort;
import java.io.BufferedReader;
import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.Queue;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.Resource;
import nl.tudelft.simulation.dsol.formalisms.ResourceRequestorInterface;
import nl.tudelft.simulation.dsol.formalisms.process.Process;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;
import nl.tudelft.simulation.logger.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import parameter.InputParameters;
import static parameter.InputParameters.INPUTPATH;
import static parameter.InputParameters.getBigBerthDWT;
import static parameter.InputParameters.getRandom;
import static parameter.InputParameters.getSimulationSpeed;
import parameter.OutputParameters;
import static parameter.OutputParameters.outputSingleParameters;
import parameter.StaticName;
import static parameter.OutputParameters.simulationRealTime;

/**
 * Class Ship船舶类
 * 基于DSOL
 * @author YutingChen, yuting.chen17@imperial.ac.uk
 * Dalian University of Technology
 */
public class Ship extends Process implements ResourceRequestorInterface
{
    private static final long serialVersionUID = 1L;
    
    /** a reference to protect boats from being garbage collection */
    protected Ship mySelf = null;
    private Port port = null;
    private static int number = 0;
    private String description = "Ship(";
    /**----------shiptype-确定船型信息--------------**/
    private final int DWT;
    private int DWTLevel;//吨级
    private double shipLength;//总长;
    private double shipWidth;//型宽;
    private double shipHeight;//型深
    private double eatWater;//吃水
    private double shipD;//富余长度;
    private int minTEU;//最小载箱量;//TEU:20ft;
    private int maxTEU;//最大载箱量;
    private final String shipType;//1:大型船，2：中型船，3：小型船;
    //--------集港开始时间，截港时间---------//
    private final double earliestTimeIn;//集港开始时间至船舶进港时间之间的时间差;
    private final double latestTimeIn;//截港时间至船舶进港时间之间的时间差;
    
    //--------船舶作业线------由船型确定//
    private int maxQCLine;//船舶作业线上限;
    private int minQCLine;//船舶作业线下限;
    //containers信息;
    private final ShipContainers shipContainers;//船上container信息;
    private double[] truckGatheringPortTimeList;//集卡集港作业时间列表;
    //--------------Output------------//
    public double arrivingTime = 0;
    public double getOutsideAnchorageTime = 0;
    public String[] assignedBerthName;
    public double getChannelTime = 0;
    public double startberthingTime = 0;
    public String[] assignedQCName;
    public double startUnloadingTime = 0;
    public double quayCraneFinishToMoveTime = 0;
    public double endUnloadingTime = 0;
    public double startLoadingTime = 0;
    public double endLoadingTime = 0;
    public double startUnberthingTime = 0;
    public double endUnberthingTime = 0;
    //将输出时间改成其他服务水平的参数
    //位置
    private Point2D berthingLocation;//中轴线靠泊位置;/////用什么表示比较好？？
    //船舶状态;
    public String shipState = StaticName.FREE;//等待--FREE   作业中--UNLOADING,LOADING   离开--LEAVING
    //-----分配信息-----//
    Berth[] assignedberth;//分配泊位;
    private QuayCrane[] assignedQC;//分配岸桥;
    private final DEVSSimulator simulator1;
    
    /**
     * constructs a new Boat
     * @param simulator1 the simulator to schedule on
     * @param port the port to sail to
     * @param shipDWT 船舶载重吨;
     * @param arrivaltime
     * @throws java.io.IOException
     */
    public Ship(final DEVSSimulator simulator1, final Port port,
            String shipDWT,double arrivaltime) throws IOException
    {
        super(simulator1);
        this.simulator1 = (DEVSSimulator)super.simulator;
        this.mySelf = this;
        this.port = port;        
        this.description = this.description + (Ship.number++) + ") ";
        //shipType;
        this.shipType = "1";
        this.DWT = Integer.parseInt(shipDWT);
        //max/min QCLine;
        this.setMaxMinQCLine();
 //       this.maxQCLine = 3;//////////////////
        //ship state;
        this.shipState = StaticName.FREE;
        this.assignedberth = null;
        this.assignedQC = null;
        this.setShipParameters();
        this.shipContainers = new ShipContainers(this.mySelf);
        //
        this.arrivingTime = arrivaltime;
        this.earliestTimeIn = InputParameters.getEarliestTimeIn();//集港开始时间至船舶进港时间之间的时间差;
        this.latestTimeIn = InputParameters.getLatestTimeIn();//截港时间至船舶进港时间之间的时间差;
//********************靠离泊信息，靠离泊位置 未定;********************************    
    }
    private void setShipParameters() throws IOException{
        int row = 11,column = 9;
        double[][] arr = new double[row][column]; //插入的数组  
        File file = new File(INPUTPATH+"ShipDesign.txt");  //存放数组数据的文件  
        try (BufferedReader in = new BufferedReader(new FileReader(file))) {
            String line;  //一行数据
            int row1=0;
            line = in.readLine();//第一行是变量名;
            //逐行读取，并将每个数组放入到数组中
            while((line = in.readLine()) != null){
                String[] temp = line.split("\t");
                for(int j=0;j<temp.length;j++){
                    arr[row1][j] = Double.parseDouble(temp[j]);
                }
                row1++;
            }  
        }
        int line;
        for(int i = 0;i<row;i++){
            if(arr[i][1]<=this.getDWT() && arr[i][2]>=this.getDWT()){
                line = i;
                DWTLevel = (int) arr[line][0];
                shipLength = arr[line][3];//总长;
                if(shipLength<=40){
                    shipD = 5;
                }else if(shipLength<=85){
                    shipD = 10;
                }else if(shipLength <= 150){
                    shipD = 15;
                }else if(shipLength <= 200){
                    shipD = 20;
                }else if(shipLength <= 230){
                    shipD = 25;
                }else{
                    shipD = 30;
                }
                shipWidth = arr[line][4];//型宽;
                shipHeight = arr[line][5];//型深
                eatWater = arr[line][6];//吃水
                minTEU = (int) arr[line][7];//最小载箱量;//TEU:20ft;
                maxTEU = (int) arr[line][8];//最大载箱量;
            }
        }
        
    }
    /**
     * @throws java.rmi.RemoteException
     * @throws nl.tudelft.simulation.dsol.SimRuntimeException
     */
    @Override
    //船舶服务进程;
    public  void process() throws RemoteException, SimRuntimeException, NullPointerException
    {
        this.process_TruckGathering();//先进行集港作业的生成进程;
        while(simulationRealTime < this.arrivingTime){
            this.hold(1.0/getSimulationSpeed());
        }
        //船舶到达;
        this.arrivingTime = simulationRealTime;
        //----------------------------参数输入---------------------------------//时间步长改为通过txt输入？？
        //设置判断中的等待步长;
        double waitStep_outside_anchorage = 2;
        double waitStep_berth = 5;
        double waitStep_navigation_condition = 2;
        double waitStep_berthing_requirement = 2;
        double waitStep_QC = 2;
        //设置各活动duration;
        double start_To_outsideanchorage = 5;
        double outsideanchorage_To_channel = 10;
        double channel_To_berth = 10;
        double berth_leaving = 10;
        //----------------------------进港,进程开始----------------------------------//
        try
        {
            //港外锚地是否空闲??
            while(this.getPort().getOutsideAnchorage().getAvailableCapacity() <= 0){
                this.hold(waitStep_outside_anchorage/getSimulationSpeed());
                //System.out.println(this+"判断：waiting for available outsideanchorage area@"+simulationRealTime);
            }
            //事件：驶入港外锚地，request&suspend
            this.getPort().getOutsideAnchorage().requestCapacity(1.0, this);
            this.suspend();
            System.out.println(this+"事件1：驶入港外锚地@"+simulationRealTime);
            this.getOutsideAnchorageTime = simulationRealTime;
            
            //活动：
            this.hold(start_To_outsideanchorage/getSimulationSpeed());//5
            //是否有合适泊位??
            while(this.assignBerth() == false){//////此处request Resource
                this.hold(waitStep_berth/getSimulationSpeed());
                //System.out.println(this+"判断：waiting for available berth@"+simulationRealTime);
            }
            this.suspend();
            //泊位调配成功，request&suspend
            this.assignedBerthName = new String[this.assignedberth.length];
            for(int i = 0;i<this.assignedBerthName.length;i++){
                this.assignedBerthName[i] = this.assignedberth[i].getName();
            }
            //System.out.println(this+"指泊：get Berth successful@"+simulationRealTime);
            //是否有通航条件??是否航道有空闲??
            while(this.getPort().isNavigationConditionOK() == false || this.getPort().getChannel().getAvailableCapacity() <= 0){
                this.hold(waitStep_navigation_condition/getSimulationSpeed());
                //System.out.println(this+"判断：waiting for NavigationCondition or Channel@"+simulationRealTime);  
            }
            //事件：进入航道;request&release&suspend
            this.getPort().getChannel().requestCapacity(1.0, this); 
            this.getPort().getOutsideAnchorage().releaseCapacity(1.0);
            this.suspend();
            this.getChannelTime = simulationRealTime;
            System.out.println(this+"事件2：进入航道@"+simulationRealTime);
            //活动：
            this.hold(outsideanchorage_To_channel/getSimulationSpeed());//5
            this.getPort().getChannel().releaseCapacity(1.0);
            //事件：靠泊;
            //是否满足靠泊需求??
            while(this.getPort().isBerthingRequirement() == false){
                this.hold(waitStep_berthing_requirement/getSimulationSpeed());
                //System.out.println(this+"判断：waiting for berthing requirement@"+simulationRealTime);
            }
            this.startberthingTime = simulationRealTime;
            System.out.println(this+"事件4：靠泊@"+simulationRealTime);
            //活动
            this.hold(channel_To_berth/getSimulationSpeed());//2
            //泊位做标记;
            for (Berth assignedberth1 : this.assignedberth) {
                assignedberth1.setBerthState(0); //泊位做标记:船舶在泊,等待装卸;     
            }
    //        System.out.println(this+"活动4@"+super.simulator.getSimulatorTime());

            //---------------靠泊完成，开始装卸子系统装卸部分---------------------
            //判断：是否满足装卸要求----------------------------------------------
            //是否有合适QC??
            while(this.assignQC() == false){
                this.hold(waitStep_QC/getSimulationSpeed());
                //System.out.println(this+"判断：waiting for available QC@"+simulationRealTime);
            }
            this.assignedQCName = new String[this.getAssignedQC().length];
            for(int i=0;i<this.assignedQCName.length;i++){
                this.assignedQCName[i]  = this.getAssignedQC()[i].getName();
            }
            //设置所有分配岸桥的服务位置;
            this.setAllQCsServiceLocation();
            ///////前沿装卸船作业////////
            //QC调配成功，request resource成功
//System.out.println(this+"岸桥分配：get QCs successful@"+simulationRealTime); 
            //事件：开始装卸船作业;
            this.shipState = StaticName.UNLOADING;//船舶状态改为作业中;
            this.startUnloadingTime = simulationRealTime;
            System.out.println(this+"事件5：开始装卸@"+simulationRealTime);
            for (Berth assignedberth1 : this.assignedberth) {
                assignedberth1.setBerthState(1); //泊位做标记:船舶在泊,开始装卸;     
            }
            this.suspend();
            //---------------------------装卸作业开始----------------------------//
            //卸船作业开始;
            while(this.shipContainers.getPresent_needunload_num()>0
                    || this.allQCFree() == false){
                this.hold(1.0/getSimulationSpeed());
            }
            this.endUnloadingTime = simulationRealTime;
            System.out.println(this+"-------Unlading结束-------@"+simulationRealTime);
            this.shipState = StaticName.FREE;
            //卸船作业结束;
            this.hold(10/getSimulationSpeed());//装卸船之间隔个10min;
            //装船作业开始;
            System.out.println(this+"-------Loading开始-------@"+simulationRealTime);
            this.shipState = StaticName.LOADING;
            this.startLoadingTime = simulationRealTime;
            while(this.getShipContainers().allLoadingContainersOnShip() == false
                   || this.allQCFree() == false){
                this.hold(1.0/getSimulationSpeed());
                //System.out.println(this+"等待装船结束@"+simulationRealTime);
                if(this.allQCFree() == true && this.getShipContainers().getPresent_needload_num() < 2){
                    break;
                }
                if((simulationRealTime-this.assignedQC[0].getQCWork()[0].getTwStartTime()>120)&&
                        this.getShipContainers().allLoadingContainersOnShip() == false){
                    //QC等待时间过长;---可能出问题了???
                    this.shipContainers.clearNeedLoadNum();
                }
            }
            //--------------------------装卸作业结束---------------------------//
            this.endLoadingTime = simulationRealTime;
            this.shipState = StaticName.FREE;
            boolean itag = true;
            while(itag){
                itag = true;
                for (QuayCrane assignedQC1 : this.assignedQC) {
                    if (assignedQC1.isMainTrolleyFree() && assignedQC1.isMainTrolleyFree()) {
                        itag = false;
                    }
                }
                this.hold(1/getSimulationSpeed());
            }
            this.shipContainers.clearNeedLoadNum();
            this.hold(10/getSimulationSpeed());
            System.out.println(this+"活动6：卸装船结束@"+simulationRealTime);
            if (this.quayCraneFinishToMoveTime == 0){
                this.quayCraneFinishToMoveTime = this.startUnloadingTime;
            }
            System.out.println(this+"-------Loading结束-------@"+simulationRealTime);
            //装船作业结束;
            //////装卸船任务结束////////
            //释放QC,解除关联，重置状态参数;
//System.out.println(this+"-------setShipLeave()-------@"+simulationRealTime);
            for(QuayCrane QC1 : this.assignedQC){
                QC1.setShipLeave();//解除关联,储存装卸信息,重置QCWork;
//System.out.println(this+"-------QC1-------@"+simulationRealTime);
            }
//System.out.println(this+"-------Berth-------@"+simulationRealTime);
            for (Berth assignedberth1 : this.assignedberth) {
//System.out.println(this+"-------Berthsss------@"+simulationRealTime);
                assignedberth1.setBerthState(2); //泊位做标记:船舶在泊,结束装卸    
            }
//System.out.println(this+"-------Berth完成-------@"+simulationRealTime);
            //释放岸桥资源;
//System.out.println(this+"QC.setShipLeave();@"+simulationRealTime);
            this.getPort().getQuayCraneResource().releaseCapacity(this.getAssignedQC().length);
            this.shipState = StaticName.LEAVING;//船舶状态改为离开;
            //判断：是否航道有空闲?
            while(this.getPort().getChannel().getAvailableCapacity() <= 0){
                this.hold(waitStep_navigation_condition/getSimulationSpeed());
//System.out.println(this+"判断：waiting for Channel@"+simulationRealTime);  
            }
            //航道空闲,request资源;
            System.out.println(this+"事件7：开始驶离@"+simulationRealTime);
            this.getPort().getChannel().requestCapacity(1.0, this);
            this.suspend();
            //事件：准备驶离;request&suspend
            this.startUnberthingTime = simulationRealTime;
            for (Berth berth : this.assignedberth) {
                berth.setBerthState(3); //泊位做标记:船舶驶离，但暂时禁止其他船舶进入本船靠泊的泊位  
            }
            //活动：船舶驶离
            this.hold(berth_leaving/getSimulationSpeed());//驶离用时
            this.endUnberthingTime = simulationRealTime;
            for(Berth berth:this.assignedberth){
                berth.setShipLeave();
                //泊位状态重置;其中berth参数重置为4;即泊位完全空闲，允许其他船舶靠泊;
            }
            this.looseBerth();//释放泊位资源;改变泊位状态;
            this.getPort().getChannel().releaseCapacity(1.0);
            System.out.println(this+"事件8：结束驶离@"+simulationRealTime);
            //船舶成功离港;
            //输出船舶TimeLine到txt中;
            this.outPut();
            System.out.println(this.toString() + "----arrived at time=" + arrivingTime
                    + " and left at time=" + simulationRealTime
                    + ". ProcessTime = "
                    + (simulationRealTime - arrivingTime));
            //this.hold(InputParameters.getLatestTimeOut()*5/getSimulationSpeed());///////////
            //this.shipContainers.clearNeedLeavingPort();
            } catch (RemoteException | SimRuntimeException exception)
        {
            Logger.severe(this, "process", exception);
        } 
        //结束所有进程时播放音乐;
        if(this.toString().equals("Ship(9) ")==true)
        {
            System.out.println(this.toString()+".Process已结束");
//            try{ 
//                String file="C:/Users/Administrator.USER-20151209UN/Desktop/Brave+Shine.mid";
//                Runtime.getRuntime().exec("cmd /c start "   +   file.replaceAll(" ", "\" \""));
//            } catch(FileNotFoundException e){ 
//                System.out.print("FileNotFoundException "); 
//            } catch(IOException e){ 
//                System.out.print("Ship.Process.RunTime.getRuntime().exec(***)有错误!"); 
//            }
        }else{
            System.out.println(this.toString()+".Process已结束");
        }
    }
    /**
     * 创建集卡 集港作业列表及对应时间;
     * @throws nl.tudelft.simulation.dsol.SimRuntimeException
     * @throws java.rmi.RemoteException
     */
    private void process_TruckGathering() throws SimRuntimeException, RemoteException, NullPointerException{
        double a = simulationRealTime;//开始集港时间;
        double c = a + (Math.abs(this.latestTimeIn-this.earliestTimeIn));//结束集港时间;
        double b = (c-a)*InputParameters.getTriangleDist_mode()+a;//顶点
        if(this.shipContainers.getLoadingContainerOutsideTEU() == 0){
            System.out.println("Ship.process_TruckGathering()");
            throw new UnsupportedOperationException("!!!Error:Ship.process_TruckGathering()!!!");
        }
        double num = Math.ceil(this.shipContainers.getLoadingContainerOutsideTEU()/2);//集港的所有集卡总数;
        System.out.println(this.toString()+"GatheringTruckNum:"+num);
        double time[] = new double[(int)num];
        for(int i = 0;i<time.length;i++){
            time[i] = (int)InputParameters.getRandomTriangle(StaticName.TRIANGULAR, a, b, c);
        }
        this.truckGatheringPortTimeList = new double[(int)num];
        System.arraycopy(QuickSort.sort(time), 0, this.truckGatheringPortTimeList, 
                0, this.truckGatheringPortTimeList.length);
        //列表生成;打印列表;
        outputSingleParameters("TruckGatheringList/"+this.toString(), this.truckGatheringPortTimeList);
        //生成集港作业;
        for(int i = 0;i<num;i++){
            if(this.truckGatheringPortTimeList[i]==simulationRealTime){
                //生成该次集港作业;
                double yardNum = getRandom(0,InputParameters.getTotalYardNum()-1);
                this.port.getYards()[(int)yardNum].addOneTruckGatheringTask(this.mySelf);
            }else if(this.truckGatheringPortTimeList[i] > simulationRealTime){
                while(this.truckGatheringPortTimeList[i]>simulationRealTime){
                    this.hold(1.0/getSimulationSpeed());
                }
                //生成该次集港作业;
                double yardNum = getRandom(0,InputParameters.getTotalYardNum()-1);
                this.port.getYards()[(int)yardNum].addOneTruckGatheringTask(this.mySelf);
            }else{
                double yardNum = getRandom(0,InputParameters.getTotalYardNum()-1);
                this.port.getYards()[(int)yardNum].addOneTruckGatheringTask(this.mySelf);
            }
            while (i != num - 1 && this.truckGatheringPortTimeList[i + 1] > simulationRealTime) {
                this.hold(1.0/getSimulationSpeed());
            }
        }
        if(this.getShipContainers().getandSetOnTruck_Max2TEUOutsideContainers() == null){
            System.out.println("-/-/-"+this.toString()+"所有集港作业生成完毕"+simulationRealTime+"-/-/-");
        }else{
            System.out.println("-/-/-"+this.toString()+".process_truckGathering()错误！！"+simulationRealTime+"-/-/-");
            throw new UnsupportedOperationException("Error:ship.process_truckGathering()错误"); 
        }
    }
    
    ///////目前就两个泊位 用这个就可以;
    private boolean assignBerth() throws RemoteException, SimRuntimeException{
        //目前就两个泊位，就都按一个船靠一个泊位来;
        //泊位长度改为可变;方便船舶靠泊时计算靠泊位置;
        if (this.getPort().getBigBerthResource().getAvailableCapacity() >= 1) {
            if(this.findandSetOneBerth("Big", 1)==true){
                //满足长度要求;
                this.getPort().getBigBerthResource().requestCapacity(1, this);
//System.out.println(this + "泊位分配成功：" + "ShipType:" + this.getShipType()+ " " + "BerthNum:" + this.assignedberth.length);
                return true;
            }else{
                return false;
            }
        }
        return false;
    }
    
    
    /**
     * 可靠泊多个泊位时的分配函数,依据船型指配合适泊位及数量;
     * @return boolean 若分配成功，请求资源成功，则返回true;
     * @throws java.rmi.RemoteException
     * @throws nl.tudelft.simulation.dsol.SimRuntimeException
     */
    private boolean assignBerths() throws RemoteException, SimRuntimeException{
        //目前就两个泊位，就不用弄着部分了。都按一个船靠一个泊位来弄;
        //根据船型来判断分配泊位;
        switch(Integer.parseInt(this.getShipType())){
            //大型船舶
            case 1:
                if(this.getPort().getBigBerthResource().getAvailableCapacity() >= 1){
                    this.getPort().getBigBerthResource().requestCapacity(1,this);
                    this.setBerth("Big",1);
                }else if(this.getPort().getMiddleBerthResource() != null &&
                        this.getPort().getMiddleBerthResource().getAvailableCapacity() >= 2){
                    this.getPort().getMiddleBerthResource().requestCapacity(2, this);
                    this.setBerth("Middle",2);
                }else if(this.getPort().getSmallBerthResource() != null &&
                        this.getPort().getSmallBerthResource().getAvailableCapacity() >= 3){
                    this.getPort().getSmallBerthResource().requestCapacity(3, this);
                    this.setBerth("Small",3);
                }else
                    return false;
                break;
            //中型船舶;
            case 2:
                if(this.getPort().getMiddleBerthResource() != null &&
                        this.getPort().getMiddleBerthResource().getAvailableCapacity() >= 1){
                    this.getPort().getMiddleBerthResource().requestCapacity(1,this);
                    this.setBerth("Middle",1);
                }else if(this.getPort().getSmallBerthResource() != null &&
                        this.getPort().getSmallBerthResource().getAvailableCapacity() >= 2){
                    this.getPort().getSmallBerthResource().requestCapacity(2, this);
                    this.setBerth("Small",2);
                    //由于此处设置的中型，小型泊位均小于2
                    //故只要有资源，泊位一定是挨着的,不需要再对位置进行判断;
                }else if(this.getPort().getBigBerthResource().getAvailableCapacity() >= 1){
                    this.getPort().getBigBerthResource().requestCapacity(1, this);
                    this.setBerth("Big",1);
                }else
                    return false;
                break;
            //小型船舶;
            case 3:
                if(this.getPort().getSmallBerthResource() != null &&
                        this.getPort().getSmallBerthResource().getAvailableCapacity() >= 1){
                    this.getPort().getSmallBerthResource().requestCapacity(1,this);
                    this.setBerth("Small",1);
                }else if(this.getPort().getMiddleBerthResource() != null &&
                        this.getPort().getMiddleBerthResource().getAvailableCapacity() >= 1){
                    this.getPort().getMiddleBerthResource().requestCapacity(1, this);
                    this.setBerth("Middle",1);
                }else
                    return false;
                break;
        }
        //
        System.out.println(this+"泊位分配成功："+"ShipType:"+this.getShipType()+
                " "+"BerthNum:"+this.assignedberth.length);
        for (Berth assignedberth1 : this.assignedberth) {
            System.out.println("Berth:" + assignedberth1.getName() + " BerthType:" + assignedberth1.getBerthType());
        }
        //
        return true;
    }
    //release Berth Resource;
    private boolean looseBerth() throws RemoteException{
        for (Berth assignedberth1 : this.assignedberth) {
            switch (assignedberth1.getBerthType()) {
                case 1:
                    this.getPort().getBigBerthResource().releaseCapacity(1);
                    break;
                case 2:
                    this.getPort().getMiddleBerthResource().releaseCapacity(1);
                    break;
                case 3:
                    this.getPort().getSmallBerthResource().releaseCapacity(1);
                    break;
            }
        }
        int num = this.assignedberth.length;
        for(int i = 0;i<num;i++){
            this.assignedberth[i].setBerthState(4);//船舶已经驶离，允许其他船舶靠泊;
        }
        System.out.println("***"+this+"泊位释放成功："+"ShipType:"+this.getShipType()+"   BerthNum:"+this.assignedberth.length+
                "   BerthName:"+this.assignedberth[0].getName()+"...");
        return true;
    }
    //关联Ship和Berth(s)，改变相应状态参数;
    private boolean setBerth(String Type,int Num){
        int BerthType = 0;
        switch(Type){
            case "Big":
                BerthType = 1;break;
            case "Middle":
                BerthType = 2;break;
            case "Small":
                BerthType = 3;break;
        }
        int nowNum = 0;
        this.assignedberth = new Berth[Num];
        for (Berth berth : this.getPort().getBerths()) {
            if (berth.getBerthType() == BerthType && berth.getBerthState() == 4 && nowNum < Num) {
                berth.setBerthState(-1); 
                berth.setShip(this); 
                this.assignedberth[nowNum] = berth;
                nowNum++;
                if(nowNum == Num){
                    break;
                }
            }    
        }
        return true;
    }
    /**
     * 岸桥调度,依据船型，泊位指配合适岸桥及数量;
     * @return 若分配不成功则返回false---需要wait再次调配
     * @throws java.rmi.RemoteException
     * @throws nl.tudelft.simulation.dsol.SimRuntimeException
     */
    private boolean assignQC() throws RemoteException, SimRuntimeException{
        //-------------------------搜寻所在泊位上是否有连续的空闲岸桥;-------------------------------------
        double thisNumber1 = this.assignedberth[0].getNumber();//该berth在berth[]中的编号，也表示了位置关系;
        double thisNumber2 = this.assignedberth[this.assignedberth.length-1].getNumber();//该berth在berth[]中的编号;
        if(thisNumber1 == 0){
            //在最左边的泊位,则搜寻正在工作的最小岸桥编号即可;
            System.out.print("****"+this.description+" 在最左侧泊位：Berth["+thisNumber1+"]");
            
            if(this.getPort().getFirstWorkQC() == null){//所有岸桥均空闲;
                if(this.maxQCLine <= this.getPort().getQuayCranes().length)
                    this.assignedQC = new QuayCrane[this.maxQCLine];
                else
                    this.assignedQC = new QuayCrane[this.getPort().getQuayCranes().length];
                this.getPort().getQuayCraneResource().requestCapacity(this.getAssignedQC().length, this);//请求QC资源;
                for(int i = 0;i<this.getAssignedQC().length;i++){
                    this.assignedQC[i] = this.getPort().getQuayCranes()[i];
                    this.getAssignedQC()[i].setCraneState(1);
                    this.getAssignedQC()[i].setServiceShip(this);
                }
                System.out.println("****"+this.description+"分配成功：泊位数："+this.getAssignedQC().length);
                return true;
            }else if( this.getPort().getFirstWorkQC().getNumber()>= this.minQCLine){
                int num = (int) this.getPort().getFirstWorkQC().getNumber();
                num = Math.min(num, this.maxQCLine);
                this.assignedQC = new QuayCrane[num];
                this.getPort().getQuayCraneResource().requestCapacity(this.getAssignedQC().length, this);//请求QC资源;
                for(int i = 0;i<this.getAssignedQC().length;i++){
                    this.assignedQC[i] = this.getPort().getQuayCranes()[i];
                    this.getAssignedQC()[i].setCraneState(1);
                    this.getAssignedQC()[i].setServiceShip(this);
                }
                System.out.println("****"+this.description+"分配成功：泊位数："+this.getAssignedQC().length);
                return true;
            }else{
                System.out.println("****"+this.description+"分配不成功，需等待 or 全局配置;");
                //"分配不成功，需要等待 or 全局配置";
            }     
        }else if(thisNumber2 == this.getPort().getBerths().length-1){
            //在最右边的泊位,则只看thisNumber1左侧泊位的最大岸桥编号; 
            System.out.print("****"+this.description+" 在最右侧泊位：Berth["+thisNumber2+"]");
            int num = this.getPort().getQuayCranes().length;
            if(this.getPort().getLastWorkQC() != null){
                num = (int)(this.getPort().getQuayCranes().length-this.getPort().getLastWorkQC().getNumber()-1);
            }
            if(num >= this.minQCLine){
                num = Math.min(num, this.maxQCLine);
                this.assignedQC = new QuayCrane[num];
                this.getPort().getQuayCraneResource().requestCapacity(this.getAssignedQC().length, this);//请求QC资源;
                int ww = this.getPort().getQuayCranes().length-1;
                for(int i = 0;i<this.getAssignedQC().length;i++){
                    this.assignedQC[i] = this.getPort().getQuayCranes()[ww-i];
                    this.assignedQC[i].setCraneState(1);
                    this.assignedQC[i].setServiceShip(this);
                } 
                System.out.println("****"+this.description+"分配成功：泊位数："+this.getAssignedQC().length);
                return true;
            }else{
                System.out.println("****"+this.description+"分配不成功，需等待 or 全局配置;");
                //"分配不成功，需等待 or 全局配置;"
            }
        }else{
            System.out.println("****"+this.description+"两边都要看,相减;");
            //两边都要看,相减;
            double Num1 = -1;//初始化;表示左侧无岸桥正在工作;
            double Num2 = this.getPort().getQuayCranes().length;//初始化;表示右侧无岸桥正在工作;
            if(this.getPort().getLastWorkQCInBerth(0, thisNumber1-1) != null)
                Num1 = this.getPort().getLastWorkQCInBerth(0, thisNumber1-1).getNumber();//左侧最后一个工作岸桥的编号;
            if(this.getPort().getFirstWorkQCInBerth(thisNumber2+1, this.getPort().getBerths().length-1) != null)
                Num2 = this.getPort().getFirstWorkQCInBerth(thisNumber2+1, this.getPort().getBerths().length-1).getNumber();//右侧第一个工作岸桥的编号;
            if(Num1 == -1){
                //不是左端部泊位，但是左侧岸桥均空闲;
                double minQCPerBerth = Math.floor(this.getPort().getQuayCranes().length/this.getPort().getBerths().length);////////有待商榷;
                Num1 = minQCPerBerth*(thisNumber1+1)-1;//左侧泊位预留的最后一个岸桥编号;
            }
            double num = Num2-Num1-1;//连续的空闲QC数量;
            if(num >= this.minQCLine){
                //有足够岸桥供分配;
                num = Math.min(num, this.maxQCLine);//分配的岸桥数量;
                this.assignedQC = new QuayCrane[(int)num];
                this.getPort().getQuayCraneResource().requestCapacity(this.getAssignedQC().length, this);//请求QC资源;
                //如何确定分配的岸桥编号;
                for(int i = 0;i<this.getAssignedQC().length;i++){
                    this.assignedQC[i] = this.getPort().getQuayCranes()[(int)Num1+1+i];
                    this.getAssignedQC()[i].setCraneState(1);
                    this.getAssignedQC()[i].setServiceShip(this);
                } 
                System.out.println("****"+this.description+"分配成功：泊位数："+this.getAssignedQC().length);
                return true;
            }   
        }
        return false;
        
        /**
         * 
        System.out.println("****"+this.description+"无法直接获得连续的空闲岸桥时，则需要移动其他船舶的服务岸桥以完成指配");
        //---------在无法直接获得连续的空闲岸桥时，则需要移动其他船舶的服务岸桥以完成指配;--------------
        //找出港内所有泊位停泊的所有需要装卸的船;
        Queue<Ship> shipsOnBerth = new ArrayDeque<>();//所有在泊船只;
        System.out.println("****"+this.description+"分配QC：Queue successful");/////
        int shipNumOnBerth = 0;//在泊数量;
        for (Berth berth : this.port.getBerths()) {//这样得到的船只本就是按顺序排列的,不需要再在后面做排序;
            if (berth.getBerthState() == 0 || berth.getBerthState() == 1) {//船舶在泊，等待装卸;
                if (shipsOnBerth.contains(berth.getShip()) == false) {
                    shipNumOnBerth++;
                    shipsOnBerth.offer(berth.getShip());
                }
            }
        }
        System.out.println("****"+this.description+"分配QC：shipsOnBerth:"+shipNumOnBerth);/////
        //确保最小作业线数，得到剩余装卸桥数量;
        double[] shipMinLine = new double[shipNumOnBerth];
        double[] shipMaxLine = new double[shipNumOnBerth];
        int i = 0;
        double allMinLineQC = 0;
        double allMaxLineQC = 0;
        double allLineQC = 0;
        for(Ship ship:shipsOnBerth){
            shipMinLine[i] = ship.minQCLine;
            shipMaxLine[i] = ship.maxQCLine;
            i++;
            allMinLineQC += ship.minQCLine;
            allMaxLineQC += ship.maxQCLine;
        }
        double leftQC = this.port.getQuayCranes().length-allMinLineQC;//剩余装卸桥数量;
        //对所有有船泊位按照最大作业线分配装卸桥; 
        if(leftQC < 0){
           return false;//分配失败;hold之后再分配;
       }
        allLineQC = allMinLineQC;
        double[] addLine = new double[shipNumOnBerth];
        double[] shipLine = new double[shipNumOnBerth];
        double leftQCbefore = leftQC;
       for(i=0;i<addLine.length;i++){
            addLine[i] = leftQCbefore*shipMaxLine[i]/allMaxLineQC;//按照最大作业线从大到小分配;
            addLine[i] = Math.floor(addLine[i]);//舍掉小数取整;
            if(addLine[i]+shipMinLine[i]>shipMaxLine[i]){
                addLine[i] = shipMaxLine[i]-shipMinLine[i];
            }
            shipLine[i] = shipMinLine[i]+addLine[i];
            leftQC -= addLine[i];
        }
        leftQCbefore = leftQC;
        //添加余下装卸桥;
        for(i = 0;i<addLine.length;i++){
            addLine[i] = leftQCbefore*(shipMaxLine[i]-shipLine[i])/(allMaxLineQC-allLineQC);
            addLine[i] = Math.ceil(addLine[i]);//进位取整;
            if(addLine[i]+shipLine[i]>shipMaxLine[i])
            {
                addLine[i] = shipMaxLine[i] - shipLine[i];
            }
            shipLine[i] += addLine[i];
            leftQC -= addLine[i];
            if(leftQC == 0)
                break;
            if(leftQC <0){
                shipLine[i] += leftQC;
                leftQC = 0;
                break;
            }
        }//分配完毕;shipLine[i],shipsOnBerth,leftQC
        //依据船舶位置分配具体编号的岸桥;
        //释放所有岸桥资源，重分配;
        double x1 = this.port.getQuayCraneResource().getCapacity();
        double x2 = this.port.getQuayCraneResource().getAvailableCapacity();
        this.port.getQuayCraneResource().releaseCapacity(x1-x2);
        i = 0;
        this.port.getQuayCraneResource().requestCapacity(this.port.getQuayCranes().length-leftQC, this);//请求QC资源;///////
        this.port.StartAssignAllQC();//开始分配，重置所有QC的boolean haveAssigned
        for(Ship ship:shipsOnBerth){
            ship.assignedQC = new QuayCrane[(int)shipLine[i]];
            for(int j = 0;j<shipLine[i];j++){
                //ship中关联QC
                ship.assignedQC[j] = this.port.getFisrtFreeQC();//get沿码头前沿坐标从小到大的第一个空闲cranes;
                ship.assignedQC[j].setCraneState(1);//分配成功，标记QC state;
                ship.assignedQC[j].setHaveAssigned(true);
                //QC中关联serviceShip; 
                ship.assignedQC[j].setServiceShip(ship);
            }
            i++;
        }
        return true;//分配成功;
        * 
        * */
        
    }
    //release QC Resource and Chaneg the parameters of QCs;
    private void looseQC() throws RemoteException{
        for(QuayCrane QC:this.getAssignedQC()){
            this.getPort().getQuayCraneResource().releaseCapacity(1);
            QC.setShipLeave();
            System.out.println("***"+this+"QC释放成功："+"CraneType:"+QC.getLocation()+"   BerthNum:"+this.assignedberth.length+
                "   BerthName:"+this.assignedberth[0].getName()+"...");
        }    
    }
    /**
     * @see nl.tudelft.simulation.dsol.formalisms.ResourceRequestorInterface
     *      #receiveRequestedResource(double,
     *      nl.tudelft.simulation.dsol.formalisms.Resource)
     */
    @Override
    public void receiveRequestedResource(final double requestedCapacity,
            final Resource resource)
    {
        this.resume();
    }
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return this.description;
    }

    /**
     * @return the shipContainers
     */
    public ShipContainers getShipContainers() {
        return shipContainers;
    }
    /**
     * 输出仿真获得的各个参数;
     */
    private void outPut(){
        OutputParameters.outputShip(this);
        Queue<String> que = new ArrayDeque<>();
        que.add("---------------输入参数---------------");
        que.add("shipType\t"+this.getShipType());
        que.add("DWT\t"+this.getDWT());
        que.add("DWTLevel\t"+this.DWTLevel);
        que.add("maxQCLine\t"+Integer.toString(this.maxQCLine));
        que.add("minQCLine\t"+Integer.toString(this.minQCLine)); 
        que.add("arrivingTime\t"+this.arrivingTime);
        que.add("total_unload_TEU\t"+this.shipContainers.getTotal_unload_TEU());
        que.add("total_load_TEU\t"+this.shipContainers.getTotal_load_TEU());
        que.add("---------------输出参数-时间---------------");
        que.add("ATime\t"+this.getOutsideAnchorageTime);
        que.add("CTime\t"+this.getChannelTime);
        que.add("BTime\t"+this.startberthingTime);
        que.add("QCFinishToMoveTime\t"+this.quayCraneFinishToMoveTime);
        que.add("SUTime\t"+this.startUnloadingTime);
        que.add("EUTime\t"+this.endUnloadingTime);
        que.add("SLTime\t"+this.startLoadingTime);
        que.add("ELTime\t"+this.endLoadingTime);
        que.add("UBTime\t"+this.startUnberthingTime);
        que.add("lTime\t"+this.endUnberthingTime);
        que.add("---------------输出参数-配置---------------");
        String BerthName="";
        for (String assignedBerthName1 : this.assignedBerthName) {
            BerthName += assignedBerthName1+" ";
        }
        que.add("assignedBerthName:      "+BerthName);
        String QCName="";
        for (String assignedQCName1 : this.assignedQCName) {
            QCName += assignedQCName1+" ";
        }
        que.add("assignedQCName:         "+QCName);
        
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+"Ship/ship"+this.getNumber()+"/output.txt");
        if(file.exists() != true){
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file,true);
            writer = new BufferedWriter(fw);
            while(iterator.hasNext()){
                writer.write(iterator.next());
                writer.newLine();//换行
            }
            writer.flush();
        } catch (FileNotFoundException e) {
        }catch (IOException e) {
        }finally{
            try {
                writer.close();
                fw.close();
            } catch (IOException e) {
            }
        }
    }

    /**
     * @return the port
     */
    public Port getPort() {
        return port;
    }

    /**
     * @return the assignedQC
     */
    public QuayCrane[] getAssignedQC() {
        return assignedQC;
    }

    /**
     * @return the berthingLocation
     */
    public Point2D getBerthingLocation() {
        return berthingLocation;
    }

    /**
     * 设置靠泊位置;
     * @param berthingLocation the berthingLocation to set
     */
    private void setBerthingLocation(Point2D berthingLocation) {
        this.berthingLocation = new Point2D.Double(0, 0);
        this.berthingLocation.setLocation(berthingLocation);
    }
    /**
     * 设置thia.assignedBerth[]各个serviceLocation
     */
    private void setAllQCsServiceLocation() {
        double start_Y_OfBerth;
        double end_Y_OfBerth;
        int berthNum = this.assignedberth.length;
        start_Y_OfBerth = this.assignedberth[0].getLocation().getY()-0.5*
                this.assignedberth[0].getLength();
        end_Y_OfBerth = this.assignedberth[berthNum-1].getLocation().getY()+0.5*
                this.assignedberth[berthNum-1].getLength();
        double berthingX,berthingY;
        berthingX = 0;
        berthingY = (start_Y_OfBerth + end_Y_OfBerth)/2;
        this.setBerthingLocation(new Point2D.Double(berthingX, berthingY));
        //
        double startYofShip = this.getBerthingLocation().getY() - 0.5*this.getShipLength();
        double endYofShip = this.getBerthingLocation().getY() + 0.5*this.getShipLength();
//        System.out.print("startYofShip: ");
//        System.out.println(startYofShip);
//        System.out.print("endYofShip: ");
//        System.out.println(endYofShip);
        int QCNum = this.assignedQC.length;
        double length = endYofShip-startYofShip;
        double delta = 0;
        if(length <= 0){
            throw new UnsupportedOperationException("Ship.setAllQCsServiceLocation()!!!!!!wrong.");  
        }else{
            delta = length/QCNum;
        }
        //
        if (QCNum == 1) {
            this.assignedQC[0].setServiceLocationY(startYofShip + 0.5 * delta);
        } else if (this.assignedQC[0].getNumber() < this.assignedQC[this.assignedQC.length - 1].getNumber()) {
            this.assignedQC[0].setServiceLocationY(startYofShip + 0.5 * delta);
            for (int i = 1; i < QCNum; i++) {
                this.assignedQC[i].setServiceLocationY(this.assignedQC[i - 1].
                        getServiceLocation().getY() + delta);
            }//如果其他旁边的空闲岸桥挡住了位子，则需要移动;
            this.assignedQC[0].moveLeftQC();
            this.assignedQC[this.assignedQC.length - 1].moveRightQC();
        } else {
            this.assignedQC[QCNum-1].setServiceLocationY(startYofShip + 0.5 * delta);
            for (int i = QCNum-1; i > 0; i--) {
                this.assignedQC[i-1].setServiceLocationY(this.assignedQC[i].
                        getServiceLocation().getY() + delta);
            }//如果其他旁边的空闲岸桥挡住了位子，则需要移动;
            this.assignedQC[this.assignedQC.length - 1].moveLeftQC();
            this.assignedQC[0].moveRightQC();
        }
//System.out.println("Ship.setAllQCsServiceLocation()成功！！"); 
    }
    /**
     * @return boolean 是否为之服务的所有QC均完成针对该船舶的装卸工作;
     */
    public boolean allQCFree(){
        for(QuayCrane QC1:this.getAssignedQC()){
            if(QC1.isGantryTrolleyFree() == false || QC1.isMainTrolleyFree() == false){
                return false;
            }
        }
        return true;
    }
    /**
     * @return the simulator
     */
    public DEVSSimulator getSimulator() {
        return (DEVSSimulator) super.simulator;
    }
    /**
     * 在QCWork中使用,用于获取QC移动至装卸位置所需要的double Time
     * @param quayCraneFinishToMoveTime the quayCraneBeginToMoveTime to set
     */
    public void setQuayCraneFinishToMoveTime(double quayCraneFinishToMoveTime) {
        this.quayCraneFinishToMoveTime = quayCraneFinishToMoveTime;
    }
    /**
     * @param ftSize
     * @param containerState
     * @return container or throw exception
     */
    public Container setLoadingContainersOnBlock(String ftSize,String containerState){
        return this.port.setLoadingContainersOnBlock(this.mySelf,ftSize,containerState);
    }
    /**
     * @return the earliestTimeIn
     */
    public double getEarliestTimeIn() {
        return earliestTimeIn;
    }
    /**
     * @return the latestTimeIn
     */
    public double getLatestTimeIn() {
        return latestTimeIn;
    }
    /**
     * @return the shipType
     */
    public String getShipType() {
        return shipType;
    }

    /**
     * @return the shipLength
     */
    public double getShipLength() {
        return shipLength;
    }

    public int getNumber() {
        int num; 
        String str = this.toString();
        str=str.trim();
        String str2 = "";
        for(int i=0;i<str.length();i++){
            if(str.charAt(i)>=48 && str.charAt(i)<=57){
                str2+=str.charAt(i);
            }
        }
        num = Integer.parseInt(str2);
        return num;
    }

    //设置该船舶载重吨对应的岸桥数量;
    private void setMaxMinQCLine() {
        if(this.getDWT()>=4501 && this.getDWT()<=27500){//5000-30000
            this.minQCLine = 1;
            this.maxQCLine = 2;
        }else if(this.getDWT()>=27501 && this.getDWT()<=45000){//3W
            this.minQCLine = 2;
            this.maxQCLine = 3;
        }else if(this.getDWT()>=45001 && this.getDWT()<=65000){//5W
            this.minQCLine = 3;
            this.maxQCLine = 4;
        }else if(this.getDWT()>=65001 && this.getDWT()<=85000){//7W
            this.minQCLine = 3;
            this.maxQCLine = 4;
        }else if(this.getDWT()>=85001 && this.getDWT()<=115000){//10W
            this.minQCLine = 3;
            this.maxQCLine = Math.max(this.minQCLine,Math.max(port.getQuayCranes().length/port.getBerths().length, 4));
        }else if(this.getDWT()>=115001){//12W&up
            this.minQCLine = 3;
            this.maxQCLine = Math.max(this.minQCLine,Math.max(port.getQuayCranes().length/port.getBerths().length, 5));
        }else{
            System.out.println("Error：规范中没有该范围DWT的QCLine-DWT:"+this.getDWT());
            throw new UnsupportedOperationException("!!!Error:Ship.setMaxMinQCLine()!!!");   
        }
    }

    /**
     * @return the shipWidth
     */
    public double getShipWidth() {
        return shipWidth;
    }

    /**
     * @return the DWTLevel
     */
    public int getDWTLevel() {
        return DWTLevel;
    }

    /**
     * @return the minTEU
     */
    public int getMinTEU() {
        return minTEU;
    }

    /**
     * @return the maxTEU
     */
    public int getMaxTEU() {
        return maxTEU;
    }

    /**
     * @return the DWT
     */
    public int getDWT() {
        return DWT;
    }
//检查空闲岸线长度是否符合要求并set后返回true false
    private boolean findandSetOneBerth(String Type, int Num) {
        int BerthType = 0;
        switch(Type){
            case "Big":
                BerthType = 1;break;
            case "Middle":
                BerthType = 2;break;
            case "Small":
                BerthType = 3;break;
        }
        this.assignedberth = new Berth[Num];
        double length = 0;
        for(Berth berth : this.getPort().getBerths()) {
            if (berth.getBerthState() != 4) {
                if(berth.getShip() != null){
                    length+=berth.getShip().shipLength+berth.getShip().shipD;
                }
            }    
        }
        for (Berth berth : this.getPort().getBerths()) {
            if (berth.getBerthType() == BerthType && berth.getBerthState() == 4 && 
                    port.getTotalLength()-length-2*this.shipD>=this.shipLength) {
                berth.setBerthState(-1); 
                if(berth.getNumber() == 0){
                    berth.setLocationY(InputParameters.getpBerth1_0().getY()-InputParameters.getBerthLength(getBigBerthDWT())/2);
                    berth.setLength(Math.min(port.getTotalLength()-length,this.shipLength+this.shipD*2));//改长度;
                    berth.setLocationY(berth.getLocation().getY()+0.5*berth.getLength());//改中心位置;
                }else if(berth.getNumber() == 1){
                    berth.setLocationY(InputParameters.getpBerth1_0().getY()+3*InputParameters.getBerthLength(getBigBerthDWT())/2);
                    berth.setLength(Math.min(port.getTotalLength()-length,this.shipLength+this.shipD*2));//改长度;
                    berth.setLocationY(berth.getLocation().getY()-0.5*berth.getLength());//改中心位置;
                }
                berth.setShip(this); 
                this.assignedberth[0] = berth;
                return true;
            }    
        }
        return false;
    }

}
