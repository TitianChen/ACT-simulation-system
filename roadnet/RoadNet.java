//路网类;
package roadnet;

import storageblock.Yard;
import java.util.ArrayDeque;
import java.util.Queue;
import parameter.InputParameters;
import parameter.StaticName;
import CYT_model.Point2D;
import Vehicle.AGV;

/**
 * @author YutingChen, 760698296@qq.com Dalian University of Technology
 */
//码头各区域网络关系
public final class RoadNet {

    //变量
    public RoadSection[] roadSections;
    public Object[] roadNums;//与roadSections对应的roadNums;
    public FunctionArea[] functionAreas;//功能区;
    public Object[] funcareaNums;//其他各个区域块的编号;
    public MatrixGraph graph;
    public Object[][][] route;

    public Yard getYardArea(Object areanum) {
        for (FunctionArea functionArea : functionAreas) {
            if (functionArea.getAreaNum().equals(areanum) == true) {
                return (Yard) functionArea;
            }
        }
        System.out.println("!!!Error:RoadNet.getArea()!!!");
        throw new UnsupportedOperationException("Error:RoadNet.getArea() Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    //获得前沿装卸路段RoadSection;
    public RoadSection getagvUnderQCRoad() {
        //Road1
        for (RoadSection roadSection : this.roadSections) {
            if (roadSection.getroadNum().equals(StaticName.ROADUNDERQC) == true) {
                return roadSection;
            }
        }
        System.out.println("!!!Error:RoadNet.getAGVUnderQCRoad()!!!");
        throw new UnsupportedOperationException("!!!Error:RoadNet.getAGVUnderQCRoad()!!!.");
    }

    
    private Point2D findNearestPoint(Point2D point, RoadSection road) {
        double minx = Math.min(road.getedge()[0].getX(), road.getedge()[3].getX());
        double maxx = Math.max(road.getedge()[0].getX(), road.getedge()[3].getX());
        double miny = Math.min(road.getedge()[0].getY(), road.getedge()[3].getY());
        double maxy = Math.max(road.getedge()[0].getY(), road.getedge()[3].getY());
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
     * 找到该RoadSection中最合适的终点;
     * @param roadSection
     * @param firstP
     * @return Point2D
     */
    private Point2D findProperFinalPoint(RoadSection roadSection, Point2D startP, RoadSection nextroadSection, Point2D endP) {
        if(nextroadSection.getroadNum().equals("Road8_Yard2AGVTransfer")==false && 
                nextroadSection.getroadNum().equals("Road2_Yard1AGVTransfer")==false && 
                roadSection.getroadNum().equals("Road8_Yard2AGVTransfer")==false && 
                roadSection.getroadNum().equals("Road2_Yard1AGVTransfer")==false){
            int side = roadSection.findeside(nextroadSection.getroadNum());
            double x = roadSection.CentralAxis(side).getX();
            double y = roadSection.CentralAxis(side).getY();
            return new Point2D.Double(x, y);
        }else if (nextroadSection.getroadNum().equals("Road8_Yard2AGVTransfer") == true
                || nextroadSection.getroadNum().equals("Road2_Yard1AGVTransfer") == true) {
            //下一个路段为AGV交换区;
            double minx = Math.min(roadSection.getedge()[0].getX(), roadSection.getedge()[3].getX());
            double maxx = Math.max(roadSection.getedge()[0].getX(), roadSection.getedge()[3].getX());
            double miny = Math.min(roadSection.getedge()[0].getY(), roadSection.getedge()[3].getY());
            double maxy = Math.max(roadSection.getedge()[0].getY(), roadSection.getedge()[3].getY());
            double x, y;
            if (endP.getX() < minx) {
                x = minx;
            } else if (endP.getX() > maxx) {
                x = maxx;
            } else {
                x = endP.getX();
            }
            if (endP.getY() < miny) {
                y = miny;
            } else if (endP.getY() > maxy) {
                y = maxy;
            } else {
                y = endP.getY();
            }
            return new Point2D.Double(x, y);
        } else if (roadSection.getroadNum().equals("Road8_Yard2AGVTransfer") == true
                || roadSection.getroadNum().equals("Road2_Yard1AGVTransfer") == true) {
            //当前为AGV交换区;下一个路段不是AGV交换区;
            if(roadSection.getroadNum().equals("Road2_Yard1AGVTransfer") == true){
                return new Point2D.Double(startP.getX(), startP.getY()-0.5*InputParameters.getLengthOfTransferPoint());
            }else if(roadSection.getroadNum().equals("Road8_Yard2AGVTransfer") == true){
                return new Point2D.Double(startP.getX(), startP.getY()+0.5*InputParameters.getLengthOfTransferPoint());
            }
        }
        System.out.println("!!!Error:findProperFinalPoint()!!!");
        return null;
    }

    /**
     * 浅获取;
     * @return 
     */
    private RoadSection getagvWaterRoad() {
        for (RoadSection roadSection : this.roadSections) {
            if(roadSection.getroadNum().equals(StaticName.ROADWATERSIDE)){
                return roadSection;
            }
        }
        System.out.println("!!!Error:RoadNet.getagvWaterRoad()!!!");
        throw new UnsupportedOperationException("!!!Error:RoadNet.getagvWaterRoad()!!!.");
    }

    public double getMiny() {
        double miny = 1000;
        for (RoadSection road : this.roadSections) {
            if(road.getedge()[0].getY()<miny){
                miny = road.getedge()[0].getY();
            }
            if(road.getedge()[1].getY()<miny){
                miny = road.getedge()[0].getY();
            }
            if(road.getedge()[2].getY()<miny){
                miny = road.getedge()[0].getY();
            }
            if(road.getedge()[3].getY()<miny){
                miny = road.getedge()[0].getY();
            } 
        }
        return miny;
    }
    public double getMaxy() {
        double maxy = 1000;
        for (RoadSection road : this.roadSections) {
            if(road.getedge()[0].getY()>maxy){
                maxy = road.getedge()[0].getY();
            }
            if(road.getedge()[1].getY()>maxy){
                maxy = road.getedge()[0].getY();
            }
            if(road.getedge()[2].getY()>maxy){
                maxy = road.getedge()[0].getY();
            }
            if(road.getedge()[3].getY()>maxy){
                maxy = road.getedge()[0].getY();
            } 
        }
        return maxy;
    }

    public enum Visit {
        unvisited, visited
    };

    //构造方法
    public RoadNet(RoadSection[] roadsections, FunctionArea[] functionAreas) throws CloneNotSupportedException {
        this.roadSections = new RoadSection[roadsections.length];
        this.functionAreas = new FunctionArea[functionAreas.length];
        this.roadNums = new Object[roadsections.length];
        this.funcareaNums = new Object[functionAreas.length];
        System.arraycopy(roadsections, 0, this.roadSections, 0, this.roadSections.length);
        System.arraycopy(functionAreas, 0, this.functionAreas, 0, this.functionAreas.length);
        //graph中添加road节点;
        for (int i = 0; i < this.roadSections.length; i++) {
            this.roadNums[i] = this.roadSections[i].getroadNum();
        }
        graph = new MatrixGraph(roadNums);
        //graph中添加functionArea节点;
        for (int i = 0; i < this.functionAreas.length; i++) {
            this.funcareaNums[i] = this.functionAreas[i].getAreaNum();
            graph.addVex(this.funcareaNums[i]);
        }
        //添加路指向的边;
        for (RoadSection roadSection : this.roadSections) {
            //添加路和路之间的关系;边的权值为1;有向关系,info存储1的边向;
            graph.addEdge(roadSection.getroadNum(), roadSection.getside1roadNum(), "side1", 1);
            graph.addEdge(roadSection.getroadNum(), roadSection.getside2roadNum(), "side2", 1);
            //添加路与其他area之间的相互关系;边的权值为2;有向关系,info存储？？??????------;
            graph.addEdge(roadSection.getroadNum(), roadSection.getfuncAreaNum(), "iiiiii", 2);
            graph.addEdge(roadSection.getfuncAreaNum(), roadSection.getroadNum(), "iiiiii", 2);
        }
        //添加其他function area之间的关系
        for (FunctionArea functionArea : this.functionAreas) {
            //graph.addEdge(???);////没有小车，其他功能区之间并没有边相连;
        }
        //System.out.println(graph.printGraph());
        this.route = new Object[roadSections.length][roadSections.length][];
        //找寻路径,并存储;
        for (int i = 0; i < this.roadSections.length; i++) {
            for (int j = 0; j < this.roadSections.length; j++) {
                //System.out.print("i:" + i + " j:" + j + "---");///////////////
                graph.getPaths(roadSections[i].getroadNum(), null,
                        roadSections[i].getroadNum(), roadSections[j].getroadNum());
                this.route[i][j] = this.bestRoute(graph.getQueue());
            }
        }
    }

    //根据长度找到roadSection中的最优路线;
    public Object[] bestRoute(Queue<Object> que0) throws CloneNotSupportedException {
        //System.out.print(que0);
        Queue<Object> routei = new ArrayDeque<>();
        double Length = 100000000;
        double leni = 0;
        while (que0.isEmpty() == false) {
            if (que0.peek().equals("end") == false) {
                routei.add(que0.peek());
                if (this.findRoadSection((String) que0.peek()) != null)//该Object在road中;
                {
                    leni += this.findRoadSection((String) que0.poll()).getLength();
                } else {
                    que0.poll();
                    leni += 1000000000;//route跑到功能区里去了;
                }
            } else {
                que0.poll();
                if (Length > leni) {
                    Length = leni;
                } else {
                    routei.clear();
                }
                leni = 0;
            }
        }
        if (Length > 1000000)//此路不通
        {
            //System.out.print("---BestRoute: 无");/////////
            //System.out.print(routei);//////////
            //System.out.println(Length);//////
            return null;//没有可以通行的路径;
        } else {//通
            //System.out.print("---BestRoute:");//////
            //System.out.print(routei);//////
            //System.out.println(Length);//////
            return routei.toArray();
        }
    }

    public Object getSomeInfo(int i, int j) {
        return this.graph.edges[i][j].getInfo();
    }

    //方法
    //寻找最短路程对应的Object[] roadsection_num;
    public double minPathLength(Point2D startPoint, Point2D endPoint, String car) {
        Point2D startpoint = new Point2D.Double(startPoint.getX(), startPoint.getY());
        Point2D endpoint = new Point2D.Double(endPoint.getX(), endPoint.getY());
        double cartype = 0;
        switch (car) {
            case StaticName.AGVCAR:
                cartype = 1;
                break;
            case StaticName.TRUCKCAR:
                cartype = 2;
                break;
            default:
                System.out.println("!!!Error:RoadNet.minPathLength()!!!");
                throw new UnsupportedOperationException("Error:Error:RoadNet.minPathLength()!!!");
        }
        //Object[] routeNum;//存储需要经过的每一个roadSection/functionArea;
        double minLength = 0;
        Object startRoad;
        if (this.isInRoadSection(startpoint) == true) {
            ///加edge
            startRoad = this.findRoadSectionName(startpoint, cartype);
        }else{
            FunctionArea thisArea = this.findFunctionArea(startpoint);
            if (thisArea.getAreaNum().equals(StaticName.AGVBUFFER)) {
                //AGV buffer区;
//System.out.println("!!!"+((AGVBufferArea)thisArea).P1+""+((AGVBufferArea)thisArea).P2);
//                startpoint.setLocation(startpoint.getX()+0.5*((AGVBufferArea)thisArea).getLength()+
//                        +0.5*InputParameters.getSingleRoadWidth(), startpoint.getY());
                if(this.isInRoadSection(this.getagvWaterRoad().findNearestPoint(startpoint)) == true){
                    minLength += this.getagvWaterRoad().findNearestPoint(startpoint).distance(startpoint);//
                    startpoint.setLocation(getagvWaterRoad().findNearestPoint(startpoint));//s
                    startRoad = this.findRoadSectionName(startpoint, cartype);
                }else{
                    System.out.println("!!!Error:RoadNet.AGV buffer default;minPathLength()!!!");
                    System.out.println("!!!Error:startP!!!："+startpoint+this.getagvUnderQCRoad().getedge()[0]+
                            this.getagvUnderQCRoad().getedge()[2]);
                    throw new UnsupportedOperationException("Error:Error:RoadNet.AGV buffer default;minPathLength()!!!");
                }
            } else {
                System.out.println("!!!Error:RoadNet.minPathLength()!!!");
                throw new UnsupportedOperationException("Error:Error:RoadNet.minPathLength()!!!");
            }
        }
        //System.out.print("startRoad："+startRoad.toString());
        Object endRoad;
        if (this.isInRoadSection(endpoint) == true) {
            endRoad = this.findRoadSectionName(endpoint, cartype);
        } else {
            FunctionArea thisArea = this.findFunctionArea(endpoint);
            if(thisArea.getAreaNum().equals(StaticName.AGVBUFFER)){
                if(startRoad.equals(StaticName.ROADUNDERQC)){
                    //起始点是UnderQC,目的地是BufferArea
                    endRoad = this.findRoadSectionName(StaticName.ROADUNDERQC);
                    endpoint.setLocation(endpoint.getX()-0.5*((AGVBufferArea)thisArea).getLength()-0.5, endpoint.getY());
                    //System.out.println("endPoint:"+endpoint);
                    minLength += 0.5*((AGVBufferArea)thisArea).getLength()+0.5;
                    if(this.isInRoadSection(endpoint) == false){
                        System.out.println("!!!Error:RoadNet.minPathLength()ROADUNDERQC-endPoint isInRoadSection()!!!");
                        throw new UnsupportedOperationException("Error:Error:RoadNet.minPathLength()endPoint!!!");
                    }
                }else{
                    //起始点是transferPoint,目的地是BufferArea
                    endRoad = this.findRoadSectionName(StaticName.ROADWATERSIDE);
                    endpoint.setLocation(endpoint.getX()+0.5*((AGVBufferArea)thisArea).getLength()+
                            0.5*InputParameters.getSingleRoadWidth(), endpoint.getY());
                    minLength += 0.5*((AGVBufferArea)thisArea).getLength()+0.5*InputParameters.getSingleRoadWidth();
                    if(this.isInRoadSection(endpoint) == false){
                        System.out.println("!!!Error:RoadNet.minPathLength()yard-endPoint isInRoadSection()!!!");
                        throw new UnsupportedOperationException("Error:Error:RoadNet.minPathLength()endPoint!!!");
                    }
                }
            }else{
                System.out.println("!!!Error:RoadNet.minPathLength()endPoint!!!");
                throw new UnsupportedOperationException("Error:Error:RoadNet.minPathLength()endPoint!!!");
            }
        }
        //System.out.println("--endRoad："+endRoad.toString());
        int i = this.graph.vertexs.indexOf(startRoad);
        int j = this.graph.vertexs.indexOf(endRoad);
        //System.out.print("startRoad："+ i);System.out.println(" endRoad："+j);
        minLength += minLength(startpoint, endpoint, this.route[i][j]);
        //System.out.println(startpoint + " " + endpoint + " MinLength: " + minLength);
        return minLength;
    }
    public Point2D calculateNowPoint(Point2D startPoint, Point2D endPoint, AGV agv, double totallength, double nowlength) {
        Point2D startpoint = new Point2D.Double(startPoint.getX(), startPoint.getY());
        Point2D endpoint = new Point2D.Double(endPoint.getX(), endPoint.getY());
        double totalLength = totallength;
        double nowLength = nowlength;
        double cartype = 1;
        double minLength = 0;
        Object startRoad;
        if (this.isInRoadSection(startpoint) == true) {
            startRoad = this.findRoadSectionName(startpoint, cartype);
        } else {
            FunctionArea thisArea = this.findFunctionArea(startpoint);
            if (thisArea.getAreaNum().equals(StaticName.AGVBUFFER)) {
                //起点是AGV buffer区;
                if (0.5 * ((AGVBufferArea) thisArea).getLength() + 0.5 * InputParameters.getSingleRoadWidth() >= nowlength) {
                    return new Point2D.Double(startpoint.getX()+nowlength, startpoint.getY());
                }else{
                    totalLength -= 0.5 * ((AGVBufferArea) thisArea).getLength() + 0.5 * InputParameters.getSingleRoadWidth();
                    nowLength -= 0.5 * ((AGVBufferArea) thisArea).getLength() + 0.5 * InputParameters.getSingleRoadWidth();
                    startpoint.setLocation(startpoint.getX()+0.5 * ((AGVBufferArea) thisArea).getLength() + 
                            0.5 * InputParameters.getSingleRoadWidth(), startpoint.getY());
                    if(this.isInRoadSection(startpoint) == false){
//System.out.println("!!!:RoadNet.calculateNowPoint()--else AGVBuffer!!!");
                        return startpoint;
//throw new UnsupportedOperationException("Error:Error:RoadNet.calculateNowPoint()!!!");/////考虑加速才暂时不要的;
                    }
                    return calculateNowPoint(startpoint, endpoint, agv, totalLength, nowLength);
                }
            } else {
//System.out.println("!!!:RoadNet.calculateNowPoint() startPoint!!!!!");
                return startpoint;
//throw new UnsupportedOperationException("Error:Error:RoadNet.calculateNowPoint()!!!");/////考虑加速才暂时不要的;
            }
        }
        Object endRoad;
        if (this.isInRoadSection(endpoint) == true) {
            endRoad = this.findRoadSectionName(endpoint, cartype);
            //System.out.println("Calculate__endRoad：" + endRoad.toString()+" ");
            int i = this.graph.vertexs.indexOf(startRoad);
            int j = this.graph.vertexs.indexOf(endRoad);
            minLength += minLength(startpoint, endpoint, this.route[i][j]);
            if(minLength >= nowLength){
                //nowPoint在route[i][j]里面;
                //System.out.println("Calculate__endRoad：nowPoint在route[i][j]里面" + endRoad.toString()+" ");
                return this.calculateNowPoint(startpoint, endpoint, agv, nowLength,this.route[i][j]); 
            }else{
//System.out.println("!!!:RoadNet.calculateNowPoint()ROADUNDERQC-endPoint-else!!!"+minLength+"**"+nowLength);
                return startpoint;
//throw new UnsupportedOperationException("Error:Error:RoadNet.calculateNowPoint()endPoint--else!!!");/////考虑加速才暂时不要的;
            }
        } else {
            //终点是功能区;
            FunctionArea thisArea = this.findFunctionArea(endpoint);
            double endPlus = 0;
            if(thisArea.getAreaNum().equals(StaticName.AGVBUFFER)){
                //System.out.println("Calculate__endRoad：终点是AGVBUFFER" + " ");
                if(startRoad.equals(StaticName.ROADUNDERQC)){
                    //起始点是UnderQC,目的地是BufferArea
                    endRoad = this.findRoadSectionName(StaticName.ROADUNDERQC);
                    //System.out.println("Calculate__endRoad：终点set ROADUNDERQC" + " ");
                    endpoint.setLocation(endpoint.getX()-0.5*((AGVBufferArea)thisArea).getLength(), endpoint.getY());
                    endPlus += 0.5*((AGVBufferArea)thisArea).getLength();
                    //System.out.println("Calculate__endRoad：" + endRoad.toString());
                    int i = this.graph.vertexs.indexOf(startRoad);
                    int j = this.graph.vertexs.indexOf(endRoad);
                    minLength += minLength(startpoint, endpoint, this.route[i][j]);
                    if (minLength >= nowLength) {
                        //nowPoint在route[i][j]里面;
                        return this.calculateNowPoint(startpoint, endpoint, agv, nowLength, this.route[i][j]);
                    } else {
                        if(endPlus+minLength>=nowLength){
                            //nowPoint在功能区里;
                            return ((AGVBufferArea)(thisArea)).calculateNowPoint(endpoint, 
                                    ((AGVBufferArea)(thisArea)).getBookedArea(agv), agv, 
                                    nowLength-minLength);
                        }else{
//System.out.println("!!!:RoadNet.calculateNowPoint()ROADUNDERQC-nowPoint在功能区里;-else!!!" + minLength + "**" + nowLength+"**"+endPlus);
                            return startpoint;
//throw new UnsupportedOperationException("Error:Error:RoadNet.calculateNowPoint()endPoint--else!!!");/////考虑加速才暂时不要的;
                        }
                    }
                }else{
                    //起始点是transferPoint,目的地是BufferArea
                    endRoad = this.findRoadSectionName(StaticName.ROADWATERSIDE);
                    //System.out.println("Calculate__endRoad：终点set ROADWATERSIDE" + " ");
                    endpoint.setLocation(endpoint.getX()+0.5*((AGVBufferArea)thisArea).getLength()+
                            0.5*InputParameters.getSingleRoadWidth(), endpoint.getY());
                    if(this.isInRoadSection(endpoint) == false){
                        System.out.println("!!!Error:RoadNet.calculateNowPoint()yard-endPoint"+
                                "起始点是transferPoint,目的地是BufferArea"+"isInRoadSection()!!!");
                        throw new UnsupportedOperationException("Error:Error:RoadNet.minPathLength()endPoint!!!");
                    }
                    int i = this.graph.vertexs.indexOf(startRoad);
                    int j = this.graph.vertexs.indexOf(endRoad);
                    minLength += minLength(startpoint, endpoint, this.route[i][j]);
                    if (minLength >= nowLength) {
                        //nowPoint在route[i][j]里面;
                        return this.calculateNowPoint(startpoint, endpoint, agv, nowLength, this.route[i][j]);
                    } else {
                        endPlus += 0.5*((AGVBufferArea)thisArea).getLength()+0.5*InputParameters.getSingleRoadWidth();
                        if(endPlus+minLength>=nowLength){
                            //nowPoint在BufferArea里;
                            return ((AGVBufferArea)(thisArea)).calculateNowPoint(endpoint, 
                                    ((AGVBufferArea)(thisArea)).getBookedArea(agv), agv, 
                                    nowLength-minLength);
                        }else{
//System.out.println("!!!:RoadNet.calculateNowPoint()ROADUNDERQC-"+"\t"+ "nowPoint在功能区里;-else!!!" + minLength + "**" + nowLength+"**"+endPlus);
                            return startpoint;
//throw new UnsupportedOperationException("Error:Error:RoadNet.calculateNowPoint()endPoint--else!!!");/////考虑加速才暂时不要的;
                        }
                    }
                }
            }else{
//System.out.println("!!:RoadNet.calculateNowPoint()endPoint!!!");
                return startpoint;
//throw new UnsupportedOperationException("Error:Error:RoadNet.calculateNowPoint()endPoint!!!");
            }
        }
    }
    private double minLength(Point2D startPoint, Point2D endPoint, Object[] minroute) {
        Point2D startpoint = new Point2D.Double(startPoint.getX(), startPoint.getY());
        Point2D endpoint = new Point2D.Double(endPoint.getX(), endPoint.getY());
        double minlength = 0;
        if (minroute.length == 1) {
            //System.out.println(minroute[0].toString()+"***");
            minlength = findRoadSection((String) minroute[0]).PathLengthOnThisRoad(startpoint, endpoint);
        } else {
            int starti = 0;
            int endi = minroute.length - 1;
            Point2D firstP = new Point2D.Double(startpoint.getX(), startpoint.getY());
            if(this.findRoadSection((String)minroute[starti]).isOnThisRoad(firstP) == false){
                System.out.println("!!!Error:RoadNet.minLength()!!");
                System.out.println("Point:"+firstP+""+(String)minroute[starti]+
                        this.findRoadSection((String)minroute[starti]).getedge()[0]+
                        this.findRoadSection((String)minroute[starti]).getedge()[3]);
                throw new UnsupportedOperationException("Error:Error:RoadNet.minLength()!!!");
            }
            while (starti != endi) {
                Point2D nextP = this.findProperFinalPoint(this.findRoadSection((String)minroute[starti]),firstP,
                        this.findRoadSection((String)minroute[starti+1]),endpoint);
                //System.out.print("firstP:"+firstP+"--ProperFinalPoint:"+nextP);
                minlength += this.findRoadSection((String) minroute[starti]).PathLengthOnThisRoad(firstP, nextP);
                //System.out.println("--thisLength:"+findRoadSection((String) minroute[starti]).PathLengthOnThisRoad(firstP, nextP));
                firstP.setLocation(nextP);//nextP-->firstP
                if(findRoadSection((String)minroute[starti+1]).isOnThisRoad(firstP) == false){
                    if(findRoadSection((String)minroute[starti+1]).isOnThisRoadEdge(firstP) == true){
                       firstP.setLocation(this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1]))); 
                    }else{
                        //firstP不在RoadSection[starti+1]里面;说明RS[starti]和RS[starti+1]之间有距离(用于转弯)
                        if(findRoadSection((String)minroute[starti+1]).getroadNum().equals("Road8_Yard2AGVTransfer")
                                || findRoadSection((String)minroute[starti+1]).getroadNum().equals("Road2_Yard1AGVTransfer")){
                            if(this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])).distance(firstP)<
                                    InputParameters.getTurningRadiusOfCar()){
                                minlength += this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])).distance(firstP);
                                firstP.setLocation(this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])));
                            }else{
                                System.out.println("!!!Error:RoadNet.minLength().firstP starti+1:transfer!!");
                                System.out.println("Point:" + firstP + "" + (String) minroute[starti+1]
                                        + this.findRoadSection((String) minroute[starti+1]).getedge()[0]
                                        + this.findRoadSection((String) minroute[starti+1]).getedge()[3]);
                                throw new UnsupportedOperationException("Error:Error:RoadNet.minLength().firstP--starti+1:transfer!!!");
                            }
                        }else if(findRoadSection((String)minroute[starti]).getroadNum().equals("Road8_Yard2AGVTransfer")
                                || findRoadSection((String)minroute[starti]).getroadNum().equals("Road2_Yard1AGVTransfer")){
                            if(this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])).distance(firstP)<
                                    InputParameters.getTurningRadiusOfCar()){
                                minlength += this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])).distance(firstP);
                                firstP.setLocation(this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])));
                            }else{
                                System.out.println("!!!Error:RoadNet.minLength().firstP starti:transfer!!");
                                System.out.println("Point:" + firstP + "" + (String) minroute[starti]
                                        + this.findRoadSection((String) minroute[starti]).getedge()[0]
                                        + this.findRoadSection((String) minroute[starti]).getedge()[3]);
                                throw new UnsupportedOperationException("Error:Error:RoadNet.minLength().firstP--starti:transfer!!!");
                            }
                        }else if(findRoadSection((String)minroute[starti+1]).getroadType() == 3){
                            //转弯圆弧;
                            //System.out.println(minlength);
                            minlength += this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])).distance(firstP);
                            if(this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])).distance(firstP)>5){
                                System.out.println("!!!Error:RoadNet.minLength().firstP starti+1:转弯圆弧!!");
                                System.out.println("!!!Erro:Point:" + firstP + "" + (String) minroute[starti+1]
                                        + this.findRoadSection((String) minroute[starti+1]).getedge()[0]
                                        + this.findRoadSection((String) minroute[starti+1]).getedge()[3]);
                                throw new UnsupportedOperationException("Error:Error:RoadNet.minLength().firstP--starti+1:转弯圆弧!!!");
                            }
                            firstP.setLocation(this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])));
                        }else{
                            System.out.println("!!!Error:RoadNet.minLength()::!!"+
                                    findRoadSection((String)minroute[starti]).getroadNum()+
                                    findRoadSection((String)minroute[starti+1]).getroadNum());
                        }
                    }                    
                }
                starti++;//start换
            }
            minlength += findRoadSection((String) minroute[endi]).PathLengthOnThisRoad(firstP, endpoint);
        }
        /////一段段叠加;
        // 每一段：输入：起点和所在road,nextroad/endPoint
        //       输出：side=f(nextroad)-> next坐标=f(side) -> length=f(起点,side) -> PathLength
        return minlength;
    }

    /**
     * 计算 在AGV从StartPoint行驶到endPoint这段路程中，当行驶了nowLength时AGV的所在位置;
     * @param startPoint
     * @param endPoint
     * @param agv
     * @param nowLength
     * @param minroute
     * @return 
     */
    private Point2D calculateNowPoint(Point2D startPoint, Point2D endPoint, AGV agv, double nowLength, Object[] minroute) {
        Point2D startpoint = new Point2D.Double(startPoint.getX(), startPoint.getY());
        Point2D endpoint = new Point2D.Double(endPoint.getX(), endPoint.getY());
        double minlength = 0;
        if (minroute.length == 1) {
            //System.out.println(minroute[0].toString()+"***");
            minlength = findRoadSection((String) minroute[0]).PathLengthOnThisRoad(startpoint, endpoint);
            return findRoadSection((String) minroute[0]).calculateNowPoint(startPoint,endPoint,agv,nowLength);
        } else {
            int starti = 0;
            int endi = minroute.length - 1;
            Point2D firstP = new Point2D.Double(startpoint.getX(), startpoint.getY());
            if(this.findRoadSection((String)(minroute[starti])).isOnThisRoad(firstP) == false){
//System.out.println("!!!:RoadNet.private calculateNowPoint()!!");
                return startpoint;
//throw new UnsupportedOperationException("Error:Error:RoadNet.minPathLength()calculateNowPoint" + "isOnThisRoad(firstP) == false!!!");/////考虑加速才暂时不要的;
            }
            while (starti != endi) {
                Point2D nextP = this.findProperFinalPoint(this.findRoadSection((String)minroute[starti]),firstP,
                        this.findRoadSection((String)minroute[starti+1]),endpoint);
                if((minlength+this.findRoadSection((String) minroute[starti]).PathLengthOnThisRoad(firstP, nextP)) >= nowLength){
                    return this.findRoadSection((String) minroute[starti]).calculateNowPoint(firstP, nextP, agv, nowLength-minlength);
                }
                minlength += this.findRoadSection((String) minroute[starti]).PathLengthOnThisRoad(firstP, nextP);
                firstP.setLocation(nextP);//nextP-->firxtP
                if(findRoadSection((String)minroute[starti+1]).isOnThisRoad(firstP) == false){
                    if((minlength+firstP.distance(this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])))) >= nowLength){
                        return findRoadSection((String) minroute[starti]).calculateNowPoint(firstP, //starti or starti+1????
                                this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])), agv, nowLength-minlength);
                    }
                    minlength += firstP.distance(this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])));
                    firstP.setLocation(this.findNearestPoint(firstP,findRoadSection((String)minroute[starti+1])));
                }
                starti++;//start换
            }
            if ((minlength + findRoadSection((String) minroute[endi]).PathLengthOnThisRoad(firstP, endpoint)) >= nowLength){
                return findRoadSection((String) minroute[endi]).
                        calculateNowPoint(firstP, endpoint, agv, nowLength - minlength);
            }else{
//System.out.println("!!!:RoadNet.minPathLength()calculateNowPoint end!!!");
                return startpoint;
//throw new UnsupportedOperationException("Error:Error:RoadNet.minPathLength()calculateNowPoint end!!!");/////考虑加速才暂时不要的;
            }
        }
    }
    public boolean isInRoadSection(Point2D point) {
        for (RoadSection roadSection : roadSections) {
            if (roadSection.isOnThisRoad(point) == true){
                return true;
            }
        }
        return false;
    }

    private FunctionArea findFunctionArea(Point2D point) {
        for (FunctionArea functionArea : this.functionAreas) {
            if (functionArea.isInThisRectangle(point)) {
                return functionArea;
            }
        }
        System.out.println("!!!Error:RoadNet.findFunctionArea(Point2D point)!!!");
        throw new UnsupportedOperationException("Error:Error:RoadNet.findFunctionArea(Point2D point)!!!");
    }

    public Object findRoadSectionName(Point2D point, double cartype) {
        for (RoadSection roadSection : roadSections) {
            if (roadSection.getroadType() != 3 &&
                    roadSection.isOnThisRoad(point) == true && roadSection.getcarType() == cartype) {
                //先优先找直线段;
                return roadSection.getroadNum(); //返回Object roadNum; 
            }
        }
        for (RoadSection roadSection : roadSections) {
            if (roadSection.getroadType() == 3 && 
                    roadSection.isOnThisRoad(point) == true && roadSection.getcarType() == cartype) {
                //找不到直线段，再找圆弧段;
                return roadSection.getroadNum(); //返回Object roadNum; 
            }
        }
        //再找边界点;
        for (RoadSection roadSection : roadSections) {
            if (roadSection.getroadType() != 3 && roadSection.isOnThisRoadEdge(point) == true && roadSection.getcarType() == cartype ) {
                return roadSection.getroadNum();
            }
        }
//System.out.println("!!!注意!!!RoadNet.findRoadSectionName null");
        return null;
    }

    public RoadSection findRoadSection(String SectionNum) {
        for (RoadSection roadSection : roadSections) {
            if (roadSection.getroadNum().equals(SectionNum) == true) {
                return roadSection;
            }
        }
        return null;
    }
    public Object findRoadSectionName(String SectionNum){
        for (RoadSection roadSection : roadSections) {
            if (roadSection.getroadNum().equals(SectionNum) == true) {
                return roadSection.getroadNum();
            }
        }
        return null;
    }
}
