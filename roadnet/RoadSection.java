/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roadnet;
import CYT_model.Point2D;
import Vehicle.AGV;
import parameter.InputParameters;
import static parameter.InputParameters.getLanesNumOfAGV;
import static parameter.InputParameters.getSingleRoadWidth;
import parameter.StaticName;
import static parameter.StaticName.AGVCAR;
import static parameter.StaticName.TRUCKCAR;
/**
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 */
//供集卡，AGV行驶的区域
public final class RoadSection{
    //变量
    private final Point2D[] edge = new Point2D[4];//[垂直于前沿--x][平行于前沿--y]道路端点;顺序：side1_p1&p2,side2_p1&p2;
    private final double width,length;//道路宽度;长度
    private double radius=0;//对于圆弧段，有该属性;
    private final int carType;//AGV行驶车道-1,集卡行驶车道—2;
    private final int roadType;//行驶方向：垂直于前沿--1，平行于前沿--2，转弯圆弧--3，丁字路口--4，十字路口--5;
    private final int laneNum;//车道数    
    private final String roadNum;//Section编号;储存于图中用于标记;
    private final String side1roadNum;
    private final String side2roadNum;
    private final String funcAreaNum;
    private final double speed;//该路段默认速度;
    
    //构造方法
    public RoadSection(Point2D[] point,int roadtype,String cartype,int lanenum,String roadnum,
            String side1num,String side2num,String funcareanum){
        //构造内容：Scetion端点坐标,roadType,carType,laneNum,No.road,.....
        //丁字路口，十字路口在此处不作考虑;
        System.arraycopy(point, 0, this.edge, 0, 4);
        
        if(roadtype == 3){
            this.radius  = InputParameters.getTurningRadiusOfCar()+(-0.5+0.5*getLanesNumOfAGV())*getSingleRoadWidth();
            this.width = this.edge[0].distance(edge[1]);
            this.length = Math.max(this.edge[0].distance(edge[2]),this.edge[0].distance(edge[3]));
        }else{
            this.length = this.edge[0].distance(edge[2]);
            this.width = this.edge[0].distance(edge[1]);
        }
        switch(cartype){
            case AGVCAR:
                this.carType = 1;
                break;
            case TRUCKCAR:
                this.carType = 2;
                break;
            default:
                this.carType = -1;         
        }
        this.roadType = roadtype;
        this.laneNum = lanenum;
        this.roadNum = roadnum; 
        this.side1roadNum = side1num;
        this.side2roadNum = side2num;
        this.funcAreaNum = funcareanum;
        this.speed = 2;///////////改为与roadType/carType有关的值,数组放在输入参数中;
    }    
    //参数调用方法
    public double getWidth(){
        return this.width;
    }
    public double getLength(){
        return this.length;
    }
            
    public int getcarType(){
        return this.carType;
    }
    /**
     * 行驶方向：垂直于前沿--1，平行于前沿--2，转弯圆弧--3，丁字路口--4，十字路口--5;
     * @return 
     */
    public int getroadType(){
        return this.roadType;
    }
    public int getlaneNum(){
        return this.laneNum;
    }
    public String getroadNum(){
        return this.roadNum;
    }
    public Point2D[] getedge(){
        return this.edge;
    }
    public double getspeed(){
        return this.speed;
    }
    public Object getside1roadNum(){
        return this.side1roadNum;
    }
    public Object getside2roadNum(){
        return this.side2roadNum;
    }
    public Object getfuncAreaNum()
    {
        return this.funcAreaNum;
    }
    //计算该道路上两点之间的行驶距离;用于叠加计算小车行驶距离
    public double PathLengthOnThisRoad(Point2D startpoint,Point2D endpoint){
        Point2D sP = new Point2D.Double(startpoint.getX(), startpoint.getY());
        Point2D eP = new Point2D.Double(endpoint.getX(), endpoint.getY());
        double l = 0;
        switch (roadType){
        //直线段计算方法;
            case 1:
            case 2:
                if(this.roadNum.equals(StaticName.ROADUNDERQC)){
                    return Math.abs(startpoint.getX()-endpoint.getX())+Math.abs(startpoint.getY()-endpoint.getY());
                }
                Point2D centralP1 = this.calculateCentralAxis(sP);
                if(this.roadNum.equals("Road8_Yard2AGVTransfer") || this.roadNum.equals("Road2_Yard1AGVTransfer")){
                    if(startpoint.distance(endpoint)>0.5*19+1){
                        System.out.println("!!!!!!Error:RoadSection.PathLengthOnThisRoad(AGVTransferPPP)!!!!!!");
                        throw new UnsupportedOperationException("!!!!!!Error:AGV.obtainContainersFromQC(AGVTransferPP)!!!!!!"); 
                    }
                    return startpoint.distance(endpoint);
                }
                if(centralP1.distance(sP) != 0){
                    l += centralP1.distance(sP);//边界到中轴线的距离;
                    //System.out.println("_centralP1_:"+centralP1+"l:"+l);
                    sP.setLocation(centralP1);//改点; 
                }
                Point2D centralP2 = this.calculateCentralAxis(eP);
                if(centralP2.distance(eP) != 0){//不在中轴线上，即终点在边界上;
                    l += centralP2.distance(eP);//边界到中轴线的距离;
                    //System.out.println("_centralP2_:"+centralP2+"l:"+l);
                    eP.setLocation(centralP2);//改点;
                }
                l += sP.distance(eP);
                return l;
            case 3://圆弧段,默认起始点均在中轴线上;
                l = this.getRadius()*Math.PI/2;
                if(this.getRadius() == 0){
                    System.out.println("!!!!!!Error:RoadSection.PathLengthOnThisRoad()!!!!!!");
                    throw new UnsupportedOperationException("!!!!!!Error:AGV.obtainContainersFromQC()!!!!!!"); 
                }
                return l;
            default:
                System.out.println("!!!!!!Error:RoadSection.PathLengthOnThisRoad() default!!!!!!");
                throw new UnsupportedOperationException("!!!!!!Error:AGV.obtainContainersFromQC() default!!!!!!");
        }
    }
    //计算时间 using 该路段默认速度
    public double TimeOnThisRoad(Point2D startpoint,Point2D endpoint){
        double time;
        time = this.PathLengthOnThisRoad(startpoint,endpoint)/this.speed;
        return time;
    }
    //计算行驶时间 using 用户自定义速度;
    public double TimeOnThisRoad(Point2D startpoint,Point2D endpoint,double speed0){
        double time;
        time = this.PathLengthOnThisRoad(startpoint,endpoint)/speed0;
        return time;
    }
    //判断该点是否在此路段中;
    public boolean isOnThisRoad(Point2D point){
        boolean itag;
        itag = false;
        switch(this.roadType)
        {
            case 1:
            case 2:
                double minx = Math.min(this.edge[0].getX(),this.edge[3].getX());
                double maxx = Math.max(this.edge[0].getX(),this.edge[3].getX());
                double miny = Math.min(this.edge[0].getY(),this.edge[3].getY());
                double maxy = Math.max(this.edge[0].getY(),this.edge[3].getY());
                if(point.getX() >= minx && point.getX() <= maxx  &&
                        point.getY() >= miny && point.getY() <= maxy){
                    return true;
                }
                break;  
            case 3:
                //圆弧路段;
                //Edge:1,3在内侧，0,2在外侧;
                minx = Math.min(this.edge[0].getX(),this.edge[2].getX());
                maxx = Math.max(this.edge[0].getX(),this.edge[2].getX());
                miny = Math.min(this.edge[0].getY(),this.edge[2].getY());
                maxy = Math.max(this.edge[0].getY(),this.edge[2].getY());
                if(point.getX() >= minx && point.getX() <= maxx  &&
                        point.getY() >= miny && point.getY() <= maxy){
                    return true;
                }
                break;  
                //throw new ArrayIndexOutOfBoundsException("@!!!!!!!Error:RoadSection.isOnThisRoad"); 
            default:
                System.out.println("roadType不合法");
                throw new ArrayIndexOutOfBoundsException("roadType不合法！");
        }
        return itag;
    }
    public boolean isOnThisRoadEdge(Point2D point){
        switch(this.roadType)
        {
            case 1:
            case 2:
                double minx = Math.min(this.edge[0].getX(),this.edge[3].getX());
                double maxx = Math.max(this.edge[0].getX(),this.edge[3].getX());
                double miny = Math.min(this.edge[0].getY(),this.edge[3].getY());
                double maxy = Math.max(this.edge[0].getY(),this.edge[3].getY());
                if(point.getX() > minx && point.getX() < maxx  &&
                        point.getY() > miny && point.getY() < maxy){
                    System.out.println("RS.isOnThisRoadEdgeEdgeEdge false");
                    return true;
                }else if(this.findNearestPoint(point).distance(point)<0.001){
                    return true;
                }
                break;  
            case 3:
                System.out.println("RS.isOnThisRoadEdge--RoadType:3需要补充");
                throw new ArrayIndexOutOfBoundsException("RS.isOnThisRoadEdge--RoadType:3需要补充");
            default:
                System.out.println("roadType不合法");
                throw new ArrayIndexOutOfBoundsException("roadType不合法！");
        }
        return false;
    }
    //判断点point是否在中轴线上并返回其中轴线坐标(假设在转弯路段，车辆始终处于在车道中轴线上行驶的状态)
    public Point2D calculateCentralAxis(Point2D point){
        Point2D Centralpoint = new Point2D.Double(point.getX(), point.getY());
        if(roadType == 1)//垂直于前沿/x方向
            Centralpoint.setLocation(point.getX(), (edge[1].getY()+edge[0].getY())/2);
        if(roadType == 2)
            Centralpoint.setLocation((edge[1].getX()+edge[0].getX())/2, point.getY());
        return Centralpoint;
    }
    /**
     * 某一侧的中点;
     * @param i
     * @return 
     */
    public Point2D CentralAxis(int i){
        Point2D PP;
        double X,Y;
        if(i == 1){//side1{
            X = (this.getedge()[0].getX()+this.getedge()[1].getX())/2;
            Y = (this.getedge()[0].getY()+this.getedge()[1].getY())/2;
        }else{
            X = (this.getedge()[2].getX()+this.getedge()[3].getX())/2;
            Y = (this.getedge()[2].getY()+this.getedge()[3].getY())/2;
        }
        PP = new Point2D.Double(X, Y);
        return PP;
    }
    public int findeside(String roadnextnum){
        int i = 0;
        if(this.side1roadNum != null && this.side1roadNum.equals(roadnextnum))
            i = 1;
        else if(this.side2roadNum != null && this.side2roadNum.equals(roadnextnum))
            i = 2;
        else{
            System.out.println("findside()出错");
            i = 3;//false;
        }
        return i;
    }

    /**
     * @return the radius
     */
    public double getRadius() {
        return radius;
    }

    public Point2D findNearestPoint(Point2D point) {
        double minx = Math.min(this.getedge()[0].getX(), this.getedge()[3].getX());
        double maxx = Math.max(this.getedge()[0].getX(), this.getedge()[3].getX());
        double miny = Math.min(this.getedge()[0].getY(), this.getedge()[3].getY());
        double maxy = Math.max(this.getedge()[0].getY(), this.getedge()[3].getY());
        double x, y;
        if (point.getX() < minx) {
            x = minx;
        } else if (point.getX() > maxx) {
            x = maxx;
        } else {
            x = point.getX();
        }
        if (point.getY() < miny) {
            y = miny;
        } else if (point.getY() > maxy) {
            y = maxy;
        } else {
            y = point.getY();
        }
        return new Point2D.Double(x, y);
    }
    /**
     * @param startPoint
     * @param endPoint
     * @param agv
     * @param nowLength
     * @return 
     */
    public Point2D calculateNowPoint(Point2D startPoint, Point2D endPoint, AGV agv, double nowLength) {
        Point2D sP = new Point2D.Double(startPoint.getX(), startPoint.getY());
        Point2D eP = new Point2D.Double(endPoint.getX(), endPoint.getY());
        if(this.isOnThisRoad(sP) == false && this.isOnThisRoadEdge(sP) ==false){
            if(this.findNearestPoint(sP).distance(sP)<10){////////10------------要不要改
                if(nowLength<this.findNearestPoint(sP).distance(sP)){
                    if(this.findNearestPoint(sP).getX() == sP.getX()){
                        if(this.findNearestPoint(sP).getY()>sP.getY()){
                            return new Point2D.Double(sP.getX(),sP.getY()+nowLength);
                        }else{
                            return new Point2D.Double(sP.getX(),sP.getY()-nowLength);
                        }
                    }else if(this.findNearestPoint(sP).getY() == sP.getY()){
                        if(this.findNearestPoint(sP).getX()>sP.getX()){
                            return new Point2D.Double(sP.getX()+nowLength,sP.getY());
                        }else{
                            return new Point2D.Double(sP.getX()-nowLength,sP.getY());
                        }
                    }
                }else{
                    return calculateNowPoint(this.findNearestPoint(sP), eP, agv, nowLength-
                            this.findNearestPoint(sP).distance(sP));    
                }
            }else {
                System.out.print("注意!!RS.calculateNowPoint:sP" + sP);
                System.out.println(this.roadNum + this.getedge()[0] + "" + this.getedge()[3]);
                throw new UnsupportedOperationException("!!!!!!Error:RS.RoadSection.calculateNowPoint()!!!!!!");
            }
        }else if(this.isOnThisRoad(eP) == false && this.isOnThisRoadEdge(eP) ==false){
//            System.out.print("注意!!RS.calculateNowPoint:eP"+eP);
//            System.out.println(this.roadNum+this.getedge()[0]+ ""+this.getedge()[3]);
        }
        double l = 0;
        switch (roadType){
        //直线段计算方法;
            //垂直于前沿--1--变X，平行于前沿--2--变Y，转弯圆弧--3;
            case 1:
                //垂直于前沿;
                Point2D centralP1 = this.calculateCentralAxis(sP);
                if(centralP1.distance(sP) != 0){
                    l += centralP1.distance(sP);//边界到中轴线的距离;
                    if(l>=nowLength){
                        if(centralP1.getX() == sP.getX()){
                            if(sP.getY()<=centralP1.getY()){
                                return new Point2D.Double(sP.getX(),sP.getY()+nowLength);
                            }else{
                                return new Point2D.Double(sP.getX(),sP.getY()-nowLength);
                            }
                        }else{
                            System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()centralP1!!!!!!");
                            throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()centralP1!!!!!!");
                        }
                    }else{
                        return this.calculateNowPoint(centralP1, eP, agv, nowLength-l);
                    }
                }
                //起点在中轴线上;
                //判断终点在不在中轴线上;
                Point2D centralP2 = this.calculateCentralAxis(eP);
                if(centralP2.distance(eP) != 0){//起点在中轴线，终点不在中轴线上;
                    l += sP.distance(centralP2);
                    double plus = centralP2.distance(eP);
                    if(l>=nowLength){
                        //nowPoint在中轴线上;
                        if(centralP2.getX() == eP.getX()){
                            if (sP.getX() <= centralP2.getX()) {
                                return new Point2D.Double(sP.getX() + nowLength, sP.getY());
                            } else {
                                return new Point2D.Double(sP.getX() - nowLength, sP.getY());
                            }
                        }else{
                            System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()centralP1!!!!!!");
                            throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()centralP1!!!!!!");
                        }
                    }else if(l+plus>nowLength){
                        //nowPoint在中轴线到终点的路上;
                        if(centralP2.getX() == eP.getX()){
                            if (centralP2.getY() <= eP.getY()) {
                                return new Point2D.Double(eP.getX(), centralP2.getY()+(nowLength-l));
                            } else {
                                return new Point2D.Double(eP.getX(), centralP2.getY()-(nowLength-l));
                            }
                        }else{
                            System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()centralP1!!!!!!");
                            throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()centralP1!!!!!!");
                        }
                    }else{
                        System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()rt-1-else!!!!!!");
                        throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()centralP1!!!!!!");
                    }
                }else{
                    //起点在中轴线，终点在中轴线;
                    l = sP.distance(eP);
                    if(l>=nowLength){
                        //终点在里面;
                        if(sP.getY() == eP.getY()){
                            if(sP.getX()<eP.getX()){
                                return new Point2D.Double(sP.getX()+nowLength,sP.getY());
                            }else{
                                return new Point2D.Double(sP.getX()-nowLength,sP.getY());
                            }
                        }else{
                            System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()起点&终点在中轴线;!!!!!!");
                            System.out.println(sP.getY()+"!!!"+eP.getY());
                            throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()起点&终点在中轴线;!!!!!!");
                        }
                    }else{
                        System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()起点&终点在中轴线 else;!!!!!!");
                        throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()起点&终点在中轴线 else;!!!!!!");
                    }
                }
            case 2:
                if(this.roadNum.equals(StaticName.ROADUNDERQC)){
                    double length1 = Math.abs(sP.getX()-eP.getX());
                    double length2 = Math.abs(sP.getY()-eP.getY());
                    if(length1>=nowLength){
                        if(sP.getX()<eP.getX()){
                            return new Point2D.Double(sP.getX()+nowLength,sP.getY()); 
                        }else{
                            return new Point2D.Double(sP.getX()-nowLength,sP.getY());
                        }
                    }else if(length1+length2>=nowLength){
                        if(sP.getY()<eP.getY()){
                            return new Point2D.Double(eP.getX(),sP.getY()+nowLength-length1);
                        }else{
                            return new Point2D.Double(eP.getX(),sP.getY()-(nowLength-length1));
                        }
                    }else{
                        System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()ROADUNDERQC!!!!!!");
                            throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()ROADUNDERQC!!!!!!");
                    }
                }          
                //平行于前沿的路段;
                centralP1 = this.calculateCentralAxis(sP);
                if(this.roadNum.equals("Road8_Yard2AGVTransfer") || this.roadNum.equals("Road2_Yard1AGVTransfer")){
                    if(sP.distance(eP)>0.5*19+1){
                        System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()!!!!!!");
                        throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()!!!!!!"); 
                    }
                    double x = (sP.getX()+eP.getX())*0.5;
                    if(sP.getY()<eP.getY()){
                        return new Point2D.Double(x,sP.getY()+nowLength);
                    }else{
                        return new Point2D.Double(x,sP.getY()-nowLength);
                    }
                }
                if(centralP1.distance(sP) != 0){
                    l += centralP1.distance(sP);//边界到中轴线的距离;
                    if(l>=nowLength){
                        if(centralP1.getY() == sP.getY()){
                            if(sP.getX()<=centralP1.getX()){
                                return new Point2D.Double(sP.getX()+nowLength,sP.getY());
                            }else{
                                return new Point2D.Double(sP.getX()-nowLength,sP.getY());
                            }
                        }else{
                            System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()centralP1!!!!!!");
                            throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()centralP1!!!!!!");
                        }
                    }else{
                        return this.calculateNowPoint(centralP1, eP, agv, nowLength-l);
                    }
                }
                //起点在中轴线上;
                //再判断终点是否在中轴线上;
                centralP2 = this.calculateCentralAxis(eP);
                if(centralP2.distance(eP) != 0){//起点在中轴线，终点不在中轴线上;
                    double plus = centralP2.distance(eP);
                    l = centralP2.distance(sP);
                    if(l>=nowLength){
                        if(centralP2.getY() == eP.getY()){
                            if(sP.getY() <= centralP2.getY()){
                                return new Point2D.Double(sP.getX(),sP.getY()+nowLength);
                            }else{
                                return new Point2D.Double(sP.getX(),sP.getY()-nowLength);
                            }
                        }else{
                            System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()centralP2!!!!!!");
                            throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()centralP2!!!!!!");
                        }
                    }else if(l+plus>=nowLength){
                        if(centralP2.getY() == eP.getY()){
                            if(centralP2.getX() < eP.getX()){
                                return new Point2D.Double(centralP2.getX()+(nowLength-l),eP.getY());
                            }else{
                                return new Point2D.Double(centralP2.getX()-(nowLength-l),eP.getY());
                            }
                        }else{
                            System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()centralP2!!!!!!");
                            throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()centralP2!!!!!!");
                        }
                    } else {
                        System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()rt2-else!!!!!!");
                        throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()rt2-else!!!!!!");
                    }
                }else{
                    //起点在中轴线，终点在中轴线;
                    l += sP.distance(eP);
                    if(l>=nowLength){
                        if(sP.getX() == eP.getX()){
                            if(sP.getY()<eP.getY()){
                                return new Point2D.Double(sP.getX(),sP.getY()+nowLength);
                            }else{
                                return new Point2D.Double(sP.getX(),sP.getY()-nowLength);
                            }
                        }else if(sP.getY() == eP.getY()){
                            if(sP.getX()<eP.getX()){
                                return new Point2D.Double(sP.getX()+nowLength,sP.getY());
                            }else{
                                return new Point2D.Double(sP.getX()-nowLength,sP.getY());
                            }
                        }else{
                            System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()起点&终点在中轴线;!!!!!!");
                            throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()起点&终点在中轴线;!!!!!!");
                        }
                    }else {
                        System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()起点&终点在中轴线 else;!!!!!!");
                        throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()起点&终点在中轴线 else;!!!!!!");
                    }
                }
            case 3://圆弧段,默认起始点均在中轴线上;
                l = this.getRadius()*(Math.PI/2);
                if(this.getRadius() == 0){
                    System.out.println("!!!!!!Error:RoadSection.PathLengthOnThisRoad()!!!!!!");
                    throw new UnsupportedOperationException("!!!!!!Error:AGV.obtainContainersFromQC()!!!!!!"); 
                }
                if(l>=nowLength){
                    double theta = nowLength/this.getRadius();
                    if(theta>0.00001+Math.PI/2){
                        System.out.println("!!!!!!Error:RoadSection.PathLengthOnThisRoad()Theta:"+theta+
                                " PI/2:"+Math.PI/2);
                        throw new UnsupportedOperationException("!!!!!!Error:AGV.obtainContainersFromQC()!!!!!!"); 
                    }
                    if(sP.getX()>eP.getX() && sP.getY()<eP.getY()){
                        double x = sP.getX()-this.getRadius()*Math.sin(theta);
                        double y = eP.getY()-this.getRadius()*Math.cos(theta);
                        return new Point2D.Double(x,y);
                    }else if(sP.getX()<eP.getX() && sP.getY()>eP.getY()){
                        double x = eP.getX()-this.getRadius()*Math.cos(theta);
                        double y = sP.getY()-this.getRadius()*Math.sin(theta);
                        return new Point2D.Double(x,y);
                    }else if(sP.getX()<eP.getX() && sP.getY() < eP.getY()){
                        double x = eP.getX()-this.getRadius()*Math.cos(theta);
                        double y = sP.getY()+this.getRadius()*Math.sin(theta);
                        return new Point2D.Double(x,y);
                    }else if(sP.getX()>eP.getX() && sP.getY()>eP.getY()){
                        double x = sP.getX()-this.getRadius()*Math.sin(theta);
                        double y = eP.getY()+this.getRadius()*Math.cos(theta);
                        return new Point2D.Double(x,y);
                    }else{
                        return sP;
                    }
                }else{
                    System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()圆弧段!!!!!!");
                    throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()圆弧段!!!!!!");
                }
            default:
                System.out.println("!!!!!!Error:RoadSection.calculateNowPoint()default!!!!!!");
                throw new UnsupportedOperationException("!!!!!!Error:RoadSection.calculateNowPoint()default!!!!!!");
        }
    }
}
