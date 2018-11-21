/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apron;

import CYT_model.Point2D;
import CYT_model.Port;
import Vehicle.AGV;
import ship.Ship;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import parameter.InputParameters;
import static parameter.InputParameters.INPUTPATH;
import static parameter.InputParameters.MinDistanceofQC;
import static parameter.OutputParameters.outputSingleParameters;
/**
 * Class QueeCrane岸桥类
 * @author YutingChen, yuting.chen17@imperial.ac.uk
 * Dalian University of Technology
 */
public final class QuayCrane {

    private final String name;
    private final Port port;
    private final QuayCraneWork[] QCWork;
    
    
    //类型：双小车、单小车;
    private final int QCType;//类型：双小车-2,单小车-1;////厦门远海--2;
    //---型号----//
    private String QCVersion;//QC型号;---由此确定各参数;
    private double liftingHeight;//起升高度
    private double outReach;//外伸距;
    private double innerReach;//内伸距;
    private double gauge;//轨距;大型岸桥通常具有30m轨距;
    //设备运行参数;
    private double moveSpeed_Crane;//岸桥在轨道上水平移动速度;
    private double efficiencyPerHour;//岸桥装卸效率;岸桥每小时装卸自然箱数;
    private double mainTrolleySpeed;//主小车移动速度;
    private double gantryTrolleySpeed;//门架小车移动速度;
    private double mainTrolleyVerticalSpeed;//主小车起升速度;
    private double gantryTrolleyVerticalSpeed;//门架小车起升速度;
    private final double averageVerticalMove = 20;//平均起升高度;//假定个4m吧;
    //
    private final int numOfAGVPerQC;
    private final AGV[] serviceAGVs;
    //
    private Ship serviceShip;//岸桥服务集装箱船舶;
    private Point2D location;//当前所在位置;
    private Point2D serviceLocation;//岸桥装卸位置;
    private final double exchangePlatformX;//中转平台位置;
    private double mainTrolleyX;//主小车位置;
    private double gantryTrolleyX;//门架小车位置;
    //
    private double finished_load_20FTassignment;//已完成任务量;
    private double finished_load_40FTassignment;//已完成任务量;
    private double finished_unload_20FTassignment;//已完成任务量;
    private double finished_unload_40FTassignment;//已完成任务量;

    private boolean mainTrolleyFree;//主小车空闲;
    private boolean gantryTrolleyFree;//门架小车空闲;
    private boolean mainTrolleyHaveContainer;//主小车上吊具有箱;
    private boolean gantryTrolleyHaveContainer;//门架小车吊具有箱;
    private double NumOnExchangePlatform;//中转平台上箱量;(40ft)
    private final double MaxOnExchangePlatform;//中转平台上最大允许箱量;(40ft)
    
    private int[] laneIsBookedOrOccupied;//QC下正在装卸的或者准备移至岸桥进行装卸的AGV数量;
    
    private int craneState;//装卸桥状态;0-空闲,未被分配，1-已被分配，有任务;-----------------------------------------
    private boolean haveAssigned;//在assigned函数中是否已被分配-----不能在外部调用！！！
    private boolean needToMoveForOtherQC;//是否因为其他岸桥的移动而移动;

    //构造方法;
    public QuayCrane(String name,int type,String version,Point2D pLoc,Port port){
        this.name = name;
        this.port = port;
        this.numOfAGVPerQC = InputParameters.getNumOfAGVPerQC();
        this.serviceAGVs = new AGV[this.numOfAGVPerQC];
        this.QCType = type;
        this.setParameter(version);
        this.serviceShip = null;
        this.finished_load_20FTassignment = 0;
        this.finished_load_40FTassignment = 0;
        this.finished_unload_20FTassignment = 0;
        this.finished_unload_40FTassignment = 0;
        this.craneState = 0;
        this.haveAssigned = false;
        this.location = new Point2D.Double(pLoc.getX(),pLoc.getY());
        this.serviceLocation = null;
        this.exchangePlatformX = this.location.getX()+this.gauge-InputParameters.getWidthOfContainer()-2;
        this.mainTrolleyX = this.location.getX()-0.5*this.outReach;
        this.gantryTrolleyX = this.location.getX()+this.gauge+this.innerReach;
        this.mainTrolleyHaveContainer = false;
        this.gantryTrolleyHaveContainer = false;
        this.mainTrolleyFree = true;
        this.gantryTrolleyFree =true;
        this.NumOnExchangePlatform = 0;//中转平台上箱量;(40ft/1单位)
        this.MaxOnExchangePlatform = 2;//中转平台上最大允许箱量;(40ft/1单位)
        this.laneIsBookedOrOccupied = new int[InputParameters.getMaxPresentWorkingAGV()];
        for(int i = 0;i<this.laneIsBookedOrOccupied.length;i++){
            this.laneIsBookedOrOccupied[i] = 0;
        }
        this.QCWork = new QuayCraneWork[2];
        this.needToMoveForOtherQC = false;
    }
    //读取文件,设置参数,并存储QC信息至output中;
    public boolean setParameter(String version){
        this.QCVersion = version;
        String fileName = INPUTPATH+"QC_"+version+".txt";//-------------sometimes need to change the path-----------------  
        //读取文件;
        File file = new File(fileName);
        Queue<String> que1 = new ArrayDeque<>();
        if(file.exists()){
            if(file.isFile()){
                try{
                    BufferedReader input = new BufferedReader(new FileReader(file));
                    String text;
                    while((text = input.readLine())!=null){//读一行字符;
                        text = input.readLine();//两行一个变量;
                        que1.offer(text);
                    }
                }catch(IOException ioException){
                System.err.println("File Error!");
                }
            }else{
            System.out.println("File Does not exist!");
            }  
        }
        this.liftingHeight = Double.parseDouble(que1.poll());
        this.outReach = Double.parseDouble(que1.poll());
        this.innerReach = Double.parseDouble(que1.poll());
        this.gauge = Double.parseDouble(que1.poll());
        this.moveSpeed_Crane = Double.parseDouble(que1.poll());
        this.efficiencyPerHour = Double.parseDouble(que1.poll());
        this.efficiencyPerHour = 80;
        this.mainTrolleyVerticalSpeed = 180;//主小车起升速度;
        this.gantryTrolleyVerticalSpeed = 180;//门架小车起升速度;
        this.mainTrolleySpeed = 45;//主小车移动速度;
        this.gantryTrolleySpeed = 240;//门架小车移动速度;
        
        double[] parameters = {this.liftingHeight,this.outReach,this.innerReach,this.gauge, this.getMoveSpeed_Crane(),this.efficiencyPerHour};
        outputSingleParameters("QuayCraneInput",parameters);
        que1.clear();
        return true;
    }
    /**
     * @return the location
     */
    public Point2D getLocation() {
        return location;
    }
    /**
     * 装卸桥状态
     * 0-空闲,未被分配，1-已被分配，有任务;-----
     * @return the craneState
     */
    public int getCraneState() {
        return craneState;
    }
    /**
     * @param loc
     */
    public void setLocation(Point2D loc) {
        this.location.setLocation(loc);
    }
    public void setLocation(double x, double y){
        this.getLocation().setLocation(x, y);
    }

    /**
     * 装卸桥状态;0-空闲,未被分配;1-已被分配，有任务
     * @param craneState the craneState to set
     */
    public void setCraneState(int craneState) {
        this.craneState = craneState;
    }

    /**
     * @return the serviceShip
     */
    public Ship getServiceShip() {
        return this.serviceShip;
    }

    /**
     * 设置服务船舶，若船舶改变，则重置装卸任务量，若船舶不变，则不变化;
     * @param serviceship the servicehip to set
     */
    public void setServiceShip(Ship serviceship) {
        if(serviceship == null){
            this.serviceShip = null;
            this.serviceLocation = null;
            this.NumOnExchangePlatform = 0;
            this.clearFinished();
        }else if(this.serviceShip == null || this.serviceShip.toString().equals(serviceship.toString())==false){
            this.serviceShip = serviceship;
            this.clearFinished();
        }
    }
    public void setShipLeave()
    {
        this.outputWorkThisShip();//储存该ship的装卸信息;
        this.gantryTrolleyFree = true;
        this.mainTrolleyFree = true;
        this.NumOnExchangePlatform = 0;
        this.setServiceShip(null);
        this.getQCWork()[0].setShip(null);
        this.getQCWork()[1].setShip(null);
        this.setCraneState(0);
    }
    

    /**
     * 储存QC服务于该ship时的装卸量信息;
     */
    public void outputWorkThisShip(){
        Queue<String> que = new ArrayDeque<>();
        que.add(this.getName());
        que.add("finished_load_20FTassignment\t"+Integer.toString((int)this.finished_load_20FTassignment));
        que.add("finished_load_40FTassignment\t"+Integer.toString((int)this.finished_load_40FTassignment));
        que.add("finished_unload_20FTassignment\t"+Integer.toString((int)this.finished_unload_20FTassignment));
        que.add("finished_unload_40FTassignment\t"+Integer.toString((int)this.finished_unload_40FTassignment));
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+"Ship/ship"+this.serviceShip.getNumber()+"/qcWork.txt");
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
     * @return the finished_load_assignment
     */
    public double getFinished_load_20FTassignment() {
        return finished_load_20FTassignment;
    }
    
    /**
     * @return the finished_load_assignment
     */
    public double getFinished_load_40FTassignment() {
        return finished_load_40FTassignment;
    }

    /**
     * @return the finished_unload_20FTassignment
     */
    public double getFinished_unload_20FTassignment() {
        return finished_unload_20FTassignment;
    }
      /**
     * @return the finished_unload_40FTassignment
     */
    public double getFinished_unload_40FTassignment() {
        return finished_unload_40FTassignment;
    }

    
    /**
     * @return the haveAssigned
     */
    public boolean isHaveAssigned() {
        return haveAssigned;
    }

    /**
     * @param haveAssigned the haveAssigned to set
     */
    public void setHaveAssigned(boolean haveAssigned) {
        this.haveAssigned = haveAssigned;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * 提取name中的数字;
     */
    public double getNumber()
    {
        double number; 
        String str = this.getName();
        str=str.trim();
        String str2 = "";
        for(int i=0;i<str.length();i++){
            if(str.charAt(i)>=48 && str.charAt(i)<=57){
                str2+=str.charAt(i);
            }
        }
        number = Integer.parseInt(str2);
        return number;
    }

    /**
     * @return the serviceLocation
     */
    public Point2D getServiceLocation() {
        return serviceLocation;
    }
    /**
     * @param servicelocation the serviceLocation to set
     */
    public void setServiceLocation(Point2D servicelocation) {
        this.serviceLocation.setLocation(servicelocation);
    }
    /**
     * 类型：双小车-2,单小车-1;
     * @return the QCType
     */
    public int getQCType() {
        return QCType;
    }

    /**
     * @return the mainTrolleyFree
     */
    public boolean isMainTrolleyFree() {
        return mainTrolleyFree;
    }

    /**
     * @return the gantryTrolleyFree
     */
    public boolean isGantryTrolleyFree() {
        return gantryTrolleyFree;
    }

    /**
     * @param mainTrolleyFree the mainTrolleyFree to set
     */
    public void setMainTrolleyFree(boolean mainTrolleyFree) {
        this.mainTrolleyFree = mainTrolleyFree;
    }

    /**
     * @param gantryTrolleyFree the gantryTrolleyFree to set
     */
    public void setGantryTrolleyFree(boolean gantryTrolleyFree) {
        this.gantryTrolleyFree = gantryTrolleyFree;
    }

    /**
     * @return the NumOnExchangePlatform
     */
    public double getNumOnExchangePlatform() {
        return NumOnExchangePlatform;
    }

    /**
     * @return the MaxOnExchangePlatform
     */
    public double getMaxOnExchangePlatform() {
        return MaxOnExchangePlatform;
    }

    /**
     * @param addnum +：加；-：减
     */
    public void setNumOnExchangePlatform(double addnum) {
        this.NumOnExchangePlatform += addnum;
//        if(this.NumOnExchangePlatform<0){
//            System.out.println(this.toString()+"!!!!!!!!!!!!!!!setNumOnExchangePlatform!!!!!!!!!!!!!!!");
//            System.out.println(this.toString()+"!!!!!!!!!!!!!!!setNumOnExchangePlatform!!!!!!!!!!!!!!!"+
//                    NumOnExchangePlatform);
//           // throw new UnsupportedOperationException("!!!!! AGV.getContainerType Not supported yet.");
//        }
    }
    @Override
    public String toString(){
        return this.getName();
        
    }

    /**
     * @param num 
     * result +=num;
     */
    public void setFinished_load_20FTassignment(double num) {
        this.finished_load_20FTassignment += num;
    }
    
    /**
     * @param num 
     * result +=num;
     */
    public void setFinished_load_40FTassignment(double num) {
        this.finished_load_40FTassignment += num;
    }

    /**
     * @param num
     * result +=num;
     */
    public void setFinished_unload_20FTassignment(double num) {
        this.finished_unload_20FTassignment += num;
    }
    
    /**
     * @param num
     * result +=num;
     */
    public void setFinished_unload_40FTassignment(double num) {
        this.finished_unload_40FTassignment += num;
    }


    public void setServiceLocationY(double y) {
        this.serviceLocation = new Point2D.Double(0, 0);
        this.serviceLocation.setLocation(this.location.getX(), y);
    }
    
    private double pathLengthtoServiceLocation(){
        double length = 0;
        length = Math.abs(this.location.getY()-this.serviceLocation.getY());
        return length;
    }
    
    /**
     * 单位:double m
     * @param startY
     * @param endY
     * @return m
     */
    public double pathLength(double startY,double endY){
        return Math.abs(endY-startY); 
    }
    /**
     * @param length
     * @return double miniute
     */
    public double calculateNeededMoveTime(double length){
        return length/(this.getMoveSpeed_Crane()*60);
    }

    public Port getPort() {
        return this.port;
    }

    public void addQuayCraneWork(QuayCraneWork aThis) {
        if(aThis.getTrolleyType() == 1){
            this.QCWork[0] = aThis;//主小车
        }else{
            this.QCWork[1] = aThis;//门架小车
        }
    }
    /**
     * 设置QuayCrane对应的服务AGV;
     * @param agv:该QC对应的一个AGV
     * @param i
     */
    public void addServiceAGV(AGV agv,int i){ 
        this.serviceAGVs[i] = agv;
    }

    public AGV[] getServiceAGV() {
        return this.serviceAGVs;
    }

    private void clearFinished() {
        this.finished_load_20FTassignment = 0;
        this.finished_load_40FTassignment = 0;
        this.finished_unload_20FTassignment = 0;
        this.finished_unload_40FTassignment = 0;
    }

    /**
     * @return the innerReach
     */
    public double getInnerReach() {
        return innerReach;
    }

    /**
     * @return the outReach
     */
    public double getOutReach() {
        return outReach;
    }

    /**
     * @return the gauge
     */
    public double getGauge() {
        return gauge;
    }

    /**
     * 判断左侧相邻岸桥是否需要移动;
     * 若空闲，且安全距离不满足要求则移动;
     */
    public void moveLeftQC() {
        if(this.getNumber() != 0 && (this.serviceLocation.getY()-port.getQuayCranes()
                [(int)(getNumber()-1)].getLocation().getY())<MinDistanceofQC &&
                (port.getQuayCranes()[(int)(getNumber()-1)].getCraneState() == 0)){
            //该岸桥左边的岸桥空闲且需要移动;
            this.port.getQuayCranes()[(int)(this.getNumber()-1)].setNeedToMoveForOtherQC(true); 
            this.port.getQuayCranes()[(int)(this.getNumber()-1)].
                    setServiceLocationY(this.serviceLocation.getY()-MinDistanceofQC);
            this.port.getQuayCranes()[(int)(this.getNumber()-1)].moveLeftQC();//依次移动左侧岸桥;
        }
    }

    /**
     * 判断右侧相邻岸桥是否需要移动;
     * 若空闲且安全距离不满足要求则移动;
     */
    public void moveRightQC() {
        if(this.getNumber() != port.getQuayCranes().length-1 && 
                (port.getQuayCranes()[(int)(getNumber()+1)].getLocation().getY()-this.serviceLocation.getY())
                <MinDistanceofQC && port.getQuayCranes()[(int)(getNumber()+1)].getCraneState() == 0 ){
            //该岸桥右边的岸桥空闲且需要移动;
            this.port.getQuayCranes()[(int)(this.getNumber()+1)].setNeedToMoveForOtherQC(true); 
            this.port.getQuayCranes()[(int)(this.getNumber()+1)].
                    setServiceLocationY(this.serviceLocation.getY()+MinDistanceofQC);
            this.port.getQuayCranes()[(int)(this.getNumber()+1)].moveRightQC();//依次移动右侧岸桥;
        }
        
    }

    /**
     * @return the needToMoveForOtherQC
     */
    public boolean isNeedToMoveForOtherQC() {
        return needToMoveForOtherQC;
    }

    /**
     * @param needToMoveForOtherQC the needToMoveForOtherQC to set
     */
    public void setNeedToMoveForOtherQC(boolean needToMoveForOtherQC) {
        this.needToMoveForOtherQC = needToMoveForOtherQC;
    }

    /**
     * @return the QCWork
     */
    public QuayCraneWork[] getQCWork() {
        return QCWork;
    }
    /**
     * @param laneNum
     * @param setState 0:解除引用 1：占用位子
     */
    public void setLaneIsBookedOrOccupied(int laneNum, int setState){
        if (laneNum < 1 || laneNum > this.getLaneIsBookedOrOccupied().length) {
            System.out.println("!!!!!!!!!!Error:laneNum:"+laneNum);
            throw new UnsupportedOperationException("setLaneIsBookedOrOccupied"); 
        }
        this.laneIsBookedOrOccupied[laneNum-1] = setState;
    }

    public int getPresentWorkingOrToWorkAGVNum() {
        int num = 0;
        for(int i = 0;i<this.getLaneIsBookedOrOccupied().length;i++){
            if(this.getLaneIsBookedOrOccupied()[i] == 1){
                num++;
            }
        }
        return num;
    }

    /**
     * @return the laneIsBookedOrOccupied
     */
    public int[] getLaneIsBookedOrOccupied() {
        return laneIsBookedOrOccupied;
    }

    /**
     * 岸桥在轨道上水平移动速度;
     * m/s
     * @return m/s moveSpeed_Crane
     */
    public double getMoveSpeed_Crane() {
        return moveSpeed_Crane;
    }

    /**
     * @return the mainTrolleyX
     */
    public double getMainTrolleyX() {
        return mainTrolleyX;
    }

    /**
     * @return the gantryTrolleyX
     */
    public double getGantryTrolleyX() {
        return gantryTrolleyX;
    }

    /**
     * @param mainTrolleyX the mainTrolleyX to set
     */
    public void setMainTrolleyX(double mainTrolleyX) {
        this.mainTrolleyX = mainTrolleyX;
    }

    /**
     * @param gantryTrolleyX the gantryTrolleyX to set
     */
    public void setGantryTrolleyX(double gantryTrolleyX) {
        this.gantryTrolleyX = gantryTrolleyX;
    }

    /**
     * @return the averageVerticalMove
     */
    public double getAverageVerticalMove() {
        return averageVerticalMove;
    }

    /**
     * @return the mainTrolleySpeed
     */
    public double getMainTrolleySpeed() {
        return mainTrolleySpeed;
    }

    /**
     * @return the gantryTrolleySpeed
     */
    public double getGantryTrolleySpeed() {
        return gantryTrolleySpeed;
    }

    /**
     * @return the mainTrolleyVerticalSpeed
     */
    public double getMainTrolleyVerticalSpeed() {
        return mainTrolleyVerticalSpeed;
    }

    /**
     * @return the gantryTrolleyVerticalSpeed
     */
    public double getGantryTrolleyVerticalSpeed() {
        return gantryTrolleyVerticalSpeed;
    }

    /**
     * @return the exchangePlatformX
     */
    public double getExchangePlatformX() {
        return exchangePlatformX;
    }

    /**
     * @return the mainTrolleyHaveContainer
     */
    public boolean isMainTrolleyHaveContainer() {
        return mainTrolleyHaveContainer;
    }

    /**
     * @return the gantryTrolleyHaveContainer
     */
    public boolean isGantryTrolleyHaveContainer() {
        return gantryTrolleyHaveContainer;
    }

    /**
     * @param mainTrolleyHaveContainer the mainTrolleyHaveContainer to set
     */
    public void setMainTrolleyHaveContainer(boolean mainTrolleyHaveContainer) {
        this.mainTrolleyHaveContainer = mainTrolleyHaveContainer;
    }

    /**
     * @param gantryTrolleyHaveContainer the gantryTrolleyHaveContainer to set
     */
    public void setGantryTrolleyHaveContainer(boolean gantryTrolleyHaveContainer) {
        this.gantryTrolleyHaveContainer = gantryTrolleyHaveContainer;
    }

            
    
}
