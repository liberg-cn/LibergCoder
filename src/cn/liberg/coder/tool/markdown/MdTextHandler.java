package cn.liberg.coder.tool.markdown;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MdTextHandler {
    //**加粗**
    public static final Pattern BOLD = Pattern.compile("\\*\\*([^*]+)\\*\\*");
    //~~删除~~
    public static final Pattern RE_DEL = Pattern.compile("~~([^~]+)~~");
    //`行内代码`
    public static final Pattern RE_CODE = Pattern.compile("`([^`]+)`");
    //[链接名称](链接地址)
    public static final Pattern RE_LINK = Pattern.compile("\\[([^\\]]*)\\]\\(([^\\(]*)\\)");
    //![图注](图片地址)
    public static final Pattern RE_IMG = Pattern.compile("!\\[([^\\]]*)\\]\\(([^\\(]*)\\)");

    private Matcher boldMather;
    private Matcher delMather;
    private Matcher codeMather;
    private Matcher linkMather;
    private Matcher imgMather;

    private String text;
    private StringBuilder sb;
    private int pos = 0;

    public MdTextHandler(String text) {
        this.text = text;
    }

    public StringBuilder build() {
        if (sb == null) {
            sb = new StringBuilder(256);
        }
        Character c;
        while ((c = nextChar()) != null) {
            if (c == '~') {
                matchDel();
            } else if (c == '*') {
                matchBold();
            } else if (c == '`') {
                matchCode();
            } else if (c == '[') {
                matchLink();
            } else if (c == '!') {
                matchImg();
            } else {
                sb.append(c);
            }
        }
        return sb;
    }

    public void buildTo(StringBuilder sb) {
        this.sb = sb != null ? sb : new StringBuilder(256);
        build();
    }

    private Character nextChar() {
        if (pos < text.length()) {
            return text.charAt(pos++);
        } else {
            return null;
        }
    }

    public static void main(String[] args) {
        String text = "这是**加粗1**，这是**加粗2**，这是`代码1`，还有百度地址[链接](www.baidu.com)，还有~~删除~~，还有![这是图片](www.baidu.com)其余内容。";
        System.out.println(new MdTextHandler(text).build());

    }

    private void matchBold() {
        if (boldMather == null) {
            boldMather = BOLD.matcher(text);
        }
        if (boldMather.find(pos - 1)) {
            sb.append("<b>");
            sb.append(boldMather.group(1));
            sb.append("</b>");
            pos = boldMather.end();
        }
    }

    private void matchDel() {
        if (delMather == null) {
            delMather = RE_DEL.matcher(text);
        }
        if (delMather.find(pos - 1)) {
            sb.append("<del>");
            sb.append(delMather.group(1));
            sb.append("</del>");
            pos = delMather.end();
        }
    }

    private void matchCode() {
        if (codeMather == null) {
            codeMather = RE_CODE.matcher(text);
        }
        if (codeMather.find(pos - 1)) {
            sb.append("<code>");
            sb.append(codeMather.group(1));
            sb.append("</code>");
            pos = codeMather.end();
        }
    }

    private void matchLink() {
        if (linkMather == null) {
            linkMather = RE_LINK.matcher(text);
        }
        if (linkMather.find(pos - 1)) {
            sb.append("<a href=\"");
            sb.append(linkMather.group(2) + "\">");
            sb.append(linkMather.group(1));
            sb.append("</a>");
            pos = linkMather.end();
        }
    }

    private void matchImg() {
        if (imgMather == null) {
            imgMather = RE_IMG.matcher(text);
        }
        if (imgMather.find(pos - 1)) {
            sb.append("<img src=\"");
            sb.append(imgMather.group(2) + "\" alt=\"");
            sb.append(imgMather.group(1) + "\"/>");
            pos = imgMather.end();
        }
    }
}
