package com.mycompany.echo.service;

import com.microsoft.bot.schema.Pair;
import com.mycompany.echo.controller.BotController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GetUnitTestCount {
    @Autowired
    public BotController botController;
    @Autowired
    public ObjectGetter objectGetter;
    public Pair<Integer, Integer> unitTests(String s){
        String[] st = s.split("/");
        int n = st.length;
        String merge_id = st[n - 1];
        String project_id = botController.project_id;
        // default
        String website = "https://gitlab.com/api/v4/projects/"
                + project_id
                + "/merge_requests/"
                + merge_id
                + "/diffs";
        Pair<Object, String> response = objectGetter.getObj(website);
        if(response.getLeft() == null){
            return new Pair<>(-1, -1);
        }
        JSONArray array = (JSONArray) response.getLeft();
        int countAdded = 0, countSubtracted = 0;
        for (Object o : array) {
            JSONObject object = (JSONObject) o;
            String diff = (String) object.get("diff");
            String[] changes = diff.split("\n");
            for (String change : changes) {
                if (!change.isEmpty() && change.charAt(0) == '+') {
                    int index = change.indexOf("@Test");
                    while (index != -1) {
                        countAdded++;
                        index = change.indexOf("@Test", index + 1);
                    }
                }
                else if (!change.isEmpty() && change.charAt(0) == '-') {
                    int index = change.indexOf("@Test");
                    while (index != -1) {
                        countSubtracted++;
                        index = change.indexOf("@Test", index + 1);
                    }
                }
            }
        }
        return new Pair<>(countAdded, countSubtracted);
    }
}
