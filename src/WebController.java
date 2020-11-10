import org.apache.http.NameValuePair;
import org.apache.http.entity.*;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
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
import java.io.StringReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;

import purejavahidapi.DeviceRemovalListener;
import purejavahidapi.HidDevice;
import purejavahidapi.HidDeviceInfo;
import purejavahidapi.InputReportListener;
import purejavahidapi.PureJavaHidApi;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;


public class WebController {

	public static final String RSA_PUBLIC = "-----BEGIN RSA PUBLIC KEY-----\nMIGJAoGBAL1zTCmLCknSMnqPiGNo0mj2bQuhBdte/s8rE+EtWp5ZfPjewneoKwjNXd9hM1s2RtHhehka+unOxfyDvMyzRPsrUdeCxmROZ/v7fFtgnSzlgQbqYNIaC62euPuvD5AR7pekPQUYtFgmx14SJrBNz213y9v6GQNfVUOMl0ojqKdbAgMBAAE=\n-----END RSA PUBLIC KEY-----";

	
	public static final String COURSE_NAME = "test";
	public static final String CHARACTERS = "0123456789"
			+ "abcdefghijklmnopqrstuvwxyz"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final int ID_LENGTH = 20;
	
	private static final int WINDOW_WIDTH = 500;
	private static final int WINDOW_HEIGHT = 300;
	private static final String WINDOW_TITLE = "Student Responses";
	
	private int votesA = 0;
	private int votesB = 0;
	private int votesC = 0;
	private int votesD = 0;
	private int votesE = 0;
	private int[] allVotes;
	
	private Display display;
	
	private Firestore db;
	
	private ArrayList<NameValuePair> courses;
	
	private String courseName = "";
	private String courseID = "";
	private String sessionID = "";
	private long sessionNum;
	private long slideCount;
	private String pollID = "";
	
	private String instructorUN = "";
	private String instructorPW = "";
	private String instructorID = "";
	
	private Map<String, Object> courseCategories;

	private boolean courseSelected = false;
	
	List<QueryDocumentSnapshot> users;
	
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
	
	private WebsocketClientEndpoint socket;
	
	public WebController(Display display) {
		this.display = display;
		courses = new ArrayList<NameValuePair>();
		
		Thread t = new Thread(new Runnable() { public void run() {
			BasicConfigurator.configure();
			begin();
		}});
		
		t.start();
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
		usernameBox = new JTextField(instructorUN, 20);
		layout.putConstraint(SpringLayout.WEST, usernameBox, 5, SpringLayout.EAST, usernameLabel);
		layout.putConstraint(SpringLayout.NORTH, usernameBox, 0, SpringLayout.NORTH, usernameLabel);
		
		// add the password
		JLabel passwordLabel = new JLabel("Password: ");
		layout.putConstraint(SpringLayout.WEST, passwordLabel, 6, SpringLayout.WEST, displayFrame);
		layout.putConstraint(SpringLayout.NORTH, passwordLabel, 25, SpringLayout.NORTH, usernameBox);
		passwordBox = new JPasswordField(instructorPW, 20);
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
		
//		// add the lecture number input field
//		JLabel lectureLabel = new JLabel("Lecture Number: ");
//		lectureNumber = new JTextField("0", 9);
//		lectureNumber.addFocusListener(new FocusListener() {
//			@Override 
//			public void focusLost(final FocusEvent pE) {}
//            
//			@Override 
//			public void focusGained(final FocusEvent pE) {
//                lectureNumber.selectAll();
//            }
//		});
//		courseSelectionPanel.add(lectureLabel);
//		courseSelectionPanel.add(lectureNumber);

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
		
		//displayFrame.pack();
		displayFrame.validate();
		displayFrame.setVisible(true);
		
		credController = new CredentialController(usernameBox, passwordBox);
	}
	
	/*
	 * A lot of this code was taken from this tutorial: 
	 * https://www.baeldung.com/httpurlconnection-post
	 */
	private boolean login(String username, String password) {
		try {
			// Establish connection
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpPost httpPost = new HttpPost("http://54.153.95.213:3001/login");
			httpPost.setHeader("Accept", "application/json");
			httpPost.setHeader("Content-type", "application/json");
			
			// Create json packet
			String toSend = "{\"email\": \"" + username + "\", \"password\": \""
					+ getEncrypted(password) + "\"}";
			StringEntity stringEntity = new StringEntity(toSend);
			System.out.println(toSend);
			httpPost.setEntity(stringEntity);
			
			// send the packet
			CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
			
			String response = EntityUtils.toString(httpResponse.getEntity());
			
			System.out.println(response);
		      
			// https://www.tutorialspoint.com/json/json_java_example.htm
			JSONParser parser = new JSONParser();
			
			JSONObject full = (JSONObject) parser.parse(response);
			
			boolean success = (boolean) full.get("success");
			
			if(success) {
				JSONObject data = (JSONObject) full.get("data");
				JSONArray JSONCourses = (JSONArray) data.get("courses");
				
				JSONObject account = (JSONObject) data.get("account");
				instructorID = (String) account.get("accountID");
				
				courseSelector.removeAllItems();
				for(int i = 0; i < JSONCourses.size(); i ++) {
					Object obj = JSONCourses.get(i);
					
					// only look at JSONObjects
					if(!obj.getClass().equals(data.getClass())) {
						break;
					}
					
					JSONObject course = (JSONObject) obj;
					
					String courseName = (String) course.get("courseName");
					String courseID = (String) course.get("courseID");
					
					NameValuePair coursePair = new BasicNameValuePair(courseName, courseID);
					courses.add(coursePair);
					
					courseSelector.addItem(courseName);
					
				}
				
				// show the course selection menu
				CardLayout layout = (CardLayout)displayPanel.getLayout();
				layout.show(displayPanel, "Courses");
			}
			httpClient.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	private ActionListener login = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				// get the credentials from the input boxes
				instructorUN = usernameBox.getText();
				instructorPW = new String(passwordBox.getPassword());
				
				login(instructorUN, instructorPW);
				
			} catch(Exception err) {
				System.out.println("Failed to connect to web.");
				err.printStackTrace();
			}
		}
	};
	
	private ActionListener selectedCourse = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			courseName = (String)courseSelector.getSelectedItem();
			
			if(courseName.contentEquals("Test Course 2")) {
				selectorSetActiveSession(true);
			}
			else {
				selectorSetActiveSession(false);
			}
		}
	};
	
	public void selectorSetActiveSession(boolean isActive) {
		if(isActive) {
			resumeSessionButton.setText("Join Active Session");
//			resumeSessionButton.removeActionListener(resumeSession);
//			resumeSessionButton.addActionListener(resumeSession);

			newSessionButton.setText("End Active Session");
			newSessionButton.removeActionListener(newSession);
			newSessionButton.addActionListener(endActiveSession);
		}
		else {
			resumeSessionButton.setText("Resume Session");
//			resumeSessionButton.removeActionListener(resumeSession);
//			resumeSessionButton.addActionListener(resumeSession);

			newSessionButton.setText("New Session");
			newSessionButton.removeActionListener(endActiveSession);
			newSessionButton.addActionListener(newSession);
		}
	}
	
	private ActionListener endActiveSession = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			selectorSetActiveSession(false);
		}
	};
	
	private ActionListener newSession = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			courseName = (String)courseSelector.getSelectedItem();
//			System.err.println("CourseName: " + courseName);
			
//			System.err.println("num courses: " + courses.size());
			for(NameValuePair course : courses) {
//				System.err.println("CourseName: " + course.getName());
				
				if(course.getName().equals(courseName)) {
					courseID = course.getValue();
					break;
				}
			}
			
			courseSelected = true;
//			System.err.println("CourseID: " + courseID);
			
			createSession();
			activateSession();
			displayFrame.setVisible(false);
		}
	};
	
	private ActionListener resumeSession = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			courseName = (String)courseSelector.getSelectedItem();
//			System.err.println("CourseName: " + courseName);
			
			for(NameValuePair course : courses) {
				if(course.getName().equals(courseName)) {
					courseID = course.getValue();
					break;
				}
			}
			
			courseSelected = true;
//			System.err.println("CourseID: " + courseID);
			
			resumeSession();
			displayFrame.setVisible(false);
		}
	};
	
	public void createPoll() {
		try {
			if (sessionID.contentEquals("")) {
				return;
			}
			
			display.nextQuestion();
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("pollStartTime", "" + System.currentTimeMillis()));
			params.add(new BasicNameValuePair("pollSessionID", "" + sessionID));
			
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost("54.153.95.213")
					.setPort(3001)
					.setPath("/createPoll")
					.addParameters(params)
					.build();
			
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(uri);
			
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			String response = EntityUtils.toString(httpResponse.getEntity());
			
			System.out.println(response);
		      
			// https://www.tutorialspoint.com/json/json_java_example.htm
			JSONParser parser = new JSONParser();
			
			JSONObject arr = (JSONObject)parser.parse(response);
			JSONObject obj = (JSONObject)arr.get("data");
			
			pollID = (String)obj.get("pollID");
			
			System.out.println("Poll ID: " + pollID);
			
			httpClient.close();
			
			resetVotes();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void activatePoll() {
		try {
			if (sessionID.contentEquals("")) {
				return;
			}
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("pollID", pollID));
			
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost("54.153.95.213")
					.setPort(3001)
					.setPath("/activatePoll")
					.addParameters(params)
					.build();
			
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(uri);
			
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			httpClient.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		newResponse("123456", "a");
		newResponse("987654", "a");
	}
	
	public void deactivatePoll() {
		try {
			if (sessionID.contentEquals("")) {
				return;
			}
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("pollID", pollID));
			
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost("54.153.95.213")
					.setPort(3001)
					.setPath("/deactivatePoll")
					.addParameters(params)
					.build();
			
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(uri);
			
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			System.out.println(EntityUtils.toString(httpResponse.getEntity()));
			System.out.println(httpResponse.getStatusLine());
			
			httpClient.close(); 
			
			pollID = "";
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void createSession() {
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();

			System.err.println("CourseID: " + courseID);
			params.add(new BasicNameValuePair("sessionStartTime", "" + System.currentTimeMillis()));
			params.add(new BasicNameValuePair("sessionCourseID", courseID));
			
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost("54.153.95.213")
					.setPort(3001)
					.setPath("/createSession")
					.addParameters(params)
					.build();
			
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(uri);
			
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			String response = EntityUtils.toString(httpResponse.getEntity());
			
			System.out.println(response);
		      
			// https://www.tutorialspoint.com/json/json_java_example.htm
			JSONParser parser = new JSONParser();
			
			JSONObject arr = (JSONObject)parser.parse(response);
			
			if((boolean)arr.get("success")) {
				JSONObject obj = (JSONObject)arr.get("data");
				
				sessionID = (String)obj.get("sessionID");
				sessionNum = (long)obj.get("sessionNum");
				slideCount = (long) 0;
				
				System.out.println(sessionID);
			}
			
			httpClient.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void resumeSession() {
		try {
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("courseID", courseID));
			
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost("54.153.95.213")
					.setPort(3001)
					.setPath("/resumeSession")
					.addParameters(params)
					.build();
			
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(uri);
			
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			String response = EntityUtils.toString(httpResponse.getEntity());

			System.err.println("CourseID: " + courseID);
			System.out.println(response);
		      
			// https://www.tutorialspoint.com/json/json_java_example.htm
			JSONParser parser = new JSONParser();
			
			JSONObject arr = (JSONObject)parser.parse(response);
//			JSONObject obj = (JSONObject)arr.get("data");
			
			sessionID = (String)arr.get("sessionID");
			sessionNum = (long)arr.get("sessionNum");
			slideCount = (long)arr.get("slideCount");
			
			
			System.out.println(sessionID);
			
			httpClient.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void activateSession() {
		try {
			if (sessionID.contentEquals("")) {
				return;
			}
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("sessionID", sessionID));
			
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost("54.153.95.213")
					.setPort(3001)
					.setPath("/activateSession")
					.addParameters(params)
					.build();
			
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(uri);
			
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			System.out.println(EntityUtils.toString(httpResponse.getEntity()));
			System.out.println(httpResponse.getStatusLine());
			
			httpClient.close();
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void deactivateSession() {
		try {
			if (sessionID.contentEquals("")) {
				return;
			}
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("sessionID", sessionID));
			
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost("54.153.95.213")
					.setPort(3001)
					.setPath("/deactivateSession")
					.addParameters(params)
					.build();
			
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(uri);
			
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			System.out.println(EntityUtils.toString(httpResponse.getEntity()));
			System.out.println(httpResponse.getStatusLine());
			
			httpClient.close(); 
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
	
	public int[] getVotes(boolean shouldGetNew) {
		if(!shouldGetNew) {
			return allVotes;
		}
		
		try {
			if (pollID.contentEquals("")) {
				return null;
			}
			
			List<NameValuePair> params = new ArrayList<NameValuePair>();
			
			params.add(new BasicNameValuePair("pollID", pollID));
			
			URI uri = new URIBuilder()
					.setScheme("http")
					.setHost("54.153.95.213")
					.setPort(3001)
					.setPath("/getPollData")
					.addParameters(params)
					.build();
			
			CloseableHttpClient httpClient = HttpClients.createDefault();
			HttpGet httpGet = new HttpGet(uri);
			
			CloseableHttpResponse httpResponse = httpClient.execute(httpGet);
			
			String response = EntityUtils.toString(httpResponse.getEntity());
			
			System.err.println(response);
		      
			// https://www.tutorialspoint.com/json/json_java_example.htm
			JSONParser parser = new JSONParser();
			
			JSONObject obj = (JSONObject)parser.parse(response);
			JSONObject data = (JSONObject)obj.get("data");
			JSONArray arr = (JSONArray)data.get("votes");
			
			allVotes = new int[] {0, 0, 0, 0, 0};
			
			if(arr != null) {
				for(int i = 0; i < arr.size(); i++) {
					JSONObject iObj = (JSONObject)arr.get(i);
					String vote = (String)iObj.get("studentVote");
					System.out.println("Student's vote: " + vote);
					
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
			
			httpClient.close();
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
	public String getEncrypted(String data) {
		String out = "";
		
		try {
			byte[] bytes = data.getBytes();
			
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
			System.out.println("Sending vote to server.");
			
			// Create json packet
			String toSend = "{\"type\":\"vote\", \"iClicker\": \"" + studentID + "\", \"vote\": \""
					+ vote + "\", \"courseID\": \"" + courseID + "\"}";

			if(socket != null) {
				socket.sendMessage(toSend);
			}
			else {
				System.err.println("SOCKET NOT OPENED ON WEBCONTROLLER SIDE!!!!!!!!!");
				StringEntity stringEntity = new StringEntity(toSend);
				
				// Establish connection
				CloseableHttpClient httpClient = HttpClients.createDefault();
				HttpPost httpPost = new HttpPost("http://54.153.95.213:3001/vote");
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");
				httpPost.setEntity(stringEntity);
				
				// send the packet
				CloseableHttpResponse httpResponse = httpClient.execute(httpPost);
				
				String response = EntityUtils.toString(httpResponse.getEntity());
				
				//System.out.println(response);
			      
				// https://www.tutorialspoint.com/json/json_java_example.htm
				JSONParser parser = new JSONParser();
				
				JSONObject full = (JSONObject) parser.parse(response);
				
				boolean success = (boolean) full.get("success");
				
				System.out.println(success);
				
				httpClient.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public void setSocket(WebsocketClientEndpoint socket) {
		this.socket = socket;
	}
}