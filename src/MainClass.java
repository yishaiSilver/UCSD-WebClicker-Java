import org.apache.log4j.BasicConfigurator;

public class MainClass {
	
	public static final String WEB_URL = "www.google.com";
	public static final int NUM_VOTES = 5;
	
	public static void main(String[] args) {
		Display display = new Display(); // used to display votes coming in

		WebController web = new WebController(display); // used to interface with the database.
		
		USBController usb = new USBController(web, display); // interfaces with USB device.
		
		ControlWindow controller = new ControlWindow(display, usb, web);
		
		try {
			while(true) {
				controller.setNumResponsesText("" + display.getNumResponses());
				
				Thread.sleep(500);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
