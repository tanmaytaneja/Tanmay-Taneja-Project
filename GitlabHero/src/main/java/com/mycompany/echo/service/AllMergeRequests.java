package com.mycompany.echo.service;

import com.microsoft.bot.schema.Mention;
import com.microsoft.bot.schema.Pair;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import com.mycompany.echo.controller.BotController;
import com.mycompany.echo.models.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;

@Service
public class AllMergeRequests {

    @Autowired
    public BotController botController;
    @Autowired
    public ObjectGetter objectGetter;
    @Autowired
    public UserService userService;
    public Pair<String, ArrayList<Mention>> getMRs(String s, String email) {
        String username = getUsernameFromEmail(email);
        String[] st = s.split("\\s+");
        String query = st[1];
        int n = s.split("\\s+").length;
        int count;
        if(n == 2) count = botController.default_count;
        else count = Integer.parseInt(st[2]);
        ArrayList<ArrayList<String>> mergeRequestIds = getAllMergeRequests(count, query, username);
        if(mergeRequestIds.get(0).size() == 0){
            return new Pair<>("No merge requests found!", null);
        } else {
            ArrayList<Mention> mentions = new ArrayList<>();
            StringBuilder toReturn = new StringBuilder();
            toReturn.append("**Here's a list of ").append(query).append(" MRs to be reviewed by you :-**\n\n");
            for(int i=0; i<mergeRequestIds.get(0).size(); i++){
                String title = (mergeRequestIds.get(0)).get(i);
                String link = (mergeRequestIds.get(1)).get(i);
                String hyperlink = botController.setHyperlink(title, link);
                String author = (mergeRequestIds.get(2)).get(i);
                String author_id = (mergeRequestIds.get(3)).get(i);
                String author_email = getEmailFromID(author_id);
                User author_user = userService.getUserDetailsByEmail(author_email);
                TeamsChannelAccount authorTeamsAcc = author_user.getTeamsAcc();
                Mention author_mention = botController.mentionCreator(authorTeamsAcc, author);
                mentions.add(author_mention);
                toReturn.append(i + 1)
                        .append(". ")
                        .append(hyperlink)
                        .append(" (")
                        .append(author_mention.getText())
                        .append(")\n");
            }
            return new Pair<>(toReturn.toString(), mentions);
        }
    }
    private ArrayList<ArrayList<String>> getAllMergeRequests(int count, String query, String username) {
        String project_id = botController.project_id;
        ArrayList<ArrayList<String>> toReturn = new ArrayList<>();
        ArrayList<String> titles = new ArrayList<>();
        ArrayList<String> links = new ArrayList<>();
        ArrayList<String> authors = new ArrayList<>();
        ArrayList<String> author_ids = new ArrayList<>();
        // default
        String website = "https://gitlab.com/api/v4/projects/"
                + project_id
                + "/merge_requests?state="
                + query
                + "&reviewer_username="
                + username;
        if(Objects.equals(query, "all")) website = "https://gitlab.com/api/v4/projects/"
                + project_id
                + "/merge_requests?"
                + "reviewer_username="
                + username;
        Pair<Object, String> response = objectGetter.getObj(website);
        if(response.getLeft() == null) {
            toReturn.add(titles);
            toReturn.add(links);
            toReturn.add(authors);
            toReturn.add(author_ids);
            return toReturn;
        }
        JSONArray array = (JSONArray) response.getLeft();
        for (int i=0; i<Math.min(array.size(), count); i++) {
            JSONObject curr = (JSONObject) array.get(i);
            titles.add((String) curr.get("title"));
            links.add((String) curr.get("web_url"));
            JSONObject author = (JSONObject) curr.get("author");
            Long author_id = (Long) author.get("id");
            String author_name = (String) author.get("name");
            authors.add(author_name);
            author_ids.add(author_id.toString());
        }
        toReturn.add(titles);
        toReturn.add(links);
        toReturn.add(authors);
        toReturn.add(author_ids);
        return toReturn;
    }
    public String getEmailFromID(String id){
        String project_id = botController.project_id;
        // default
        String website = "https://gitlab.com/api/v4/users/" + id;
        Pair<Object, String> response = objectGetter.getObj(website);
        if(response.getLeft() == null) {
            if(Objects.equals(response.getRight(), "")) {
                return "Error!";
            } else return response.getRight();
        }
        JSONObject object = (JSONObject) response.getLeft();
        return (String) object.get("public_email");
    }
    public String getUsernameFromEmail(String email){
        String project_id = botController.project_id;
        // default
        String website = "https://gitlab.com/api/v4/projects/"
                + project_id
                + "/members?query="
                + email;
        Pair<Object, String> response = objectGetter.getObj(website);
        if(response.getLeft() == null) {
            if(Objects.equals(response.getRight(), "")) {
                return "Error!";
            } else return response.getRight();
        }
        JSONArray array = (JSONArray) response.getLeft();
        JSONObject object = (JSONObject) array.get(0);
        return (String) object.get("username");
    }
}
