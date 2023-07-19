package com.mycompany.echo.utils;

public class CheckMessageType {
    public static boolean isLocker(String s){
        // syntax : !lock <resource> <duration(Xd Yh Zm)>
        return s.split("\\s+")[0].equals("!lock");
    }

    public static boolean isNotifier(String s){
        // syntax !ask <resource>
        s = s.split("\\s+")[0];
        return s.equals("!notify");
    }

    public static boolean isCheck(String s){
        // syntax : !check <resource>
        s = s.split("\\s+")[0];
        return s.equals("!check");
    }

    public static boolean isUnlocker(String s){
        // syntax : !unlock <resource>
        s = s.split("\\s+")[0];
        return s.equals("!unlock");
    }

    public static boolean isHelp(String s){
        // syntax : !help
        return s.equals("!help");
    }

    public static boolean isGet(String s) {
        // syntax : !get <type>
        s = s.split("\\s+")[0];
        return s.equals("!get");
    }

    public static boolean isAdd(String s) {
        // syntax : !add <resource>
        s = s.split("\\s+")[0];
        return s.equals("!add");
    }

    public static boolean isDelete(String s){
        // syntax : !delete <resource>
        s = s.split("\\s+")[0];
        return s.equals("!delete");
    }

    public static boolean isSetDefault(String s){
        // syntax : !set-default <duration(Xd Yh Zm)>
        s = s.split("\\s+")[0];
        return s.equals("!set-default");
    }
}
