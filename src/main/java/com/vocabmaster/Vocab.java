package com.vocabmaster;

import java.io.*;

class Vocab implements Serializable {
    private static final long serialVersionUID = 1L;
    private String term;
    private String definition;
    private String notes;
    private String example; // New: example sentence

    public Vocab(String term, String definition, String notes) {
        this.term = term;
        this.definition = definition;
        this.notes = notes;
        this.example = "";
    }

    public Vocab(String term, String definition, String notes, String example) {
        this.term = term;
        this.definition = definition;
        this.notes = notes;
        this.example = (example != null) ? example : "";
    }

    // Getters
    public String getTerm()       { return term; }
    public String getDefinition() { return definition; }
    public String getNotes()      { return notes; }
    public String getExample()    { return (example != null) ? example : ""; }

    // Setters (for AI enhancement)
    public void setDefinition(String definition) { this.definition = definition; }
    public void setExample(String example)       { this.example = (example != null) ? example : ""; }
    public void setNotes(String notes)           { this.notes = notes; }

    /**
     * Custom deserialization for backward compatibility with serialVersionUID=1L files.
     * Old files won't have the 'example' field — default it to empty string.
     */
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        ois.defaultReadObject();
        if (example == null) {
            example = "";
        }
    }

    @Override
    public String toString() {
        return String.format("%-20s %-30s %s", term, definition, notes);
    }
}