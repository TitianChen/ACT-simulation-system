/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parameter;


import apron.Berth;
import CYT_model.Port;
import CYT_model.Point2D;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import nl.tudelft.simulation.jstats.distributions.DistExponential;
import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.distributions.DistTriangular;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import static parameter.StaticName.AGVCAR;
import roadnet.AGVBufferArea;
import roadnet.FunctionArea;
import roadnet.RoadNet;
import roadnet.RoadSection;
import storageblock.Yard;


/**
 *
 * @author YutingChen, yuting.chen17@imperial.ac.uk
 * Dalian University of Technology
 */
public final class InputParameters{

    //仿真输入;
    private static int simulationSpeed = 91;//仿真速度;
    //文件存储位置;
    public static final String OUTPUTPATH = "F:/SIMULATION/Output/";
    public static final String INPUTPATH = "F:/SIMULATION/Input/";
    private static double yearTEUW;//年TEU;//万
    
    //码头路网、基本布局部分;
    private static int anchorageNum;//锚地数量;///目前做的是没有内锚地的。
    private static int channelNum;//航道数量;
    //船舶到达部分;
    private static int shipNum;//到港船舶总数;
    private static int prop15;
    private static int prop12;
    private static int prop10;
    private static int prop7;
    private static int prop5;
    private static int prop3;
    private static String shipArrivalTimeRule;//船舶到达的时间间隔规律服从负指数分布;
    private static String shipConatainersTransfferedRule;//船舶装卸量概率分布服从标准正态分布;
    private static double shipArrivalDeltaTime;//船舶到港间隔时间均值;单位:h;
    private static double earliestTimeIn;//集港时间(早于船舶到达时间)距船舶到达时间之间的距离;
    private static double latestTimeIn;//截港时间(早于船舶到达时间)距船舶到达时间之间的距离;
    private static double earliestTimeOut;//提箱出港开始时间距集装箱到达堆场时间之间的距离;
    private static double latestTimeOut;//提箱离港最晚时间距集装箱到达堆场时间之间的距离;
    private static double triangleDist_mode;//三角形分布顶点距左顶点的水平距离/左右顶点的边长;
    private static StreamInterface stream;
    private static double haveTEUProp;//载箱率;(占最大载箱量) 
    private static double unloadProp;//卸货比例(占总载箱量)
    private static double loadProp;//装船比例
    private static double proportionOf20;//20ft箱比例;
    //码头设施部分;
    private static int bigBerthDWT;//针对两个相同的Berth而言：泊位的设计吨级;单位：万吨级
    private static int numOfAGVPerQC;//每台QC配备的AGV数量;
    public static String AGVType;
    public static int bigBerthNum;//泊位默认排序：海侧看陆侧--从左往右从大到小。
    private static int middleBerthNum;//中泊位;
    private static int smallBerthNum;//小泊位;
    //
    private static int quayCraneNum;
    private static int QCtype;
    private static int maxPresentWorkingAGV;
    private static String QCVersion;
    //
    private static String YCtype;
    private static int YCLiftingNum;
    private static int YCContainerHeightNum;
    private static int PerYCRowNum;
    private static int YCNumPerBlock;
    //
    private static int totalYardNum;//箱区编号;
    private static int totalBlockNumPerYard;//堆场编号;一个堆场两个RMG
    private static int totalBayNum;//贝位编号;20ft--奇数,40ft--偶数;
    private static double distanceYOfBlock;//堆场之间的距离;
    private static double distanceOfContainers;//相邻集装箱之间的距离;
    private static double lengthOfYard;//箱区长度,不包括两端的车辆交换区;
    private static double lengthOfTransferPoint;//交换区长度;
    private static double RMGtrackGuage;//轨距;
    private static double InitialProportionOfContainersOnYard;//初始堆箱比例;
    //private static double WWTransferProportion;//水水中转所占比例;
    //路网部分;
    private static double turningRadiusOfCar;//转弯半径;
    private static double singleRoadWidth;//单车道宽度;
    private static int lanesNumOfAGV;//AGV车道数;
    private static int lanesNumOfTruck;//集卡车道数;
    private static int widthOfTruckRoad;//堆场间，集卡道路的长度;
    private static Point2D.Double pYard1_0;//左堆场P1点;
    private static Point2D.Double pBerth1_0;//左泊位P1点;
    private static double AGVBufferLength;//AGV缓冲区x方向长度;
    private static double distanceBetweenAGVRoadAndBlock;//堆场和前沿道路之间的距离
    private static double QCGuage;//岸桥轨距;
    public static double MinDistanceofQC = 40;//QC间最小距离;
    public static double animationTimeUnit = 0.0005;  
    private static boolean needAnimation;///是否需要动画;
    private static double simulationYear;

    
    public InputParameters(){
        
        InputParameters.anchorageNum = 3;
        InputParameters.channelNum = 2;
        
        InputParameters.needAnimation = false;
        InputParameters.yearTEUW = 120;
        InputParameters.simulationYear = 0.3;//0.5;//0.3;//0.5;//////比较时用0.3即可;
        
        InputParameters.haveTEUProp = 1;
        InputParameters.unloadProp = 0.35*0.5;
        InputParameters.loadProp = 0.35*0.5;
        
        //time的时间均为min;
        InputParameters.prop15 = (int) (0.01 * 100);
        InputParameters.prop12 = (int) (0.02 * 100);
        InputParameters.prop10 = (int) (0.17 * 100);
        InputParameters.prop7 = (int) (0.20 * 100);
        InputParameters.prop5 = (int) (0.25 * 100);
        InputParameters.prop3 = (int) (0.35 * 100);
        InputParameters.earliestTimeIn = 4320;//三天内完成集港
        InputParameters.latestTimeIn = 20;//
        InputParameters.earliestTimeOut = 20;//200;
        InputParameters.latestTimeOut = 5760;//四天内完成出港;
        InputParameters.triangleDist_mode = 0.5;
        
        InputParameters.numOfAGVPerQC = 6;
        InputParameters.shipArrivalTimeRule = StaticName.NEGEXPDISTRIBUTION;
        InputParameters.shipConatainersTransfferedRule = StaticName.NORMALDISTRIBUTION;
        
        int averageTEU = (int)(0.01*0.35*(12500*prop15+11000*prop12+9500*prop10+6630*prop7+5650*prop5+3500*prop3));
        InputParameters.shipNum = (int)(simulationYear*yearTEUW*10000/averageTEU);
        InputParameters.shipArrivalDeltaTime = (int)(60*(simulationYear*345*24/shipNum));//858;//120万TEU---14.30h;
        
        InputParameters.stream = new Java2Random();
        InputParameters.proportionOf20 = 0.7;
        
        InputParameters.bigBerthDWT = 10;//10万吨级;////可以输入：1,3,5,7,10,15,20
        InputParameters.AGVType = "111";
        InputParameters.bigBerthNum = 2;
        InputParameters.middleBerthNum = 0;
        InputParameters.smallBerthNum = 0;
        InputParameters.QCVersion = "twincar_1";
        InputParameters.QCtype = 2;//双小车岸桥;
        InputParameters.maxPresentWorkingAGV = 4;
        InputParameters.quayCraneNum = 4*2;//findMaxQCNumPerBerth(bigBerthDWT)*bigBerthNum;
        
        //堆场部分;
        InputParameters.YCtype = "RMG";
        InputParameters.YCLiftingNum = 7;
        InputParameters.YCContainerHeightNum = 6;
        InputParameters.PerYCRowNum = 7;
        InputParameters.YCNumPerBlock = 2;
        //路网部分;
        InputParameters.turningRadiusOfCar = 9;//转弯半径;
        InputParameters.AGVBufferLength = 27;//AGV缓冲区x方向长度;
        InputParameters.singleRoadWidth = 4;//单车道宽度;
        InputParameters.lanesNumOfAGV = 4;//AGV车道数;
        InputParameters.lanesNumOfTruck = 8;//集卡车道数;
        InputParameters.distanceBetweenAGVRoadAndBlock = 9.27;//堆场和前沿道路之间的距离
        //码头布局部分;
        InputParameters.pBerth1_0 = new Point2D.Double(0, getBerthLength(getBigBerthDWT())/2);//泊位中点坐标
        InputParameters.totalYardNum = 2;//----暂时定2;
        InputParameters.totalBlockNumPerYard = 8;
        InputParameters.totalBayNum = 40;
        InputParameters.distanceYOfBlock = 4;
        InputParameters.distanceOfContainers = 0.4;
        InputParameters.lengthOfYard = InputParameters.totalBayNum*InputParameters.getLengthOf20FTContainer()+
                (InputParameters.totalBayNum-1)*InputParameters.distanceOfContainers;
        InputParameters.lengthOfTransferPoint = 19;
        InputParameters.widthOfTruckRoad = (int)(singleRoadWidth*lanesNumOfTruck+7*2);
        InputParameters.RMGtrackGuage = Math.max(23.47,
                PerYCRowNum*getWidthOfContainer()+(PerYCRowNum-1)*distanceOfContainers+4);
        InputParameters.QCGuage = 35;
        InputParameters.pYard1_0 = new Point2D.Double(3.5+QCGuage+7.21-0.5*singleRoadWidth+maxPresentWorkingAGV*singleRoadWidth
                +AGVBufferLength+lanesNumOfAGV*singleRoadWidth+distanceBetweenAGVRoadAndBlock, getpBerth1_0().getY()+
                0.5*getBerthLength(getBigBerthDWT())-0.5*widthOfTruckRoad-lengthOfTransferPoint-lengthOfYard);
        InputParameters.InitialProportionOfContainersOnYard = 2000000000;//8;
        //InputParameters.WWTransferProportion = 10000000;
    }
    public static void updateRelativeInput(){
        //船舶到港;
        int averageTEU = (int)(0.01*0.35*(12500*getProp15()+11000*getProp12()+9500*getProp10()+6630*getProp7()+5650*getProp5()+3500*getProp3()));
        InputParameters.shipNum = (int)(simulationYear*yearTEUW*10000/averageTEU);
        InputParameters.shipArrivalDeltaTime = (int)(60*(simulationYear*345*24/shipNum));//858;//120万TEU---14.30h;
        //码头布局部分;
        InputParameters.pBerth1_0 = new Point2D.Double(0, getBerthLength(getBigBerthDWT())/2);//泊位中点坐标
        InputParameters.lengthOfYard = InputParameters.totalBayNum*InputParameters.getLengthOf20FTContainer()+
                (InputParameters.totalBayNum-1)*InputParameters.distanceOfContainers;
        InputParameters.widthOfTruckRoad = (int)(singleRoadWidth*lanesNumOfTruck+7*2);
        InputParameters.pYard1_0 = new Point2D.Double(3.5+QCGuage+7.21-0.5*singleRoadWidth+maxPresentWorkingAGV*singleRoadWidth
                +AGVBufferLength+lanesNumOfAGV*singleRoadWidth+distanceBetweenAGVRoadAndBlock, getpBerth1_0().getY()+
                0.5*getBerthLength(getBigBerthDWT())-0.5*widthOfTruckRoad-lengthOfTransferPoint-lengthOfYard); 
        //堆场布局部分;
        InputParameters.RMGtrackGuage = Math.max(23.47,
                PerYCRowNum*getWidthOfContainer()+(PerYCRowNum-1)*distanceOfContainers+4);
    }
    public static int getBerthLength(Berth berth) {
        if (berth.getBerthType() == 1) {
            //大型泊位;
            return InputParameters.getBerthLength(InputParameters.getBigBerthDWT());
        } else {
            System.out.println("Error:InputParameters.getBerthLength(Berth berth)!");
            throw new UnsupportedOperationException("Error:InputParameters.getBerthLength(Berth berth)");
        }
    }

    //根据设计吨级确定泊位长度;------参照港规课本定的;
    public static int getBerthLength(int designDWT) {
        switch (designDWT) {
            case 1:
                return 161;
            case 2:
                return 210;
            case 3:
                return 281;
            case 5:
                return 333;
            case 7:
                return 340;
            case 10:
                return 386;
            case 15:
                return 438;
            case 20:
                return 440;
            default:
                System.out.println("Error:InputParameters.getBerthLength(int designDWT)!");
                throw new UnsupportedOperationException("Error:InputParameters.getBerthLength(int designDWT)");
        }
    }
    public static double getWidthOfContainer(){
        return 2.44;
    }
    public static double getLengthOf20FTContainer(){
        return 6.1;
    }
    public static double getLengthOf40FTContainer(){
        return 12.2;
    }
    public static double getLengthofContainer(int ftsize){
        if(ftsize == 20){
            return getLengthOf20FTContainer();
        }else if(ftsize == 40){
            return getLengthOf40FTContainer();
        }else{
            System.out.println("Error:getLengthofContainer(int ftsize):"+ftsize);
            throw new UnsupportedOperationException("Error:getLengthofContainer(int ftsize)");
        }
        
    }
    public static double getHeightOfContainer(){
        return 2.59;
    }
    /**
     * 均布随机
     * @param start
     * @param end
     * @return 
     */
    public static double getRandom(int start,int end){
        double res = (start+Math.floor(Math.random()*(end-start+1)));
        if(res>end){
            System.out.println("Error:getRandom(int start,int end)--res:"+res);
            throw new UnsupportedOperationException("Error:SetParameter.getRandom");
        }else if(res<start){
            System.out.println("Error:getRandom(int start,int end)--res:"+res);
            throw new UnsupportedOperationException("Error:SetParameter.getRandom");
        }else{
            return res;
        }
    }

    public static double getRandom(String distributionRule,double mean){          
        switch(distributionRule){
            case StaticName.NEGEXPDISTRIBUTION:
                //负指数分布;
                DistExponential distExp = new DistExponential(stream, mean);
                return distExp.draw();
            default:
                break;
        }
        throw new UnsupportedOperationException("Error:SetParameter.getRandom");
    }
    /**
     * @param miu
     * @param sigma
     * @param min
     * @param max
     * @return 
     */
    public static double getNormalRandom(double miu,double sigma,double min,double max){
        DistNormal distNormal = new DistNormal(stream,miu,sigma);
        double res = distNormal.draw();
        while(res>max || res<min){
            res = distNormal.draw();
        }
        return res;
    }
    /**
     * 三角形分布
     * a<b<c
     * @param distributionRule
     * @param a the minimum 开始点
     * @param b the mode 顶点，最高点的横坐标;
     * @param c the maximum 结束点
     * @return random double
     */
    public static double getRandomTriangle(String distributionRule,double a,double b,double c){
        if(distributionRule.equals(StaticName.TRIANGULAR) == false){
            System.out.println("Error:SetParameter.getRandomTriangle");
            throw new UnsupportedOperationException("Error:SetParameter.getRandomTriangle");
        }
        DistTriangular distTriangle = new DistTriangular(stream,a,b,c);
        return distTriangle.draw();
    }
    public static int getRandomIntTriangle(String distributionRule,int min,int max){
        if(distributionRule.equals(StaticName.TRIANGULAR) == false){
            System.out.println("Error:SetParameter.getRandomTriangle");
            throw new UnsupportedOperationException("Error:SetParameter.getRandomTriangle");
        }
        DistTriangular distTriangle = new DistTriangular(stream,min,(int)(0.5*(min+max)),max);
        int res = (int)(distTriangle.draw()+0.49);
        if(res<min){
            res = min;
        }else if(res>max){
            res = max;
        }
        return res;
    }
    
    
    
    //船舶到港输入参数初始化;
    public static void createShipArrive(String name,double[] arrivalTime,double firstArrivalTime){
        if(name.equals("arrivalTime") == false){
            throw new UnsupportedOperationException("Error:SetParameter.createShipArrive");
        }
        arrivalTime[0] = firstArrivalTime;
        if(arrivalTime.length == 1){
            return;
        }
        for(int i = 1;i<arrivalTime.length;i++){
            //服从均值为ShipArrivalDeltaTime的负指数分布;
            double deltaT_minute = getRandom(shipArrivalTimeRule, getShipArrivalDeltaTime());
            while(deltaT_minute<20){
                //不让间隔过小;
                deltaT_minute = getRandom(shipArrivalTimeRule, getShipArrivalDeltaTime());
            }
            deltaT_minute = Math.ceil(deltaT_minute);
            arrivalTime[i] = deltaT_minute+arrivalTime[i-1];
        }
    }
    public static void createShipArrive(String name,String[] str){
        switch(name){
            case "shipDWT":
                for(int i = 0;i<str.length;i++){
                    int W;
                    int ran = (int)getRandom(1,100);
                    if(ran<=getProp3()){
                        W = 3;
                    }else if(ran<=getProp3()+getProp5()){
                        W = 5;
                    }else if(ran<=getProp3()+getProp5()+getProp7()){
                        W = 7;
                    }else if(ran<=getProp3()+getProp5()+getProp7()+getProp10()){
                        W=10;
                    }else if(ran<=getProp3()+getProp5()+getProp7()+getProp10()+getProp12()){
                        W=12;
                    }else{
                        W=15;
                    }
                    W = W * 10000;
                    str[i] = Integer.toString((int)getRandom(getMinDWT(W),getMaxDWT(W)));
                    if(InputParameters.middleBerthNum != 0 || InputParameters.smallBerthNum != 0){
                        System.out.println("Error:InputParameters.createShipArrive.BerthNum改一下哈！");
                        throw new UnsupportedOperationException("Error:"
                                + "InputParameters.createShipArrive.BerthNum改一下哈");
                    }
                }    
                break;
            default:
                throw new UnsupportedOperationException("Error:SetParameter.createShipArrive");
        }
    }
    
    /**
     * 1,2,3,5,7,10,12,15
     * @param DWTLevel
     * @return 
     */
    public static int getMinDWT(int DWTLevel){
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
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InputParameters.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InputParameters.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(int i = 0;i<row;i++){
            if(arr[i][0]==DWTLevel){
                return (int)arr[i][1];
            }
        }
        System.out.println("!!Error:InputParameters.getMinDWT(DWTLevel)");
        throw new UnsupportedOperationException("Error:InputParameters.getMinDWT(DWTLevel)");
    }
    /**
     * 1,2,3,5,7,10,12,15
     * @param DWTLevel
     * @return 
     */
    public static int getMaxDWT(int DWTLevel){
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
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InputParameters.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InputParameters.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(int i = 0;i<row;i++){
            if(arr[i][0]==DWTLevel){
                return (int)arr[i][2];
            }
        }
        System.out.println("!!Error:InputParameters.getMaxDWT(DWTLevel)");
        throw new UnsupportedOperationException("Error:InputParameters.getMaxDWT(DWTLevel)");
        
    }
    
    
    
    
    /**
     * 获得该港口允许停泊的船舶最小载重吨
     * @return 
     */
    public static int getMinDWT(){
        return 4501; //默认停靠5万吨级以上的船舶;
    }
    /**
     * 用作集装箱码头堆场布局的底色------渐变至white;
     * @return float[] HSB
     */
    public static float[] getColor_DeepGreen(){
        float[] rgbvals = new float[3];
        float[] hsb = new float[3];
        hsb = Color.RGBtoHSB(34, 139, 34, rgbvals);
        return hsb;
    }
    /**
     * 用作集装箱码头堆场布局的底色------渐变至white;
     * @return float[] HSB
     */
    public static float[] getColor_LightPure(){
        float[] rgbvals = new float[3];
        float[] hsb = new float[3];
        hsb = Color.RGBtoHSB(252, 229, 205, rgbvals);
        return hsb;
    }
    /**
     * 用作集装箱码头船舶的颜色-----渐变至white;
     * @return float[] HSB
     */
    public static float[] getColor_Brown(){
        float[] rgbvals = new float[3];
        float[] hsb = new float[3];
        hsb = Color.RGBtoHSB(152, 51, 0, rgbvals);
        return hsb;
    }
    
        
    
    
    
    /**
     * 获得该港口允许停泊的船舶最大载重吨
     * @return 
     */
    public static int getMaxDWT(){
        int row = 11,column = 9;
        double[][] arr = new double[row][column]; //插入的数组  
        File file = new File(INPUTPATH+"ShipDesign.txt");  //存放数组数据的文件  
        try (BufferedReader in = new BufferedReader(new FileReader(file)) //
        ) {
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
        } catch (FileNotFoundException ex) {
            Logger.getLogger(InputParameters.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(InputParameters.class.getName()).log(Level.SEVERE, null, ex);
        }
        for(int i = 0;i<row;i++){
            if(arr[i][0] == getBigBerthDWT()*10000){
                return (int) arr[i][2];
            }
        }
        System.out.println("!!Error:InputParameters.getMaxDWT()");
        throw new UnsupportedOperationException("Error:InputParameters.getMaxDWT()");
    }
    
    /**
     * Object[] To double[]
     * @param obj object[]
     * @return double[]
     */
    public static double[] objectToDouble(Object[] obj){
        int num = obj.length;
        double[] dou = new double[num];
        for(int i = 0;i<num;i++){
            dou[i] = Double.parseDouble(obj[i].toString());
        }
        return dou;
    }
    /**
     * Object To double
     * @param obj object
     * @return double
     */
    public static double objectToDouble(Object obj){
        int num = 1;
        double dou = 0;
        for(int i = 0;i<num;i++){
            dou = Double.parseDouble(obj.toString());
        }
        return dou;
    }

    public static int getShipNum() {
        return InputParameters.shipNum;
    }

    /**
     * @return the shipArrivalRule
     * 
     */
    public static String getShipArrivalRule() {
        return InputParameters.shipArrivalTimeRule;
    }

    /**
     * @return the shipConatainersTransfferedRule
     */
    public static String getShipConatainersTransfferedRule() {
        return shipConatainersTransfferedRule;
    }

    /**
     * @return the shipArrivalDeltaTime
     */
    public static double getShipArrivalDeltaTime() {
        return shipArrivalDeltaTime;
    }

    /**
     * @param aShipArrivalDeltaTime the shipArrivalDeltaTime to set
     */
    public static void setShipArrivalDeltaTime(double aShipArrivalDeltaTime) {
        shipArrivalDeltaTime = aShipArrivalDeltaTime;
        updateRelativeInput();
    }

    /**
     * @return the numOfAGVPerQC
     */
    public static int getNumOfAGVPerQC() {
        return numOfAGVPerQC;
    }

    /**
     * @return the bigBerthNum
     */
    public static int getBigBerthNum() {
        return bigBerthNum;
    }

    /**
     * @return the quayCraneNum
     */
    public static int getQuayCraneNum() {
        return quayCraneNum;
    }

    /**
     * @return the AGVType
     */
    public static String getAGVType() {
        return AGVType;
    }

    /**
     * @return the QCtype
     */
    public static int getQCtype() {
        return QCtype;
    }

    /**
     * @return the QCVersion
     */
    public static String getQCVersion() {
        return QCVersion;
    }

    /**
     * @return the YCtype
     */
    public static String getYCtype() {
        return YCtype;
    }

    /**
     * @return the YCLiftingNum
     */
    public static int getYCLiftingNum() {
        return YCLiftingNum;
    }

    /**
     * @return the YCContainerHeightNum
     */
    public static int getYCContainerHeightNum() {
        return YCContainerHeightNum;
    }

    /**
     * @return the PerYCRowNum
     */
    public static int getPerYCRowNum() {
        return PerYCRowNum;
    }

    /**
     * @return the totalYardNum
     */
    public static int getTotalYardNum() {
        return totalYardNum;
    }

    /**
     * @return the totalSectionNum
     */
    public static int getTotalBlockNumPerYard() {
        return totalBlockNumPerYard;
    }

    /**
     * @return the totalBayNum
     */
    public static int getTotalBayNum() {
        return totalBayNum;
    }

    /**
     * @return the YCNumPerBlock
     */
    public static int getYCNumPerBlock() {
        return YCNumPerBlock;
    }

    /**
     * @return the distanceYOfBlock
     */
    public static double getDistanceYOfBlock() {
        return distanceYOfBlock;
    }

    /**
     * @param aDistanceYOfSection the distanceYOfBlock to set
     */
    public static void setDistanceYOfBlock(double aDistanceYOfSection) {
        distanceYOfBlock = aDistanceYOfSection;
        updateRelativeInput();
    }

    /**
     * @return the distanceOfContainers
     */
    public static double getDistanceOfContainers() {
        return distanceOfContainers;
    }

    /**
     * @return the lengthOfYard
     */
    public static double getLengthOfYard() {
        return lengthOfYard;
    }

    /**
     * @return the lengthOfTransferPoint
     */
    public static double getLengthOfTransferPoint() {
        return lengthOfTransferPoint;
    }

    /**
     * @return the RMGtrackGuage
     */
    public static double getRMGtrackGuage() {
        return RMGtrackGuage;
    }

    /**
     * @return the turningRadiusOfCar
     */
    public static double getTurningRadiusOfCar() {
        return turningRadiusOfCar;
    }

    /**
     * @return the singleRoadWidth
     */
    public static double getSingleRoadWidth() {
        return singleRoadWidth;
    }

    /**
     * @return the lanesNumOfAGV
     */
    public static int getLanesNumOfAGV() {
        return lanesNumOfAGV;
    }

    /**
     * @return the lanesNumOfTruck
     */
    public static int getLanesNumOfTruck() {
        return lanesNumOfTruck;
    }

    /**
     * @param aTurningRadiusOfCar the turningRadiusOfCar to set
     */
    public static void setTurningRadiusOfCar(double aTurningRadiusOfCar) {
        turningRadiusOfCar = aTurningRadiusOfCar;
        updateRelativeInput();
    }

    /**
     * @return the MaxPresentWorkingAGV
     */
    public static int getMaxPresentWorkingAGV() {
        return maxPresentWorkingAGV;
    }

    /**
     * @return the InitialPropoertionOfContainersOnBlock
     */
    public static double getInitialProportionOfContainersOnYard() {
        return InitialProportionOfContainersOnYard;
    }

    /**
     * @return the earliestTimeIn
     */
    public static double getEarliestTimeIn() {
        return earliestTimeIn;
    }

    /**
     * @return the latestTimeIn
     */
    public static double getLatestTimeIn() {
        return latestTimeIn;
    }

    /**
     * @return the earliestTimeOut
     */
    public static double getEarliestTimeOut() {
        return earliestTimeOut;
    }

    /**
     * @return the latestTimeOut
     */
    public static double getLatestTimeOut() {
        return latestTimeOut;
    }

    /**
     * @return the triangleDist_mode
     */
    public static double getTriangleDist_mode() {
        return triangleDist_mode;
    }

    /**
     * @return the middleBerthNum
     */
    public static int getMiddleBerthNum() {
        return middleBerthNum;
    }

    /**
     * @return the smallBerthNum
     */
    public static int getSmallBerthNum() {
        return smallBerthNum;
    }

    /**
     * @return the widthOfTruckRoad
     */
    public static int getWidthOfTruckRoad() {
        return widthOfTruckRoad;
    }

    /**
     * @return the pYard1_0
     */
    public static Point2D.Double getpYard1_0() {
        return pYard1_0;
    }

    /**
     * @return the pBerth1_0
     */
    public static Point2D.Double getpBerth1_0() {
        return pBerth1_0;
    }

    /**
     * @return the simulationSpeed
     */
    public static int getSimulationSpeed() {
        return simulationSpeed;
    }

    /**
     * @param aSimulationSpeed the simulationSpeed to set
     */
    public static void setSimulationSpeed(String aSimulationSpeed) {
        setSimulationSpeed(Integer.parseInt(aSimulationSpeed));
    }

    /**
     * @param aSimulationSpeed the simulationSpeed to set
     */
    public static void setSimulationSpeed(int aSimulationSpeed) {
        simulationSpeed = aSimulationSpeed;
        updateRelativeInput();
    }

    /**
     * @param aShipNum the shipNum to set
     */
    private static void setShipNum(int aShipNum) {
        shipNum = aShipNum;
        updateRelativeInput();
    }
    /**
     *路网构造函数; 
     * @param port
     * @return 
     * @throws CloneNotSupportedException
    */
    public static RoadNet creatRoadNet(Port port) throws CloneNotSupportedException {
        //FunctionArea stocking
        //yard交换区不在坐标内;
        Point2D[] pYard1 = new Point2D[2];
        pYard1[0] = new Point2D.Double(getpYard1_0().getX(), getpYard1_0().getY());
        pYard1[1] = new Point2D.Double(pYard1[0].getX()+getRMGtrackGuage()*getTotalBlockNumPerYard()+
                getDistanceYOfBlock()*(getTotalBlockNumPerYard()-1), 
                pYard1[0].getY()+getLengthOfYard());
        FunctionArea Yard1 = new Yard(pYard1[0],pYard1[1],"Yard1","Yard1",StaticName.PARALLEL,port);
        
        Point2D[] pYard2 = new Point2D[2];
        pYard2[0] = new Point2D.Double(pYard1[0].getX(), pYard1[1].getY()+getLengthOfTransferPoint()*2+
                getWidthOfTruckRoad());
        pYard2[1] = new Point2D.Double(pYard1[1].getX(),pYard2[0].getY()+getLengthOfYard());
        FunctionArea Yard2 = new Yard(pYard2[0],pYard2[1],"Yard2","Yard2",StaticName.PARALLEL,port);
        port.getYards()[0] = (Yard) Yard1;
        port.getYards()[1] = (Yard) Yard2;
        
        //////注意：不要再改缓冲区名字了因为：PathLengthOnThisRoad()
        //RoadSection Road1前沿装卸路段;
        Point2D[] point1 = new Point2D[4];
        point1[0] = new Point2D.Double(getpBerth1_0().getX()+3.5+QCGuage+7.21-0.5*singleRoadWidth,getpBerth1_0().getY()-(getBerthLength(getBigBerthDWT())/2));
        point1[1] = new Point2D.Double(point1[0].getX()+maxPresentWorkingAGV*singleRoadWidth, point1[0].getY());
        point1[2] = new Point2D.Double(point1[0].getX(),point1[0].getY()+port.getTotalLength());
        point1[3] = new Point2D.Double(point1[1].getX(),point1[2].getY());       
        RoadSection Road1 = new RoadSection(point1,2,AGVCAR,getMaxPresentWorkingAGV(),StaticName.ROADUNDERQC,null,null,StaticName.AGVBUFFER);
        System.out.println("Road1成功");
        //RoadSecttion Road2 Yard1AGV交换区;
        Point2D[] point2 = new Point2D[4];
        point2[0] = new Point2D.Double(pYard1[0].getX(),pYard1[0].getY()-lengthOfTransferPoint);
        point2[1] = new Point2D.Double(pYard1[1].getX(),pYard1[0].getY()-lengthOfTransferPoint);
        point2[2] = new Point2D.Double(pYard1[0].getX(),pYard1[0].getY());
        point2[3] = new Point2D.Double(pYard1[1].getX(),pYard1[0].getY()); 
        RoadSection Road2 = new RoadSection(point2,2,AGVCAR,100,"Road2_Yard1AGVTransfer","Road3",null,StaticName.YARD1);
        System.out.println("Road2成功");
        //RoadSection Road3;Yard1堆场AGV行驶车道
        Point2D[] point3 = new Point2D[4];
        point3[0] = new Point2D.Double(point2[0].getX(),point2[0].getY()-turningRadiusOfCar+(0.5-lanesNumOfAGV)*singleRoadWidth);
        point3[1] = new Point2D.Double(point2[0].getX(),point2[0].getY()-turningRadiusOfCar+0.5*singleRoadWidth);
        point3[2] = new Point2D.Double(point2[1].getX(),point3[0].getY());
        point3[3] = new Point2D.Double(point2[1].getX(),point3[1].getY()); 
        RoadSection Road3 = new RoadSection(point3,1,AGVCAR,lanesNumOfAGV,"Road3","Road4","Road2_Yard1AGVTransfer",null);
        System.out.println("Road3成功");
        //RoadSection Road4;Yrad1AGV行驶车道圆弧区;
        //0 2在外侧，1,3在内侧.
        Point2D[] point4 = new Point2D[4];
        point4[0] = new Point2D.Double(pYard1[0].getX()-distanceBetweenAGVRoadAndBlock-lanesNumOfAGV*singleRoadWidth,
                pYard1[0].getY()-lengthOfTransferPoint);
        point4[1] = new Point2D.Double(pYard1[0].getX()-distanceBetweenAGVRoadAndBlock,
                pYard1[0].getY()-lengthOfTransferPoint);
        point4[2] = new Point2D.Double(point3[0].getX(),point3[0].getY());
        point4[3] = new Point2D.Double(point3[1].getX(),point3[1].getY()); 
        RoadSection Road4 = new RoadSection(point4,3,AGVCAR,lanesNumOfAGV,"Road4",StaticName.ROADWATERSIDE,"Road3",null);
        System.out.println("Road4成功");
        //RoadSection Road5前沿AGV行驶路段;
        Point2D[] point5 = new Point2D[4];
        point5[0] = new Point2D.Double(point4[0].getX(),point4[0].getY());
        point5[1] = new Point2D.Double(point4[1].getX(),point4[1].getY());
        point5[2] = new Point2D.Double(point5[0].getX(),point5[0].getY()+(2*lengthOfTransferPoint+pYard2[1].getY()-pYard1[0].getY()));
        point5[3] = new Point2D.Double(point5[1].getX(),point5[2].getY());       
        RoadSection Road5 = new RoadSection(point5,2,AGVCAR,lanesNumOfAGV,StaticName.ROADWATERSIDE,"Road4","Road6",null);
        System.out.println("Road5成功");
        
        //RoadSection Road8 Yard2AGV交换区;
        Point2D[] point8 = new Point2D[4];
        point8[0] = new Point2D.Double(pYard2[0].getX(),pYard2[1].getY());
        point8[1] = new Point2D.Double(pYard2[1].getX(),pYard2[1].getY());
        point8[2] = new Point2D.Double(pYard2[0].getX(),pYard2[1].getY()+lengthOfTransferPoint);
        point8[3] = new Point2D.Double(pYard2[1].getX(),pYard2[1].getY()+lengthOfTransferPoint); 
        RoadSection Road8 = new RoadSection(point8,2,AGVCAR,100,"Road8_Yard2AGVTransfer",null,"Road7",StaticName.YARD2);
        System.out.println("Road8成功");
        //RoadSection Road7;Yard2堆场AGV行驶车道
        Point2D[] point7 = new Point2D[4];
        point7[0] = new Point2D.Double(point8[2].getX(),point8[2].getY()+turningRadiusOfCar+(-0.5+lanesNumOfAGV)*singleRoadWidth);
        point7[1] = new Point2D.Double(point8[2].getX(),point8[2].getY()+turningRadiusOfCar-0.5*singleRoadWidth);
        point7[2] = new Point2D.Double(point8[1].getX(),point7[0].getY());
        point7[3] = new Point2D.Double(point8[1].getX(),point7[1].getY()); 
        RoadSection Road7 = new RoadSection(point7,1,AGVCAR,lanesNumOfAGV,"Road7","Road6","Road8_Yard2AGVTransfer",null);
        System.out.println("Road7成功");
        
        //RoadSection Road6;Yrad2AGV行驶车道圆弧区;
        //1,3在内侧，0,2在外侧
        Point2D[] point6 = new Point2D[4];
        point6[0] = new Point2D.Double(point5[2].getX(),point5[2].getY());
        point6[1] = new Point2D.Double(point5[3].getX(),point5[3].getY());
        point6[2] = new Point2D.Double(point7[0].getX(),point7[0].getY());
        point6[3] = new Point2D.Double(point7[1].getX(),point7[1].getY()); 
        RoadSection Road6 = new RoadSection(point6,3,AGVCAR,lanesNumOfAGV,"Road6",StaticName.ROADWATERSIDE,"Road7",null);
        System.out.println("Road6成功");
        
        //功能区;AGV缓冲区;
        Point2D[] AGVbufferArea = new Point2D[2];
        AGVbufferArea[0] = new Point2D.Double(point1[1].getX(), point5[0].getY());
        AGVbufferArea[1] = new Point2D.Double(point5[0].getX(), point5[2].getY());
        System.out.println("UUUUU");
        FunctionArea agvBufferArea = new AGVBufferArea(AGVbufferArea[0], AGVbufferArea[1], StaticName.AGVBUFFER, StaticName.AGVBUFFER);
        System.out.println("WWWWWWW");
        //Yard1 TruckTransferArea;
        Point2D[] point9 = new Point2D[4];
        point9[0] = new Point2D.Double(pYard1[0].getX(),pYard1[1].getY()+lengthOfTransferPoint);
        point9[1] = new Point2D.Double(pYard1[1].getX(),pYard1[1].getY()+lengthOfTransferPoint);
        point9[2] = new Point2D.Double(pYard1[0].getX(),pYard1[1].getY());
        point9[3] = new Point2D.Double(pYard1[1].getX(),pYard1[1].getY()); 
        RoadSection Road9 = new RoadSection(point9,2,StaticName.TRUCKCAR,100,"Road9_Yard1TruckTransfer",null,null,StaticName.YARD1);
        System.out.println("Road9成功");
        //Yard2 TruckTransferArea;
        Point2D[] point10 = new Point2D[4];
        point10[0] = new Point2D.Double(pYard2[0].getX(),pYard2[0].getY());
        point10[1] = new Point2D.Double(pYard2[1].getX(),pYard2[0].getY());
        point10[2] = new Point2D.Double(pYard2[0].getX(),pYard2[0].getY()-lengthOfTransferPoint);
        point10[3] = new Point2D.Double(pYard2[1].getX(),pYard2[0].getY()-lengthOfTransferPoint); 
        RoadSection Road10 = new RoadSection(point10,2,StaticName.TRUCKCAR,100,"Road10_Yard2AGVTransfer",null,null,StaticName.YARD2);
        System.out.println("Road10成功");
        //RoadSection Road11 堆场Truck行驶车道
        Point2D[] point11 = new Point2D[4];
        point11[0] = new Point2D.Double(point9[0].getX(),point9[0].getY()+turningRadiusOfCar-0.5*singleRoadWidth);
        point11[1] = new Point2D.Double(point9[0].getX(),point9[0].getY()+turningRadiusOfCar+(-0.5+lanesNumOfTruck)*singleRoadWidth);
        point11[2] = new Point2D.Double(point9[1].getX(),point11[0].getY());
        point11[3] = new Point2D.Double(point9[1].getX(),point11[1].getY()); 
        RoadSection Road11 = new RoadSection(point11,1,StaticName.TRUCKCAR,InputParameters.getLanesNumOfTruck(),
                "Road11","Road9_Yard1TruckTransfer","Road10_Yard2AGVTransfer",null);
        System.out.println("Road11成功");
        
        //InputParameters.lanesNumOfTruck = 6
        
        System.out.println("开始creat roadNet");
        /**
         * 
         */
        RoadSection[] roads = {Road1,Road2,Road3,Road4,Road5,Road6,Road7,Road8,Road9,Road10,Road11};
        FunctionArea[] functions = {Yard1,Yard2,agvBufferArea};
        RoadNet roadNet = new RoadNet(roads,functions);
        System.out.println(Road3.getLength());
        return roadNet;
        
        
//        double res;
//        Point2D p1 = new Point2D.Double(1,20);
//        Point2D p2 = new Point2D.Double(12,52);
//        getRoadNetWork().minPathLength(p1,p2,1);  
        
    /**    
        double res; 
        Point2D p1 = new Point2D.Double(1,8);
        Point2D p2 = new Point2D.Double(2,9.5);
        res = Road2.TimeOnThisRoad(p1,p2); //  System.out.println(res);
        */
    }

    /**
     * 根据泊位设计吨级确定允许配备的最大岸桥数量;
     * @param bigBerthDWT
     * @return 
     * 1,3,5,7,10,15,20
     */
    public static int findMaxQCNumPerBerth(int DWT) {
//        if(this.DWT>=4501 && this.DWT<=27500){2
//            this.minQCLine = 1;
//            this.maxQCLine = 2;
//        }else if(this.DWT>=27501 && this.DWT<=45000){3
//            this.minQCLine = 2;
//            this.maxQCLine = 3;
//        }else if(this.DWT>=45001 && this.DWT<=65000){5
//            this.minQCLine = 3;
//            this.maxQCLine = 4;
//        }else if(this.DWT>=65001 && this.DWT<=85000){7
//            this.minQCLine = 3;
//            this.maxQCLine = 4;
//        }else if(this.DWT>=85001 && this.DWT<=115000){10
//            this.minQCLine = 3;//4;
//            this.maxQCLine = 5;
//        }else if(this.DWT>=115001){>=12
//            this.minQCLine = 5;
//            this.maxQCLine = 5;
        switch (DWT){
            //单位：万吨级;
            case 1:
                return 2;
            case 3:
                return 3;
            case 5:
                return 4;
            case 7:
                return 4;
            case 10:
                return 5;
            case 12:
                return 5;
            case 15: 
                return 5;
            case 20:
                return 5;
            default:
                return -1;
        }
    }
    /**
     * 根据泊位设计吨级确定允许配备的最小岸桥数量;
     * @param bigBerthDWT
     * @return 
     * 3,5,7,10,15,20
     */
    public static int findMinQCNumPerBerth(int DWT) {
//        if(this.DWT>=4501 && this.DWT<=27500){2
//            this.minQCLine = 1;
//            this.maxQCLine = 2;
//        }else if(this.DWT>=27501 && this.DWT<=45000){3
//            this.minQCLine = 2;
//            this.maxQCLine = 3;
//        }else if(this.DWT>=45001 && this.DWT<=65000){5
//            this.minQCLine = 3;
//            this.maxQCLine = 4;
//        }else if(this.DWT>=65001 && this.DWT<=85000){7
//            this.minQCLine = 3;
//            this.maxQCLine = 4;
//        }else if(this.DWT>=85001 && this.DWT<=115000){10
//            this.minQCLine = 3;
//            this.maxQCLine = 5;
//        }else if(this.DWT>=115001){>=12
//            this.minQCLine = 5;
//            this.maxQCLine = 5;
        switch (DWT){
            //单位：万吨级;
            case 1:
                return 1;
            case 3:
                return 2;
            case 5:
                return 3;
            case 7:
                return 3;
            case 10:
                return 3;
            case 12:
                return 3;
            case 15: 
                return 3;
            case 20:
                return 3;
            default:
                return -1;
        }
    }


    /**
     * @return the bigBerthDWT
     */
    public static int getBigBerthDWT() {
        return bigBerthDWT;
    }

    /**
     * @param aBigBerthDWT the bigBerthDWT to set
     */
    public static void setBigBerthDWT(int aBigBerthDWT) {
        bigBerthDWT = aBigBerthDWT;
        setQuayCraneNum();
        updateRelativeInput();
    }

    /**
     */
    public static void setQuayCraneNum() {
        setQuayCraneNumPerBerth(findMaxQCNumPerBerth(bigBerthDWT));
        updateRelativeInput();
    }
    
    public static void setQuayCraneNumPerBerth(int num) {
        InputParameters.quayCraneNum = num*bigBerthNum;
        updateRelativeInput();
    }

    /**
     * @param aTotalBayNum the totalBayNum to set
     */
    public static void setTotalBayNum(int aTotalBayNum) {
        totalBayNum = aTotalBayNum;
        updateRelativeInput();
    }

    /**
     * @return the proportionOf20
     */
    public static double getProportionOf20() {
        return proportionOf20;
    }

    /**
     * @param aMaxPresentWorkingAGV the maxPresentWorkingAGV to set
     */
    public static void setMaxPresentWorkingAGV(int aMaxPresentWorkingAGV) {
        maxPresentWorkingAGV = aMaxPresentWorkingAGV;
        updateRelativeInput();
    }

    /**
     * @param aNumOfAGVPerQC the numOfAGVPerQC to set
     */
    public static void setNumOfAGVPerQC(int aNumOfAGVPerQC) {
        numOfAGVPerQC = aNumOfAGVPerQC;
        updateRelativeInput();
    }

    /**
     * @return the anchorageNum
     */
    public static int getAnchorageNum() {
        return anchorageNum;
    }

    /**
     * @param aAnchorageNum the anchorageNum to set
     */
    public static void setAnchorageNum(int aAnchorageNum) {
        anchorageNum = aAnchorageNum;
    }

    /**
     * @return the channelNum
     */
    public static int getChannelNum() {
        return channelNum;
    }

    /**
     * @param aChannelNum the channelNum to set
     */
    public static void setChannelNum(int aChannelNum) {
        channelNum = aChannelNum;
    }

    /**
     * @param aTotalBlockNumPerYard the totalBlockNumPerYard to set
     */
    public static void setTotalBlockNumPerYard(int aTotalBlockNumPerYard) {
        totalBlockNumPerYard = aTotalBlockNumPerYard;
    }

    /**
     * @return the haveTEUProp
     */
    public static double getHaveTEUProp() {
        return haveTEUProp;
    }

    /**
     * @return the unloadProp
     */
    public static double getUnloadProp() {
        return unloadProp;
    }

    /**
     * @return the loadProp
     */
    public static double getLoadProp() {
        return loadProp;
    }

    /**
     * @return the needAnimation
     */
    public static boolean isNeedAnimation() {
        return needAnimation;
    }

    /**
     * @param aNeedAnimation the needAnimation to set
     */
    public static void setNeedAnimation(boolean aNeedAnimation) {
        needAnimation = aNeedAnimation;
    }

    /**
     * @param aEarliestTimeIn the earliestTimeIn to set
     */
    public static void setEarliestTimeIn(double aEarliestTimeIn) {
        earliestTimeIn = aEarliestTimeIn;
    }

    /**
     * @param aLatestTimeOut the latestTimeOut to set
     */
    public static void setLatestTimeOut(double aLatestTimeOut) {
        latestTimeOut = aLatestTimeOut;
    }

    /**
     * @return the yearTEUW
     */
    public static double getYearTEUW() {
        return yearTEUW;
    }

    /**
     * @param aYearTEUW the yearTEUW to set
     */
    public static void setYearTEUW(double aYearTEUW) {
        yearTEUW = aYearTEUW;
        updateRelativeInput();
    }

    /**
     * @param aPerYCRowNum the PerYCRowNum to set
     */
    public static void setPerYCRowNum(int aPerYCRowNum) {
        PerYCRowNum = aPerYCRowNum;
        updateRelativeInput();
    }

    /**
     * @return the simulationYear
     */
    public static double getSimulationYear() {
        return simulationYear;
    }

    /**
     * @param aSimulationYear the simulationYear to set
     */
    public static void setSimulationYear(double aSimulationYear) {
        simulationYear = aSimulationYear;
        updateRelativeInput();
    }

    /**
     * @return the prop15
     */
    public static int getProp15() {
        return prop15;
    }

    /**
     * @return the prop12
     */
    public static int getProp12() {
        return prop12;
    }

    /**
     * @return the prop10
     */
    public static int getProp10() {
        return prop10;
    }

    /**
     * @return the prop7
     */
    public static int getProp7() {
        return prop7;
    }

    /**
     * @return the prop5
     */
    public static int getProp5() {
        return prop5;
    }

    /**
     * @return the prop3
     */
    public static int getProp3() {
        return prop3;
    }
}
