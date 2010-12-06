package statistics;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A very simple statistic configuration tool which just allows the setting of sample frequency, that's it. 
 * @author brendan
 *
 */
public class NoHistoConfigTool extends JFrame {
	Statistic stat;
	JPanel mainPanel;
	JCheckBox useCustomFreqBox;
	JTextField customFreqField;

	
	public NoHistoConfigTool(Statistic stat) {
		super("Configure Data Collector");
		this.stat = stat;
	
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.setPreferredSize(new Dimension(300, 120));
			
		JPanel centerPanel = new JPanel();
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.add(Box.createVerticalStrut(10));
		useCustomFreqBox = new JCheckBox("Use custom data collection frequency");
		useCustomFreqBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		centerPanel.add(useCustomFreqBox);
		useCustomFreqBox.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				if (useCustomFreqBox.isSelected()) {
					customFreqField.setEnabled(true);
					customFreqField.setEditable(true);
				}
				else {
					customFreqField.setEnabled(false);
					customFreqField.setEditable(false);
				}
			}			
		});
		
		JPanel freqPanel = new JPanel();
		freqPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		freqPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		customFreqField = new JTextField(String.valueOf(1000));
		customFreqField.setPreferredSize(new Dimension(80, 30));
		customFreqField.setMinimumSize(new Dimension(80, 1));

		if (stat.hasUserSuppliedSampleFrequency) {
			customFreqField.setText(String.valueOf(stat.getSampleFrequency()));
			useCustomFreqBox.setSelected(true);
		}
		else {
			useCustomFreqBox.setSelected(false);
			customFreqField.setEditable(false);
			customFreqField.setEnabled(false);
		}
		freqPanel.add(new JLabel("Collection frequency:"));
		freqPanel.add(customFreqField);
		
		centerPanel.add(freqPanel);
		
		centerPanel.add(Box.createVerticalStrut(10));
		
		
		
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancel();
			}
		});
		bottomPanel.add(cancelButton, BorderLayout.WEST);
		
		doneButton = new JButton("Done");
		doneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				done();
			}
		});
		bottomPanel.add(doneButton, BorderLayout.EAST);
		
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		add(mainPanel);
		
		pack();
		this.getRootPane().setDefaultButton(doneButton);
		setLocationRelativeTo(null);
	}
	
	private JPanel makePanel(String label, JComponent comp) {
		JPanel p = new JPanel();
		p.setLayout(new FlowLayout(FlowLayout.LEFT));
		p.setAlignmentX(Component.LEFT_ALIGNMENT);
		p.add(new JLabel(label));
		p.add(comp);
		return p;
	}
	
	protected void done() {
		
		if (useCustomFreqBox.isSelected()) {
			try {
				Integer freq = Integer.parseInt(customFreqField.getText());
				stat.setSampleFrequency(freq);
			}
			catch (NumberFormatException nfe) {
				//Don't worry about it
			}
		}
		else {
			stat.unsetUserSampleFrequency();
		}
		
		setVisible(false);
	}


	protected void cancel() {
		setVisible(false);
	}


	JButton cancelButton;
	JButton doneButton;
}
