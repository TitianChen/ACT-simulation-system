/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package algorithm;

import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 *
 * @author Administrator
 */
public final class formater {
    private static final DecimalFormat formater = new DecimalFormat("#.0000"); 
    public static final double format(double number){
        //formater.setRoundingMode(RoundingMode.CEILING); //模式  向上最近的整数   
        //return java.lang.Double.parseDouble(formater.format(number)); 
        return number;
    }
}
