package siteConfigurators;

import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JLabel;
import javax.swing.JTextField;


/**
 * A helper class for the general discrete fitness configurator; this implements a single
 * panel allowing the user to select the start position and selection intensity of a 
 * class of selected sites.
 * 
 * @author brendan
 *
 */
public class SiteClassPanel extends javax.swing.JPanel {

	JTextField start;
	JTextField selection;
	
	public SiteClassPanel() {
		setOpaque(false);
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel("Start:"));
		start = new JTextField();
		start.setPreferredSize(new Dimension(60, 24));
		add(start);
		add(new JLabel("Selection coeff:"));
		selection = new JTextField();
		selection.setPreferredSize(new Dimension(60, 24));
		add(selection);
	}
	
	public SiteClassPanel(int startSite, double sel) {
		setOpaque(false);
		setLayout(new FlowLayout(FlowLayout.LEFT));
		add(new JLabel("Start:"));
		start = new JTextField(String.valueOf(startSite));
		start.setPreferredSize(new Dimension(60, 24));
		add(start);
		add(new JLabel("Selection coeff:"));
		selection = new JTextField(String.valueOf(sel));
		selection.setPreferredSize(new Dimension(60, 24));
		add(selection);
	}
	
	public JTextField getStartField() {
		return start;
	}
	
	public int getStart() {
		return Integer.parseInt(start.getText());
	}
	
	public double getSelectionCoeff() {
		return Double.parseDouble(selection.getText());
	}
	
	public void setStart(int startPos) {
		start.setText(String.valueOf(startPos));
	}
	
	public void setSelection(double s) {
		selection.setText(String.valueOf(s));
	}
}
