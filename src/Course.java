
public class Course {
	private String name;
	private String courseID;
	private String activeSession;
	
	public Course(String name, String courseID, String activeSession) {
		this.name = name;
		this.courseID = courseID;
		this.activeSession = activeSession;
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
}
