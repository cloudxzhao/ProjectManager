package com.projecthub.module.wiki.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Wiki 内容处理工具类 */
@Slf4j
@Component
public class WikiContentUtils {

  private static final int DEFAULT_SUMMARY_LENGTH = 200;
  private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<[^>]+>");
  private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

  // Markdown patterns
  private static final Pattern HEADING_PATTERN =
      Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);
  private static final Pattern BOLD_PATTERN = Pattern.compile("\\*\\*(.+?)\\*\\*");
  private static final Pattern ITALIC_PATTERN = Pattern.compile("\\*(.+?)\\*");
  private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```(\\w*)\\n([\\s\\S]*?)```");
  private static final Pattern INLINE_CODE_PATTERN = Pattern.compile("`([^`]+)`");
  private static final Pattern LINK_PATTERN = Pattern.compile("\\[([^\\]]+)\\]\\(([^)]+)\\)");
  private static final Pattern IMAGE_PATTERN = Pattern.compile("!\\[([^\\]]*)]\\(([^)]+)\\)");
  private static final Pattern LIST_ITEM_PATTERN =
      Pattern.compile("^[-*+]\\s+(.+)$", Pattern.MULTILINE);
  private static final Pattern NUMBERED_LIST_PATTERN =
      Pattern.compile("^\\d+\\.\\s+(.+)$", Pattern.MULTILINE);
  private static final Pattern BLOCKQUOTE_PATTERN =
      Pattern.compile("^>\\s+(.+)$", Pattern.MULTILINE);
  private static final Pattern HR_PATTERN = Pattern.compile("^---+$", Pattern.MULTILINE);
  private static final Pattern PARAGRAPH_PATTERN = Pattern.compile("\\n\\n");

  /** Markdown 转 HTML (简单实现) */
  public static String renderToHtml(String markdown) {
    if (markdown == null || markdown.isEmpty()) {
      return "";
    }

    try {
      String html = markdown;

      // 转义 HTML 特殊字符 (但保留已有的 HTML)
      // 先处理 code blocks 以避免转义
      html =
          CODE_BLOCK_PATTERN
              .matcher(html)
              .replaceAll(
                  match -> {
                    String lang = match.group(1);
                    String code = match.group(2);
                    return "<pre><code class=\"language-"
                        + lang
                        + "\">"
                        + escapeHtml(code)
                        + "</code></pre>";
                  });

      // 行内代码
      html = INLINE_CODE_PATTERN.matcher(html).replaceAll("<code>$1</code>");

      // 标题
      html =
          HEADING_PATTERN
              .matcher(html)
              .replaceAll(
                  match -> {
                    String hashes = match.group(1);
                    String text = match.group(2);
                    int level = hashes.length();
                    return "<h" + level + ">" + text + "</h" + level + ">";
                  });

      // 粗体
      html = BOLD_PATTERN.matcher(html).replaceAll("<strong>$1</strong>");

      // 斜体
      html = ITALIC_PATTERN.matcher(html).replaceAll("<em>$1</em>");

      // 链接
      html = LINK_PATTERN.matcher(html).replaceAll("<a href=\"$2\">$1</a>");

      // 图片
      html = IMAGE_PATTERN.matcher(html).replaceAll("<img src=\"$2\" alt=\"$1\">");

      // 无序列表
      html = LIST_ITEM_PATTERN.matcher(html).replaceAll("<li>$1</li>");
      html = html.replaceAll("(<li>.*</li>)", "<ul>$1</ul>");

      // 有序列表
      html = NUMBERED_LIST_PATTERN.matcher(html).replaceAll("<li>$1</li>");

      // 引用
      html = BLOCKQUOTE_PATTERN.matcher(html).replaceAll("<blockquote>$1</blockquote>");

      // 水平线
      html = HR_PATTERN.matcher(html).replaceAll("<hr>");

      // 段落 - 将连续的非 HTML 行转换为段落
      StringBuilder result = new StringBuilder();
      String[] lines = html.split("\\n");
      boolean inParagraph = false;

      for (String line : lines) {
        line = line.trim();
        if (line.isEmpty()) {
          if (inParagraph) {
            result.append("</p>");
            inParagraph = false;
          }
        } else if (!line.startsWith("<") && !line.startsWith("&")) {
          // 非 HTML 标签开头的行，作为段落处理
          if (!inParagraph) {
            result.append("<p>");
            inParagraph = true;
          } else {
            result.append("<br>");
          }
          result.append(line);
        } else {
          if (inParagraph) {
            result.append("</p>");
            inParagraph = false;
          }
          result.append(line).append("\n");
        }
      }

      if (inParagraph) {
        result.append("</p>");
      }

      return result.toString();
    } catch (Exception e) {
      log.error("Markdown 渲染失败: {}", e.getMessage());
      return escapeHtml(markdown);
    }
  }

  /** 生成文档摘要 */
  public static String generateSummary(String content) {
    if (content == null || content.isEmpty()) {
      return "";
    }

    // 先尝试从 HTML 中提取纯文本
    String plainText = stripHtmlTags(content);

    // 替换多个空白字符为单个空格
    plainText = WHITESPACE_PATTERN.matcher(plainText).replaceAll(" ").trim();

    if (plainText.length() <= DEFAULT_SUMMARY_LENGTH) {
      return plainText;
    }

    // 在指定长度处截断，避免截断单词
    int truncateIndex = plainText.lastIndexOf(' ', DEFAULT_SUMMARY_LENGTH);
    if (truncateIndex == -1) {
      truncateIndex = DEFAULT_SUMMARY_LENGTH;
    }

    return plainText.substring(0, truncateIndex) + "...";
  }

  /** 从 HTML 中提取纯文本 */
  private static String stripHtmlTags(String html) {
    if (html == null) {
      return "";
    }
    return HTML_TAG_PATTERN.matcher(html).replaceAll(" ").trim();
  }

  /** 版本对比，生成 HTML 格式的差异 (简单行级对比) */
  public static String diffToHtml(String oldContent, String newContent) {
    if (oldContent == null) {
      oldContent = "";
    }
    if (newContent == null) {
      newContent = "";
    }

    try {
      List<DiffLine> diffLines = computeLineDiff(oldContent, newContent);

      if (diffLines.isEmpty()) {
        return "<div class=\"diff-no-changes\">无变化</div>";
      }

      StringBuilder html = new StringBuilder();
      html.append("<div class=\"diff-container\">");

      for (DiffLine line : diffLines) {
        String cssClass =
            switch (line.getType()) {
              case ADDED -> "diff-added";
              case REMOVED -> "diff-removed";
              case UNCHANGED -> "diff-unchanged";
            };
        String prefix =
            switch (line.getType()) {
              case ADDED -> "+";
              case REMOVED -> "-";
              case UNCHANGED -> " ";
            };
        html.append("<div class=\"").append(cssClass).append("\">");
        html.append(prefix).append(" ").append(escapeHtml(line.getContent()));
        html.append("</div>");
      }

      html.append("</div>");
      return html.toString();
    } catch (Exception e) {
      log.error("版本对比失败: {}", e.getMessage());
      return "<div class=\"diff-error\">对比失败: " + e.getMessage() + "</div>";
    }
  }

  /** 简单的行级对比，生成差异列表 */
  public static List<DiffLine> computeLineDiff(String oldContent, String newContent) {
    List<DiffLine> diffLines = new ArrayList<>();

    if (oldContent == null) {
      oldContent = "";
    }
    if (newContent == null) {
      newContent = "";
    }

    String[] oldLines = oldContent.split("\n", -1);
    String[] newLines = newContent.split("\n", -1);

    // 简单的行级 diff 算法
    int[][] dp = new int[oldLines.length + 1][newLines.length + 1];

    // 计算 LCS 长度
    for (int i = 1; i <= oldLines.length; i++) {
      for (int j = 1; j <= newLines.length; j++) {
        if (oldLines[i - 1].equals(newLines[j - 1])) {
          dp[i][j] = dp[i - 1][j - 1] + 1;
        } else {
          dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
        }
      }
    }

    // 回溯找到差异
    int i = oldLines.length;
    int j = newLines.length;

    while (i > 0 || j > 0) {
      if (i > 0 && j > 0 && oldLines[i - 1].equals(newLines[j - 1])) {
        diffLines.add(0, new DiffLine(oldLines[i - 1], DiffType.UNCHANGED, i, j));
        i--;
        j--;
      } else if (j > 0 && (i == 0 || dp[i][j - 1] >= dp[i - 1][j])) {
        diffLines.add(0, new DiffLine(newLines[j - 1], DiffType.ADDED, -1, j));
        j--;
      } else if (i > 0) {
        diffLines.add(0, new DiffLine(oldLines[i - 1], DiffType.REMOVED, i, -1));
        i--;
      }
    }

    return diffLines;
  }

  /** HTML 转义 */
  private static String escapeHtml(String text) {
    if (text == null) {
      return "";
    }
    return text.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;")
        .replace("'", "&#39;");
  }

  /** 生成父路径 */
  public static String generateParentPath(Long parentId, String parentPath) {
    if (parentId == null) {
      return "/";
    }
    if (parentPath == null || parentPath.isEmpty()) {
      return "/" + parentId + "/";
    }
    return parentPath + parentId + "/";
  }

  /** 计算层级 */
  public static int calculateLevel(String parentPath) {
    if (parentPath == null || parentPath.equals("/")) {
      return 0;
    }
    return parentPath.split("/").length - 1;
  }

  /** 差异行类型 */
  public enum DiffType {
    ADDED,
    REMOVED,
    UNCHANGED
  }

  /** 差异行 */
  @lombok.Data
  @lombok.AllArgsConstructor
  public static class DiffLine {
    private String content;
    private DiffType type;
    private int oldLineNumber;
    private int newLineNumber;
  }
}
