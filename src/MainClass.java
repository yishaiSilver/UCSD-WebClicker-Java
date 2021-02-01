public class MainClass {
	
	public static final String WEB_URL = "www.google.com";
	public static final int NUM_VOTES = 5;
	
	public static void main(String[] args) {
		Display display = new Display(); // used to display votes coming in

		WebController web = new WebController(display); // used to interface with the database.
		
		USBController usb = new USBController(web, display); // interfaces with USB device.
		
		ControlWindow controller = new ControlWindow(display, usb, web);
		
		long startMilli = -1;
		
		try {
			while(true) {
				if(controller.isPollActive()) {
					if(startMilli == -1) {
						startMilli = System.currentTimeMillis();
					}
					
					long timeElapsed = (System.currentTimeMillis() - startMilli) / 1000;
					
					if(web.isSessionStarted()) {
						int[] votes = web.getVotes(true);
					
						if(votes != null) {
							display.updateDataset(votes);
							
							int total = 0;
							for(int i : votes) {
								total += i;
							}
								
							controller.setElapsedTime((int)timeElapsed);
							controller.setNumResponsesText("" + total);
						}
					}
					else {
						int total = display.getNumResponses();

						controller.setElapsedTime((int)timeElapsed);
						controller.setNumResponsesText("" + total);
					}
				}else {
					startMilli = -1;
				}
				
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
