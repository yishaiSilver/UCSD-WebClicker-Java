
public class Course {
	private String name;
	private String courseID;
	private String activeSession;
	private String freq;
	
	public Course(String name, String courseID, String activeSession, String freq) {
		this.name = name;
		this.courseID = courseID;
		this.activeSession = activeSession;
		this.freq = freq;
	}
	
	public String getName() {
		return name;
	}
	
	public String getID() {
		return courseID;
	}
	
	public String getSession() {
		return activeSession;
	}
	
	public void setSession(String sessionID) {
		this.activeSession = sessionID;
	}
	
	public boolean hasActiveSession() {
		return !activeSession.contentEquals("");
	}
	
	public void setFreq(String freq) {
		this.freq = freq;
	}
	
	public String getFreq() {
		return freq;
	}
}
