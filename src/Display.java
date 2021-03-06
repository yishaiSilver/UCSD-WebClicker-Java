/**
 * This class creates a JFrame and populates it with a JFreeChart 
 * bar chart. 
 * 
 * @author Yishai
 * @version 1.0
 * @since 2019-08-19
 */
import java.awt.Color;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.*;
import org.jfree.data.category.CategoryDataset; 
import org.jfree.data.category.DefaultCategoryDataset;

public class Display extends JFrame {

	//Ignore
	private static final long serialVersionUID = -2932614175973594471L;
	
	private static Question question;
	
	//The JFrame used to display everything and its characteristics
	private static JFrame displayFrame;
	private static final int WINDOW_WIDTH = 500;
	private static final int WINDOW_HEIGHT = 300;
	private static final String WINDOW_TITLE = "Student Responses";

	//The colors used for the bars in the bar chart
	private static final Color A_COLOR = new Color(70,1,155);
	private static final Color B_COLOR = new Color(0,126,254);
	private static final Color C_COLOR = new Color(0,187,0);
	private static final Color D_COLOR = new Color(254,246,1);
	private static final Color E_COLOR = new Color(221,0,0);
	private Color[] colors;
	
	//The multiple choice options
	private static final String A = "A";
	private static final String B = "B";
	private static final String C = "C";
	private static final String D = "D";
	private static final String E = "E";
	
	public static final int OKAY_BUTTON = 0;
	public static final int CANCEL_BUTTON = 1;
	public static final int CLOSE_BUTTON = -1;
	
	//The number of students who have voted for each option
	private static int aCount;
	private static int bCount;
	private static int cCount;
	private static int dCount;
	private static int eCount;
	
	//The chart
	private static JFreeChart chart;
	
	//The data used by the bar chart
	private static DefaultCategoryDataset data;
	
	//Boolean for whether or not USB is connected
//	private static boolean connected = false;
	
	private ControlWindow controller;
	
	/**
	 * Used to initialize the display's variables.
	 */
	public Display() {
		//Initialize variables
		question = new Question();
		data = new DefaultCategoryDataset();
		colors = new Color[] {
				A_COLOR,
				B_COLOR,
				C_COLOR,
				D_COLOR,
				E_COLOR
			};
		
		
		begin();
		closeDisplay();
	}
	
	/**
	 * Used to initialize the JFrame.
	 */
	public void begin() {
		//Initialize displayFrame
		displayFrame = new JFrame();
		displayFrame.setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		displayFrame.setTitle(WINDOW_TITLE);
		displayFrame.setAlwaysOnTop(true);
		displayFrame.setFocusableWindowState(false);
		displayFrame.setLayout(new BorderLayout());
		
		//Save session on exit.
		displayFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		displayFrame.addWindowListener(windowListener);
		
		// Center the display on the screen
		displayFrame.setLocationRelativeTo(null);
		
		
		
		//Add the chart to displayFrame
		displayFrame.setContentPane(getChart());
		displayFrame.validate();
		displayFrame.setVisible(true);
		
		//Close the display
		//closeDisplay();
	}
	
	public void setController(ControlWindow controller) {
		this.controller = controller;
	}
	
	private WindowListener windowListener = new WindowListener() {
		@Override
        public void windowClosing(WindowEvent e) {
			closeDisplay();
			controller.toggleDisplay(false, false);
        }
		
		@Override
        public void windowIconified(WindowEvent e) {
			if(controller != null) {
				controller.toggleDisplay(false, false);
			}
		}
		
        public void windowOpened(WindowEvent e) {}
        public void windowClosed(WindowEvent e) {}
        public void windowDeiconified(WindowEvent e) {}
        public void windowActivated(WindowEvent e) {}
        public void windowDeactivated(WindowEvent e) {}
	};
	
	public int getNumResponses() {
		return aCount + bCount + cCount + dCount + eCount;
	}
	
	public int[] getVotes() {
		return new int[] {aCount, bCount, cCount, dCount, eCount};
	}
	
	/**
	 * Used to log a student's response.
	 * 
	 * @param studentID the student's ID
	 * @param response the student's response (A,B,C,D, or E)
	 */
	public boolean newResponse(String studentID, String response, boolean shouldUpdateCount) {
//		boolean output = false;
		boolean newStudent = true;
		
		//Get extant responses
		ArrayList<Response> responses = question.getResponses();
		
		//loop through all extant responses
		for(int i = 0; i < responses.size(); i ++) {
			//if student is updating response, act accordingly, exit
			if(responses.get(i).getStudentID().equals(studentID)) {
				
				// if the response already exists, don't do anything
				if(responses.get(i).getResponse().contentEquals(response)) {
					return false;
				}
				
				responses.get(i).setResponse(response);
				newStudent = false;
				break;
			}
		}

		//new student, new response
		if(newStudent) {
			Response newResponse = new Response(studentID, response); 
			responses.add(newResponse);
		}
		
		if(shouldUpdateCount) {
			updateDataset();
		 }
		
		return true;
	}
	
	/**
	 * Used to count number of responses correlating to each option
	 * and represent such data in a way compatible with JFreeChart
	 *
	 * @return the data-set to be displayed by JFreeChart 
	 */
	public static CategoryDataset updateDataset() {
		//reset number of respective responses
		resetCount();
		
		//get number of respective responses
		for(Response response : question.getResponses()) {
			switch(response.getResponse()){
				case A:
					aCount++;
					break;
				case B:
					bCount++;
					break;
				case C:
					cCount++;
					break;
				case D:
					dCount++;
					break;
				case E:
					eCount++;
					break;
			}
		}
		
		float total = aCount + bCount + cCount + dCount + eCount;
		
		float aVal = (total == 0) ? 0 : aCount / total;
		float bVal = (total == 0) ? 0 : bCount / total;
		float cVal = (total == 0) ? 0 : cCount / total;
		float dVal = (total == 0) ? 0 : dCount / total;
		float eVal = (total == 0) ? 0 : eCount / total;

		//Set the chart's values to respective counts
		data.setValue(aVal, "Options", A);
		data.setValue(bVal, "Options", B);
		data.setValue(cVal, "Options", C);
		data.setValue(dVal, "Options", D);
		data.setValue(eVal, "Options", E);
		
		//return the data-set
		return data;
	}

	/**
	 * Used to count number of responses correlating to each option
	 * and represent such data in a way compatible with JFreeChart
	 *
	 * @return the data-set to be displayed by JFreeChart 
	 */
	public CategoryDataset updateDataset(int[] votes) {
		//reset number of respective responses
		resetCount();
		
		float total = 0;
		for(int i : votes) {
			total += i;
		}
		
		aCount = votes[0];
		bCount = votes[1];
		cCount = votes[2];
		dCount = votes[3];
		eCount = votes[4];

		float aVal = (total == 0) ? 0 : aCount / total;
		float bVal = (total == 0) ? 0 : bCount / total;
		float cVal = (total == 0) ? 0 : cCount / total;
		float dVal = (total == 0) ? 0 : dCount / total;
		float eVal = (total == 0) ? 0 : eCount / total;
		
		//Set the chart's values to respective counts
		data.setValue(aVal, "Options", A);
		data.setValue(bVal, "Options", B);
		data.setValue(cVal, "Options", C);
		data.setValue(dVal, "Options", D);
		data.setValue(eVal, "Options", E);
		
		//return the data-set
		return data;
	}
	
	/**
	 * Used to create the chart.
	 * 
	 * @return a panel containing the chart
	 */
	public ChartPanel getChart() {
		//Change the bar graph image to be simple
		ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
		BarRenderer.setDefaultShadowsVisible(false);
		
		//Get the data set
		CategoryDataset data = updateDataset();
		//Create a nameless, titleless chart with data
		chart = ChartFactory.createBarChart(
				"",
				"",
				"",
				data,
				PlotOrientation.VERTICAL,
				false,
				true,
				false
				);
		
		//Update title
		//setConnected(connected);
		
		//Modify the chart's grid-lines
		CategoryPlot plot = chart.getCategoryPlot();
		plot.setRangeGridlinePaint(Color.BLACK);
		plot.setRangeGridlinesVisible(true);
		plot.getRangeAxis().setLowerBound(0);
		plot.getRangeAxis().setUpperBound(1);
        NumberAxis xAxis2 = (NumberAxis) plot.getRangeAxis();
        xAxis2.setNumberFormatOverride(NumberFormat.getPercentInstance());
		
		//Use custom colors when plotting chart
		plot.setRenderer(new CustomRenderer(colors));
		
		//Create panel with chart
		ChartPanel chartPanel = new ChartPanel(chart);
		
		chartPanel.setDomainZoomable(false);
		chartPanel.setRangeZoomable(false);
		
		return chartPanel;
	}
	
	/**
	 * Used to open the display
	 */
	public void openDisplay() {
		displayFrame.setState(Frame.NORMAL);
	}
	
	/**
	 * Used to hide the display
	 */
	public void closeDisplay() {
		displayFrame.setState(Frame.ICONIFIED);
	}
	
	public boolean isOpen() {
		return displayFrame.getState() == Frame.NORMAL;
	}
	
	/**
	 * Used to reset the data in the chart
	 */
	public static void resetCount() {
		aCount = 0;
		bCount = 0;
		cCount = 0;
		dCount = 0;
		eCount = 0;
	}
	
	/**
	 * Used to record all responses and then reset said responses.
	 */
	public void nextQuestion() {
		question = new Question();
		updateDataset();
	}

	/**
	 * Used to change the title of the bar chart / notify 
	 * of not being connected
	 * 
	 * @param connectedArg boolean to represent being connected
	 *
	public static void setConnected(boolean connectedArg) {
		//Chart may not be initialized at beginning of program,
		//ignore error.
		try {
			if(connectedArg) {
				chart.setTitle("");
			}
			else {
				chart.setTitle("Not connected!");
			}
		} catch (Exception e) {
			System.out.println("Error setting chart title.");
		}
		
		connected = connectedArg;
	}*/
}

