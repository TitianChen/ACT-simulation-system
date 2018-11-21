/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package storageblock;

import CYT_model.Point2D;
import parameter.InputParameters;

/**
 * 是container class里面的一个属性;
 * 描述集装箱在堆场中箱位的位置类;
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 */
public class Slot {
    private Yard yard;//block编号;
    private Block block;//段编号;一段堆场两个RMG
    private int rowNum;//行编号;
    private int bayNum;//贝位编号;20ft--奇数,40ft--偶数;
    private int heightNum;//高度编号;
    private Point2D centralLocation;//中心点location;
    public Slot(Yard yard,Block block,int rowNum,int bayNum,int heightNum){
        this.yard = yard;
        this.block = block;
        this.rowNum = rowNum;
        if(this.rowNum>InputParameters.getPerYCRowNum() || bayNum > (InputParameters.getTotalBayNum()*2-1) 
                || this.rowNum < 1 || bayNum < 1){
            System.out.println("!!!this.rowNum > PerYCRowNum!!!：：：："+rowNum+"bayNum："+bayNum);
            System.out.println("!!!Error:Slot()!!!");
            throw new UnsupportedOperationException("Error:Slot.");
        }
        this.bayNum = bayNum;
        this.heightNum = heightNum;
        this.caculateLocation();
    }

    public Slot(Slot slot) {
        this.yard = slot.yard;
        this.block = slot.block;
        this.rowNum = slot.rowNum;
        if(this.rowNum>InputParameters.getPerYCRowNum() || slot.bayNum > (InputParameters.getTotalBayNum()*2-1) 
                || this.rowNum < 1 || slot.bayNum < 1){
            System.out.println("!!!this.rowNum > PerYCRowNum!!!：：：："+slot.rowNum+"bayNum："+slot.bayNum);
            System.out.println("!!!Error:Slot()!!!");
            throw new UnsupportedOperationException("Error:Slot.");
        }
        this.bayNum = slot.bayNum;
        this.heightNum = slot.heightNum;
        this.caculateLocation();
    }

    Slot(Slot slot1, Slot slot2) {
        this.yard = slot1.yard;
        this.block = slot1.block;
        this.rowNum = slot1.rowNum;
        if(this.rowNum>InputParameters.getPerYCRowNum() || slot1.bayNum > (InputParameters.getTotalBayNum()*2-1) 
                || this.rowNum < 1 || slot1.bayNum < 1){
            System.out.println("!!!this.rowNum > PerYCRowNum!!!：：：："+slot1.rowNum+"bayNum："+slot1.bayNum);
            System.out.println("!!!Error:Slot1()!!!");
            throw new UnsupportedOperationException("Error:Slot1.");
        }
        if(this.rowNum>InputParameters.getPerYCRowNum() || slot2.bayNum > (InputParameters.getTotalBayNum()*2-1) 
                || this.rowNum < 1 || slot2.bayNum < 1){
            System.out.println("!!!this.rowNum > PerYCRowNum!!!：：：："+slot2.rowNum+"bayNum："+slot2.bayNum);
            System.out.println("!!!Error:Slot2()!!!");
            throw new UnsupportedOperationException("Error:Slot.");
        }
        this.bayNum = (slot1.bayNum+slot2.bayNum)/2;
        this.heightNum = slot1.heightNum;
        this.caculateLocation();
    }
    /**
     * 计算得到Slot中心点位置;
     */
    private void caculateLocation(){
        centralLocation = new Point2D.Double(0, 0);
        double d = 0.5*(InputParameters.getRMGtrackGuage()-InputParameters.getPerYCRowNum()*InputParameters.getWidthOfContainer()
                -InputParameters.getDistanceOfContainers()*(InputParameters.getPerYCRowNum()-1));
        double x = block.getP2().getX()-d-(0.5+(this.rowNum-1))*InputParameters.getWidthOfContainer()
                -InputParameters.getDistanceOfContainers()*(this.rowNum-1);
        double y = block.getP1().getY()+0.5*this.bayNum*InputParameters.getLengthOf20FTContainer()
                +InputParameters.getDistanceOfContainers()*0.5*(this.bayNum-1);
        this.getCentralLocation().setLocation(x, y);        
    }
    @Override
    public String toString(){
        return this.yard.getAreaNum()+" Block"+this.block.getLine()+
                " row("+this.rowNum+")bay("+this.bayNum+")height("+this.heightNum+")";
    }

    /**
     * @return the yardNum
     */
    public Object getYardNum() {
        return getYard().getAreaNum();
    }

    /**
     * @return the sectionNum
     */
    public Block getBlock() {
        return block;
    }

    /**
     * @return the rowNum
     */
    public int getRowNum() {
        return rowNum;
    }

    /**
     * @return the bayNum
     */
    public int getBayNum() {
        return bayNum;
    }

    /**
     * @return the heightNum
     */
    public int getHeightNum() {
        return heightNum;
    }

    /**
     * @return the centralLocation
     */
    public Point2D getCentralLocation() {
        return centralLocation;
    }

    /**
     * @return the yard
     */
    public Yard getYard() {
        return yard;
    }
    
}
