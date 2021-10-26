/**
 * This class creates a JFrame and populates it with a JFreeChart 
 * bar chart. 
 * 
 * @author Yishai
 * @version 1.0
 * @since 2019-08-19
 */

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
//import javax.swing.text.StyledDocument;


public class ControlWindow extends JFrame {

	//Ignore
	private static final long serialVersionUID = -2932614175973594471L;
	
	//private static Question question;
	
	//The JFrame used to display everything and its characteristics
	private static JFrame displayFrame;
	private static final int WINDOW_WIDTH = 320;
	private static final int WINDOW_HEIGHT = 85;
	private static final String WINDOW_TITLE = "PiClicker";

	public static final int BEGIN_POLL_BUTTON = 0;
	public static final int STOP_POLL_BUTTON = 1;
	public static final int DISPLAY_RESULTS = 1;
	public static final int CLOSE_BUTTON = -1;

	
	//Boolean for whether or not USB is connected
//	private static boolean connected = false;
	
	private JButton openCloseButton;
	private JButton startStopButton;

	private JTextPane numResponsesText;
	private JTextPane timeElapsedText;
	
	private Display display;
	
	private USBController usb;
	private WebController web;
	
	private JLabel usbLabelRed;
	private JLabel webLabelRed;

	private JLabel usbLabelGreen;
	private JLabel webLabelGreen;

	private String freq1 = "A";
	private String freq2 = "A";
	
	private boolean shouldStart = true;
	
	/**
	 * Used to initialize the display's variables.
	 */
	public ControlWindow(Display display, USBController usb, WebController web) {
		this.display = display;
		this.usb = usb;
		this.web = web;
		
		web.setControlWindow(this);
		usb.setControlWindow(this);
		
		display.setController(this);
		
		begin();
	}
	
	/**
	 * Used to initialize the JFrame.
	 */
	public void begin() {
		//Initialize displayFrame
		displayFrame = new JFrame();
		displayFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		displayFrame.setTitle(WINDOW_TITLE);
		displayFrame.setAlwaysOnTop(true);
		displayFrame.setFocusableWindowState(false);
		displayFrame.setLayout(new FlowLayout());
		
		//Save session on exit.
		//displayFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		displayFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if(web.isSessionStarted()) {
					Object[] options = {"Yes", "No"};
					int choice = JOptionPane.showOptionDialog(displayFrame,
							"Would you like to end the session?",
							"End Session",
							JOptionPane.YES_NO_CANCEL_OPTION, 
							JOptionPane.QUESTION_MESSAGE, 
							null,
							options, 
							options[0]);
					
					if(choice == JOptionPane.YES_OPTION) {
						web.deactivateSession();
					}
				}
				
				System.exit(0);
			}
		});
		
		//Add the chart to displayFrame
		//displayFrame.setContentPane(getChart());
		
		java.net.URL imageURL = ControlWindow.class.getResource("resources/usbRed30.png");
		ImageIcon usbIcon = new ImageIcon(imageURL);
		usbLabelRed = new JLabel(usbIcon);
		displayFrame.add(usbLabelRed);
		
		imageURL = ControlWindow.class.getResource("resources/usbGreen30.png");
		usbIcon = new ImageIcon(imageURL);
		usbLabelGreen = new JLabel(usbIcon);
		displayFrame.add(usbLabelGreen);
		usbLabelGreen.setVisible(false);
		usbLabelGreen.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent arg0) {
				System.out.println("Clicked");
				usb.selectFrequency();
				System.out.println("Opened");
			}
		});
		
		imageURL = ControlWindow.class.getResource("resources/webRed30.png");
		ImageIcon webIcon = new ImageIcon(imageURL);
		webLabelRed = new JLabel(webIcon);
		displayFrame.add(webLabelRed);
		
		imageURL = ControlWindow.class.getResource("resources/webGreen30.png");
		webIcon = new ImageIcon(imageURL);
		webLabelGreen = new JLabel(webIcon);
		displayFrame.add(webLabelGreen);
		webLabelGreen.setVisible(false);

		startStopButton = new JButton("Start");
		startStopButton.addActionListener(StartStop);
		startStopButton.setPreferredSize(new Dimension(65, 30));
		displayFrame.add(startStopButton);
		
		openCloseButton = new JButton("Show");
		openCloseButton.addActionListener(OpenClose);
		openCloseButton.setPreferredSize(new Dimension(70, 30));
		displayFrame.add(openCloseButton);
		

		timeElapsedText = new JTextPane();
		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyleConstants.setAlignment(attr, StyleConstants.ALIGN_CENTER);
		timeElapsedText.setParagraphAttributes(attr, true);
		Font font = new Font(Font.MONOSPACED,
				Font.PLAIN,
				timeElapsedText.getFont().getSize());
		timeElapsedText.setFont(font);
		timeElapsedText.setText("--:--");
		displayFrame.add(timeElapsedText);
		
		numResponsesText = new JTextPane();
//		StyledDocument doc = numResponsesText.getStyledDocument();
		StyleConstants.setAlignment(attr, StyleConstants.ALIGN_CENTER);
		numResponsesText.setParagraphAttributes(attr, true);
		numResponsesText.setFont(font);
		numResponsesText.setText("-");
		numResponsesText.setPreferredSize(new Dimension(27, 23));
		displayFrame.add(numResponsesText);
		
		displayFrame.validate();
		displayFrame.setVisible(true);
		


//		System.out.println(numResponsesText.getWidth());
//
//		System.out.println(numResponsesText.getHeight());
		//Open the display
		//openDisplay();
	}
	
	public void updateUSBStatus(boolean connected) {
		usbLabelRed.setVisible(!connected);
		usbLabelGreen.setVisible(connected);
		displayFrame.revalidate();
	}
	
	public void updateWebStatus(boolean connected) {
		webLabelRed.setVisible(!connected);
		webLabelGreen.setVisible(connected);
		displayFrame.revalidate();
	}
	
	private ActionListener OpenClose = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			OpenClose();
		}
	};
	
	public void OpenClose() {
		toggleDisplay(!display.isOpen(), false);
	}
	
	public void toggleDisplay(boolean state, boolean commandFromWeb) {
		if(state) {
			display.openDisplay();
			setOpenCloseText("Hide");
			
			if(web.isSessionStarted() && !commandFromWeb) {
				web.showPoll();
			}
		}
		else {
			display.closeDisplay();
			setOpenCloseText("Show");
			
			if(web.isSessionStarted() && !commandFromWeb) {
				web.hidePoll();
			}
		}
	}
	
	public void setOpenCloseText(String str) {
		openCloseButton.setText(str);
	}
	
	private ActionListener StartStop = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(web.getDisplayFrame().isVisible()) {
				JOptionPane.showMessageDialog(displayFrame, "Please login or close the login prompt.");
			}
			else {
				StartStop();
			}
		}
	};
	
	public void StartStop() {
		togglePoll(shouldStart, false);
	}
	
	public void togglePoll(boolean state, boolean commandFromWeb) {
		if(state) {
			display.nextQuestion();
			
			if(web.isSessionStarted() && !commandFromWeb) {
				System.out.println("Creating poll");
				web.createPoll();
				web.activatePoll();
				System.out.println("Created poll");
			}
			
			if(web.isSessionStarted() && commandFromWeb) {
				usb.startPoll();
				web.takeScreenshot();
			}
			else if(!web.isSessionStarted()) {
				usb.startPoll();
			}
	
			if(display.isOpen()) {
				display.closeDisplay();
			}
			
			setStartStopText("Stop");
			timeElapsedText.setText("00:00");
			numResponsesText.setText("0");
			shouldStart = false;
		}
		else {
			if(web.isSessionStarted() && !commandFromWeb) {
				web.deactivatePoll();
			}
			
			if(web.isSessionStarted() && commandFromWeb) {
				usb.stopPoll();
			}
			else if(!web.isSessionStarted()) {
				usb.stopPoll();
			}
			
			setStartStopText("Start");
			timeElapsedText.setText("--:--");
			numResponsesText.setText("-");
			shouldStart = true;
		}
	}
	
	public void setFrequency(String freq1, String freq2, boolean commandFromWeb) {
		boolean isnew = !(this.freq1.equals(freq1) && this.freq2.equals(freq2));
		if(!isnew) {
			return;
		}
		
		usb.changeFreq(freq1, freq2);
		this.freq1 = freq1;
		this.freq2 = freq2;
		
		System.out.println("Changing Frequency from web: " +  freq1 + ", " + freq2);
	}
	
	public boolean isPollActive() {
		return !shouldStart;
	}
	
	public void setStartStopText(String str) {
		startStopButton.setText(str);
	}
	
	public void setElapsedTime(int time) {
		int minutes = time / 60;
		int seconds = time % 60;
		
		String minStr = "" + minutes;
		while(minStr.length() < 2) {
			minStr = "0" + minStr;
		}
		
		String secStr = "" + seconds;
		while(secStr.length() < 2) {
			secStr = "0" + secStr;
		}
		
		timeElapsedText.setText(minStr + ":" + secStr);
	}
	
	public void setNumResponsesText(String str) {
		numResponsesText.setText(str);
	}
	
	/**
	 * Used to change the title of the bar chart / notify 
	 * of not being connected
	 * 
	 * @param connectedArg boolean to represent being connected
	 */
	public static void setConnected(boolean connectedArg) {
	}
	
}

