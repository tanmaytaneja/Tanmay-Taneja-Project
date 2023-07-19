package com.mycompany.echo.utils;

public class Help {
    public static String handleHelp(String Name){
        return "**Welcome back **"
                + Name
                + "**!**\n\n"
                + "*Here's a summary of available commands :*\n\n"
                + "1. **!lock** *\\<resource>* *\\<duration(Xd Yh Zm)>* : lock a resource for a certain duration.\n"
                + "2. **!unlock** *\\<resource>* : unlock the resource (valid for owner).\n"
                + "3. **!check** *\\<resource>* : check availability of a particular resource.\n"
                + "4. **!notify** *\\<resource>* : notify the owner of \\<resource> that you are requesting for it.\n"
                + "5. **!get** *\\<type>* : retrieve statuses of resources of a particular \\'type'.\n"
                + "6. **!add** *\\<resource>* : add a resource in the database.\n"
                + "7. **!delete** *\\<resource>* : delete a resource from the database.\n"
                + "8. **!set-default** *\\<duration(Xd Yh Zm)>* : set default locking time for everyone.\n"
                + "9. **!help** : get command summary again.";
    }
}

