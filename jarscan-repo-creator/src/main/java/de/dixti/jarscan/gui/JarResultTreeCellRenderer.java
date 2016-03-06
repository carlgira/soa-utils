package de.dixti.jarscan.gui;

import de.dixti.jarscan.Result;
import java.awt.Component;

import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;


public class JarResultTreeCellRenderer extends DefaultTreeCellRenderer {

	public JarResultTreeCellRenderer() {
		super();
		// TODO unterschiedliche Icons
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean sel, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {
		if (value instanceof DefaultMutableTreeNode) {
			DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
			Object o = node.getUserObject();
			if (o instanceof Result) {
				Result result = (Result) o;
				value = result.getPath();
			}
		}
		return super.getTreeCellRendererComponent(tree, value, sel, expanded,
				leaf, row, hasFocus);
	}
}
