package com.vocabmaster;

import java.util.*;

public class PresetLists {
    public static Map<String, List<Vocab>> getPresets() {
        Map<String, List<Vocab>> presets = new LinkedHashMap<>();

        // HSK Level 1 (Sample)
        List<Vocab> hsk1 = new ArrayList<>();
        hsk1.add(new Vocab("爱", "Love", "Verb/Noun"));
        hsk1.add(new Vocab("八", "Eight", "Number"));
        hsk1.add(new Vocab("爸爸", "Dad", "Noun"));
        hsk1.add(new Vocab("杯子", "Cup", "Noun"));
        hsk1.add(new Vocab("北京", "Beijing", "Place"));
        hsk1.add(new Vocab("本", "Measure word for books", "Measure Word"));
        hsk1.add(new Vocab("不客气", "You're welcome", "Phrase"));
        presets.put("HSK_Level_1_Sample", hsk1);

        // TOEFL Essential (Sample)
        List<Vocab> toefl = new ArrayList<>();
        toefl.add(new Vocab("Abundant", "Present in large quantities", "Adjective"));
        toefl.add(new Vocab("Accumulate", "Gather together or acquire", "Verb"));
        toefl.add(new Vocab("Accurate", "Correct in all details", "Adjective"));
        toefl.add(new Vocab("Adapt", "Make suitable for a new use", "Verb"));
        toefl.add(new Vocab("Adequate", "Satisfactory or acceptable", "Adjective"));
        presets.put("TOEFL_Essential_Sample", toefl);

        // Travel Phrases (Sample)
        List<Vocab> travel = new ArrayList<>();
        travel.add(new Vocab("Hello", "你好 (Nǐ hǎo)", "Greeting"));
        travel.add(new Vocab("Thank you", "谢谢 (Xièxiè)", "Polite"));
        travel.add(new Vocab("How much?", "多少钱? (Duōshǎo qián?)", "Shopping"));
        travel.add(new Vocab("Where is the bathroom?", "洗手间在哪里? (Xǐshǒujiān zài nǎlǐ?)", "Question"));
        travel.add(new Vocab("I don't understand", "我听不懂 (Wǒ tīng bù dǒng)", "Phrase"));
        presets.put("Travel_Phrases_Sample", travel);

        return presets;
    }
}
