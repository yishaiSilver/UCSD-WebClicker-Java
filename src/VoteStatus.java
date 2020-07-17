
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class VoteStatus extends JFrame {

	private static final long serialVersionUID = 785973425627149560L;

	public JFrame displayFrame;

	public JLabel numResponsesText;

	public VoteStatus() {

		this.displayFrame = new JFrame("Incomming Votes");
		numResponsesText = new JLabel();
		numResponsesText.setText("Poll Running, 0 responses.");
		JPanel p = new JPanel();
		numResponsesText.setFont(numResponsesText.getFont().deriveFont(48.0f));
		p.add(numResponsesText);
		displayFrame.add(p);
		displayFrame.pack();
		displayFrame.setAlwaysOnTop(true);
		displayFrame.setFocusableWindowState(false);
		displayFrame.setLocation((int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration().getBounds().getMaxX() / 2 - displayFrame.getWidth() / 2), 0);
		displayFrame.setVisible(false);

	}

	public void view(boolean v) {
		if (v) {
			displayFrame.setVisible(true);
		} else {
			displayFrame.setVisible(false);
		}

	}

	public void setText(String str) {
		numResponsesText.setText(str);
		displayFrame.pack();
		displayFrame.setLocation((int) (GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice()
				.getDefaultConfiguration().getBounds().getMaxX() / 2 - displayFrame.getWidth() / 2), 0);
	}

}
