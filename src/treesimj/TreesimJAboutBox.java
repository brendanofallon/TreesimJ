/*
 * TreesimJAboutBox.java
 */

package treesimj;

import javax.swing.JLabel;


public class TreesimJAboutBox extends javax.swing.JDialog {

    public TreesimJAboutBox(java.awt.Frame parent) {
        super(parent);
        initComponents();
        getRootPane().setDefaultButton(closeButton);
    }

    public void closeAboutBox() {
        dispose();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {
    	this.getContentPane().add(new JLabel("Not implemented yet."));
       
    }
    
    private javax.swing.JButton closeButton;
    
}
