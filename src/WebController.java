import org.apache.log4j.BasicConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.awt.BorderLayout;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

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
	
	private String instructorID = "yishai";
	private String instructorPW = "123456";
	
	private String courseID = "";
	private String sessionID = "";
	private String pollID = "";
	
	private Map<String, Object> courseCategories;
	
	List<QueryDocumentSnapshot> users;
	JTextField usernameBox;
	JPasswordField passwordBox;
	
	public WebController(Display display) {
		this.display = display;
		
		Thread t = new Thread(new Runnable() { public void run() { 
			BasicConfigurator.configure(); // for web
			
			while(true) {
				try {
					InputStream serviceAccount = new FileInputStream("assets/serviceAccount.json"); // ### NEED TO CHANGE ###
					GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
					FirebaseOptions options = new FirebaseOptions.Builder()
					    .setCredentials(credentials)
					    .build();
					FirebaseApp.initializeApp(options);
					db = FirestoreClient.getFirestore();
					
					ApiFuture<QuerySnapshot> query = db.collection("users").get();
					QuerySnapshot querySnapshot = query.get();
					users = querySnapshot.getDocuments();
					break;
				} catch(Exception e) {
					e.printStackTrace();
				}
				try {
					Thread.sleep(500);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			
			begin();
		}});
		t.start();
	}
	
	private void begin() {
		//Initialize displayFrame
		JFrame displayFrame = new JFrame();
		displayFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		displayFrame.setTitle(WINDOW_TITLE);
		displayFrame.setAlwaysOnTop(true);
		displayFrame.setLayout(new BorderLayout());
		
		usernameBox = new JTextField(instructorID, 20);
		
		passwordBox = new JPasswordField(instructorPW, 20);
		passwordBox.setEchoChar('*');
		passwordBox.addActionListener(login);
		
		displayFrame.add(usernameBox);
		displayFrame.add(passwordBox);
		
		//Save session on exit.
		//displayFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		
		// Center the display on the screen
		displayFrame.setLocationRelativeTo(null);
		
		displayFrame.validate();
		displayFrame.setVisible(true);
	}
	
	private ActionListener login = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			try {
				boolean userFound = false;
				for (QueryDocumentSnapshot document : users) {
					System.err.println(document.getId());
					if(document.getId() != null &&
							document.getId().contentEquals(instructorID) &&
							document.getString("password").contentEquals(instructorPW)) {
						System.err.println("USER FOUND");
						userFound = true;
						break;
					}
				}
				
				if(!userFound) {
					System.err.println("No user found."); // HANDLE INCORRECT LOGIN INFORMATION
				}
				else {
					// HANDLE CORRECT LOGIN INFORMATION
					
					//courseID = document.getId();
					//pollID = document.getString("courseActivityPollID");
					//courseCategories = (HashMap<String, Object>)document.get("courseCategories");
					//temporary
					//newSession();
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
				ApiFuture<QuerySnapshot> query = db.collection("courses").get();
				QuerySnapshot querySnapshot = query.get();
				List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
				
				boolean userFound = false;
				for (QueryDocumentSnapshot document : documents) {
					System.err.println(document.getId());
					if(document.getId() != null &&
							document.getId().contentEquals(instructorID) &&
							document.getString("password").contentEquals(instructorPW)) {
						System.err.println("USER FOUND");
						userFound = true;
						break;
					}
				}
				
				if(!userFound) {
					System.err.println("No user found."); // HANDLE INCORRECT LOGIN INFORMATION
				}
				else {
					// HANDLE CORRECT LOGIN INFORMATION
					
					//courseID = document.getId();
					//pollID = document.getString("courseActivityPollID");
					//courseCategories = (HashMap<String, Object>)document.get("courseCategories");
					//temporary
					//newSession();
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
