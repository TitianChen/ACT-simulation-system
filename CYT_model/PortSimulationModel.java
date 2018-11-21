
package CYT_model;

import apron.QuayCraneWork;
import apron.QuayCrane;
import Vehicle.AGV;
import algorithm.FileMethod;
import gui.CYTMainGUI;
import java.io.File;
import storageblock.YardCrane;
import ship.Ship;
import java.rmi.RemoteException;
import java.util.logging.Level;

import nl.tudelft.simulation.dsol.ModelInterface;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.experiment.ExperimentalFrame;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.language.io.URLResource;
import nl.tudelft.simulation.logger.Logger;
import nl.tudelft.simulation.xml.dsol.ExperimentParser;
import parameter.InputParameters;
import parameter.OutputParameters;
import static parameter.OutputParameters.simulationRealTime;
import parameter.OutputProcess;
import parameter.StaticName;

/**
 * 船舶靠离泊子系统 void main() 基于DSOL
 *
 * @author YutingChen, yuting.chen17@imperial.ac.uk
 * Dalian University of Technology
 */
public class PortSimulationModel implements ModelInterface {

    private static final long serialVersionUID = 1L;
    public static SimulatorInterface simulator;
    private static int shipNum;
    private static double[] arrivalTime;
    private static String[] shipDWT;
    private static short priorityNum;

    /**
     * constructs a new BoatModel
     */
    public PortSimulationModel() {
        super();
    }
    
    
    public static void stop() {
        try {
            simulator.stop();
        } catch (RemoteException ex) {
            java.util.logging.Logger.getLogger(PortSimulationModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SimRuntimeException ex) {
            java.util.logging.Logger.getLogger(PortSimulationModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void resume() {
        try {
            simulator.start();
        } catch (RemoteException ex) {
            java.util.logging.Logger.getLogger(PortSimulationModel.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SimRuntimeException ex) {
            java.util.logging.Logger.getLogger(PortSimulationModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    /**
     * @throws java.rmi.RemoteException
     * @see nl.tudelft.simulation.dsol.ModelInterface
     * #constructModel(nl.tudelft.simulation.dsol.simulators.SimulatorInterface)
     * 参数输入;
     */
    @Override
    public void constructModel(final SimulatorInterface simulator)
            throws SimRuntimeException, RemoteException{
        OutputParameters.outputShipStart();
        PortSimulationModel.simulator = simulator;
        priorityNum = 1;
        if(simulator.getSimulatorTime() > 0){
            System.out.println("!!!Error:PPPPPPPPPP+Time."+simulator.getSimulatorTime());
                throw new UnsupportedOperationException("Error:PPPPPPPPPPPPPP");
        }
        try {
            DEVSSimulatorInterface devsSimulator = (DEVSSimulatorInterface) PortSimulationModel.simulator;
            Port port = new Port(devsSimulator);
//System.out.println("port构造成功");
            this.constructQuayCraneModel(PortSimulationModel.simulator, port);
            this.constructAGVModel(PortSimulationModel.simulator, port);
            this.constructYardCraneModel(PortSimulationModel.simulator, port);
            this.constructShipModel(PortSimulationModel.simulator, port);
            this.constructOutputModel(PortSimulationModel.simulator,port);
        } catch (CloneNotSupportedException ex) {
            java.util.logging.Logger.getLogger(PortSimulationModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * 输出结果的进程;
     * @param simulator
     * @param port 
     */
    private void constructOutputModel(SimulatorInterface simulator, Port port){
        DEVSSimulatorInterface devsSimulator = (DEVSSimulatorInterface) simulator;
        try {
            devsSimulator.scheduleEvent(new SimEvent(1, this, OutputProcess.class, "<init>",
                    new Object[]{simulator, port}));
        } catch (RemoteException | SimRuntimeException ex) {
            java.util.logging.Logger.getLogger(PortSimulationModel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void constructShipModel(SimulatorInterface simulator, Port port) throws RemoteException, SimRuntimeException {
        DEVSSimulatorInterface devsSimulator = (DEVSSimulatorInterface) simulator;
        shipNum = InputParameters.getShipNum();
        arrivalTime = new double[shipNum];
        shipDWT = new String[shipNum];
        InputParameters.createShipArrive("arrivalTime",arrivalTime,InputParameters.getEarliestTimeIn());
        InputParameters.createShipArrive("shipDWT", shipDWT);
        for (int i = 0; i < arrivalTime.length; i++) {
           OutputParameters.addOneRowToFile("ShipArrivingList", i+"\t"+(int)arrivalTime[i]+"\t"+shipDWT[i]); 
        }
        for (int i = 0; i < arrivalTime.length; i++) {
            PortSimulationModel.scheduleShipArrival(arrivalTime[i], devsSimulator, port, shipDWT[i]);
        }
    }

    /**
     * schedules the creation of a boat
     *
     * @param time the time when the boat should arrive
     * @param simulator the simulator on which we schedule
     * @param port the port
     * @throws RemoteException on network failuer
     * @throws SimRuntimeException on simulation exception
     */
    //船舶到达调度
    private static void scheduleShipArrival(final double arrivaltime,
            final DEVSSimulatorInterface simulator, final Port port,
            String shiptype)
            throws RemoteException, SimRuntimeException {
        if (Math.abs(arrivaltime - InputParameters.getEarliestTimeIn()-simulationRealTime)<0.001) {
//System.out.println("start scheduleShipArrival");
            simulator.scheduleEvent(new SimEvent(PortSimulationModel.simulator.getSimulatorTime(),
                    PortSimulationModel.class, Ship.class, "<init>",
                    new Object[]{simulator, port, shiptype, arrivaltime}));
        }
    }
    public static void updateShipArrival() throws RemoteException, SimRuntimeException {
        DEVSSimulatorInterface devsSimulator = (DEVSSimulatorInterface) simulator;
        for (int i = 0; i < arrivalTime.length; i++) {
            if (Math.abs(arrivalTime[i] - InputParameters.getEarliestTimeIn()-simulationRealTime)<0.001) {
                PortSimulationModel.scheduleShipArrival(arrivalTime[i], devsSimulator, CYTMainGUI.port, shipDWT[i]);
            }
        }
    }

    /**
     * @param simulator
     * @param port
     * @throws nl.tudelft.simulation.dsol.SimRuntimeException
     * @throws java.rmi.RemoteException
     * @see nl.tudelft.simulation.dsol.ModelInterface
     * #constructModel(nl.tudelft.simulation.dsol.simulators.SimulatorInterface)
     * 参数输入;
     */
    public void constructQuayCraneModel(SimulatorInterface simulator, Port port)
            throws SimRuntimeException, RemoteException {
        PortSimulationModel.simulator = simulator;
        DEVSSimulatorInterface devsSimulator = (DEVSSimulatorInterface) simulator;

        //----------------------------参数输入-----------------------------------
        //QuayCrane信息初始化;
        double QCNum = port.getQuayCranes().length;
        double[] startTime = new double[(int) QCNum];
        for (int i = 0; i < QCNum; i++) {
            startTime[i] = 0;
        }
        System.out.println("---------岸桥作业线构造成功----------");
        for (int i = 0; i < QCNum; i++) {
            //此处岸桥形式不同，仿真初始化也不一样;
            //在论文里可以以此做对比，分析双小车岸桥在装卸效率上的提高程度;
            if (port.getQuayCranes()[0].getQCType() == 2) {
                //双小车岸桥;     
                this.scheduleQCWorkLineStart(startTime[i], devsSimulator, priorityNum++, port.getQuayCranes()[i], 1, i);//主小车进程;                
                this.scheduleQCWorkLineStart(startTime[i], devsSimulator, priorityNum, port.getQuayCranes()[i], 2, i);//门架小车进程;       
            } else {
                //单小车岸桥;
                //////////有空可以补充，进行比较;/////////
            }
        }
    }
    private void scheduleQCWorkLineStart(double time, DEVSSimulatorInterface simulator, short priority,
            QuayCrane quayCrane, int trolleyType, int num) throws RemoteException, SimRuntimeException {
        simulator.scheduleEvent(new SimEvent(time, priority, this, QuayCraneWork.class, "<init>",
                new Object[]{simulator, quayCrane, trolleyType, num}));//Creat a new QuayCraneWork; one by one;
    }

    private void constructAGVModel(SimulatorInterface simulator, Port port) throws RemoteException, SimRuntimeException {
        PortSimulationModel.simulator = simulator;
        DEVSSimulatorInterface devsSimulator = (DEVSSimulatorInterface) simulator;
        //AGV信息初始化;
        double AGVNumperQC = port.getNumOfAGVPerQC();
        double AGVNum = AGVNumperQC * port.getQuayCranes().length;
        double[] startTime = new double[(int) AGVNum];
        for (int i = 0; i < AGVNum; i++) {
            startTime[i] = 0;
        }
        System.out.println("---------AGV model 构造成功----------");
        for (int j = 0; j < AGVNumperQC; j++) {
            for (int i = 0; i < port.getQuayCranes().length; i++) {
                //构造的时候同一个岸桥的服务AGV要分开;
                this.scheduleAGVWorkStart(startTime[(int)(i * AGVNumperQC + j)], devsSimulator, priorityNum,
                        "AGVNo." + Integer.toString((int)(i * AGVNumperQC + j)), port.getQuayCranes()[i],j, port.getAGVType());
            }
        }
    }
    
    private void scheduleAGVWorkStart(final double time, final DEVSSimulatorInterface simulator, short priority,
            String name, QuayCrane serviceQuayCrane, int num, String agvType) throws RemoteException, SimRuntimeException {

        simulator.scheduleEvent(new SimEvent(time, priority, this, AGV.class, "<init>",
                new Object[]{simulator, name, serviceQuayCrane, num, agvType}));//Creat a new AGV; one by one;
    }

    public void constructYardCraneModel(SimulatorInterface simulator, final Port port)
            throws SimRuntimeException, RemoteException {
        DEVSSimulatorInterface devsSimulator = (DEVSSimulatorInterface) simulator;
        //----------------------------参数输入-----------------------------------
        //YardCrane信息初始化;
        double YCNum = InputParameters.getTotalYardNum()*InputParameters.getTotalBlockNumPerYard()
                *InputParameters.getYCNumPerBlock();
        double[] startTime = new double[(int)YCNum];
        for (int i = 0; i < YCNum; i++) {
            startTime[i] = 0;
        }
        char sectionNum = 'A';
        System.out.println("---------场桥实体构造成功----------");
        int n = 0;
        for(int blockNum = 1;blockNum<=InputParameters.getTotalYardNum();blockNum++){
            for(int j = 0;j<InputParameters.getTotalBlockNumPerYard();j++){
                if(InputParameters.getYCNumPerBlock() == 2){
                    this.scheduleYCStart(startTime[n], devsSimulator, port, blockNum, (char)(sectionNum+j), StaticName.WATERSIDE);
                    n++;
                    this.scheduleYCStart(startTime[n], devsSimulator, port, blockNum, (char)(sectionNum+j), StaticName.LANDSIDE);
                    n++;
                }
            }
        }
    }
    private void scheduleYCStart(final double time, final DEVSSimulatorInterface simulator,
            final Port port,int blockNum,char sectionNum,String number) throws RemoteException, SimRuntimeException {
        simulator.scheduleEvent(new SimEvent(time, this, YardCrane.class, "<init>",
                new Object[]{simulator, port, blockNum, sectionNum, number}));//Creat a new QuayCraneWork; one by one;
    }
    /**
     * @return the simulator
     */
    @Override
    public SimulatorInterface getSimulator() {
        return PortSimulationModel.simulator;
    }
    /**
     * commandline executes the model
     *
     */
    public static void start() {

        try {
            Logger.setLogLevel(Level.WARNING);
            String path1 = "F:/SIMULATION";//"C:/Users/Administrator.USER-20151209UN/Desktop/System1_Berthing";
            ExperimentalFrame experimentalFrame = ExperimentParser
                    .parseExperimentalFrame(URLResource
                            .getResource(path1 + "/XML_PortSimulation_model.xml"));
            System.out.println("before_start");
            experimentalFrame.start();//实验开始,各资源，实体准备就绪
            experimentalFrame.remove();
            System.out.println("start_end");
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

}
