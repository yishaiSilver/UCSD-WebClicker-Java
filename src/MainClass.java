import org.apache.log4j.BasicConfigurator;

public class MainClass {
	
	public static final String WEB_URL = "www.google.com";
	public static final int NUM_VOTES = 5;
	
	public static void main(String[] args) {
		Display display = new Display(); // used to display votes coming in

		WebController web = new WebController(); // used to interface with the database.
		
		USBController usb = new USBController(); // interfaces with USB device.
		
		ControlWindow controller = new ControlWindow(display, usb, web);
		
		try {
			while(true) {
				controller.setNumResponsesText("" + (usb.getNumVotes() + web.getNumVotes()));
				
				int[] usbVotes = usb.getVotes();
				int[] webVotes = web.getVotes();
				
				int[] totalVotes = new int[NUM_VOTES];
				for(int i = 0; i < NUM_VOTES; i ++) {
					totalVotes[i] = usbVotes[i] + webVotes[i];
				}
				
				display.updateDataset(totalVotes);
				
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
