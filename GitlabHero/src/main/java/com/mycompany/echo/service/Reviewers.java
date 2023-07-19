package com.mycompany.echo.service;

import com.microsoft.bot.schema.Pair;
import com.mycompany.echo.controller.BotController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
@Service
public class Reviewers {
    @Autowired
    public BotController botController;
    @Autowired
    public ObjectGetter objectGetter;
    public ArrayList<String> findReviewers(String s){
        String[] st = s.split("/");
        int n = st.length;
        String merge_id = st[n - 1];
        String project_id = botController.project_id;
        // default
        String website = "https://gitlab.com/api/v4/projects/"
                + project_id
                + "/merge_requests/"
                + merge_id
                + "/reviewers";
        Pair<Object, String> response = objectGetter.getObj(website);
        if(response.getLeft() == null) {
            return new ArrayList<>();
        }
        JSONArray array = (JSONArray) response.getLeft();
        ArrayList<String> toReturn = new ArrayList<>();
        for (Object o : array) {
            JSONObject object = (JSONObject) o;
            JSONObject user = (JSONObject) object.get("user");
            long id = (long) user.get("id");
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
