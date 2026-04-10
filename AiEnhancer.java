import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * AiEnhancer — Google Gemma 3 API integration for vocabulary enhancement and search.
 *
 * Use endpoint: POST https://generativelanguage.googleapis.com/v1beta/models/gemma-3-27b-it:generateContent?key=API_KEY
 */
public class AiEnhancer {

    private static final String API_BASE_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/gemma-3-27b-it:generateContent?key=";
    private static final String CONFIG_KEY = "ai.api_key";

    // ── API Key management ────────────────────────────────────────────────────

    public static String loadApiKey() {
        try {
            String userHome = System.getProperty("user.home");
            File cfg = new File(new File(userHome, ".vocabapp"), "config.properties");
            if (!cfg.exists()) return null;
            java.util.Properties props = new java.util.Properties();
            try (FileInputStream in = new FileInputStream(cfg)) {
                props.load(in);
            }
            String key = props.getProperty(CONFIG_KEY, "").trim();
            return key.isEmpty() ? null : key;
        } catch (IOException e) {
            return null;
        }
    }

    public static void saveApiKey(String key) throws IOException {
        String userHome = System.getProperty("user.home");
        File appDir = new File(userHome, ".vocabapp");
        appDir.mkdirs();
        File cfg = new File(appDir, "config.properties");
        java.util.Properties props = new java.util.Properties();
        if (cfg.exists()) {
            try (FileInputStream in = new FileInputStream(cfg)) {
                props.load(in);
            }
        }
        props.setProperty(CONFIG_KEY, key);
        try (FileOutputStream out = new FileOutputStream(cfg)) {
            props.store(out, "Vocab Master Config");
        }
    }

    // ── AI Calls ──────────────────────────────────────────────────────────────

    /**
     * Enhances a single vocab's definition and example sentence via Gemma 3 API.
     * Kept for single search usage.
     */
    public static String[] enhanceVocab(String term, String existingDef, String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) return null;

        String prompt = "You are a vocabulary assistant. Enhance the entry for the word: \"" + term + "\".\n" +
            "Current definition: \"" + existingDef + "\"\n\n" +
            "Provide an improved, clear definition and a natural example sentence.\n" +
            "Respond ONLY in this exact plain text format:\n" +
            "Definition: [your definition here]\n" +
            "Example: [your example here]";
            
        String response = callApi(prompt, apiKey);
        if (response == null) return null;

        String def = extractField(response, "Definition:");
        String ex = extractField(response, "Example:");
        
        if (def == null && ex == null) {
            def = response.trim();
            ex = "";
        }
        return new String[]{ def != null ? def : existingDef, ex != null ? ex : "" };
    }

    /**
     * Batch processes multiple words at once to reduce API calls and avoid 503 Rate Limits.
     * Returns a map of Term -> String[]{enhancedDefinition, example}
     */
    public static Map<String, String[]> enhanceVocabBatch(List<Vocab> batch, String apiKey) {
        if (apiKey == null || apiKey.isEmpty() || batch.isEmpty()) return null;

        StringBuilder prompt = new StringBuilder();
        prompt.append("You are a vocabulary assistant. I will provide a list of vocabulary words and their basic definitions.\n");
        prompt.append("For EACH word, provide an improved/clear definition and a natural example sentence.\n\n");
        prompt.append("You MUST separate each word's response with exactly '===ENTRY===' on its own line.\n");
        prompt.append("Format EACH entry EXACTLY as follows:\n");
        prompt.append("Term: [The term]\n");
        prompt.append("Definition: [Improved definition]\n");
        prompt.append("Example: [Example sentence]\n\n");
        prompt.append("Here are the words to enhance:\n\n");

        for (Vocab v : batch) {
            prompt.append("Word: ").append(v.getTerm()).append("\n");
            prompt.append("Current: ").append(v.getDefinition()).append("\n\n");
        }

        String response = callApi(prompt.toString(), apiKey);
        if (response == null) return null;

        Map<String, String[]> results = new HashMap<>();
        // Split by the delimiter we requested
        String[] entries = response.split("===ENTRY===");
        
        for (String entry : entries) {
            entry = entry.trim();
            if (entry.isEmpty()) continue;
            
            String term = extractField(entry, "Term:");
            String def = extractField(entry, "Definition:");
            String ex = extractField(entry, "Example:");
            
            if (term != null && !term.isEmpty()) {
                // If AI hallucinated a bit with markdown, trim it
                if (term.startsWith("**") && term.endsWith("**")) term = term.substring(2, term.length() - 2);
                if (def == null) def = "";
                if (ex == null) ex = "";
                results.put(term.toLowerCase(), new String[]{def, ex});
            }
        }
        return results;
    }

    public static String[] searchTerm(String query, String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) return null;

        String prompt = "Look up the vocabulary term or phrase: \"" + query + "\"\n" +
            "Be concise. Respond ONLY in this exact plain text format:\n" +
            "Term: [the term]\n" +
            "Definition: [definition]\n" +
            "Example: [example sentence]\n" +
            "Notes: [part of speech / notes]\n";

        String response = callApi(prompt, apiKey);
        if (response == null) return null;

        String term    = extractField(response, "Term:");
        String def     = extractField(response, "Definition:");
        String example = extractField(response, "Example:");
        String notes   = extractField(response, "Notes:");

        if (term == null) term = query;
        if (def  == null) def  = response.trim();
        if (example == null) example = "";
        if (notes   == null) notes   = "";

        return new String[]{ term, def, example, notes };
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    /**
     * Extracts a field from a plain text response 
     * e.g., "Definition: apple is a fruit\nExample: I ate an apple."
     */
    private static String extractField(String text, String fieldName) {
        String searchField = fieldName;
        int idx = text.indexOf(searchField);
        if (idx == -1) return null;
        
        idx += searchField.length();
        int endIdx = text.indexOf("\n", idx);
        
        // If there's another known label indicating the end, use that (for multi-line text)
        int nextLabel1 = text.indexOf("Term:", idx);
        int nextLabel2 = text.indexOf("Definition:", idx);
        int nextLabel3 = text.indexOf("Example:", idx);
        int nextLabel4 = text.indexOf("Notes:", idx);
        
        int minNext = Integer.MAX_VALUE;
        if (nextLabel1 != -1) minNext = Math.min(minNext, nextLabel1);
        if (nextLabel2 != -1) minNext = Math.min(minNext, nextLabel2);
        if (nextLabel3 != -1) minNext = Math.min(minNext, nextLabel3);
        if (nextLabel4 != -1) minNext = Math.min(minNext, nextLabel4);
        
        // If a known label comes after, capture multi-line up to that label
        if (minNext != Integer.MAX_VALUE) {
            return text.substring(idx, minNext).trim();
        }
        
        if (endIdx == -1) {
            return text.substring(idx).trim();
        } else {
            // For safety, assume the rest of the block if no other labels exist
            return text.substring(idx).trim();
        }
    }

    private static String callApi(String prompt, String apiKey) {
        try {
            URL url = new URL(API_BASE_URL + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(45000); // 45 seconds to allow batch processing
            conn.setDoOutput(true);

            // Build JSON body for Google Gemini/Gemma models
            String body = "{" +
                "\"contents\": [{\"parts\":[{\"text\":" + jsonString(prompt) + "}]}]" +
                "}";

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body.getBytes(StandardCharsets.UTF_8));
            }

            int status = conn.getResponseCode();
            InputStream is = (status >= 200 && status < 300) ? conn.getInputStream() : conn.getErrorStream();
            String responseBody = readStream(is);

            if (status != 200) {
                System.err.println("AI API error " + status + ": " + responseBody);
                return null;
            }

            // Extract 'text' from Gemini format:
            String content = extractJsonText(responseBody);
            return content;

        } catch (Exception e) {
            System.err.println("AI call failed: " + e.getMessage());
            return null;
        }
    }

    private static String readStream(InputStream is) throws IOException {
        if (is == null) return "";
        BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) sb.append(line).append("\n");
        return sb.toString();
    }
    
    /** Primitive JSON string extractor for the "text" field */
    private static String extractJsonText(String json) {
        if (json == null) return null;
        String search = "\"text\":";
        int idx = json.indexOf(search);
        if (idx < 0) return null;
        idx += search.length();
        while (idx < json.length() && (json.charAt(idx) == ' ' || json.charAt(idx) == ':')) idx++;
        if (idx >= json.length()) return null;
        
        if (json.charAt(idx) == '"') {
            StringBuilder sb = new StringBuilder();
            idx++;
            while (idx < json.length()) {
                char c = json.charAt(idx);
                if (c == '\\' && idx + 1 < json.length()) {
                    char next = json.charAt(idx + 1);
                    if (next == '"') sb.append('"');
                    else if (next == 'n') sb.append('\n');
                    else if (next == 't') sb.append('\t');
                    else if (next == '\\') sb.append('\\');
                    else sb.append(next);
                    idx += 2;
                } else if (c == '"') {
                    break;
                } else {
                    sb.append(c);
                    idx++;
                }
            }
            return sb.toString();
        }
        return null;
    }

    private static String jsonString(String s) {
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                       .replace("\n", "\\n").replace("\r", "\\r")
                       .replace("\t", "\\t") + "\"";
    }
}
