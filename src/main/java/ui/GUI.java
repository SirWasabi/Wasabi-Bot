package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;

public class GUI {

	public JFrame frame = new JFrame("Cona Bot");
	public JButton start = new JButton();
	public JButton close = new JButton();
	public JCheckBox boxOne = new JCheckBox("DISABLE GREETINGS AND GOODBYES");
	public JCheckBox boxTwo = new JCheckBox("DISABLE MUSIC PLAYER");
	public JCheckBox boxThree = new JCheckBox("DISABLE VOLUME CHANGE");
	public JCheckBox boxFour = new JCheckBox("DISABLE ONLINE STATUS PRINT");
	public JCheckBox boxFive = new JCheckBox("DISABLE TEXT TO SPEECH");
	public JCheckBox boxSix = new JCheckBox("DISABLE LYRICS");
	public JPanel panelClose = new JPanel();
	public JPanel panelTop = new JPanel();
	public JPanel panelDown = new JPanel();
	public JPanel panelDownLeft = new JPanel();
	public JTextArea textArea = new JTextArea();
	public JLabel textVolume = new JLabel();
	public JLabel mario = new JLabel();
	public JPanel glass = (JPanel) frame.getGlassPane();
	public JScrollPane scrollAreaText = new JScrollPane(textArea);
	public Icon i = new ImageIcon("images/start.png");
	public Icon g = new ImageIcon("images/startG.png");
	public Icon e = new ImageIcon("images/exit.png");
	public Icon x = new ImageIcon("images/exitG.png");
	public Icon check = new ImageIcon("images/checkbox.png");
	public Icon ticked = new ImageIcon("images/ticked.png");
	public Color color = new Color(24, 24, 23);
	public Color borders = new Color(43, 43, 43);
	public Font font = new Font("VCR OSD Mono", Font.BOLD, 12);
	public Border emptyBorder = BorderFactory.createEmptyBorder();
	public Border titleBorder = BorderFactory.createTitledBorder(new LineBorder(borders, 5, true), "Log", 0, 0,
			font, Color.WHITE);
	public Border titleBorderVolume = BorderFactory.createTitledBorder(new LineBorder(borders, 5, true), "Volume",
			2, 0, font, Color.WHITE);
	public ArrayList<Icon> marios = new ArrayList<Icon>();

	public void addFullscreenListener(Component c) {
		c.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					if (frame.getExtendedState() == JFrame.MAXIMIZED_BOTH) {
						frame.setSize(frame.getPreferredSize());
						frame.setLocationRelativeTo(null);
					} else {
						frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
					}
				}
			}
		});
	}

	public void addButtonHoverListener(final JButton b, final Icon i, final Icon j) {
		b.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mouseEntered(java.awt.event.MouseEvent evt) {
				b.setIcon(i);
			}

			public void mouseExited(java.awt.event.MouseEvent evt) {
				b.setIcon(j);
			}
		});
	}

	public void setupMario() {
		marios.add(new ImageIcon("images/1.png"));
		marios.add(new ImageIcon("images/2.png"));
		marios.add(new ImageIcon("images/3.png"));
		marios.add(new ImageIcon("images/2.png"));
		Timer timer = new Timer(500, new ActionListener() {
			int i = 0;
			@Override
			public void actionPerformed(ActionEvent e) {
				if (i < marios.size()) {
					mario.setIcon(marios.get(i));
					i++;
				} else {
					i = 0;
				}
			}
		});
		timer.start();
	}
	
	public void setCheckboxes(Color background, Color foreground, Font font, Icon unselected, Icon selected, boolean focus) {
		boxOne.setBackground(background);
		boxOne.setForeground(foreground);
		boxOne.setFont(font);
		boxOne.setFocusPainted(focus);
		boxOne.setIcon(unselected);
		boxOne.setSelectedIcon(selected);
		boxTwo.setBackground(background);
		boxTwo.setForeground(foreground);
		boxTwo.setFont(font);
		boxTwo.setFocusPainted(focus);
		boxTwo.setIcon(unselected);
		boxTwo.setSelectedIcon(selected);
		boxThree.setBackground(background);
		boxThree.setForeground(foreground);
		boxThree.setFont(font);
		boxThree.setFocusPainted(focus);
		boxThree.setIcon(unselected);
		boxThree.setSelectedIcon(selected);
		boxFour.setBackground(background);
		boxFour.setForeground(foreground);
		boxFour.setFont(font);
		boxFour.setFocusPainted(focus);
		boxFour.setIcon(unselected);
		boxFour.setSelectedIcon(selected);
		boxFive.setBackground(background);
		boxFive.setForeground(foreground);
		boxFive.setFont(font);
		boxFive.setFocusPainted(focus);
		boxFive.setIcon(unselected);
		boxFive.setSelectedIcon(selected);
		boxSix.setBackground(background);
		boxSix.setForeground(foreground);
		boxSix.setFont(font);
		boxSix.setFocusPainted(focus);
		boxSix.setIcon(unselected);
		boxSix.setSelectedIcon(selected);
	}

	public void addFrameContent() {
		setupMario();
		addButtonHoverListener(start, g, i);
		addButtonHoverListener(close, x, e);
		addFullscreenListener(frame);
		addFullscreenListener(textArea);
		frame.setUndecorated(true);
		ComponentResizer cr = new ComponentResizer();
		cr.registerComponent(frame);
		FrameDragListener frameDragListener = new FrameDragListener(frame);
		frame.addMouseListener(frameDragListener);
		frame.addMouseMotionListener(frameDragListener);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setPreferredSize(new Dimension(900, 500));
		frame.add(panelTop, BorderLayout.NORTH);
		frame.add(panelDown, BorderLayout.CENTER);
		start.setIcon(i);
		start.setBorder(emptyBorder);
		start.setBackground(color);
		close.setIcon(e);
		close.setBorder(emptyBorder);
		close.setBackground(color);
		panelTop.add(start);
		panelTop.add(close);
		panelTop.setBackground(color);
		panelDown.setLayout(new BorderLayout());
		GridLayout layout = new GridLayout(10, 0);
		panelDownLeft.setLayout(layout);
		panelDown.add(panelDownLeft, BorderLayout.WEST);
		DefaultCaret caret = (DefaultCaret) textArea.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		panelDown.add(scrollAreaText);
		panelDownLeft.add(textVolume);
		panelDownLeft.add(boxOne);
		panelDownLeft.add(boxTwo);
		panelDownLeft.add(boxThree);
		panelDownLeft.add(boxFour);
		panelDownLeft.add(boxFive);
		panelDownLeft.add(boxSix);
		glass.setLayout(new BorderLayout());
		glass.add(mario, BorderLayout.SOUTH);
		glass.setVisible(true);
		panelDownLeft.setBackground(color);
		setCheckboxes(color, Color.WHITE, font, check, ticked, false);
		textVolume.setText("50");
		textVolume.setForeground(Color.WHITE);
		textVolume.setBorder(titleBorderVolume);
		textVolume.setFont(font);
		textArea.setFont(font);
		textArea.setDragEnabled(true);
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		scrollAreaText.setBorder(titleBorder);
		scrollAreaText.setBackground(color);
		textArea.setBackground(color);
		textArea.setForeground(Color.WHITE);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
	}
}
