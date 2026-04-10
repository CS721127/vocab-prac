import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static Map<String, String> en = new HashMap<>();
    private static Map<String, String> cn = new HashMap<>();
    private static boolean isEnglish = true;

    static {
        // ── English ──────────────────────────────────────────────────────────
        en.put("app.title", "Vocab Master");
        en.put("menu.file", "File");
        en.put("menu.new_list", "New List");
        en.put("menu.open_list", "Open List");
        en.put("menu.import", "Import Recommended Lists");
        en.put("menu.import_csv", "Import from Excel/CSV...");
        en.put("menu.import_mdx", "Import from MDX (tab-delimited)...");
        en.put("menu.export_csv", "Export to CSV (Excel)...");
        en.put("menu.export_pdf", "Export to PDF (Print)...");
        en.put("menu.language", "Language");
        en.put("btn.add", "Add Word");
        en.put("btn.delete", "Delete Selected");
        en.put("btn.practice", "Practice Mode");
        en.put("btn.reset", "Reset All");
        en.put("btn.ai_enhance", "AI Enhance");
        en.put("btn.search", "Search");
        en.put("col.term", "Term");
        en.put("col.def", "Definition");
        en.put("col.example", "Example");
        en.put("col.notes", "Notes");
        en.put("status.ready", " Ready");
        en.put("status.search_results", " Found %d result(s) for \"%s\"");
        en.put("status.no_results", " No local results for \"%s\"");
        en.put("dialog.add.title", "Add New Word");
        en.put("dialog.add.save", "Save");
        en.put("msg.term_exists", "Term already exists!");
        en.put("msg.required", "Term and Definition are required.");
        en.put("msg.confirm_delete", "Delete '%s'?");
        en.put("msg.confirm_reset", "Are you sure you want to DELETE ALL DATA?\nThis cannot be undone.");
        en.put("msg.practice_empty", "Add some words first!");
        en.put("practice.title", "Practice Mode");
        en.put("practice.mode", "Direction:");
        en.put("practice.order", "Order:");
        en.put("practice.order.sequential", "Sequential");
        en.put("practice.order.reversed", "Reversed");
        en.put("practice.order.random", "Random");
        en.put("practice.check", "Check");
        en.put("practice.next", "Next");
        en.put("practice.correct", "✓  Correct!");
        en.put("practice.incorrect", "✗  Incorrect. Answer: %s");
        en.put("practice.complete", "Practice Complete!");
        en.put("practice.results.title", "Round Results");
        en.put("practice.results.perfect", "🎉 Perfect! No mistakes!");
        en.put("practice.results.wrong_count", "You got %d word(s) wrong:");
        en.put("practice.retry", "Practice Wrong Words Again");
        en.put("practice.stop", "Stop");
        en.put("practice.progress", "Question %d / %d");
        en.put("list.created", "List '%s' created and loaded.");
        en.put("list.loaded", "List '%s' loaded.");
        en.put("list.imported", "List '%s' imported successfully.");
        en.put("import.success", "Imported %d words successfully.");
        en.put("import.error", "Import failed: %s");
        en.put("export.success", "Exported to:\n%s");
        en.put("export.error", "Export failed: %s");
        en.put("export.pdf.hint", "File saved. Open it in your browser and press Ctrl+P (⌘P) to Save as PDF.");
        en.put("search.hint", "Search term or definition...");
        en.put("search.not_found", "No local results. Search via AI?");
        en.put("search.ai_prompt", "Searching Gemma 3 AI for: \"%s\"");
        en.put("search.clear", "Show All");
        en.put("ai.title", "AI Enhancement");
        en.put("ai.confirm", "AI will enhance definitions and add example sentences for all %d words.\nProceed?");
        en.put("ai.running", "Enhancing vocabulary with AI...");
        en.put("ai.done", "AI enhancement complete. %d words updated.");
        en.put("ai.no_key", "Gemma API Key not configured.\n\nPlease open Settings and enter your API key.\n\n(Endpoint: https://aistudio.google.com/)");
        en.put("detail.title", "Full Definition — %s");

        // ── Chinese ───────────────────────────────────────────────────────────
        cn.put("app.title", "词汇大师 (Vocab Master)");
        cn.put("menu.file", "文件");
        cn.put("menu.new_list", "新建列表");
        cn.put("menu.open_list", "打开列表");
        cn.put("menu.import", "导入推荐列表");
        cn.put("menu.import_csv", "从 Excel/CSV 导入...");
        cn.put("menu.import_mdx", "从 MDX（Tab分隔）导入...");
        cn.put("menu.export_csv", "导出为 CSV（Excel）...");
        cn.put("menu.export_pdf", "导出为 PDF（打印版）...");
        cn.put("menu.language", "语言 (Language)");
        cn.put("btn.add", "添加单词");
        cn.put("btn.delete", "删除选中");
        cn.put("btn.practice", "练习模式");
        cn.put("btn.reset", "重置所有");
        cn.put("btn.ai_enhance", "AI 完善");
        cn.put("btn.search", "搜索");
        cn.put("col.term", "词汇");
        cn.put("col.def", "定义/释义");
        cn.put("col.example", "例句");
        cn.put("col.notes", "笔记");
        cn.put("status.ready", " 就绪");
        cn.put("status.search_results", " 找到 %d 个结果，关键词：\"%s\"");
        cn.put("status.no_results", " 本地未找到 \"%s\"");
        cn.put("dialog.add.title", "添加新词");
        cn.put("dialog.add.save", "保存");
        cn.put("msg.term_exists", "词汇已存在！");
        cn.put("msg.required", "词汇和定义是必填项。");
        cn.put("msg.confirm_delete", "删除 '%s'?");
        cn.put("msg.confirm_reset", "确定要删除所有数据吗？\n此操作无法撤销。");
        cn.put("msg.practice_empty", "请先添加一些单词！");
        cn.put("practice.title", "练习模式");
        cn.put("practice.mode", "方向：");
        cn.put("practice.order", "顺序：");
        cn.put("practice.order.sequential", "正序");
        cn.put("practice.order.reversed", "倒序");
        cn.put("practice.order.random", "随机");
        cn.put("practice.check", "检查");
        cn.put("practice.next", "下一个");
        cn.put("practice.correct", "✓  正确！");
        cn.put("practice.incorrect", "✗  错误。答案：%s");
        cn.put("practice.complete", "练习完成！");
        cn.put("practice.results.title", "本轮结果");
        cn.put("practice.results.perfect", "🎉 完美！没有错误！");
        cn.put("practice.results.wrong_count", "答错了 %d 个词：");
        cn.put("practice.retry", "重新练习错误词汇");
        cn.put("practice.stop", "停止");
        cn.put("practice.progress", "第 %d / %d 题");
        cn.put("list.created", "列表 '%s' 已创建并加载。");
        cn.put("list.loaded", "列表 '%s' 已加载。");
        cn.put("list.imported", "列表 '%s' 导入成功。");
        cn.put("import.success", "成功导入 %d 个词汇。");
        cn.put("import.error", "导入失败：%s");
        cn.put("export.success", "已导出到：\n%s");
        cn.put("export.error", "导出失败：%s");
        cn.put("export.pdf.hint", "文件已保存。在浏览器中打开，按 ⌘P 选择「存储为PDF」即可打印。");
        cn.put("search.hint", "搜索词汇或释义...");
        cn.put("search.not_found", "本地未找到，是否通过 AI 搜索？");
        cn.put("search.ai_prompt", "正在通过 Gemma 3 AI 搜索：\"%s\"");
        cn.put("search.clear", "显示全部");
        cn.put("ai.title", "AI 完善");
        cn.put("ai.confirm", "AI 将为 %d 个词汇完善释义并添加例句。\n是否继续？");
        cn.put("ai.running", "正在使用 AI 完善词汇...");
        cn.put("ai.done", "AI 完善完成，已更新 %d 个词汇。");
        cn.put("ai.no_key", "未配置 Gemma API 密钥。\n\n请在「设置」中输入您的 API 密钥。\n\n（接口地址：https://aistudio.google.com/）");
        cn.put("detail.title", "完整释义 — %s");
    }

    public static void setLanguage(boolean english) { isEnglish = english; }
    public static boolean isEnglish() { return isEnglish; }

    public static String get(String key) {
        return isEnglish ? en.getOrDefault(key, key) : cn.getOrDefault(key, key);
    }

    public static String get(String key, Object... args) {
        String template = get(key);
        return String.format(template, args);
    }
}
