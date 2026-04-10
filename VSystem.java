import java.io.*;
import java.util.*;

class VSystem {
    private List<Vocab> vocabs;
    private String fileName;
    private List<Integer> check;
    private List<Vocab> wrong;
    private Scanner scan;

    public VSystem(String listName) {
        // Store in ~/.vocabapp/
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
                    lists.add(name.substring(0, name.length() - 11)); // Remove "_events.dat"
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

    public boolean addVocab(String term, String definition, String notes) {
        for (Vocab voc : vocabs) {
            if (voc.getTerm().equalsIgnoreCase(term)) {
                System.out.println("Repeated Term!");
                return false;
            }
        }
        boolean b = vocabs.add(new Vocab(term, definition, notes));
        saveEvents();
        return b;
    }

    public boolean deleteVocab(String term) {
        boolean a = vocabs.removeIf(event -> event.getTerm().equalsIgnoreCase(term));
        saveEvents();
        return a;
    }

    public boolean printVocab() {
        boolean a = false;
        if (!vocabs.isEmpty()) {
            System.out.println(String.format("%-20s %-30s %s", "Term", "Definition", "Notes"));
            System.out.println("------------------------------------------------------------");
        }
        for (Vocab voc : vocabs) {
            if (voc.getTerm() != null) {
                System.out.println(voc);
                a = true;
            }
        }
        return a;
    }

    public boolean deleteAll() {
        vocabs.clear();
        saveEvents();
        return vocabs.isEmpty();
    }

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

    public void game1(String mode) { // Definition -> Term
        wrong = new ArrayList<>();
        check = new ArrayList<>();
        String a;
        if (mode.equals("1")) { // Normal order
            for (int i = 0; i < vocabs.size(); i++) {
                System.out.println("\n" + vocabs.get(i).getDefinition() + "\t" + vocabs.get(i).getNotes());
                a = scan.nextLine();
                if (a.equals("sc")){
                    break;
                }
                if (checkDefToTerm(vocabs.get(i).getDefinition(), a)) {
                    System.out.println("Good! Correct!");
                } else {
                    wrong.add(vocabs.get(i));
                    System.out.println("Incorrect!");
                }
                System.out.println("Right Answer: " + vocabs.get(i));
            }
        } else if (mode.equals("2")) { // Reverse order
            for (int i = vocabs.size() - 1; i >= 0; i--) {
                System.out.println("\n" + vocabs.get(i).getDefinition() + "\t" + vocabs.get(i).getNotes());
                a = scan.nextLine();
                if (a.equals("sc")){
                    break;
                }
                if (checkDefToTerm(vocabs.get(i).getDefinition(), a)) {
                    System.out.println("Good! Correct!");
                } else {
                    wrong.add(vocabs.get(i));
                    System.out.println("Incorrect!");
                }
                System.out.println("Right Answer: " + vocabs.get(i));
            }
        } else { // Random
            Random r = new Random();
            while (check.size() < vocabs.size()) {
                int b = r.nextInt(vocabs.size());
                if (!check.contains(b)) {
                    check.add(b);
                    System.out.println("\n" + vocabs.get(b).getDefinition() + "\t" + vocabs.get(b).getNotes());
                    a = scan.nextLine();
                    if (a.equals("sc")){
                        break;
                    }
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
        if (wrong.isEmpty()){
            System.out.println("N/A");
        }
        else {
            for (Vocab voc : wrong) {
                if (voc.getTerm() != null) {
                    System.out.println(voc);
                }
            }
        }
    }

    public void game2(String mode) { // Term -> Definition
        wrong = new ArrayList<>();
        check = new ArrayList<>();
        String c;
        if (mode.equals("1")) { // Normal
            for (int i = 0; i < vocabs.size(); i++) {
                System.out.println("\n" + vocabs.get(i).getTerm() + "\t" + vocabs.get(i).getNotes());
                c = scan.nextLine();
                if (c.equals("sc")){
                    break;
                }
                if (checkTermToDef(vocabs.get(i).getTerm(), c)) {
                    System.out.println("Good! Correct!");
                } else {
                    wrong.add(vocabs.get(i));
                    System.out.println("Incorrect!");
                }
                System.out.println("Right Answer: " + vocabs.get(i));
            }
        } else if (mode.equals("2")) { // Reverse
            for (int i = vocabs.size() - 1; i >= 0; i--) {
                System.out.println("\n" + vocabs.get(i).getTerm() + "\t" + vocabs.get(i).getNotes());
                c = scan.nextLine();
                if (c.equals("sc")){
                    break;
                }
                if (checkTermToDef(vocabs.get(i).getTerm(), c)) {
                    System.out.println("Good! Correct!");
                } else {
                    wrong.add(vocabs.get(i));
                    System.out.println("Incorrect!");
                }
                System.out.println("Right Answer: " + vocabs.get(i));
            }
        } else { // Random
            Random r = new Random();
            while (check.size() < vocabs.size()) {
                int b = r.nextInt(vocabs.size());
                if (!check.contains(b)) {
                    check.add(b);
                    System.out.println("\n" + vocabs.get(b).getTerm() + "\t" + vocabs.get(b).getNotes());
                    c = scan.nextLine();
                    if (c.equals("sc")){
                        break;
                    }
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
        if (wrong.isEmpty()){
            System.out.print("N/A");
        }
        else {
            for (Vocab voc : wrong) {
                if (voc.getTerm() != null) {
                    System.out.println(voc);
                }
            }
        }
    }

    public void game3(String mode, String choice) { // Flashcard
        wrong = new ArrayList<>();
        check = new ArrayList<>();
        String a;
        switch (mode) {
            case "1": // Normal
                if (choice.equals("2")) { // Term -> Definition
                    for (int i = 0; i < vocabs.size(); i++) {
                        System.out.println("\n" + vocabs.get(i).getTerm());
                        a = scan.nextLine();
                        if (a.equals("x")) {
                            wrong.add(vocabs.get(i));
                        }
                        if (a.equals("sc")){
                            break;
                        }
                        System.out.println("Right Answer: " + vocabs.get(i));
                    }
                } else { // Definition -> Term
                    for (int i = 0; i < vocabs.size(); i++) {
                        System.out.println("\n" + vocabs.get(i).getDefinition());
                        a = scan.nextLine();
                        if (a.equals("x")) {
                            wrong.add(vocabs.get(i));
                        }
                        if (a.equals("sc")){
                            break;
                        }
                        System.out.println("Right Answer: " + vocabs.get(i));
                    }
                }
                break;
            case "2": // Reverse
                if (choice.equals("2")) { // Term -> Definition
                    for (int i = vocabs.size() - 1; i >= 0; i--) {
                        System.out.println("\n" + vocabs.get(i).getTerm());
                        a = scan.nextLine();
                        if (a.equals("x")) {
                            wrong.add(vocabs.get(i));
                        }
                        if (a.equals("sc")){
                            break;
                        }
                        System.out.println("Right Answer: " + vocabs.get(i));
                    }
                } else { // Definition -> Term
                    for (int i = vocabs.size() - 1; i >= 0; i--) {
                        System.out.println("\n" + vocabs.get(i).getDefinition());
                        a = scan.nextLine();
                        if (a.equals("x")) {
                            wrong.add(vocabs.get(i));
                        }
                        if (a.equals("sc")){
                            break;
                        }
                        System.out.println("Right Answer: " + vocabs.get(i));
                    }
                }
                break;
            case "3": // Random
                Random r = new Random();
                while (check.size() < vocabs.size()) {
                    int b = r.nextInt(vocabs.size());
                    if (!check.contains(b)) {
                        check.add(b);
                        if (choice.equals("2")) { // Term -> Definition
                            System.out.println("\n" + vocabs.get(b).getTerm());
                        } else { // Definition -> Term
                            System.out.println("\n" + vocabs.get(b).getDefinition());
                        }
                        a = scan.nextLine();
                        if (a.equals("x")) {
                            wrong.add(vocabs.get(b));
                        }
                        if (a.equals("sc")){
                            break;
                        }
                        System.out.println("Right Answer: " + vocabs.get(b));
                    }
                }
                break;
        }
        System.out.println("\nCheck: ");
        if (wrong.isEmpty()){
            System.out.println("N/A");
        }
        else {
            for (Vocab voc : wrong) {
                if (voc.getTerm() != null) {
                    System.out.println(voc);
                }
            }
        }
    }

    public List<Vocab> getVocabs(){
        return vocabs;
    }

    public List<Vocab> getWrongs(){
        return wrong;
    }

    private void saveEvents() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName))) {
            oos.writeObject(vocabs);
        } catch (IOException e) {
            System.out.println("Error saving events: " + e.getMessage());
        }
    }

    private void loadEvents() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            vocabs = (ArrayList<Vocab>) ois.readObject();
        } catch (FileNotFoundException e) {
            // File not found, nothing to load
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Error loading events: " + e.getMessage());
        }
    }
}