
package CYT_model;

import apron.QuayCrane;
import apron.Berth;
import Vehicle.AGV;
import storageblock.YardCrane;
import roadnet.FunctionArea;
import roadnet.AGVBufferArea;
import roadnet.RoadNet;
import CYT_model.Point2D;
import java.rmi.RemoteException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tudelft.simulation.dsol.formalisms.Resource;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import parameter.InputParameters;
import static parameter.InputParameters.getAnchorageNum;
import static parameter.InputParameters.getTotalBlockNumPerYard;
import static parameter.InputParameters.getTotalYardNum;
import static parameter.InputParameters.getYCNumPerBlock;
import parameter.StaticName;
import storageblock.Yard;
import ship.Ship;
import storageblock.Block;

/**
 * Class Port港区
 * 基于DSOL
 * @author YutingChen, yuting.chen17@imperial.ac.uk
 * Dalian University of Technology
 */
public final class Port
{
    private Port myself; 
    private final RoadNet roadNetWork;
    private Yard[] yards = new Yard[InputParameters.getTotalYardNum()];
    
    // @function 初始化港区内固定式永久实体，移动式永久实体;
    // Resource;
    //----------------------------固定式永久实体---------------------------------
    // 锚地;
    private Resource outsideanchorage = null;
    // 航道;
    private Resource channel = null;
    // 泊位;
    private double totalLength;//岸线总长;
    private Berth[] berths = null;
    private Resource bigBerthResource = null;
    private final Resource middleBerthResource = null;
    private final Resource smallBerthResource = null;
    //----------------------------移动式永久实体---------------------------------
    
    private Resource quayCraneResource = null;//岸桥资源;

    private final int numOfAGVPerQC = InputParameters.getNumOfAGVPerQC();;
    private final String AGVType = InputParameters.getAGVType();
    private final AGV AGVs[] = new AGV[this.numOfAGVPerQC*InputParameters.getQuayCraneNum()];
    
    private QuayCrane[] quayCranes = null;//岸桥类;
    private YardCrane[] yardCranes = null;//场桥类;
    
    //通航条件;
    private final boolean navigationConditionOK;//通航条件;
    private final boolean berthingRequirement;//靠泊要求;
    
    //-------------------集港作业列表----------------//
    //所有集港作业都在ship到达前加入队列中;
    
    private final DEVSSimulatorInterface simulator;

    /**
     * constructs a new Port
     * @param simulator the simulator
     * @throws java.lang.CloneNotSupportedException
     */
    public Port(final DEVSSimulatorInterface simulator) throws CloneNotSupportedException
    {
        super();
        this.myself = this;
        this.simulator = simulator;
        ////////////////////////////////////////////////////////////////////////关于各参数的设置可以在txt或界面中解决？？
        //----------------------------固定式永久实体-----------------------------
        //锚地
        this.outsideanchorage = new Resource(simulator,"OutsideAnchorage",getAnchorageNum());//港外锚地;
        //航道
        this.channel = new Resource(simulator,"Channel",InputParameters.getChannelNum());//航道;
        //泊位
        //--------输入：大中小泊位数量,????位置????;
        int bigBerthNum = InputParameters.getBigBerthNum();//
        if (InputParameters.getMiddleBerthNum() != 0 && InputParameters.getSmallBerthNum() != 0) {
            //说明此处要改
            System.out.println("Port.port()BerthNum改！！");
            throw new UnsupportedOperationException("Error:Port.port()BerthNum");
        }
        int totalBerthNum = bigBerthNum;
        //赋值Berth[];
        this.berths = new Berth[totalBerthNum];
        //berth[]位置;
        Point2D[] berthLocat = new Point2D[totalBerthNum];//注意赋值是沿前沿坐标从小到大排列赋值;
        berthLocat[0] = new Point2D.Double(InputParameters.getpBerth1_0().getX(), InputParameters.getpBerth1_0().getY());///////////////////////////////坐标最小的一个泊位中点位置
        for(int i = 1;i<totalBerthNum;i++){
            berthLocat[i] = new Point2D.Double(0, 0);
        }
        //----------------------记得按位置来定义berth[]数组;----------------------------------------
        for(int i=0;i<bigBerthNum;i++){
            this.berths[i] = new Berth("BerthNo."+Integer.toString(i),1,berthLocat[i]);
        }
        totalLength = 0;
        //Berth位置;
        for(int i = 1;i<totalBerthNum;i++){
            berths[i].setLocation(new Point2D.Double(
                    0, berths[i-1].getLocation().getY()+berths[i-1].getLength()));
            this.totalLength += berths[i-1].getLength();
            if(i == totalBerthNum-1){
                this.totalLength += berths[i].getLength();//岸线总长;
            }
        }        
        //目前就2个大伯位!!
        this.bigBerthResource = new Resource(simulator,"Berth",bigBerthNum);//大型泊位;
//        this.middleBerthResource = new Resource(simulator,"Berth",middleBerthNum);//中型泊位;
//        this.smallBerthResource = new Resource(simulator,"Berth",smallBerthNum);//小型泊位;
        //----------------------------移动式永久实体-----------------------------
        //****岸桥****
        //-------------输入：岸桥数量,????位置????;
        int quayCraneNum = InputParameters.getQuayCraneNum();
        this.quayCranes = new QuayCrane[quayCraneNum];//
        //岸桥对应参数：
        ///------------输入参数数组;
        int QCtype = InputParameters.getQCtype();//双小车岸桥;
        String QCversion = InputParameters.getQCVersion();
        Point2D[] Ploc = new Point2D[quayCraneNum];
        double dQC = this.totalLength/quayCraneNum;
        Ploc[0] = new Point2D.Double(0,0+dQC/2);
        for(int i = 1;i<quayCraneNum;i++){
            Ploc[i] = new Point2D.Double(0,Ploc[i-1].getY()+dQC);//------------need to change--------------
            //沿前沿坐标从小到大排列赋值;
        }
        //赋值;
        for(int i=0;i<quayCraneNum;i++){
            this.quayCranes[i] = new QuayCrane("QCNo."+Integer.toString(i),QCtype,QCversion,Ploc[i],this);
        }
        this.quayCraneResource = new Resource(simulator,"QuayCrane",quayCraneNum);//创建岸桥资源;
        //****场桥****
        this.yardCranes = new YardCrane[getTotalYardNum()*getTotalBlockNumPerYard()*getYCNumPerBlock()];
        for(int i = 0;i<this.yardCranes.length;i++){
            this.yardCranes[i] = null;
        }
        
        //设置路网
        this.roadNetWork = InputParameters.creatRoadNet(this.myself);
        System.out.println(Arrays.toString(getRoadNetWork().route[0][2]));
        
        
        //--------------------------------通航条件-------------------------------
        this.navigationConditionOK = true;
        this.berthingRequirement = true; 
    }
    /**
     * @return the anchorage
     */
    public Resource getOutsideAnchorage() {
        return this.outsideanchorage;
    }
    /**
     * @return the navigationCondition
     */
    public boolean isNavigationConditionOK() {
        return navigationConditionOK;
    }
    /**
     * @return the channel
     */
    public Resource getChannel() {
        return channel;
    }


    /**
     * @return the berthingRequirement
     */
    public boolean isBerthingRequirement() {
        return berthingRequirement;
    }

    /**
     * @return the bigBerthResource
     */
    public Resource getBigBerthResource() {
        return bigBerthResource;
    }

    /**
     * @return the middleBerthResource
     */
    public Resource getMiddleBerthResource() {
        return middleBerthResource;
    }

    /**
     * @return the smallBerthResource
     */
    public Resource getSmallBerthResource() {
        return smallBerthResource;
    }

    /**
     * @return the berths
     */
    public Berth[] getBerths() {
        return berths;
    }

    /**
     * @param berths the berths to set
     */
    public void setBerths(Berth[] berths) {
        this.berths = berths;
    }

    /**
     * @param bigBerthResource the bigBerthResource to set
     */
    public void setBigBerthResource(Resource bigBerthResource) {
        this.bigBerthResource = bigBerthResource;
    }

    /**
     * @return the quayCranes
     */
    public QuayCrane[] getQuayCranes() {
        return quayCranes;
    }
    /*
    * 清空岸桥重分配信息;
    */
    public void StartAssignAllQC(){
        for(QuayCrane QC:this.quayCranes){
            if (QC.isHaveAssigned() == true)
                QC.setHaveAssigned(false);    
        }
    }
    /**
     * @return 沿码头前沿坐标从小到大第一个正在作业的cranes;
     */
    public QuayCrane getFirstWorkQC(){
        for(QuayCrane QC:this.quayCranes){
            if (QC.getCraneState() == 1)
                return QC;
        }
        return null;
    }
    /**
     * @return 沿码头前沿坐标从小到大最后一个正在作业的cranes;
     */
    public QuayCrane getLastWorkQC(){
        int i = this.quayCranes.length-1;
        for(;i>=0;i--)
        {
            if (this.quayCranes[i].getCraneState() == 1)
                return this.quayCranes[i];
        }
        return null;
    }
  
    /**
     * @param num1
     * @param num2
     * @return 输入泊位中从小到大第一个正在作业的cranes;
     * 返回QuayCrane Or Null;
     */
    public QuayCrane getFirstWorkQCInBerth(double num1,double num2){
        for(int i = (int) num1;i <= num2;i++){
            if(this.getBerths()[i].getBerthState() == 1){
                return this.getBerths()[i].getShip().getAssignedQC()[0];//最小编号的QC;
            }
        }
        System.out.println("!!!:没有正在作业的QC!!!!getFirstWorkQCInBerth()!!!");
        return null;//没有正在作业的QC;
    }
    /**
     * @param num1(num1<num2)
     * @param num2
     * @return 输入泊位中从小到大最后一个正在作业的cranes;
     * 返回QuayCrane Or Null;
     */
    public QuayCrane getLastWorkQCInBerth(double num1,double num2){
        for(int i = (int) num2;i >= num1;i--){
            if(this.getBerths()[i].getBerthState() == 1){
                return this.getBerths()[i].getShip().getAssignedQC()[this.getBerths()[i].getShip().getAssignedQC().length-1];//最大编号的QC;
            }
        }
        System.out.println("!!!没有正在作业的QC!!!  QCgetLastWorkQCInBerth()!!!");
        return null;//没有正在作业的QC;
    }
    /**
     * 区分！！未被重分配 VS 未被分配作业
     * @return 沿码头前沿坐标从小到大的第一个还没有被重新分配的cranes;
     * 该crane的作业状态可能是正在作业,也可能是空闲;
     */
    public QuayCrane getFisrtFreeQC(){
        for(QuayCrane QC:this.quayCranes){
            if (QC.isHaveAssigned() == false)
                return QC;
        }
        System.out.println("cant't find any free QuayCrane");
        return null;
    }           
    /**
     * @param quayCranes the quayCranes to set
     */
    public void setQuayCranes(QuayCrane[] quayCranes) {
        this.quayCranes = quayCranes;
    }
    /**
     * @return the quayCraneResource
     */
    public Resource getQuayCraneResource() {
        return quayCraneResource;
    }
    /**
     * @param quayCraneResource the quayCraneResource to set
     */
    public void setQuayCraneResource(Resource quayCraneResource) {
        this.quayCraneResource = quayCraneResource;
    }
    /**
     * @return double AGV的总数;
     */
    public double getAGVNum() {
        return this.numOfAGVPerQC*this.quayCranes.length;
    }
    /**
     * @return the numOfAGVPerQC
     */
    public double getNumOfAGVPerQC() {
        return this.numOfAGVPerQC;
    }
    /**
     * @return the AGVType
     */
    public String getAGVType() {
        return AGVType;
    }

    public void addSeriveAGV(AGV agv) {
        for(int i = 0;i<this.getAGVs().length;i++){
            if(this.getAGVs()[i] == null){
                this.AGVs[i] = agv;
                break;
            }
        }
    }

    /**
     * @return the AGVs
     */
    public AGV[] getAGVs() {
        return AGVs;
    }

    /**
     * @return the yardCranes
     */
    public YardCrane[] getYardCranes() {
        return yardCranes;
    }

    /**
     * @param yardCrane the yardCrane to set
     */
    public void setYardCranes(YardCrane yardCrane) {
        for(int i = 0;i<this.yardCranes.length;i++){
            if(this.yardCranes[i] == null){
                this.yardCranes[i] = yardCrane;
                break;
            }
        }
    }

    /**
     * @return the roadNetWork
     */
    public RoadNet getRoadNetWork(){
        if(this.roadNetWork != null){
            return this.roadNetWork;
        }else{
            System.out.println("!!!!getRoadNetWork null!!!!");
            return this.roadNetWork;
        }
    }
    /**
     * @return Yard[]
     */
    public Yard[] getYards(){
        return this.yards;
    }
    /**
     * 判断这两个箱是不是相邻的20ft
     * @param containers
     * @param num
     * @return true false
     */
    public boolean isNeighbour20FT(Container[] containers,int num){
        if(Math.abs(containers[1].getPresentSlot().getBayNum()-containers[0].getPresentSlot().getBayNum()) == 2){
            //相邻bay位;
            if(containers[0].getPresentSlot().getBayNum()%2 == 1 &&
                    containers[0].getPresentSlot().getRowNum() == containers[1].getPresentSlot().getRowNum() &&
                    containers[0].getPresentSlot().getYardNum() == containers[1].getPresentSlot().getYardNum()){
                return true;
            }
        }
        return false;
    }
    /**
     * @param containers
     * @param num
     * @return 几个集装箱的中心position
     */
    public Point2D getCentralSlotPosition(Container[] containers,int num){
        if(num != 2){
            System.out.println("Error:Port.getCentralSlotPosition(containers,num)");
            throw new UnsupportedOperationException("Error:Port.getCentralSlotPosition(containers,num)!!");
        }
        double x = containers[0].getPresentSlot().getCentralLocation().getX();
        double y = (containers[0].getPresentSlot().getCentralLocation().getY()+
                containers[1].getPresentSlot().getCentralLocation().getY())/2;
        return new Point2D.Double(x, y);
    }
    /**
     * 为Ship初始化OnBlock的container
     * BlockNum SectionNum随机;
     * @param ship
     * @param ftSize
     * @param containerState
     * @return container or throw exception
     */
    public Container setLoadingContainersOnBlock(Ship ship,String ftSize,String containerState){
        double ftsize = 0;
        
        switch(ftSize){
            case StaticName.SIZE20FT:
                ftsize = 20;
                break;
            case StaticName.SIZE40FT:
                ftsize = 40;
                break;
        }
        int yardNo = (int)Math.floor(Math.random()*InputParameters.getTotalYardNum());
        int blockNo = (int)Math.floor(Math.random()*InputParameters.getTotalBlockNumPerYard());
        for(int i = 0;i<this.yards[yardNo].getBlock()[blockNo].getCurrentContainers().length;i++){
            if(yards[yardNo].getBlock()[blockNo].getCurrentContainers()[i].getFTsize() == ftsize &&
                    yards[yardNo].getBlock()[blockNo].getCurrentContainers()[i].getGoalShip() == null &&
                    yards[yardNo].getBlock()[blockNo].getCurrentContainers()[i].getState().equals(containerState)==true &&
                    yards[yardNo].getBlock()[blockNo].getCurrentContainers()[i].getTime_LeavingBlock() == -1){
//System.out.println("setLoadingContainersOnBlock成功 "+"FT"+ftsize+" Container in"+" yard["+yardNo+"] block["+blockNo+"]");
                yards[yardNo].getBlock()[blockNo].getCurrentContainers()[i].setGoalShip(ship);
                return yards[yardNo].getBlock()[blockNo].getCurrentContainers()[i];
            }
        }
        System.out.println("!!!E!!!!:setLoadingContainersOnBlock：null");
        return setLoadingContainersOnBlock(ship,ftSize,containerState);
        
//        throw new UnsupportedOperationException("Error:YardCrane.haveRightLoadingAGVArriving(Point2D position)出错！！");
    }
    /**
     * 获得当前已经靠泊成功的所有Ship;
     * @return ship[]
     */
    public Ship[] getCurrentShips(){
        Ship[] ships = new Ship[this.berths.length];
        int num = 0;
        for (Berth berth : this.berths) {
            if (berth.getShip() != null) {
                if (num > 0) {
                    boolean isIn = false;
                    for (int j = 0; j<num; j++) {
                        if (ships[j].equals(berth.getShip()) == true) {
                            isIn = true;
                            break;
                        }
                    }
                    if (isIn == false) {
                        ships[num] = berth.getShip();
                        num++;
                    }
                } else {
                    ships[num] = berth.getShip();
                    num++;
                }
            }
        }
        if(num == 0){
            return null;
        }else{
            Ship[] nowShips = new Ship[num];
            System.arraycopy(ships, 0, nowShips, 0, num);
            return nowShips;
        }
    }
    public Ship[] getShipsHaveToPortWork(){
        //获得到达的在港船舶数量;
        if(this.getCurrentShips() == null){
            return null;
        }
        int shipNum = this.getCurrentShips().length;
        int[] shipHaveWork = new int[shipNum];
        int num = 0;
        for(int i = 0;i<shipNum;i++){
            if(this.getCurrentShips()[i].getShipContainers().getLoadingContainersOutside() != null){
                num++;
                shipHaveWork[i] = 1;
            }else{
                shipHaveWork[i] = 0;
            }
        }
        if(num == 0){
            return null;
        }
        Ship[] shipHaveToPortWork = new Ship[num];
        for(int i = 0;i<num;i++){
            for(int j = 0;j<shipNum;j++){
                if(shipHaveWork[j] == 1){
                    shipHaveToPortWork[i] = this.getCurrentShips()[j];
                    shipHaveWork[j] = 0;
                    break;
                }
            }
        }
        if(shipHaveToPortWork[num-1] == null){
            System.out.println("!!!Error:Port.getshipHaveToPortWork()!!!");
            throw new UnsupportedOperationException("Not supported yet.");
        }
        return shipHaveToPortWork;
    }
    public String getSimulatorTime(){
        try {
            return new DecimalFormat("#.0000").format(this.simulator.getSimulatorTime());
        } catch (RemoteException ex) {
            Logger.getLogger(Port.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Integer.toString(0);
    }
    
    public Object[][] getInputMatrix(){
        Object[][] res = {
            {
                this.getYards().length,
                InputParameters.getBerthLength(InputParameters.getBigBerthDWT())+"m*"+this.getBerths().length,
                Integer.toString(InputParameters.getBigBerthDWT()) + "万吨级",
                this.quayCranes.length,
                Integer.toString(InputParameters.getTotalBayNum()),
                Integer.toString(InputParameters.getLanesNumOfAGV()),
                Integer.toString(InputParameters.getLanesNumOfTruck()),
//            ,{{
//                1 + ":" + InputParameters.getWWTransferProportion(),
//                "2",
//                "3",
//                "4",
//                "5",
//                "6",
//                "7", 
//            }}
            }};
        return res;   
    }
    public String[] getInputStringName(){
        String[] rowName={
            "Yard","Berth长度","设计船型","岸桥数量","贝位数","AGV车道数","集卡车道数"
//                ,{"水水中转比例","2","3", "4", "5","6","7"}
        };
        return rowName;
    }

    /**
     * @return the totalLength
     */
    public double getTotalLength() {
        if(totalLength == 0){
            System.out.println("!!!Error:Port.getTotalLength()!!!");
        throw new UnsupportedOperationException("!!!Error:Port.getTotalLength()!!!");
        }
        return totalLength;
    }

    /**
     * AGV在QC下进行装卸时的区域的中轴线X坐标;
     * @return 
     */
    public double getAGVUnderQCAreaCentralX() {
        return this.getRoadNetWork().getagvUnderQCRoad().CentralAxis(1).getX();
    }

    public AGVBufferArea getAGVBufferArea() {
        for (FunctionArea functionArea : this.getRoadNetWork().functionAreas) {
            if(functionArea.getAreaNum().equals(StaticName.AGVBUFFER)){
                return (AGVBufferArea)functionArea;
            }
        }
        System.out.println("!!!Error:Port.getAGVBufferArea()!!!");
        throw new UnsupportedOperationException("!!!Error:Port.getAGVBufferArea()!!!");
    }

    /**
     * @param laneNum 车道编号;
     * @return 
     */
    public double getAGVUnderQCAreaPositionX(int laneNum) {
        double x1 = this.getRoadNetWork().getagvUnderQCRoad().getedge()[0].getX();
        //double x2 = this.getRoadNetWork().getagvUnderQCRoad().getedge()[1].getX();
        for(int i = 1;i<=InputParameters.getMaxPresentWorkingAGV();i++){
            if(i == laneNum){
                return x1+(i-0.5)*InputParameters.getSingleRoadWidth();
            } 
        }
        if(laneNum == 0){
            System.out.println("!!!Error:Port.getAGVUnderQCAreaPositionX()!!!LaneNum;"+0);
        throw new UnsupportedOperationException("!!!Error:Port.getAGVBufferArea()!!!");
        }
        System.out.println("!!!Error:Port.getAGVUnderQCAreaPositionX()!!!:::"+laneNum);
        throw new UnsupportedOperationException("!!!Error:Port.getAGVBufferArea()!!!");
                
    }

    /////////container总数;
    /////////堆场容量可能超限;
    public String getCNUM() {
        int num = 0;
        for (Yard yard : this.getYards()) {
            for (Block block : yard.getBlock()) {
                num += block.getContainersTEU();
            }
        }
        return num+"TEU";
    }
}