/**
 * This class is used to represent a single question and it's responses
 * 
 * @author Yishai
 * @version 1.0
 * @since 2019-08-23
 */
import java.util.ArrayList;

public class Question {

	private String correctAnswer;
	
	private ArrayList<Response> responses = new ArrayList<Response>();
	
	public Question() {
		
	}
	
	public void newResponse(Response response) {
		responses.add(response);
	}
	
	public ArrayList<Response> getResponses() {
		return responses;
	}
	
	public void setAnswer(String answer) {
		correctAnswer = answer;
	}
	
	public String getAnswer() {
		return correctAnswer;
	}
}
