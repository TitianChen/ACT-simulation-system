/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import CYT_model.MainModel;
import CYT_model.PortSimulationModel;
import algorithm.FileMethod;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import parameter.InputParameters;
import static parameter.InputParameters.bigBerthNum;
import static parameter.InputParameters.getBigBerthDWT;
import static parameter.OutputParameters.simulationRealTime;
import parameter.OutputProcess;

/**
 * @author YutingChen, 760698296@qq.com Dalian University of Technology
 */
class InputPanel extends JPanel {
    public static Color bgC = Color.LIGHT_GRAY;
    private static GridBagLayout gridbag;//布局形式;
    private static JLabel realTime;
    private static JLabel totalTEU;
    private static JLabel speedLabel;
    private static JTextField speedText;
    private static JLabel anchorageLabel;
    private static JTextField anchorageText;
    private static JLabel channelLabel;
    private static JTextField channelText;
    
    private static JLabel simulationDay;
    private static JTextField simulationDayText;
    private static JLabel shipDeltaTime;
    private static JTextField shipDeltaTimeText;    
    private static JLabel maxAGVUnderQC;
    private static JTextField maxAGVUnderQCText;
    private static JLabel berthDWT;
    private static ButtonGroup berthDWTGroup = new ButtonGroup();
    private static JLabel QCNumPerBerthLabel;
    private static JTextField QCNumPerBerthText;
    private static JLabel bayNum;
    private static JTextField bayNumText;
    private static JLabel blockNum;
    private static JTextField blockNumText;    
    private final JLabel rowNum;
    private final JTextField rowNumText;
    private static JButton startButton;
    private static JButton endButton;
    private static JButton clearButton;    
    private static JButton stopButton;
    private static JButton resumeButton;    
    private static JButton changeButton;
    private static JButton animationButton;    
    private static JButton closeAnimationButton;
    private static JButton openOutputButton;
    private static final long serialVersionUID = 1L;
    private static JButton resetButton;
    private AnimationGUI animationGUI;
    private static JLabel maxAGVPerQC;
    private static JTextField maxAGVPerQCText;
    private static JLabel gatheringPort;
    private static JTextField gatheringPortText;
    private static JTextField leavingPortText;
    private static JLabel leavingPort;
    private static JLabel TEU;
    private static JTextField TEUText;
    private static JLabel berthLabel;
    private static JTextField berthText;
    private static JLabel yardNum;
    private static JTextField yardNumText;
    private static JLabel QCTypeLabel;
    private static JTextField QCTypeText;
    private static JRadioButton berth3DWT;
    private static JRadioButton berth5DWT;
    private static JRadioButton berth7DWT;
    private static JRadioButton berth10DWT;
    private static JRadioButton berth15DWT;
    private static int berthDWTNum = 7;
    private final JLabel shipPropLabel;
    private final JTextField shipPropText;
    private final JLabel shipPropExampleText;



    public InputPanel() {
        super();
        bgC = super.getBackground();
        //Font font = new Font("TimesRoman", Font.PLAIN, 12);
        //Font font = new Font("华文新魏", Font.PLAIN, 14);
        Font font = new Font("楷体", Font.PLAIN, 12);
        gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        super.setLayout(gridbag);
        
        JLabel title = new JLabel("自动化集装箱码头仿真系统");
        title.setFont(new Font("幼圆", Font.BOLD, 18));
        super.add(title);
        c.gridx = 0;c.gridy = 0;c.gridwidth = 6;
        c.fill = GridBagConstraints.CENTER;
        gridbag.setConstraints(title, c);
        c.gridwidth = 1;
        JLabel author = new JLabel("By YutingChen");
        author.setFont(new Font("Times", Font.PLAIN, 12));
        super.add(author);
        c.gridx+=5;//c.gridy++;//c.gridwidth = 6;
        c.fill = GridBagConstraints.CENTER;
        gridbag.setConstraints(author, c);
        c.gridwidth = 1;
        
        realTime = new JLabel("仿真时间：" + "未开始");
        realTime.setFont(font);
        super.add(realTime);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;c.gridy++;
        c.gridwidth = 2;
        gridbag.setConstraints(realTime, c);
        c.gridwidth = 1;
        
        totalTEU = new JLabel("堆箱量：" + "0");
        totalTEU.setFont(font);
        super.add(totalTEU);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx += 2;
        c.gridwidth = 2;
        gridbag.setConstraints(totalTEU, c);
        c.gridwidth = 1;

        speedLabel = new JLabel("仿真速度：(min/s)");
        speedLabel.setFont(font);
        super.add(speedLabel);
        c.gridx += 2;
        gridbag.setConstraints(speedLabel, c);
        
        speedText = new JTextField(Integer.toString(InputParameters.getSimulationSpeed()), 5);
        speedText.addActionListener((ActionEvent e) -> {
            String val = speedText.getText();
            InputParameters.setSimulationSpeed(val);
        });
        super.add(speedText);
        c.gridx ++;
        gridbag.setConstraints(speedText, c);
        
        berthDWT = new JLabel("泊位设计DWT");
        berthDWT.setFont(font);
        super.add(berthDWT);
        c.gridx = 0;c.gridy++;
        gridbag.setConstraints(berthDWT, c);
        berth3DWT = new JRadioButton("3万DWT");
        berth5DWT = new JRadioButton("5万DWT");
        berth7DWT = new JRadioButton("7万DWT");
        berth10DWT = new JRadioButton("10万DWT");
        berth15DWT = new JRadioButton("15万DWT");
        berth3DWT.setFont(font);
        berth5DWT.setFont(font);
        berth7DWT.setFont(font);
        berth10DWT.setFont(font);
        berth15DWT.setFont(font);
        berthDWTGroup.add(berth3DWT);
        berthDWTGroup.add(berth5DWT);
        berthDWTGroup.add(berth7DWT);
        berthDWTGroup.add(berth10DWT);
        berthDWTGroup.add(berth15DWT);
        super.add(berth3DWT);
        super.add(berth5DWT);
        super.add(berth7DWT);
        super.add(berth10DWT);
        super.add(berth15DWT);
        c.gridx++;gridbag.setConstraints(berth3DWT, c);
        c.gridx++;gridbag.setConstraints(berth5DWT, c);
        c.gridx++;gridbag.setConstraints(berth7DWT, c);       
        c.gridx++;gridbag.setConstraints(berth10DWT, c);       
        c.gridx++;gridbag.setConstraints(berth15DWT, c);      
        berth3DWT.addItemListener(new itemListener());
        berth5DWT.addItemListener(new itemListener());
        berth7DWT.addItemListener(new itemListener());
        berth10DWT.addItemListener(new itemListener());
        berth15DWT.addItemListener(new itemListener());
        berth3DWT.setBackground(bgC);
        berth5DWT.setBackground(bgC);
        berth7DWT.setBackground(bgC);
        berth10DWT.setBackground(bgC);
        berth15DWT.setBackground(bgC);
        
        
                
//        //InputParameters.prop15 = (int) (0.01 * 100);
//        InputParameters.prop12 = (int) (0.02 * 100);
//        InputParameters.prop10 = (int) (0.17 * 100);
//        InputParameters.prop7 = (int) (0.20 * 100);
//        InputParameters.prop5 = (int) (0.25 * 100);
//        InputParameters.prop3 = (int) (0.35 * 100);
        shipPropLabel = new JLabel("船舶吨级比例/%");
        shipPropLabel.setFont(font);
        super.add(shipPropLabel);
        c.gridx =0;c.gridy ++;
        gridbag.setConstraints(shipPropLabel, c);
        shipPropExampleText = new JLabel(
                "对应吨级(万){3;5;7;10;12;15}", 10);
        shipPropExampleText.setFont(font);
        super.add(shipPropExampleText);c.gridx ++;c.gridwidth = 2;gridbag.setConstraints(shipPropExampleText, c);
        shipPropText = new JTextField("{"+Double.toString(InputParameters.getProp3()) + "; "
                + Double.toString(InputParameters.getProp5()) + "; "
                + Double.toString(InputParameters.getProp7()) + "; "
                + Double.toString(InputParameters.getProp10()) + "; "
                + Double.toString(InputParameters.getProp12()) + "; "
                 + Double.toString(InputParameters.getProp15()) + "}", 10);
        super.add(shipPropText);c.gridx +=c.gridwidth;c.gridwidth = 3;gridbag.setConstraints(shipPropText, c);
        c.gridwidth = 1;
        
        anchorageLabel = new JLabel("锚地数量");
        anchorageLabel.setFont(font);
        super.add(anchorageLabel);
        c.gridx = 0;c.gridy ++;
        gridbag.setConstraints(anchorageLabel, c);
        
        anchorageText = new JTextField(Integer.toString(InputParameters.getAnchorageNum()), 5);
        anchorageText.addActionListener((ActionEvent e) -> {
            String val = anchorageText.getText();
            InputParameters.setAnchorageNum(Integer.parseInt(val));
        });
        super.add(anchorageText);
        c.gridx++;
        gridbag.setConstraints(anchorageText, c);
        
        channelLabel = new JLabel("航道线数");
        channelLabel.setFont(font);
        super.add(channelLabel);
        c.gridx++;
        gridbag.setConstraints(channelLabel, c);
        
        channelText = new JTextField(Integer.toString(InputParameters.getChannelNum()), 5);
        channelText.addActionListener((ActionEvent e) -> {
            String val = channelText.getText();
            InputParameters.setChannelNum(Integer.parseInt(val));
        });
        super.add(channelText);
        c.gridx ++;
        gridbag.setConstraints(channelText, c);
        
        berthLabel = new JLabel("泊位数量");
        berthLabel.setFont(font);
        super.add(berthLabel);
        c.gridx ++;
        gridbag.setConstraints(berthLabel, c);
        
        berthText = new JTextField(Integer.toString(InputParameters.getBigBerthNum()), 5);
        super.add(berthText);
        c.gridx ++;
        gridbag.setConstraints(berthText, c);
        berthText.setEditable(false);

        TEU = new JLabel("年吞吐量/万TEU");
        TEU.setFont(font);
        super.add(TEU);
        c.gridx = 0;c.gridy ++;
        gridbag.setConstraints(TEU, c);
        
        TEUText = new JTextField(Integer.toString((int)InputParameters.getYearTEUW()), 5);
        TEUText.addActionListener((ActionEvent e) -> {
            InputParameters.setYearTEUW(Integer.parseInt(TEUText.getText()));
            shipDeltaTimeText.setText(Double.toString(InputParameters.getShipArrivalDeltaTime()));
        });
        super.add(TEUText);
        c.gridx ++;
        gridbag.setConstraints(TEUText, c);
        
        simulationDay = new JLabel("仿真时长/天");
        simulationDay.setFont(font);
        super.add(simulationDay);
        c.gridx ++;
        gridbag.setConstraints(simulationDay, c);
        
        simulationDayText = new JTextField(Integer.toString((int)(InputParameters.getSimulationYear()*365)), 5);
        simulationDayText.addActionListener((ActionEvent e) -> {
            InputParameters.setSimulationYear(Double.parseDouble(simulationDayText.getText())/365);
        });
        super.add(simulationDayText);
        c.gridx ++;
        gridbag.setConstraints(simulationDayText, c);
        
        shipDeltaTime = new JLabel("来船平均间隔/min");
        shipDeltaTime.setFont(font);
        super.add(shipDeltaTime);
        c.gridx ++;
        gridbag.setConstraints(shipDeltaTime, c);
        
        shipDeltaTimeText = new JTextField(Double.toString(InputParameters.getShipArrivalDeltaTime()), 5);
        shipDeltaTimeText.addActionListener((ActionEvent e) -> {
            InputParameters.setShipArrivalDeltaTime(Double.parseDouble(shipDeltaTimeText.getText()));
        });
        super.add(shipDeltaTimeText);
        c.gridx ++;
        gridbag.setConstraints(shipDeltaTimeText, c);
        shipDeltaTimeText.setEditable(false);
        
        maxAGVUnderQC = new JLabel("岸桥下AGV车道数");
        maxAGVUnderQC.setFont(font);
        super.add(maxAGVUnderQC);
        c.gridx = 0;c.gridy ++;
        gridbag.setConstraints(maxAGVUnderQC, c);
        
        maxAGVUnderQCText = new JTextField(Integer.toString(InputParameters.getMaxPresentWorkingAGV()), 5);
        maxAGVUnderQCText.addActionListener((ActionEvent e) -> {
            InputParameters.setMaxPresentWorkingAGV((int)Double.parseDouble(maxAGVUnderQCText.getText()));
        });
        super.add(maxAGVUnderQCText);
        c.gridx ++;
        gridbag.setConstraints(maxAGVUnderQCText, c);
        
        maxAGVPerQC = new JLabel("MaxAGVLinePerQC");
        maxAGVPerQC.setFont(font);
        super.add(maxAGVPerQC);
        c.gridx ++;
        gridbag.setConstraints(maxAGVPerQC, c);
        
        maxAGVPerQCText = new JTextField(Integer.toString(InputParameters.getNumOfAGVPerQC()), 5);
        maxAGVPerQCText.addActionListener((ActionEvent e) -> {
            InputParameters.setNumOfAGVPerQC((int)Double.parseDouble(maxAGVPerQCText.getText()));
        });
        super.add(maxAGVPerQCText);
        c.gridx ++;
        gridbag.setConstraints(maxAGVPerQCText, c);
        
        yardNum = new JLabel("堆场数量");
        yardNum.setFont(font);
        super.add(yardNum);
        c.gridx ++;
        gridbag.setConstraints(yardNum, c);
        
        yardNumText = new JTextField(Integer.toString(InputParameters.getTotalYardNum()), 5);
        yardNumText.addActionListener((ActionEvent e) -> {
            //
        });
        super.add(yardNumText);
        c.gridx ++;
        gridbag.setConstraints(yardNumText, c);
        yardNumText.setEditable(false);
        
        QCNumPerBerthLabel = new JLabel("QCs/Berth");
        QCNumPerBerthLabel.setFont(font);
        super.add(QCNumPerBerthLabel);
        c.gridx = 0;c.gridy ++;
        gridbag.setConstraints(QCNumPerBerthLabel, c);
        
        QCNumPerBerthText = new JTextField(Integer.toString(InputParameters.getQuayCraneNum()/bigBerthNum), 5);
        QCNumPerBerthText.setEnabled(true);
        QCNumPerBerthText.addActionListener((ActionEvent e) -> {
            if (QCNumPerBerthText.getText().contains("~") == true) {
                QCNumPerBerthText.setBackground(Color.PINK);
            } else {
                QCNumPerBerthText.setBackground(Color.WHITE);
                InputParameters.setQuayCraneNumPerBerth(Integer.parseInt(QCNumPerBerthText.getText()));
            }
        });
        super.add(QCNumPerBerthText);
        c.gridx ++;
        gridbag.setConstraints(QCNumPerBerthText, c);
        
        QCTypeLabel = new JLabel("岸桥型号");
        QCTypeLabel.setFont(font);
        super.add(QCTypeLabel);
        c.gridx ++;
        gridbag.setConstraints(QCTypeLabel, c);
        
        QCTypeText = new JTextField("双小车岸桥", 6);
        QCTypeText.setEditable(false);
        QCTypeText.addActionListener((ActionEvent e) -> {
            //
        });
        super.add(QCTypeText);
        c.gridx ++;
        gridbag.setConstraints(QCTypeText, c);
        QCTypeText.setEditable(false);
        
        bayNum = new JLabel("BayNum(20~50)");
        bayNum.setFont(font);
        super.add(bayNum);
        c.gridx = 0;c.gridy ++;
        gridbag.setConstraints(bayNum, c);
        
        bayNumText = new JTextField(Integer.toString(InputParameters.getTotalBayNum()), 5);
        bayNumText.addActionListener((ActionEvent e) -> {
            InputParameters.setTotalBayNum(Integer.parseInt(bayNumText.getText()));
        });
        super.add(bayNumText);
        c.gridx ++;
        gridbag.setConstraints(bayNumText, c);
        
        blockNum = new JLabel("BlockNum");
        blockNum.setFont(font);
        super.add(blockNum);
        c.gridx ++;
        gridbag.setConstraints(blockNum, c);
        
        blockNumText = new JTextField(Integer.toString(InputParameters.getTotalBlockNumPerYard()), 5);
        blockNumText.addActionListener((ActionEvent e) -> {
            InputParameters.setTotalBlockNumPerYard(Integer.parseInt(blockNumText.getText()));
        });
        super.add(blockNumText);
        c.gridx ++;
        gridbag.setConstraints(blockNumText, c);
        
        
        rowNum = new JLabel("RowNum");
        rowNum.setFont(font);
        super.add(rowNum);
        c.gridx ++;
        gridbag.setConstraints(rowNum, c);
        
        rowNumText = new JTextField(Integer.toString(InputParameters.getPerYCRowNum()), 5);
        rowNumText.addActionListener((ActionEvent e) -> {
            InputParameters.setPerYCRowNum(Integer.parseInt(rowNumText.getText()));
        });
        super.add(rowNumText);
        c.gridx ++;
        gridbag.setConstraints(rowNumText, c);
        
        gatheringPort = new JLabel("最早集港时间/min");
        gatheringPort.setFont(font);
        super.add(gatheringPort);
        c.gridx = 0;c.gridy ++;
        gridbag.setConstraints(gatheringPort, c);
        
        gatheringPortText = new JTextField(Integer.toString((int)InputParameters.getEarliestTimeIn()), 5);
        gatheringPortText.addActionListener((ActionEvent e) -> {
            InputParameters.setEarliestTimeIn(Integer.parseInt(gatheringPortText.getText()));
        });
        super.add(gatheringPortText);
        c.gridx ++;
        gridbag.setConstraints(gatheringPortText, c);

        leavingPort = new JLabel("最晚出港时间/min");
        leavingPort.setFont(font);
        super.add(leavingPort);
        c.gridx ++;
        gridbag.setConstraints(leavingPort, c);
        
        leavingPortText = new JTextField(Integer.toString((int)InputParameters.getLatestTimeOut()), 5);
        leavingPortText.addActionListener((ActionEvent e) -> {
            InputParameters.setLatestTimeOut(Integer.parseInt(leavingPortText.getText()));
        });
        super.add(leavingPortText);
        c.gridx ++;
        gridbag.setConstraints(leavingPortText, c);
        
        //Buttons;
        startButton = new JButton("开始仿真");
        startButton.setFont(font);
        endButton = new JButton("结束仿真");
        endButton.setFont(font);
        resetButton = new JButton("重置");
        resetButton.setFont(font);
        super.add(startButton);
        super.add(endButton);
        super.add(resetButton);
        c.gridx = 0;c.gridy++;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(startButton, c);
        gridbag.setConstraints(endButton, c);
        gridbag.setConstraints(resetButton, c);
        resetButton.setVisible(false);

        berth7DWT.setSelected(true);
        
        startButton.addActionListener((ActionEvent e) -> {
            InputParameters.setSimulationSpeed(speedText.getText());
            InputParameters.setBigBerthDWT(berthDWTNum);
            System.out.println("berthDWT:"+berthDWTNum);
            if(QCNumPerBerthText.getText().contains("~")){
                QCNumPerBerthText.setBackground(Color.RED);
                return;
            }else if(Integer.parseInt(QCNumPerBerthText.getText())<InputParameters.findMinQCNumPerBerth(getBigBerthDWT())||
                    Integer.parseInt(QCNumPerBerthText.getText())>InputParameters.findMaxQCNumPerBerth(getBigBerthDWT())){
                //输入的QCNumText不合格;//电脑自动改;
                QCNumPerBerthText.setText(Integer.toString(InputParameters.getQuayCraneNum()/bigBerthNum));
                QCNumPerBerthText.setBackground(Color.cyan);
            }else {
                QCNumPerBerthText.setBackground(bgC);
                InputParameters.setQuayCraneNumPerBerth(Integer.parseInt(QCNumPerBerthText.getText()));
            }
            InputParameters.setTotalBayNum(Integer.parseInt(bayNumText.getText()));
            InputParameters.setTotalBlockNumPerYard(Integer.parseInt(blockNumText.getText()));
            InputParameters.setPerYCRowNum(Integer.parseInt(rowNumText.getText()));
            InputParameters.setYearTEUW(Integer.parseInt(TEUText.getText()));
            shipDeltaTimeText.setText(Double.toString(InputParameters.getShipArrivalDeltaTime()));
            InputParameters.setSimulationYear(Double.parseDouble(simulationDayText.getText())/365);
            InputParameters.setShipArrivalDeltaTime(Double.parseDouble(shipDeltaTimeText.getText()));
            InputParameters.setMaxPresentWorkingAGV((int)Double.parseDouble(maxAGVUnderQCText.getText()));
            InputParameters.setNumOfAGVPerQC((int) Double.parseDouble(maxAGVPerQCText.getText()));
            InputParameters.setAnchorageNum(Integer.parseInt(anchorageText.getText()));
            InputParameters.setChannelNum(Integer.parseInt(channelText.getText()));
            InputParameters.setEarliestTimeIn(Integer.parseInt(gatheringPortText.getText()));
            InputParameters.setLatestTimeOut(Integer.parseInt(leavingPortText.getText()));
            TEUText.setEditable(false);
            simulationDayText.setEditable(false);
            shipPropText.setEditable(false);
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
            MainModel.startModel();
            startButton.setEnabled(false);
            clearButton.setEnabled(false);
            startButton.setVisible(false);
            endButton.setVisible(true);
            changeButton.setEnabled(true);
        });
        endButton.addActionListener((ActionEvent e) -> {
            PortSimulationModel.stop();
            clearButton.setEnabled(true);
            endButton.setVisible(false);
            resetButton.setVisible(true);
        });
        resetButton.addActionListener((ActionEvent e) -> {
            MainModel.reset();
            this.resetPanel();
            resetButton.setVisible(false);
            startButton.setEnabled(true);
            startButton.setVisible(true);
            animationGUI.closePanel();
        });
        changeButton = new JButton("更改输入");
        changeButton.setFont(font);
        changeButton.addActionListener(new changeInputeAction());
        super.add(changeButton);
        c.gridx+=2;//c.gridy ++;
        gridbag.setConstraints(changeButton, c);
        changeButton.setEnabled(false);

        openOutputButton = new JButton("数据窗口");
        openOutputButton.setFont(font);
        super.add(openOutputButton);
        openOutputButton.addActionListener((ActionEvent e) -> {
            //MainFrame.getRightPane().setVisible(true);
            //MainFrame.getSplitPane().setDividerLocation(0.3);
        });
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx+=2;//c.gridy ++;
        gridbag.setConstraints(openOutputButton, c);

        clearButton = new JButton("清理存储数据");
        clearButton.setFont(font);
        super.add(clearButton);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;c.gridy +=2;
        gridbag.setConstraints(clearButton, c);
        clearButton.setEnabled(true);
        clearButton.addActionListener((ActionEvent e) -> {
            new FileMethod().deleteFile(InputParameters.OUTPUTPATH);
        });

        animationButton = new JButton("查看动画");
        animationButton.setFont(font);
        closeAnimationButton = new JButton("关闭动画");
        closeAnimationButton.setFont(font);
        super.add(animationButton);
        super.add(closeAnimationButton);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx+=2;//c.gridy ++;
        gridbag.setConstraints(animationButton, c);
        gridbag.setConstraints(closeAnimationButton, c);
        animationButton.addActionListener((ActionEvent e) -> {
            if (simulationRealTime >= 1) {
                InputParameters.setNeedAnimation(true);
                animationGUI = new AnimationGUI(OutputProcess.getPort());
                closeAnimationButton.setVisible(true);
                animationButton.setVisible(false);
            }
        });
        closeAnimationButton.addActionListener((ActionEvent e) -> {
            if (simulationRealTime >= 1) {
                InputParameters.setNeedAnimation(false);
                closeAnimationButton.setVisible(false);
                animationButton.setVisible(true);
                animationGUI.closePanel();
            }
        });
        
        stopButton = new JButton("Stop");
        stopButton.setFont(font);
        resumeButton = new JButton("Resume");
        resumeButton.setFont(font);
        super.add(stopButton);
        super.add(resumeButton);
        stopButton.setVisible(true);
        resumeButton.setVisible(false);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx+=2;//c.gridy ++;
        gridbag.setConstraints(stopButton, c);
        gridbag.setConstraints(resumeButton, c);
        stopButton.addActionListener((ActionEvent e) -> {
            if (simulationRealTime >= 1) {
                PortSimulationModel.stop();
            }
            stopButton.setVisible(false);
            resumeButton.setVisible(true);
        });
        resumeButton.addActionListener((ActionEvent e) -> {
            if (simulationRealTime >= 1) {
                PortSimulationModel.resume();
            }
            stopButton.setVisible(true);
            resumeButton.setVisible(false);
        });
        
    }

    @Override
    public void paint(Graphics g){
        super.paint(g);
        Graphics2D graphics2D = (Graphics2D)g;
        ////this.setBackground();///淡紫色
        float[] rgbvals = new float[3];
        float[] hsb = new float[3];
        hsb = Color.RGBtoHSB(220, 242, 255, rgbvals);
        hsb = Color.RGBtoHSB(255, 225, 255, rgbvals);
        bgC = this.getBackground();
        //this.setBackground(bgC);
//        g.setColor(Color.BLUE); 
//        g.drawLine(0, speedLabel.getY()+18, super.getWidth(),speedLabel.getY()+18);
        //this.setBackground(Color.getHSBColor(hsb[0],hsb[1],hsb[2]));//红,白,黑;小-强;
        ///////this.add(new Image());
    }
    /**
     * @return the realTime
     */
    public static JLabel getRealTime() {
        return realTime;
    }

    /**
     * @return the speedLabel
     */
    public static JLabel getSpeedLabel() {
        return speedLabel;
    }

    /**
     * @return the speedText
     */
    public static JTextField getSpeedText() {
        return speedText;
    }

    /**
     * @return the simulationDay
     */
    public static JLabel getSimulationDay() {
        return simulationDay;
    }

    /**
     * @return the simulationDayText
     */
    public static JTextField getSimulationDayText() {
        return simulationDayText;
    }

    /**
     * @return the berthDWT
     */
    public static JLabel getBerthDWT() {
        return berthDWT;
    }

    /**
     * @return the bayNum
     */
    public static JLabel getBayNum() {
        return bayNum;
    }

    /**
     * @return the bayNumText
     */
    public static JTextField getBayNumText() {
        return bayNumText;
    }

    /**
     * @return the startButton
     */
    public static JButton getStartButton() {
        return startButton;
    }

    /**
     * @return the endButton
     */
    public static JButton getEndButton() {
        return endButton;
    }

    /**
     * @return the clearButton
     */
    public static JButton getClearButton() {
        return clearButton;
    }

    /**
     * @return the serialVersionUID
     */
    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    private void resetPanel() {
        realTime.setText("仿真时间：" + "未开始");
        speedLabel = new JLabel("仿真速度：(min/s)");       
        speedText.setText(Integer.toString(InputParameters.getSimulationSpeed()));
        speedText.setEditable(true);
        anchorageText.setText(Integer.toString(InputParameters.getAnchorageNum()));
        anchorageText.setEditable(true);
        channelText.setText(Integer.toString(InputParameters.getChannelNum()));
        channelText.setEditable(true);
        TEUText.setText(Integer.toString((int)(InputParameters.getYearTEUW())));
        TEUText.setEditable(true);
        simulationDayText.setText(Integer.toString((int)(InputParameters.getSimulationYear()*365)));
        simulationDayText.setEditable(true);
        shipDeltaTimeText.setText(Double.toString(InputParameters.getShipArrivalDeltaTime()));
        shipDeltaTimeText.setEditable(true);
        maxAGVUnderQCText.setText(Integer.toString(InputParameters.getMaxPresentWorkingAGV()));    
        maxAGVUnderQCText.setEditable(true);
        maxAGVPerQCText.setText(Integer.toString(InputParameters.getMaxPresentWorkingAGV()));
        maxAGVPerQCText.setEditable(true);
        berth3DWT.setEnabled(true);
        berth5DWT.setEnabled(true);
        berth7DWT.setEnabled(true);
        berth10DWT.setEnabled(true);
        berth15DWT.setEnabled(true);
        berth7DWT.setSelected(true);
        QCNumPerBerthText.setText(Integer.toString(InputParameters.getQuayCraneNum()/bigBerthNum));
        QCNumPerBerthText.setEditable(true);
        bayNumText.setText(Integer.toString(InputParameters.getTotalBayNum()));
        bayNumText.setEditable(true);
        blockNumText.setText(Integer.toString(InputParameters.getTotalBlockNumPerYard()));
        blockNumText.setEditable(true);
        rowNumText.setText(Integer.toString(InputParameters.getPerYCRowNum()));
        rowNumText.setEditable(true);
        gatheringPortText.setText(Integer.toString((int)InputParameters.getEarliestTimeIn()));
        gatheringPortText.setEditable(true);
        leavingPortText.setText(Integer.toString((int)InputParameters.getLatestTimeOut()));
        leavingPortText.setEditable(true);
        startButton.setVisible(true);
        endButton.setVisible(false);
        closeAnimationButton.setVisible(false);
        animationButton.setVisible(true);
        resetButton.setVisible(false);
        changeButton.setBackground(Color.LIGHT_GRAY);
        changeButton.setEnabled(false);
        clearButton.setEnabled(true);      
        stopButton.setVisible(true);
        resumeButton.setVisible(false);   
    }

    private static class changeInputeAction implements ActionListener {

        public changeInputeAction() {
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            InputParameters.setSimulationSpeed(speedText.getText());
        }
    }

    private static class blockNumText {

        public blockNumText() {
        }
    }

    /**
     * @return the totalTEU
     */
    public static JLabel getTotalTEU() {
        return totalTEU;
    }

    /**
     * berth3DWT.addItemListener(new itemListener());
        //c.gridx = 6;c.gridy = 4;
        //gridbag.setConstraints(berthDWTButton, c);
        berthDWTText.addActionListener((ActionEvent e) -> {
            InputParameters.setBigBerthDWT(Integer.parseInt(berthDWTText.getText()));
            //改变岸桥QCNumText
            QCNumPerBerthText.setText(Integer.toString(InputParameters.findMinQCNumPerBerth(getBigBerthDWT()))+
                    "~"+InputParameters.findMaxQCNumPerBerth(getBigBerthDWT()));  
        });
     */
    private static class itemListener implements ItemListener {
        public itemListener() {
        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            if(e.getItemSelectable() == berth3DWT){
                berthDWTNum = 3;
                InputParameters.setBigBerthDWT(3);
            }else if(e.getItemSelectable() == berth5DWT){
                berthDWTNum = 5;
                InputParameters.setBigBerthDWT(5);
            }else if(e.getItemSelectable() == berth7DWT){
                berthDWTNum = 7;
                InputParameters.setBigBerthDWT(7);
            }else if(e.getItemSelectable() == berth10DWT){
                berthDWTNum = 10;
                InputParameters.setBigBerthDWT(10);
            }else if(e.getItemSelectable() == berth15DWT){
                berthDWTNum = 15;
                InputParameters.setBigBerthDWT(15);
            }else{
                JOptionPane.showMessageDialog(new JPanel(), "注意", "还未选定泊位设计吨级",20);
            }
            //改变岸桥QCNumText
            QCNumPerBerthText.setText(Integer.toString(InputParameters.findMinQCNumPerBerth(getBigBerthDWT()))+
                    "~"+InputParameters.findMaxQCNumPerBerth(getBigBerthDWT()));  
            QCNumPerBerthText.setBackground(Color.PINK);
        }
    }

}
