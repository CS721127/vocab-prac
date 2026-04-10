import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.Properties;

public class SettingsDialog extends JDialog {
    private JComboBox<String> comboLang;
    private JCheckBox checkDark;
    private File configFile;
    private Properties props;
    private SwingApp app;

    public SettingsDialog(JFrame parent, SwingApp app) {
        super(parent, "Settings", true);
        this.app = app;
        setSize(300, 200);
        setLocationRelativeTo(parent);
        setLayout(new GridLayout(4, 1, 10, 10));

        // Load Config
        String userHome = System.getProperty("user.home");
        configFile = new File(new File(userHome, ".vocabapp"), "config.properties");
        props = new Properties();
        loadConfig();

        // Language
        JPanel pnlLang = new JPanel(new FlowLayout(FlowLayout.LEFT));
        pnlLang.add(new JLabel("Language: "));
        String[] langs = {"English", "Chinese"};
        comboLang = new JComboBox<>(langs);
        if (props.getProperty("language", "en").equals("cn")) {
            comboLang.setSelectedIndex(1);
        }
        pnlLang.add(comboLang);
        add(pnlLang);

        // Theme (Placeholder for now, just saves preference)
        JPanel pnlTheme = new JPanel(new FlowLayout(FlowLayout.LEFT));
        checkDark = new JCheckBox("Dark Mode (Restart required)");
        checkDark.setSelected(Boolean.parseBoolean(props.getProperty("dark_mode", "false")));
        pnlTheme.add(checkDark);
        add(pnlTheme);

        // Buttons
        JPanel pnlBtn = new JPanel();
        JButton btnSave = new JButton("Save");
        btnSave.addActionListener(e -> saveSettings());
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> dispose());
        pnlBtn.add(btnSave);
        pnlBtn.add(btnCancel);
        add(pnlBtn);

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
        props.setProperty("dark_mode", String.valueOf(checkDark.isSelected()));

        try (FileOutputStream out = new FileOutputStream(configFile)) {
            props.store(out, "VocabMaster Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Apply Language Immediately
        LanguageManager.setLanguage(lang.equals("en"));
        app.updateUIText();
        
        dispose();
    }
}
