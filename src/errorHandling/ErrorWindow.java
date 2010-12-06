package errorHandling;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import treesimj.TJRunTimeException;
import treesimj.TreesimJView;

/**
 * A error window class that displays an error message from an exception and a stack trace if the user clicks on the button. Implemented
 * in static style so we can easily call ErrorWindow.show( someException ) from anywhere
 * @author brendan
 *
 */
public class ErrorWindow extends JFrame {

	JPanel mainPanel;
	JLabel mainMessageLabel;
	JButton okButton;
	JPanel cardPanel;
	JPanel emptyPanel;
	JPanel fillerPanel;
	
	JPanel stackPanel;
	JScrollPane stackScrollPane;
	JTextArea stackTA;
	JButton showButton;
	
	Font stackFont = new Font("Sans", Font.PLAIN, 11);
	Dimension textAreaSize = new Dimension(300, 200);
	
	String defaultMessage = "Sorry, but an error occurred and it may not be possible to complete this operation.";
	
	public ErrorWindow(Exception e) {
		this(e, null);
	}
	
	public ErrorWindow(Exception e, String message) {
		super("Error");
		setLayout(new BorderLayout());
		mainPanel = new JPanel();

		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 8, 4, 4));
		this.add(mainPanel);
		mainPanel.add(Box.createVerticalStrut(10));
		if (message == null)
			mainMessageLabel = new JLabel(defaultMessage);
		else
			mainMessageLabel = new JLabel(message);
		mainMessageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(mainMessageLabel);
		mainPanel.add(Box.createVerticalStrut(10));

		JLabel messageLabel = new JLabel("<html><strong>Message</strong><html> : " + e.getLocalizedMessage());
		messageLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(messageLabel);

		String exStr = e.getClass().toString();
		if (exStr.contains("."))	
			exStr = exStr.substring(exStr.lastIndexOf(".")+1);
		JLabel typeLabel = new JLabel("<html><strong>Type</strong><html> : " + exStr);
		typeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

		mainPanel.add(typeLabel);
		
		showButton = new JButton(" Show details ");
		showButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showButtonPressed();
			}
		});
		showButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		fillerPanel = new JPanel();
		fillerPanel.setLayout(new BoxLayout(fillerPanel, BoxLayout.PAGE_AXIS));
		fillerPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		stackPanel = new JPanel();
		stackPanel.setLayout(new BoxLayout(stackPanel, BoxLayout.PAGE_AXIS));
		stackPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		stackTA = new JTextArea();
		stackTA.setFont(stackFont);
		stackTA.setEditable(false);
		for(int i=0; i<e.getStackTrace().length; i++) {
			stackTA.append( e.getStackTrace()[i].toString() + "\n");	
		}
		stackScrollPane = new JScrollPane(stackTA);
		stackPanel.add(stackScrollPane);
		stackScrollPane.setPreferredSize(textAreaSize);
		stackScrollPane.setMaximumSize(new Dimension(1000, 200));
		stackScrollPane.setBorder(BorderFactory.createEmptyBorder());
		stackScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(showButton);
		mainPanel.add(fillerPanel);
		
		okButton = new JButton("  OK  ");		

		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(okButton);
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				okButtonClicked();
			}
		});
		
		pack();
	}
	
	protected void showButtonPressed() {
		fillerPanel.add(stackScrollPane);
		fillerPanel.setPreferredSize(textAreaSize);
		pack();
		repaint();
	}

	public void okButtonClicked() {
		setVisible(false);
	}

	/**
	 * Pop open an error window with the generic default message.  
	 * @param e
	 */
	public static void showErrorWindow(Exception e) {
		ErrorWindow window = new ErrorWindow(e, null);
		window.setLocationRelativeTo(null);
		window.setVisible(true);	
	}
	
	/**
	 * Pop open an error window with the supplied message. If message is null, a generic default will be used. 
	 * @param e
	 */
	public static void showErrorWindow(Exception e, String message) {
		ErrorWindow window = new ErrorWindow(e, message);
		window.setLocationRelativeTo(null);
		window.setVisible(true);	
	}

}
