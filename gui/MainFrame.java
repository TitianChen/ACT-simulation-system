/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gui;

import javax.swing.JFrame;
import javax.swing.JSplitPane;

/**
 *
 * @author YutingChen, 760698296@qq.com Dalian University of Technology
 */
public class MainFrame extends JFrame{    
    private static final long serialVersionUID = 1L;
    private static InputPanel leftPanel;//左侧输入面板;
//private static OutputPane rightPane;//右侧窗口;
    private static JSplitPane splitPane;//分栏;
//
//    public MainFrame(){
//        super();
//        super.setTitle("PortSimulation_YutingChen");
//        leftPanel = new InputPanel();
////rightPane = new OutputPane();
// //splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, getLeftPanel(), getRightPane());
//        splitPane.setOneTouchExpandable(true);
//        splitPane.setDividerLocation(150);
//        super.add(splitPane);
//        super.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        super.setSize(700, 500);
//        super.setVisible(true);
////rightPane.setVisible(false);
//    }

    /**
     * @return the leftPanel
     */
    public static InputPanel getLeftPanel() {
        return leftPanel;
    }

    /**
     * @return the splitPane
     */
    public static JSplitPane getSplitPane() {
        return splitPane;
    }
}
