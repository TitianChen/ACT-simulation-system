/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package CYT_model;

import apron.QuayCrane;
import Vehicle.AGV;
import storageblock.YardCrane;
import parameter.StaticName;
import storageblock.Slot;
import storageblock.Block;
import parameter.InputParameters;
import static parameter.OutputParameters.simulationRealTime;
import ship.Ship;

/**
 * @author YutingChen, yuting.chen17@imperial.ac.uk
 * Dalian University of Technology
 */
public class Container {

    public final double size;//尺寸：40ft、20ft;
    private String state;//Container状态;
    private AGV serviceAGV;//当前为之服务的AGV;
    private QuayCrane serviceQC;//当前为止服务的QC;
    private YardCrane serviceYC;//当前为之服务的YC;
    private final Ship formalShip;//之前卸船的船舶;
    private Ship goalShip;//若为待装船集装箱式,将要装上的船舶;
    public Slot presentSlot;//若在箱区内，则表示当前箱位;
    private double time_StartInBlock;//开始到箱区的时间;
    private double time_LeavingBlock;//离开堆场的时间;
    
    /**
     * 初始化在港外的待装船的集装箱;
     * @param size
     * @param state 
     */
    public Container(String size,String state){
        switch(size){
            case StaticName.SIZE20FT:
                this.size = 20;
                break;
            case StaticName.SIZE40FT:
                this.size = 40;
                break;
            default:
                throw new UnsupportedOperationException("Container.构造函数 switch() 出错！！");
        }
        this.state = state; 
        this.serviceAGV = null;
        this.serviceQC = null;
        this.serviceYC = null;
        this.formalShip = null;
        this.goalShip = null;
        this.presentSlot = null;
        this.time_StartInBlock = -1;//初始化-1;
        this.time_LeavingBlock = -1;//初始化-1
    }
    /**
     * 初始化在堆场内的Containers
     * @param size
     * @param state
     * @param section 
     */
    public Container(String size,String state,Block section){
        switch(size){
            case StaticName.SIZE20FT:
                this.size = 20;
                break;
            case StaticName.SIZE40FT:
                this.size = 40;
                break;
            default:
                throw new UnsupportedOperationException("Container.构造函数 switch() 出错！！");
        }
        this.state = state; 
        this.serviceAGV = null;
        this.serviceQC = null;
        this.formalShip = null;
        this.goalShip = null;
        this.presentSlot =null;
        this.presentSlot = null;
        this.time_StartInBlock = -1;//初始化-1;
        this.time_LeavingBlock = -1;//初始化-1
//        System.out.println("------------------------containers构造成功");
    }
    /**
     * 初始化在船上的集装箱;
     * @param size
     * @param state
     * @param formalShip 
     */
    public Container(String size,String state,Ship formalShip){
        switch(size){
            case StaticName.SIZE20FT:
                this.size = 20;
                break;
            case StaticName.SIZE40FT:
                this.size = 40;
                break;
            default:
                throw new UnsupportedOperationException("Container.构造函数 switch() 出错！！");
        }
        this.state = state; 
        this.serviceAGV = null;
        this.serviceQC = null;
        this.formalShip = formalShip;    
        this.goalShip = null;
        this.presentSlot = null;
        this.time_StartInBlock = -1;//初始化-1;
        this.time_LeavingBlock = -1;//初始化-1
    }
    /**
     * @return 40ft-return 40; 20ft-return 20; 
     */
    public double getFTsize() {
        return this.size;
    }

    /**
     * 设置集装箱状态;
     * @param state
     * ONAGV:在AGV上
     * OutSide:在港外
     * OnTruck:在集卡上
     * OnStocking：到箱区内
     * OnShip:在船上
     * OnQC:在岸桥;
     * OnYC:在场桥;
     * OnAGVCouple:在AGV伴侣上;
     */
    public void setState(final String state) {
        switch(state){
            case StaticName.ONAGV:
                this.state = StaticName.ONAGV;
                break;
            case StaticName.ONSTOCKING:
                this.state = StaticName.ONSTOCKING;
                break;
            case StaticName.ONSHIP:
                this.state = StaticName.ONSHIP;
                break;
            case StaticName.ONQC:
                this.state = StaticName.ONQC;
                break;
            case StaticName.ONYC:
                this.state = StaticName.ONYC;
                break;
            case StaticName.ONAGVCOUPLE:
                this.state = StaticName.ONAGVCOUPLE;
                break;
            case StaticName.OUTSIDE:
                this.state = StaticName.OUTSIDE;
                break;
            case StaticName.ONTRUCK:
                this.state = StaticName.ONTRUCK;
                break;
            default:
                throw new UnsupportedOperationException("!!!!Error:Container.setState() wrong!!!");
        }
    }

    public void setServiceAGV(AGV thisAGV) {
        this.serviceAGV  = thisAGV;
    }

    /**
     * return 集装箱状态;
     * ONAGV:在AGV上
     * OnStocking：在堆场箱区内
     * OnAGVCouple：在AGV伴侣上
     * OnShip:在船上
     * OnQC:在岸桥;
     * OnYC:在场桥;
     * OutSide:在港外;
     * Ontruck:在集卡上;
     * @return this.state;
     */
    public String getState() {
        return state;
    }

    /**
     * 没有分配ServiceAGV时为null;
     * 结束该轮后serviceAGV也变为null;
     * @return the serviceAGV
     */
    public AGV getServiceAGV() {
        if(this.serviceAGV != null){
            return serviceAGV;
        }else{
            return null;
        }
        
    }
    /**
     * @return the formalShip
     */
    public Ship getFormalShip() {
        return formalShip;
    }
    /**
     * @return this.serviceQC
     */
    public QuayCrane getServiceQC() {
        if(this.serviceQC != null){
            return this.serviceQC;
        }else{
            System.out.println("!!!!!Error:AGV.getServiceQC()!!!!!!");
            throw new UnsupportedOperationException("Error:AGV.getServiceQC()"); 
        }
    }
    /**
     * @param serviceQC the serviceQC to set
     */
    public void setServiceQC(QuayCrane serviceQC) {
        if(this.serviceQC == null){
            this.serviceQC = serviceQC;
        }else if(serviceQC == null){
            this.serviceQC = null;
        }else if(this.serviceQC.toString().equals(serviceQC.toString())==true){
        }else{
            System.out.println("UnsupportedOperationException---Error:AGV.setServiceQC");
            throw new UnsupportedOperationException("Error:AGV.setServiceQC"); 
        }
    }

    /**
     * @return the serviceYC
     */
    public YardCrane getServiceYC() {
        if(this.serviceYC != null){
            return serviceYC;
        }else{
            System.out.println("container getServiceYC == null!!!");
            return null;
        }
    }

    /**
     * @param serviceYC the serviceYC to set
     */
    public void setServiceYC(YardCrane serviceYC) {
        if(this.serviceYC == null){
            this.serviceYC = serviceYC;
        }else if(serviceYC == null){
            this.serviceYC = null;
        }else{
            throw new UnsupportedOperationException("Error:Container.setServiceYC"); 
        }
    }

    /**
     * @return the presentSlot
     */
    public Slot getPresentSlot() {
        if(this.presentSlot == null){
//System.out.println("!!!ATTENTION!!!Container.getPresentSlot()_NULL!!!!");
            return null;
        }
        return presentSlot;
    }

    /**
     * @param presentSlot the presentSlot to set
     */
    public void setPresentSlot(Slot presentSlot) {
        if (presentSlot != null) {
            this.presentSlot = new Slot(presentSlot);
        } else {
            this.presentSlot = null;
        }
    }

    /**
     * @return the goalShip
     */
    public Ship getGoalShip() {
        return goalShip;
    }

    /**
     * @param goalShip the goalShip to set
     */
    public void setGoalShip(Ship goalShip) {
        this.goalShip = goalShip;
    }

    /**
     * @param time the time_StartInBlock to set
     */
    public void setTime_StartInBlock(double time) {
        this.time_StartInBlock = time;
    }

    /**
     * @param time_LeavingBlock the time_LeavingBlock to set
     */
    public void setTime_LeavingBlock(double time_LeavingBlock) {
        this.time_LeavingBlock = time_LeavingBlock;
    }
    /**
     * 依据分布规则确定该箱离港列表生成时间;
     * @param distributionRule
     */
    public void setTime_LeavingBlock(String distributionRule) {
        if(this.getTime_StartInBlock() < 0){
            this.setTime_StartInBlock(simulationRealTime);
        }
        double a = this.getTime_StartInBlock()+InputParameters.getEarliestTimeOut();
        double c = a + (Math.abs(InputParameters.getEarliestTimeOut()-InputParameters.getLatestTimeOut()));//结束集港时间;
        double b = (c-a)*InputParameters.getTriangleDist_mode()+a;//顶点
        if(this.getTime_StartInBlock() == -1){
            System.out.println("Error:setTime_LeavingBlock(String distributionRule)");
            throw new UnsupportedOperationException("!!!Error:setTime_LeavingBlock(String distributionRule)!!!");
        }
        this.time_LeavingBlock = (int)InputParameters.getRandomTriangle(StaticName.TRIANGULAR, a, b, c); 
    }

    /**
     * @return the time_StartInBlock
     */
    public double getTime_StartInBlock() {
        return time_StartInBlock;
    }

    /**
     * @return the time_LeavingBlock
     */
    public double getTime_LeavingBlock() {
        return time_LeavingBlock;
    }
}
