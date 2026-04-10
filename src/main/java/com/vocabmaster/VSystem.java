package com.vocabmaster;

import java.io.*;
import java.util.*;

class VSystem {
    private List<Vocab> vocabs;
    private String fileName;
    private List<Integer> check;
    private List<Vocab> wrong;
    private Scanner scan;

    public VSystem(String listName) {
        String userHome = System.getProperty("user.home");
        File appDir = new File(userHome, ".vocabapp");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        this.fileName = new File(appDir, listName + "_events.dat").getAbsolutePath();
        vocabs = new ArrayList<>();
        loadEvents();
        scan = new Scanner(System.in);
    }

    public static List<String> getAvailableLists() {
        List<String> lists = new ArrayList<>();
        String userHome = System.getProperty("user.home");
        File appDir = new File(userHome, ".vocabapp");
        if (appDir.exists() && appDir.isDirectory()) {
            File[] files = appDir.listFiles((dir, name) -> name.endsWith("_events.dat"));
            if (files != null) {
                for (File f : files) {
                    String name = f.getName();
                    lists.add(name.substring(0, name.length() - 11));
                }
            }
        }
        return lists;
    }

    public void clearAllData() {
        vocabs.clear();
        File file = new File(fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────────────────

    public boolean addVocab(String term, String definition, String notes) {
        return addVocab(term, definition, notes, "");
    }

    public boolean addVocab(String term, String definition, String notes, String example) {
        for (Vocab voc : vocabs) {
            if (voc.getTerm().equalsIgnoreCase(term)) {
                System.out.println("Repeated Term!");
                return false;
            }
        }
        boolean b = vocabs.add(new Vocab(term, definition, notes, example));
        saveEvents();
        return b;
    }

    public boolean deleteVocab(String term) {
        boolean a = vocabs.removeIf(event -> event.getTerm().equalsIgnoreCase(term));
        saveEvents();
        return a;
    }

    public boolean deleteAll() {
        vocabs.clear();
        saveEvents();
        return vocabs.isEmpty();
    }

    // ── SEARCH ────────────────────────────────────────────────────────────────

    /** Returns vocabs matching by term (contains, case-insensitive). */
    public List<Vocab> searchByTerm(String query) {
        List<Vocab> results = new ArrayList<>();
        for (Vocab vocab : vocabs) {
            if (vocab.getTerm().toLowerCase().contains(query.toLowerCase())) {
                results.add(vocab);
            }
        }
        return results;
    }

    /** Returns vocabs matching by definition (contains, case-insensitive). */
    public List<Vocab> searchByDefinition(String query) {
        List<Vocab> results = new ArrayList<>();
        for (Vocab vocab : vocabs) {
            if (vocab.getDefinition().toLowerCase().contains(query.toLowerCase())) {
                results.add(vocab);
            }
        }
        return results;
    }

    /** Returns all vocabs matching term OR definition. */
    public List<Vocab> search(String query) {
        Set<Vocab> seen = new LinkedHashSet<>();
        seen.addAll(searchByTerm(query));
        seen.addAll(searchByDefinition(query));
        return new ArrayList<>(seen);
    }

    // Legacy console methods kept for backward compat
    public boolean searchTerm(String query) {
        boolean a = false;
        for (Vocab vocab : vocabs) {
            if (vocab.getTerm().toLowerCase().contains(query.toLowerCase())) {
                System.out.println(vocab);
                a = true;
            }
        }
        return a;
    }

    public boolean searchDefinition(String query) {
        boolean a = false;
        for (Vocab vocab : vocabs) {
            if (vocab.getDefinition().toLowerCase().contains(query.toLowerCase())) {
                System.out.println(vocab);
                a = true;
            }
        }
        return a;
    }

    // ── CHECK (for game) ──────────────────────────────────────────────────────

    public boolean checkDefToTerm(String definition, String term) {
        for (Vocab vocab : vocabs) {
            if (vocab.getDefinition().equals(definition) && vocab.getTerm().equalsIgnoreCase(term)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkTermToDef(String term, String definition) {
        for (Vocab vocab : vocabs) {
            if (vocab.getTerm().equalsIgnoreCase(term) && vocab.getDefinition().toLowerCase().contains(definition.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // Also support checking against a sub-list (for wrong-word retry)
    public boolean checkDefToTermInList(List<Vocab> list, String definition, String term) {
        for (Vocab vocab : list) {
            if (vocab.getDefinition().equals(definition) && vocab.getTerm().equalsIgnoreCase(term)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkTermToDefInList(List<Vocab> list, String term, String definition) {
        for (Vocab vocab : list) {
            if (vocab.getTerm().equalsIgnoreCase(term) && vocab.getDefinition().toLowerCase().contains(definition.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    // ── IMPORT ────────────────────────────────────────────────────────────────

    /**
     * Import from CSV or tab-delimited file.
     * Supported formats:
     *   CSV:  term,definition,example,notes  (with optional header row)
     *   TSV:  term\tdefinition\texample\tnotes
     *   MDX-style: term\tdefinition  (2 columns)
     * Returns the number of imported items.
     */
    public int importFromFile(File file) throws IOException {
        int count = 0;
        try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
            String line;
            boolean firstLine = true;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;

                // Detect delimiter
                String[] parts;
                if (line.contains("\t")) {
                    parts = line.split("\t", -1);
                } else {
                    parts = parseCsvLine(line);
                }

                // Skip header if detected
                if (firstLine) {
                    firstLine = false;
                    if (parts.length > 0 && (
                            parts[0].equalsIgnoreCase("term") ||
                            parts[0].equalsIgnoreCase("word") ||
                            parts[0].equalsIgnoreCase("词汇") ||
                            parts[0].equalsIgnoreCase("Term"))) {
                        continue; // skip header row
                    }
                }

                if (parts.length < 2) continue; // need at least term + definition

                String term  = parts[0].trim();
                String def   = parts.length > 1 ? parts[1].trim() : "";
                String ex    = parts.length > 2 ? parts[2].trim() : "";
                String notes = parts.length > 3 ? parts[3].trim() : "";

                if (!term.isEmpty() && !def.isEmpty()) {
                    addVocab(term, def, notes, ex);
                    count++;
                }
            }
        }
        return count;
    }

    /** Simple CSV line parser handling quoted fields. */
    private String[] parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuote = !inQuote;
            } else if (c == ',' && !inQuote) {
                tokens.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString());
        return tokens.toArray(new String[0]);
    }

    // ── EXPORT ────────────────────────────────────────────────────────────────

    /**
     * Export all vocabs to a CSV file.
     * Format: Term,Definition,Example,Notes
     */
    public void exportToCsv(File file) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            pw.println("Term,Definition,Example,Notes");
            for (Vocab v : vocabs) {
                pw.println(
                    csvEscape(v.getTerm()) + "," +
                    csvEscape(v.getDefinition()) + "," +
                    csvEscape(v.getExample()) + "," +
                    csvEscape(v.getNotes())
                );
            }
        }
    }

    /** Escapes a value for CSV output. */
    private String csvEscape(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * Export all vocabs to a polished HTML file (print-ready PDF).
     * User prints to PDF from the browser.
     */
    public void exportToHtmlPdf(File file, String listTitle) throws IOException {
        try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"))) {
            pw.println("<!DOCTYPE html>");
            pw.println("<html lang=\"en\">");
            pw.println("<head>");
            pw.println("<meta charset=\"UTF-8\">");
            pw.println("<title>" + escapeHtml(listTitle) + " — Vocab Master</title>");
            pw.println("<style>");
            pw.println("  @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap');");
            pw.println("  * { box-sizing: border-box; margin: 0; padding: 0; }");
            pw.println("  body { font-family: 'Inter', Helvetica, Arial, sans-serif; font-size: 11pt; color: #1a1a2e; background: #fff; }");
            pw.println("  .page { max-width: 210mm; margin: 0 auto; padding: 20mm 18mm; }");
            pw.println("  h1 { font-size: 22pt; font-weight: 700; color: #2a2a5a; border-bottom: 3px solid #4f46e5; padding-bottom: 8px; margin-bottom: 6px; }");
            pw.println("  .subtitle { font-size: 10pt; color: #6b6b8a; margin-bottom: 18px; }");
            pw.println("  table { width: 100%; border-collapse: collapse; font-size: 10.5pt; }");
            pw.println("  thead tr { background: #4f46e5; color: #fff; }");
            pw.println("  thead th { padding: 8px 10px; text-align: left; font-weight: 600; letter-spacing: 0.03em; }");
            pw.println("  tbody tr:nth-child(odd)  { background: #f5f5ff; }");
            pw.println("  tbody tr:nth-child(even) { background: #ffffff; }");
            pw.println("  tbody tr:hover { background: #ededff; }");
            pw.println("  td { padding: 7px 10px; vertical-align: top; border-bottom: 1px solid #e0e0ef; }");
            pw.println("  .term { font-weight: 600; color: #2a2a5a; white-space: nowrap; }");
            pw.println("  .example { font-style: italic; color: #505070; font-size: 9.5pt; }");
            pw.println("  .notes  { color: #7070a0; font-size: 9pt; }");
            pw.println("  .footer { margin-top: 16px; font-size: 8.5pt; color: #aaa; text-align: center; }");
            pw.println("  @media print {");
            pw.println("    body { -webkit-print-color-adjust: exact; print-color-adjust: exact; }");
            pw.println("    thead { display: table-header-group; }");
            pw.println("    tr { page-break-inside: avoid; }");
            pw.println("    .no-print { display: none; }");
            pw.println("  }");
            pw.println("</style>");
            pw.println("</head>");
            pw.println("<body>");
            pw.println("<div class='no-print' style='background:#4f46e5;color:#fff;padding:10px 18px;font-family:Inter,sans-serif;font-size:11pt'>");
            pw.println("  📄 <strong>Print to PDF:</strong> Press <kbd style='background:#fff;color:#000;padding:2px 6px;border-radius:3px'>Ctrl+P</kbd> (or ⌘P on Mac) → <em>Save as PDF</em>");
            pw.println("</div>");
            pw.println("<div class='page'>");
            pw.println("  <h1>" + escapeHtml(listTitle) + "</h1>");
            pw.println("  <div class='subtitle'>Generated by Vocab Master · " + new java.util.Date() + " · " + vocabs.size() + " words</div>");
            pw.println("  <table>");
            pw.println("    <thead><tr>");
            pw.println("      <th style='width:18%'>Term</th>");
            pw.println("      <th style='width:38%'>Definition</th>");
            pw.println("      <th style='width:30%'>Example</th>");
            pw.println("      <th style='width:14%'>Notes</th>");
            pw.println("    </tr></thead>");
            pw.println("    <tbody>");
            int idx = 1;
            for (Vocab v : vocabs) {
                pw.println("    <tr>");
                pw.println("      <td class='term'>" + idx++ + ". " + escapeHtml(v.getTerm()) + "</td>");
                pw.println("      <td>" + escapeHtml(v.getDefinition()) + "</td>");
                pw.println("      <td class='example'>" + escapeHtml(v.getExample()) + "</td>");
                pw.println("      <td class='notes'>" + escapeHtml(v.getNotes()) + "</td>");
                pw.println("    </tr>");
            }
            pw.println("    </tbody>");
            pw.println("  </table>");
            pw.println("  <div class='footer'>Vocab Master — Print for Study</div>");
            pw.println("</div>");
            pw.println("</body></html>");
        }
    }

    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    // ── GAMES (legacy console) ────────────────────────────────────────────────
    // (Kept for backward compatibility; GUI now uses GameDialog)

    public void game1(String mode) {
        wrong = new ArrayList<>();
        check = new ArrayList<>();
        String a;
        if (mode.equals("1")) {
            for (int i = 0; i < vocabs.size(); i++) {
                System.out.println("\n" + vocabs.get(i).getDefinition() + "\t" + vocabs.get(i).getNotes());
                a = scan.nextLine();
                if (a.equals("sc")) break;
                if (checkDefToTerm(vocabs.get(i).getDefinition(), a)) {
                    System.out.println("Good! Correct!");
                } else {
                    wrong.add(vocabs.get(i));
                    System.out.println("Incorrect!");
                }
                System.out.println("Right Answer: " + vocabs.get(i));
            }
        } else if (mode.equals("2")) {
            for (int i = vocabs.size() - 1; i >= 0; i--) {
                System.out.println("\n" + vocabs.get(i).getDefinition() + "\t" + vocabs.get(i).getNotes());
                a = scan.nextLine();
                if (a.equals("sc")) break;
                if (checkDefToTerm(vocabs.get(i).getDefinition(), a)) {
                    System.out.println("Good! Correct!");
                } else {
                    wrong.add(vocabs.get(i));
                    System.out.println("Incorrect!");
                }
                System.out.println("Right Answer: " + vocabs.get(i));
            }
        } else {
            Random r = new Random();
            while (check.size() < vocabs.size()) {
                int b = r.nextInt(vocabs.size());
                if (!check.contains(b)) {
                    check.add(b);
                    System.out.println("\n" + vocabs.get(b).getDefinition() + "\t" + vocabs.get(b).getNotes());
                    a = scan.nextLine();
                    if (a.equals("sc")) break;
                    if (checkDefToTerm(vocabs.get(b).getDefinition(), a)) {
                        System.out.println("Good! Correct!");
                    } else {
                        wrong.add(vocabs.get(b));
                        System.out.println("Incorrect!");
                    }
                    System.out.println("Right Answer: " + vocabs.get(b));
                }
            }
        }
        System.out.println("\nWrong: ");
        if (wrong.isEmpty()) System.out.println("N/A");
        else for (Vocab voc : wrong) if (voc.getTerm() != null) System.out.println(voc);
    }

    public void game2(String mode) {
        wrong = new ArrayList<>();
        check = new ArrayList<>();
        String c;
        if (mode.equals("1")) {
            for (int i = 0; i < vocabs.size(); i++) {
                System.out.println("\n" + vocabs.get(i).getTerm() + "\t" + vocabs.get(i).getNotes());
                c = scan.nextLine();
                if (c.equals("sc")) break;
                if (checkTermToDef(vocabs.get(i).getTerm(), c)) {
                    System.out.println("Good! Correct!");
                } else {
                    wrong.add(vocabs.get(i));
                    System.out.println("Incorrect!");
                }
                System.out.println("Right Answer: " + vocabs.get(i));
            }
        } else if (mode.equals("2")) {
            for (int i = vocabs.size() - 1; i >= 0; i--) {
                System.out.println("\n" + vocabs.get(i).getTerm() + "\t" + vocabs.get(i).getNotes());
                c = scan.nextLine();
                if (c.equals("sc")) break;
                if (checkTermToDef(vocabs.get(i).getTerm(), c)) {
                    System.out.println("Good! Correct!");
                } else {
                    wrong.add(vocabs.get(i));
                    System.out.println("Incorrect!");
                }
                System.out.println("Right Answer: " + vocabs.get(i));
            }
        } else {
            Random r = new Random();
            while (check.size() < vocabs.size()) {
                int b = r.nextInt(vocabs.size());
                if (!check.contains(b)) {
                    check.add(b);
                    System.out.println("\n" + vocabs.get(b).getTerm() + "\t" + vocabs.get(b).getNotes());
                    c = scan.nextLine();
                    if (c.equals("sc")) break;
                    if (checkTermToDef(vocabs.get(b).getTerm(), c)) {
                        System.out.println("Good! Correct!");
                    } else {
                        wrong.add(vocabs.get(b));
                        System.out.println("Incorrect!");
                    }
                    System.out.println("Right Answer: " + vocabs.get(b));
                }
            }
        }
        System.out.println("\nWrong: ");
        if (wrong.isEmpty()) System.out.print("N/A");
        else for (Vocab voc : wrong) if (voc.getTerm() != null) System.out.println(voc);
    }

    // ── ACCESSORS ─────────────────────────────────────────────────────────────

    public List<Vocab> getVocabs()  { return vocabs; }
    public List<Vocab> getWrongs() { return wrong; }

    // ── PERSISTENCE ───────────────────────────────────────────────────────────

    /** Public save trigger — needed after in-place mutations (e.g. AI enhancement). */
    public void forceSave() {
        saveEvents();
    }

    private void saveEvents() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(vocabs);
        } catch (IOException e) {
            System.out.println("Error saving events: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadEvents() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                if (desc.getName().equals("Vocab")) {
                    return com.vocabmaster.Vocab.class;
                }
                return super.resolveClass(desc);
            }
        }) {
            vocabs = (ArrayList<Vocab>) ois.readObject();
        } catch (FileNotFoundException e) {
            // First run — no file yet
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading events: " + e.getMessage());
        }
    }
}