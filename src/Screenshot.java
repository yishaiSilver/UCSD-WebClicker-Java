import java.awt.Robot;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.imageio.ImageIO;

public class Screenshot {
	public static final String PI_ADDR = "raspberrypi.local";
	public static final int PI_PORT = 22;
	public static final String USER = "pi";
	
	public static final int BUFFER_SIZE = 4096;
	
	public static final byte CONFIRMATION_BYTE = (byte)0xD2;
	
	public static final String FILENAME = "./screenshot.jpg";
	public static final int HEX_STRING_BYTE_LENGTH = 2;
	
	public Screenshot(Socket client) {
		try {
			//Take screenshot
			//Get rectangle containing full screen.
			Rectangle fullscreen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage screenshot = new Robot().createScreenCapture(fullscreen);
			//Image resized = screenshot.getScaledInstance(720, 480, Image.SCALE_DEFAULT);
			//screenshot = toBufferedImage(resized);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(screenshot, "jpg", baos);
			byte[] screenshotBytes = baos.toByteArray();
			
			System.out.println("Screenshot saved, length: " + screenshotBytes.length);
			DataOutputStream dataOutput = new DataOutputStream(client.getOutputStream());
			byte[] lengthPacket = new byte[BUFFER_SIZE];
			
			String hexLength = Integer.toHexString(screenshotBytes.length);
			byte[] byteLength = hexToByteArray(hexLength);
			for(int i = 1; i < byteLength.length + 1; i ++) {
				lengthPacket[lengthPacket.length - i] = byteLength[byteLength.length - i];
			}
			
			dataOutput.write(lengthPacket);
			
			dataOutput.write(screenshotBytes);
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Derived from https://www.tutorialspoint.com/convert-hex-string-to-byte-array-in-java,
	 * this method converts a hex string to a byte array
	 * 
	 * @param str
	 * @return
	 */
	public byte[] hexToByteArray(String str) {
		if(str.length() % 2 != 0) {
			str = "0" + str;
		}
		
		byte[] bytes = new byte[str.length() / 2];
		for (int i = 0; i < bytes.length; i++) {
			int index = i * 2;
	        int j = Integer.parseInt(str.substring(index, index + 2), 16);
	        bytes[i] = (byte) j;
		}	
		return bytes;
	}
	
	/**
	 * Converts a given Image into a BufferedImage
	 *
	 * @param img The Image to be converted
	 * @return The converted BufferedImage
	 */
	public static BufferedImage toBufferedImage(Image img)
	{
	    // Create a buffered image
	    BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_RGB);

	    // Draw the image on to the buffered image
	    Graphics2D bGr = bimage.createGraphics();
	    bGr.drawImage(img, 0, 0, null);
	    bGr.dispose();

	    // Return the buffered image
	    return bimage;
	}
	
	/**
	 * Used to make first transfer of image faster. Gets the juices flowing.
	 */
	public static void takeFalseScreenshot() {
		try {
			//Take screenshot
			//Get rectangle containing full screen.
			Rectangle fullscreen = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
			BufferedImage screenshot = new Robot().createScreenCapture(fullscreen);
			//Image resized = screenshot.getScaledInstance(720, 480, Image.SCALE_DEFAULT);
			//screenshot = toBufferedImage(resized);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(screenshot, "jpg", baos);
			baos.toByteArray();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}