/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Vehicle;

import CYT_model.Point2D;


/**
 *
 * @author Administrator
 */
public interface VehicleInterface{
    
    public String getName();
    
    
    @Override
    public String toString();
    
    public Point2D getPresentPosition();
    
    
}
