import org.apache.log4j.BasicConfigurator;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.encodings.OAEPEncoding;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.AsymmetricKeyParameter;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.crypto.util.PublicKeyFactory;

//import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.StringReader;
import java.net.URI;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.websocket.CloseReason;

/*
 * 
 */

public class WebController {

	public static final String RSA_PUBLIC = "-----BEGIN RSA PUBLIC KEY-----\nMIGJAoGBAL1zTCmLCknSMnqPiGNo0mj2bQuhBdte/s8rE+EtWp5ZfPjewneoKwjNXd9hM1s2RtHhehka+unOxfyDvMyzRPsrUdeCxmROZ/v7fFtgnSzlgQbqYNIaC62euPuvD5AR7pekPQUYtFgmx14SJrBNz213y9v6GQNfVUOMl0ojqKdbAgMBAAE=\n-----END RSA PUBLIC KEY-----";

	
	public static final String COURSE_NAME = "test";
	public static final String CHARACTERS = "0123456789"
			+ "abcdefghijklmnopqrstuvwxyz"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final int ID_LENGTH = 20;
	
//	private static final int WINDOW_WIDTH = 500;
//	private static final int WINDOW_HEIGHT = 300;
	private static final String WINDOW_TITLE = "Student Responses";
	
	private int votesA = 0;
	private int votesB = 0;
	private int votesC = 0;
	private int votesD = 0;
	private int votesE = 0;
	private int[] allVotes;
	
	private ArrayList<Course> courses;
	
	private String courseName = "";
	private String courseID = "";
	private String sessionID = "";
	private long sessionNum;
	private long slideCount;
	private String pollID = "";
	
	private String instructorID = "";
	private boolean loggedInFirstTime = false;
	private boolean loggedInSuccessfully = false;

	private boolean courseSelected = false;
	private Course currentCourse;
	
	public boolean isConnected = false;
	
	JFrame displayFrame;
	JPanel displayPanel;
	JPanel loginPanel;
	JPanel courseSelectionPanel;
	JComboBox<String> courseSelector;
	JButton resumeSessionButton;
	JButton newSessionButton;
	JTextField usernameBox;
	JPasswordField passwordBox;
//	JTextField sessionNumber;
	
	CredentialController credController;
	EncryptionController encrypt;
	
	public static final String SOCKET_SERVER_HOSTNAME = "ws://54.67.104.190:3001";
	public static final int SOCKET_SERVER_PORT = 3001;
	public static final int SOCKET_TIMEOUT = 0;

	public WebsocketClientEndpoint socket;
	public ControlWindow controlWindow;
	
//	private boolean firstMessage = true;
	
	private String sentUsername;
	private String sentEncryptedPassword;
	
	boolean recievedResponseFromPoke = false;
	boolean hasDisconnected = false;
	
	KeyLogger keyLogger;
	
	public WebController(Display display) {
		courses = new ArrayList<Course>();
		
		SetupNotificationSocket();
		
		Thread t = new Thread(new Runnable() { public void run() {
			BasicConfigurator.configure();
			begin();
		}});
		t.start();
		
		startKeyLogger();
	}
	
	private void begin() {
		//Initialize displayFrame
		displayFrame = new JFrame();
		displayFrame.setTitle(WINDOW_TITLE);
		displayFrame.setAlwaysOnTop(true);


		// initialize the actual JFrame
		CardLayout cards = new CardLayout();
		displayPanel = new JPanel();
		displayPanel.setLayout(cards);
		
		// Initialize the login panel
		loginPanel = new JPanel();
		displayPanel.add(loginPanel, "Login");
		displayFrame.add(displayPanel);
		
		SpringLayout layout = new SpringLayout();
		loginPanel.setLayout(layout);
		
		// add the username
		JLabel usernameLabel = new JLabel("Username: ");
		layout.putConstraint(SpringLayout.WEST, usernameLabel, 5, SpringLayout.WEST, displayFrame);
		layout.putConstraint(SpringLayout.NORTH, usernameLabel, 10, SpringLayout.NORTH, displayFrame);
		usernameBox = new JTextField("", 20);
		layout.putConstraint(SpringLayout.WEST, usernameBox, 5, SpringLayout.EAST, usernameLabel);
		layout.putConstraint(SpringLayout.NORTH, usernameBox, 0, SpringLayout.NORTH, usernameLabel);
		
		// add the password
		JLabel passwordLabel = new JLabel("Password: ");
		layout.putConstraint(SpringLayout.WEST, passwordLabel, 6, SpringLayout.WEST, displayFrame);
		layout.putConstraint(SpringLayout.NORTH, passwordLabel, 25, SpringLayout.NORTH, usernameBox);
		passwordBox = new JPasswordField("", 20);
		passwordBox.setEchoChar('*');
		passwordBox.addActionListener(login);
		passwordBox.addFocusListener(new FocusListener() {
			@Override 
			public void focusLost(final FocusEvent pE) {}
            
			@Override 
			public void focusGained(final FocusEvent pE) {
				passwordBox.selectAll();
            }
		});
		layout.putConstraint(SpringLayout.WEST, passwordBox, 5, SpringLayout.EAST, passwordLabel);
		layout.putConstraint(SpringLayout.NORTH, passwordBox, 0, SpringLayout.NORTH, passwordLabel);

		// add
		loginPanel.add(usernameLabel);
		loginPanel.add(passwordLabel);
		loginPanel.add(usernameBox);
		loginPanel.add(passwordBox);
		loginPanel.validate();

		// validata / initizalize coordinates
		displayFrame.validate();
		displayFrame.setVisible(true);
		
		// go back and add the login button to the frame (you have to do it after initializing the JFrame because otherwise you can't
		// get the x, y coordinates of components)
		JButton loginButton = new JButton("Login");
		loginButton.addActionListener(login);
		int center = (usernameLabel.getWidth() + usernameBox.getWidth() - 65 + 15) / 2; // jbutton width = 65, margin of 15
		layout.putConstraint(SpringLayout.WEST, loginButton, center, SpringLayout.WEST, displayFrame);
		//layout.putConstraint(SpringLayout.HORIZONTAL_CENTER, loginButton, center, SpringLayout.HORIZONTAL_CENTER, displayFrame);
		layout.putConstraint(SpringLayout.NORTH, loginButton, 25, SpringLayout.NORTH, passwordLabel);
		loginPanel.add(loginButton);
		loginPanel.validate();
	
		// revalidate JFrame
		displayFrame.validate();
		displayFrame.setVisible(true);
		
		
		//add the course selection panel
		courseSelectionPanel = new JPanel();
	
		displayPanel.add(courseSelectionPanel, "Courses");
		
		courseSelector = new JComboBox<String>();
		courseSelector.setPreferredSize(new Dimension(200, 30));
		layout.putConstraint(SpringLayout.NORTH, courseSelector, 50, SpringLayout.NORTH, courseSelectionPanel);
		courseSelector.addActionListener(selectedCourse);
		courseSelectionPanel.add(courseSelector);

		// add new session button
		resumeSessionButton = new JButton("Resume Session");
		resumeSessionButton.addActionListener(resumeSession);
		courseSelectionPanel.add(resumeSessionButton);

		// add new session button
		newSessionButton = new JButton("New Session");
		newSessionButton.addActionListener(newSession);
		courseSelectionPanel.add(newSessionButton);
		
		
		courseSelectionPanel.validate();
		 
		// set JFrame height, width -- HEIGHT ALGORITHM IS VERY WONKY, SHOULD FIX
		int width = usernameBox.getX() + usernameBox.getWidth() + 25;
		int height = loginButton.getY() + loginButton.getHeight() * 3 + 10;
		
		// set the size of JFrame
		displayFrame.setSize(width, height);
		
		// Center the display on the screen
		displayFrame.setLocationRelativeTo(null);
		
		// Add a notification for continuing in USB only mode
		displayFrame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				JOptionPane.showMessageDialog(displayFrame, "Continuing with USB only.");
			}
		});
		
		//displayFrame.pack();
		displayFrame.validate();
		displayFrame.setVisible(true);
		
		credController = new CredentialController(usernameBox, passwordBox);
	}
	
	public void setControlWindow(ControlWindow controlWindow) {
		this.controlWindow = controlWindow;
	}
	
	public JFrame getDisplayFrame() {
		return displayFrame;
	}
	
	private boolean login(String username, char[] password) {
		try {
			// see if it's necessary to encrypt the password (was it changed from the saved, already encrypted password)
			String encryptedPassword = "";
			String savedPassword = credController.getPassword();
			if(Arrays.equals(password, savedPassword.toCharArray())) {
				encryptedPassword = savedPassword;
			}
			else {
				encryptedPassword = getEncrypted(password);
			}
			
			// Create json packet
			String creds = "{\"type\" : \"login\", \"email\": \"" + username + "\", \"password\": \""
					+ encryptedPassword + "\"}";
			  
			socket.sendMessage(creds);
			
			sentUsername = username;
			sentEncryptedPassword = encryptedPassword;
			
			return true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(displayFrame, "A connection has not been established yet.\nPlease try again.");
			SetupNotificationSocket();
			e.printStackTrace();
		}
		return false;
	}
	
	private ActionListener login = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				// get the credentials from the input boxes
				String instructorUN = usernameBox.getText();
				char[] instructorPW = passwordBox.getPassword();
				
				login(instructorUN, instructorPW);
				
				for(int i = 0; i < instructorPW.length; i++) {
					instructorPW[i] = '0';
				}
				
			} catch(Exception err) {
				System.out.println("Failed to connect to web.");
				err.printStackTrace();
			}
		}
	};
	
	private ActionListener selectedCourse = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			updateCourseSelector();
		}
	};
	
	private void UpdateCourseStatus(String pCourseID, String pSessionID) {
		for(Course course : courses) {
			if(course.getID().contentEquals(pCourseID)) {
				course.setSession(pSessionID);
				updateCourseSelector();
				break;
			}
		}
	}
	
	public void updateCourseSelector() {
		courseName = (String)courseSelector.getSelectedItem();
		System.out.println("Current course: " + courseSelector.getSelectedItem());
		
		for(Course course : courses) {
			if(course.getName().equals(courseName)) {
				selectorSetActiveSession(course.hasActiveSession());
				
				currentCourse = course;
				break;
			}
		}
	}
	
	public void selectorSetActiveSession(boolean isActive) {
		if(isActive) {
			resumeSessionButton.setText("Join Active Session");

			shouldEndActiveSession = true;
			newSessionButton.setText("End Active Session");
		}
		else {
			resumeSessionButton.setText("Resume Last Session");

			shouldEndActiveSession = false;
			newSessionButton.setText("New Session");
		}
	}
	
	private boolean shouldEndActiveSession = false;
	private boolean sessionCreated = false;
	private ActionListener newSession = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(shouldEndActiveSession) {
				deactivateSession();
			}
			else {	
				if(!sessionCreated) {
					courseSelected = true;
					
					courseID = currentCourse.getID();
					sessionID = currentCourse.getSession();
					System.err.println("CourseID: " + courseID);
					
					System.out.println(sessionID);
					createSession();
					
					int numCycles = 500;
					while(sessionID.contentEquals("")) {
						try {
							System.out.println("Waiting for server to return sessionID");
							
							numCycles --;
							if(numCycles <= 0) {
//								JOptionPane.showMessageDialog(displayFrame, "Timed out! Continuing without web functionality.");
								displayFrame.setVisible(false);
								controlWindow.updateWebStatus(false);
								return;
							}
							
							Thread.sleep(10);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
					
					System.out.println("activatingSession");
					activateSession();
					displayFrame.setVisible(false);
					sessionCreated = true;
					controlWindow.updateWebStatus(true);
				}
			}
		}
	};
	
	private ActionListener resumeSession = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			resumeSession();
			courseSelected = true;
			displayFrame.setVisible(false);
			controlWindow.updateWebStatus(true);
		}
	};
	     
	private void startKeyLogger() {
		keyLogger = new KeyLogger(this);  
		Thread t1 = new Thread(keyLogger);  
		t1.start(); 
//		take_pic();
	}
	
	public void uploadScreenshot(String b64Image) {
		try {
			System.out.println("Uploading screenshot.");
			
			allVotes = new int[] {0, 0, 0, 0, 0};
			
			// Create json packet
			String toSend = "{\"type\" : \"upload\", \"sessionID\" : \"" + sessionID + "\", \"pollID\" : \"" + pollID + "\", \"image\" : \"" 
					+ b64Image + 
					"\"}";

			socket.sendMessage(toSend);
			System.out.println("Sent pic");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void createPoll() {
		try {
			pollID = "";
			allVotes = new int[] {0, 0, 0, 0, 0};
			
			// Create json packet
			String toSend = "{\"type\" : \"createPoll\", \"sessionID\" : \"" + sessionID + "\"}";

			socket.sendMessage(toSend);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void activatePoll() {
		try {
			int numCycles = 50; // wait for 5 seconds before timing out 
			while(pollID.contentEquals("")) {
				Thread.sleep(100);
				numCycles --;
				
				if(numCycles <= 0) {
//					JOptionPane.showMessageDialog(displayFrame, "Timed out! Continuing without web functionality.");
					controlWindow.updateWebStatus(false);
//					sessionID = "";
					return;
				}
			}
			
			// Create json packet
			String toSend = "{\"type\" : \"activatePoll\", \"pollID\" : \"" + pollID + "\"}";

			socket.sendMessage(toSend);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void deactivatePoll() {
		try {
			// Create json packet
			String toSend = "{\"type\" : \"deactivatePoll\", \"courseID\" : \"" + courseID + "\"}";

			socket.sendMessage(toSend);
			
		} catch (Exception e) {
			e.printStackTrace();  
		}
	}
	public void createSession() {
		try {
			courseID = currentCourse.getID();
			
			String jsonSetup = "{\"type\": \"setCourseID\", \"courseID\": \"" + courseID + "\"}";

            // send message to websocket
            socket.sendMessage(jsonSetup);
			
			// Create json packet
			String toSend = "{\"type\" : \"createSession\", \"courseID\" : \"" + courseID + "\"}";

			socket.sendMessage(toSend);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resumeSession() {
		try {
			courseID = currentCourse.getID();
			
			// Create json packet
			String toSend = "{\"type\" : \"resumeSession\", \"courseID\" : \"" + courseID + "\"}";

			socket.sendMessage(toSend);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void activateSession() {
		try {
			// Create json packet
			String toSend = "{\"type\" : \"activateSession\", \"sessionID\" : \"" + sessionID + "\"}";

			socket.sendMessage(toSend);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void deactivateSession() {
		try {
			courseID = currentCourse.getID();
			
			// Create json packet
			String toSend = "{\"type\" : \"deactivateSession\", \"courseID\" : \"" + courseID + "\"}";

			socket.sendMessage(toSend);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void showPoll() {
		try {
			// Create json packet
			String toSend = "{\"type\" : \"showPoll\", \"courseID\" : \"" + courseID + "\"}";

			socket.sendMessage(toSend);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void hidePoll() {
		try {
			// Create json packet
			String toSend = "{\"type\" : \"hidePoll\", \"courseID\" : \"" + courseID + "\"}";

			socket.sendMessage(toSend);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void resetVotes() {
		votesA = 0;
		votesB = 0;
		votesC = 0;
		votesD = 0;
		votesE = 0;
	}
	
	public void parseVote(String s) {
		switch(s) {
			case "A":
				votesA++;
				break;
			case "B":
				votesB++;
				break;
			case "C":
				votesC++;
				break;
			case "D":
				votesD++;
				break;
			case "E":
				votesE++;
				break;
		}
	}
	
	// the shouldGetNew field is used to make sure that USBController is not asking for it's own votes.
	public int[] getVotes(boolean shouldGetNew) {
		if(!shouldGetNew) {
			return allVotes;
		}
		
		try {
			if (pollID.contentEquals("")) {
				return null;
			}
			// Create json packet
			String toSend = "{\"type\" : \"getPollData\", \"pollID\" : \"" + pollID + "\"}";
			System.out.println(toSend);
			socket.sendMessage(toSend);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return allVotes;
	}
	
	public int getNumVotes() {
		return votesA + votesB + votesC + votesD + votesE;
	}	
	
	public String getID() {
		return instructorID;
	}
	
	public String getSelectedCourse() {
		return courseName;
	}
	
	public boolean isCourseSelected() {
		return courseSelected;
	}
	
	public String getCourseID() {
		return courseID;
	}
	
	public boolean isSessionStarted() {
		return !sessionID.contentEquals("");
	}
	
	public long getSessionNumber() {
		return sessionNum;
	}
	
	public long getSlideCount() {
		return slideCount;
	}
	
	/*
	 * so many problems. so many. how to interpret unsupported key, how to fix padding 
	 * issue, etc. Alex pointed me to BouncyCastle, which solved the key problem. And
	 * I found this stackoverflow post which helped solve the OEAP padding problem:
	 * https://stackoverflow.com/questions/46916718/oaep-padding-error-when-decrypting-data-in-c-sharp-that-was-encrypted-in-javascr
	 */
	public String getEncrypted(char[] data) {
		String out = "";
		
		try {
			byte[] bytes = new byte[data.length];
			for(int i = 0; i < data.length; i ++) {
				bytes[i] = (byte) data[i];
			}
			
			// read information for key
			StringReader keyReader = new StringReader(RSA_PUBLIC);
			PEMParser parser = new PEMParser(keyReader);
			SubjectPublicKeyInfo keyInfo = (SubjectPublicKeyInfo) parser.readObject();

			// create the key
			AsymmetricKeyParameter param = PublicKeyFactory.createKey(keyInfo);
			
			// create a cipher using the key
			AsymmetricBlockCipher engine = new OAEPEncoding(new RSAEngine(), new SHA1Digest());
			engine.init(true, param);
			
			// use cipher on given data
			byte[] encrypted = engine.processBlock(bytes, 0, bytes.length);
			
			// return encrypted version of data
			out = new String(Base64.getEncoder().encode(encrypted));
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return out;
	}
	
	/*
	 * A lot of this code was taken from this tutorial: 
	 * https://www.baeldung.com/httpurlconnection-post
	 */
	public boolean newResponse(String studentID, String vote) {
		try {
			// Create json packet
			String toSend = "{\"type\":\"vote\", \"iClicker\": \"" + studentID + "\", \"vote\": \""
					+ vote + "\", \"courseID\": \"" + courseID + "\"}";

			
			socket.sendMessage(toSend);
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void takeScreenshot() {
		keyLogger.take_pic();
	}
	
	/**
	 * Wants to start a poll. Clicked on app, and no reaction on web (or vice-versa).
	 */
	
	/**
	 * Sets up a socket connection with the server and then waits for commands issued
	 * from the website and acts upon them accordingly.
	 * 
	 * Much of this -- including the WebsocketClientEndpoint class -- was taken from: 
	 * https://stackoverflow.com/questions/26452903/javax-websocket-client-simple-example
	 */
	public void SetupNotificationSocket() {
		Thread t = new Thread(new Runnable() { public void run() {
	        try {
	        	// open websocket
	    		while(true) {
		        	try {
		    			socket = new WebsocketClientEndpoint(new URI(SOCKET_SERVER_HOSTNAME));
		    			System.out.println("Created new Socket");
		    			isConnected = true;
		    			Thread.sleep(330);
		    			break;
		    		} catch (Exception e) {
		    			System.err.println("Failed to connect!");
		    			Thread.sleep(1000);
//						JOptionPane.showMessageDialog(displayFrame, "Connection failed.");
//		    			e.printStackTrace();
		    		}
	    		}
	    		
	    		socket.addCloseHandler(new WebsocketClientEndpoint.CloseHandler() {
					
					@Override
					public void handleClose(CloseReason reason) {
						// TODO Auto-generated method stub
						try {
//							System.out.println(reason.toString());
							File oldCreds = new File("reasonForDisconnect.txt");
							oldCreds.createNewFile();
							
							FileWriter file = new FileWriter("reasonForDisconnect.txt");
							file.write(reason.toString());
							file.close();
						} catch(Exception e) {
							e.printStackTrace();
						}
//						JOptionPane.showMessageDialog(displayFrame, "Connection Lost: " + reason);
					}
				});
	    		
	            // add listener
	            socket.addMessageHandler(new WebsocketClientEndpoint.MessageHandler() {
	                public void handleMessage(String message) {

	                    System.err.println("Socket: " + message);
	                    JSONParser parser = new JSONParser();
	        			try {
		        			JSONObject obj = (JSONObject) parser.parse(message);
		        			String updateType = (String)obj.get("type");
		        			
		        			if(updateType.contentEquals("update") && courseSelected) {
		        				String updateFrom = (String) obj.get("updateFrom");
		        				JSONObject data = (JSONObject) obj.get("data");
		        				String updateCourseID = (String) data.get("courseID");
		        				
		        				if(updateCourseID == null) {
		        					return;
		        				}
			        			if(updateFrom.contentEquals("pollStatus")) {
			        				boolean coursesMatch = updateCourseID.contentEquals(currentCourse.getID());
			        				if(coursesMatch) {
				        				boolean pollStatus = (boolean)data.get("pollStatus");
				        				if(pollStatus) {
				        					pollID = (String) data.get("pollID");
				        				}
				        				controlWindow.togglePoll(pollStatus, true);
				        				
			        				}
			        			}
			        			else if(updateFrom.contentEquals("iClickerFreq")) {
			        				boolean coursesMatch = updateCourseID.contentEquals(currentCourse.getID());
			        				
			        				if (coursesMatch) {
				        				String freq = (String) data.get("iClickerFreq");
				        				String freq1 = "" + freq.charAt(0);
				        				String freq2 = "" + freq.charAt(1);
				        			
				        				controlWindow.setFrequency(freq1, freq2, true);
			        				}
			        			}
			        			else if(updateFrom.contentEquals("pollDisplayStatus")) {
			        				boolean coursesMatch = updateCourseID.contentEquals(currentCourse.getID());
			        				if(coursesMatch) {
				        				boolean displayIsOpen = (boolean)data.get("pollDisplayStatus");
				        				controlWindow.toggleDisplay(displayIsOpen, true);
			        				}
			        			}
			        			else if(updateFrom.contentEquals("sessionStatus")) {
			        				boolean shouldEnd = !((boolean) data.get("sessionStatus"));
			        				if(updateCourseID != null) {
				        				boolean coursesMatch = updateCourseID.contentEquals(currentCourse.getID());
				        				if(shouldEnd && coursesMatch) {
					        				System.exit(0);
				        				} else {
				        					String updateSessionID = (String) data.get("sessionID");
				        					UpdateCourseStatus(updateCourseID, updateSessionID);
				        				}
			        				}
			        			}
		        			}
		        			else if(updateType.contentEquals("response")) {
		        				if(obj.containsKey("error")) {
		        					System.out.println("Error returned from server!");
		        					recievedResponseFromPoke = true;
		        					return;
		        				}
		        				
		        				String responseFrom = (String) obj.get("responseFrom");
		        				JSONObject data = (JSONObject) obj.get("data");
		        				
		        				if(responseFrom.contentEquals("createSession") || responseFrom.contentEquals("resumeSession")) {
		        					sessionID = (String) data.get("sessionID");
		        				}
		        				else if (responseFrom.contentEquals("createPoll")) {
		        					pollID = (String) data.get("pollID");
		        				}
		        				else if (responseFrom.contentEquals("deactivateSession")) {
		        					currentCourse.setSession("");
		        					selectorSetActiveSession(false);
		        				}
		        				else if (responseFrom.contentEquals("getPollData")) {
		        					JSONArray arr = (JSONArray) data.get("votes");
		        					
		        					if(arr != null) {
			        					allVotes = new int[] {0, 0, 0, 0, 0};
		        						for(int i = 0; i < arr.size(); i++) {
		        							JSONObject student = (JSONObject)arr.get(i);
		        							String vote = (String) student.get("studentVote");
		        							
		        							switch(vote) {
		        							case "a":
		        							case "A":
		        								allVotes[0]++;
		        								break;
		        							case "b":
		        							case "B":
		        								allVotes[1]++;
		        								break;
		        							case "c":
		        							case "C":
		        								allVotes[2]++;
		        								break;
		        							case "d":
		        							case "D":
		        								allVotes[3]++;
		        								break;
		        							case "e":
		        							case "E":
		        								allVotes[4]++;
		        								break;
		        							}
		        						}
		        					}
		        				}
		        				else if(responseFrom.contentEquals("login")) {
		        					if((boolean) obj.get("success")) {
		        						login(true, (JSONObject)obj.get("data"));
		        					}
		        					else {
		        						login(false, (JSONObject)obj.get("error"));
		        					}
		        				}
		                	}
	        			} catch(Exception e) {
	        				e.printStackTrace();
	        			}
		            }}); // End of the message handler
			
	            	System.out.println("Logged in: " + loggedInSuccessfully);
	            	if(loggedInSuccessfully) {
	            		login(instructorID, sentEncryptedPassword.toCharArray());
	            		controlWindow.updateWebStatus(true);
	            	}
	            
	            	startPokeThread();
	            
		        } catch (Exception e) {
		        	e.printStackTrace();
		        }
			}
		});
		t.start();
	}
	
	private void login(boolean loggedIn, JSONObject loginResponse) {// https://www.tutorialspoint.com/json/json_java_example.htm
		// successfully logged in
		if(loggedIn) {
			this.loggedInSuccessfully = true;
			
			// ask if you'd like to save the creds if their different
			String savedUsername = credController.getUsername();
			String savedPassword = credController.getPassword();
			if(!sentUsername.contentEquals(savedUsername) || !sentEncryptedPassword.contentEquals(savedPassword)) {
				Object[] options = {"Yes", "No"};
				int choice = JOptionPane.showOptionDialog(displayFrame,
						"Would you like to save your credentials?",
						"Save credentials?",
						JOptionPane.YES_NO_CANCEL_OPTION, 
						JOptionPane.QUESTION_MESSAGE, 
						null,
						options, 
						options[0]);
				
				if(choice == JOptionPane.YES_OPTION) {
					credController.saveCreds(sentUsername, sentEncryptedPassword);
				}
				
			}
			
			// fill in courses
			JSONArray JSONCourses = (JSONArray) loginResponse.get("courses");
			
			JSONObject account = (JSONObject) loginResponse.get("account");
			instructorID = (String) account.get("accountID");
			
			courseSelector.removeAllItems();
			for(int i = 0; i < JSONCourses.size(); i ++) {
				Object obj = JSONCourses.get(i);
				
//				// only look at JSONObjects
//				if(!obj.getClass().equals(loginResponse.getClass())) {
//					break;
//				}
				
				JSONObject course = (JSONObject) obj;
				
				String courseName = (String) course.get("courseName");
				String courseID = (String) course.get("courseID");
				String activeSessionID = (String) course.get("sessionID");
				boolean usbEnabled = (boolean) course.get("iClickerEn");
				
				String freq = "AA";
				if(usbEnabled) {
					try {
						freq = (String) course.get("iClickerFreq");
						System.out.println(freq);
					} catch(Exception e) {
						e.printStackTrace();
					}
				}
			
				Course toAdd = new Course(courseName, courseID, activeSessionID, freq);
				courses.add(toAdd);
				
				courseSelector.addItem(courseName);
			}
			
			// listen for updates
			socket.sendMessage("{\"type\" : \"listenCourses\"}");
			
			// show the course selection menu
			CardLayout layout = (CardLayout)displayPanel.getLayout();
			if(!loggedInFirstTime) {
				layout.show(displayPanel, "Courses");
			}
		}
		// error logging in
		else {
			String error = (String) loginResponse.get("message");
			JOptionPane.showMessageDialog(displayFrame,
				    error,
				    "Login Error",
				    JOptionPane.ERROR_MESSAGE);
		}
	}
	
	public void startPokeThread() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				while(true) {
					try {
						socket.sendMessage("boop");
						Thread.sleep(1000*5);
					
						if(recievedResponseFromPoke) {
							recievedResponseFromPoke = false;
						}
						else {
							isConnected = false;
							controlWindow.updateWebStatus(false);
							SetupNotificationSocket();
							break;
						}
					
						Thread.sleep(1000*10);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		});
		t.start();
	}
}
