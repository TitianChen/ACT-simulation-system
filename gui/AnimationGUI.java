package gui;

import CYT_model.Port;
import static com.sun.java.accessibility.util.AWTEventMonitor.addActionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import parameter.InputParameters;
/**
 * Java Swing
 * @author YutingChen, 760698296@qq.com Dalian University of Technology
 *
 */
public final class AnimationGUI extends JFrame {

    private static int frameWidth = 700;//界面的宽度  
    private static int frameHeight = 700;//界面的高度  
    private static final long serialVersionUID = 1L;
    private static Thread thread;
    public static int deltaX = 0;
    public static int deltaY = 0;
    public static double scale = 1;
    
    protected void setFrameSize(int width, int height) {
        //frameWidth = width;
        //frameHeight = height;
        //super.setSize(frameWidth, frameHeight);
    }
    private AnimationPanel animationPanel;
    private JPanel buttonPanel;
    private final JButton buttonXUp = new JButton("上移");
    private final JButton buttonXDown = new JButton("下移");
    private final JButton buttonYUp = new JButton("右移");
    private final JButton buttonYDown = new JButton("左移");
    private final JButton buttonZoom = new JButton("放大");
    private final JButton buttonShrink = new JButton("缩小");
    
    public void Show2D(Port port) {
        animationPanel = new AnimationPanel(port,this);//得到面板对象   
        thread = new Thread(animationPanel);//启动面板的动画线程  
        thread.start();
        super.setLayout(new BorderLayout(0,2));   
        //定义面板，并设置为网格布局，4行4列，组件水平、垂直间距均为3
        buttonPanel=new JPanel(new GridLayout(1,6,0,0));
        buttonPanel.add(buttonXUp);
        buttonXUp.addActionListener((java.awt.event.ActionEvent evt) -> {
            AnimationGUI.deltaX +=AnimationGUI.scale*5;
        });
        buttonPanel.add(buttonXDown);
        buttonXDown.addActionListener((java.awt.event.ActionEvent evt) -> {
            AnimationGUI.deltaX -= AnimationGUI.scale*5;
        });
        buttonPanel.add(buttonYUp);
        buttonYUp.addActionListener((java.awt.event.ActionEvent evt) -> {
            AnimationGUI.deltaY += AnimationGUI.scale*5;
        });
        buttonPanel.add(buttonYDown);
        buttonYDown.addActionListener((java.awt.event.ActionEvent evt) -> {
            AnimationGUI.deltaY -= AnimationGUI.scale*5;
        });
        buttonPanel.add(buttonZoom);
        buttonZoom.addActionListener((java.awt.event.ActionEvent evt) -> {
            AnimationGUI.scale += 0.5;
        });
        buttonPanel.add(buttonShrink);
        buttonShrink.addActionListener((java.awt.event.ActionEvent evt) -> {
            AnimationGUI.scale -= 0.5;
        });
        super.add(buttonPanel);
        getContentPane().add(buttonPanel,BorderLayout.NORTH);  
        super.add(animationPanel);//将面板加载到Frame主窗口里  
        getContentPane().add(animationPanel,BorderLayout.CENTER);
    }
    public void closePanel(){
        InputParameters.setNeedAnimation(false);
        super.setVisible(false);
        animationPanel = null;
    }

    public AnimationGUI(Port port) {     
        super.setSize(frameWidth, frameHeight);//设置GUI界面的宽高
        super.setTitle("2D动画_陈禹廷");//设置标题
        super.setResizable(true);//设置窗口大小不可改变
        super.setLocationRelativeTo(null);//设置窗口位置居中
        super.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);//默认关闭操作
        super.setVisible(true);//设置窗口可见
        this.Show2D(port);
    }

}
