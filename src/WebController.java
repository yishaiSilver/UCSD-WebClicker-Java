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
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.WriteResult;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebController {

	private int votesA = 0;
	private int votesB = 0;
	private int votesC = 0;
	private int votesD = 0;
	private int votesE = 0;
	private int[] allVotes;
	
	private String url;
	
	private Firestore db;
	
	public WebController(String url) {
		this.url = url;

		Thread t = new Thread(new Runnable() { public void run() { 
			while(true) {
				try {
					InputStream serviceAccount = new FileInputStream("path/to/serviceAccount.json"); // ### NEED TO CHANGE ###
					GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);
					FirebaseOptions options = new FirebaseOptions.Builder()
					    .setCredentials(credentials)
					    .build();
					FirebaseApp.initializeApp(options);
					Firestore db = FirestoreClient.getFirestore();
					break;
				} catch(Exception e) {
					//System.out.println("Failed to connect to web.");
					//e.printStackTrace();
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
	
	public void startPoll() {
		resetVotes();
		if(db != null) {
			try {
				DocumentReference docRef = db.collection("users").document("alovelace");
				Map<String, Object> data = new HashMap<>();
				data.put("first", "Ada");
				ApiFuture<WriteResult> result = docRef.set(data);
				System.out.println("Update time : " + result.get().getUpdateTime());
			} catch (Exception e) {
				System.out.println("Error starting poll through web");
				//e.printStackTrace();
			}
		}
	}
	
	public void stopPoll() {
		if(db != null) {
			try {
				DocumentReference docRef = db.collection("users").document("alovelace");
				Map<String, Object> data = new HashMap<>();
				data.put("first", "Ada");
				ApiFuture<WriteResult> result = docRef.set(data);
				System.out.println("Update time : " + result.get().getUpdateTime());
			} catch (Exception e) {
				System.out.println("Error stopping poll through web");
				//e.printStackTrace();
			}
		}
	}
}
