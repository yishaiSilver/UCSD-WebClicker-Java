import org.apache.log4j.BasicConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.google.api.core.ApiFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.DocumentChange;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.EventListener;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreException;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.SetOptions;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.annotations.Nullable;

import java.util.ArrayList;
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

import sun.misc.Unsafe;

public class WebController {

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
	
	private String instructorID = "";
	private String instructorPW = "";
	
	private String courseID = "";
	private String sessionID = "";
	private String pollID = "";
	
	private Map<String, Object> courseCategories;

	List<QueryDocumentSnapshot> users;
	
	JFrame displayFrame;
	JPanel displayPanel;
	JPanel loginPanel;
	JPanel courseSelectionPanel;
	JComboBox<String> courseSelector;
	JTextField usernameBox;
	JPasswordField passwordBox;
	
	CredentialController credController;
	EncryptionController encrypt;
	
	public WebController(Display display) {
		this.display = display;
		
		Thread t = new Thread(new Runnable() { public void run() { 
			BasicConfigurator.configure(); // for web
		
			boolean success = false;
			
			try {
				String file = "assets/serviceAccount.json";
				//InputStream serviceAccount = this.getClass().getClassLoader().getResourceAsStream(file);
				InputStream serviceAccount = new FileInputStream("assets/serviceAccount.json"); // ### NEED TO CHANGE ###
				GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
				FirebaseOptions options = new FirebaseOptions.Builder()
				    .setCredentials(credentials)
				    .build();
				FirebaseApp.initializeApp(options);
				db = FirestoreClient.getFirestore();
				
				ApiFuture<QuerySnapshot> query = db.collection("accounts").get();
				QuerySnapshot querySnapshot = query.get();
				users = querySnapshot.getDocuments();
				encrypt = new EncryptionController(users);
				success = true;
			} catch(Exception e) {
				JOptionPane.showMessageDialog(displayFrame, "Failed to connect to web.");
				e.printStackTrace();
			}
			try {
				Thread.sleep(500);
			} catch(Exception e) {
				e.printStackTrace();
			}
				
			if(success) {
				begin();
			}
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
		layout.putConstraint(SpringLayout.NORTH, usernameLabel, 5, SpringLayout.NORTH, displayFrame);
		usernameBox = new JTextField(instructorID, 20);
		layout.putConstraint(SpringLayout.WEST, usernameBox, 5, SpringLayout.EAST, usernameLabel);
		layout.putConstraint(SpringLayout.NORTH, usernameBox, 0, SpringLayout.NORTH, usernameLabel);
		
		// add the password
		JLabel passwordLabel = new JLabel("Password: ");
		layout.putConstraint(SpringLayout.WEST, passwordLabel, 6, SpringLayout.WEST, displayFrame);
		layout.putConstraint(SpringLayout.NORTH, passwordLabel, 25, SpringLayout.NORTH, usernameBox);
		passwordBox = new JPasswordField(instructorPW, 20);
		passwordBox.setEchoChar('*');
		passwordBox.addActionListener(login);
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
		
		courseSelector = new JComboBox();
		courseSelector.setPreferredSize(new Dimension(200, 30));
		//layout.putConstraint(SpringLayout.NORTH, courseSelector, 125, SpringLayout.NORTH, displayPanel);
		courseSelectionPanel.add(courseSelector);
		
		JButton newSession = new JButton("New Session");
		newSession.addActionListener(chooseCourse);
		courseSelectionPanel.add(newSession);
		
		
		courseSelectionPanel.validate();
		
		
		// set JFrame height, width -- HEIGHT ALGORITHM IS VERY WONKY, SHOULD FIX
		int width = usernameBox.getX() + usernameBox.getWidth() + 25;
		int height = loginButton.getY() + loginButton.getHeight() * 3 + 5;
		
		// set the size of JFrame
		displayFrame.setSize(width, height);
		
		// Center the display on the screen
		displayFrame.setLocationRelativeTo(null);
		
		//displayFrame.pack();
		displayFrame.validate();
		displayFrame.setVisible(true);
		
		credController = new CredentialController(usernameBox, passwordBox);
	}
	
	private ActionListener login = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			if(users == null) {
				System.err.println("Not connected!");
				//return;
			}
			
			try {
				// get the credentials from the input boxes
				instructorID = usernameBox.getText();
				instructorPW = new String(passwordBox.getPassword());
				
				QueryDocumentSnapshot user = encrypt.authenticateUser(instructorID, instructorPW);
					
				if(user != null) {
					// get the document id, get all the courses that have our instructor as the instructor
					String ourGuy = user.getId();
					ApiFuture<QuerySnapshot> query = db.collection("courses").whereEqualTo("courseInstructorID", ourGuy).get();
					QuerySnapshot querySnapshot = query.get();
					List<QueryDocumentSnapshot> courses = querySnapshot.getDocuments();
					
					// reset the course drop-down menu
					courseSelector.removeAllItems();
					for(QueryDocumentSnapshot course : courses) {
						courseSelector.addItem(course.getString("courseName"));
					}
					
					System.err.println("USER ID: " + instructorID);
					System.err.println("USER PASS: " + encrypt.getEncryptedPassword());
					
					credController.saveCreds(usernameBox.getText(), encrypt.getEncryptedPassword());
					
					// show the course selection menu
					CardLayout layout = (CardLayout)displayPanel.getLayout();
					layout.show(displayPanel, "Courses");
				}
				else {
					// if there was no user found, display a dialog box saying so
					JOptionPane.showMessageDialog(displayFrame, "No matching user found.");
				}
			} catch(Exception err) {
				System.out.println("Failed to connect to web.");
				err.printStackTrace();
			}
		}
	};
	
	private ActionListener chooseCourse = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				String courseName = (String)courseSelector.getSelectedItem();
				
				ApiFuture<QuerySnapshot> query = db.collection("courses").get();
				QuerySnapshot querySnapshot = query.get();
				List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
				
				for (QueryDocumentSnapshot document : documents) {
					System.err.println(document.getId());
					if(document.getId() != null &&
							document.getString("courseName").contentEquals(courseName)) {
						
						courseID = document.getId();
						pollID = document.getString("courseActivityPollID");
						courseCategories = (HashMap<String, Object>)document.get("courseCategories");

						displayFrame.setVisible(false);
						newSession();
						break;
					}
				}
				
			} catch(Exception err) {
				System.out.println("Failed to connect to web.");
				err.printStackTrace();
			}
		}
	};
	
	public boolean newSession() {
		if(!courseID.contentEquals("")) {
			try {
				
				sessionID = getID();
				
				Map<String, Object> docData = new HashMap<>();
				docData.put("sessionCourseID", courseID);
				docData.put("sessionStartTime", System.currentTimeMillis());
				ApiFuture<WriteResult> future = db.collection("sessions").document(sessionID).set(docData);
				
				ApiFuture<QuerySnapshot> query = db.collection("sessions").get();
				QuerySnapshot querySnapshot = query.get();
				List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
				
				docData = new HashMap<>();
				docData.put("courseActivitySessionID", sessionID);
				future = db.collection("courses").document(courseID).set(docData, SetOptions.merge());
				
				System.err.println("SESSION STARTED");
				
				return true;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public void startPoll() {
		display.nextQuestion();
		
		if(!courseID.contentEquals("")) {
			try {
				pollID = getID();
				
				Map<String, Object> docData = new HashMap<>();
				docData.put("pollCategories", courseCategories);
				docData.put("pollSessionID", sessionID);
				docData.put("pollStartTime", System.currentTimeMillis());
				ApiFuture<WriteResult> future = db.collection("polls").document(pollID).set(docData);
				
				docData = new HashMap<>();
				docData.put("courseActivityPollID", pollID);
				future = db.collection("courses").document(courseID).set(docData, SetOptions.merge());
				
				System.err.println(pollID);
				db.collection("polls").document(pollID).collection("students").addSnapshotListener(
						new EventListener<QuerySnapshot>() {
							@Override
							public void onEvent(QuerySnapshot snapshot, FirestoreException error) {
								if(error != null) {
									System.err.println("Listen failed! " + error);
									return;
								}
								
								if(snapshot != null && !snapshot.getDocumentChanges().isEmpty()) {
									System.err.println(snapshot.getDocumentChanges());
									List<DocumentChange> changes = snapshot.getDocumentChanges();
									for(DocumentChange change : changes) {
										QueryDocumentSnapshot doc = change.getDocument();
										
										String vote = doc.getString("vote");
										display.newResponse(doc.getId(), vote);
										//parseVote((String)doc.get("vote"));
									}
								}
							}
						});
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		resetVotes();
	}
	
	public void stopPoll() {
		if(!courseID.contentEquals("")) {
			try {
				pollID = "";
				
				DocumentReference docRef = db.collection("courses").document(courseID);
				Map<String, Object> data = new HashMap<>();
				data.put("courseActivityPollID", pollID);
				ApiFuture<WriteResult> result = docRef.set(data, SetOptions.merge());
				
			} catch (Exception e) {
				System.out.println("Error stopping poll through web");
				//e.printStackTrace();
			}
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
	
	public int[] getVotes() {
		if(!courseID.contentEquals("")) {
			try {
				ApiFuture<QuerySnapshot> query = db.collection("poll1").get();
				QuerySnapshot querySnapshot = query.get();
				List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
				
				for (QueryDocumentSnapshot document : documents) {
					switch(document.getString("vote")) {
						case "a":
							votesA++;
							break;
						case "b":
							votesB++;
							break;
						case "c":
							votesC++;
							break;
						case "d":
							votesD++;
							break;
						case "e":
							votesE++;
							break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		allVotes = new int[] {votesA, votesB, votesC, votesD, votesE};
		return allVotes;
	}
	
	public int getNumVotes() {
		return votesA + votesB + votesC + votesD + votesE;
	}	
	
	public String getID() {
		String id = "";
		for(int i = 0; i < ID_LENGTH; i ++) {
			Random rand = new Random();
			int index = rand.nextInt(CHARACTERS.length());
			id = id + CHARACTERS.charAt(index);
		}
		System.err.println(id);
		return id;
	}
	
	public boolean isConnected() {
		return db != null;
	}
	
	public void newVote(String id) {
		Map<String, Object> docData = new HashMap<>();
		docData.put("pollCategories", courseCategories);
		docData.put("pollSessionID", sessionID);
		docData.put("pollStartTime", System.currentTimeMillis());
		ApiFuture<WriteResult> future = db.collection("polls").document(pollID).set(docData);
		
		docData = new HashMap<>();
		docData.put("courseActivityPollID", pollID);
		future = db.collection("courses").document(courseID).set(docData, SetOptions.merge());
	}
}
