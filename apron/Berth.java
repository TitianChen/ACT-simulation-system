/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package apron;

import CYT_model.Point2D;
import ship.Ship;
import parameter.InputParameters;

/**
 * Class Berth泊位类
 * @author YutingChen, yuting.chen17@imperial.ac.uk
 * Dalian University of Technology
 */
public final class Berth{
    private final String name;
    private final Berth myself;
//berthState---/-1:已被指定泊位，等待靠泊;0：船舶在泊,等待装卸;1：在泊，正在装卸;2:船舶在泊，出于准结束状态;
//berthState---3:船舶离开,但暂时禁止其他船舶进入本船靠泊的泊位;4:船舶离开,其他船舶可以靠泊;  
    private int berthState;//初始时默认为4;
    private int berthType;//泊位类型;大型泊位--1；中型泊位--2；小型泊位--3；未定义--0；
    private boolean loadingRequirenments;//当前是否满足装卸要求;--------先默认为始终满足，到时候再改成随机？？？
    private Point2D location;//泊位中点所在位置;//垂直于前沿：X=0; 平行于前沿：Y;
    private double length;//泊位长度;
    private Ship ship;//所服务的船舶信息;
    public Berth()
    {
        this.name = "Unknown";
        this.myself = this;
        this.berthState = 4;
        this.berthType = 0;
        this.loadingRequirenments = true;
    }
    public Berth(String Berthname,int berthtype,Point2D pLocation){
        this.name = Berthname;
        this.myself = this;
        this.berthState = 4;
        this.berthType = berthtype;
        this.length = InputParameters.getBerthLength(this.myself);
        this.loadingRequirenments = true;
        this.location = new Point2D.Double(pLocation.getX(),pLocation.getY());
    }
    public void setShip(Ship ship){
        this.ship = ship;//浅复制-----------------------------------------------
    }
    /**
     * @return -1:指定泊位，等待靠泊;0：船舶在泊,等待装卸;1：在泊，正在装卸;2:船舶在泊，出于准结束状态;
     * 3:船舶离开,但禁止其他船舶进入本船靠泊的泊位;4:船舶离开,其他船舶可以靠泊;
     */
    public int getBerthState() {
        return berthState;
    }
    /**
     * @param berthstate the berthState to set
     */
    public void setBerthState(int berthstate) {
        this.berthState = berthstate;
//System.out.println("***泊位"+this.getName()+"状态改变为"+berthstate);
    }
    /**
     * @return the loadingRequirenments
     */
    public boolean isLoadingRequirenments() {
        return loadingRequirenments;
    }
    /**
     * @param loadingRequirenments the loadingRequirenments to set
     */
    public void setLoadingRequirenments(boolean loadingRequirenments) {
        this.loadingRequirenments = loadingRequirenments;
    }
    /**
     * @return the berthType
     */
    public int getBerthType() {
        return berthType;
    }
    /**
     * @param berthType the berthType to set
     */
    public void setBerthType(int berthType) {
        this.berthType = berthType;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the location
     */
    public Point2D getLocation() {
        return location;
    }

    /**
     * @param locat
     */
    public void setLocation(Point2D locat) {
        this.location.setLocation(locat);
    }
    
    public void setLocationY(double y) {
        this.location.setLocation(this.location.getX(),y);
    }

    /**
     * @return the ship
     */
    public Ship getShip() {
        return ship;
    }
    /**
     * ship完全离开,berth参数重置;
     */
    public void setShipLeave(){
        this.ship = null;
        this.setBerthState(4);
    }
    /**
     * 提取name中的数字;
     * @return 如 0~7
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
     * @return the length
     */
    public double getLength() {
        return length;
    }

    /**
     * @param length the length to set
     */
    public void setLength(double length) {
        this.length = length;
    }
}
        
    
