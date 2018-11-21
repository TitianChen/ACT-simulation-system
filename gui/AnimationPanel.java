/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import apron.Berth;
import CYT_model.Container;
import CYT_model.Point2D;
import CYT_model.Port;
import apron.QuayCrane;
import Vehicle.AGV;
import java.awt.BasicStroke;
import static java.awt.BasicStroke.CAP_ROUND;
import static java.awt.BasicStroke.JOIN_ROUND;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.geom.AffineTransform;
import javax.swing.JPanel;
import parameter.InputParameters;
import static parameter.InputParameters.getDistanceOfContainers;
import static parameter.InputParameters.getLengthOf40FTContainer;
import static parameter.InputParameters.getLengthofContainer;
import static parameter.InputParameters.getWidthOfContainer;
import parameter.StaticName;
import roadnet.RoadSection;
import ship.Ship;
import storageblock.AGVCouple;
import storageblock.Block;
import storageblock.Yard;
import storageblock.YardCrane;

/**
 * 动画面板类 实现Runnable接口
 * @author YutingChen, 760698296@qq.com Dalian University of Technology
 */
class AnimationPanel extends JPanel implements Runnable {

    private static Port port;
    private static final long serialVersionUID = 1L;
    private static AffineTransform imageTransform;
    private static AffineTransform stringTransform;
    private int minx = 0;
    private int miny = 0;
    private int maxx = 0;
    private int maxy = 0;
    private static AnimationGUI gui;
    

    public AnimationPanel(Port port0,AnimationGUI gui) {
        super();
        port = port0;
        AnimationPanel.gui = gui;
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        super.setLayout(gridbag);
        ///确定合适长宽;
        minx = (int)(port.getBerths()[0].getLocation().getX()-40-40);
        maxx = (int)(port.getYards()[0].P2.getX()+40);
        miny = (int)(port.getRoadNetWork().getMiny()-25);
        maxy = (int)(port.getRoadNetWork().getMaxy()+25);
    }

    /**
     * Paint码头整体系统动画
     *
     * @param g Garphics
     */
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //HSB:红，黑。
        this.setBackground(Color.getHSBColor((float)0.1, (float)0, (float)0.8));
        this.setBackground(CYTMainGUI.getBgC());//设定背景颜色  
        Graphics2D graphics2D = (Graphics2D)g;
        
        minx = (int)(port.getBerths()[0].getLocation().getX());//-40-40);
        maxx = (int)(port.getYards()[0].P2.getX());//+40);
        miny = (int)(port.getRoadNetWork().getMiny());//-25);
        maxy = (int)(port.getRoadNetWork().getMaxy());//+25);
        
        imageTransform = new AffineTransform();
        stringTransform = new AffineTransform();  
        imageTransform.setToScale(gui.scale, gui.scale);
        imageTransform.rotate(3*Math.PI/2, (minx+maxx)/2, (miny+maxy)/2);
        imageTransform.translate(minx+(this.getHeight())/2+gui.deltaX,miny+(this.getWidth())/2+gui.deltaY);   
        
        //imageTransform.translate(-maxy,minx); 
        stringTransform.translate(imageTransform.getTranslateX(), imageTransform.getTranslateY());
        graphics2D.setTransform(imageTransform);
   
        //画堆场布局;
        DrawYardLayout(graphics2D);
        //画道路;
        DrawRoadNet(graphics2D);
         //画船舶;
        DrawShips(graphics2D);
        //画泊位;
        DrawBerths(graphics2D);
        //画AGV;
        DrawAGVs(graphics2D);
        //画场桥和场桥上的集装箱;
        DrawQCsAndContainers(graphics2D);
        //画堆场集装箱;
        DrawContainersOnYard(graphics2D);
//        //画AGV上的集装箱;
//        DrawContainersOnAGV(graphics2D);
        //画场桥上的集装箱;
        DrawContainersOnYC(graphics2D);
        //画AGV伴侣;
        DrawAGVCouple(graphics2D);
        //画岸桥;
        DrawYCs(graphics2D);
//        //画集卡;
//        DrawTrucks(graphics2D);
        graphics2D.setColor(Color.yellow);//设置画笔颜色  
    }

    //动画过程在线程内实现  
    @Override
    public void run() {
        while (true) {
            try {  
                Thread.sleep(20);//暂停2秒  
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            }  
            repaint();//三秒后重新开始  
        }
    }

    private void DrawYCs(Graphics2D graphics2D) {
      
        for (YardCrane yardCrane : port.getYardCranes()) {
            //画RMG大门架子;
            double x = (int) yardCrane.currentPosition.getX();
            double y = (int) yardCrane.currentPosition.getY();
            double lengthx = (int) yardCrane.getTrackGuage();
            double lengthy = Math.max(2, Math.ceil(yardCrane.getWidth() / 10));
            if (yardCrane.isFree() == true) {
                graphics2D.setPaint(new GradientPaint((int) (x - 0.5 * lengthx), (int) (y - 0.5 * yardCrane.getWidth()), Color.BLACK,
                        (int) (x + 0.5 * lengthx), (int) (y + 0.5 * yardCrane.getWidth()), Color.BLACK));
                graphics2D.fillRect((int) (x - 0.5 * lengthx), (int) (y - 0.5 * yardCrane.getWidth()),
                        (int) (lengthx), (int) (lengthy));
                graphics2D.fillRect((int) (x - 0.5 * lengthx), (int) (y + 0.5 * yardCrane.getWidth() - lengthy),
                        (int) (lengthx), (int) (lengthy));
                graphics2D.fillRect((int) (x - 0.5 * lengthx), (int) (y - 0.5 * yardCrane.getWidth()),
                        1, (int) (yardCrane.getWidth()));
                graphics2D.fillRect((int) (x + 0.5 * lengthx) - 1, (int) (y - 0.5 * yardCrane.getWidth()),
                        1, (int) (yardCrane.getWidth()));
            } else {
                graphics2D.setPaint(new GradientPaint((int) (x - 0.5 * lengthx), (int) (y - 0.5 * yardCrane.getWidth()), Color.ORANGE,
                        (int) (x + 0.5 * lengthx), (int) (y + 0.5 * yardCrane.getWidth()), Color.ORANGE));
                graphics2D.fillRect((int) (x - 0.5 * lengthx), (int) (y - 0.5 * yardCrane.getWidth()),
                        (int) (lengthx), (int) (lengthy));
                graphics2D.fillRect((int) (x - 0.5 * lengthx), (int) (y + 0.5 * yardCrane.getWidth() - lengthy),
                        (int) (lengthx), (int) (lengthy));
                graphics2D.fillRect((int) (x - 0.5 * lengthx), (int) (y - 0.5 * yardCrane.getWidth()),
                        1, (int) (yardCrane.getWidth()));
                graphics2D.fillRect((int) (x + 0.5 * lengthx) - 1, (int) (y - 0.5 * yardCrane.getWidth()),
                        1, (int) (yardCrane.getWidth()));
            }

            //考虑一下要不要画轨道;
//            BasicStroke stroke111 = new BasicStroke(2);
//            graphics2D.setStroke(stroke111);
//            graphics2D.drawRect((int) (x - 0.5 * lengthx), (int) (y - 0.5 *lengthy),(int) (lengthx), (int) (lengthy));
//            graphics2D.setPaint(new GradientPaint((int) (x - 0.5 * lengthx), (int) (y - 0.5 * yardCrane.getWidth()), Color.ORANGE,
//                    (int) (x + 0.5 * lengthx), (int) (y + 0.5 * yardCrane.getWidth()), Color.ORANGE));
//            graphics2D.fillRect((int) (x - 0.5 * lengthx), (int) (y - 0.5 * yardCrane.getWidth()),
//                    (int) (lengthx), (int) (lengthy));
//            graphics2D.fillRect((int) (x - 0.5 * lengthx), (int) (y + 0.5 * yardCrane.getWidth() - lengthy),
//                    (int) (lengthx), (int) (lengthy));
//            graphics2D.fillRect((int) (x - 0.5 * lengthx), (int) (y - 0.5 * yardCrane.getWidth()),
//                    1, (int) (yardCrane.getWidth()));
//            graphics2D.fillRect((int) (x + 0.5 * lengthx) - 1, (int) (y - 0.5 * yardCrane.getWidth()),
//                    1, (int) (yardCrane.getWidth()));
            //画RMG吊具;
            x = (int) (yardCrane.calculateRowPosition(yardCrane.getCurrentRowPosition()));
            lengthx = (int) (InputParameters.getWidthOfContainer())+2;
            lengthy = yardCrane.getWidth()+lengthy;
            graphics2D.setColor(Color.BLUE);
            BasicStroke stroke = new BasicStroke(1);
            graphics2D.setStroke(stroke);
            graphics2D.setPaint(new GradientPaint((int) (x - 0.5 * lengthx), (int) (y - 0.5 * yardCrane.getWidth()), Color.BLUE,
                    (int) (x + 0.5 * lengthx), (int) (y + 0.5 * yardCrane.getWidth()), Color.BLUE));
//            graphics2D.fillRect((int) (x - 0.5 * lengthx), (int) (y - 0.5 *lengthy),(int) (lengthx), (int) (lengthy));
            graphics2D.drawRect((int) (x - 0.5 * lengthx), (int) (y - 0.5 * lengthy),(int) (lengthx), (int) (lengthy));
            
        }
        
    }

    private void DrawQCsAndContainers(Graphics2D graphics2D) {
        for (QuayCrane quayCrane : port.getQuayCranes()) {
            if (quayCrane.isGantryTrolleyFree() == false && quayCrane.isMainTrolleyFree() == true){
                //空闲;
                graphics2D.setColor(Color.RED);
            }if(quayCrane.isGantryTrolleyFree() == false && quayCrane.isMainTrolleyFree() == false){
                graphics2D.setColor(Color.BLUE);
            } else if(quayCrane.isGantryTrolleyFree() == true && quayCrane.isMainTrolleyFree() == false){
                //主小车不空闲;
                graphics2D.setColor(Color.ORANGE);
            }else{
                graphics2D.setColor(Color.BLACK);
            }
            double qcX = quayCrane.getLocation().getX();
            double qcY = quayCrane.getLocation().getY();
            double lengthx = (int) (quayCrane.getGauge());
            double lengthy = (int) (quayCrane.getGauge())/2;
//graphics2D.drawRect((int)qcX, (int)qcY, 20, 20);
            //画内外伸距部分;
            Color thisC1 = Color.BLACK;
            Color thisC2 = Color.GRAY;
            graphics2D.setColor(Color.BLUE);
            BasicStroke stroke = new BasicStroke((float)2);
            graphics2D.setStroke(stroke);
            graphics2D.setPaint(new GradientPaint((int)(qcX - quayCrane.getOutReach()), (int)(qcY-0.25 * lengthy), thisC1,
                    (int) (qcX + quayCrane.getGauge() + quayCrane.getInnerReach()), (int)(qcY + 0.25 * lengthy), thisC2));
            graphics2D.drawRect((int) (qcX - quayCrane.getOutReach()), (int)(qcY-0.25 * lengthy),
                    (int)(quayCrane.getOutReach() + quayCrane.getGauge() + quayCrane.getInnerReach()), (int)(0.5*lengthy));
            //画轨道部分;
            graphics2D.setPaint(new GradientPaint((int)qcX, (int)(qcY-0.5*lengthy), thisC1,
                    (int)(qcX + lengthx), (int)(qcY+0.5*lengthy), thisC2));
            graphics2D.fillRect((int)qcX, (int)(qcY-0.5*lengthy),(int)lengthx, (int)lengthy);
            double guageX = qcX+2;
            double guageY = qcY;
            double guageLengthX = lengthx-4;
            double guageLengthY = lengthy-4;
            graphics2D.setPaint(new GradientPaint((int) guageX, (int) (guageY - 0.5 * guageLengthY), Color.LIGHT_GRAY,
                    (int) (guageX + guageLengthX), (int) (guageY + 0.5 * guageLengthY), Color.LIGHT_GRAY));
            graphics2D.fillRect((int) guageX, (int) (guageY - 0.5 * guageLengthY), (int) guageLengthX, (int) guageLengthY);
            //画中转平台;
            double x = quayCrane.getExchangePlatformX() - getWidthOfContainer() - 0.5 * getDistanceOfContainers();
            double y = quayCrane.getLocation().getY() - 0.5*getLengthofContainer(40);
            double length = getLengthofContainer(40);
            double width = 2*getWidthOfContainer()+getDistanceOfContainers();
            stroke = new BasicStroke((float)2);
            graphics2D.setStroke(stroke);
            graphics2D.setPaint(new GradientPaint((int) (x), (int) (y), Color.WHITE,
                    (int) (width), (int) (y + length), Color.GRAY));
            graphics2D.fillRect((int) (x), (int) (y),
                    (int) (width), (int) (length));
            
            
            //画中转平台上的集装箱;
            width = getWidthOfContainer();
            switch ((int) (2 * quayCrane.getNumOnExchangePlatform())) {
                case 0://无箱
                    break;
                case 1://1*20ft
                    x = quayCrane.getExchangePlatformX()-getWidthOfContainer()-0.5*getDistanceOfContainers();
                    y = quayCrane.getLocation().getY()-getLengthofContainer(20);
                    length = getLengthofContainer(20);
                    graphics2D.setPaint(new GradientPaint((int) (x), (int) (y), Color.BLACK,
                            (int) (width), (int) (y+length), Color.RED));
                    graphics2D.fillRect((int) (x), (int) (y),
                            (int) (width), (int) (length));
                    break;
                case 2://1*40ft or 2*20ft
                    x = quayCrane.getExchangePlatformX()-getWidthOfContainer()-0.5*getDistanceOfContainers();
                    y = quayCrane.getLocation().getY()-getLengthofContainer(40)/2;
                    length = getLengthofContainer(40);
                    graphics2D.setPaint(new GradientPaint((int) (x), (int) (y), Color.BLACK,
                            (int) (width), (int) (y+length), Color.RED));
                    graphics2D.fillRect((int) (x), (int) (y),
                            (int) (width), (int) (length));
                    break;
                case 3://1*40ft + 1*20ft
                    //1*40
                    x = quayCrane.getExchangePlatformX()-getWidthOfContainer()-0.5*getDistanceOfContainers();
                    y = quayCrane.getLocation().getY()-getLengthofContainer(40)/2;
                    length = getLengthofContainer(40);
                    graphics2D.setPaint(new GradientPaint((int) (x), (int) (y), Color.BLACK,
                            (int) (width), (int) (y+length), Color.RED));
                    graphics2D.fillRect((int) (x), (int) (y),
                            (int) (width), (int) (length));
                    //1*20
                    x = quayCrane.getExchangePlatformX()+0.5*getDistanceOfContainers();
                    y = quayCrane.getLocation().getY()-getLengthofContainer(20);
                    width = getWidthOfContainer();
                    length = getLengthofContainer(20);
                    graphics2D.setPaint(new GradientPaint((int) (x), (int) (y), Color.BLACK,
                            (int) (width), (int) (y+length), Color.RED));
                    graphics2D.fillRect((int) (x), (int) (y),
                            (int) (width), (int) (length));
                    break;
                case 4://2*40ft
                    //1*40
                    x = quayCrane.getExchangePlatformX()-getWidthOfContainer()-0.5*getDistanceOfContainers();
                    y = quayCrane.getLocation().getY()-getLengthofContainer(40)/2;
                    width = getWidthOfContainer();
                    length = getLengthofContainer(40);
                    graphics2D.setPaint(new GradientPaint((int) (x), (int) (y), Color.BLACK,
                            (int) (width), (int) (y+length), Color.RED));
                    graphics2D.fillRect((int) (x), (int) (y),
                            (int) (width), (int) (length));
                    //1*40
                    x = quayCrane.getExchangePlatformX()+0.5*getDistanceOfContainers();
                    y = quayCrane.getLocation().getY()-getLengthofContainer(40)/2;
                    width = getWidthOfContainer();
                    length = getLengthofContainer(40);
                    graphics2D.setPaint(new GradientPaint((int) (x), (int) (y), Color.BLACK,
                            (int) (width), (int) (y+length), Color.RED));
                    graphics2D.fillRect((int) (x), (int) (y),
                            (int) (width), (int) (length));
                    break;
                default:
            }
            //画主小车;
            double mainX = quayCrane.getMainTrolleyX();
            double mainY = quayCrane.getLocation().getY();
            double mainlengthx = InputParameters.getWidthOfContainer()+2;
            double mainlengthy = lengthy;
            stroke = new BasicStroke((float)1.5);
            graphics2D.setStroke(stroke);
            graphics2D.setPaint(new GradientPaint((int) (mainX - 0.5 * mainlengthx), (int) (mainY - 0.5 * mainlengthy), Color.BLACK,
                    (int) (mainX + 0.5 * mainlengthx), (int) (mainY + 0.5 * mainlengthy), Color.GRAY));
            graphics2D.drawRoundRect((int) (mainX - 0.5 * mainlengthx), (int) (mainY - 0.5 * mainlengthy), 
                    (int) (mainlengthx), (int) (mainlengthy),2,2);
            //画车上的集装箱;
            if (quayCrane.isMainTrolleyHaveContainer()) {
                graphics2D.setPaint(new GradientPaint((int) (mainX - 0.5 * mainlengthx), (int) (mainY - 0.5 * mainlengthy), Color.BLACK,
                        (int) (mainX + 0.5 * mainlengthx), (int) (mainY + 0.5 * mainlengthy), Color.RED));
                graphics2D.fillRect((int) (mainX - 0.5 * getWidthOfContainer()), (int) (mainY - 0.5 * getLengthOf40FTContainer()),
                        (int) (getWidthOfContainer()), (int) (getLengthOf40FTContainer()));
            }
            //画门架小车;
            double gantryX = quayCrane.getGantryTrolleyX();
            double gantryY = quayCrane.getLocation().getY();
            double gantrylengthx = InputParameters.getWidthOfContainer()+2;
            double gantrylengthy = lengthy;
            stroke = new BasicStroke((float)1.5);
            graphics2D.setStroke(stroke);
            graphics2D.setPaint(new GradientPaint((int) (gantryX - 0.5 * gantrylengthx), (int) (gantryY - 0.5 * gantrylengthy), Color.BLACK,
                    (int) (gantryX + 0.5 * gantrylengthx), (int) (gantryY + 0.5 * gantrylengthy), Color.GRAY));
            graphics2D.drawRoundRect((int) (gantryX - 0.5 * gantrylengthx), (int) (gantryY - 0.5 * gantrylengthy), 
                    (int) (gantrylengthx), (int) (gantrylengthy),2,2);
            //画车上的集装箱;
            if (quayCrane.isGantryTrolleyHaveContainer()) {
                graphics2D.setPaint(new GradientPaint((int) (gantryX - 0.5 * gantrylengthx), (int) (gantryY - 0.5 * gantrylengthy), Color.BLACK,
                        (int) (gantryX + 0.5 * gantrylengthx), (int) (gantryY + 0.5 * gantrylengthy), Color.RED));
                graphics2D.fillRect((int) (gantryX - 0.5 * getWidthOfContainer()), (int) (gantryY - 0.5 * getLengthOf40FTContainer()),
                        (int) (getWidthOfContainer()), (int) (getLengthOf40FTContainer()));
            }
            
            graphics2D.setColor(Color.BLUE);
            graphics2D.drawString(Integer.toString((int)quayCrane.getNumber()),
                    (int) (quayCrane.getLocation().getX()+quayCrane.getGauge()),
                    (int) (quayCrane.getLocation().getY() - 0.5 * (quayCrane.getGauge() / 5)));
        }
        BasicStroke stroke = new BasicStroke((float) 1);
        graphics2D.setStroke(stroke);
    }

    private void DrawAGVs(Graphics2D graphics2D) {
        BasicStroke stroke = new BasicStroke((float) 1);
        graphics2D.setStroke(stroke);
        for (AGV agv : port.getAGVs()) {
            if (agv.getAGVstate().equals(StaticName.FREE)) {
                graphics2D.setColor(Color.BLACK);
            } else {
                graphics2D.setColor(Color.RED);
            }
            Point2D nowPoint = new Point2D.Double(agv.getPresentPosition().getX(),agv.getPresentPosition().getY());
            if ((agv.isOnBufferArea() || port.getAGVBufferArea().isInThisRectangle(nowPoint)) ||
                    port.getRoadNetWork().findRoadSectionName(nowPoint, 1) != null &&
                    port.getRoadNetWork().findRoadSection((String)port.getRoadNetWork().
                            findRoadSectionName(nowPoint, 1)).getroadType() == 1){
                //在AGVBufferArea 或 竖直路段;
                graphics2D.setPaint(new GradientPaint((int)(nowPoint.getX() - 0.5 * agv.getLength()),
                        (int)(nowPoint.getY() - 0.5 * agv.getWidth()),Color.ORANGE,
                        (int)(nowPoint.getX() + 0.5 * agv.getLength()), 
                        (int)(nowPoint.getY() + 0.5 * agv.getWidth()),Color.WHITE));
                graphics2D.fillRoundRect((int)(nowPoint.getX() - 0.5 * agv.getLength()),
                        (int) (nowPoint.getY() - 0.5 * agv.getWidth()),
                        (int) agv.getLength(), (int) agv.getWidth(),2,2); 
                graphics2D.setColor(Color.DARK_GRAY);
                graphics2D.drawRoundRect((int) (nowPoint.getX() - 0.5 * agv.getLength()),
                        (int) (nowPoint.getY() - 0.5 * agv.getWidth()),
                        (int) agv.getLength(), (int) agv.getWidth(), 2, 2);     
                if (agv.getContainerNum() != 0) {
                    //在AGVBufferArea 或 竖直路段;
                    graphics2D.setPaint(new GradientPaint((int) (nowPoint.getX() - 0.5 * agv.getLength()),
                            (int) (nowPoint.getY() - 0.5 * agv.getWidth()), Color.RED,
                            (int) (nowPoint.getX() + 0.5 * agv.getLength()),
                            (int) (nowPoint.getY() + 0.5 * agv.getWidth()), Color.PINK));
                    graphics2D.fillRoundRect((int) (nowPoint.getX() - 0.5 * agv.getLength()),
                            (int) (nowPoint.getY() - 0.5 * agv.getWidth()),
                            (int) agv.getLength(), (int) agv.getWidth(), 2, 2);
                    graphics2D.setColor(Color.DARK_GRAY);
                    graphics2D.drawRoundRect((int) (nowPoint.getX() - 0.5 * agv.getLength()),
                            (int) (nowPoint.getY() - 0.5 * agv.getWidth()),
                            (int) agv.getLength(), (int) agv.getWidth(), 2, 2);
                }
            } else {
                graphics2D.setPaint(new GradientPaint((int)(nowPoint.getX() - 0.5 * agv.getWidth()),
                        (int)(nowPoint.getY() - 0.5 * agv.getLength()),Color.ORANGE,
                        (int)(nowPoint.getX() + 0.5 * agv.getWidth()), 
                        (int)(nowPoint.getY() + 0.5 * agv.getLength()),Color.WHITE));
                graphics2D.fillRoundRect((int)(nowPoint.getX() - 0.5 * agv.getWidth()),
                        (int) (nowPoint.getY() - 0.5 * agv.getLength()),
                        (int) agv.getWidth(), (int) agv.getLength(),2,2); 
                graphics2D.setColor(Color.DARK_GRAY);
                graphics2D.drawRoundRect((int) (nowPoint.getX() - 0.5 * agv.getWidth()),
                        (int) (nowPoint.getY() - 0.5 * agv.getLength()),
                        (int) agv.getWidth(), (int) agv.getLength(), 2, 2);
                if(agv.getContainerNum() != 0){
                    graphics2D.setPaint(new GradientPaint((int)(nowPoint.getX() - 0.5 * agv.getWidth()),
                        (int)(nowPoint.getY() - 0.5 * agv.getLength()),Color.RED,
                        (int)(nowPoint.getX() + 0.5 * agv.getWidth()), 
                        (int)(nowPoint.getY() + 0.5 * agv.getLength()),Color.PINK));
                graphics2D.fillRoundRect((int)(nowPoint.getX() - 0.5 * agv.getWidth()),
                        (int) (nowPoint.getY() - 0.5 * agv.getLength()),
                        (int) agv.getWidth(), (int) agv.getLength(),2,2); 
                graphics2D.setColor(Color.DARK_GRAY);
                graphics2D.drawRoundRect((int) (nowPoint.getX() - 0.5 * agv.getWidth()),
                        (int) (nowPoint.getY() - 0.5 * agv.getLength()),
                        (int) agv.getWidth(), (int) agv.getLength(), 2, 2);   
                }
            }
        }
    }

    private void DrawShips(Graphics2D graphics2D) {
        float[] hsb = new float[3];
        hsb = InputParameters.getColor_Brown();

        if (port.getCurrentShips() == null) {
            return;
        }
        int num = 0;
        for (Berth berth : port.getBerths()) {
            if(berth.getShip() != null) {
                Ship currentShip = berth.getShip();
                if (currentShip.getBerthingLocation() != null) {
                    //画已经靠泊的船只;
                    int shipx = -(int) currentShip.getShipWidth() - 10;
                    int shipy = (int) (currentShip.getBerthingLocation().getY() - 0.5 * currentShip.getShipLength());
                    int width = (int) currentShip.getShipWidth();
                    int length = (int) currentShip.getShipLength();
                    graphics2D.setPaint(new GradientPaint(shipx, (int) (shipy + 1.5 * width), Color.getHSBColor(hsb[0], hsb[1], hsb[2]),
                            shipx + width, shipy + length, Color.DARK_GRAY));
                    graphics2D.fillRoundRect(shipx, (int) (shipy + 1.5 * width), width, (int) (length - 1.5 * width),
                            (int) (0.2 * width), (int) (0.2 * (length - 1.5 * width)));
                    graphics2D.fillOval(shipx, shipy, width, 3 * width);
                    //
                    width -= 2;
                    length -= 2;
                    shipx += 1;
                    shipy += 1;
                    graphics2D.setPaint(new GradientPaint(shipx, (int) (shipy + 1.5 * width), Color.PINK,
                            shipx + width, shipy + length, Color.WHITE));
                    graphics2D.fillRoundRect(shipx, (int) (shipy + 1.5 * width), width, (int) (length - 1.5 * width),
                            (int) (0.2 * width), (int) (0.2 * (length - 1.5 * width)));
                    graphics2D.fillOval(shipx, shipy, width, 3 * width);
                    graphics2D.setTransform(stringTransform);
                    //graphics2D.setFont(new Font("TimesRoman",Font.ITALIC,Math.max(12,(int)(0.5*width))));
                    graphics2D.setFont(new Font("TimesRoman", Font.ITALIC, 12));
                    String shipInfo = currentShip.toString() + currentShip.getDWTLevel() + "DWT";
                    graphics2D.setColor(Color.BLACK);
                    graphics2D.drawString(shipInfo, (int) (shipy + 0.1 * length), -shipx - 1 - (int) (0.5 * width));
                    graphics2D.setFont(new Font("楷体", Font.PLAIN, (int)(12*AnimationGUI.scale)));
                    graphics2D.setColor(Color.RED);
                    if (currentShip.shipState.equals(StaticName.UNLOADING)) {
                        graphics2D.drawString("卸船中:" + (currentShip.getShipContainers().getPresent_needunload_20ft()
                                + currentShip.getShipContainers().getPresent_needunload_40ft()) + "TEU",
                                (int) (shipy + 0.1 * length), -shipx - 1 - (int) (0.5 * width) + (int)(12*AnimationGUI.scale));
                    } else if (currentShip.shipState.equals(StaticName.LOADING)) {
                        graphics2D.drawString("装船中:" + (currentShip.getShipContainers().getPresent_needload_20ft()
                                + currentShip.getShipContainers().getPresent_needload_40ft()) + "TEU",
                                (int) (shipy + 0.1 * length), -shipx - 1 - (int) (0.5 * width) + (int)(12*AnimationGUI.scale));
                    } else if (currentShip.shipState.equals(StaticName.FREE)) {
                        graphics2D.drawString("Free", (int) (shipy + 0.1 * length), -shipx - 1 + (int)(12*AnimationGUI.scale));
                    } else {
                        graphics2D.drawString("准备驶离",
                                (int) (shipy + 0.1 * length), -shipx - 1 - (int) (0.5 * width) + (int)(12*AnimationGUI.scale));
                    }
                    graphics2D.setTransform(imageTransform);
                }
            }
        }
    }

    private void DrawContainersOnYard(Graphics2D graphics2D) {
        graphics2D.setColor(Color.DARK_GRAY);
        for (Yard yard : port.getYards()) {
            for (Block block : yard.getBlock()) {
                if (block.getCurrentContainers() == null) {
                    break;
                }
                for (Container container : block.getCurrentContainers()) {
                    if (container == null || container.getPresentSlot() == null
                            || container.getPresentSlot().getCentralLocation() == null) {
                        break;
                    }
                    double x = container.getPresentSlot().getCentralLocation().getX();
                    double y = container.getPresentSlot().getCentralLocation().getY();
                    double width = getWidthOfContainer();
                    double length = getLengthofContainer((int)container.getFTsize());
                    graphics2D.setPaint(new GradientPaint((int)(x-0.5*width),(int) (y-0.5*length),Color.DARK_GRAY,
                            (int)(x+0.5*width),(int)(y+0.5*length),Color.RED));
                    graphics2D.fillRect((int)(x-0.5*width), (int)(y-0.5*length), (int)width, (int)length);
                }

            }
        }
    }

    private void DrawContainersOnAGV(Graphics2D graphics2D) {
        graphics2D.setColor(Color.GREEN);
        for (AGV agv : port.getAGVs()) {
            switch((int)agv.getContainerNum()){
                case 1:
                    double x = agv.getPresentPosition().getX();
                    double y = agv.getPresentPosition().getY();
                    double width = getWidthOfContainer();
                    double size = 40;
                    double length = getLengthofContainer((int)size);
                    graphics2D.drawRect((int)(x-0.5*width), (int)(y+0.5*agv.getLength()-length), (int)width, (int)length);                    
                    break;
                case 2:
                    x = agv.getPresentPosition().getX();
                    y = agv.getPresentPosition().getY();
                    width = getWidthOfContainer();
                    size = 20;
                    length = getLengthofContainer((int)size);
                    graphics2D.drawRect((int)(x-0.5*width), (int)(y+0.5*agv.getLength()-length), (int)width, (int)length);
                    graphics2D.drawRect((int)(x-0.5*width), (int)(y+0.5*agv.getLength()-2*length-getDistanceOfContainers()),
                            (int)width,(int)length);
                    break;
                case 0: 
                    break;
                default:
                    System.out.println("!!!Error:DrawContainersOnAGV()!!!");
                    throw new UnsupportedOperationException("Error:DrawContainersOnAGV()");              
            }
        }
    }

    private void DrawBerths(Graphics2D graphics2D) {
        double length = InputParameters.getBerthLength(InputParameters.getBigBerthDWT());
        double x = InputParameters.getpBerth1_0().getX();
        double y = InputParameters.getpBerth1_0().getY() - 0.5 * InputParameters.getBerthLength(InputParameters.getBigBerthDWT());
        graphics2D.setStroke(new BasicStroke(1, CAP_ROUND, JOIN_ROUND));
        graphics2D.setColor(Color.DARK_GRAY);
        graphics2D.drawLine((int) x, (int) y, (int) x, (int) (y + 2 * length));
        for (Berth berth : port.getBerths()) {
            if (berth.getShip() != null) {
                length = berth.getLength();
                x = berth.getLocation().getX();
                y = berth.getLocation().getY();
                graphics2D.setStroke(new BasicStroke(2, CAP_ROUND, JOIN_ROUND));
                graphics2D.setColor(Color.GRAY);
                graphics2D.drawLine((int) x, (int) (y - 0.5 * length), (int) x, (int) (y + 0.5 * length));
            }

        }
    }

    private void DrawContainersOnYC(Graphics2D graphics2D) {
        
        float[] hsb = new float[3];
        hsb = InputParameters.getColor_DeepGreen();////深一点的哑光绿
        
        graphics2D.setColor(Color.PINK);
        for (YardCrane YC : port.getYardCranes()) {
            if(YC.isFree() == false && YC.getCurrentServiceContainer() != null){
                if(YC.getFirstYCTask().getContainer() != null &&
                        YC.getFirstYCTask().getContainer()[0].getState().equals(StaticName.ONYC)){
                    double num = YC.getFirstYCTask().getContainer().length;
                    double size = YC.getFirstYCTask().getContainer()[0].getFTsize();
                    double x = YC.calculateRowPosition(YC.getCurrentRowPosition());
                    double y = YC.currentPosition.getY();
                    double width = getWidthOfContainer();
                    double length = getLengthofContainer((int)size);
                    if(num == 2){
                        graphics2D.setPaint(new GradientPaint((int)(x-0.5*width),(int) (y-0.5*length),Color.getHSBColor(hsb[0],hsb[1],hsb[2]),
                            (int)(x+0.5*width),(int)(y+1.5*length),Color.getHSBColor(hsb[0],hsb[1],hsb[2])));
                        graphics2D.fillRect((int)(x-0.5*width), (int)(y-0.5*length), (int)width, (int)length);
                        graphics2D.fillRect((int)(x-0.5*width), (int)(y), (int)width, (int)length);
                    }else if(num == 1){
                        graphics2D.setPaint(new GradientPaint((int)(x-0.5*width),(int) (y-0.5*length),Color.getHSBColor(hsb[0],hsb[1],hsb[2]),
                            (int)(x+0.5*width),(int)(y+0.5*length),Color.getHSBColor(hsb[0],hsb[1],hsb[2])));
                        graphics2D.fillRect((int)(x-0.5*width), (int)(y-0.5*length), (int)width, (int)length);
                    }
                }
            }
        }
    }

    private void DrawRoadNet(Graphics2D graphics2D) {
        //画岸桥下的装卸区车道;
        RoadSection roadUnderQC = port.getRoadNetWork().getagvUnderQCRoad();
        int laneNum = roadUnderQC.getlaneNum();
        double singleWidth = InputParameters.getSingleRoadWidth();
        double x = Math.min(roadUnderQC.getedge()[0].getX(), roadUnderQC.getedge()[1].getX());
        for(int i = 1;i<laneNum;i++){
            graphics2D.setColor(Color.white);
            graphics2D.drawLine((int) (x+i*singleWidth), (int) roadUnderQC.getedge()[0].getY(),
                (int) (x+i*singleWidth), (int) roadUnderQC.getedge()[2].getY());
        }
        //画路段;
        graphics2D.setColor(Color.DARK_GRAY);
        for (RoadSection road : port.getRoadNetWork().roadSections) {
            switch(road.getroadType()){
            //行驶方向：垂直于前沿--1，平行于前沿--2，转弯圆弧--3，丁字路口--4，十字路口--5;
                case 1:
                    //垂直于前沿;
                    graphics2D.drawLine((int)road.getedge()[0].getX(), (int)road.getedge()[0].getY(), 
                            (int)road.getedge()[1].getX(), (int)road.getedge()[1].getY());
                    graphics2D.drawLine((int)road.getedge()[0].getX(), (int)road.getedge()[0].getY(), 
                            (int)road.getedge()[2].getX(), (int)road.getedge()[2].getY());
                    graphics2D.drawLine((int)road.getedge()[2].getX(), (int)road.getedge()[2].getY(), 
                            (int)road.getedge()[3].getX(), (int)road.getedge()[3].getY());
                    graphics2D.drawLine((int)road.getedge()[1].getX(), (int)road.getedge()[1].getY(), 
                            (int)road.getedge()[3].getX(), (int)road.getedge()[3].getY());                  
                    break;
                case 2:
                    //平行于前沿;
                    graphics2D.drawLine((int)road.getedge()[0].getX(), (int)road.getedge()[0].getY(), 
                            (int)road.getedge()[1].getX(), (int)road.getedge()[1].getY());
                    graphics2D.drawLine((int)road.getedge()[0].getX(), (int)road.getedge()[0].getY(), 
                            (int)road.getedge()[2].getX(), (int)road.getedge()[2].getY());
                    graphics2D.drawLine((int)road.getedge()[2].getX(), (int)road.getedge()[2].getY(), 
                            (int)road.getedge()[3].getX(), (int)road.getedge()[3].getY());
                    graphics2D.drawLine((int)road.getedge()[1].getX(), (int)road.getedge()[1].getY(), 
                            (int)road.getedge()[3].getX(), (int)road.getedge()[3].getY());
                    break;
                case 3:
                    //圆弧段;
                    if(road.getroadNum().equals("Road4")){
                        graphics2D.drawArc(Math.min((int)(road.getedge()[1].getX()), (int)(road.getedge()[3].getX())), 
                                Math.min((int)(road.getedge()[1].getY()), (int)(road.getedge()[3].getY())), 
                                2*Math.abs((int)(road.getedge()[1].getX()-road.getedge()[3].getX())), 
                                2*Math.abs((int)(road.getedge()[1].getX()-road.getedge()[3].getX())), 
                                90, 90);
                        graphics2D.drawArc(Math.min((int)(road.getedge()[0].getX()), (int)(road.getedge()[2].getX())), 
                                Math.min((int)(road.getedge()[0].getY()), (int)(road.getedge()[2].getY())), 
                                2*Math.abs((int)(road.getedge()[0].getX()-road.getedge()[2].getX())), 
                                2*Math.abs((int)(road.getedge()[0].getX()-road.getedge()[2].getX())), 
                                90, 90);
                    }else{
                        graphics2D.drawArc(Math.min((int)(road.getedge()[1].getX()), (int)(road.getedge()[3].getX())), 
                                (int)(Math.min(road.getedge()[1].getY(),road.getedge()[3].getY())-Math.abs(road.getedge()[1].getX()-road.getedge()[3].getX())), 
                                2*Math.abs((int)(road.getedge()[1].getX()-road.getedge()[3].getX()))-2, 
                                2*Math.abs((int)(road.getedge()[1].getX()-road.getedge()[3].getX()))-2, 
                                -90, -90);
                        graphics2D.drawArc(Math.min((int)(road.getedge()[0].getX()), (int)(road.getedge()[2].getX())), 
                                (int)(Math.min(road.getedge()[0].getY(),road.getedge()[2].getY())-Math.abs(road.getedge()[0].getX()-road.getedge()[2].getX())), 
                                2*Math.abs((int)(road.getedge()[0].getX()-road.getedge()[2].getX()))-2, 
                                2*Math.abs((int)(road.getedge()[0].getX()-road.getedge()[2].getX()))-2, 
                                -90, -90);
                    }
                    break;
                default:
                    System.out.println("!!!Error:DrawRoadNet!!!");
                    throw new UnsupportedOperationException("Error:DrawRoadNet");              
            }
        }
        //画功能区：
        //画AGV缓冲区;
        graphics2D.drawLine((int)port.getAGVBufferArea().P1.getX(), (int)port.getAGVBufferArea().P1.getY(), 
                            (int)port.getAGVBufferArea().P1.getX(), (int)port.getAGVBufferArea().P2.getY());
        graphics2D.drawLine((int)port.getAGVBufferArea().P1.getX(), (int)port.getAGVBufferArea().P1.getY(), 
                            (int)port.getAGVBufferArea().P2.getX(), (int)port.getAGVBufferArea().P1.getY());
        graphics2D.drawLine((int)port.getAGVBufferArea().P2.getX(), (int)port.getAGVBufferArea().P2.getY(), 
                            (int)port.getAGVBufferArea().P2.getX(), (int)port.getAGVBufferArea().P1.getY());
        graphics2D.drawLine((int)port.getAGVBufferArea().P2.getX(), (int)port.getAGVBufferArea().P2.getY(), 
                            (int)port.getAGVBufferArea().P1.getX(), (int)port.getAGVBufferArea().P2.getY());
        DrawAGVBuffer(graphics2D);
    }

    private void DrawAGVBuffer(Graphics2D graphics2D) {
        for(int i = 0;i<port.getAGVBufferArea().getParkingAreas().length;i++){
            if (port.getAGVBufferArea().getIsBooked()[i] == null
                    && port.getAGVBufferArea().getHaveCar()[i] == null) {
                graphics2D.setColor(Color.DARK_GRAY);
                graphics2D.drawRect((int) (port.getAGVBufferArea().getParkingAreas()[i].getX()
                    - 0.5 * port.getAGVBufferArea().getLength()),
                    (int) (port.getAGVBufferArea().getParkingAreas()[i].getY()
                    - 0.5 * port.getAGVBufferArea().getWidthOfOneParking()),
                    (int) (port.getAGVBufferArea().getLength()),
                    (int) (port.getAGVBufferArea().getWidthOfOneParking()));      
            }
            if(port.getAGVBufferArea().getIsBooked()[i] != null){
                graphics2D.setColor(Color.DARK_GRAY);
                graphics2D.drawRoundRect((int) (port.getAGVBufferArea().getParkingAreas()[i].getX()
                    - 0.5 * port.getAGVs()[0].getLength()),
                    (int) (port.getAGVBufferArea().getParkingAreas()[i].getY()
                    - 0.5 * port.getAGVs()[0].getWidth()),
                    (int) (port.getAGVs()[0].getLength()),
                    (int) (port.getAGVs()[0].getWidth()),2,2);
            }else if(port.getAGVBufferArea().getHaveCar()[i] != null){
                graphics2D.setPaint(new GradientPaint((int)(port.getAGVBufferArea().getParkingAreas()[i].getX() - 
                        0.5 * port.getAGVBufferArea().getLength()),
                        (int) (port.getAGVBufferArea().getParkingAreas()[i].getY() - 
                                0.5 * port.getAGVBufferArea().getWidthOfOneParking()+1),Color.LIGHT_GRAY,
                        (int) (port.getAGVBufferArea().getParkingAreas()[i].getX() - 
                                0.5 * port.getAGVBufferArea().getLength() + port.getAGVBufferArea().getLength()-1),
                        (int) (port.getAGVBufferArea().getParkingAreas()[i].getY() - 
                                0.5 * port.getAGVBufferArea().getWidthOfOneParking() + 
                                port.getAGVBufferArea().getWidthOfOneParking()-1),Color.WHITE));
                graphics2D.fillRect((int) (port.getAGVBufferArea().getParkingAreas()[i].getX()
                    - 0.5 * port.getAGVBufferArea().getLength()),
                    (int) (port.getAGVBufferArea().getParkingAreas()[i].getY()
                    - 0.5 * port.getAGVBufferArea().getWidthOfOneParking()+1),
                    (int) (port.getAGVBufferArea().getLength()-1),
                    (int) (port.getAGVBufferArea().getWidthOfOneParking())-2);      
            }
               
        }
    }
    

    //draw AGVCouple and Containers on that;
    private void DrawAGVCouple(Graphics2D graphics2D) {
        for (Yard yard : port.getYards()) {
            for (Block block : yard.getBlock()) {
                for (AGVCouple agvCouple : block.getAGVCouple()) {
                    double x = agvCouple.centralPosition.getX();
                    double y = agvCouple.centralPosition.getY();
                    double length = agvCouple.length;
                    double width = agvCouple.width;
                    graphics2D.setPaint(new GradientPaint((int) (x - 0.5 * width),
                            (int) (y - 0.5 * length), Color.RED,
                            (int) (x + 0.5 * width),
                            (int) (y + 0.5 * length), Color.DARK_GRAY));
                    graphics2D.fillRoundRect((int) (x - 0.5 * width),
                            (int) (y - 0.5 * length),
                            1, (int) length, 0, 2);
                    graphics2D.fillRoundRect((int) (x + 0.5 * width - 1),
                            (int) (y - 0.5 * length),
                            1, (int) length, 0, 2);
                    //DrawContainers;
                    if (agvCouple.haveContainers() == true) {
                        int size = (int) agvCouple.getCurrentContainers()[0].getFTsize();
                        int num = agvCouple.getCurrentContainers().length;
                        graphics2D.setColor(Color.GREEN);
                        switch (size) {
                            case 20:
                                switch (num) {
                                    case 1:
                                        y = y - 0.5 * getLengthofContainer(size);
                                        width = getWidthOfContainer();
                                        length = getLengthofContainer(size);
                                        graphics2D.setPaint(new GradientPaint((int) (x - 0.5 * width), (int) (y - 0.5 * length), Color.BLACK,
                                                (int) (x + 0.5 * width), (int) (y + 0.5 * length), Color.RED));
                                        graphics2D.fillRect((int) (x - 0.5 * width), (int) (y - 0.5 * length), (int) width, (int) length);
                                        break;
                                    case 2:
                                        y = y - 0.5 * getLengthofContainer(size);
                                        width = getWidthOfContainer();
                                        length = getLengthofContainer(size);
                                        graphics2D.setPaint(new GradientPaint((int) (x - 0.5 * width), (int) (y - 0.5 * length), Color.BLACK,
                                                (int) (x + 0.5 * width), (int) (y + 0.5 * length), Color.RED));
                                        graphics2D.fillRect((int) (x - 0.5 * width), (int) (y - 0.5 * length), (int) width, (int) length);
                                        y = y + 0.5 * getLengthofContainer(size);
                                        width = getWidthOfContainer();
                                        length = getLengthofContainer(size);
                                        graphics2D.setPaint(new GradientPaint((int) (x - 0.5 * width), (int) (y - 0.5 * length), Color.BLACK,
                                                (int) (x + 0.5 * width), (int) (y + 0.5 * length), Color.RED));
                                        graphics2D.fillRect((int) (x - 0.5 * width), (int) (y - 0.5 * length), (int) width, (int) length);
                                        break;
                                }
                                break;
                            case 40:
                                width = getWidthOfContainer();
                                length = getLengthofContainer(size);
                                graphics2D.setPaint(new GradientPaint((int) (x - 0.5 * width), (int) (y - 0.5 * length), Color.BLACK,
                                        (int) (x + 0.5 * width), (int) (y + 0.5 * length), Color.RED));
                                graphics2D.fillRect((int) (x - 0.5 * width), (int) (y - 0.5 * length), (int) width, (int) length);
                                break;
                            default:
                        }
                    }
                }
            }
        }
    }

    private void DrawYardLayout(Graphics2D graphics2D) {
        
        float[] hsb = new float[3];
        hsb = InputParameters.getColor_LightPure();////浅素色
        //包括RMG轨道,包括堆场轮廓，包括交换区的各个停车位;
        for (Yard yard : port.getYards()) {
            for (Block block : yard.getBlock()) {
                double centralX = 0.5*(block.getP1().getX()+block.getP2().getX());
                double y = block.getP1().getY();
                double length = block.getLengthOfBlock();
                double containerWidth = block.getAllContainerWidthOfBlock();
                double guageWidth = block.getWidthOfBlock();
                //画RMG轨道;
                graphics2D.setPaint(new GradientPaint((int)(centralX-0.5*guageWidth),(int)y, Color.BLACK,
                        (int) (centralX + 0.5*containerWidth),(int) (y + length), Color.BLACK));
                graphics2D.fillRect((int)(centralX-0.5*guageWidth),(int)y, 
                        (int) guageWidth, (int) length);
                //画堆场轮廓;
                graphics2D.setPaint(new GradientPaint((int)(centralX-0.5*containerWidth),
                        (int)y, (Color.getHSBColor(hsb[0],hsb[1],hsb[2])),
                        (int) (centralX + 0.5*containerWidth),
                        (int) (y + length), Color.WHITE));
                graphics2D.fillRoundRect((int)(centralX-0.5*containerWidth),
                        (int)y, (int) containerWidth, (int) length, 0, 0);
                //画交换区;
                //左侧;
                double x = block.getP1().getX();
                y = block.getP1().getY()-InputParameters.getLengthOfTransferPoint();
                length = InputParameters.getLengthOfTransferPoint();
                double totalWidth = block.getWidthOfBlock();
                graphics2D.setPaint(new GradientPaint((int)(x),
                        (int)y, (Color.getHSBColor(hsb[0],hsb[1],hsb[2])),
                        (int) (x + totalWidth),
                        (int) (y + length), Color.WHITE));
                graphics2D.fillRoundRect((int)(x),
                        (int)y, (int) totalWidth, (int) length, 0, 2);
                //画停车位;
                double width = totalWidth/3;
                graphics2D.setColor(Color.WHITE);
                graphics2D.drawLine((int)(x), (int)(y), (int)(x), (int)(y+length));
                graphics2D.drawLine((int)(x+width), (int)(y), (int)(x+width), (int)(y+length));
                graphics2D.drawLine((int)(x+2*width), (int)(y), (int)(x+2*width), (int)(y+length));
                graphics2D.drawLine((int)(x+3*width), (int)(y), (int)(x+3*width), (int)(y+length));
                //右侧;
                x = block.getP1().getX();
                y = block.getP2().getY();
                graphics2D.setPaint(new GradientPaint((int)(x),
                        (int)y, (Color.getHSBColor(hsb[0],hsb[1],hsb[2])),
                        (int) (x + totalWidth),
                        (int) (y + length), Color.WHITE));
                graphics2D.fillRoundRect((int)(x),
                        (int)y, (int) totalWidth, (int) length, 0, 2);
                //画停车位;
                graphics2D.setColor(Color.WHITE);
                graphics2D.drawLine((int)(x), (int)(y), (int)(x), (int)(y+length));
                graphics2D.drawLine((int)(x+width), (int)(y), (int)(x+width), (int)(y+length));
                graphics2D.drawLine((int)(x+2*width), (int)(y), (int)(x+2*width), (int)(y+length));
                graphics2D.drawLine((int)(x+3*width), (int)(y), (int)(x+3*width), (int)(y+length)); 
            }
        }
    }

}
