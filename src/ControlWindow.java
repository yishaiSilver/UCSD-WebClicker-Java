/**
 * This class creates a JFrame and populates it with a JFreeChart 
 * bar chart. 
 * 
 * @author Yishai
 * @version 1.0
 * @since 2019-08-19
 */
import java.awt.Color;
import javax.websocket.*;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;


public class ControlWindow extends JFrame {

	//Ignore
	private static final long serialVersionUID = -2932614175973594471L;
	
	//private static Question question;
	
	//The JFrame used to display everything and its characteristics
	private static JFrame displayFrame;
	private static final int WINDOW_WIDTH = 250;
	private static final int WINDOW_HEIGHT = 85;
	private static final String WINDOW_TITLE = "PiClicker";

	public static final int BEGIN_POLL_BUTTON = 0;
	public static final int STOP_POLL_BUTTON = 1;
	public static final int DISPLAY_RESULTS = 1;
	public static final int CLOSE_BUTTON = -1;

	
	//Boolean for whether or not USB is connected
	private static boolean connected = false;
	
	private JButton openCloseButton;
	private JButton startStopButton;
	
	private JTextPane numResponsesText;
	
	private Display display;
	
	private USBController usb;
	private WebController web;
	
	private boolean shouldStart = true;
	
	/**
	 * Used to initialize the display's variables.
	 */
	public ControlWindow(Display display, USBController usb, WebController web) {
		this.display = display;
		this.usb = usb;
		this.web = web;
		web.setControlWindow(this);
		
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

		openCloseButton = new JButton("Open");
		openCloseButton.addActionListener(OpenClose);
		displayFrame.add(openCloseButton);
		
		startStopButton = new JButton("Start");
		startStopButton.addActionListener(StartStop);
		displayFrame.add(startStopButton);
		
		numResponsesText = new JTextPane();
		SimpleAttributeSet attr = new SimpleAttributeSet();
		StyledDocument doc = numResponsesText.getStyledDocument();
		StyleConstants.setAlignment(attr, StyleConstants.ALIGN_CENTER);
		numResponsesText.setParagraphAttributes(attr, true);
		
		
		
		Font font = new Font(Font.MONOSPACED,
				Font.PLAIN,
				numResponsesText.getFont().getSize());
		numResponsesText.setFont(font);
		numResponsesText.setText("-");
		displayFrame.add(numResponsesText);
		
		displayFrame.validate();
		displayFrame.setVisible(true);
		
		//Open the display
		//openDisplay();
	}
	
	private ActionListener OpenClose = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			OpenClose();
		}
	};
	
	public void OpenClose() {
		if(!display.isOpen()) {
			display.openDisplay();
			setOpenCloseText("Close");
		}
		else {
			display.closeDisplay();
			setOpenCloseText("Open");
		}
	}
	
	public void toggleDisplay(boolean state) {
		if(state) {
			display.openDisplay();
			setOpenCloseText("Close");
		}
		else {
			display.closeDisplay();
			setOpenCloseText("Open");
		}
	}
	
	public void setOpenCloseText(String str) {
		openCloseButton.setText(str);
	}
	
	private ActionListener StartStop = new ActionListener() {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			StartStop();
		}
	};
	
	public void StartStop() {
		togglePoll(shouldStart, false);
		shouldStart = !shouldStart;
	}
	
	public void togglePoll(boolean state, boolean commandFromWeb) {
		if(state) {
			display.nextQuestion();
			
			usb.startPoll();
			
			if(web.isSessionStarted() && !commandFromWeb) {
				web.createPoll();
				web.activatePoll();
			}
	
			setStartStopText("Stop");
			numResponsesText.setText("0");
		}
		else {
			usb.stopPoll();
			
			if(web.isSessionStarted() && !commandFromWeb) {
				web.deactivatePoll();
			}
			
			setStartStopText("Start");
			numResponsesText.setText("-");
		}
	}
	
	public boolean isPollActive() {
		return !shouldStart;
	}
	
	public void setStartStopText(String str) {
		startStopButton.setText(str);
	}
	
	public void setNumResponsesText(String str) {
		//if(!shouldStart) { // we've already started
			numResponsesText.setText(str);
		//S}
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

