/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package parameter;
import CYT_model.Port;
import CYT_model.PortSimulationModel;
import Vehicle.AGV;
import apron.QuayCrane;
import apron.QuayCraneWork;
import gui.CYTMainGUI;
import java.rmi.RemoteException;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.ResourceRequestorInterface;
import nl.tudelft.simulation.dsol.formalisms.process.Process;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import static parameter.InputParameters.getSimulationSpeed;
import static parameter.OutputParameters.outputRoadNet;
import static parameter.OutputParameters.simulationRealTime;

/**
 *
 * @author Administrator
 */
public class OutputProcess extends Process implements ResourceRequestorInterface{

    private static final long serialVersionUID = 1L;
    private static Port port;
    public OutputProcess(DEVSSimulatorInterface simulator,Port port) {
        super(simulator);
        OutputProcess.port = port;
        CYTMainGUI.setPort(port);
    }

    @Override
    public void process() throws RemoteException, SimRuntimeException {
        System.out.println("开始输出进程  一个单位时间输出一次");
        outputRoadNet(port);
        while (true) {
            //OutputParameters.outputAllNeeded(getPort());
            CYTMainGUI.updateGUI(getPort());
            this.hold(1.0/getSimulationSpeed());
            OutputParameters.simulationRealTime ++;
            PortSimulationModel.updateShipArrival();
            if(simulationRealTime>5000 && port.getCNUM().equals("0TEU")){
                this.hold(20.0/getSimulationSpeed());
                break;
            }
        }
        CYTMainGUI.updateGUI(getPort());
        System.out.println("---------------本次仿真成功结束----------------");
    }

    /**
     * @return the port
     */
    public static Port getPort() {
        return port;
    }
    
}
