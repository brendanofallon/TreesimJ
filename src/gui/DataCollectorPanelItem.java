package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import statistics.Statistic;
import treesimj.TreesimJView;


/**
 * A single entry in the data collectors list. 
 * @author brendan
 *
 */
public class DataCollectorPanelItem extends javax.swing.JPanel {

	DataCollectorsPanel parent;
	JTextField opField = null;
	String identifier;
	final JCheckBox cBox;
	Statistic stat;
	
	public DataCollectorPanelItem(TreesimJView tj, DataCollectorsPanel parent, final Statistic stat) {
		super();
		this.parent = parent;
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		this.stat = stat;
		cBox = new JCheckBox();
		this.add(cBox);
		this.identifier = stat.getIdentifier();
		cBox.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				updateStatus(cBox.isSelected(), stat);
				  
			}
		});
		JLabel lab = new JLabel(stat.getIdentifier());
		lab.setToolTipText(stat.getDescription());
		this.add( lab );
		this.add(Box.createGlue());

		if (stat.hasConfigurationTool()) {
			ImageIcon configIcon = TreesimJView.getIcon("icons/configIcon.png"); //new ImageIcon(tj.getIconPath() + "configIcon.png");

			JButton configButton = new JButton(configIcon);
			if (configIcon==null) {
				//System.err.println("Warning : Could not find some icons (DataCollectorPanel.configIcon, path: " + tj.getIconPath() + "), reverting to text");
				configButton = new JButton("Configure");
			}

			
			configButton.setBorder(null);
			configButton.setToolTipText("Configure options for this data collector");
			configButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JFrame configTool = stat.getConfigurationTool();
					configTool.setVisible(true);
				}			
			});
			this.add(configButton);
		}
	}

	
	public String getIdentifier() {
		return identifier;
	}
	
	public Statistic getStatistic() {
		return stat;
	}
	
	public void setBoxEnabled(boolean en) {
		if (! en) {
			setSelected(en);
		}
		cBox.setEnabled(en);
	}
	
	public void setSelected(boolean sel) {
		updateStatus(sel, stat);
		cBox.setSelected(sel);
	}
	
	protected void updateStatus(boolean selected, Statistic stat) {
		parent.updateActiveDataCollectors(selected, stat); 
	}
	
	public String getOptionString() {
		if (opField != null) {
			return opField.getText();
		}
		else 
			return null;
	}
	
}
