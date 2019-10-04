package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Home {
	// 创建logger
	private Logger logger = LogManager.getLogger();
	// 记录文件
	FileWriter writer;
	DateFormat df_dateTime = DateFormat.getDateTimeInstance();
	// 同步对象
	// 获取屏幕分辨率
	private final Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
	private final int width = dScreen.width;
	private final int height = dScreen.height;
	// 要用到的数据
	long startTime;
	int rightCount = 0;
	int wrongCount = 0;
	int groupSize = 100;
	String wrongChars = "";
	// 表格
	DefaultTableModel model = new DefaultTableModel();
	JTable timeCountTable = new JTable(model);
	Vector<Vector<String>> data = new Vector<>();
	// 计时线程
	Thread t = new Thread() {
		public void run() {
			logger.trace("计时线程开始");
			synchronized (showTimeCount) {
				while (true) {
					if (rightCount == groupSize || rightCount + wrongCount == 0) {
						try {
							showTimeCount.wait();
							startTime = System.currentTimeMillis();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}

					int timeCount = (int) (System.currentTimeMillis() - startTime);
					showTimeCount.setText("计时：" + getFormatTime(timeCount));
				}
			}
		}
	};
	// 26个字母数组和要显示的字母index
	int target = 0;
	char[] chars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
			'u', 'v', 'w', 'x', 'y', 'z' };
	// 界面组件
	JLabel showTimeCount = new JLabel();
	JLabel showRightCount = new JLabel();
	JLabel showWrongCount = new JLabel();
	JLabel showWrongChars = new JLabel();
	// 界面容器
	JFrame frame = new JFrame();

	// 构造方法
	public Home() {
		try {
			writer = new FileWriter("record.txt", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// 非全屏时尺寸和位置
		frame.setSize(width / 2, height / 2);
		frame.setLocationRelativeTo(null);
		// 初始即全屏
//		frame.setExtendedState(frame.MAXIMIZED_BOTH);
		// 默认关闭窗口设置
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 向容器中添加组件

		frame.setLayout(new BorderLayout());
		frame.add(getNorth(), BorderLayout.NORTH);
		frame.add(getSouth(), BorderLayout.SOUTH);
		frame.add(getCenter(), BorderLayout.CENTER);
		// 显示
		frame.setVisible(true);
		// 功能方法
		frame.requestFocus();
		setNextRandomChar();
		addListener();
		t.start();
		initCount();
	}

	private JPanel getNorth() {
		JPanel north = new JPanel();
		north.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
		north.add(showTimeCount);
		north.add(showRightCount);
		north.add(showWrongCount);
		north.setBackground(Color.WHITE);
		return north;
	}

	private JPanel getCenter() {
		JPanel center = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public void paint(Graphics g) {
				super.paint(g);
				int size = 50;
				g.setFont(new Font("Consolas", Font.PLAIN, size));
				g.drawString(chars[target] + "", frame.getWidth() / 2 - size / 2,
						(int) (frame.getHeight() * (1 - 0.618)));
			};
		};
		center.setBackground(Color.WHITE);
		return center;
	}

	private JPanel getSouth() {
		JPanel south = new JPanel();
		Vector<String> columnNames = new Vector<>();
		columnNames.add("用时");
		columnNames.add("正确个数");
		columnNames.add("错误个数");
		columnNames.add("错误字符");
		model.setDataVector(data, columnNames);
		south.setLayout(new BorderLayout());
		south.add(showWrongChars, BorderLayout.NORTH);
		south.add(timeCountTable, BorderLayout.SOUTH);
		south.setBackground(Color.WHITE);
		return south;
	}

	private void addListener() {
		frame.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				// TODO Auto-generated method stub
				judge(e.getKeyChar());
				if (rightCount + wrongCount == 1)// 如果是一组练习敲下的第一个字母
				{
					// 同步显示与实际数据
					if (wrongCount == 0) {
						showWrongCount.setText(null);
						showWrongChars.setText(null);
					}
					showRightCount.setText("正确：" + rightCount + "");
					// 通知计时线程开始干活
					synchronized (showTimeCount) {
						showTimeCount.notifyAll();
					}
				}
			}
		});
	}

	private void setNextRandomChar() {
		int old = target;
		while ((target = (int) (Math.random() * 26)) == old)
			;
		frame.repaint();
	}

	private String getFormatTime(int millsTime) {
		int millis = millsTime % 1000;
		int second = millsTime / 1000 % 60;
		int minute = millsTime / (1000 * 60);
		return minute + ":" + second + ":" + String.format("%03d", millis);
	}

	private void initCount() {
		// 清空错误记录
		rightCount = 0;
		wrongCount = 0;
		wrongChars = "";
	}

	private void judge(char input) {
		if (input == chars[target]) {
			setNextRandomChar();
			showRightCount.setText("正确：" + (++rightCount) + "");
		} else {
			showWrongCount.setText("错误：" + (++wrongCount) + "");
			wrongChars += "  " + chars[target];
			showWrongChars.setText("出错的键：" + wrongChars);
		}

		// 如果一组练习结束了：
		if (rightCount == groupSize) {
			Vector<String> row = new Vector<>();
			row.add(showTimeCount.getText());
			row.add(showRightCount.getText());
			row.add(showWrongCount.getText());
			row.add(showWrongChars.getText());
			data.add(row);
			timeCountTable.updateUI();
			initCount();
			// 写入记录文件
			try {
				Date date = new Date(System.currentTimeMillis());
				writer.write(row.get(0) + "\t" + row.get(1) + "\t" + row.get(2) + "\t" + row.get(3) + "\t练习日期："
						+ df_dateTime.format(date) + "\n");
				writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
