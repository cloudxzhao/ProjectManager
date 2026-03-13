package com.projecthub.common.util;

import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/** HTML 工具类 用于 XSS 防护和 HTML 清理 */
public class HtmlUtil {

  private static final Pattern SCRIPT_PATTERN =
      Pattern.compile("<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
  private static final Pattern ON_EVENT_PATTERN =
      Pattern.compile("on\\w+\\s*=\\s*['\"][^'\"]*['\"]", Pattern.CASE_INSENSITIVE);

  /**
   * 转义 HTML 特殊字符 将 < > & " ' 等字符转义为 HTML 实体
   *
   * @param input 输入字符串
   * @return 转义后的字符串
   */
  public static String escapeHtml(String input) {
    if (input == null) {
      return null;
    }
    StringBuilder sb = new StringBuilder();
    for (char c : input.toCharArray()) {
      switch (c) {
        case '<' -> sb.append("&lt;");
        case '>' -> sb.append("&gt;");
        case '&' -> sb.append("&amp;");
        case '"' -> sb.append("&quot;");
        case '\'' -> sb.append("&#x27;");
        case '/' -> sb.append("&#x2F;");
        default -> sb.append(c);
      }
    }
    return sb.toString();
  }

  /**
   * 清理 HTML，移除危险的 XSS 内容 保留基本的文本格式标签（br, p, b, i, u 等）
   *
   * @param input 输入 HTML
   * @return 清理后的 HTML
   */
  public static String cleanHtml(String input) {
    if (input == null) {
      return null;
    }
    Safelist safelist =
        Safelist.relaxed()
            .removeTags(
                "script",
                "iframe",
                "object",
                "embed",
                "form",
                "input",
                "textarea",
                "select",
                "style",
                "link",
                "meta",
                "base",
                "applet",
                "marquee",
                "frame",
                "frameset")
            .removeAttributes(
                ":all",
                "onclick",
                "onerror",
                "onload",
                "onmousedown",
                "onmouseup",
                "onmouseover",
                "onmousemove",
                "onmouseout",
                "onkeydown",
                "onkeypress",
                "onkeyup",
                "onblur",
                "onchange",
                "onfocus",
                "onsubmit",
                "onreset")
            .removeTags("a"); // 移除所有链接

    return Jsoup.clean(input, safelist);
  }

  /**
   * 转义纯文本字段（用于标题、描述等） 移除所有 HTML 标签并转义特殊字符
   *
   * @param input 输入字符串
   * @return 转义后的纯文本
   */
  public static String escapePlainText(String input) {
    if (input == null) {
      return null;
    }
    // 先移除所有 HTML 标签
    String noHtml = Jsoup.parse(input).text();
    // 再转义特殊字符
    return escapeHtml(noHtml);
  }

  /**
   * 检查字符串是否包含潜在的 XSS 攻击内容
   *
   * @param input 输入字符串
   * @return 如果包含危险内容返回 true
   */
  public static boolean containsXSS(String input) {
    if (input == null) {
      return false;
    }
    return SCRIPT_PATTERN.matcher(input).find() || ON_EVENT_PATTERN.matcher(input).find();
  }
}
