package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;


public class RoundedBorderPanel extends JPanel {
	
	Color bColor = Color.LIGHT_GRAY;
	JComponent comp = null;			//The component to draw on the top of the border, if it exists
	Rectangle compBounds;	//Bounds of the component to draw
	Container parent;		//Parent of the component
	int compLeftOffset = 25;
	int compTopOffset = -3;
	int topBorderOffset = 12;
	Insets insets;
	JPanel topPanel;
	JPanel interiorPanel;
	
	
	public RoundedBorderPanel() {
		compBounds = new Rectangle();
		insets = new Insets(3, 3, 3, 3);
		super.setBorder(BorderFactory.createEmptyBorder(1,1,1,1));
		super.setLayout(new BorderLayout());
		topPanel = new JPanel();
		topPanel.setOpaque(false);
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(Box.createHorizontalStrut(compLeftOffset));
		super.add(topPanel, BorderLayout.NORTH);
		interiorPanel = new JPanel();
		interiorPanel.setOpaque(false);
		super.add(interiorPanel, BorderLayout.CENTER);
	}
	
	public void setLayout(LayoutManager lo) {
		if (interiorPanel != null)
			interiorPanel.setLayout(lo);
	}
	
	public Component add(Component c) {
		if (interiorPanel != null) 
			return interiorPanel.add(c);
		return null;
	}
	
	public void add(Component c, Object constraints) {
		if (interiorPanel != null)
			interiorPanel.add(c, constraints);
	}
	
	public void setBorderComponent(JComponent comp) {
		this.comp = comp;
		topPanel.add(comp);
		//comp.setBounds(compLeftOffset, compTopOffset, comp.getPreferredSize().width, comp.getPreferredSize().height);
		comp.setOpaque(false); //Otherwise the background border line will show through
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);

		g.setColor(bColor);
		
		g.drawRoundRect(1, topBorderOffset, getWidth()-2, getHeight()-topBorderOffset-1, 15, 15);
		
		if (comp!=null) {
			compBounds = comp.getBounds(compBounds);
			Color c = getBackground();
			c = new Color(c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f, 0.75f);
			g.setColor(c);
			g.fillRect(compLeftOffset, topBorderOffset, compBounds.width, 1);

		}
	}
}
