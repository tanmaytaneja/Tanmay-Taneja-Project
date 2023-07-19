package com.mycompany.echo.service;

import com.microsoft.bot.schema.Pair;
import com.mycompany.echo.controller.BotController;
import org.checkerframework.checker.index.qual.SearchIndexBottom;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.Objects;
@Service
public class PipelineStatus {
    @Autowired
    public BotController botController;
    @Autowired
    public ObjectGetter objectGetter;
    public String handlePipeline(String s){
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
        if(response.getLeft() == null){
            if(Objects.equals(response.getRight(), "")) {
                return "Error!";
            } else return response.getRight();
        }
        JSONObject object = (JSONObject) response.getLeft();
        try{
            JSONObject head_pipeline = (JSONObject) object.get("head_pipeline");
            String status = (String) head_pipeline.get("status");
            JSONObject user = (JSONObject) head_pipeline.get("user");
            String name = (String) user.get("name");
            String username = (String) user.get("username");
            String web_url = (String) object.get("web_url");

            // hyperlink and return string creation
            String output = botController.setHyperlink("View more", web_url);
            return "*MR initiated by :* **"
                    + name
                    + "** (" + username + ")\n\n\n"
                    + "*Pipeline status :* "
                    + status
                    + "\n\n"
                    + output
                    + ".";
        } catch (Exception e) {
            return "Pipeline Unavailable!";
        }
    }

    public String pipelineForCard(String s){
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
            if(Objects.equals(response.getRight(), "")) {
                return "Error!";
            } else return response.getRight();
        }
        JSONObject object = (JSONObject) response.getLeft();
        try {
            JSONObject head_pipeline = (JSONObject) object.get("head_pipeline");
            String status = (String) head_pipeline.get("status");
            return status + ".";
        } catch (Exception e) {
            return "No Pipelines";
        }
    }
}
