/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import parameter.InputParameters;
import parameter.OutputParameters;

/**
 *
 * @author YutingChen, 760698296@qq.com Dalian University of Technology
 */
class OutputPane extends JTabbedPane{
    private static final long serialVersionUID = 1L;
    private static GridBagLayout gridbag;//布局形式;
    private static JPanel inputPanel;
    private static JPanel outputPanel;
    private final JButton openSATButton;
    private final JButton openCShipButton;
    private final JButton openAGVButton;
    private final JButton openYardButton;
    
    public OutputPane(){
        super();
        super.setSize(200, 400);
        gridbag = new GridBagLayout();
        //面板1;       
        inputPanel = new JPanel(false);
        inputPanel.setLayout(gridbag);
        inputPanel.addMouseListener(new displayTable(inputPanel));
        super.addTab("输入数据", inputPanel);
        super.setSelectedIndex(0);
        //面板2;
        outputPanel = new JPanel(false);
        outputPanel.setLayout(gridbag);
        openSATButton = new JButton("OpenShipArrivingList");
        openSATButton.addActionListener((ActionEvent e) -> {
            String[] name = {"编号", "计划到港时间(min)", "DWT"};
            JTable shipTable = new JTable(OutputParameters.getShipArrivingList(), name);
            JScrollPane shipTablePane = new JScrollPane(shipTable);
            shipTable.setEnabled(false);
            JFrame tableF = new JFrame("ShipArriving");
            tableF.add(shipTablePane);
            tableF.pack();
            tableF.setVisible(true);
        });
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;c.gridy = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(openSATButton, c);
        outputPanel.add(openSATButton);
        
        openCShipButton = new JButton("OpenCurrentShipInfo");
        openCShipButton.addActionListener((ActionEvent e) -> {
            ////////查看当前在泊船舶信息;
        });
        c.gridx = 0;c.gridy = 1;
        gridbag.setConstraints(openCShipButton, c);
        outputPanel.add(openCShipButton);
        
        openAGVButton = new JButton("OpenAGVsInfo");
        openAGVButton.addActionListener((ActionEvent e) -> {
            ////////查看AGV信息;
        });
        c.gridx = 0;c.gridy = 2;
        gridbag.setConstraints(openAGVButton, c);
        outputPanel.add(openAGVButton);
        
        openYardButton = new JButton("OpenYardsInfo");
        openYardButton.addActionListener((ActionEvent e) -> {
            ////////查看Yard信息;
        });
        c.gridx = 0;c.gridy = 3;
        gridbag.setConstraints(openYardButton, c);
        outputPanel.add(openYardButton);
        
        super.addTab("输出数据", outputPanel);
        super.setSelectedIndex(0);
    }
    private static class displayTable implements MouseListener {
        public JComponent component;
        public JScrollPane tablePane = new JScrollPane();
        public displayTable(JComponent component) {
            this.component = component;
            component.add(tablePane);
            tablePane.setVisible(false);
        }
        @Override
        public void mouseClicked(MouseEvent e) {
            component.remove(tablePane);
            JTable table = new JTable(CYTMainGUI.port.getInputMatrix(), CYTMainGUI.port.getInputStringName());
            tablePane = new JScrollPane(table);
            component.add(tablePane);
            table.setEnabled(false);
            tablePane.setEnabled(false);
            GridBagConstraints c = new GridBagConstraints();
            c.fill = GridBagConstraints.HORIZONTAL;
            c.gridx = 0;c.gridy = 0;
            gridbag.setConstraints(tablePane, c);
            tablePane.setVisible(true);
        }

        @Override
        public void mousePressed(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseEntered(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }

        @Override
        public void mouseExited(MouseEvent e) {
            //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    } 
}
