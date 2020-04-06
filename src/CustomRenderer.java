/**
 * Using this class allows the chart to use different colors for the bars.
 * 
 * @author Yishai
 * @version 1.0
 * @since 2019-08-19
 */

import java.awt.Color;
import java.awt.*;
import org.jfree.chart.renderer.category.*;

public class CustomRenderer extends BarRenderer {
	//Ignore
	private static final long serialVersionUID = -7318293578019549312L;

	//The array of colors to be used by the renderer
	private Paint[] colors;
	
	/**
	 * Used to construct the renderer
	 * @param colors an array of colors
	 */
    public CustomRenderer(Paint[] colors) {
        this.colors = colors;
    }

    /**
     * Used to get the color correlating to the column or 
     * return orange if not set. and column
     */
    public Paint getItemPaint(final int row, final int column) {
    	if(column < colors.length)
            return colors[column];
        else  
            return Color.ORANGE;
   }
}