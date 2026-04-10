import java.util.HashMap;
import java.util.Map;

public class LanguageManager {
    private static Map<String, String> en = new HashMap<>();
    private static Map<String, String> cn = new HashMap<>();
    private static boolean isEnglish = true;

    static {
        // English
        en.put("app.title", "Vocab Master");
        en.put("menu.file", "File");
        en.put("menu.new_list", "New List");
        en.put("menu.open_list", "Open List");
        en.put("menu.import", "Import Recommended Lists");
        en.put("menu.language", "Language");
        en.put("btn.add", "Add Word");
        en.put("btn.delete", "Delete Selected");
        en.put("btn.practice", "Practice Mode");
        en.put("btn.reset", "Reset All");
        en.put("col.term", "Term");
        en.put("col.def", "Definition");
        en.put("col.notes", "Notes");
        en.put("status.ready", " Ready");
        en.put("dialog.add.title", "Add New Word");
        en.put("dialog.add.save", "Save");
        en.put("msg.term_exists", "Term already exists!");
        en.put("msg.required", "Term and Definition are required.");
        en.put("msg.confirm_delete", "Delete '%s'?");
        en.put("msg.confirm_reset", "Are you sure you want to DELETE ALL DATA?\nThis cannot be undone.");
        en.put("msg.practice_empty", "Add some words first!");
        en.put("practice.title", "Practice Mode");
        en.put("practice.mode", "Mode:");
        en.put("practice.check", "Check");
        en.put("practice.next", "Next");
        en.put("practice.correct", "Correct!");
        en.put("practice.incorrect", "Incorrect. Answer: %s");
        en.put("practice.complete", "Practice Complete!");
        en.put("list.created", "List '%s' created and loaded.");
        en.put("list.loaded", "List '%s' loaded.");
        en.put("list.imported", "List '%s' imported successfully.");

        // Chinese
        cn.put("app.title", "词汇大师 (Vocab Master)");
        cn.put("menu.file", "文件");
        cn.put("menu.new_list", "新建列表");
        cn.put("menu.open_list", "打开列表");
        cn.put("menu.import", "导入推荐列表");
        cn.put("menu.language", "语言 (Language)");
        cn.put("btn.add", "添加单词");
        cn.put("btn.delete", "删除选中");
        cn.put("btn.practice", "练习模式");
        cn.put("btn.reset", "重置所有");
        cn.put("col.term", "词汇");
        cn.put("col.def", "定义/释义");
        cn.put("col.notes", "笔记");
        cn.put("status.ready", " 就绪");
        cn.put("dialog.add.title", "添加新词");
        cn.put("dialog.add.save", "保存");
        cn.put("msg.term_exists", "词汇已存在！");
        cn.put("msg.required", "词汇和定义是必填项。");
        cn.put("msg.confirm_delete", "删除 '%s'?");
        cn.put("msg.confirm_reset", "确定要删除所有数据吗？\n此操作无法撤销。");
        cn.put("msg.practice_empty", "请先添加一些单词！");
        cn.put("practice.title", "练习模式");
        cn.put("practice.mode", "模式:");
        cn.put("practice.check", "检查");
        cn.put("practice.next", "下一个");
        cn.put("practice.correct", "正确！");
        cn.put("practice.incorrect", "错误。答案: %s");
        cn.put("practice.complete", "练习完成！");
        cn.put("list.created", "列表 '%s' 已创建并加载。");
        cn.put("list.loaded", "列表 '%s' 已加载。");
        cn.put("list.imported", "列表 '%s' 导入成功。");
    }

    public static void setLanguage(boolean english) {
        isEnglish = english;
    }

    public static boolean isEnglish() {
        return isEnglish;
    }

    public static String get(String key) {
        return isEnglish ? en.getOrDefault(key, key) : cn.getOrDefault(key, key);
    }
    
    public static String get(String key, Object... args) {
        String template = get(key);
        return String.format(template, args);
    }
}
