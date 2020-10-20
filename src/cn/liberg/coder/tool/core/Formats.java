package cn.liberg.coder.tool.core;


public final class Formats {
    public static final String ENTITY_ID = "id";
    public static final String TABLE_ID = "_id";
    public static final String NL = "\r\n";
    public static final String NL2 = "\r\n\r\n";
    public static final String BLANK = " ";
    public static final String[] IND = {
            "",
            "    ",
            "        ",
            "            "};
    public static final String IN = IND[1];
    public static final String IN2 = IND[2];

    public static String strip(String src) {
        int len = src.length();
        if (len >= 2 && src.charAt(0) == '"' && src.charAt(len - 1) == '"') {
            return src.substring(1, src.length() - 1);
        } else {
            return src;
        }
    }

    public static int[] charCount(String src, char... chars) {
        int[] rt = new int[chars.length];
        for (int i = 0; i < rt.length; i++) {
            rt[i] = 0;
        }
        char[] arr = src.toCharArray();
        char one;
        for (int i = 0; i < arr.length; i++) {
            one = arr[i];
            for (int j = 0; j < chars.length; j++) {
                if (one == chars[j]) {
                    rt[j]++;
                    break;
                }
            }
        }
        return rt;
    }

    public static String upperFirstLetter(String input) {
        if(input!=null && input.length()>0) {
            return input.substring(0,1).toUpperCase() + input.substring(1);
        }
        return input;
    }

    public static void main(String[] args) {
        System.out.println(toDataBaseName("LibergCoder"));
        System.out.println(toDataBaseName("liberg-coder"));

        System.out.println(upperFirstLetter("name"));

        int[] counts = charCount("abc(xxx(t)))z", '(', ')');
        System.out.println(toTableFieldName("id"));
        System.out.println(toTableFieldName("userName"));
        System.out.println(toTableFieldName("mUserName"));
        System.out.println(toTableFieldName("mUserPPPsName"));
        System.out.println(toTableFieldName("name"));
        System.out.println(toTableFieldName("mobile"));
        System.out.println("-------------");

//        System.out.println(toEntityFieldName("_user_name"));
//        System.out.println(toEntityFieldName("_user_name"));
//        System.out.println(toEntityFieldName("_user_ppps_name"));
//        System.out.println(toEntityFieldName("name"));
//        System.out.println("-------------");
//
//        System.out.println(toEntityFieldName("_user_name", (byte) 'm'));
//        System.out.println(toEntityFieldName("_user_name", (byte) 'm'));
//        System.out.println(toEntityFieldName("_user_ppps_name", (byte) 'm'));
//        System.out.println(toEntityFieldName("name", (byte)'m'));
//        System.out.println("-------------");

        System.out.println(forShort("name"));
        System.out.println(forShort("userName"));
        System.out.println(forShort("mUserName"));
        System.out.println(forShort("mobile"));

        System.out.println("end");
    }

    public static String forShort(String name) {
        if (ENTITY_ID.equals(name) || TABLE_ID.equals("name")) {
            return "id";
        }
        byte[] buf = new byte[name.length()];
        int p = 0;
        boolean upper = true;
        boolean first = true;
        char c;
        int start = 0;
        if(name.length()>1 && name.charAt(0) == 'm' && Character.isUpperCase(name.charAt(1))) {
            start = 1;
        }
        for (int i = start; i < name.length(); i++) {
            c = name.charAt(i);
            if (c == '_') {
                upper = true;
                first = true;
            } else if (c >= 'A' && c <= 'Z') {
                if (upper || first) {
                    buf[p++] = (byte) (c + 32);
                }
                upper = false;
                first = false;
            } else {
                if (upper) {
                    buf[p++] = (byte) c;
                    upper = false;
                }
                first = true;
            }
        }

        return new String(buf, 0, p);
    }

    public static String toTableFieldName(String name) {
        char c;
        boolean first = true;
        byte[] buf = new byte[name.length() + 10];
        int p = 0;
        int start = 0;
        buf[p++] = '_';
        if (name.length() > 1 && name.charAt(0) == 'm' && Character.isUpperCase(name.charAt(1))) {
            start = 1;
            first = false;
        }
        for (int i = start; i < name.length(); i++) {
            c = name.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                if (first) {
                    buf[p++] = '_';
                    first = false;
                }
                buf[p++] = (byte) (c + 32);
            } else {
                first = true;
                buf[p++] = (byte) c;
            }
        }
        return new String(buf, 0, p);
    }

    public static String toEntityFieldName(String name) {
        assert name.length() > 0;
        char c;
        boolean upper = true;
        byte[] buf = new byte[name.length() + 1];
        int p = 0;
        for (int i = 0; i < name.length(); i++) {
            c = name.charAt(i);
            if (c == '_') {
                upper = true;
            } else {
                if (upper && c >= 'a' && c <= 'z') {
                    c -= 32;
                }
                buf[p++] = (byte) c;
                upper = false;
            }
        }

        byte c0 = buf[0];
        if (c0 >= 'A' && c0 <= 'Z') {
            buf[0] = (byte) (c0 + 32);
        }
        return new String(buf, 0, p);
    }

    public static String toEntityFieldName(String name, byte prefix) {
        char c;
        boolean upper = true;
        byte[] buf = new byte[name.length() + 1];
        int p = 0;
        buf[p++] = prefix;
        for (int i = 0; i < name.length(); i++) {
            c = name.charAt(i);
            if (c == '_') {
                if (i > 0) {
                    upper = true;
                }
            } else {
                if (upper && c >= 'a' && c <= 'z') {
                    c -= 32;
                }
                buf[p++] = (byte) c;
                upper = false;
            }
        }
        return new String(buf, 0, p);
    }

    public static String toTableName(String name) {
        char c;
        boolean first = true;
        byte[] buf = new byte[name.length() + 10];
        int p = 0;
        for (int i = 0; i < name.length(); i++) {
            c = name.charAt(i);
            if (c >= 'A' && c <= 'Z') {
                if (first) {
                    if (i > 0) buf[p++] = '_';
                    first = false;
                }
                buf[p++] = (byte) (c + 32);
            } else {
                first = true;
                buf[p++] = (byte) c;
            }
        }
        return new String(buf, 0, p);
    }

    public static String toEntityName(String name) {
        char c;
        boolean upper = true;
        byte[] buf = new byte[name.length()];
        int p = 0;
        for (int i = 0; i < name.length(); i++) {
            c = name.charAt(i);
            if (c == '_') {
                upper = true;
            } else {
                if (upper && c >= 'a' && c <= 'z') {
                    c -= 32;
                }
                buf[p++] = (byte) c;
                upper = false;
            }
        }
        return new String(buf, 0, p);
    }

    public static String toDataBaseName(String projectName) {
        char c;
        byte[] buf = new byte[projectName.length()+10];
        int p = 0;
        for (int i = 0; i < projectName.length(); i++) {
            c = projectName.charAt(i);
            if(c >= 'A' && c <= 'Z') {
                if(i>0) {
                    buf[p++] = '_';
                }
                buf[p++] = (byte)(c+32);
            } else if (c == '-') {
                buf[p++] = '_';
            } else {
                buf[p++] = (byte) c;
            }
        }
        return new String(buf, 0, p);
    }

}
