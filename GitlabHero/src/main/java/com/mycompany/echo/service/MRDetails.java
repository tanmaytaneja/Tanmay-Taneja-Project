package com.mycompany.echo.service;
import com.microsoft.bot.schema.*;
import com.mycompany.echo.controller.BotController;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class MRDetails {
    @Autowired
    public ObjectGetter objectGetter;
    @Autowired
    public BotController botController;
    @Autowired
    public PipelineStatus pipelineStatus;
    @Autowired
    public GetUnitTestCount getUnitTestCount;

    public ThumbnailCard getCard(String s) {
        ThumbnailCard card = new ThumbnailCard();
        ArrayList<String> stringArray = fetchDetails(s);
        String MergeRequestTitle = stringArray.get(2);
        card.setTitle(MergeRequestTitle);
        String Author = stringArray.get(0);
        card.setSubtitle(Author + " requested you to review the merge request :");
        String text = textSetter(s);
        card.setText(text);
        String profilePicture = stringArray.get(1);
        card.setImages(new CardImage(profilePicture));
        card.setButtons(new CardAction(
                ActionTypes.OPEN_URL, "Review", s));
        return card;
    }
    public String textSetter(String s) {
        String[] st = s.split("/");
        int n = st.length;
        String mergeID  = "Merge Request ID : " + st[n - 1];
        String pipeline = "Pipeline status : " + pipelineStatus.pipelineForCard(s);
        Pair<Integer, Integer> unitTests = getUnitTestCount.unitTests(s);
        String netUnitTests = "Unit tests (+) : "
                                + unitTests.getLeft()
                                + "<br>"
                                + "Unit tests (-) : "
                                + unitTests.getRight();
        return mergeID + "<br>" + pipeline + "<br>" + netUnitTests;
    }

    public ArrayList<String> fetchDetails(String s) {
        ArrayList<String> returnArray = new ArrayList<>();
        try{
            String[] stringArray = s.split("/");
            int n = stringArray.length;
            String merge_id = stringArray[n - 1];
            String website = "https://gitlab.com/api/v4/projects/"
                    + botController.project_id
                    + "/merge_requests/"
                    + merge_id;
            Pair<Object, String> response = objectGetter.getObj(website);
            if(response.getLeft() == null){
                for(int i=0; i<3; i++) returnArray.add("???");
                return returnArray;
            }
            // default
            JSONObject object = (JSONObject) response.getLeft();
            JSONObject author = (JSONObject) object.get("author");
            String name = (String) author.get("name");

            // 1) adding author name
            returnArray.add(name);

            // 2) adding avatar_url
            String avatar_url = (String) author.get("avatar_url");
            returnArray.add(avatar_url);

            // 3) adding project title
            String title = (String) object.get("title");
            int sz = title.length();
            int x = sz - 1;
            // doing this to remove the type of file! CAN REMOVE THIS IF NEEDED => just return title;
            for(int i=sz - 1; i>=0; i--){
                if(title.charAt(i) == '.'){
                    x = i;
                    break;
                }
            }
            returnArray.add(title.substring(0, x));
            return returnArray;

        } catch (Exception e) {
            int n = returnArray.size();
            for(int i=n; i<3; i++) returnArray.add("???");
            return returnArray;
        }
    }
}
