package com.mycompany.echo.service;

import com.microsoft.bot.schema.Pair;
import com.mycompany.echo.controller.BotController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
@Service
public class Assignees {
    @Autowired
    public BotController botController;
    @Autowired
    public ObjectGetter objectGetter;
    public ArrayList<String> findAssignees(String s){
        String[] st = s.split("/");
        int n = st.length;
        String merge_id = st[n - 1];
        String project_id = botController.project_id;
        // default
        String website = "https://gitlab.com/api/v4/projects/"
                + project_id
                + "/merge_requests/"
                + merge_id;
        Pair<Object, String> response = objectGetter.getObj(website);
        if(response.getLeft() == null) {
            return new ArrayList<>();
        }
        JSONObject object = (JSONObject) response.getLeft();
        JSONArray assignees = (JSONArray) object.get("assignees");
        ArrayList<String> toReturn = new ArrayList<>();
        for (Object o : assignees) {
            JSONObject obj = (JSONObject) o;
//            JSONObject user = (JSONObject) obj.get("user");
            long id = (long) obj.get("id");
            String user_email = getEmail(id);
            toReturn.add(user_email);
        }
        return toReturn;
    }

    public String getEmail(long id){
        try{
            // default
            String website = "https://gitlab.com/api/v4/users/" + id;
            Pair<Object, String> response = objectGetter.getObj(website);
            if(response.getLeft() == null) {
                return "tanmaytaneja2@gmail.com";
            }
            JSONObject object = (JSONObject) response.getLeft();
            return (String) object.get("public_email");
        }
        catch(Exception e){
            e.printStackTrace();
            return "tanmaytaneja2@gmail.com";
        }
    }
}
