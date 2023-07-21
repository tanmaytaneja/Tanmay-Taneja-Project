package com.mycompany.echo.utils;

@Component
public class CheckMessageType {
    public static boolean isPipeline(String s){
        // syntax : !pipeline <MR Link>
        s = s.split("\\s+")[0];
        return s.equals("!pipeline");
    }
    public static boolean isHelp(String s){
        // syntax : !help
        return s.equals("!help");
    }

    public static boolean isNetUnitTest(String s) {
        // syntax : !unit-tests <MR Link>
        s = s.split("\\s+")[0];
        return s.equals("!unit-tests");
    }

    public static boolean isCodeChangesLinks(String s) {
        // syntax : !code-changes <MR Link>
        s = s.split("\\s+")[0];
        return s.equals("!code-changes");
    }

    public static boolean isMergeRequest(String s) {
        // syntax : !review <MR Link>
        s = s.split("\\s+")[0];
        return s.equals("!review");
    }

    public static boolean isGetAllMergeRequests(String s) {
        // syntax : !get-all-MRs
        s = s.split("\\s+")[0];
        return s.equals("!get-mrs");
    }

    public static boolean isReviewers(String s) {
        // syntax : !reviewers <MR Link>
        s = s.split("\\s+")[0];
        return s.equals("!reviewers");
    }

    public static boolean isAssignees(String s) {
        // syntax : !assignees <MR Link>
        s = s.split("\\s+")[0];
        return s.equals("!assignees");
    }

    public static boolean isSetDefault(String s) {
        // syntax : !set-default <max_count>
        s = s.split("\\s+")[0];
        return s.equals("!set-default");
    }

    public static boolean isSetProjectID(String s) {
        // syntax : !set-project <id>
        s = s.split("\\s+")[0];
        return s.equals("!set-project");
    }
}
