package ship;

import CYT_model.Container;
import Vehicle.AGV;
import apron.QuayCrane;
import java.io.*;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import parameter.InputParameters;
import static parameter.InputParameters.getHaveTEUProp;
import static parameter.InputParameters.getLoadProp;
import static parameter.InputParameters.getNormalRandom;
import static parameter.InputParameters.getUnloadProp;
import parameter.OutputParameters;
import parameter.StaticName;
import storageblock.Block;
import storageblock.Yard;

/**
 * Class Ship的属性之一，保存Ship中的所有集装箱信息;
 * @author YutingChen, yuting.chen17@imperial.ac.uk Dalian University of Technology
 */
public class ShipContainers {

    private final Ship ship;
    //Infromation of Container[](NeedLoad && NeedUnload);
    private Container[] ContainersTotalNeedLoad;//总共需要装的箱;-----由Port.Yard 生成,ShipContainers直接饮用;
    private Container[] ContainersTotalNeedUnload;//总共需要卸的箱;-----由Ship new 新生成;
    //现有箱信息;
    private int present_total_20ft;
    private int present_total_40ft;
    private double[] situation_present;//包括尺寸、位置、是否已完成装卸等;
    //需装箱信息;
    private int total_load_num;//总共需装箱数量
    private int present_needload_num;//现需装箱数量;
    private int present_needload_20ft;//20ft需装箱;
    private int present_needload_40ft;//40ft需装箱;
    //需卸箱信息;
    private int total_unload_num;//总共需卸箱数量;
    private int present_needunload_num;//现需卸箱数量;
    private int present_needunload_20ft;//20ft;
    private int present_needunload_40ft;//40ft;
    //概率分布生成各参数并存储至txt中;
    private int total_load_TEU;
    private int total_unload_TEU;

    public ShipContainers(Ship ship) {
        this.ship = ship;
        //构造;
        present_total_20ft = (int)(ship.getMaxTEU()*getHaveTEUProp()*InputParameters.getProportionOf20());
        present_total_40ft = (int)((ship.getMaxTEU()*getHaveTEUProp()*(1-InputParameters.getProportionOf20()))/2);
        present_needload_20ft = (int) (present_total_20ft*getLoadProp()*getNormalRandom(1,0.33,0.9,1.1));
        present_needload_40ft = (int)(present_total_40ft*getLoadProp()*getNormalRandom(1,0.33,0.9,1.1));
        total_load_num = present_needload_20ft+present_needload_40ft;
        total_load_TEU = present_needload_20ft+present_needload_40ft*2;
        present_needload_num = total_load_num;
        present_needunload_20ft = (int) (present_total_20ft*getUnloadProp()*getNormalRandom(1,0.33,0.9,1.1));
        present_needunload_40ft = (int)(present_total_40ft*getUnloadProp()*getNormalRandom(1,0.33,0.9,1.1));
        total_unload_num = present_needunload_20ft+present_needunload_40ft;
        total_unload_TEU = present_needunload_20ft+present_needunload_40ft*2;
        present_needunload_num = total_unload_num;
        //new 需要装船的船;
        //目前默认有装船任务;
        this.setContainersTotalNeedLoad();
        //new 需要卸船的船;目前默认有卸船任务;
        if (this.total_load_num != 0) {
            this.ContainersTotalNeedUnload = new Container[this.total_unload_num];
            for (int i = 0; i < this.present_needunload_20ft; i++) {
                this.ContainersTotalNeedUnload[i] = new Container(StaticName.SIZE20FT, StaticName.ONSHIP, this.ship);
            }
            for (int i = this.present_needunload_20ft; i < this.total_unload_num; i++) {
                this.ContainersTotalNeedUnload[i] = new Container(StaticName.SIZE40FT, StaticName.ONSHIP, this.ship);
            }
        } else {
            System.out.println(this.ship.toString() + "!!!!!该船没有卸船任务!!!!!");
        }
//this.outputContainers();
    }
    private void setContainersTotalNeedLoad() {
        this.ContainersTotalNeedLoad = new Container[this.total_load_num];
        //开始New outSide的container;
        int outsideNUM40 = this.present_needload_40ft;
        int outsideNUM20 = this.present_needload_20ft;
        int num40 = 0;
        int num20 = 0;
        if (outsideNUM40 + outsideNUM20 == 0) {
            System.out.println(this.ship.toString() + "没有ShipContainers.setLoadingContainersOutSide");
            return;
        }
        for (int i = 0; i < ContainersTotalNeedLoad.length; i++) {
            double random1 = Math.random();
            if (random1 < (outsideNUM40 / (outsideNUM40 + outsideNUM20))) {
                if (num40 < outsideNUM40) {
                    ContainersTotalNeedLoad[i] = new Container(StaticName.SIZE40FT, StaticName.OUTSIDE);
                    num40++;
                } else {
                    ContainersTotalNeedLoad[i] = new Container(StaticName.SIZE20FT, StaticName.OUTSIDE);
                    num20++;
                }
            } else {
                if (num20 < outsideNUM20) {
                    this.ContainersTotalNeedLoad[i] = new Container(StaticName.SIZE20FT, StaticName.OUTSIDE);
                    num20++;
                } else {
                    this.ContainersTotalNeedLoad[i] = new Container(StaticName.SIZE40FT, StaticName.OUTSIDE);
                    num40++;
                }
            }
        }
//System.out.println(this.ship.toString() + "ShipContainers.setLoadingContainersOutSide()成功");
    }

    /**
     * @param size Container尺寸;
     * @param num Container数量;
     * @param QC
     * @return null:失败 OR container[]:成功
     */
    public synchronized Container[] ObtainUnloadingContainersOnQC(double size, final int num, QuayCrane QC) {
        Container[] containers = new Container[num];
        int num0 = 0;
        for (Container containersTotalNeedUnload : this.getContainersTotalNeedUnload()) {
            if (containersTotalNeedUnload.getState().equals(StaticName.ONQC)) {
                if (containersTotalNeedUnload.getFTsize() == size) {
                    if (containersTotalNeedUnload.getServiceQC() != null
                            && containersTotalNeedUnload.getServiceQC().toString().equals(QC.toString()) == true) {
                        containers[num0] = containersTotalNeedUnload;
                        num0++;
                        if (num0 == num) {
                            return containers;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * @param size Container尺寸;
     * @param num Container数量;
     * @param QC
     * @return container[]:成功,null:失败
     */
    public synchronized Container[] ObtainLoadingContainersOnQC(double size, int num, QuayCrane QC) {
        Container[] containers = new Container[num];
        int num0 = 0;
        for (Container containersTotalNeedLoad : this.getContainersTotalNeedLoad()) {
            if (containersTotalNeedLoad != null && containersTotalNeedLoad.getState().equals(StaticName.ONQC)) {
                if (containersTotalNeedLoad.getFTsize() == size) {
                    if (containersTotalNeedLoad.getServiceQC() != null
                            && containersTotalNeedLoad.getServiceQC().toString().equals(QC.toString()) == true) {
                        containers[num0] = containersTotalNeedLoad;
                        num0++;
                        if (num0 == num) {
                            return containers;
                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获得state=OnStocking的LoadingContainers[] (包括已分配AGV 和 没有分配AGV 的 Container)
     *
     * @return 有箱：container[] 无箱：null;
     */
    public Container[] getLoadingContainersOnStocking() {
        int totalNum = this.getContainersTotalNeedLoad().length;
        int num = 0;
        int itag = 0;
        for (int i = 0; i < totalNum; i++) {
            if (this.getContainersTotalNeedLoad()[i].getState().equals(StaticName.ONSTOCKING) == true) {
                num++;
            }
        }
        if (num == 0) {
            return null;
        }
        Container[] containers = new Container[num];
        int num0 = 0;
        for (int i = 0; i < totalNum; i++) {
            if (this.getContainersTotalNeedLoad()[i].getState().equals(StaticName.ONSTOCKING) == true) {
                containers[num0] = this.getContainersTotalNeedLoad()[i];
                num0++;
            }
        }
        return containers;
        //////////////////////////////////////////////////////////////////添加状态OnYC后该状态函数需修改????
    }

    /**
     * 获得所有标准箱量;(20ft-1TEU)
     *
     * @return
     */
    public double getLoadingContainerOutsideTEU() {
        double teu = 0;
        int totalNum = this.getContainersTotalNeedLoad().length;
        int itag = 0;
        for (int i = 0; i < totalNum; i++) {
            if (this.getContainersTotalNeedLoad()[i].getState().equals(StaticName.OUTSIDE) == true) {
                if (this.getContainersTotalNeedLoad()[i].size == 20) {
                    teu++;
                } else if (this.getContainersTotalNeedLoad()[i].size == 40) {
                    teu += 2;
                }
            }
        }
        return teu;
    }

    /**
     * 获得state=Outside的LoadingContainers[]
     *
     * @return 有箱：container[] 无箱：null;
     */
    public Container[] getLoadingContainersOutside() {
        int totalNum = this.getContainersTotalNeedLoad().length;
        int num = 0;
        int itag = 0;
        for (int i = 0; i < totalNum; i++) {
            if (this.getContainersTotalNeedLoad()[i].getState().equals(StaticName.OUTSIDE) == true) {
                num++;
            }
        }
        if (num == 0) {
            return null;
        }
        Container[] containers = new Container[num];
        int num0 = 0;
        for (int i = 0; i < totalNum; i++) {
            if (this.getContainersTotalNeedLoad()[i].getState().equals(StaticName.OUTSIDE) == true) {
                containers[num0] = this.getContainersTotalNeedLoad()[i];
                num0++;
            }
        }
        return containers;
    }

    public Container[] getandSetOnTruck_Max2TEUOutsideContainers() {
        int num20 = 0;
        if (this.getLoadingContainersOutside() == null) {
//System.out.println("!!!NULL:1*40ft ShipContainers.getMax2TEUOutsideContainers()!!!");
            return null;
        }
        Container[] containers = new Container[2];
        for (Container loadingContainersOutside : this.getLoadingContainersOutside()) {
            if (loadingContainersOutside.getFTsize() == 40 && loadingContainersOutside.getState().equals(StaticName.OUTSIDE) == true) {
                containers = new Container[1];
                containers[0] = loadingContainersOutside;
//System.out.println("!!!成功:1*40ft ShipContainers.getMax2TEUOutsideContainers()!!!");
                containers[0].setState(StaticName.ONTRUCK);
                return containers;
            } else if (loadingContainersOutside.getFTsize() == 20 && loadingContainersOutside.getState().equals(StaticName.OUTSIDE) == true) {
                containers[num20] = loadingContainersOutside;
                num20++;
                if (num20 == 2) {
//System.out.println("!!!成功:2*20ft ShipContainers.getMax2TEUOutsideContainers()!!!");
                    containers[0].setState(StaticName.ONTRUCK);
                    containers[1].setState(StaticName.ONTRUCK);
                    return containers;
                }
            }
        }
        if (num20 == 1) {
            Container[] container20 = new Container[1];
            container20[0] = containers[0];
            containers[0].setState(StaticName.ONTRUCK);
//System.out.println("!!!成功:1*20ft ShipContainers.getMax2TEUOutsideContainers()!!!");
            return container20;
        }
        System.out.println("!!!Error:ShipContainers.getMax2TEUOutsideContainers()!!!");
        throw new UnsupportedOperationException("Error:Error:ShipContainers.getMax2TEUOutsideContainers().");
    }

    /**
     * @param size Container尺寸;
     * @param num Container数量;
     * @param QC
     * @return container[]:成功,null:失败
     */
    public Container[] SetUnloadingContainersOnShipToQuayCrane(double size, int num, QuayCrane QC) {
        switch (num) {
            case 1:
                for (Container containersTotalNeedUnload : this.getContainersTotalNeedUnload()) {
                    if (containersTotalNeedUnload.getState().equals(StaticName.ONSHIP)) {
                        if (containersTotalNeedUnload.getFTsize() == size) {
                            containersTotalNeedUnload.setState(StaticName.ONQC);
                            containersTotalNeedUnload.setServiceQC(QC);
//System.out.println("SetUnloadingContainersOnShipToQuayCrane成功");
                            Container[] containers = new Container[1];
                            containers[0] = containersTotalNeedUnload;
                            return containers;
                        }
                    }
                }
                //OutputParameters.outputAllUnloadingState(QC.getServiceShip());
                return null;
            case 2:
                int num0 = 0;
                Container[] containers = new Container[2];
                for (Container containersTotalNeedUnload : this.getContainersTotalNeedUnload()) {
                    if (containersTotalNeedUnload.getState().equals(StaticName.ONSHIP)) {
                        if (containersTotalNeedUnload.getFTsize() == size) {
                            containersTotalNeedUnload.setState(StaticName.ONQC);
                            containersTotalNeedUnload.setServiceQC(QC);
                            containers[num0] = containersTotalNeedUnload;
                            num0++;
                            if (num0 == num) {
//System.out.println("SetTwoUnloadingContainersOnShipToQuayCrane成功");
                                return containers;
                            }
                        }
                    }
                }
                //OutputParameters.outputAllUnloadingState(QC.getServiceShip());
                return null;
            default:
                throw new UnsupportedOperationException("!!!Error:ShipContainers."
                        + "SetOneUnloadingContainersOnShipToQuayCrane()!!!");
        }
    }

    /**
     * 改变QC上的container的状态;(setState == Onship，setServiceQC == null)
     *
     * @param size
     * @param num
     * @param QC
     * @param ship
     * @return boolean 是否设置成功：：true
     * @exception UnsupportedOperationException
     */
    public synchronized boolean SetLoadingContainersOnQuayCraneToShip(double size, int num, QuayCrane QC, Ship ship) {
        int num0 = 0;
        for (Container containersTotalNeedLoad : this.getContainersTotalNeedLoad()) {
            if (containersTotalNeedLoad != null && containersTotalNeedLoad.getState().equals(StaticName.ONQC) 
                    && containersTotalNeedLoad.getServiceQC() != null 
                    && containersTotalNeedLoad.getServiceQC().toString().equals(QC.toString()) == true) {
                if (containersTotalNeedLoad.getFTsize() == size) {
                    containersTotalNeedLoad.setState(StaticName.ONSHIP);
                    containersTotalNeedLoad.setServiceQC(null);
                    num0++;
                    if (num0 == num) {
//System.out.println("SetLoadingContainersOnQCToShip成功");
                        return true;
                    }
                }
            }
        }
//System.out.println("!!!!!!!!!!!SC:!!!!!!!!SetLoadingContainersOnQuayCraneToShip 失败！!!!!!!!!!!!!!！");
        return false;
        
       // throw new UnsupportedOperationException("!!!Error:ShipContainers.SetUnloadingContainersOnShipToQuayCrane()!!!");
    }

    /**
     * 从AGV到QC QC获得containers Loading 装船;
     *
     * @param containers
     * @param agv
     */
    public void setLoadingContainersOnAGVToQuayCrane(Container[] containers, AGV agv) {
        int num = containers.length;
        for (int i = 0; i < num; i++) {
            containers[i].setServiceAGV(null);
            containers[i].setState(StaticName.ONQC);
            containers[i].setServiceQC(agv.getServiceQC());
            return;
        }
        System.out.println("Error:ShipContainers.setLoadingContainersOnAGVToQuayCrane()");
        throw new UnsupportedOperationException("!!!Error:ShipContainers.setLoadingContainersOnAGVToQuayCrane()!!!");
    }

    /**
     * 是否所有需要装船的箱都已经在该船舶上了？？
     *
     * @return true false
     */
    public boolean allLoadingContainersOnShip() {
        if (this.ship.getShipContainers().ContainersTotalNeedLoad == null) {
            return true;
        }
        int num = this.ship.getShipContainers().ContainersTotalNeedLoad.length;
        for (int i = 0; i < num; i++) {
            if(this.ship.getShipContainers().ContainersTotalNeedLoad[i] == null){
                return false;
            }
            if (this.ship.getShipContainers().ContainersTotalNeedLoad[i] != null
                    && this.ship.getShipContainers().ContainersTotalNeedLoad[i].getState().equals(StaticName.ONSHIP) == false) {
//System.out.print("SSSSSSSSSSSShip:"+this.ship.toString()+this.ship.getShipContainers().ContainersTotalNeedLoad[i].getState());
                return false;
            }
        }
        return true;
    }
    
    
    /**
     * set all contaienrs need to load on ship already
     */
    public void setAllLoadingOnShip() {
        if (this.ship.getShipContainers().ContainersTotalNeedLoad == null) {
            return;
        }
        int num = this.ship.getShipContainers().ContainersTotalNeedLoad.length;
        for (int i = 0; i < num; i++) {
            if (this.ship.getShipContainers().ContainersTotalNeedLoad[i] != null
                    && this.ship.getShipContainers().ContainersTotalNeedLoad[i].getState().equals(StaticName.ONSHIP) == false) {
                Container[] con = new Container[1];
                con[0] = ship.getShipContainers().ContainersTotalNeedLoad[i];
                for (Yard yard : ship.getPort().getYards()) {
                    for(Block block:yard.getBlock()){
                        block.move1Or2MaybeContainer(con);
                    }
                }
                this.ship.getShipContainers().ContainersTotalNeedLoad[i].setState(StaticName.ONSHIP);
            }
        }
    }

    /**
     * @return the present_total_20ft
     */
    public int getPresent_total_20ft() {
        return this.present_total_20ft;
    }

    /**
     * @return the present_total_40ft
     */
    public int getPresent_total_40ft() {
        return this.present_total_40ft;
    }

    /**
     * @return the situation_present
     */
    public double[] getSituation_present() {
        return this.situation_present;
    }

    /**
     * @return the total_load_num
     */
    public int getTotal_load_num() {
        return this.total_load_num;
    }

    /**
     * @return the present_needload_num
     */
    public int getPresent_needload_num() {
        return this.present_needload_num;
    }

    /**
     * @return the present_needload_20ft
     */
    public int getPresent_needload_20ft() {
        return this.present_needload_20ft;
    }

    /**
     * @return the present_needload_40ft
     */
    public int getPresent_needload_40ft() {
        return this.present_needload_40ft;
    }

    /**
     * @return the total_unload_num
     */
    public int getTotal_unload_num() {
        return this.total_unload_num;
    }

    /**
     * @return the present_needunload_num
     */
    public int getPresent_needunload_num() {
        return this.present_needunload_num;
    }

    /**
     * @return the present_needunload_20ft
     */
    public int getPresent_needunload_20ft() {
        return this.present_needunload_20ft;
    }

    /**
     * @return the present_needunload_40ft
     */
    public int getPresent_needunload_40ft() {
        return this.present_needunload_40ft;
    }

    /**
     * @param num +:多，-：少
     */
    public void setPresent_total_20ft(int num) {
        this.present_total_20ft += num;
    }

    /**
     * @param num +:多，-：少
     */
    public void setPresent_total_40ft(int num) {
        this.present_total_40ft += num;
    }

    /**
     * @param situation_present the situation_present to set
     */
    public void setSituation_present(double[] situation_present) {
        this.situation_present = situation_present;
    }

    /**
     * @param total_load_num the total_load_num to set
     */
    public void setTotal_load_num(double total_load_num) {
        this.total_load_num = (int) total_load_num;
    }

    /**
     * @param num 负数表示完成一个，需要装卸的量减少一个;
     */
    public void setPresent_needload_num(double num) {
        this.present_needload_num += num;
    }

    /**
     * @param num 负数表示完成一个，需要装卸的量减少一个;
     */
    public synchronized void setPresent_needload_20ft(double num) {
        this.present_needload_20ft += num;
    }

    /**
     * @param num 负数表示完成一个，需要装卸的量减少一个;
     */
    public synchronized void setPresent_needload_40ft(double num) {
        this.present_needload_40ft += num;
    }

    /**
     * @param total_unload_num the total_unload_num to set
     */
    public synchronized void setTotal_unload_num(double total_unload_num) {
        this.total_unload_num = (int) total_unload_num;
    }

    /**
     * @param num 负数表示完成一个，需要装卸的量减少一个;
     */
    public synchronized void setPresent_needunload_num(double num) {
        this.present_needunload_num += num;
//        if(this.present_needunload_num<0){
//            System.out.println("!!!Error:SC:setPresent_needunload_num!!!:::"+this.ship);
//            throw new UnsupportedOperationException("!!!Error:Port.getAGVBufferArea()!!!");
//        }
    }

    /**
     * @param num 负数表示完成一个，需要装卸的量减少一个;
     */
    public synchronized void setPresent_needunload_20ft(double num) {
        this.present_needunload_20ft += num;
//        if(this.present_needunload_20ft<0){
//            System.out.println("!!!Error:SC:setPresent_needunload_20ft!!!:::"+this.ship);
//            throw new UnsupportedOperationException("!!!Error:Port.getAGVBufferArea()!!!");
//        }
    }

    /**
     * @param num 负数表示完成一个，需要装卸的量减少一个;
     */
    public synchronized void setPresent_needunload_40ft(double num) {
        this.present_needunload_40ft += num;
//        if(this.present_needunload_40ft<0){
//            System.out.println("!!!Error:SC:setPresent_needunload_40ft!!!:::"+this.ship);
//            throw new UnsupportedOperationException("!!!Error:Port.getAGVBufferArea()!!!");
//        }
    }

    /**
     * @return the ContainersTotalNeedUnload
     */
    public Container[] getContainersTotalNeedUnload() {
        return ContainersTotalNeedUnload;
    }

    /**
     * @return the ContainersTotalNeedLoad
     */
    public Container[] getContainersTotalNeedLoad() {
        return ContainersTotalNeedLoad;
    }

    public boolean haveUnloadingContainersOnShip() {
        for (Container containersTotalNeedUnload : this.getContainersTotalNeedUnload()) {
            if (containersTotalNeedUnload.getState().equals(StaticName.ONSHIP)) {
                return true;
            }
        }
        return false;
    }

    public int getUnloadTEU() {
        int TEU = 0;
        for (Container container : this.getContainersTotalNeedUnload()) {
            switch ((int)container.size) {
                case 20:
                    TEU++;
                    break;
                case 40:
                    TEU += 2;
                    break;
                default:
                    System.out.println("!!!Error:SC:getUnloadTEU!!!:::"+this.ship);
                    throw new UnsupportedOperationException("!!!Error:SC.getUnloadTEU()!!!");
            }
        }
        return TEU;
    }

    public int getLoadTEU() {
        int TEU = 0;
        for (Container container : this.getContainersTotalNeedLoad()) {
            switch ((int)container.size) {
                case 20:
                    TEU++;
                    break;
                case 40:
                    TEU += 2;
                    break;
                default:
                    System.out.println("!!!Error:SC:getUnloadTEU!!!:::"+this.ship);
                    throw new UnsupportedOperationException("!!!Error:SC.getUnloadTEU()!!!");
            }
        }
        return TEU;
        
    }

    /**
     * @return the total_load_TEU
     */
    public int getTotal_load_TEU() {
        return total_load_TEU;
    }

    /**
     * @return the total_unload_TEU
     */
    public int getTotal_unload_TEU() {
        return total_unload_TEU;
    }

    public void clearNeedLoadNum() {        
        this.setAllLoadingOnShip();
        this.present_needload_20ft = 0;
        this.present_needload_40ft = 0;
        this.present_needload_num = 0;
    }

    /**
     * 清除所有需要离港的集装箱;
     * 讲道理这部不应该存在的
     * 但是为了避免偶尔出现的闹心Bug!!!!
     * 加上吧
     */
    public void clearNeedLeavingPort() {
        /////上面这个不要弄了！！！！！
//        for (Yard yard : ship.getPort().getYards()) {
//            for (Block block : yard.getBlock()) {
//                double itag = 0;
//                while (block.moveProblemContainer() == false) {
//                    itag++;
//                    if (itag > 100) {
//                        break;
//                        //别循环太多，老娘真怕出问题;= =
//                    }
//                }
//            }
//        }
        for (Container ContainersTotalNeedUnload1 : this.ship.getShipContainers().ContainersTotalNeedUnload) {
            if (ContainersTotalNeedUnload1 != null && (ContainersTotalNeedUnload1.getState().equals(StaticName.OUTSIDE) == false 
                    && ContainersTotalNeedUnload1.getState().equals(StaticName.ONTRUCK) == false)) {
                for (Yard yard : ship.getPort().getYards()) {
                    Container[] con = new Container[1];
                    con[0] = ContainersTotalNeedUnload1;
                    for(Block block:yard.getBlock()){
                        block.move1Or2MaybeContainer(con);
                    }
                }
                ContainersTotalNeedUnload1.setState(StaticName.OUTSIDE);
            }
        } 
    }

}
