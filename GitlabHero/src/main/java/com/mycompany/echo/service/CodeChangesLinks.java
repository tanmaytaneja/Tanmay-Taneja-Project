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
public class CodeChangesLinks {
    @Autowired
    public BotController botController;
    @Autowired
    public UserService userService;
    @Autowired
    public ObjectGetter objectGetter;
    public String divider = "\n\n------------------------------\n\n";
    public Pair<String, ArrayList<Mention>> handleCC(String s) {
        String project_id = botController.project_id;
        String[] st = s.split("/");
        int n = st.length;
        String merge_id = st[n - 1];
        // default
        String website = "https://gitlab.com/api/v4/projects/"
                + project_id
                + "/merge_requests/"
                + merge_id
                + "/commits";
        Pair<Object, String> response = objectGetter.getObj(website);
        if(response.getLeft() == null){
            if(Objects.equals(response.getRight(), "")){
                return new Pair<>("Error!", null);
            } else return new Pair<>(response.getRight(), null);
        }
        JSONArray array = (JSONArray) response.getLeft();
        int sz = array.size();
        if(sz == 0){
            return new Pair<>("No changes found!", null);
        }
        StringBuilder toReturn = new StringBuilder();
        toReturn.append("**Number of changes :** ").append(sz);
        ArrayList<Mention> author_mentions = new ArrayList<>();
        // getting links for each code change
        for (Object o : array) {
            JSONObject object_i = (JSONObject) o;
            String web_url = (String) object_i.get("web_url");
            String author = (String) object_i.get("author_name");
            String filename = (String) object_i.get("message");
            String author_email = (String) object_i.get("author_email");
            User author_user = userService.getUserDetailsByEmail(author_email);
            TeamsChannelAccount authorTeamsAcc = author_user.getTeamsAcc();
            Mention authorMention = botController.mentionCreator(authorTeamsAcc, author);
            String output = botController.setHyperlink("**Review**", web_url);
            author_mentions.add(authorMention);
            toReturn.append(divider)
                    .append("\n\n**Author :** ")
                    .append(authorMention.getText())
                    .append("\n\n**Title :** ")
                    .append(filename)
                    .append("\n\n")
                    .append(output);
        }
        return new Pair<>(toReturn.toString(), author_mentions);
    }
}
