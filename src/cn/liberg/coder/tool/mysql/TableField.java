package cn.liberg.coder.tool.mysql;

import cn.liberg.coder.tool.core.Formats;
import cn.liberg.coder.tool.java.JField;
import cn.liberg.coder.tool.java.MetaAnno;
import cn.liberg.coder.tool.util.RegExpr;
import cn.liberg.coder.tool.util.Strings;

import java.util.regex.Matcher;

public class TableField {
    public String name;
    public String type;
    public int length;//仅对varchar类型有效
    public String defaultValue;//NULL,'0','-1','abc'等
    public String comment;
    public boolean isIndex;//是否是索引字段
    public String typeDefine;

    public static final int TYPE_STRING = 1;
    public static final int TYPE_LONG = 2;
    public static final int TYPE_INT = 4;
    public static final int TYPE_BYTE = 8;

    public static final RegExpr RE_STR = new RegExpr("^`(\\w+)` +([\\(\\)\\w]+) +DEFAULT +([-'\\w]+)( +COMMENT +'(.*)')?");
    public static final RegExpr RE_LINE = new RegExpr("^tb.add\\( *\"(\\w+)\", *((true|false), *)?type(\\w*)\\(([^\\)]*)\\), *(\"(.*)\"|null) *\\);$");

    private TableField() {

    }

    public void diffWith(TableField org, TableUpgrader upgrader) {
        if(org.isIndex && !isIndex) {
            upgrader.dropIndex(name);
        } else if(!org.isIndex && isIndex) {
            upgrader.addIndex(name);
        }
        if(!typeDefine.equals(org.typeDefine)) {
            upgrader.modifyColumn(name, typeDefine);
        }
    }

    //"_case_words_id", typeLong(), null
    //"_case_words_id", true, typeLong(), "comment"
    public String toDefineLine() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("\"");
        sb.append(name);
        sb.append("\", ");
        if (isIndex) {
            sb.append("true, ");
        }
        sb.append(typeDefine);
        if (comment != null) {
            sb.append(", \"" + comment + "\"");
        } else {
            sb.append(", null");
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        //"`_user_name` INT DEFAULT 0 COMMENT ''"
        StringBuilder sb = new StringBuilder(128);
        sb.append("`");
        sb.append(name);
        sb.append("` ");
        sb.append(type);
        sb.append(" DEFAULT ");
        sb.append(defaultValue);
        if (comment != null) {
            sb.append(" COMMENT '");
            sb.append(comment);
            sb.append("'");
        }
        return sb.toString();
    }

    public TableField(JField jf) {
        name = Formats.toTableFieldName(jf.name);
        comment = jf.desc;
        String orgType = jf.type;
        String dv = jf.defaultValue;
        MetaAnno anno = jf.getAnno();
        isIndex = anno.hasTrue("dbmap", "isIndex");
        switch (orgType) {
            case "String":
                String len = anno.getValue("dbmap", "length", "255");
                length = Integer.parseInt(len);
                type = "VARCHAR(" + length + ")";
                String tDefVal = "";
                if (dv != null) {
                    tDefVal = dv;
                    defaultValue = dv.replaceAll("\"", "'");
                } else {
                    defaultValue = "NULL";
                }

                if(length>4096) {
                    type = "TEXT";
                    typeDefine = "typeText()";
                } else {
                    String tArg = length == 255 ? "" : Integer.toString(length);
                    if (tDefVal.length() > 0) {
                        if (tArg.length() > 0) {
                            tArg += ", ";
                        }
                        tArg += tDefVal;
                    }
                    typeDefine = "typeString(" + tArg + ")";
                }
                break;
            case "long":
                type = "BIGINT";
                if (dv != null) {
                    defaultValue = "'" + dv + "'";
                    typeDefine = "typeLong(" + dv + ")";
                } else {
                    defaultValue = "'0'";
                    typeDefine = "typeLong()";
                }
                break;
            case "int":
                type = "INT";
                if (dv != null) {
                    defaultValue = "'" + dv + "'";
                    typeDefine = "typeInt(" + dv + ")";
                } else {
                    defaultValue = "'0'";
                    typeDefine = "typeInt()";
                }
                break;
            case "byte":
                type = "TINYINT";
                if (dv != null) {
                    defaultValue = "'" + dv + "'";
                    typeDefine = "typeByte(" + dv + ")";
                } else {
                    defaultValue = "'0'";
                    typeDefine = "typeByte()";
                }
                break;
            default://不支持的类型暂时都归为VARCHAR
                type = "VARCHAR(255)";
                length = 255;
                defaultValue = "NULL";
                typeDefine = "typeString()";
                break;
        }
    }

    public static TableField fromString(String line) {
        TableField tf = null;
        Matcher mch = RE_STR.findMatcher(line);
        if (mch != null) {
            tf = new TableField();
            tf.name = mch.group(1);
            String type = mch.group(2).toUpperCase();
            tf.type = type;
            tf.defaultValue = mch.group(3);
            if (mch.group(5) != null) {
                tf.comment = mch.group(5);
            }

            String lenStr = null;
            if (type.startsWith("VARCHAR")) {
                lenStr = Strings.findFirst(type, 0, '(', ')');
            }
            if (lenStr != null) {
                tf.length = Integer.parseInt(lenStr);
            } else {
                tf.length = 255;
            }
        }
        return tf;
    }

    public static TableField fromDefineLine(String line) {
        TableField tf = null;
        Matcher mch = RE_LINE.findMatcher(line);
        if (mch != null) {
            tf = new TableField();
            tf.name = mch.group(1);

            if("true".equals(mch.group(3))) {
                tf.isIndex = true;
            }
            parseType(tf, mch.group(4), mch.group(5));
            tf.comment = mch.group(7);//maybe null
        }
        return tf;
    }

    private static void parseType(TableField tf, String typeStr, String argStr) {
        argStr = argStr.trim();
        String[] args = null;
        if(argStr.length()>0) {
            args = argStr.split(",");
            for(int i=0;i<args.length;i++) {
                args[i] = args[i].trim();
            }
        }
        switch (typeStr) {
            case "Byte":
                tf.type = "TINYINT";
                if(args!=null) {
                    tf.defaultValue = args[0];
                } else {
                    tf.defaultValue = "0";
                }
                tf.typeDefine = "typeByte(" + argStr + ")";
                break;
            case "Int":
                tf.type = "INT";
                if(args!=null) {
                    tf.defaultValue = args[0];
                } else {
                    tf.defaultValue = "0";
                }
                tf.typeDefine = "typeInt(" + argStr + ")";
                break;
            case "Long":
                tf.type = "BIGINT";
                if(args!=null) {
                    tf.defaultValue = args[0];
                } else {
                    tf.defaultValue = "0";
                }
                tf.typeDefine = "typeLong(" + argStr + ")";
                break;
            case "String":
                if(args!=null) {
                    if(args.length>=2) {
                        tf.type = "VARCHAR(" + args[0] + ")";
                        tf.length = Integer.parseInt(args[0]);
                        tf.defaultValue = args[1];
                    } else if(args.length>=1) {
                        tf.type = "VARCHAR(" + args[0] + ")";
                        tf.length = Integer.parseInt(args[0]);
                        tf.defaultValue = "NULL";
                    }
                    tf.typeDefine = "typeString(" + argStr + ")";
                } else {
                    tf.type = "VARCHAR(255)";
                    tf.length = 255;
                    tf.typeDefine = "typeString()";
                }
                break;
            case "Text":
                tf.type = "TEXT";
                tf.defaultValue = "NULL";
                tf.typeDefine = "typeText()";
                break;
            default:
                break;
        }
    }
}
