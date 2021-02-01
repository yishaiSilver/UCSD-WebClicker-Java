import java.awt.AWTException;
//import java.awt.DisplayMode;
//import java.awt.GraphicsDevice;
//import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Robot;
//import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;

import javax.imageio.ImageIO;
//import javax.swing.JButton;
import javax.swing.JFrame;
//import javax.swing.JTextPane;

import org.jnativehook.GlobalScreen;
import org.jnativehook.NativeHookException;
import org.jnativehook.keyboard.NativeKeyEvent;
import org.jnativehook.keyboard.NativeKeyListener;

//import com.google.api.client.http.HttpResponse;
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

//	private boolean shouldStart = true;
	public WebController web;

	private static final int MAX_TITLE_LENGTH = 1024;
	private boolean ctrl = false, shift = false; 
//			s = false, v = false;
	public int screen_shot_number;
	public int current_number;

	public KeyLoggerHelper(WebController web) {
		System.out.println("STARTING KEYLOGGER");
		this.web = web;
		this.screen_shot_number = 0;
		this.current_number = 0;

	}
	
//	public void take_pic() {
//		// System.out.println("Take Screenshot");
//		try {
//			int width = 0;
//			int height = 0;
//			int xCor = 0;
//			int yCor = 0;
//
//			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
//			GraphicsDevice[] gs = ge.getScreenDevices();
//
//			DisplayMode mode = gs[gs.length - 1].getDisplayMode();
//			width += mode.getWidth();
//			height += mode.getHeight();
//			double scale = 96.0 / Toolkit.getDefaultToolkit().getScreenResolution();
//			xCor = (int) ((gs[gs.length - 1].getDefaultConfiguration().getBounds().getX()) * scale);
//			yCor = (int) ((gs[gs.length - 1].getDefaultConfiguration().getBounds().getY()) * scale);
//
//			BufferedImage image = new Robot().createScreenCapture(new Rectangle(xCor, yCor, width, height));
////			ImageIO.write(image, "jpg",
////					new File("screenshots/screenshot" + Integer.toString(screen_shot_number) + ".jpg"));
//
//			ByteArrayOutputStream baos = new ByteArrayOutputStream();
//			ImageIO.write(image, "jpg", baos);
//			
//			String b64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
//
//			System.out.println("Uploaded Screenshot"); 
//			web.uploadScreenshot(b64Image);
//			
//
//			screen_shot_number++;
//			current_number++;
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (HeadlessException | AWTException e) {
//
//		}
//	}

	public void take_pic() {
		// System.out.println("Take Screenshot");
		try {
			Rectangle fullscreen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage screenshot = new Robot().createScreenCapture(fullscreen);
			Image resized = screenshot.getScaledInstance(720, 480, Image.SCALE_DEFAULT);
			screenshot = Screenshot.toBufferedImage(resized);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(screenshot, "jpg", baos);

			File file = new File("screenshot.png");
//			file.createNewFile();
			
			ImageIO.write(screenshot, "png", file);

			String b64Image = Base64.getEncoder().encodeToString(baos.toByteArray());
			
			System.out.println("Uploaded Screenshot: " + baos.toByteArray().length); 
			web.uploadScreenshot(b64Image);

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
//  		} else if (isWindowInFullScreen() && getWindowInFocus().toLowerCase().contains("powerpoint")) {              
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
		GlobalScreen.addNativeKeyListener(new KeyLoggerHelper(this.web));
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
