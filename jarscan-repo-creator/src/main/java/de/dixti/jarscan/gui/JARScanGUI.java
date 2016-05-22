package de.dixti.jarscan.gui;

import com.thomaskuenneth.tkfolderselector.TKFolderSelector;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

import de.dixti.jarscan.JarScan;
import de.dixti.jarscan.Options;
import de.dixti.jarscan.Result;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.UIManager;

public class JARScanGUI extends JFrame implements ActionListener {

    private static final String TITLE = Messages.getString("TITLE");
    private static final String CHECKBOX_RECURSE_ARCHIVES = Messages.getString("CHECKBOX_RECURSE_ARCHIVES");
    private static final String BUTTON_SEARCH = Messages.getString("BUTTON_SEARCH");
    /**
     * used to select the base directory
     */
    private TKFolderSelector selector;
    /**
     * Shall embedded archives be scanned?
     */
    private JCheckBox checkboxRecurseArchives;
    /**
     * text field to enter the search string
     */
    private JTextField textfieldSearch;
    /**
     * button to start the scan
     */
    private JButton buttonSearch;
    /**
     * tree view of the search results
     */
    private JTree tree;
    /**
     * the model for the tree contains the search results
     */
    private DefaultTreeModel treeModel;
    /**
     * the current base directory
     */
    private File baseDir;

    public JARScanGUI() {
        super(TITLE);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setupDefaultValues();
        buildGUI();
        setVisible(true);
    }

    /**
     * sets up some default values
     */
    private void setupDefaultValues() {
        setBaseDir(System.getProperty("user.dir"));
    }

    /**
     * builds the user interface
     */
    private void buildGUI() {
        // the content pane
        JPanel cp = new JPanel(new BorderLayout());
        cp.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        // choose the base directory
        selector = new TKFolderSelector(getBaseDir());
        selector.addActionListener(this);
        cp.add(selector, BorderLayout.NORTH);
        // panel for search string and button
        JPanel panelSearch = new JPanel(new BorderLayout(10, 0));
        textfieldSearch = new JTextField();
        textfieldSearch.setActionCommand(BUTTON_SEARCH);
        textfieldSearch.addActionListener(this);
        panelSearch.add(textfieldSearch, BorderLayout.CENTER);
        buttonSearch = new JButton(BUTTON_SEARCH);
        buttonSearch.addActionListener(this);
        panelSearch.add(buttonSearch, BorderLayout.EAST);
        // panel for checkbox and panelSearch
        JPanel panelGridLayout = new JPanel(new GridLayout(2, 1, 0, 10));
        panelGridLayout.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        // include embedded archives?
        checkboxRecurseArchives = new JCheckBox(CHECKBOX_RECURSE_ARCHIVES);
        panelGridLayout.add(checkboxRecurseArchives);
        panelGridLayout.add(panelSearch);
        // panel for panelGridLayout and scrollpaneResults
        JPanel panelMainArea = new JPanel(new BorderLayout());
        panelMainArea.add(panelGridLayout, BorderLayout.NORTH);
        // the result area
        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode());
        tree = new JTree(treeModel);
        tree.setShowsRootHandles(true);
        tree.setRootVisible(false);
        tree.setCellRenderer(new JarResultTreeCellRenderer());
        JScrollPane scrollpaneResults = new JScrollPane(tree);
        panelMainArea.add(scrollpaneResults, BorderLayout.CENTER);
        // putting it all together
        cp.add(panelMainArea, BorderLayout.CENTER);
        setContentPane(cp);
        pack();
    }

    private String getSearchString() {
        return textfieldSearch.getText();
    }

    private void setBaseDir(String file) {
        this.baseDir = new File(file);
    }

    private File getBaseDir() {
        return baseDir;
    }

    public boolean getRecurseArchives() {
        return checkboxRecurseArchives.isSelected();
    }

    private void search() {
        // prepare the tree
        MutableTreeNode parent = new DefaultMutableTreeNode();
        treeModel.setRoot(parent);
        DefaultMutableTreeNode childNode = null;
        // prepare search and then carry it out
        Options options = new Options(getSearchString());
        options.setDir(getBaseDir());
        options.setRecursive(getRecurseArchives());
        JarScan engine = new JarScan(options);
        Result result = engine.scan();
        List<Result> list = result.getResultList();
        for (Result element : list) {
            // create new node
            childNode = new DefaultMutableTreeNode(element);
            treeModel.insertNodeInto(childNode, parent, parent.getChildCount());
            // and its children
            if (element.getHitCount() > 0) {
                List<Result> hits = element.getFilesWithHit();
                for (Result hit : hits) {
                    DefaultMutableTreeNode node = new DefaultMutableTreeNode(
                            hit);
                    treeModel.insertNodeInto(node, childNode, childNode.getChildCount());
                }
            }
        }
        // display the topmost nodes
        if (childNode != null) {
            tree.scrollPathToVisible(new TreePath(childNode.getPath()));
        }
    }

    /**
     * obligatory entry point to the program
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Throwable thr) {
        }
        new JARScanGUI();
    }

    //////////////////////////////
    // ActionListener interface //
    //////////////////////////////
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        if (BUTTON_SEARCH.equals(cmd)) {
            search();
        } else if (selector.equals(e.getSource())) {
            setBaseDir(e.getActionCommand());
        }
    }
}
