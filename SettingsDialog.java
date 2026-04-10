import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class SettingsDialog extends JDialog {
    private JComboBox<String> comboLang;
    private JPasswordField txtApiKey;
    private File configFile;
    private Properties props;
    private SwingApp app;

    public SettingsDialog(JFrame parent, SwingApp app) {
        super(parent, "Settings", true);
        this.app = app;
        setSize(460, 260);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));

        // Load Config
        String userHome = System.getProperty("user.home");
        configFile = new File(new File(userHome, ".vocabapp"), "config.properties");
        props = new Properties();
        loadConfig();

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // ── Language ──────────────────────────────────────────────────────────
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Language:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        String[] langs = { "English", "中文 (Chinese)" };
        comboLang = new JComboBox<>(langs);
        if (props.getProperty("language", "en").equals("cn")) comboLang.setSelectedIndex(1);
        form.add(comboLang, gbc);

        // ── Gemma API Key ──────────────────────────────────────────────────────
        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0.3;
        form.add(new JLabel("Gemma API Key:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        txtApiKey = new JPasswordField();
        String existingKey = props.getProperty("ai.api_key", "");
        txtApiKey.setText(existingKey);
        txtApiKey.setFont(new Font("Monospaced", Font.PLAIN, 13));
        form.add(txtApiKey, gbc);

        // Hint
        gbc.gridy = 2; gbc.gridx = 0; gbc.gridwidth = 2;
        JLabel hint = new JLabel("<html><font color='gray' size='2'>Get your key at: <a href=''>https://aistudio.google.com/</a><br>Used for AI Enhance and AI Search features.</font></html>");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        form.add(hint, gbc);
        gbc.gridwidth = 1;

        add(form, BorderLayout.CENTER);

        // ── Buttons ───────────────────────────────────────────────────────────
        JPanel pnlBtn = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        JButton btnSave = new JButton("Save");
        btnSave.setFont(btnSave.getFont().deriveFont(Font.BOLD));
        btnSave.addActionListener(e -> saveSettings());
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        pnlBtn.add(btnCancel);
        pnlBtn.add(btnSave);
        add(pnlBtn, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadConfig() {
        if (configFile.exists()) {
            try (FileInputStream in = new FileInputStream(configFile)) {
                props.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveSettings() {
        String lang = comboLang.getSelectedIndex() == 0 ? "en" : "cn";
        props.setProperty("language", lang);

        String apiKey = new String(txtApiKey.getPassword()).trim();
        if (!apiKey.isEmpty()) {
            props.setProperty("ai.api_key", apiKey);
        } else {
            props.remove("ai.api_key");
        }

        configFile.getParentFile().mkdirs();
        try (FileOutputStream out = new FileOutputStream(configFile)) {
            props.store(out, "VocabMaster Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }

        LanguageManager.setLanguage(lang.equals("en"));
        app.updateUIText();
        dispose();
    }
}
