import org.apache.log4j.BasicConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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

import sun.misc.Unsafe;

public class WebController {

	public static final String COURSE_NAME = "test";
	public static final String CHARACTERS = "0123456789"
			+ "abcdefghijklmnopqrstuvwxyz"
			+ "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final int ID_LENGTH = 20;
	
	private int votesA = 0;
	private int votesB = 0;
	private int votesC = 0;
	private int votesD = 0;
	private int votesE = 0;
	private int[] allVotes;
	
	private Display display;
	
	private Firestore db;
	
	private String courseID = "";
	private String sessionID = "";
	private String pollID = "";
	private Map<String, Object> courseCategories;
	
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
					
					ApiFuture<QuerySnapshot> query = db.collection("courses").get();
					QuerySnapshot querySnapshot = query.get();
					List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
					
					for (QueryDocumentSnapshot document : documents) {
						System.err.println(document.get("courseName"));
						if(document.getString("courseName") != null &&
								document.getString("courseName").contentEquals(COURSE_NAME)) {
							courseID = document.getId();
							pollID = document.getString("courseActivityPollID");
							courseCategories = (HashMap<String, Object>)document.get("courseCategories");
							System.err.println("UPDATED COURSE");
							break;
						}
					}
					
					//temporary
					newSession();
					break;
				} catch(Exception e) {
					System.out.println("Failed to connect to web.");
					e.printStackTrace();
				}
				
				try {
					Thread.sleep(500);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
		}});
		t.start();
	}
	
	public boolean newSession() {
		if(db != null) {
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
		
		if(db != null) {
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
								// I might need to filter out changes so that only votes
								// from the active poll register. But prob not. Ask Litao.
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
		if(db != null) {
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
		if(db != null) {
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
