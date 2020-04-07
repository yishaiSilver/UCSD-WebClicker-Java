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
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.google.firebase.database.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sun.misc.Unsafe;

public class WebController {

	public static final String COURSE_NAME = "Course1";
	
	private int votesA = 0;
	private int votesB = 0;
	private int votesC = 0;
	private int votesD = 0;
	private int votesE = 0;
	private int[] allVotes;
	
	private Firestore db;
	
	private String courseID = "";
	private String sessionID = "";
	private String pollID = "";
	private Map<String, Object> courseCategories;
	
	public WebController() {
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
					System.err.println("CONNECTED");
					
					ApiFuture<QuerySnapshot> query = db.collection("courses").get();
					QuerySnapshot querySnapshot = query.get();
					List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
					
					for (QueryDocumentSnapshot document : documents) {
						if(document.getString("courseName") == COURSE_NAME) {
							courseID = document.getId();
							pollID = document.getString("courseActivityPollID");
							courseCategories = (HashMap<String, Object>)document.get("courseCategories");
						}
					}
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
				Map<String, Object> docData = new HashMap<>();
				docData.put("sessionCourseID", courseID);
				long startTime = System.currentTimeMillis();
				docData.put("sessionStartTime", startTime);
				ApiFuture<WriteResult> future = db.collection("sessions").document().set(docData);
				
				ApiFuture<QuerySnapshot> query = db.collection("sessions").get();
				QuerySnapshot querySnapshot = query.get();
				List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
				
				for (QueryDocumentSnapshot document : documents) {
					if(document.getLong("sessionStartTime") == startTime) {
						sessionID = document.getId();
						break;
					}
				}
				
				docData = new HashMap<>();
				docData.put("courseActivitySessionID", sessionID);
				future = db.collection("sessions").document(courseID).set(docData);
				
				return true;
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return false;
	}
	
	public void startPoll() {
		if(db != null) {
			try {
				Map<String, Object> docData = new HashMap<>();
				docData.put("pollCategories", courseCategories);
				docData.put("pollSessionID", sessionID);
				long startTime = System.currentTimeMillis();
				docData.put("pollStartTime", startTime);
				ApiFuture<WriteResult> future = db.collection("polls").document().set(docData);
				
				
				// I really need to change this to use a known ID so that we don't have to loop
				// through all of the polls in the data base
				ApiFuture<QuerySnapshot> query = db.collection("polls").get();
				QuerySnapshot querySnapshot = query.get();
				List<QueryDocumentSnapshot> documents = querySnapshot.getDocuments();
				
				for (QueryDocumentSnapshot document : documents) {
					if(document.getLong("pollsStartTime") == startTime) {
						pollID = document.getId();
						break;
					}
				}
				// this would be the end of the change
				
				docData = new HashMap<>();
				docData.put("courseActivityPollID", pollID);
				future = db.collection("sessions").document(courseID).set(docData);
				
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
										//change.
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
				ApiFuture<WriteResult> result = docRef.set(data);
				
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
}
