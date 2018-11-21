/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package roadnet;

import CYT_model.Point2D;


/**
 * @author YutingChen, 760698296@qq.com 
 * Dalian University of Technology
 */
public abstract class Rectangle{
    public final Point2D P1 = new Point2D.Double(0,0);
    public final Point2D P2 = new Point2D.Double(0,0);
    public Rectangle(Point2D p1,Point2D p2){
        this.P1.setLocation(p1);
        this.P2.setLocation(p2);
    }
    public boolean isInThisRectangle(Point2D p){
        if(getP1().getX()<=getP2().getX()){
            if(getP1().getY()<=getP2().getY()){
                if(p.getX()<=getP2().getX() && p.getX()>=getP1().getX()
                    && p.getY() <= getP2().getY() && p.getY() >= getP1().getY()){
                    return true;
                }
            }else{
                if(p.getX()<=getP2().getX() && p.getX()>=getP1().getX()
                    && p.getY() >= getP2().getY() && p.getY() <= getP1().getY()){
                    return true;
                }
            }
        }else{
            if(getP1().getY()<getP2().getY()){
                if(p.getX()>=getP2().getX() && p.getX()<=getP1().getX()
                    && p.getY() <= getP2().getY() && p.getY() >= getP1().getY()){
                    return true;
                }
            }else{
                if(p.getX()>=getP2().getX() && p.getX()<=getP1().getX()
                    && p.getY() >= getP2().getY() && p.getY() <= getP1().getY()){
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * @return the P1
     */
    public Point2D getP1() {
        return P1;
    }

    /**
     * @return the P2
     */
    public Point2D getP2() {
        return P2;
    }

    
    
    
    
    
}
