package ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Home {
	//创建logger
	private Logger logger=LogManager.getLogger();
	//获取屏幕分辨率
	private final Dimension dScreen=Toolkit.getDefaultToolkit().getScreenSize();
	private final int width=dScreen.width;
	private final int height=dScreen.height;
	//要用到的数据
	long startTime;
	int rightCount=0;
	int wrongCount=0;
	long last100Time;
	Set<Integer> wrongSet=new HashSet<>();
	//计时线程
	Thread t=new Thread() {
		public void run() {
			logger.trace("计时线程开始");
			while(true)
			{
					int timeCount=(int) (System.currentTimeMillis()-startTime);
					showTimeCount.setText("计时："+getFormatTime(timeCount));

			}
		}
	};
	//26个字母数组和要显示的字母index
	int target=0;;
	char[] chars= {'a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	//界面组件
	JLabel showTimeCount=new JLabel();
	JLabel showRightCount=new JLabel();
	JLabel showWrongCount=new JLabel();
	JButton start=new JButton("计时开始");
	//界面容器
	JFrame frame=new JFrame();
	JPanel north=new JPanel();
	JPanel south=new JPanel();
	JPanel center=new JPanel() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		public void paint(Graphics g) {
			int size=50;
			g.setFont(new Font("Consolas",Font.PLAIN,size));
			g.drawString(chars[target]+"", frame.getWidth()/2-size/2, (int) (frame.getHeight()*(1-0.618)));
		};
	};
	public Home() {
		//非全屏时尺寸和位置
		frame.setSize(width/2,height/2 );
		frame.setLocationRelativeTo(null);
		//初始即全屏
//		frame.setExtendedState(frame.MAXIMIZED_BOTH);
		//默认关闭窗口设置
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//向容器中添加组件
		north.setLayout(new FlowLayout(FlowLayout.CENTER,20,0));
		north.add(showTimeCount);
		north.add(showRightCount);
		north.add(showWrongCount);
		south.add(start);
		frame.setLayout(new BorderLayout());
		frame.add(north,BorderLayout.NORTH);
		frame.add(south,BorderLayout.SOUTH);
		frame.add(center,BorderLayout.CENTER);
		//显示
		frame.setVisible(true);
		//功能方法
		frame.requestFocus();
		showRandomChar();
		addListener();
	}
	private void addListener(){
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				if(e.getKeyChar()==chars[target])
				{
					showRightCount.setText("正确："+(++rightCount)+"");
					if(rightCount%100==0)
					{
						long current=System.currentTimeMillis();
						logger.info("第"+rightCount/100+"组："+"\t错误："+wrongCount+"\t用时："+getFormatTime((int) (current-last100Time)));
						//打印出错字符
						System.out.print("出错的字符：");
						for(int i:wrongSet)
							System.out.print("\t"+chars[i]);
						System.out.println();
						//清空错误记录
						wrongCount=0;
						wrongSet.clear();
						last100Time=current;
					}
					showRandomChar();
				}
				else
				{
					showWrongCount.setText("错误："+(++wrongCount)+"");
					wrongSet.add(target);
				}
			}

		});
		start.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				last100Time=startTime=System.currentTimeMillis();
				t.start();
				frame.requestFocus();
			}
		});
	}
	private void showRandomChar()
	{
		int old=target;
		while((target=(int) (Math.random()*26))==old);
		frame.repaint();
	}
	private String getFormatTime(int millsTime)
	{
		int millis=millsTime%1000;
		int second=millsTime/1000%60;
		int minute=millsTime/(1000*60);
		return minute+":"+second+":"+String.format("%03d", millis);
	}
}
