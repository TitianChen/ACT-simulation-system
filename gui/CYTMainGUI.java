package gui;

import CYT_model.MainModel;
import CYT_model.Port;
import CYT_model.PortSimulationModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import parameter.InputParameters;
import static parameter.InputParameters.bigBerthNum;
import static parameter.InputParameters.getBigBerthDWT;
import static parameter.OutputParameters.simulationRealTime;
import parameter.OutputProcess;

public class CYTMainGUI extends javax.swing.JFrame {

    private int berthDWTNum = 7;
    private static Color bgC;
    public static Port port;
    private AnimationGUI animationGUI;
    protected static OutputGUI outputGUI =  null;

    /**
     * Creates new form Antenna
     */
    public CYTMainGUI() {
        initComponents();
        bgC = super.getBackground();

        /**
         * 仿真输入
         */
        realTime.setText("未开始");
        speedText.setText(Integer.toString(InputParameters.getSimulationSpeed()));
        speedText.addActionListener((ActionEvent e) -> {
            String val = speedText.getText();
            InputParameters.setSimulationSpeed(val);
        });
        simulationDayText.setText(Integer.toString((int) (InputParameters.getSimulationYear() * 365)));
        simulationDayText.addActionListener((ActionEvent e) -> {
            InputParameters.setSimulationYear(Double.parseDouble(simulationDayText.getText()) / 365);
        });
        /**
         * 码头布局设计;
         */
        //泊位设计吨级
        berthDWTGroup.add(berth3DWT);
        berthDWTGroup.add(berth5DWT);
        berthDWTGroup.add(berth7DWT);
        berthDWTGroup.add(berth10DWT);
        berthDWTGroup.add(berth15DWT);
        berth3DWT.addItemListener(new berthListener());
        berth5DWT.addItemListener(new berthListener());
        berth7DWT.addItemListener(new berthListener());
        berth10DWT.addItemListener(new berthListener());
        berth15DWT.addItemListener(new berthListener());
        berth7DWT.setSelected(true);
        //锚地数量
        anchorageText.setText(Integer.toString(InputParameters.getAnchorageNum()));
        anchorageText.addActionListener((ActionEvent e) -> {
            String val = anchorageText.getText();
            InputParameters.setAnchorageNum(Integer.parseInt(val));
        });
        //航道线数
        channelText.setText(Integer.toString(InputParameters.getChannelNum()));
        channelText.addActionListener((ActionEvent e) -> {
            String val = channelText.getText();
            InputParameters.setChannelNum(Integer.parseInt(val));
        });
        //泊位数量;
        berthText.setText(Integer.toString(InputParameters.getBigBerthNum()));
        berthText.setEditable(false);
        //岸桥下车道数;
        maxAGVUnderQCText.setText(Integer.toString(InputParameters.getMaxPresentWorkingAGV()));
        maxAGVUnderQCText.addActionListener((ActionEvent e) -> {
            InputParameters.setMaxPresentWorkingAGV((int) Double.parseDouble(maxAGVUnderQCText.getText()));
        });
        //AGV数量/QC;
        maxAGVPerQCText.setText(Integer.toString(InputParameters.getNumOfAGVPerQC()));
        maxAGVPerQCText.addActionListener((ActionEvent e) -> {
            InputParameters.setNumOfAGVPerQC((int) Double.parseDouble(maxAGVPerQCText.getText()));
        });
        //堆场数量;
        yardNumText.setText(Integer.toString(InputParameters.getTotalYardNum()));
        yardNumText.addActionListener((ActionEvent e) -> {
            //
        });
        yardNumText.setEditable(false);
        //QCNumPerBerth
        QCNumPerBerthText.setText(Integer.toString(InputParameters.getQuayCraneNum() / bigBerthNum));
        QCNumPerBerthText.addActionListener((ActionEvent e) -> {
            if (QCNumPerBerthText.getText().contains("~") == true) {
                QCNumPerBerthText.setBackground(Color.PINK);
            } else {
                QCNumPerBerthText.setBackground(Color.WHITE);
                InputParameters.setQuayCraneNumPerBerth(Integer.parseInt(QCNumPerBerthText.getText()));
            }
        });
        //bayNum
        bayNumText.setText(Integer.toString(InputParameters.getTotalBayNum()));
        bayNumText.addActionListener((ActionEvent e) -> {
            InputParameters.setTotalBayNum(Integer.parseInt(bayNumText.getText()));
        });
        //blockNum
        blockNumText.setText(Integer.toString(InputParameters.getTotalBlockNumPerYard()));
        blockNumText.addActionListener((ActionEvent e) -> {
            InputParameters.setTotalBlockNumPerYard(Integer.parseInt(blockNumText.getText()));
        });
        //rowNum
        rowNumText.setText(Integer.toString(InputParameters.getPerYCRowNum()));
        rowNumText.addActionListener((ActionEvent e) -> {
            InputParameters.setPerYCRowNum(Integer.parseInt(rowNumText.getText()));
        });
        /**
         * 船舶到港
         */
        //设计年吞吐量;
        TEUText.setText(Integer.toString((int) InputParameters.getYearTEUW()));
        TEUText.addActionListener((ActionEvent e) -> {
            InputParameters.setYearTEUW(Integer.parseInt(TEUText.getText()));
            shipDeltaTimeText.setText(Double.toString(InputParameters.getShipArrivalDeltaTime()));
        });
        //集港
        gatheringPortText.setText(Integer.toString((int) InputParameters.getEarliestTimeIn()));
        gatheringPortText.addActionListener((ActionEvent e) -> {
            InputParameters.setEarliestTimeIn(Integer.parseInt(gatheringPortText.getText()));
        });
        //出港;
        leavingPortText.setText(Integer.toString((int) InputParameters.getLatestTimeOut()));
        leavingPortText.addActionListener((ActionEvent e) -> {
            InputParameters.setLatestTimeOut(Integer.parseInt(leavingPortText.getText()));
        });
        //间隔时间;
        shipDeltaTimeText.setText(Double.toString(InputParameters.getShipArrivalDeltaTime()));
        shipDeltaTimeText.addActionListener((ActionEvent e) -> {
            InputParameters.setShipArrivalDeltaTime(Double.parseDouble(shipDeltaTimeText.getText()));
        });
        shipDeltaTimeText.setEditable(false);

    }

    /**
     * 更新GUI界面显示参数;
     *
     * @param port
     */
    public static final void updateGUI(Port port) {
        realTime.setText(simulationRealTime + "min");
        if (outputGUI != null) {
            outputGUI.update();
        }
//InputPanel.getTotalTEU().setText("堆箱量："+port.getCNUM());
    }

    public static final void setPort(Port thisPort) {
        port = thisPort;
    }

    /**
     * @return the port
     */
    public static Port getPort() {
        return port;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        berthDWTGroup = new javax.swing.ButtonGroup();
        Simulation = new javax.swing.JPanel();
        realTimeLable = new javax.swing.JLabel();
        speedLabel = new javax.swing.JLabel();
        speedText = new javax.swing.JTextField();
        realTime = new javax.swing.JTextField();
        simulationDay = new javax.swing.JLabel();
        simulationDayText = new javax.swing.JTextField();
        openOutputButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        InputPanel = new javax.swing.JPanel();
        berthDWT = new javax.swing.JLabel();
        berth3DWT = new javax.swing.JRadioButton();
        berth5DWT = new javax.swing.JRadioButton();
        berth7DWT = new javax.swing.JRadioButton();
        berth10DWT = new javax.swing.JRadioButton();
        berth15DWT = new javax.swing.JRadioButton();
        anchorageLabel = new javax.swing.JLabel();
        channelLabel = new javax.swing.JLabel();
        anchorageText = new javax.swing.JTextField();
        channelText = new javax.swing.JTextField();
        yardNumText = new javax.swing.JTextField();
        yardNum = new javax.swing.JLabel();
        berthText = new javax.swing.JTextField();
        berthLabel = new javax.swing.JLabel();
        QCTypeLabel = new javax.swing.JLabel();
        QCNumPerBerthText = new javax.swing.JTextField();
        QCNumPerBerthLabel = new javax.swing.JLabel();
        QCNumPerBerthLabel1 = new javax.swing.JLabel();
        vehicleTypeLabel = new javax.swing.JLabel();
        maxAGVPerQC = new javax.swing.JLabel();
        maxAGVPerQCText = new javax.swing.JTextField();
        YCTypeLabel = new javax.swing.JLabel();
        blockNum = new javax.swing.JLabel();
        blockNumText = new javax.swing.JTextField();
        rowNum = new javax.swing.JLabel();
        rowNumText = new javax.swing.JTextField();
        bayNum = new javax.swing.JLabel();
        bayNumText = new javax.swing.JTextField();
        maxAGVUnderQC = new javax.swing.JLabel();
        maxAGVUnderQCText = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox<>();
        jComboBox2 = new javax.swing.JComboBox<>();
        jComboBox3 = new javax.swing.JComboBox<>();
        jPanel1 = new javax.swing.JPanel();
        TEU = new javax.swing.JLabel();
        TEUText = new javax.swing.JTextField();
        shipDeltaTime = new javax.swing.JLabel();
        shipDeltaTimeText = new javax.swing.JTextField();
        gatheringPort = new javax.swing.JLabel();
        gatheringPortText = new javax.swing.JTextField();
        leavingPortText = new javax.swing.JTextField();
        leavingPort = new javax.swing.JLabel();
        animationButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        stopButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("港口仿真_陈禹廷");
        setResizable(false);

        Simulation.setBorder(javax.swing.BorderFactory.createTitledBorder("仿真基本参数"));

        realTimeLable.setText("当前时间/min");

        speedLabel.setText("仿真速度       ");

        speedText.setText("90");

        realTime.setEditable(false);
        realTime.setText("未开始");
        realTime.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                realTimeActionPerformed(evt);
            }
        });

        simulationDay.setText("仿真总天数");

        simulationDayText.setText("110.000");

        org.jdesktop.layout.GroupLayout SimulationLayout = new org.jdesktop.layout.GroupLayout(Simulation);
        Simulation.setLayout(SimulationLayout);
        SimulationLayout.setHorizontalGroup(
            SimulationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(SimulationLayout.createSequentialGroup()
                .addContainerGap()
                .add(SimulationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(SimulationLayout.createSequentialGroup()
                        .add(realTimeLable, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(realTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(SimulationLayout.createSequentialGroup()
                        .add(simulationDay, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(simulationDayText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(speedLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(speedText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 60, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(133, Short.MAX_VALUE))
        );
        SimulationLayout.setVerticalGroup(
            SimulationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(SimulationLayout.createSequentialGroup()
                .add(SimulationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(realTimeLable)
                    .add(realTime, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(SimulationLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(simulationDay)
                    .add(simulationDayText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(speedLabel)
                    .add(speedText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        openOutputButton.setText("查看数据");
        openOutputButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openOutputButtonActionPerformed(evt);
            }
        });

        startButton.setText("开始仿真");
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });

        InputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("码头布局"));

        berthDWT.setText("泊位设计吨级");

        berth3DWT.setText("3万吨级");
        berth3DWT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                berth3DWTActionPerformed(evt);
            }
        });

        berth5DWT.setText("5万吨级");

        berth7DWT.setText("7万吨级");
        berth7DWT.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                berth7DWTActionPerformed(evt);
            }
        });

        berth10DWT.setText("10万吨级");

        berth15DWT.setText("15万吨级");

        anchorageLabel.setText("锚地数量");

        channelLabel.setText("航道线数");

        anchorageText.setText("3");
        anchorageText.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                anchorageTextActionPerformed(evt);
            }
        });

        channelText.setText("2");

        yardNumText.setText("2");

        yardNum.setText("堆场数量");

        berthText.setText("2");

        berthLabel.setText("泊位数量");

        QCTypeLabel.setText("岸桥");

        QCNumPerBerthText.setText("4");

        QCNumPerBerthLabel.setText("岸桥数量/泊位");

        vehicleTypeLabel.setText("水平运输");

        maxAGVPerQC.setText("AGV数量/岸桥");

        maxAGVPerQCText.setText("6");

        YCTypeLabel.setText("场桥种类");

        blockNum.setText("区段数量/分堆场");

        blockNumText.setText("8");

        rowNum.setText("区段宽/集装箱宽度");

        rowNumText.setText("7");

        bayNum.setText("最大贝位数");

        bayNumText.setText("40");

        maxAGVUnderQC.setText("前沿装卸区车道数");

        maxAGVUnderQCText.setText("4");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "双小车双20ft" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "RMG" }));

        jComboBox3.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "AGV" }));

        org.jdesktop.layout.GroupLayout InputPanelLayout = new org.jdesktop.layout.GroupLayout(InputPanel);
        InputPanel.setLayout(InputPanelLayout);
        InputPanelLayout.setHorizontalGroup(
            InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(InputPanelLayout.createSequentialGroup()
                .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, InputPanelLayout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(QCTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 124, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, InputPanelLayout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(QCNumPerBerthLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(QCNumPerBerthText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 68, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, InputPanelLayout.createSequentialGroup()
                        .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(blockNumText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 58, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(InputPanelLayout.createSequentialGroup()
                        .add(13, 13, 13)
                        .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(blockNum)
                            .add(InputPanelLayout.createSequentialGroup()
                                .add(anchorageLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(anchorageText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(channelLabel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(channelText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 20, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(InputPanelLayout.createSequentialGroup()
                        .add(vehicleTypeLabel)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(jComboBox3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(InputPanelLayout.createSequentialGroup()
                        .add(yardNum, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 50, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .add(yardNumText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 42, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                    .add(InputPanelLayout.createSequentialGroup()
                        .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(rowNum)
                            .add(maxAGVUnderQC))
                        .add(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(InputPanelLayout.createSequentialGroup()
                        .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                            .add(InputPanelLayout.createSequentialGroup()
                                .add(YCTypeLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(InputPanelLayout.createSequentialGroup()
                                .add(berthLabel)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .add(berthText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 44, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                        .add(249, 249, 249))
                    .add(InputPanelLayout.createSequentialGroup()
                        .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(InputPanelLayout.createSequentialGroup()
                                .add(maxAGVUnderQCText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 21, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(maxAGVPerQC)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(maxAGVPerQCText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 31, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                            .add(InputPanelLayout.createSequentialGroup()
                                .add(rowNumText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 19, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(bayNum, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                                .add(bayNumText)))
                        .add(0, 0, Short.MAX_VALUE))))
            .add(InputPanelLayout.createSequentialGroup()
                .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(InputPanelLayout.createSequentialGroup()
                        .add(13, 13, 13)
                        .add(berthDWT)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(berth3DWT)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(berth5DWT)
                        .add(0, 0, 0)
                        .add(berth7DWT)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(berth10DWT)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(berth15DWT))
                    .add(InputPanelLayout.createSequentialGroup()
                        .add(66, 66, 66)
                        .add(QCNumPerBerthLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 80, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        InputPanelLayout.linkSize(new java.awt.Component[] {berth10DWT, berth15DWT, berth3DWT, berth5DWT, berth7DWT}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        InputPanelLayout.linkSize(new java.awt.Component[] {anchorageText, channelText}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        InputPanelLayout.linkSize(new java.awt.Component[] {bayNum, maxAGVPerQC}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        InputPanelLayout.linkSize(new java.awt.Component[] {maxAGVUnderQC, rowNum}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        InputPanelLayout.linkSize(new java.awt.Component[] {maxAGVUnderQCText, rowNumText}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        InputPanelLayout.setVerticalGroup(
            InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, InputPanelLayout.createSequentialGroup()
                .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(berthDWT)
                    .add(berth3DWT)
                    .add(berth5DWT)
                    .add(berth7DWT)
                    .add(berth15DWT)
                    .add(berth10DWT))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(anchorageLabel)
                    .add(channelLabel)
                    .add(anchorageText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(channelText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(yardNum)
                    .add(yardNumText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(berthLabel)
                    .add(berthText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(QCTypeLabel)
                    .add(jComboBox1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(vehicleTypeLabel)
                    .add(jComboBox3, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(YCTypeLabel)
                    .add(jComboBox2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(QCNumPerBerthLabel1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(QCNumPerBerthLabel)
                    .add(QCNumPerBerthText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(maxAGVUnderQC)
                    .add(maxAGVUnderQCText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(maxAGVPerQC)
                    .add(maxAGVPerQCText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.UNRELATED)
                .add(InputPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(blockNum)
                    .add(blockNumText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(rowNum)
                    .add(rowNumText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(bayNum)
                    .add(bayNumText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
        );

        InputPanelLayout.linkSize(new java.awt.Component[] {berth10DWT, berth15DWT, berth3DWT, berth5DWT, berth7DWT}, org.jdesktop.layout.GroupLayout.VERTICAL);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("船舶到港参数"));

        TEU.setText("设计吞吐量/万TEU");

        TEUText.setText("120");

        shipDeltaTime.setText("来船平均间隔/min");

        shipDeltaTimeText.setText("120");

        gatheringPort.setText("最早集港时间/min");

        gatheringPortText.setText("4320");

        leavingPortText.setText("5760");

        leavingPort.setText("最晚出港时间/min");

        org.jdesktop.layout.GroupLayout jPanel1Layout = new org.jdesktop.layout.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(TEU)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(TEUText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(shipDeltaTime))
                    .add(jPanel1Layout.createSequentialGroup()
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                            .add(gatheringPort)
                            .add(leavingPort))
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                            .add(gatheringPortText, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE)
                            .add(leavingPortText))))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(shipDeltaTimeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 35, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel1Layout.linkSize(new java.awt.Component[] {TEU, gatheringPort, leavingPort, shipDeltaTime}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.linkSize(new java.awt.Component[] {TEUText, gatheringPortText, leavingPortText, shipDeltaTimeText}, org.jdesktop.layout.GroupLayout.HORIZONTAL);

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel1Layout.createSequentialGroup()
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(TEU)
                    .add(TEUText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(shipDeltaTime)
                    .add(shipDeltaTimeText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(gatheringPort)
                    .add(gatheringPortText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(leavingPortText, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(leavingPort)))
        );

        animationButton.setText("查看动画");
        animationButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                animationButtonActionPerformed(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("华文新魏", 0, 18)); // NOI18N
        jLabel1.setForeground(new java.awt.Color(0, 102, 102));
        jLabel1.setText("自动化集装箱码头仿真系统");

        stopButton.setText("暂停仿真");
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        resetButton.setText("重置参数");
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        jLabel2.setBackground(new java.awt.Color(0, 102, 51));
        jLabel2.setFont(new java.awt.Font("楷体", 0, 14)); // NOI18N
        jLabel2.setForeground(new java.awt.Color(0, 102, 0));
        jLabel2.setText("By 陈禹廷");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createSequentialGroup()
                        .addContainerGap()
                        .add(startButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(animationButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(openOutputButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(stopButton)
                        .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                        .add(resetButton))
                    .add(layout.createSequentialGroup()
                        .add(117, 117, 117)
                        .add(jLabel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 225, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                        .add(18, 18, 18)
                        .add(jLabel2))
                    .add(layout.createSequentialGroup()
                        .add(10, 10, 10)
                        .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING, false)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, InputPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 477, Short.MAX_VALUE)
                            .add(org.jdesktop.layout.GroupLayout.LEADING, Simulation, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .add(10, 10, 10)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(jLabel1)
                    .add(jLabel2))
                .add(14, 14, 14)
                .add(Simulation, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .add(10, 10, 10)
                .add(InputPanel, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(startButton, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 40, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(animationButton)
                    .add(openOutputButton)
                    .add(stopButton)
                    .add(resetButton))
                .addContainerGap(org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(new java.awt.Component[] {animationButton, openOutputButton, resetButton, startButton, stopButton}, org.jdesktop.layout.GroupLayout.VERTICAL);

        Simulation.getAccessibleContext().setAccessibleName("");
        InputPanel.getAccessibleContext().setAccessibleName("Input");

        getAccessibleContext().setAccessibleName("CYTMainGUI");

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void openOutputButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openOutputButtonActionPerformed

        if (outputGUI == null && simulationRealTime >= 1) {
            java.awt.EventQueue.invokeLater(new Runnable() {
                public void run() {
                    outputGUI = new OutputGUI();
                    outputGUI.setVisible(true);
                }
            });
        } else {

        }
       // OutputGUI.outputGUIMain();    
    }//GEN-LAST:event_openOutputButtonActionPerformed

    private void realTimeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_realTimeActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_realTimeActionPerformed

    private void anchorageTextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_anchorageTextActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_anchorageTextActionPerformed

    private void berth7DWTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_berth7DWTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_berth7DWTActionPerformed

    private void berth3DWTActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_berth3DWTActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_berth3DWTActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void animationButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_animationButtonActionPerformed
        if (simulationRealTime >= 1) {
            if (animationButton.getText().equals("查看动画")) {
                InputParameters.setNeedAnimation(true);
                animationGUI = new AnimationGUI(OutputProcess.getPort());
                animationButton.setText("关闭动画");
            } else if (animationButton.getText().equals("关闭动画")) {
                InputParameters.setNeedAnimation(false);
                animationGUI.closePanel();
                animationButton.setText("查看动画");
            }
        }
    }//GEN-LAST:event_animationButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        if (this.startButton.getText().equals("开始仿真")) {
            InputParameters.setSimulationSpeed(speedText.getText());
            InputParameters.setBigBerthDWT(berthDWTNum);
            System.out.println("berthDWT:" + berthDWTNum);
            if (QCNumPerBerthText.getText().contains("~")) {
                QCNumPerBerthText.setBackground(Color.RED);
                return;
            } else if (Integer.parseInt(QCNumPerBerthText.getText()) < InputParameters.findMinQCNumPerBerth(getBigBerthDWT())
                    || Integer.parseInt(QCNumPerBerthText.getText()) > InputParameters.findMaxQCNumPerBerth(getBigBerthDWT())) {
                //输入的QCNumText不合格;//电脑自动改;
                QCNumPerBerthText.setText(Integer.toString(InputParameters.getQuayCraneNum() / bigBerthNum));
                QCNumPerBerthText.setBackground(Color.cyan);
            } else {
                QCNumPerBerthText.setBackground(getBgC());
                InputParameters.setQuayCraneNumPerBerth(Integer.parseInt(QCNumPerBerthText.getText()));
            }
            InputParameters.setTotalBayNum(Integer.parseInt(bayNumText.getText()));
            InputParameters.setTotalBlockNumPerYard(Integer.parseInt(blockNumText.getText()));
            InputParameters.setPerYCRowNum(Integer.parseInt(rowNumText.getText()));
            InputParameters.setYearTEUW(Integer.parseInt(TEUText.getText()));
            shipDeltaTimeText.setText(Double.toString(InputParameters.getShipArrivalDeltaTime()));
            InputParameters.setSimulationYear(Double.parseDouble(simulationDayText.getText()) / 365);
            InputParameters.setShipArrivalDeltaTime(Double.parseDouble(shipDeltaTimeText.getText()));
            InputParameters.setMaxPresentWorkingAGV((int) Double.parseDouble(maxAGVUnderQCText.getText()));
            InputParameters.setNumOfAGVPerQC((int) Double.parseDouble(maxAGVPerQCText.getText()));
            InputParameters.setAnchorageNum(Integer.parseInt(anchorageText.getText()));
            InputParameters.setChannelNum(Integer.parseInt(channelText.getText()));
            InputParameters.setEarliestTimeIn(Integer.parseInt(gatheringPortText.getText()));
            InputParameters.setLatestTimeOut(Integer.parseInt(leavingPortText.getText()));
            MainModel.startModel();
            TEUText.setEditable(false);
            simulationDayText.setEditable(false);
//shipPropText.setEditable(false);
            shipDeltaTimeText.setEditable(false);
            maxAGVUnderQCText.setEditable(false);
            maxAGVPerQCText.setEditable(false);
            bayNumText.setEditable(false);
            blockNumText.setEditable(false);
            rowNumText.setEditable(false);
            berth3DWT.setEnabled(false);
            berth5DWT.setEnabled(false);
            berth7DWT.setEnabled(false);
            berth10DWT.setEnabled(false);
            berth15DWT.setEnabled(false);
            QCNumPerBerthText.setEditable(false);
            anchorageText.setEditable(false);
            channelText.setEditable(false);
            gatheringPortText.setEditable(false);
            leavingPortText.setEditable(false);
            startButton.setText("结束仿真");
            stopButton.setText("暂停仿真");
            resetButton.setVisible(false);
//clearButton.setEnabled(false);
//changeButton.setEnabled(true);
        } else if (startButton.getText().equals("结束仿真")) {
            PortSimulationModel.stop();
            startButton.setText("开始仿真");
            stopButton.setText("暂停仿真");
            resetButton.setVisible(true);
        } else {
            System.out.println("ErroR：检查CYTMainGUI.StartButton名字");
        }
    }//GEN-LAST:event_startButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        if (this.stopButton.getText().equals("暂停仿真") && startButton.getText().equals("结束仿真")) {
            if (simulationRealTime >= 1) {
                PortSimulationModel.stop();
            }
            stopButton.setText("继续仿真");
        } else if (this.stopButton.getText().equals("继续仿真") && startButton.getText().equals("结束仿真")) {
            PortSimulationModel.resume();
            stopButton.setText("暂停仿真");
        } else {
            System.out.println("CYTMainGUI--改stopButton名字;");
        }
    }//GEN-LAST:event_stopButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        MainModel.reset();
        if (InputParameters.isNeedAnimation() == true) {
            animationGUI.closePanel();
        }
        this.QCNumPerBerthText.setEditable(true);
        this.TEUText.setEditable(true);
        this.anchorageText.setEditable(true);
        this.bayNumText.setEditable(true);
        this.berth3DWT.setEnabled(true);
        this.berth5DWT.setEnabled(true);
        this.berth7DWT.setEnabled(true);
        this.berth10DWT.setEnabled(true);
        this.berth15DWT.setEnabled(true);
        this.blockNumText.setEditable(true);
        this.channelText.setEditable(true);
        this.gatheringPortText.setEditable(true);
        this.leavingPortText.setEditable(true);
        this.maxAGVPerQCText.setEditable(true);
        this.maxAGVUnderQCText.setEditable(true);
        this.rowNumText.setEditable(true);
        this.shipDeltaTimeText.setEditable(true);
        this.simulationDayText.setEditable(true);
        this.speedText.setEditable(true);
        this.yardNumText.setEditable(false);
        this.berthText.setEditable(false);
        realTime.setText("未开始");
        speedText.setText(Integer.toString(InputParameters.getSimulationSpeed()));
        simulationDayText.setText(Integer.toString((int) (InputParameters.getSimulationYear() * 365)));
        /**
         * 码头布局设计;
         */
        //泊位设计吨级
        berth7DWT.setSelected(true);
        //锚地数量
        anchorageText.setText(Integer.toString(InputParameters.getAnchorageNum()));
        //航道线数
        channelText.setText(Integer.toString(InputParameters.getChannelNum()));
        //泊位数量;
        berthText.setText(Integer.toString(InputParameters.getBigBerthNum()));
        //岸桥下车道数;
        maxAGVUnderQCText.setText(Integer.toString(InputParameters.getMaxPresentWorkingAGV()));
        //AGV数量/QC;
        maxAGVPerQCText.setText(Integer.toString(InputParameters.getNumOfAGVPerQC()));
        //堆场数量;
        yardNumText.setText(Integer.toString(InputParameters.getTotalYardNum()));
        yardNumText.setEditable(false);
        //QCNumPerBerth
        QCNumPerBerthText.setText(Integer.toString(InputParameters.getQuayCraneNum() / bigBerthNum));
        //bayNum
        bayNumText.setText(Integer.toString(InputParameters.getTotalBayNum()));
        //blockNum
        blockNumText.setText(Integer.toString(InputParameters.getTotalBlockNumPerYard()));
        //rowNum
        rowNumText.setText(Integer.toString(InputParameters.getPerYCRowNum()));
        /**
         * 船舶到港
         */
        //设计年吞吐量;
        TEUText.setText(Integer.toString((int) InputParameters.getYearTEUW()));
        //集港
        gatheringPortText.setText(Integer.toString((int) InputParameters.getEarliestTimeIn()));
        //出港;
        leavingPortText.setText(Integer.toString((int) InputParameters.getLatestTimeOut()));
        //间隔时间;
        shipDeltaTimeText.setText(Double.toString(InputParameters.getShipArrivalDeltaTime()));
        shipDeltaTimeText.setEditable(false);
    }//GEN-LAST:event_resetButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    //public static void main(String args[]) {
    public static void mainGUI() {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            javax.swing.UIManager.LookAndFeelInfo[] installedLookAndFeels = javax.swing.UIManager.getInstalledLookAndFeels();
            for (int idx = 0; idx < installedLookAndFeels.length; idx++) {
                if ("Nimbus".equals(installedLookAndFeels[idx].getName())) {
                    javax.swing.UIManager.setLookAndFeel(installedLookAndFeels[idx].getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(CYTMainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(CYTMainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(CYTMainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(CYTMainGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new CYTMainGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel InputPanel;
    private javax.swing.JLabel QCNumPerBerthLabel;
    private javax.swing.JLabel QCNumPerBerthLabel1;
    private javax.swing.JTextField QCNumPerBerthText;
    private javax.swing.JLabel QCTypeLabel;
    private javax.swing.JPanel Simulation;
    private javax.swing.JLabel TEU;
    private javax.swing.JTextField TEUText;
    private javax.swing.JLabel YCTypeLabel;
    private javax.swing.JLabel anchorageLabel;
    private javax.swing.JTextField anchorageText;
    private javax.swing.JButton animationButton;
    private javax.swing.JLabel bayNum;
    private javax.swing.JTextField bayNumText;
    private javax.swing.JRadioButton berth10DWT;
    private javax.swing.JRadioButton berth15DWT;
    private javax.swing.JRadioButton berth3DWT;
    private javax.swing.JRadioButton berth5DWT;
    private javax.swing.JRadioButton berth7DWT;
    private javax.swing.JLabel berthDWT;
    private javax.swing.ButtonGroup berthDWTGroup;
    private javax.swing.JLabel berthLabel;
    private javax.swing.JTextField berthText;
    private javax.swing.JLabel blockNum;
    private javax.swing.JTextField blockNumText;
    private javax.swing.JLabel channelLabel;
    private javax.swing.JTextField channelText;
    private javax.swing.JLabel gatheringPort;
    private javax.swing.JTextField gatheringPortText;
    private javax.swing.JComboBox<String> jComboBox1;
    private javax.swing.JComboBox<String> jComboBox2;
    private javax.swing.JComboBox<String> jComboBox3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel leavingPort;
    private javax.swing.JTextField leavingPortText;
    private javax.swing.JLabel maxAGVPerQC;
    private javax.swing.JTextField maxAGVPerQCText;
    private javax.swing.JLabel maxAGVUnderQC;
    private javax.swing.JTextField maxAGVUnderQCText;
    private javax.swing.JButton openOutputButton;
    private static javax.swing.JTextField realTime;
    private javax.swing.JLabel realTimeLable;
    private javax.swing.JButton resetButton;
    private javax.swing.JLabel rowNum;
    private javax.swing.JTextField rowNumText;
    private javax.swing.JLabel shipDeltaTime;
    private javax.swing.JTextField shipDeltaTimeText;
    private javax.swing.JLabel simulationDay;
    private javax.swing.JTextField simulationDayText;
    private javax.swing.JLabel speedLabel;
    private javax.swing.JTextField speedText;
    private javax.swing.JButton startButton;
    private javax.swing.JButton stopButton;
    private javax.swing.JLabel vehicleTypeLabel;
    private javax.swing.JLabel yardNum;
    private javax.swing.JTextField yardNumText;
    // End of variables declaration//GEN-END:variables

    private class berthListener implements ItemListener {

        public berthListener() {
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getItemSelectable() == berth3DWT) {
                berthDWTNum = 3;
                InputParameters.setBigBerthDWT(3);
            } else if (e.getItemSelectable() == berth5DWT) {
                berthDWTNum = 5;
                InputParameters.setBigBerthDWT(5);
            } else if (e.getItemSelectable() == berth7DWT) {
                berthDWTNum = 7;
                InputParameters.setBigBerthDWT(7);
            } else if (e.getItemSelectable() == berth10DWT) {
                berthDWTNum = 10;
                InputParameters.setBigBerthDWT(10);
            } else if (e.getItemSelectable() == berth15DWT) {
                berthDWTNum = 15;
                InputParameters.setBigBerthDWT(15);
            } else {
                JOptionPane.showMessageDialog(new JPanel(), "注意", "还未选定泊位设计吨级", 20);
            }
            //改变岸桥QCNumText
            QCNumPerBerthText.setText(Integer.toString(InputParameters.findMinQCNumPerBerth(getBigBerthDWT()))
                    + "~" + InputParameters.findMaxQCNumPerBerth(getBigBerthDWT()));
            QCNumPerBerthText.setBackground(Color.PINK);
        }
    }

    /**
     * @return the bgC
     */
    public static Color getBgC() {
        return bgC;
    }

}
