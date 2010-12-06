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

public class DefaultStatisticConfigurator extends JFrame {

	Statistic stat;
	JPanel mainPanel;
	JCheckBox useCustomFreqBox;
	JTextField customFreqField;
	JSpinner histoBins;
	JTextField histoMinField;
	JTextField histoMaxField;
	
	public DefaultStatisticConfigurator(Statistic stat) {
		super("Configure Data Collector");
		this.stat = stat;
	
		mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		mainPanel.setPreferredSize(new Dimension(300, 250));
			
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
		
		histoBins = new JSpinner();
		SpinnerNumberModel binsModel = new SpinnerNumberModel(stat.getHistoBins(), 1, 1000, 1);
		histoBins.setModel(binsModel);
		centerPanel.add(makePanel("Histogram bins:", histoBins));
		
		
		histoMinField = new JTextField(" ");
		if (stat.getUserHistoMin()!=null) 
			histoMinField.setText(String.valueOf(stat.getUserHistoMin()));
		histoMinField.setPreferredSize(new Dimension(100, 30));
		histoMinField.setMinimumSize(new Dimension(80, 1));	
		JPanel histoMinPanel = makePanel("Histogram minimum:", histoMinField);
		centerPanel.add(histoMinPanel);
		
		histoMaxField = new JTextField("  ");
		if (stat.getUserHistoMax()!=null) {
			histoMaxField.setText(String.valueOf(stat.getUserHistoMax()));
		}
		histoMaxField.setPreferredSize(new Dimension(100, 30));
		histoMaxField.setMinimumSize(new Dimension(80, 1));	
		JPanel histoMaxPanel = makePanel("Histogram maximum:", histoMaxField);
		centerPanel.add(histoMaxPanel);
		
		
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
		try {
			Double hMin = Double.parseDouble(histoMinField.getText());
			stat.setUserHistoMin(hMin);
		}
		catch (NumberFormatException nfe) {
			//Don't worry about it
		}
		
		try {
			Double hMax = Double.parseDouble(histoMaxField.getText());
			stat.setUserHistoMax(hMax);
		}
		catch (NumberFormatException nfe) {
			//Don't worry about it
		}
		
		
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
		
		stat.setUserHistoBins((Integer)histoBins.getValue());
		
		setVisible(false);
	}


	protected void cancel() {
		setVisible(false);
	}


	JButton cancelButton;
	JButton doneButton;
}
