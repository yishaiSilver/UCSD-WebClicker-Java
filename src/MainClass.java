import org.apache.log4j.BasicConfigurator;

public class MainClass {
	
	public static final String WEB_URL = "www.google.com";
	public static final int NUM_VOTES = 5;
	
	public static void main(String[] args) {
		Display display = new Display(); // used to display votes coming in

		WebController web = new WebController(display); // used to interface with the database.
		
		USBController usb = new USBController(web, display); // interfaces with USB device.
		
		ControlWindow controller = new ControlWindow(display, usb, web);
		
		boolean startedKeyLogger = false;
		
		try {
			while(true) {
				System.out.println(web.isCourseSelected());
				if(web.isCourseSelected() && !startedKeyLogger) {
					String imageUploadSite = "http://54.153.95.213:3001/upload";
					String lectureNum = web.getLectureNumber();
					String courseName = web.getSelectedCourse();
					String instructorID = web.getID();
					VoteStatus voteStatus = new VoteStatus();
					
					System.out.println(lectureNum + courseName + instructorID);
					
					KeyLogger l = new KeyLogger(display, usb, web, voteStatus, imageUploadSite, "Lecture"+lectureNum, courseName, instructorID);  
					Thread t1 = new Thread(l);  
					t1.start(); 
					startedKeyLogger = true;
				}
				
				controller.setNumResponsesText("" + display.getNumResponses());
				
				//voteStatus.setText("Poll Running, "+Integer.toString(usb.getNumVotes() + web.getNumVotes())+" responses.");
				
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
