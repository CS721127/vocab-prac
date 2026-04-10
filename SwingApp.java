import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.Map;

public class SwingApp {
    private VSystem sys;
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private String currentListName = "default";
    
    // UI Components to refresh on language change
    private JMenu menuFile, menuLang, menuHelp;
    private JMenuItem itemNew, itemOpen, itemImport, itemSettings, itemAbout;
    private JButton btnAdd, btnDelete, btnPractice, btnClear;
    private JLabel statusLabel;
    private String[] columnNames;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingApp().createAndShowGUI());
    }

    public SwingApp() {
        // Load Config
        String userHome = System.getProperty("user.home");
        File configFile = new File(new File(userHome, ".vocabapp"), "config.properties");
        if (configFile.exists()) {
            try (java.io.FileInputStream in = new java.io.FileInputStream(configFile)) {
                java.util.Properties props = new java.util.Properties();
                props.load(in);
                String lang = props.getProperty("language", "en");
                LanguageManager.setLanguage(lang.equals("en"));
            } catch (java.io.IOException e) {
                e.printStackTrace();
            }
        }
        sys = new VSystem(currentListName);
    }

    public void createAndShowGUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(900, 600);
        frame.setLocationRelativeTo(null);

        // --- Menu Bar ---
        JMenuBar menuBar = new JMenuBar();
        
        menuFile = new JMenu();
        itemNew = new JMenuItem();
        itemNew.addActionListener(e -> createNewList());
        itemOpen = new JMenuItem();
        itemOpen.addActionListener(e -> openList());
        itemImport = new JMenuItem();
        itemImport.addActionListener(e -> importPreset());
        itemSettings = new JMenuItem();
        itemSettings.addActionListener(e -> new SettingsDialog(frame, this));
        
        menuFile.add(itemNew);
        menuFile.add(itemOpen);
        menuFile.addSeparator();
        menuFile.add(itemImport);
        menuFile.addSeparator();
        menuFile.add(itemSettings);
        
        menuLang = new JMenu();
        JMenuItem itemEn = new JMenuItem("English");
        itemEn.addActionListener(e -> changeLanguage(true));
        JMenuItem itemCn = new JMenuItem("中文 (Chinese)");
        itemCn.addActionListener(e -> changeLanguage(false));
        
        menuLang.add(itemEn);
        menuLang.add(itemCn);

        menuHelp = new JMenu("Help");
        itemAbout = new JMenuItem("About");
        itemAbout.addActionListener(e -> new AboutDialog(frame));
        menuHelp.add(itemAbout);

        menuBar.add(menuFile);
        menuBar.add(menuLang);
        menuBar.add(menuHelp);
        frame.setJMenuBar(menuBar);

        // --- Toolbar ---
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        btnAdd = new JButton();
        btnAdd.addActionListener(e -> showAddDialog());
        
        btnDelete = new JButton();
        btnDelete.addActionListener(e -> deleteSelected());

        btnPractice = new JButton();
        btnPractice.addActionListener(e -> showPracticeDialog());

        btnClear = new JButton();
        btnClear.setForeground(Color.RED);
        btnClear.addActionListener(e -> clearAllData());

        toolBar.add(btnAdd);
        toolBar.add(btnDelete);
        toolBar.addSeparator();
        toolBar.add(btnPractice);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnClear);

        frame.add(toolBar, BorderLayout.NORTH);

        // --- Table ---
        tableModel = new DefaultTableModel(0, 3) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(25);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));

        frame.add(new JScrollPane(table), BorderLayout.CENTER);

        // --- Status Bar ---
        statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createEtchedBorder());
        frame.add(statusLabel, BorderLayout.SOUTH);

        updateUIText(); // Set initial text
        refreshTable();

        frame.setVisible(true);
    }

    public void updateUIText() {
        frame.setTitle(LanguageManager.get("app.title") + " - [" + currentListName + "]");
        
        menuFile.setText(LanguageManager.get("menu.file"));
        itemNew.setText(LanguageManager.get("menu.new_list"));
        itemOpen.setText(LanguageManager.get("menu.open_list"));
        itemImport.setText(LanguageManager.get("menu.import"));
        itemSettings.setText("Settings"); 
        menuLang.setText(LanguageManager.get("menu.language"));
        
        btnAdd.setText(LanguageManager.get("btn.add"));
        btnDelete.setText(LanguageManager.get("btn.delete"));
        btnPractice.setText(LanguageManager.get("btn.practice"));
        btnClear.setText(LanguageManager.get("btn.reset"));
        
        statusLabel.setText(LanguageManager.get("status.ready"));

        columnNames = new String[]{
            LanguageManager.get("col.term"), 
            LanguageManager.get("col.def"), 
            LanguageManager.get("col.notes")
        };
        tableModel.setColumnIdentifiers(columnNames);
    }
    
    private void changeLanguage(boolean isEnglish) {
        LanguageManager.setLanguage(isEnglish);
        updateUIText();
        refreshTable(); // Re-render table header if needed
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Vocab> list = sys.getVocabs();
        for (Vocab v : list) {
            tableModel.addRow(new Object[]{v.getTerm(), v.getDefinition(), v.getNotes()});
        }
    }

    private void createNewList() {
        String name = JOptionPane.showInputDialog(frame, LanguageManager.get("menu.new_list"));
        if (name != null && !name.trim().isEmpty()) {
            currentListName = name.trim();
            sys = new VSystem(currentListName);
            updateUIText();
            refreshTable();
            JOptionPane.showMessageDialog(frame, LanguageManager.get("list.created", currentListName));
        }
    }

    private void openList() {
        List<String> lists = VSystem.getAvailableLists();
        if (lists.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "No lists found.");
            return;
        }
        String selected = (String) JOptionPane.showInputDialog(
            frame, 
            LanguageManager.get("menu.open_list"), 
            LanguageManager.get("menu.open_list"), 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            lists.toArray(), 
            currentListName
        );
        
        if (selected != null) {
            currentListName = selected;
            sys = new VSystem(currentListName);
            updateUIText();
            refreshTable();
            JOptionPane.showMessageDialog(frame, LanguageManager.get("list.loaded", currentListName));
        }
    }

    private void importPreset() {
        Map<String, List<Vocab>> presets = PresetLists.getPresets();
        String selected = (String) JOptionPane.showInputDialog(
            frame, 
            LanguageManager.get("menu.import"), 
            LanguageManager.get("menu.import"), 
            JOptionPane.QUESTION_MESSAGE, 
            null, 
            presets.keySet().toArray(), 
            null
        );
        
        if (selected != null) {
            currentListName = selected;
            sys = new VSystem(currentListName);
            // Populate
            List<Vocab> data = presets.get(selected);
            for (Vocab v : data) {
                sys.addVocab(v.getTerm(), v.getDefinition(), v.getNotes());
            }
            updateUIText();
            refreshTable();
            JOptionPane.showMessageDialog(frame, LanguageManager.get("list.imported", currentListName));
        }
    }

    private void showAddDialog() {
        JDialog dialog = new JDialog(frame, LanguageManager.get("dialog.add.title"), true);
        dialog.setLayout(new GridLayout(4, 2, 10, 10));
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(frame);

        JTextField txtTerm = new JTextField();
        JTextField txtDef = new JTextField();
        JTextField txtNotes = new JTextField();

        dialog.add(new JLabel("  " + LanguageManager.get("col.term") + ":"));
        dialog.add(txtTerm);
        dialog.add(new JLabel("  " + LanguageManager.get("col.def") + ":"));
        dialog.add(txtDef);
        dialog.add(new JLabel("  " + LanguageManager.get("col.notes") + ":"));
        dialog.add(txtNotes);

        JButton btnSave = new JButton(LanguageManager.get("dialog.add.save"));
        btnSave.addActionListener(e -> {
            String t = txtTerm.getText().trim();
            String d = txtDef.getText().trim();
            String n = txtNotes.getText().trim();
            if (!t.isEmpty() && !d.isEmpty()) {
                if (sys.addVocab(t, d, n)) {
                    refreshTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, LanguageManager.get("msg.term_exists"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog, LanguageManager.get("msg.required"), "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        dialog.add(new JLabel("")); // Spacer
        dialog.add(btnSave);

        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String term = (String) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(frame, LanguageManager.get("msg.confirm_delete", term), "Confirm", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                sys.deleteVocab(term);
                refreshTable();
            }
        } else {
            JOptionPane.showMessageDialog(frame, "Please select a row to delete.");
        }
    }

    private void clearAllData() {
        int confirm = JOptionPane.showConfirmDialog(frame, 
            LanguageManager.get("msg.confirm_reset"), 
            "Reset All", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
            
        if (confirm == JOptionPane.YES_OPTION) {
            sys.clearAllData();
            refreshTable();
        }
    }

    private void showPracticeDialog() {
        if (sys.getVocabs().isEmpty()) {
            JOptionPane.showMessageDialog(frame, LanguageManager.get("msg.practice_empty"));
            return;
        }
        new GameDialog(frame, sys);
    }
}
