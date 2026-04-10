import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * GameDialog — Practice Mode dialog.
 *
 * Features:
 *  - Direction selector: Term→Def / Def→Term
 *  - Order selector: Sequential / Reversed / Random
 *  - Wrong-word tracking with post-round results table
 *  - Retry loop: after each round, if there are wrong words,
 *    ask the user if they want to retry only those words.
 *    Loop continues until all correct or user stops.
 */
public class GameDialog extends JDialog {

    // ── State ─────────────────────────────────────────────────────────────────
    private final VSystem sys;
    private List<Vocab> quizList;       // current round's words
    private List<Vocab> wrongList;      // wrong answers this round
    private int currentIndex = 0;
    private boolean isTermToDef = true;
    private int orderMode = 2;          // 1=sequential, 2=random, 3=reversed

    // ── UI ────────────────────────────────────────────────────────────────────
    private JComboBox<String> comboDirection;
    private JComboBox<String> comboOrder;
    private JLabel lblProgress;
    private JLabel lblQuestion;
    private JTextArea txtNotes;         // shows hint/notes below question
    private JTextField txtAnswer;
    private JLabel lblFeedback;
    private JButton btnCheck;
    private JButton btnNext;
    private JButton btnStop;
    private JPanel cardPanel;           // CardLayout host
    private CardLayout cardLayout;

    private static final String CARD_QUIZ    = "QUIZ";
    private static final String CARD_RESULTS = "RESULTS";

    public GameDialog(JFrame parent, VSystem sys) {
        super(parent, LanguageManager.get("practice.title"), true);
        this.sys = sys;
        setSize(580, 430);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout(8, 8));
        getRootPane().setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        buildTopPanel();
        buildCardPanel();
        buildBottomPanel();

        startNewRound(new ArrayList<>(sys.getVocabs()));
        setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI Construction
    // ─────────────────────────────────────────────────────────────────────────

    private void buildTopPanel() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        top.setOpaque(false);

        // Direction combo
        String[] directions = {
            LanguageManager.get("col.term") + "  →  " + LanguageManager.get("col.def"),
            LanguageManager.get("col.def")  + "  →  " + LanguageManager.get("col.term")
        };
        comboDirection = new JComboBox<>(directions);
        comboDirection.addActionListener(e -> {
            isTermToDef = comboDirection.getSelectedIndex() == 0;
            showCurrentQuestion();
        });

        // Order combo
        String[] orders = {
            LanguageManager.get("practice.order.sequential"),
            LanguageManager.get("practice.order.random"),
            LanguageManager.get("practice.order.reversed")
        };
        comboOrder = new JComboBox<>(orders);
        comboOrder.setSelectedIndex(1); // default random
        comboOrder.addActionListener(e -> {
            int sel = comboOrder.getSelectedIndex();
            orderMode = (sel == 0) ? 1 : (sel == 1) ? 2 : 3;
            // Restart current round with new order
            startNewRound(quizList);
        });

        // Progress label
        lblProgress = new JLabel();
        lblProgress.setFont(new Font("SansSerif", Font.PLAIN, 12));
        lblProgress.setForeground(Color.GRAY);

        top.add(new JLabel(LanguageManager.get("practice.mode")));
        top.add(comboDirection);
        top.add(Box.createHorizontalStrut(10));
        top.add(new JLabel(LanguageManager.get("practice.order")));
        top.add(comboOrder);
        top.add(Box.createHorizontalStrut(20));
        top.add(lblProgress);

        add(top, BorderLayout.NORTH);
    }

    private void buildCardPanel() {
        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.add(buildQuizCard(),    CARD_QUIZ);
        cardPanel.add(buildResultsCard(), CARD_RESULTS);
        add(cardPanel, BorderLayout.CENTER);
    }

    private JPanel buildQuizCard() {
        JPanel quiz = new JPanel(new GridBagLayout());
        quiz.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 4, 6, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.gridx = 0;

        // Question label
        lblQuestion = new JLabel("", SwingConstants.CENTER);
        lblQuestion.setFont(new Font("SansSerif", Font.BOLD, 20));
        gbc.gridy = 0;
        gbc.ipady = 10;
        quiz.add(lblQuestion, gbc);
        gbc.ipady = 0;

        // Notes / hint area
        txtNotes = new JTextArea(2, 30);
        txtNotes.setEditable(false);
        txtNotes.setFont(new Font("SansSerif", Font.ITALIC, 12));
        txtNotes.setForeground(Color.GRAY);
        txtNotes.setBackground(quiz.getBackground());
        txtNotes.setWrapStyleWord(true);
        txtNotes.setLineWrap(true);
        txtNotes.setOpaque(false);
        txtNotes.setFocusable(false);
        gbc.gridy = 1;
        quiz.add(txtNotes, gbc);

        // Answer field
        txtAnswer = new JTextField();
        txtAnswer.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtAnswer.setHorizontalAlignment(JTextField.CENTER);
        txtAnswer.addActionListener(e -> checkAnswer());
        gbc.gridy = 2;
        gbc.ipady = 6;
        quiz.add(txtAnswer, gbc);
        gbc.ipady = 0;

        // Feedback label
        lblFeedback = new JLabel(" ", SwingConstants.CENTER);
        lblFeedback.setFont(new Font("SansSerif", Font.BOLD, 14));
        gbc.gridy = 3;
        quiz.add(lblFeedback, gbc);

        return quiz;
    }

    /** Results card — rebuilt dynamically each round. Placeholder panel. */
    private JPanel buildResultsCard() {
        return new JPanel(); // replaced dynamically by showResults()
    }

    private void buildBottomPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        bottom.setOpaque(false);

        btnStop = new JButton(LanguageManager.get("practice.stop"));
        btnStop.addActionListener(e -> dispose());

        btnCheck = new JButton(LanguageManager.get("practice.check"));
        btnCheck.addActionListener(e -> checkAnswer());

        btnNext = new JButton(LanguageManager.get("practice.next"));
        btnNext.setEnabled(false);
        btnNext.addActionListener(e -> nextQuestion());

        bottom.add(btnStop);
        bottom.add(btnCheck);
        bottom.add(btnNext);
        add(bottom, BorderLayout.SOUTH);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Round Management
    // ─────────────────────────────────────────────────────────────────────────

    /** Starts a fresh round with the given word list, applying current order. */
    private void startNewRound(List<Vocab> words) {
        wrongList    = new ArrayList<>();
        currentIndex = 0;
        quizList     = new ArrayList<>(words);

        switch (orderMode) {
            case 1: /* sequential — keep as-is */ break;
            case 3: Collections.reverse(quizList); break;
            default: Collections.shuffle(quizList); break; // random
        }

        cardLayout.show(cardPanel, CARD_QUIZ);
        showCurrentQuestion();
    }

    private void showCurrentQuestion() {
        if (quizList == null || quizList.isEmpty()) {
            showResults();
            return;
        }
        if (currentIndex >= quizList.size()) {
            showResults();
            return;
        }

        Vocab v = quizList.get(currentIndex);
        updateProgress();

        if (isTermToDef) {
            lblQuestion.setText(v.getTerm());
            // Show notes as hint if available
            String hint = v.getNotes() != null && !v.getNotes().isEmpty() ? v.getNotes() : "";
            txtNotes.setText(hint);
        } else {
            lblQuestion.setText(v.getDefinition());
            txtNotes.setText("");
        }

        txtAnswer.setText("");
        txtAnswer.setEditable(true);
        lblFeedback.setText(" ");
        lblFeedback.setForeground(Color.BLACK);
        btnCheck.setEnabled(true);
        btnNext.setEnabled(false);
        txtAnswer.requestFocusInWindow();
    }

    private void updateProgress() {
        lblProgress.setText(LanguageManager.get("practice.progress",
            currentIndex + 1, quizList.size()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Quiz Logic
    // ─────────────────────────────────────────────────────────────────────────

    private void checkAnswer() {
        String ans = txtAnswer.getText().trim();
        if (ans.isEmpty()) return;

        Vocab v = quizList.get(currentIndex);
        boolean correct;
        if (isTermToDef) {
            correct = sys.checkTermToDefInList(quizList, v.getTerm(), ans);
        } else {
            correct = sys.checkDefToTermInList(quizList, v.getDefinition(), ans);
        }

        if (correct) {
            lblFeedback.setText(LanguageManager.get("practice.correct"));
            lblFeedback.setForeground(new Color(0, 160, 60));
        } else {
            String rightAns = isTermToDef ? v.getDefinition() : v.getTerm();
            lblFeedback.setText(LanguageManager.get("practice.incorrect", rightAns));
            lblFeedback.setForeground(new Color(200, 30, 30));
            wrongList.add(v);
        }

        txtAnswer.setEditable(false);
        btnCheck.setEnabled(false);
        btnNext.setEnabled(true);
        btnNext.requestFocusInWindow();
    }

    private void nextQuestion() {
        currentIndex++;
        showCurrentQuestion();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Results Panel
    // ─────────────────────────────────────────────────────────────────────────

    private void showResults() {
        // Build results panel dynamically
        JPanel resultsPanel = new JPanel(new BorderLayout(8, 8));
        resultsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Title
        JLabel title = new JLabel(LanguageManager.get("practice.results.title"), SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        resultsPanel.add(title, BorderLayout.NORTH);

        // Center: perfect message or wrong words table
        if (wrongList.isEmpty()) {
            JLabel perfect = new JLabel(LanguageManager.get("practice.results.perfect"), SwingConstants.CENTER);
            perfect.setFont(new Font("SansSerif", Font.BOLD, 16));
            perfect.setForeground(new Color(0, 150, 60));
            resultsPanel.add(perfect, BorderLayout.CENTER);
        } else {
            JPanel centerPanel = new JPanel(new BorderLayout(4, 4));
            JLabel wrongCountLabel = new JLabel(
                LanguageManager.get("practice.results.wrong_count", wrongList.size()),
                SwingConstants.CENTER);
            wrongCountLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            wrongCountLabel.setForeground(new Color(200, 60, 30));
            centerPanel.add(wrongCountLabel, BorderLayout.NORTH);

            // Wrong words table
            String[] cols = {
                LanguageManager.get("col.term"),
                LanguageManager.get("col.def"),
                LanguageManager.get("col.example")
            };
            DefaultTableModel model = new DefaultTableModel(cols, 0) {
                public boolean isCellEditable(int r, int c) { return false; }
            };
            for (Vocab v : wrongList) {
                model.addRow(new Object[]{ v.getTerm(), v.getDefinition(), v.getExample() });
            }
            JTable wrongTable = new JTable(model);
            wrongTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
            wrongTable.setRowHeight(24);
            wrongTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 13));
            wrongTable.setFillsViewportHeight(true);
            centerPanel.add(new JScrollPane(wrongTable), BorderLayout.CENTER);
            resultsPanel.add(centerPanel, BorderLayout.CENTER);
        }

        // Bottom buttons
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        JButton btnStop = new JButton(LanguageManager.get("practice.stop"));
        btnStop.addActionListener(e -> dispose());
        btnPanel.add(btnStop);

        if (!wrongList.isEmpty()) {
            JButton btnRetry = new JButton(LanguageManager.get("practice.retry"));
            btnRetry.setFont(btnRetry.getFont().deriveFont(Font.BOLD));
            btnRetry.setBackground(new Color(79, 70, 229));
            btnRetry.setForeground(Color.WHITE);
            btnRetry.setOpaque(true);
            btnRetry.setBorderPainted(false);
            btnRetry.addActionListener(e -> startNewRound(wrongList));
            btnPanel.add(btnRetry);
        }

        resultsPanel.add(btnPanel, BorderLayout.SOUTH);

        // Swap the results card
        cardPanel.add(resultsPanel, CARD_RESULTS);
        cardLayout.show(cardPanel, CARD_RESULTS);

        lblProgress.setText("");
        btnCheck.setEnabled(false);
        btnNext.setEnabled(false);
    }
}
