import java.io.*;

class Vocab implements Serializable {
    private static final long serialVersionUID = 1L;
    private String term; 
    private String definition;
    private String notes; 

    public Vocab(String term, String definition, String notes) {
        this.definition = definition;
        this.term = term;
        this.notes = notes;
    }

    public String getTerm() {
        return term;
    }

    public String getDefinition() {
        return definition;
    }
    
    public String getNotes() {
        return notes;
    }

    @Override
    public String toString() {
        // Use String.format for cleaner alignment: Term (20 chars), Definition (30 chars), Notes (rest)
        return String.format("%-20s %-30s %s", term, definition, notes);
    }
}