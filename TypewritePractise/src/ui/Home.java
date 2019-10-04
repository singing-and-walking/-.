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
	// ����logger
	private Logger logger = LogManager.getLogger();
	// ��¼�ļ�
	FileWriter writer;
	DateFormat df_dateTime = DateFormat.getDateTimeInstance();
	// ͬ������
	// ��ȡ��Ļ�ֱ���
	private final Dimension dScreen = Toolkit.getDefaultToolkit().getScreenSize();
	private final int width = dScreen.width;
	private final int height = dScreen.height;
	// Ҫ�õ�������
	long startTime;
	int rightCount = 0;
	int wrongCount = 0;
	int groupSize = 100;
	String wrongChars = "";
	// ���
	DefaultTableModel model = new DefaultTableModel();
	JTable timeCountTable = new JTable(model);
	Vector<Vector<String>> data = new Vector<>();
	// ��ʱ�߳�
	Thread t = new Thread() {
		public void run() {
			logger.trace("��ʱ�߳̿�ʼ");
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
					showTimeCount.setText("��ʱ��" + getFormatTime(timeCount));
				}
			}
		}
	};
	// 26����ĸ�����Ҫ��ʾ����ĸindex
	int target = 0;
	char[] chars = { 'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
			'u', 'v', 'w', 'x', 'y', 'z' };
	// �������
	JLabel showTimeCount = new JLabel();
	JLabel showRightCount = new JLabel();
	JLabel showWrongCount = new JLabel();
	JLabel showWrongChars = new JLabel();
	// ��������
	JFrame frame = new JFrame();

	// ���췽��
	public Home() {
		try {
			writer = new FileWriter("record.txt", true);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// ��ȫ��ʱ�ߴ��λ��
		frame.setSize(width / 2, height / 2);
		frame.setLocationRelativeTo(null);
		// ��ʼ��ȫ��
//		frame.setExtendedState(frame.MAXIMIZED_BOTH);
		// Ĭ�Ϲرմ�������
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// ��������������

		frame.setLayout(new BorderLayout());
		frame.add(getNorth(), BorderLayout.NORTH);
		frame.add(getSouth(), BorderLayout.SOUTH);
		frame.add(getCenter(), BorderLayout.CENTER);
		// ��ʾ
		frame.setVisible(true);
		// ���ܷ���
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
		columnNames.add("��ʱ");
		columnNames.add("��ȷ����");
		columnNames.add("�������");
		columnNames.add("�����ַ�");
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
				if (rightCount + wrongCount == 1)// �����һ����ϰ���µĵ�һ����ĸ
				{
					// ͬ����ʾ��ʵ������
					if (wrongCount == 0) {
						showWrongCount.setText(null);
						showWrongChars.setText(null);
					}
					showRightCount.setText("��ȷ��" + rightCount + "");
					// ֪ͨ��ʱ�߳̿�ʼ�ɻ�
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
		// ��մ����¼
		rightCount = 0;
		wrongCount = 0;
		wrongChars = "";
	}

	private void judge(char input) {
		if (input == chars[target]) {
			setNextRandomChar();
			showRightCount.setText("��ȷ��" + (++rightCount) + "");
		} else {
			showWrongCount.setText("����" + (++wrongCount) + "");
			wrongChars += "  " + chars[target];
			showWrongChars.setText("����ļ���" + wrongChars);
		}

		// ���һ����ϰ�����ˣ�
		if (rightCount == groupSize) {
			Vector<String> row = new Vector<>();
			row.add(showTimeCount.getText());
			row.add(showRightCount.getText());
			row.add(showWrongCount.getText());
			row.add(showWrongChars.getText());
			data.add(row);
			timeCountTable.updateUI();
			initCount();
			// д���¼�ļ�
			try {
				Date date = new Date(System.currentTimeMillis());
				writer.write(row.get(0) + "\t" + row.get(1) + "\t" + row.get(2) + "\t" + row.get(3) + "\t��ϰ���ڣ�"
						+ df_dateTime.format(date) + "\n");
				writer.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}
}
