import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * SwingApp — Main UI for Vocab Master.
 *
 * New features:
 *  - 4-column table: Term, Definition, Example, Notes
 *  - Definition column truncated at 80 chars; click row to expand full detail
 *  - Search bar: searches locally; if no result offers AI (Qwen) search
 *  - Export to CSV (Excel) and to printable HTML (PDF)
 *  - Import from CSV/Excel and MDX-style tab-delimited files
 *  - AI Enhance button: enhances all vocab definitions + adds examples
 *  - Practice mode (GameDialog) with order selection + wrong-word retry
 */
public class SwingApp {
    private VSystem sys;
    private JFrame frame;
    private JTable table;
    private DefaultTableModel tableModel;
    private String currentListName = "default";
    private boolean isSearchFiltered = false;

    // ── Menu / Toolbar refs for language refresh ──────────────────────────────
    private JMenu menuFile, menuLang, menuHelp;
    private JMenuItem itemNew, itemOpen, itemImport;
    private JMenuItem itemImportCsv, itemImportMdx;
    private JMenuItem itemExportCsv, itemExportPdf;
    private JMenuItem itemSettings, itemAbout;
    private JButton btnAdd, btnDelete, btnPractice, btnAiEnhance, btnClear;
    private JLabel statusLabel;
    private JTextField searchField;
    private JButton btnSearch, btnSearchClear;
    private String[] columnNames;

    // ── Definition truncation length ──────────────────────────────────────────
    private static final int DEF_TRUNCATE = 80;

    // ─────────────────────────────────────────────────────────────────────────

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingApp().createAndShowGUI());
    }

    public SwingApp() {
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

    // ─────────────────────────────────────────────────────────────────────────
    // GUI Construction
    // ─────────────────────────────────────────────────────────────────────────

    public void createAndShowGUI() {
        frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 660);
        frame.setLocationRelativeTo(null);

        buildMenuBar();
        buildTopPanel();   // toolbar + search bar
        buildTable();
        buildStatusBar();

        updateUIText();
        refreshTable();
        frame.setVisible(true);
    }

    private void buildMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File menu
        menuFile = new JMenu();

        itemNew = new JMenuItem();
        itemNew.addActionListener(e -> createNewList());

        itemOpen = new JMenuItem();
        itemOpen.addActionListener(e -> openList());

        itemImport = new JMenuItem();
        itemImport.addActionListener(e -> importPreset());

        itemImportCsv = new JMenuItem();
        itemImportCsv.addActionListener(e -> importFromCsv());

        itemImportMdx = new JMenuItem();
        itemImportMdx.addActionListener(e -> importFromMdx());

        itemExportCsv = new JMenuItem();
        itemExportCsv.addActionListener(e -> exportToCsv());

        itemExportPdf = new JMenuItem();
        itemExportPdf.addActionListener(e -> exportToPdf());

        itemSettings = new JMenuItem();
        itemSettings.addActionListener(e -> new SettingsDialog(frame, this));

        menuFile.add(itemNew);
        menuFile.add(itemOpen);
        menuFile.addSeparator();
        menuFile.add(itemImport);
        menuFile.add(itemImportCsv);
        menuFile.add(itemImportMdx);
        menuFile.addSeparator();
        menuFile.add(itemExportCsv);
        menuFile.add(itemExportPdf);
        menuFile.addSeparator();
        menuFile.add(itemSettings);

        // Language menu
        menuLang = new JMenu();
        JMenuItem itemEn = new JMenuItem("English");
        itemEn.addActionListener(e -> changeLanguage(true));
        JMenuItem itemCn = new JMenuItem("中文 (Chinese)");
        itemCn.addActionListener(e -> changeLanguage(false));
        menuLang.add(itemEn);
        menuLang.add(itemCn);

        // Help menu
        menuHelp = new JMenu("Help");
        itemAbout = new JMenuItem("About");
        itemAbout.addActionListener(e -> new AboutDialog(frame));
        menuHelp.add(itemAbout);

        menuBar.add(menuFile);
        menuBar.add(menuLang);
        menuBar.add(menuHelp);
        frame.setJMenuBar(menuBar);
    }

    private void buildTopPanel() {
        JPanel topContainer = new JPanel(new BorderLayout(0, 0));

        // ── Toolbar ───────────────────────────────────────────────────────────
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));

        btnAdd = new JButton();
        btnAdd.addActionListener(e -> showAddDialog());
        styleToolbarBtn(btnAdd, new Color(34, 139, 34));

        btnDelete = new JButton();
        btnDelete.addActionListener(e -> deleteSelected());

        btnPractice = new JButton();
        btnPractice.addActionListener(e -> showPracticeDialog());
        styleToolbarBtn(btnPractice, new Color(79, 70, 229));

        btnAiEnhance = new JButton();
        btnAiEnhance.addActionListener(e -> runAiEnhance());
        styleToolbarBtn(btnAiEnhance, new Color(180, 80, 200));

        btnClear = new JButton();
        btnClear.setForeground(Color.RED);
        btnClear.addActionListener(e -> clearAllData());

        toolBar.add(btnAdd);
        toolBar.addSeparator();
        toolBar.add(btnDelete);
        toolBar.addSeparator();
        toolBar.add(btnPractice);
        toolBar.addSeparator();
        toolBar.add(btnAiEnhance);
        toolBar.add(Box.createHorizontalGlue());
        toolBar.add(btnClear);

        // ── Search Bar ────────────────────────────────────────────────────────
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        searchPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Color.LIGHT_GRAY));

        searchField = new JTextField(28);
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.addActionListener(e -> doSearch());

        btnSearch = new JButton();
        btnSearch.addActionListener(e -> doSearch());
        styleToolbarBtn(btnSearch, new Color(70, 130, 180));

        btnSearchClear = new JButton();
        btnSearchClear.addActionListener(e -> clearSearch());
        btnSearchClear.setEnabled(false);

        searchPanel.add(new JLabel("🔍"));
        searchPanel.add(searchField);
        searchPanel.add(btnSearch);
        searchPanel.add(btnSearchClear);

        topContainer.add(toolBar, BorderLayout.NORTH);
        topContainer.add(searchPanel, BorderLayout.SOUTH);
        frame.add(topContainer, BorderLayout.NORTH);
    }

    private void styleToolbarBtn(JButton btn, Color color) {
        btn.setForeground(color);
        btn.setFont(btn.getFont().deriveFont(Font.BOLD));
    }

    private void buildTable() {
        tableModel = new DefaultTableModel(0, 4) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        table = new JTable(tableModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(28);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
        table.setFont(new Font("SansSerif", Font.PLAIN, 14));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(true);
        table.setGridColor(new Color(230, 230, 235));
        table.setIntercellSpacing(new Dimension(1, 1));

        // Column widths
        table.getColumnModel().getColumn(0).setPreferredWidth(160);  // Term
        table.getColumnModel().getColumn(1).setPreferredWidth(300);  // Definition
        table.getColumnModel().getColumn(2).setPreferredWidth(240);  // Example
        table.getColumnModel().getColumn(3).setPreferredWidth(120);  // Notes

        // Custom renderer for truncated definition column (col 1) and example col (col 2)
        TruncatedCellRenderer truncRenderer = new TruncatedCellRenderer(DEF_TRUNCATE);
        table.getColumnModel().getColumn(1).setCellRenderer(truncRenderer);
        table.getColumnModel().getColumn(2).setCellRenderer(new TruncatedCellRenderer(60));

        // Double-click to expand detail dialog
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = table.rowAtPoint(e.getPoint());
                    if (row >= 0) showDetailDialog(row);
                }
            }
        });

        // Alternating row colors
        table.setDefaultRenderer(Object.class, new AlternatingRowRenderer());

        frame.add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void buildStatusBar() {
        statusLabel = new JLabel();
        statusLabel.setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
        statusLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        frame.add(statusLabel, BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI Text / Language
    // ─────────────────────────────────────────────────────────────────────────

    public void updateUIText() {
        frame.setTitle(LanguageManager.get("app.title") + " — [" + currentListName + "]");

        menuFile.setText(LanguageManager.get("menu.file"));
        itemNew.setText(LanguageManager.get("menu.new_list"));
        itemOpen.setText(LanguageManager.get("menu.open_list"));
        itemImport.setText(LanguageManager.get("menu.import"));
        itemImportCsv.setText(LanguageManager.get("menu.import_csv"));
        itemImportMdx.setText(LanguageManager.get("menu.import_mdx"));
        itemExportCsv.setText(LanguageManager.get("menu.export_csv"));
        itemExportPdf.setText(LanguageManager.get("menu.export_pdf"));
        itemSettings.setText("Settings");
        menuLang.setText(LanguageManager.get("menu.language"));

        btnAdd.setText(LanguageManager.get("btn.add"));
        btnDelete.setText(LanguageManager.get("btn.delete"));
        btnPractice.setText(LanguageManager.get("btn.practice"));
        btnAiEnhance.setText(LanguageManager.get("btn.ai_enhance"));
        btnClear.setText(LanguageManager.get("btn.reset"));
        btnSearch.setText(LanguageManager.get("btn.search"));
        btnSearchClear.setText(LanguageManager.get("search.clear"));
        searchField.setToolTipText(LanguageManager.get("search.hint"));

        statusLabel.setText(LanguageManager.get("status.ready"));

        columnNames = new String[]{
            LanguageManager.get("col.term"),
            LanguageManager.get("col.def"),
            LanguageManager.get("col.example"),
            LanguageManager.get("col.notes")
        };
        tableModel.setColumnIdentifiers(columnNames);
    }

    private void changeLanguage(boolean isEnglish) {
        LanguageManager.setLanguage(isEnglish);
        updateUIText();
        refreshTable();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Table Refresh
    // ─────────────────────────────────────────────────────────────────────────

    private void refreshTable() {
        refreshTableWith(sys.getVocabs());
        isSearchFiltered = false;
        btnSearchClear.setEnabled(false);
    }

    private void refreshTableWith(List<Vocab> list) {
        tableModel.setRowCount(0);
        for (Vocab v : list) {
            tableModel.addRow(new Object[]{
                v.getTerm(),
                v.getDefinition(),
                v.getExample(),
                v.getNotes()
            });
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // List Management
    // ─────────────────────────────────────────────────────────────────────────

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
            List<Vocab> data = presets.get(selected);
            for (Vocab v : data) {
                sys.addVocab(v.getTerm(), v.getDefinition(), v.getNotes(), v.getExample());
            }
            updateUIText();
            refreshTable();
            JOptionPane.showMessageDialog(frame, LanguageManager.get("list.imported", currentListName));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Import / Export
    // ─────────────────────────────────────────────────────────────────────────

    private void importFromCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(LanguageManager.get("menu.import_csv"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                int count = sys.importFromFile(file);
                refreshTable();
                JOptionPane.showMessageDialog(frame, LanguageManager.get("import.success", count));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                    LanguageManager.get("import.error", ex.getMessage()),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void importFromMdx() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(LanguageManager.get("menu.import_mdx"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Tab-delimited text (*.txt, *.tsv)", "txt", "tsv"));
        if (chooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            try {
                int count = sys.importFromFile(file);
                refreshTable();
                JOptionPane.showMessageDialog(frame, LanguageManager.get("import.success", count));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                    LanguageManager.get("import.error", ex.getMessage()),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(LanguageManager.get("menu.export_csv"));
        chooser.setSelectedFile(new File(currentListName + ".csv"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV files (*.csv)", "csv"));
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".csv"))
                file = new File(file.getAbsolutePath() + ".csv");
            try {
                sys.exportToCsv(file);
                JOptionPane.showMessageDialog(frame, LanguageManager.get("export.success", file.getAbsolutePath()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                    LanguageManager.get("export.error", ex.getMessage()),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void exportToPdf() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(LanguageManager.get("menu.export_pdf"));
        chooser.setSelectedFile(new File(currentListName + "_vocab.html"));
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("HTML file (*.html)", "html"));
        if (chooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            if (!file.getName().toLowerCase().endsWith(".html"))
                file = new File(file.getAbsolutePath() + ".html");
            try {
                sys.exportToHtmlPdf(file, currentListName);
                // Open in browser
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().browse(file.toURI());
                }
                JOptionPane.showMessageDialog(frame,
                    LanguageManager.get("export.pdf.hint") + "\n\n" +
                    LanguageManager.get("export.success", file.getAbsolutePath()));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(frame,
                    LanguageManager.get("export.error", ex.getMessage()),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Search
    // ─────────────────────────────────────────────────────────────────────────

    private void doSearch() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            clearSearch();
            return;
        }

        List<Vocab> results = sys.search(query);

        if (!results.isEmpty()) {
            refreshTableWith(results);
            isSearchFiltered = true;
            btnSearchClear.setEnabled(true);
            statusLabel.setText(LanguageManager.get("status.search_results", results.size(), query));
        } else {
            // No local results → offer AI search
            statusLabel.setText(LanguageManager.get("status.no_results", query));
            int choice = JOptionPane.showConfirmDialog(
                frame,
                LanguageManager.get("search.not_found"),
                "AI Search",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );
            if (choice == JOptionPane.YES_OPTION) {
                doAiSearch(query);
            }
        }
    }

    private void clearSearch() {
        searchField.setText("");
        refreshTable();
        statusLabel.setText(LanguageManager.get("status.ready"));
        btnSearchClear.setEnabled(false);
        isSearchFiltered = false;
    }

    private void doAiSearch(String query) {
        String apiKey = AiEnhancer.loadApiKey();
        if (apiKey == null) {
            JOptionPane.showMessageDialog(frame,
                LanguageManager.get("ai.no_key"),
                LanguageManager.get("ai.title"),
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        statusLabel.setText(LanguageManager.get("search.ai_prompt", query));
        SwingWorker<String[], Void> worker = new SwingWorker<>() {
            @Override protected String[] doInBackground() {
                return AiEnhancer.searchTerm(query, apiKey);
            }
            @Override protected void done() {
                try {
                    String[] result = get();
                    if (result != null) {
                        // Show result and ask if user wants to add it
                        String msg = "Term: " + result[0] + "\nDefinition: " + result[1] +
                            (result[2].isEmpty() ? "" : "\nExample: " + result[2]) +
                            (result[3].isEmpty() ? "" : "\nNotes: " + result[3]);
                        int add = JOptionPane.showConfirmDialog(frame,
                            msg + "\n\nAdd this word to your list?",
                            "AI Search Result",
                            JOptionPane.YES_NO_OPTION);
                        if (add == JOptionPane.YES_OPTION) {
                            sys.addVocab(result[0], result[1], result[3], result[2]);
                            refreshTable();
                        }
                    } else {
                        JOptionPane.showMessageDialog(frame, "No result found.", "AI Search", JOptionPane.INFORMATION_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "AI search error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
                statusLabel.setText(LanguageManager.get("status.ready"));
            }
        };
        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // AI Enhancement
    // ─────────────────────────────────────────────────────────────────────────

    private void runAiEnhance() {
        List<Vocab> vocabs = sys.getVocabs();
        if (vocabs.isEmpty()) {
            JOptionPane.showMessageDialog(frame, LanguageManager.get("msg.practice_empty"));
            return;
        }

        String apiKey = AiEnhancer.loadApiKey();
        if (apiKey == null) {
            JOptionPane.showMessageDialog(frame,
                LanguageManager.get("ai.no_key"),
                LanguageManager.get("ai.title"),
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(frame,
            LanguageManager.get("ai.confirm", vocabs.size()),
            LanguageManager.get("ai.title"),
            JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        // Progress dialog
        JDialog progressDialog = new JDialog(frame, LanguageManager.get("ai.title"), false);
        JProgressBar bar = new JProgressBar(0, vocabs.size());
        bar.setStringPainted(true);
        JLabel lbl = new JLabel(LanguageManager.get("ai.running"), SwingConstants.CENTER);
        progressDialog.setLayout(new BorderLayout(10, 10));
        progressDialog.add(lbl, BorderLayout.NORTH);
        progressDialog.add(bar, BorderLayout.CENTER);
        progressDialog.setSize(380, 120);
        progressDialog.setLocationRelativeTo(frame);

        SwingWorker<Integer, Integer> worker = new SwingWorker<>() {
            @Override protected Integer doInBackground() {
                int updated = 0;
                int batchSize = 10;
                
                for (int i = 0; i < vocabs.size(); i += batchSize) {
                    int end = Math.min(i + batchSize, vocabs.size());
                    List<Vocab> batch = vocabs.subList(i, end);
                    
                    Map<String, String[]> results = AiEnhancer.enhanceVocabBatch(batch, apiKey);
                    
                    if (results != null) {
                        for (Vocab v : batch) {
                            String[] enhanced = results.get(v.getTerm().toLowerCase());
                            if (enhanced != null) {
                                v.setDefinition(enhanced[0]);
                                if (!enhanced[1].isEmpty() && v.getExample().isEmpty()) {
                                    v.setExample(enhanced[1]);
                                }
                                updated++;
                            }
                        }
                    }
                    publish(end);
                }
                
                sys.forceSave();
                return updated;
            }

            @Override protected void process(java.util.List<Integer> chunks) {
                int progress = chunks.get(chunks.size() - 1);
                bar.setValue(progress);
                bar.setString(progress + " / " + vocabs.size());
            }

            @Override protected void done() {
                progressDialog.dispose();
                try {
                    int updated = get();
                    refreshTable();
                    JOptionPane.showMessageDialog(frame, LanguageManager.get("ai.done", updated));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        worker.execute();
        progressDialog.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Word CRUD
    // ─────────────────────────────────────────────────────────────────────────

    private void showAddDialog() {
        JDialog dialog = new JDialog(frame, LanguageManager.get("dialog.add.title"), true);
        dialog.setLayout(new GridBagLayout());
        dialog.setSize(480, 320);
        dialog.setLocationRelativeTo(frame);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 4, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        JTextField txtTerm  = new JTextField();
        JTextField txtDef   = new JTextField();
        JTextField txtEx    = new JTextField();
        JTextField txtNotes = new JTextField();

        String[][] rows = {
            { LanguageManager.get("col.term"),    "" },
            { LanguageManager.get("col.def"),     "" },
            { LanguageManager.get("col.example"), "" },
            { LanguageManager.get("col.notes"),   "" }
        };
        JTextField[] fields = { txtTerm, txtDef, txtEx, txtNotes };

        for (int i = 0; i < fields.length; i++) {
            gbc.gridy = i;
            gbc.gridx = 0; gbc.weightx = 0.3;
            dialog.add(new JLabel(rows[i][0] + ":"), gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            fields[i].setFont(new Font("SansSerif", Font.PLAIN, 14));
            dialog.add(fields[i], gbc);
        }

        JButton btnSave = new JButton(LanguageManager.get("dialog.add.save"));
        btnSave.setFont(btnSave.getFont().deriveFont(Font.BOLD));
        btnSave.addActionListener(e -> {
            String t  = txtTerm.getText().trim();
            String d  = txtDef.getText().trim();
            String ex = txtEx.getText().trim();
            String n  = txtNotes.getText().trim();
            if (!t.isEmpty() && !d.isEmpty()) {
                if (sys.addVocab(t, d, n, ex)) {
                    refreshTable();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        LanguageManager.get("msg.term_exists"), "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(dialog,
                    LanguageManager.get("msg.required"), "Warning", JOptionPane.WARNING_MESSAGE);
            }
        });

        gbc.gridy = fields.length;
        gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(12, 10, 10, 10);
        dialog.add(btnSave, gbc);

        // Enter on any field triggers save
        for (JTextField f : fields) f.addActionListener(e -> btnSave.doClick());

        dialog.setVisible(true);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row != -1) {
            String term = (String) tableModel.getValueAt(row, 0);
            int confirm = JOptionPane.showConfirmDialog(frame,
                LanguageManager.get("msg.confirm_delete", term), "Confirm", JOptionPane.YES_NO_OPTION);
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

    // ─────────────────────────────────────────────────────────────────────────
    // Detail Dialog (expanded definition pop-up)
    // ─────────────────────────────────────────────────────────────────────────

    private void showDetailDialog(int row) {
        String term    = (String) tableModel.getValueAt(row, 0);
        String def     = (String) tableModel.getValueAt(row, 1);
        String example = (String) tableModel.getValueAt(row, 2);
        String notes   = (String) tableModel.getValueAt(row, 3);

        JDialog detail = new JDialog(frame, LanguageManager.get("detail.title", term), false);
        detail.setSize(520, 340);
        detail.setLocationRelativeTo(frame);
        detail.setLayout(new BorderLayout(10, 10));

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBorder(BorderFactory.createEmptyBorder(16, 20, 10, 20));

        // Term
        JLabel lblTerm = new JLabel(term);
        lblTerm.setFont(new Font("SansSerif", Font.BOLD, 22));
        lblTerm.setForeground(new Color(60, 60, 180));
        content.add(lblTerm);
        content.add(Box.createVerticalStrut(10));

        // Notes tag
        if (notes != null && !notes.isEmpty()) {
            JLabel lblNotes = new JLabel("[ " + notes + " ]");
            lblNotes.setFont(new Font("SansSerif", Font.ITALIC, 12));
            lblNotes.setForeground(Color.GRAY);
            content.add(lblNotes);
            content.add(Box.createVerticalStrut(8));
        }

        // Definition
        addSectionHeader(content, LanguageManager.get("col.def"));
        JTextArea txtDef = makeReadonlyTextArea(def);
        content.add(new JScrollPane(txtDef));
        content.add(Box.createVerticalStrut(10));

        // Example
        if (example != null && !example.isEmpty()) {
            addSectionHeader(content, LanguageManager.get("col.example"));
            JTextArea txtEx = makeReadonlyTextArea(example);
            txtEx.setFont(new Font("SansSerif", Font.ITALIC, 13));
            txtEx.setForeground(new Color(80, 80, 180));
            content.add(new JScrollPane(txtEx));
        }

        detail.add(new JScrollPane(content), BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> detail.dispose());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(btnClose);
        detail.add(btnRow, BorderLayout.SOUTH);

        detail.setVisible(true);
    }

    private void addSectionHeader(JPanel panel, String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
        lbl.setForeground(Color.DARK_GRAY);
        panel.add(lbl);
    }

    private JTextArea makeReadonlyTextArea(String text) {
        JTextArea area = new JTextArea(text);
        area.setFont(new Font("SansSerif", Font.PLAIN, 14));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setEditable(false);
        area.setBackground(new Color(248, 248, 252));
        area.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        area.setRows(3);
        return area;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Practice
    // ─────────────────────────────────────────────────────────────────────────

    private void showPracticeDialog() {
        if (sys.getVocabs().isEmpty()) {
            JOptionPane.showMessageDialog(frame, LanguageManager.get("msg.practice_empty"));
            return;
        }
        new GameDialog(frame, sys);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Inner classes: Cell Renderers
    // ─────────────────────────────────────────────────────────────────────────

    /** Renders cell text truncated to maxLength chars, showing '…' if truncated. */
    private static class TruncatedCellRenderer extends DefaultTableCellRenderer {
        private final int maxLength;
        TruncatedCellRenderer(int maxLength) { this.maxLength = maxLength; }

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (value != null) {
                String s = value.toString();
                if (s.length() > maxLength) {
                    setText(s.substring(0, maxLength) + "…");
                    setToolTipText("<html><body style='width:320px'>" + s.replace("<","&lt;") + "</body></html>");
                } else {
                    setText(s);
                    setToolTipText(null);
                }
            }
            return this;
        }
    }

    /** Alternating row background colors. */
    private static class AlternatingRowRenderer extends DefaultTableCellRenderer {
        private static final Color ODD  = new Color(248, 248, 255);
        private static final Color EVEN = Color.WHITE;
        private static final Color SEL  = new Color(210, 210, 240);

        @Override
        public Component getTableCellRendererComponent(
                JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int col) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
            if (isSelected) {
                setBackground(SEL);
            } else {
                setBackground(row % 2 == 0 ? EVEN : ODD);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
            return this;
        }
    }
}
