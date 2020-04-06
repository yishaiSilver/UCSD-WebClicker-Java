/**
 * This class is used to represent a student and his or her response.
 * 
 * @author Yishai
 * @version 1.0
 * @since 2019-08-19
 */
public class Response {
	
	private String studentID;
	private String response;
	
	/**
	 * Used to construct a response
	 *
	 * @param studentID
	 * @param response
	 */
	public Response(String studentID, String response) {
		this.studentID = studentID;
		this.response = response;
	}
	
	/**
	 * Used to access the student's ID
	 * @return the student's ID
	 */
	public String getStudentID() {
		return studentID;
	}
	
	/**
	 * Used to access the student's response
	 * @return the student's response
	 */
	public String getResponse() {
		return response;
	}
	
	/**
	 * Used to change the student's response
	 * @param response the student's new response
	 */
	public void setResponse(String response) {
		this.response = response;
	}
}
