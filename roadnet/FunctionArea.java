/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roadnet;

import CYT_model.Point2D;


/**
 *
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 */
//用于其他功能区继承或者直接使用;
public abstract class FunctionArea extends Rectangle{
    String areaNum;//功能区编号;
    private final String areaName; 
    /**
     *
     * @param p1
     * @param p2
     * @param areanum
     * @param areaname
     */
    public FunctionArea(Point2D p1,Point2D p2,String areanum,String areaname){
        super(p1,p2);
        this.areaNum = areanum;
        this.areaName = areaname;
        /////要改进，要加坐标否？？？
    }
    /////位置坐标？
    public void setAreaNum(String num)
    {
        areaNum = num;
    }
    public String getAreaNum()
    {
        return this.areaNum;
    }
    
    
}
