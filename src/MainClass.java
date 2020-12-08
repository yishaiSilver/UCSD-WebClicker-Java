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
//				System.out.println(web.isCourseSelected());
//				if(web.isCourseSelected() && !startedKeyLogger) {
//					String imageUploadSite = "http://54.153.95.213:3001/upload";
//					String sessionNum = web.getSessionNumber();
//					String courseName = web.getSelectedCourse();
//					String instructorID = web.getID();
//					VoteStatus voteStatus = new VoteStatus();
//					
//					System.out.println(sessionNum + courseName + instructorID);
//					
//					KeyLogger l = new KeyLogger(display, usb, web, voteStatus, imageUploadSite, "Lecture" + sessionNum, courseName, instructorID);  
//					Thread t1 = new Thread(l);  
//					t1.start(); 
//					startedKeyLogger = true;
//				}
	
				if(controller.isPollActive()) {
					if(web.isSessionStarted()) {
						int[] votes = web.getVotes(true);
					
						if(votes != null) {
							display.updateDataset(votes);
							
							int total = 0;
							for(int i : votes) {
								total += i;
							}
								
							controller.setNumResponsesText("" + total);
						}
					}
					else {
						int total = display.getNumResponses();

						controller.setNumResponsesText("" + total);
					}
				}
				
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
