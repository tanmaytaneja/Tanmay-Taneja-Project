package com.mycompany.echo.utils;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

public class Help {
    public static String handleHelp(String Name){
        return "**Welcome Back **"
                + Name
                + "**!**\n\n"
                + "*Here's a summary of available commands :*\n\n"
                + "1. **!pipeline** *\\<MR link>* : retrieve pipeline status for a MR.\n"
                + "2. **!unit-tests** *\\<MR link>* : retrieve net unit tests added in a MR.\n"
                + "3. **!get-mrs** *\\<state>* *\\<max_count>* : retrieve previous 'max count' "
                + "'state' MR's to be reviewed by you.\n"
                + "4. **!review** *\\<MR link>* : send a MR notification to reviewers.\n"
                + "5. **!code-changes** *\\<MR link>* : retrieve code changes.\n"
                + "6. **!reviewers** *\\<MR link>* : retrieve reviewers for a MR.\n"
                + "7. **!assignees** *\\<MR link>* : retrieve assignees for a MR.\n"
                + "8. **!set-default** *\\<max_count>* : set default count for !get-mrs command.\n"
                + "9. **!set-project** *\\<id>* : configure bot for new project.\n"
                + "10. **!help** : get command summary again.";
    }
}
