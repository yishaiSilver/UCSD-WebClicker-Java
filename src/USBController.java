/**
 * This is the highest level of this program. This interfaces with
 * Raspberry Pi HID device and controls a display, thus allowing
 * the instructor to hide and display the chart of students' responses. 
 * 
 * @author Yishai
 * @version 1.0
 * @since 2019-08-19
 */
import java.util.Arrays;
import java.util.List;

import purejavahidapi.*;

public class USBController {
	
	//Ints and their associated answers
	public static final int ANSWER_A = 81;
	public static final int ANSWER_B = 82;
	public static final int ANSWER_C = 83;
	public static final int ANSWER_D = 84;
	public static final int ANSWER_E = 85;
	
	//Answers
	public static final String A = "A";
	public static final String B = "B";
	public static final String C = "C";
	public static final String D = "D";
	public static final String E = "E";
	
	//Bytes for commands
	public static final byte BYTE_OPEN = (byte)0xAA;
	public static final byte BYTE_CLOSE = (byte)0xBB;
	public static final byte BYTE_NEXT_QUESTION = (byte)0xCC;
	public static final byte BYTE_SCREENSHOT = (byte)0xD1;
	public static final byte BYTE_SAVE_ALL_SCREENSHOTS = (byte)0xD2;
	public static final byte BYTE_RESPONSE_ONE = (byte)0x02;
	public static final byte BYTE_RESPONSE_TWO = (byte)0x30;
	
	//PiClicker VID and PID
	public static final short PICLICKER_VID = (short)0x1881;
	public static final short PICLICKER_PID = (short)0x0150;
	
	public static final byte[] START_POLL = new byte[] {0x01, 0x19, 0x66, 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] STOP_POLL = new byte[] {0x01, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 
			0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	
	//Is the device open?
	private static boolean deviceOpen = false;
	
	private int votesA = 0;
	private int votesB = 0;
	private int votesC = 0;
	private int votesD = 0;
	private int votesE = 0;
	private int[] allVotes;
	
	private HidDevice dev;
	
	/**
	 * Runs a loop to connect to Pi and then control display 
	 * 
	 * @param args none expected
	 */
	public USBController() {
		
		Thread t = new Thread(new Runnable() { public void run() { 
			try {
				//Screenshot screenshotController = new Screenshot();
				while(true) {
					//Scan for device when it's not open
					if(!deviceOpen) {
						
						//Get list of all devices
						List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
						HidDeviceInfo devInfo = null;
						
						//Loop through list of devices, look for PiClicker
						for (HidDeviceInfo info : devList) {
							if (info.getVendorId() == PICLICKER_VID && info.getProductId() == PICLICKER_PID) {
								// Save PiClicker's info. Change display to connected
								devInfo = info;
								System.out.println("Device found.");
								deviceOpen = true;
								break;
							}
						}
						
						//If the device is open, configure it
						if(deviceOpen) {
							dev = PureJavaHidApi.openDevice(devInfo);						
	
							//Give it an InputReportListener
							dev.setInputReportListener(new InputReportListener() {
								@Override
								public void onInputReport(HidDevice source, byte Id, byte[] data, int len) {
									//Print out the report
									System.out.printf("onInputReport: id %d len %d data ", Id, len);
									for (int i = 0; i < len; i++) {
										System.out.printf("%02X ", data[i]);
									}
									System.out.println();
									
									//Check the first byte of the report and act accordingly
									if(data[0] == BYTE_OPEN) {
										//display.openDisplay();
									}
									else if (data[0] == BYTE_CLOSE) {
										//display.closeDisplay();
									}
									else if (data[0] == BYTE_NEXT_QUESTION) {
										Display.nextQuestion();
									}
									else if (data[0] == BYTE_SCREENSHOT) {
										//screenshotController.newScreenshot();
									}
									else if(data[0] == BYTE_SAVE_ALL_SCREENSHOTS) {
										//screenshotController.saveAllScreenshots(source);
									}
									else if(data[0] == BYTE_RESPONSE_ONE && data[1] == BYTE_RESPONSE_TWO) {
										//Get bytes responsible for ID
										byte[] idArr = Arrays.copyOfRange(data, 5, 8);
										String idStr = "";
										//Add each byte to idStr
										for(int i = 0; i < idArr.length; i ++) {
											idStr += String.format("%02X", idArr[i]);
										}
					
										System.out.println(idStr);
										
										//Get byte responsible for response
										byte responseByte = data[4];
										//Convert byte to string
										String responseStr = String.format("%02X", responseByte);
										//Parse string to int
										int responseInt = Integer.parseInt(responseStr);
										//Get choice equivalent of int
										String responseLetter = "";
										switch (responseInt) {
											case 81:
												responseLetter = A;
												votesA++;
												break;
											case 82:
												responseLetter = B;
												votesB++;
												break;
											case 83:
												responseLetter = C;
												votesC++;
												break;
											case 84:
												responseLetter = D;
												votesD++;
												break;
											case 85:
												responseLetter = E;
												votesE++;
												break;
										}
										
										//Register the response
										//display.newResponse(idStr, responseLetter);
									}
								}
							});
							
							dev.setDeviceRemovalListener(new DeviceRemovalListener() {
								@Override
								public void onDeviceRemoval(HidDevice source) {
									System.out.println("Device removed.");
									deviceOpen = false;
									dev = null;
									Display.setConnected(false);
								}
							});
							break;
						}
					}
					
					//System.out.println("Failed to connect to USB.");
	
					Thread.sleep(500);
				}	
			} catch (Exception e) {
				e.printStackTrace();
			}
		}});
		
		t.start();
	}
	
	public void resetVotes() {
		votesA = 0;
		votesB = 0;
		votesC = 0;
		votesD = 0;
		votesE = 0;
	}
	
	public int[] getVotes() {
		allVotes = new int[] {votesA, votesB, votesC, votesD, votesE};
		return allVotes;
	}
	
	public int getNumVotes() {
		return votesA + votesB + votesC + votesD + votesE;
	}
	
	public void startPoll() {
		resetVotes();
		
		if(dev != null) {
			try {
				dev.setOutputReport((byte)0, START_POLL, START_POLL.length);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopPoll() {
		if(dev != null) {
			try {
				dev.setOutputReport((byte)0, STOP_POLL, STOP_POLL.length);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}