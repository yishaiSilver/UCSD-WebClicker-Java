import java.io.File;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextPane;

public class KeyLogger implements Runnable {

	private Display display;

	private USBController usb;
	private WebController web;
	public VoteStatus votes;
	public String imageUploadSite, lecture, courseName, instructorID;
	public int slideNum;

	public KeyLogger(Display display, USBController usb, WebController web, VoteStatus votes, String imageUploadSite,
			String lecture, int slideNum, String courseName, String instructorID) {
		this.display = display;
		this.usb = usb;
		this.web = web;
		this.votes = votes;
		this.imageUploadSite = imageUploadSite;
		this.lecture = lecture;
		this.courseName = courseName;
		this.instructorID = instructorID;
		this.slideNum = slideNum;

	}

	public void run() {
		
		File file = new File("screenshots/");
		file.mkdir();
		KeyLoggerHelper h = new KeyLoggerHelper(display, usb, web, votes, imageUploadSite, lecture, slideNum, courseName,
				instructorID);
		h.startHotKeys();

	}

}
