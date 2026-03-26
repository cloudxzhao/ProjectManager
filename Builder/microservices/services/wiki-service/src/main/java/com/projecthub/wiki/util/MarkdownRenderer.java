package com.projecthub.wiki.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Markdown 渲染工具类
 * 支持基本的 Markdown 语法和代码高亮
 */
@Slf4j
@Component
public class MarkdownRenderer {

    // 代码块匹配正则
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w+)?\\n([\\s\\S]*?)```");

    // 行内代码匹配正则
    private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`([^`]+)`");

    // 标题匹配正则
    private static final Pattern HEADER_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);

    // 粗体匹配正则
    private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*([^*]+)\\*\\*");

    // 斜体匹配正则
    private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*([^*]+)\\*");

    // 链接匹配正则
    private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");

    // 图片匹配正则
    private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[([^\\]]*)\\]\\(([^)]+)\\)");

    // 无序列表匹配正则
    private static final Pattern UNORDERED_LIST_PATTERN = Pattern.compile("^[\\-\\*\\+]\\s+(.+)$", Pattern.MULTILINE);

    // 有序列表匹配正则
    private static final Pattern ORDERED_LIST_PATTERN = Pattern.compile("^\\d+\\.\\s+(.+)$", Pattern.MULTILINE);

    // 引用匹配正则
    private static final Pattern BLOCKQUOTE_PATTERN = Pattern.compile("^>\\s*(.+)$", Pattern.MULTILINE);

    // 水平线匹配正则
    private static final Pattern HORIZONTAL_RULE_PATTERN = Pattern.compile("^(\\-{3,}|\\*{3,}|_{3,})$", Pattern.MULTILINE);

    /**
     * 将 Markdown 转换为 HTML
     * @param markdown Markdown 文本
     * @return HTML 字符串
     */
    public String render(String markdown) {
        if (markdown == null || markdown.isEmpty()) {
            return "";
        }

        String html = markdown;

        // 1. 处理代码块（先处理，避免被其他规则干扰）
        html = renderCodeBlocks(html);

        // 2. 处理行内代码
        html = renderInlineCode(html);

        // 3. 处理标题
        html = renderHeaders(html);

        // 4. 处理粗体
        html = renderBold(html);

        // 5. 处理斜体
        html = renderItalic(html);

        // 6. 处理图片（在链接之前处理）
        html = renderImages(html);

        // 7. 处理链接
        html = renderLinks(html);

        // 8. 处理无序列表
        html = renderUnorderedLists(html);

        // 9. 处理有序列表
        html = renderOrderedLists(html);

        // 10. 处理引用
        html = renderBlockquotes(html);

        // 11. 处理水平线
        html = renderHorizontalRules(html);

        // 12. 处理段落
        html = renderParagraphs(html);

        // 13. 处理换行
        html = renderLineBreaks(html);

        return html;
    }

    /**
     * 渲染代码块
     */
    private String renderCodeBlocks(String html) {
        Matcher matcher = CODE_BLOCK_PATTERN.matcher(html);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String language = matcher.group(1) != null ? matcher.group(1) : "";
            String code = escapeHtml(matcher.group(2));
            String highlighted = highlightCode(code, language);
            matcher.appendReplacement(sb, "<pre class=\"code-block\"><code class=\"language-" + language + "\">" + highlighted + "</code></pre>");
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 渲染行内代码
     */
    private String renderInlineCode(String html) {
        return INLINE_CODE_PATTERN.matcher(html).replaceAll("<code>$1</code>");
    }

    /**
     * 渲染标题
     */
    private String renderHeaders(String html) {
        return HEADER_PATTERN.matcher(html).replaceAll(match -> {
            int level = match.group(1).length();
            String text = match.group(2);
            return "<h" + level + ">" + text + "</h" + level + ">";
        });
    }

    /**
     * 渲染粗体
     */
    private String renderBold(String html) {
        return BOLD_PATTERN.matcher(html).replaceAll("<strong>$1</strong>");
    }

    /**
     * 渲染斜体
     */
    private String renderItalic(String html) {
        return ITALIC_PATTERN.matcher(html).replaceAll("<em>$1</em>");
    }

    /**
     * 渲染链接
     */
    private String renderLinks(String html) {
        return LINK_PATTERN.matcher(html).replaceAll("<a href=\"$2\" target=\"_blank\" rel=\"noopener noreferrer\">$1</a>");
    }

    /**
     * 渲染图片
     */
    private String renderImages(String html) {
        return IMAGE_PATTERN.matcher(html).replaceAll("<img src=\"$2\" alt=\"$1\" loading=\"lazy\" />");
    }

    /**
     * 渲染无序列表
     */
    private String renderUnorderedLists(String html) {
        return UNORDERED_LIST_PATTERN.matcher(html).replaceAll("<li>$1</li>");
    }

    /**
     * 渲染有序列表
     */
    private String renderOrderedLists(String html) {
        return ORDERED_LIST_PATTERN.matcher(html).replaceAll("<li>$1</li>");
    }

    /**
     * 渲染引用
     */
    private String renderBlockquotes(String html) {
        return BLOCKQUOTE_PATTERN.matcher(html).replaceAll("<blockquote>$1</blockquote>");
    }

    /**
     * 渲染水平线
     */
    private String renderHorizontalRules(String html) {
        return HORIZONTAL_RULE_PATTERN.matcher(html).replaceAll("<hr />");
    }

    /**
     * 渲染段落
     */
    private String renderParagraphs(String html) {
        // 将双换行符转换为段落标签
        String[] lines = html.split("\\n\\n");
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty() &&
                !line.startsWith("<h") &&
                !line.startsWith("<pre") &&
                !line.startsWith("<ul") &&
                !line.startsWith("<li") &&
                !line.startsWith("<blockquote") &&
                !line.startsWith("<hr")) {
                sb.append("<p>").append(line).append("</p>");
            } else {
                sb.append(line);
            }
        }

        return sb.toString();
    }

    /**
     * 渲染换行
     */
    private String renderLineBreaks(String html) {
        // 将单个换行符转换为 <br>
        return html.replaceAll("(?<!<br/?>)\\n(?!<br/?>|</h|</p|</li|</blockquote|</pre)", "<br />\n");
    }

    /**
     * 简单代码高亮
     * @param code 代码内容
     * @param language 编程语言
     * @return 高亮后的代码
     */
    private String highlightCode(String code, String language) {
        if (code == null || code.isEmpty()) {
            return "";
        }

        String highlighted = code;

        // 根据语言进行简单的关键词高亮
        switch (language.toLowerCase()) {
            case "java":
                highlighted = highlightJavaKeywords(highlighted);
                break;
            case "javascript":
            case "js":
                highlighted = highlightJsKeywords(highlighted);
                break;
            case "python":
            case "py":
                highlighted = highlightPythonKeywords(highlighted);
                break;
            case "sql":
                highlighted = highlightSqlKeywords(highlighted);
                break;
            case "json":
                highlighted = highlightJson(highlighted);
                break;
            case "html":
            case "xml":
                // HTML/XML 已经被 escape，不需要额外处理
                break;
            default:
                // 通用高亮
                break;
        }

        return highlighted;
    }

    /**
     * Java 关键词高亮
     */
    private String highlightJavaKeywords(String code) {
        String[] keywords = {"public", "private", "protected", "class", "interface", "extends",
                            "implements", "new", "return", "if", "else", "for", "while", "do",
                            "switch", "case", "break", "continue", "try", "catch", "finally",
                            "throw", "throws", "static", "final", "abstract", "synchronized",
                            "volatile", "transient", "native", "strictfp", "enum", "import",
                            "package", "void", "boolean", "byte", "char", "short", "int",
                            "long", "float", "double", "true", "false", "null", "this", "super"};

        String result = code;
        for (String keyword : keywords) {
            result = Pattern.compile("\\b" + keyword + "\\b")
                    .matcher(result)
                    .replaceAll("<span class=\"keyword\">" + keyword + "</span>");
        }
        return result;
    }

    /**
     * JavaScript 关键词高亮
     */
    private String highlightJsKeywords(String code) {
        String[] keywords = {"var", "let", "const", "function", "return", "if", "else", "for",
                            "while", "do", "switch", "case", "break", "continue", "try", "catch",
                            "finally", "throw", "new", "this", "class", "extends", "super",
                            "import", "export", "default", "from", "async", "await", "yield",
                            "true", "false", "null", "undefined", "typeof", "instanceof"};

        String result = code;
        for (String keyword : keywords) {
            result = Pattern.compile("\\b" + keyword + "\\b")
                    .matcher(result)
                    .replaceAll("<span class=\"keyword\">" + keyword + "</span>");
        }
        return result;
    }

    /**
     * Python 关键词高亮
     */
    private String highlightPythonKeywords(String code) {
        String[] keywords = {"def", "class", "return", "if", "elif", "else", "for", "while",
                            "break", "continue", "try", "except", "finally", "raise", "with",
                            "as", "import", "from", "lambda", "yield", "global", "nonlocal",
                            "True", "False", "None", "and", "or", "not", "in", "is", "pass",
                            "async", "await"};

        String result = code;
        for (String keyword : keywords) {
            result = Pattern.compile("\\b" + keyword + "\\b")
                    .matcher(result)
                    .replaceAll("<span class=\"keyword\">" + keyword + "</span>");
        }
        return result;
    }

    /**
     * SQL 关键词高亮
     */
    private String highlightSqlKeywords(String code) {
        String[] keywords = {"SELECT", "FROM", "WHERE", "INSERT", "INTO", "VALUES", "UPDATE",
                            "SET", "DELETE", "CREATE", "ALTER", "DROP", "TABLE", "INDEX", "VIEW",
                            "JOIN", "LEFT", "RIGHT", "INNER", "OUTER", "ON", "GROUP", "BY",
                            "ORDER", "ASC", "DESC", "HAVING", "LIMIT", "OFFSET", "UNION", "ALL",
                            "AS", "DISTINCT", "COUNT", "SUM", "AVG", "MAX", "MIN", "NULL",
                            "NOT", "NULL", "AND", "OR", "IN", "BETWEEN", "LIKE", "EXISTS",
                            "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "CONSTRAINT", "DEFAULT",
                            "UNIQUE", "CHECK", "CASCADE", "TRANSACTION", "COMMIT", "ROLLBACK"};

        String result = code.toUpperCase();
        for (String keyword : keywords) {
            result = Pattern.compile("\\b" + keyword + "\\b")
                    .matcher(result)
                    .replaceAll("<span class=\"keyword\">" + keyword + "</span>");
        }
        // 恢复原始大小写用于非关键词
        return code;
    }

    /**
     * JSON 高亮
     */
    private String highlightJson(String code) {
        // 键高亮
        String result = Pattern.compile("\"([^\"]+)\":")
                .matcher(code)
                .replaceAll("<span class=\"json-key\">\"$1\"</span>:");

        // 字符串值高亮
        result = Pattern.compile(":\"([^\"]*)\"")
                .matcher(result)
                .replaceAll(":<span class=\"json-string\">\"$1\"</span>");

        // 数字高亮
        result = Pattern.compile(":(\\d+\\.?\\d*)")
                .matcher(result)
                .replaceAll(":<span class=\"json-number\">$1</span>");

        // 布尔值高亮
        result = Pattern.compile(":(true|false)")
                .matcher(result)
                .replaceAll(":<span class=\"json-boolean\">$1</span>");

        return result;
    }

    /**
     * HTML 转义
     */
    private String escapeHtml(String code) {
        if (code == null) {
            return "";
        }
        return code.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}
