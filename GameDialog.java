import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

public class GameDialog extends JDialog {
    private VSystem sys;
    private List<Vocab> quizList;
    private int currentIndex = 0;
    private boolean isTermToDef = true; // Mode: Term -> Def or Def -> Term

    private JLabel lblQuestion;
    private JTextField txtAnswer;
    private JLabel lblFeedback;
    private JButton btnCheck;
    private JButton btnNext;

    public GameDialog(JFrame parent, VSystem sys) {
        super(parent, LanguageManager.get("practice.title"), true);
        this.sys = sys;
        this.quizList = new ArrayList<>(sys.getVocabs());
        Collections.shuffle(this.quizList); // Default to random order

        setSize(500, 300);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(10, 10));

        // --- Top Panel (Settings) ---
        JPanel topPanel = new JPanel();
        String[] modes = {
            LanguageManager.get("col.term") + " -> " + LanguageManager.get("col.def"), 
            LanguageManager.get("col.def") + " -> " + LanguageManager.get("col.term")
        };
        JComboBox<String> comboMode = new JComboBox<>(modes);
        comboMode.addActionListener(e -> {
            isTermToDef = comboMode.getSelectedIndex() == 0;
            showCurrentQuestion();
        });
        topPanel.add(new JLabel(LanguageManager.get("practice.mode")));
        topPanel.add(comboMode);
        add(topPanel, BorderLayout.NORTH);

        // --- Center Panel (Quiz) ---
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        lblQuestion = new JLabel("Question", SwingConstants.CENTER);
        lblQuestion.setFont(new Font("SansSerif", Font.BOLD, 18));
        
        txtAnswer = new JTextField();
        txtAnswer.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtAnswer.setHorizontalAlignment(JTextField.CENTER);
        txtAnswer.addActionListener(e -> checkAnswer()); // Enter key triggers check

        lblFeedback = new JLabel(" ", SwingConstants.CENTER);
        lblFeedback.setFont(new Font("SansSerif", Font.BOLD, 14));

        centerPanel.add(lblQuestion);
        centerPanel.add(txtAnswer);
        centerPanel.add(lblFeedback);

        add(centerPanel, BorderLayout.CENTER);

        // --- Bottom Panel (Controls) ---
        JPanel bottomPanel = new JPanel();
        btnCheck = new JButton(LanguageManager.get("practice.check"));
        btnCheck.addActionListener(e -> checkAnswer());

        btnNext = new JButton(LanguageManager.get("practice.next"));
        btnNext.setEnabled(false);
        btnNext.addActionListener(e -> nextQuestion());

        bottomPanel.add(btnCheck);
        bottomPanel.add(btnNext);
        add(bottomPanel, BorderLayout.SOUTH);

        showCurrentQuestion();
        setVisible(true);
    }

    private void showCurrentQuestion() {
        if (currentIndex >= quizList.size()) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("practice.complete"));
            dispose();
            return;
        }

        Vocab v = quizList.get(currentIndex);
        if (isTermToDef) {
            lblQuestion.setText(v.getTerm());
        } else {
            lblQuestion.setText(v.getDefinition());
        }
        
        txtAnswer.setText("");
        txtAnswer.setEditable(true);
        lblFeedback.setText(" ");
        lblFeedback.setForeground(Color.BLACK);
        btnCheck.setEnabled(true);
        btnNext.setEnabled(false);
        txtAnswer.requestFocus();
    }

    private void checkAnswer() {
        String ans = txtAnswer.getText().trim();
        if (ans.isEmpty()) return;

        Vocab v = quizList.get(currentIndex);
        boolean correct = false;

        if (isTermToDef) {
            // Check if answer matches definition (loose check)
            correct = sys.checkTermToDef(v.getTerm(), ans);
        } else {
            // Check if answer matches term
            correct = sys.checkDefToTerm(v.getDefinition(), ans);
        }

        if (correct) {
            lblFeedback.setText(LanguageManager.get("practice.correct"));
            lblFeedback.setForeground(new Color(0, 150, 0));
        } else {
            lblFeedback.setText(LanguageManager.get("practice.incorrect", (isTermToDef ? v.getDefinition() : v.getTerm())));
            lblFeedback.setForeground(Color.RED);
        }

        txtAnswer.setEditable(false);
        btnCheck.setEnabled(false);
        btnNext.setEnabled(true);
        btnNext.requestFocus();
    }

    private void nextQuestion() {
        currentIndex++;
        showCurrentQuestion();
    }
}
