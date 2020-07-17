import java.awt.AWTException;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextPane;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

import com.google.api.client.http.HttpResponse;
import com.sun.jna.Native;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.platform.win32.WinDef.RECT;

public class KeyLoggerHelper extends JFrame implements NativeKeyListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private boolean shouldStart = true;
	public Display display;
	public USBController usb;
	public WebController web;
	public VoteStatus voteStatus;
	public String imageUploadSite, lecture, courseName, instructorID;

	private static final int MAX_TITLE_LENGTH = 1024;
	private boolean ctrl = false, shift = false, s = false, v = false;
	private int screen_shot_number = 1;
	private int current_number = 1;

	public KeyLoggerHelper(Display display, USBController usb, WebController web, VoteStatus voteStatus,
			String imageUploadSite, String lecture, String courseName, String instructorID) {
		this.display = display;
		this.usb = usb;
		this.web = web;
		this.voteStatus = voteStatus;
		this.imageUploadSite = imageUploadSite;
		this.lecture = lecture;
		this.courseName = courseName;
		this.instructorID = instructorID;

	}

	public void start_stop_poll() {
		if (shouldStart) {
			usb.startPoll();
			web.createPoll();
			voteStatus.view(true);
		} else {
			usb.stopPoll();
			web.deactivatePoll();
			voteStatus.view(false);
		}

		shouldStart = !shouldStart;
	}

	public void view_poll() {
		if (!display.isOpen()) {
			display.openDisplay();
		} else
			display.closeDisplay();
	}

	public void take_pic() {
		// System.out.println("Take Screenshot");
		try {
			int width = 0;
			int height = 0;

			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			GraphicsDevice[] gs = ge.getScreenDevices();

			DisplayMode mode = gs[gs.length - 1].getDisplayMode();
			width += mode.getWidth();
			height += mode.getHeight();
			BufferedImage image = new Robot()
					.createScreenCapture(gs[gs.length - 1].getDefaultConfiguration().getBounds());
			ImageIO.write(image, "jpg",
					new File("screenshots/screenshot" + Integer.toString(screen_shot_number) + ".jpg"));
			screen_shot_number++;
			current_number++;
			CloseableHttpClient httpclient = HttpClients.createDefault();
			MultipartEntityBuilder entitybuilder = MultipartEntityBuilder.create();
			entitybuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
			entitybuilder.addTextBody("courseName", courseName);
			entitybuilder.addTextBody("sessionID", lecture);
			entitybuilder.addTextBody("instructorID", instructorID);
			entitybuilder.addBinaryBody("file",
					new File("screenshots/screenshot" + Integer.toString(screen_shot_number - 1) + ".jpg"));
			HttpEntity mutiPartHttpEntity = entitybuilder.build();
			RequestBuilder reqbuilder = RequestBuilder.post(imageUploadSite);
			reqbuilder.setEntity(mutiPartHttpEntity);
			HttpUriRequest multipartRequest = reqbuilder.build();
			CloseableHttpResponse httpresponse = httpclient.execute(multipartRequest);
			System.out.println(EntityUtils.toString(httpresponse.getEntity()));
			System.out.println(httpresponse.getStatusLine());

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (HeadlessException | AWTException e) {

		}
	}

	public void nativeKeyPressed(NativeKeyEvent e) {

		if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL)
			ctrl = true;
		else if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT)
			shift = true;
		if (ctrl && shift) {
			if (e.getKeyCode() == NativeKeyEvent.VC_J)
				start_stop_poll();
			else if (e.getKeyCode() == NativeKeyEvent.VC_K)
				view_poll();
		}
		if (!isWindowInFullScreen() && !getWindowInFocus().toLowerCase().contains("powerpoint")) {
			if (ctrl && shift) {
				if (e.getKeyCode() == NativeKeyEvent.VC_L)
					take_pic();
				else if (e.getKeyCode() == NativeKeyEvent.VC_ESCAPE) {
					try {
						GlobalScreen.unregisterNativeHook();
					} catch (NativeHookException e1) {
						e1.printStackTrace();
					}
				}
			}
		} else if (isWindowInFullScreen() && getWindowInFocus().toLowerCase().contains("powerpoint")) {
			if (e.getKeyCode() == NativeKeyEvent.VC_SPACE) {
				if (screen_shot_number == current_number)
					take_pic();
				else
					current_number++;
			} else if (e.getKeyCode() == NativeKeyEvent.VC_N) {
				if (screen_shot_number == current_number)
					take_pic();
				else
					current_number++;
			} else if (e.getKeyCode() == NativeKeyEvent.VC_ENTER) {
				if (screen_shot_number == current_number)
					take_pic();
				else
					current_number++;
			} else if (e.getKeyCode() == NativeKeyEvent.VC_PAGE_DOWN) {
				if (screen_shot_number == current_number)
					take_pic();
				else
					current_number++;
			} else if (e.getKeyCode() == NativeKeyEvent.VC_RIGHT) {
				if (screen_shot_number == current_number)
					take_pic();
				else
					current_number++;
			} else if (e.getKeyCode() == NativeKeyEvent.VC_DOWN) {
				if (screen_shot_number == current_number)
					take_pic();
				else
					current_number++;
			} else if (e.getKeyCode() == NativeKeyEvent.VC_P)
				current_number--;
			else if (e.getKeyCode() == NativeKeyEvent.VC_PAGE_UP)
				current_number--;
			else if (e.getKeyCode() == NativeKeyEvent.VC_LEFT)
				current_number--;
			else if (e.getKeyCode() == NativeKeyEvent.VC_UP)
				current_number--;
			else if (e.getKeyCode() == NativeKeyEvent.VC_BACKSPACE)
				current_number--;
		}
	}

	public void nativeKeyReleased(NativeKeyEvent e) {
		if (e.getKeyCode() == NativeKeyEvent.VC_CONTROL)
			ctrl = false;
		else if (e.getKeyCode() == NativeKeyEvent.VC_SHIFT)
			shift = false;
	}

	public void nativeKeyTyped(NativeKeyEvent e) {
		// System.out.println("Key Typed: " + e.getKeyText(e.getKeyCode()));
	}

	public void startHotKeys() {
		try {
			GlobalScreen.registerNativeHook();
		} catch (NativeHookException ex) {
			System.exit(1);
		}
		GlobalScreen.addNativeKeyListener(new KeyLoggerHelper(this.display, this.usb, this.web, this.voteStatus,
				this.imageUploadSite, this.lecture, this.courseName, this.instructorID));
	}

	public void stopHotKeys() {
		try {
			GlobalScreen.unregisterNativeHook();
		} catch (NativeHookException e1) {
			e1.printStackTrace();
		}
	}

	public boolean isProcRunning(String s) {
		boolean toReturn = false;
		try {
			String line;
			String pidInfo = "";
			Process p = Runtime.getRuntime().exec(System.getenv("windir") + "\\system32\\" + "tasklist.exe");
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			while ((line = input.readLine()) != null) {
				pidInfo += line;
			}

			input.close();

			if (pidInfo.toLowerCase().contains(s)) {
				toReturn = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return toReturn;
	}

	public boolean isWindowInFullScreen() {
		WinDef.HWND foregroundWindow = User32.INSTANCE.GetForegroundWindow();
		WinDef.RECT foregroundRectangle = new WinDef.RECT();
		WinDef.RECT desktopWindowRectangle = new WinDef.RECT();
		User32.INSTANCE.GetWindowRect(foregroundWindow, foregroundRectangle);
		WinDef.HWND desktopWindow = User32.INSTANCE.GetDesktopWindow();
		User32.INSTANCE.GetWindowRect(desktopWindow, desktopWindowRectangle);
		return foregroundRectangle.toString().equals(desktopWindowRectangle.toString());
	}

	public String getWindowInFocus() {
		char[] buffer = new char[MAX_TITLE_LENGTH * 2];
		HWND hwnd = User32.INSTANCE.GetForegroundWindow();
		User32.INSTANCE.GetWindowText(hwnd, buffer, MAX_TITLE_LENGTH);
		RECT rect = new RECT();
		User32.INSTANCE.GetWindowRect(hwnd, rect);
		return Native.toString(buffer);
	}

}
