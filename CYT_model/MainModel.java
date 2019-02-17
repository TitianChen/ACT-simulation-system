/*
 * based on simulation framework：TuDelft DSOL Version:2.1.0
 */
package CYT_model;

import algorithm.FileMethod;
import gui.CYTMainGUI;
import java.io.File;
import parameter.InputParameters;
import parameter.OutputParameters;

/**
 * MainModel
 * @author YutingChen, yuting.chen17@imperial.ac.uk
 * Dalian University of Technology
 * Imperial College London
 */
public class MainModel{
    /**
     * commandline executes the model
     * 
     * @param args the arguments to the commandline
     */
    public static void main(final String[] args){ 
        InputParameters ss = new InputParameters();
        OutputParameters oo = new OutputParameters();
        new FileMethod().deleteFile(InputParameters.OUTPUTPATH);
        new File(InputParameters.OUTPUTPATH).mkdirs();
        //PortGUI.createInputGUI();
        CYTMainGUI.mainGUI();
    }
    public static void reset(){
        InputParameters ss = new InputParameters();
        OutputParameters oo = new OutputParameters(); 
        new FileMethod().deleteFile(InputParameters.OUTPUTPATH);
        new File(InputParameters.OUTPUTPATH).mkdirs();   
    }
    public static void startModel() {
        PortSimulationModel.start();
    }
}
