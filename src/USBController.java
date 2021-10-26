/**
 * This is the highest level of this program. This interfaces with
 * Raspberry Pi HID device and controls a display, thus allowing
 * the instructor to hide and display the chart of students' responses. 
 * 
 * @author Yishai
 * @version 1.0
 * @since 2019-08-19
 */
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SpringLayout;

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
	
	/*
	 * Startup:
	 * 
				sendReport(PACKET_01120000);
				sendReport(PACKET_01320004);
				sendReport(PACKET_011e0000);
				sendReport(PACKET_01150000);
				sendReport(PACKET_01220000);
				sendReport(PACKET_01102141);
				sendReport(PACKET_011e0000);
				sendReport(PACKET_01160000);
				sendReport(PACKET_01142020);
				sendReport(PACKET_01136943);
				sendReport(PACKET_01142020);
	 */
	
////	public static final byte[] PACKET_AA = new byte[] {0x01, 0x10, 0x21, 0x41, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
////	public static final byte[] PACKET_AB = new byte[] {0x01, 0x10, 0x21, 0x42, 0x63, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
////	public static final byte[] PACKET_AC = new byte[] {0x01, 0x10, 0x21, 0x43, 0x63, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
////	public static final byte[] PACKET_AD = new byte[] {0x01, 0x10, 0x21, 0x44, 0x65, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//	
////	public static final byte[] PACKET_BA = new byte[] {0x01, 0x10, 0x22, 0x41, 0x63, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
////	public static final byte[] PACKET_BB = new byte[] {0x01, 0x10, 0x22, 0x42, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//	public static final byte[] PACKET_BC = new byte[] {0x01, 0x10, 0x21, 0x41, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//	public static final byte[] PACKET_BA = new byte[] {0x01, 0x10, 0x21, 0x41, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//	
//	public static final byte[] PACKET_CA = new byte[] {0x01, 0x10, 0x21, 0x41, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//	public static final byte[] PACKET_CA = new byte[] {0x01, 0x10, 0x21, 0x41, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
////	public static final byte[] PACKET_CC = new byte[] {0x01, 0x10, 0x23, 0x43, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//	public static final byte[] PACKET_CA = new byte[] {0x01, 0x10, 0x21, 0x41, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//	
//	public static final byte[] PACKET_DA = new byte[] {0x01, 0x10, 0x21, 0x41, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//	public static final byte[] PACKET_DA = new byte[] {0x01, 0x10, 0x21, 0x41, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
//	public static final byte[] PACKET_DA = new byte[] {0x01, 0x10, 0x21, 0x41, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
////	public static final byte[] PACKET_DD = new byte[] {0x01, 0x10, 0x24, 0x44, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

	public static final int FREQ_1_INDEX = 2;
	public static final int FREQ_2_INDEX = 3;
	
	public static final byte FREQ_A1 = 0x21;
	public static final byte FREQ_B1 = 0x22;
	public static final byte FREQ_C1 = 0x23;
	public static final byte FREQ_D1 = 0x24;

	public static final byte FREQ_A2 = 0x41;
	public static final byte FREQ_B2 = 0x42;
	public static final byte FREQ_C2 = 0x43;
	public static final byte FREQ_D2 = 0x44;
	
	
	public static final byte[] PACKET_0112aa00 = new byte[] {0x01, 0x12, (byte) 0xaa, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_01110000 = new byte[] {0x01, 0x11, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_01120000 = new byte[] {0x01, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_01133030 = new byte[] {0x01, 0x13, 0x30, 0x30, 0x3a, 0x30, 0x33, 0x20, 0x4e, 0x55, 0x4d, 0x20, 0x20, 0x20, 0x20, 0x20, 0x30, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_01150000 = new byte[] {0x01, 0x15, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_01160000 = new byte[] {0x01, 0x16, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_011e0000 = new byte[] {0x01, 0x1e, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_0132aa00 = new byte[] {0x01, 0x32, (byte) 0xaa, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_011eaa00 = new byte[] {0x01, 0x1e, (byte) 0xaa, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_01150057 = new byte[] {0x01, 0x15, 0x00, 0x57, 0x05, 0x04, 0x21, 0x43, 0x01, 0x00, 0x66, 0x00, 0x07, 0x00, 0x04, 0x12, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_01220000 = new byte[] {0x01, 0x22, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_0110aa00 = new byte[] {0x01, 0x10, (byte) 0xaa, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_0111aa00 = new byte[] {0x01, 0x11, (byte) 0xaa, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_0119aa00 = new byte[] {0x01, 0x19, (byte) 0xaa, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_0116aa00 = new byte[] {0x01, 0x16, (byte) 0xaa, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_0129aa00 = new byte[] {0x01, 0x29, (byte) 0xaa, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_012aaa00 = new byte[] {0x01, 0x2a, (byte) 0xaa, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_022c0000 = new byte[] {0x02, 0x2c, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_01102141 = new byte[] {0x01, 0x10, 0x21, 0x41, 0x60, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
	public static final byte[] PACKET_01298080 = new byte[] {0x01, 0x29, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, (byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01};
	public static final byte[] PACKET_012a2141 = new byte[] {01, 0x2a, 0x21, 0x41, 0x05, 0x65, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06};
	public static final byte[] PACKET_01320004 = new byte[] {01, 0x32, 0x00, 0x04, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x76};
	public static final byte[] PACKET_01136943 = new byte[] {01, 0x13, 0x69, 0x43, 0x6c, 0x69, 0x63, 0x6b, 0x65, 0x72, 0x20, 0x53, 0x79, 0x73, 0x74, 0x65, 0x6d, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06};
	public static final byte[] PACKET_01142020 = new byte[] {01, 0x14, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x06};
	
	//Is the device open?
	private static boolean devicePresent = false;
	private static boolean deviceConfigured = false;
	
	private byte freq1 = FREQ_A1;
	private byte freq2 = FREQ_A2;
	
	private int votesA = 0;
	private int votesB = 0;
	private int votesC = 0;
	private int votesD = 0;
	private int votesE = 0;
	private int[] allVotes;
	
	private WebController web;
	private Display display;
	
	private HidDevice dev;
	
	private Thread pollThread;
	private boolean pollThreadShouldStop;
	
	private boolean canStartPoll;
	
	ArrayList<Response> responses;
	
	private ControlWindow controlWindow;
	
	private JFrame displayFrame;
	private JPanel loginPanel;
	private JPanel freqSelectionPanel;
	private JComboBox<String> freqSelector1;
	private JComboBox<String> freqSelector2;
	private JButton confirmButton;
	
	/**
	 * Runs a loop to connect to Pi and then control display 
	 * 
	 * @param args none expected
	 */
	public USBController(WebController web, Display display) {
		this.web = web;
		this.display = display;
		this.pollThread = null;
		this.pollThreadShouldStop = false;
		this.responses = new ArrayList<Response>();
		
		Thread t = new Thread(new Runnable() { public void run() { 
			try {
				//Screenshot screenshotController = new Screenshot();
				while(true) {
					//Scan for device when it's not open
					if(!devicePresent) {
						
						//Get list of all devices
						List<HidDeviceInfo> devList = PureJavaHidApi.enumerateDevices();
						HidDeviceInfo devInfo = null;
						
						//Loop through list of devices, look for PiClicker
						for (HidDeviceInfo info : devList) {
							if (info.getVendorId() == PICLICKER_VID && info.getProductId() == PICLICKER_PID) {
								// Save PiClicker's info. Change display to connected
								devInfo = info;
								System.err.println("Device found.");
								devicePresent = true;
								break;
							}
						}
						
						//If the device is open, configure it
						if(devicePresent && !deviceConfigured) {
							begin();
							dev = PureJavaHidApi.openDevice(devInfo);
							
							//Give it an InputReportListener
							dev.setInputReportListener(new InputReportListener() {
								@Override
								public void onInputReport(HidDevice source, byte Id, byte[] data, int len) {
									//Print out the report
//									System.out.printf("onInputReport: id %d len %d data ", Id, len);
//									for (int i = 0; i < len; i++) {
//										System.out.printf("%02X ", data[i]);
//										System.out.flush();
//									}
//									System.out.println();
									
									//Check the first byte of the report and act accordingly
									if(data[0] == (byte) 0x02 && data[1] == (byte) 0x2C) {
										canStartPoll = true;
										System.err.println("Can now start poll.");
									}
									else if(data[0] == BYTE_OPEN) {
										//display.openDisplay();
									}
									else if (data[0] == BYTE_CLOSE) {
										//display.closeDisplay();
									}
									else if (data[0] == BYTE_NEXT_QUESTION) {
										//Display.nextQuestion();
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
// need to switch to display, web	
										String idStr = "";
										//Add each byte to idStr
										for(int i = 0; i < idArr.length; i ++) {
											idStr += String.format("%02X", idArr[i]);
										}
										
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
										
//										System.err.println("Student ID: " + idStr);
										
										//Register the response
										boolean connected = web.isCourseSelected();
										boolean shouldUpdateDB = display.newResponse(idStr, responseLetter, !connected);
//										System.out.println(web.isSessionStarted());										
										if(shouldUpdateDB && web.isSessionStarted()) {
											web.newResponse(idStr, responseLetter);
										}
									}
								}
							});
							
							dev.setDeviceRemovalListener(new DeviceRemovalListener() {
								@Override
								public void onDeviceRemoval(HidDevice source) {
									System.out.println("Device removed.");
									devicePresent = false;
									deviceConfigured = false;
									dev = null;
									controlWindow.updateUSBStatus(false);
									//Display.setConnected(false);
								}
							});
							

							startup();
//							Thread.sleep(250);
							startSession();
//							
//							startPoll();
							deviceConfigured = true;
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
	
	public void setControlWindow(ControlWindow controlWindow) {
		this.controlWindow = controlWindow;
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
				
				responses.clear();
				
				pollThread = new Thread(new Runnable() { public void run() {
					try {
						while(true) {
							System.err.println("Trying to start poll. ");
							if(canStartPoll) {
								dev.setOutputReport((byte)0, START_POLL, START_POLL.length);
								dev.setOutputReport((byte)0, PACKET_01110000, PACKET_01110000.length);
								break;
							}
						}
						
						int time = 2; 
						Thread.sleep(1000);
						
						while(true) {
							if(pollThreadShouldStop) {
								pollThread = null;
								pollThreadShouldStop = false;
								break;
							}
														
							byte[] screenTimeUpdate = getTimeReport(time, 0);
							dev.setOutputReport((byte)0, screenTimeUpdate, screenTimeUpdate.length);
							Thread.sleep(500);

							int[] allVotes = display.getVotes();
							if(web.isCourseSelected()) {
								allVotes = web.getVotes(false);
							}

							byte[] screenVoteUpdate = getVoteCountReport(0, 0, 0, 0, 0);
							if(allVotes != null) {
								screenVoteUpdate = getVoteCountReport(allVotes[0], allVotes[1], allVotes[2], allVotes[3], allVotes[4]);
							}
							
							dev.setOutputReport((byte)0, screenVoteUpdate, screenVoteUpdate.length);
							Thread.sleep(500);
							
							time ++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}});
				pollThread.start();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public void stopPoll() {
		if(dev != null) {
			try {
				dev.setOutputReport((byte)0, STOP_POLL, STOP_POLL.length);
				sendReport(PACKET_01136943);
				sendReport(PACKET_01142020);
				if (pollThread != null) {
					pollThreadShouldStop = true;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public boolean isConnected() {
		return canStartPoll;
	}
	
	public void startup() {
		/*
		 * 01120000
		 * read
		 * 01320004
		 * read
		 * 011e0000
		 * read
		 * 01150000
		 * read
		 * 01220000
		 * read
		 * 01102141
		 * read
		 * 011e0000 
		 * read
		 * 01160000
		 * read
		 * 01136943
		 * 01142020
		 * */
		if(dev != null) {
			try {
				sendReport(PACKET_01120000);
				sendReport(PACKET_01320004);
				sendReport(PACKET_011e0000);
				sendReport(PACKET_01150000);
				sendReport(PACKET_01220000);
				
				byte[] freq_report = PACKET_01102141;
				freq_report[FREQ_1_INDEX] = freq1;
				freq_report[FREQ_2_INDEX] = freq2;
				
				sendReport(freq_report);
				sendReport(PACKET_011e0000);
				sendReport(PACKET_01160000);
				sendReport(PACKET_01136943);
				sendReport(PACKET_01142020);
				System.err.println("Completed startup!");
				
				if(controlWindow != null) {
					controlWindow.updateUSBStatus(true);
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void startSession() {
		/*
		 * 01150000
		 * read
		 * 01120000
		 * read
		 * 011e0000
		 * read
		 * 01150000
		 * read
		 * 01220000
		 * read
		 * 01102141
		 * read
		 * 011e0000
		 * read
		 * 01160000
		 * read
		 * 01298080
		 * read
		 * 01150000
		 * read
		 * 01120000
		 * read
		 * 011e0000
		 * read
		 * 012a2141
		 * read
		 * read
		 * 01150000
		 * read
		 * 01220000
		 * read
		 * 01102141
		 * read
		 * 011e0000
		 * read
		 * 01160000
		 * read
		 * */
		if(dev != null) {
			try {

				byte[] freq_report = PACKET_01102141;
				freq_report[FREQ_1_INDEX] = freq1;
				freq_report[FREQ_2_INDEX] = freq2;
				
				sendReport(PACKET_01150000);
				sendReport(PACKET_01120000);
				sendReport(PACKET_011e0000);
				sendReport(PACKET_01150000);
				sendReport(PACKET_01220000);
				sendReport(freq_report);
				sendReport(PACKET_011e0000);
				sendReport(PACKET_01160000);
				sendReport(PACKET_01298080);
				sendReport(PACKET_01150000);
				sendReport(PACKET_01120000);
				sendReport(PACKET_011e0000);
				sendReport(PACKET_012a2141);
				sendReport(PACKET_01150000);
				sendReport(PACKET_01220000);
				sendReport(freq_report);
				sendReport(PACKET_011e0000);
				sendReport(PACKET_01160000);
				System.err.println("Completed start session!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	

	public void changeFreq(String one, String two) {
		/*
		 * 01150000
		 * */
		
		String freqs= "ABCD";
		int freq1 = freqs.indexOf(one);
		int freq2 = freqs.indexOf(two);

		byte freq_byte_1 = (byte) (0x21 + freq1);
		byte freq_byte_2 = (byte) (0x41 + freq2);
		
		System.out.println(freq1);
		
		
		if(dev != null) {
			try {
				byte[] freq_report = PACKET_01102141;
				freq_report[FREQ_1_INDEX] = freq_byte_1;
				freq_report[FREQ_2_INDEX] = freq_byte_2;
				
				sendReport(PACKET_01120000);
				sendReport(PACKET_011e0000);
				sendReport(PACKET_01150000);
				sendReport(PACKET_01220000);
				sendReport(freq_report); // actual freq change
				sendReport(PACKET_011e0000);
				sendReport(PACKET_01160000);
				sendReport(PACKET_01298080);
				
				System.err.println("Completed freq change!");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		freqSelector1.setSelectedItem(one);
		freqSelector2.setSelectedItem(two);
	}
	
	// time is in seconds
	private byte[] getTimeReport(int time, int totalVotes) {
		String minutes = "" + (time / 60) % 60;
		while(minutes.length() < 2) {
			minutes = "0" + minutes;
		}
		
		String seconds = "" + time % 60;
		while(seconds.length() < 2) {
			seconds = "0" + seconds;
		}
		
		String votes = "" + totalVotes;
		while(votes.length() < 5) {
			votes = " " + votes;
		}
		
		byte[] output = {0x01, 0x13, (byte) minutes.charAt(0), (byte) minutes.charAt(1), 0x3a, (byte) seconds.charAt(0), (byte) seconds.charAt(1), 0x20, 0x4e, 0x55, 0x4d, 0x20, 0x20, (byte) votes.charAt(0), (byte) votes.charAt(1), (byte) votes.charAt(2), (byte) votes.charAt(3), (byte) votes.charAt(4), 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, (byte) 0xa0};
		return output;
	}
	
	private byte[] getVoteCountReport(int aCount, int bCount, int cCount, int dCount, int eCount) {
		String aStr = "" + aCount;
		while(aStr.length() < 3) {
			aStr = " " + aStr;
		}
		
		String bStr = "" + bCount;
		while(bStr.length() < 3) {
			bStr = " " + bStr;
		}
		
		String cStr = "" + cCount;
		while(cStr.length() < 3) {
			cStr = " " + cStr;
		}
		
		
		String dStr = "" + dCount;
		while(dStr.length() < 3) {
			dStr = " " + dStr;
		}
		
		String eStr = "" + eCount;
		while(eStr.length() < 3) {
			eStr = " " + eStr;
		}
		
		byte[] output = {0x01, 0x14, 
				(byte) aStr.charAt(0), (byte) aStr.charAt(1), (byte) aStr.charAt(2),
				(byte) bStr.charAt(0), (byte) bStr.charAt(1), (byte) bStr.charAt(2),
				(byte) cStr.charAt(0), (byte) cStr.charAt(1), (byte) cStr.charAt(2),
				(byte) dStr.charAt(0), (byte) dStr.charAt(1), (byte) dStr.charAt(2),
				(byte) eStr.charAt(0), (byte) eStr.charAt(1), (byte) eStr.charAt(2), 
				0x20, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x48};
		return output;
	}
	
	private void sendReport(byte[] report) {
		dev.setOutputReport((byte) 0, report, report.length);

//		System.out.printf("onOutputReport: id %d len %d data ", 0, report.length);
//		for (int i = 0; i < report.length; i++) {
//			System.out.printf("%02X ", report[i]);
//			System.out.flush();
//		}
//		System.out.println();
		
		try {
			Thread.sleep(100);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void selectFrequency() {
		displayFrame.setVisible(true);
	}
	
	private void begin() {
		//Initialize displayFrame
		displayFrame = new JFrame();
		displayFrame.setTitle("Select Frequency");
		displayFrame.setAlwaysOnTop(true);

		
		SpringLayout layout = new SpringLayout();
		displayFrame.setLayout(layout);
		
		//add the course selection panel
		freqSelectionPanel = new JPanel();
	
		displayFrame.add(freqSelectionPanel);
		
		freqSelector1 = new JComboBox<String>();
		freqSelector1.setPreferredSize(new Dimension(35, 30));
		freqSelector1.addItem("A");
		freqSelector1.addItem("B");
		freqSelector1.addItem("C");
		freqSelector1.addItem("D");
		layout.putConstraint(SpringLayout.NORTH, freqSelector1, 50, SpringLayout.NORTH, freqSelectionPanel);
		freqSelector1.addActionListener(selectedCourse);

		freqSelectionPanel.add(freqSelector1);
		
		freqSelector2 = new JComboBox<String>();
		freqSelector2.setPreferredSize(new Dimension(35, 30));
		freqSelector2.addItem("A");
		freqSelector2.addItem("B");
		freqSelector2.addItem("C");
		freqSelector2.addItem("D");
		layout.putConstraint(SpringLayout.NORTH, freqSelector2, 90, SpringLayout.NORTH, freqSelectionPanel);
		freqSelector2.addActionListener(selectedCourse);
		
		freqSelectionPanel.add(freqSelector2);

		// add new session button
		confirmButton = new JButton("Confirm Frequency");
		confirmButton.addActionListener(confirmFrequency);
		freqSelectionPanel.add(confirmButton);
		
		
		freqSelectionPanel.validate();
		 
//		// set JFrame height, width -- HEIGHT ALGORITHM IS VERY WONKY, SHOULD FIX
//		int width = usernameBox.getX() + usernameBox.getWidth() + 25;
//		int height = loginButton.getY() + loginButton.getHeight() * 3 + 10;
		
		int width = 250;
		int height = 90;
		

		displayFrame.setSize(width, height);
		
		
		// Center the display on the screen
		displayFrame.setLocationRelativeTo(null);
		
		//displayFrame.pack();
		displayFrame.validate();
		displayFrame.setVisible(false);
	}
	
	public boolean isSelectorOpen() {
		return displayFrame.isVisible();
	}
	
	private ActionListener selectedCourse = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
//			updateCourseSelector();
		}
	};
	
	private ActionListener confirmFrequency = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			displayFrame.setVisible(false);
			String one = (String) freqSelector1.getSelectedItem();
			String two = (String) freqSelector2.getSelectedItem();
			System.out.println("Selected: " + one + ", " + two);
			
			changeFreq(one, two);
		}
	};
	
}