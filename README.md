# Vocab Master

Vocab Master is a lightweight, local-first Java Swing application designed to help you build, study, and master vocabulary effectively. 

## ✨ Features

- **Practice Mode**: Test your knowledge in Sequential, Reversed, or Random orders.
- **Wrong-Word Retry Loop**: Automatically tracks errors. Re-test exactly the words you got wrong until you achieve a perfect score.
- **AI Enhancement**: Uses **Google Gemma 3 27B** to automatically provide precise definitions and natural example sentences for your entire vocabulary list.
- **AI Search**: Local search missing a word? Search it seamlessly through Gemma AI and natively add it to your list.
- **Example Sentences**: Robust support for an 'Example' column mapped cleanly into a smooth 4-column UI.
- **High-Quality Print & Export**: 
  - Produce perfectly paginated PDF study guides directly via HTML styling.
  - Export/Import to CSV (Open natively in Microsoft Excel).
  - Import raw terms from MDX database tab-delimited exports.
- **Smart UI Truncation**: Easily view deeply nested long definitions with a simple "double-click to expand modal" overlay.

## 🚀 Getting Started

### Prerequisites
- **Java SE Development Kit (JDK) 8+** (or newer) needs to be installed on your machine.

### Build and Run
1. Clone the repository or navigate to the source directory.
2. Compile and run the project via the terminal:

```bash
javac *.java
java SwingApp
```

### Configuring AI Features (Google Gemma API)
1. Within the Vocab Master window, click on **File > Settings**.
2. Visit [Google AI Studio](https://aistudio.google.com/) to obtain a free API key.
3. Drop the API key (`AIza...`) into the **Gemma API Key** field.
4. Hit **Save**. You're now ready to use the "AI Enhance" tool and the AI Search Fallback!

## 💾 Storage & Data

Vocab Master uses local serialization mapping without spinning up heavy databases. 
- Custom lists are stored in `~/.vocabapp/`
- User configurations are similarly stored under `~/.vocabapp/config.properties`.

*Enjoy zero-latency loading and a true local-first application!*
