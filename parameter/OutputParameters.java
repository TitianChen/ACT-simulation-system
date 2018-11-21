/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parameter;

import CYT_model.Container;
import CYT_model.Port;
import apron.QuayCrane;
import Vehicle.AGV;
import java.io.BufferedReader;
import storageblock.Block;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;
import static parameter.InputParameters.INPUTPATH;
import static parameter.InputParameters.OUTPUTPATH;
import roadnet.RoadSection;
import storageblock.Yard;
import ship.Ship;

/**
 *
 * @author YutingChen, yuting.chen17@imperial.ac.uk
 * Dalian University of Technology
 */
public final class OutputParameters {
    public static double simulationRealTime = 0;

    public static void outputAllUnloadingState(Ship ship) {
        Queue<String> que = new ArrayDeque<>();
        que.add(ship.getShipContainers().getTotal_unload_num()+"");
        int i = 1;
        for (Container container : ship.getShipContainers().getContainersTotalNeedUnload()) {
            que.add(i+":"+container + container.getState() + container.getFTsize());
            i++;
        }
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+ship.toString()+"UContainers_State"+".txt");
        if(file.exists() != true){
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
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
    public OutputParameters(){    
        simulationRealTime = 0;
    }
    public static void addRowsToFile(String fileName, StringBuffer strb) {
        Queue<String> que = new ArrayDeque<>();
        que.add(strb.toString());
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+fileName+".txt");
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
     * @param port
     */
    public static void outputAllNeeded(Port port){
        //更新泊位,在泊船只信息文件
        OutputParameters.outputBerthShip(port);
        OutputParameters.outputAGVXY(port);
        //更新堆场集装箱,场桥信息文件
        for (Yard yard : port.getYards()) {
            OutputParameters.outputCurrentBlockYardCraneXY(yard);
            OutputParameters.outputCurrentYardContainersMatrix(yard,Integer.toString((int)simulationRealTime));
            for (Block block : yard.getBlock()) {
                OutputParameters.outputCurrentBlockContainersXY(block,Integer.toString((int)simulationRealTime));
            }
        }
        //更新前沿岸桥信息文件
        OutputParameters.outputCurrentQuayCraneXY(port);
        OutputParameters.outputSimulatorTimeAndSpeed(port);
    }
    /**
     * 添加一行内容至fileName.txt中;
     * @param fileName
     * @param content 
     */
    public static void addOneRowToFile(String fileName, String content){
        Queue<String> que = new ArrayDeque<>();
        que.add(content);
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+fileName+".txt");
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
     * output 单个参数数组到文件name.txt中;
     * @param name
     * @param parameters 
     */
    public static void outputSingleParameters(String name,double[] parameters){
        Queue<String> que = new ArrayDeque<>();
        for(int i=0;i<parameters.length;i++){
            que.add(parameters[i]+"\t");
        }
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+name+".txt");
        if(file.exists() != true){
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
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
        que.clear();
    }
    public static void outputShip(Ship ship){
        Queue<String> que = new ArrayDeque<>();
        String BerthName="";
        for (String assignedBerthName1 : ship.assignedBerthName) {
            BerthName += assignedBerthName1+" ";
        }
        String QCName="";
        for (String assignedQCName1 : ship.assignedQCName) {
            QCName += assignedQCName1+" ";
        }
        que.add(ship.getNumber()+"\t"+
                ship.getShipType()+"\t"+
                ship.getDWTLevel()+"\t"+
                ship.getDWT()+"\t"+
                ship.getShipLength()+"\t"+
                ship.getShipWidth()+"\t"+
                ship.getShipContainers().getPresent_total_20ft()+ship.getShipContainers().getPresent_total_40ft()*2+"\t"+
                Integer.toString(ship.getShipContainers().getUnloadTEU())+"\t"+
                Integer.toString(ship.getShipContainers().getLoadTEU())+"\t"+
                Integer.toString((int)ship.arrivingTime)+"\t"+
                Integer.toString((int)ship.getOutsideAnchorageTime)+"\t"+
                Integer.toString((int)ship.getChannelTime)+"\t"+
                Integer.toString((int)ship.startberthingTime)+"\t"+
                Integer.toString((int)ship.startUnloadingTime)+"\t"+
                Integer.toString((int)ship.quayCraneFinishToMoveTime)+"\t"+
                Integer.toString((int)ship.endUnloadingTime)+"\t"+
                Integer.toString((int)ship.startLoadingTime)+"\t"+
                Integer.toString((int)ship.endLoadingTime)+"\t"+
                Integer.toString((int)ship.startUnberthingTime)+"\t"+
                Integer.toString((int)ship.endUnberthingTime)+"\t"+
                BerthName+"\t"+
                QCName);
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+"Ship/AllShips"+".txt");
        if (file.exists() != true) {
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
    public static void outputShipStart(){
        Queue<String> que = new ArrayDeque<>();
        que.add("---------------All Ships Output---------------");
        que.add("number"+"\t"+
                "shipType"+"\t"+
                "DWTLevel"+"\t"+
                "DWT"+"\t"+
                "Length"+"\t"+
                "Width"+"\t"+
                "totalTEU"+"\t"+
                "unloadTEU"+"\t"+
                "loadTEU"+"\t"+
                "ArriveTime"+"\t"+
                "ATime"+"\t"+
                "CTime"+"\t"+
                "BTime"+"\t"+
                "SUTime"+"\t"+
                "QCFinishM"+"\t"+
                "EUTime"+"\t"+
                "SLTime"+"\t"+
                "ELTime"+"\t"+
                "SUBTime"+"\t"+
                "EUBTime"+"\t"+
                "Berths"+"\t"+
                "QCs");
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+"Ship/AllShips"+".txt");
        if (file.exists() != true) {
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
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
    public static void outputBlockCurrentContainer(Block block){
        Queue<String> que = new ArrayDeque<>();
        int num[][] = new int[block.getRowNumPerBlock()][block.getBayNumPerRow()];
        for(int i = 0;i<block.getRowNumPerBlock();i++){
            for(int j = 0;j<block.getBayNumPerRow();j++){
                num[i][j] = 0;
            }
        }
        int totalNum = 0;
        for(int i = 0;i<block.getCurrentContainers().length;i++){
            if(block.getCurrentContainers()[i].getPresentSlot() != null){
                int row = block.getCurrentContainers()[i].getPresentSlot().getRowNum();
                int bay = block.getCurrentContainers()[i].getPresentSlot().getBayNum();
                if(bay%2 == 0){
                    //40ft箱;
                    num[row-1][(bay-2)/2]++;
                    num[row-1][bay/2]++;
                    totalNum++;
                }else{
                    num[row-1][(bay-1)/2]++;
                    totalNum++;
                }
            }
        }
        //que.add("----------------"+this.yard.areaNum+"Block"+this.line+"-------------------");
        //que.add("-------------------ContainersNum------------------------"+totalNum);
        String str = "";
        for(int j = 0;j<block.getBayNumPerRow();j++){
            str += 0+"\t";
        }
        que.add(str);
        for(int i = 0;i<block.getRowNumPerBlock();i++){
            str = "";
            for(int j = 0;j<block.getBayNumPerRow();j++){
                str += num[i][j]+"\t";
            }
            que.add(str);
        }
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+block.getYard().getAreaNum()+".txt");
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
        que.clear();
        
        que = new ArrayDeque<>();
        que.add("----------------"+block.getYard().getAreaNum()+"Block"+block.getLine()+"-------------------");
        que.add("-------------------ContainersSlot------------------------");
        que.add("FTsize:\t"+"Position:\t"+"Yard:\t"+"Block:\trow:\tbay:\theight:\t");
        for(int i = 0;i<block.getCurrentContainers().length;i++){
            que.add(block.getCurrentContainers()[i].size+"\t"+
                    block.getCurrentContainers()[i].getState()+"\t"+
                    block.getCurrentContainers()[i].getPresentSlot().getYardNum()+"\tBlock"+
                    block.getCurrentContainers()[i].getPresentSlot().getBlock().getLine()+"\t"+
                    block.getCurrentContainers()[i].getPresentSlot().getRowNum()+"\t"+
                    block.getCurrentContainers()[i].getPresentSlot().getBayNum()+"\t"+
                    block.getCurrentContainers()[i].getPresentSlot().getHeightNum()+"\t");
        }
        iterator = que.iterator();
        file = new File(InputParameters.OUTPUTPATH+"BlockSection/"+block.getYard().getAreaNum()+"Block"+block.getLine()+".txt");
        if(file.exists() != true){
            file.getParentFile().mkdirs();
        }
        fw = null;
        writer = null;
        try {
            fw = new FileWriter(file);
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
    public static void outputRoadNet(Port port){
        Queue<String> que1 = new ArrayDeque<>();
        for (RoadSection road : port.getRoadNetWork().roadSections) {
            que1.add(road.getroadNum()+":"+road.getedge()[0]+"\t"+
                    road.getedge()[1]+"\t"+road.getedge()[2]+"\t"+road.getedge()[3]);
            que1.add("--"+road.getside1roadNum()+"-"+road.getside2roadNum()+"-"+
                    road.getfuncAreaNum());
        }
        Iterator<String> iterator = que1.iterator();
        File file = new File(InputParameters.OUTPUTPATH+"RoadNet"+".txt");
        if (file.exists() != true) {
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            while (iterator.hasNext()) {
                writer.write(iterator.next());
                writer.newLine();//换行
            }
            writer.flush();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                writer.close();
                fw.close();
            } catch (IOException e) {
            }
        }
        que1.clear();
        
    }
    private static void outputAGVXY(Port port){
        Queue<String> que1 = new ArrayDeque<>();
        for(AGV agv:port.getAGVs()){
            if(agv.getAGVstate().equals(StaticName.FREE)){
                que1.add(agv.getNumber()+"\t"+agv.getPresentPosition().getX()+"\t"+
                        agv.getPresentPosition().getY()+"\t"+0);
            }else if(agv.getAGVstate().equals(StaticName.LOADING)){
                que1.add(agv.getNumber()+"\t"+agv.getPresentPosition().getX()+"\t"+
                        agv.getPresentPosition().getY()+"\t"+1);
            }else if(agv.getAGVstate().equals(StaticName.UNLOADING)){
                que1.add(agv.getNumber()+"\t"+agv.getPresentPosition().getX()+"\t"+
                        agv.getPresentPosition().getY()+"\t"+2);
            }
        }
        Iterator<String> iterator = que1.iterator();
        File file = new File(InputParameters.OUTPUTPATH+"AGV_X_Y_State"+"/"
                +Integer.toString((int)simulationRealTime)+".txt");
        if (file.exists() != true) {
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            while (iterator.hasNext()) {
                writer.write(iterator.next());
                writer.newLine();//换行
            }
            writer.flush();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                writer.close();
                fw.close();
            } catch (IOException e) {
            }
        }
        que1.clear();
    }
    private static void outputCurrentBlockYardCraneXY(Yard yard){
        Queue<String> que1 = new ArrayDeque<>();
        for(int i = 0;i<yard.getBlock().length;i++){
            for(int YCi = 0;YCi<InputParameters.getYCNumPerBlock();YCi++){
                if(yard.getBlock()[i].getYC()[YCi].currentPosition == null ||
                        yard.getBlock()[i].getYC()[YCi].currentPosition.getX() == 0){
                    //currentPosition此时不存在;
                    //这一步先放着吧，不知道有没有判断的必要，气死我了。
                    return;
                }
                if(yard.getBlock()[i].getYC()[YCi].getFirstYCTask() != null){
                    que1.add(yard.getBlock()[i].getYC()[YCi].currentPosition.getX() + "\t"
                            + yard.getBlock()[i].getYC()[YCi].currentPosition.getY() + "\t"
                            + InputParameters.getRMGtrackGuage() + "\t"+ "1");
                }else{
                    que1.add(yard.getBlock()[i].getYC()[YCi].currentPosition.getX() + "\t"
                            + yard.getBlock()[i].getYC()[YCi].currentPosition.getY() + "\t"
                            + InputParameters.getRMGtrackGuage() + "\t"+ "0");
                }
            } 
        }
        Iterator<String> iterator = que1.iterator();
        File file = new File(InputParameters.OUTPUTPATH+yard.getAreaNum()+"_X_Y_Length"+"/"
                +Integer.toString((int)simulationRealTime)+".txt");
        if (file.exists() != true) {
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            while (iterator.hasNext()) {
                writer.write(iterator.next());
                writer.newLine();//换行
            }
            writer.flush();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                writer.close();
                fw.close();
            } catch (IOException e) {
            }
        }
        que1.clear();
    }
    private static void outputCurrentBlockContainersXY(Block block,String currentTime){
        Queue<String> que1 = new ArrayDeque<>();
        for (int i = 0; i < block.getCurrentContainers().length; i++) {
            if (block.getCurrentContainers()[i].getPresentSlot() != null) {
                que1.add(block.getCurrentContainers()[i].getPresentSlot().
                        getCentralLocation().getX() + "\t" + block.getCurrentContainers()[i].
                                getPresentSlot().getCentralLocation().getY() + "\t"
                        + block.getCurrentContainers()[i].size);
            }
        }
        Iterator<String> iterator = que1.iterator();
        File file = new File(InputParameters.OUTPUTPATH+"AllBlockContainersXY/" + 
                block.getYard().getAreaNum()+ Integer.toString(block.getLine())+"/"
                + currentTime + ".txt");
        if (file.exists() != true) {
            file.getParentFile().getParentFile().mkdirs();
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            while (iterator.hasNext()) {
                writer.write(iterator.next());
                writer.newLine();//换行
            }
            writer.flush();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                writer.close();
                fw.close();
            } catch (IOException e) {
            }
        }
        que1.clear();
    }
    private static void outputCurrentYardContainersMatrix(Yard yard,String currentTime){
        Queue<String> que = new ArrayDeque<>();
        for (int blockNum = 0; blockNum < InputParameters.getTotalBlockNumPerYard(); blockNum++) {
            Block block = yard.getBlock()[blockNum];
            int num[][] = new int[InputParameters.getPerYCRowNum()][InputParameters.getTotalBayNum()];
            for (int i = 0; i < InputParameters.getPerYCRowNum(); i++) {
                for (int j = 0; j < InputParameters.getTotalBayNum(); j++) {
                    num[i][j] = 0;
                }
            }
            int totalNum = 0;
            if(block.getCurrentContainers()==null){
                //System.out.println("Error:block.getCurrentContainers()==null!!!!!!");
                //throw new UnsupportedOperationException("Error:YardCrane.process_Loading()！！检查AGV Loading Yard Task分配至YCTask的规则！！");
                return;
            }
            for (int i = 0; i < block.getCurrentContainers().length; i++) {
                if (block.getCurrentContainers()[i].getPresentSlot() != null) {
                    //System.out.print(yard.getAreaNum()+"if ()----");
                    int row = block.getCurrentContainers()[i].getPresentSlot().getRowNum();
                    //System.out.print(yard.getAreaNum()+"!!!---row:"+row+" i"+i+" blockNum:"+blockNum);
                    int bay = block.getCurrentContainers()[i].getPresentSlot().getBayNum();
                    //System.out.print(yard.getAreaNum()+"!!!---bay:"+bay);
                    if (bay % 2 == 0) {
                        //40ft箱;
                        //System.out.println("!!!40ft---1:"+(row - 1)+" 2:"+(bay-2)/2);
                        if(row > InputParameters.getPerYCRowNum() ||
                                bay/2>=InputParameters.getTotalBayNum()){
                            System.out.println("!!!!-----Error!!!!"+yard.getAreaNum()+"!!!!exception"+"!!!---row:"+row+
                                    " i"+i+" blockNum:"+blockNum+" bay:"+bay+"!!!--YCRowNum："+InputParameters.getPerYCRowNum()+
                                    " TotalBayNum:"+InputParameters.getTotalBayNum());
                        }
                        num[row-1][(bay-2)/2]++;
                        num[row-1][bay/2]++;
                        totalNum++;
                    } else {
                        if(row > InputParameters.getPerYCRowNum() ||
                                (bay-1)/2>=InputParameters.getTotalBayNum()){
                            System.out.println("!!!!-----Error!!!!"+yard.getAreaNum()+"!!!!exception"+"!!!---row:"+row+
                                    " i"+i+" blockNum:"+blockNum+" bay:"+bay+"!!!--YCRowNum："+InputParameters.getPerYCRowNum()+
                                    " TotalBayNum:"+InputParameters.getTotalBayNum());
                        }
                        num[(row-1)][((bay-1)/2)]++;
                        totalNum++;
                    }
                }
            }
            String str = "";
            for (int j = 0; j < InputParameters.getTotalBayNum(); j++) {
                str += 0 + "\t";
            }
            que.add(str);
            for (int i = 0; i < InputParameters.getPerYCRowNum(); i++) {
                str = "";
                for (int j = 0; j < InputParameters.getTotalBayNum(); j++) {
                    str += num[i][j] + "\t";
                }
                que.add(str);
            }
        }
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH + yard.getAreaNum() + 
                "/"+ currentTime + ".txt");
        if (file.exists() != true) {
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            while (iterator.hasNext()) {
                writer.write(iterator.next());
                writer.newLine();//换行
            }
            writer.flush();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                writer.close();
                fw.close();
            } catch (IOException e) {
            }
        }
        que.clear();
    }
    private static void outputBerthShip(Port port){
        Queue<String> que1 = new ArrayDeque<>();
        int berthNum = port.getBerths().length;
        for(int i =0;i<berthNum;i++){
            if(port.getBerths()[i].getShip() == null){
                que1.add(port.getBerths()[i].getNumber()+"\t"+"-1"+"\t"+
                        port.getBerths()[i].getLocation().getX()+"\t"+port.getBerths()[i].getLocation().getY()+"\t"+
                        port.getBerths()[i].getLength()+"\t"+0+"\t"+0);
            }else{
                que1.add(port.getBerths()[i].getNumber()+"\t"+port.getBerths()[i].getShip().getNumber()+"\t"+
                        port.getBerths()[i].getLocation().getX()+"\t"+port.getBerths()[i].getLocation().getY()+"\t"+
                        port.getBerths()[i].getLength()+"\t"+port.getBerths()[i].getShip().getShipLength()+"\t"+
                        port.getBerths()[i].getShip().getShipWidth());
            }
        }
        Iterator<String> iterator = que1.iterator();
        File file = new File(InputParameters.OUTPUTPATH+"Berth/"+Integer.toString((int)simulationRealTime)+ ".txt");
        if (file.exists() != true) {
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
            writer = new BufferedWriter(fw);
            while (iterator.hasNext()) {
                writer.write(iterator.next());
                writer.newLine();//换行
            }
            writer.flush();
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } finally {
            try {
                writer.close();
                fw.close();
            } catch (IOException e) {
            }
        }
        que1.clear();
    }
    private static void outputCurrentQuayCraneXY(Port port) {
        Queue<String> que = new ArrayDeque<>();
        for (QuayCrane QC : port.getQuayCranes()) {
            if(QC.isGantryTrolleyFree() == true && QC.isMainTrolleyFree() == true){
                //空闲状态;
                que.add(QC.getNumber()+"\t"+QC.getLocation().getX()+"\t"+QC.getLocation().getY()+"\t"+0);
            }else{
                //工作状态;
                que.add(QC.getNumber()+"\t"+QC.getLocation().getX()+"\t"+QC.getLocation().getY()+"\t"+1);
            }
        }
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+"AllQCs_XY/"+Integer.toString((int)simulationRealTime)+".txt");
        if (file.exists() != true) {
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
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
    private static void outputSimulatorTimeAndSpeed(Port port){
        Queue<String> que = new ArrayDeque<>();
        que.add(port.getSimulatorTime()+" ");
        que.add(InputParameters.getSimulationSpeed()+" ");
        que.add(simulationRealTime+" ");
        Iterator<String> iterator = que.iterator();
        File file = new File(InputParameters.OUTPUTPATH+"CurrentSimulatorTime"+".txt");
        if (file.exists() != true) {
            file.getParentFile().mkdirs();
        }
        FileWriter fw = null;
        BufferedWriter writer = null;
        try {
            fw = new FileWriter(file);
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
    public static String[][] getShipArrivingList(){
        File file = new File(OUTPUTPATH + "ShipArrivingList.txt");  //存放数组数据的文件  
        String[][] arr = new String[5][];
        try (BufferedReader rea = new BufferedReader(new FileReader(file))) {
            String line;  //一行数据
            int row1 = 0;
            //line = rea.readLine();//第一行是变量名;
            //逐行读取，并将每个数组放入到数组中
            int row = 0;
            while ((line = rea.readLine()) != null) {
                row++;
            }
            arr = new String[row][3]; //插入的数组  
            for (int i = 0; i < row; i++) {
                for (int j = 0; j < 3; j++) {
                    arr[i][j] = "-";
                }
            }
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OutputParameters.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OutputParameters.class.getName()).log(Level.SEVERE, null, ex);
        }
        try (BufferedReader rea = new BufferedReader(new FileReader(file))) {
            String line;  //一行数据
            int row1 = 0;
            //line = rea.readLine();//第一行是变量名;
            //逐行读取，并将每个数组放入到数组中
            while ((line = rea.readLine()) != null) {
                String[] temp = line.split("\t");
                for (int j = 0; j < temp.length; j++) {
                    arr[row1][j] = temp[j];
                }
                row1++;
            }
            return arr;
        } catch (FileNotFoundException ex) {
            Logger.getLogger(OutputParameters.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(OutputParameters.class.getName()).log(Level.SEVERE, null, ex);
        }
        return arr;
    }
    
        
}
