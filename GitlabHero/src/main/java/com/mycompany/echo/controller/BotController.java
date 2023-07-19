// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.mycompany.echo.controller;

import com.codepoetics.protonpack.collectors.CompletableFutures;
import com.microsoft.bot.builder.ActivityHandler;
import com.microsoft.bot.builder.MessageFactory;
import com.microsoft.bot.builder.TurnContext;
import com.microsoft.bot.builder.teams.TeamsInfo;
import com.microsoft.bot.integration.BotFrameworkHttpAdapter;
import com.microsoft.bot.schema.*;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import com.mycompany.echo.models.User;
import com.mycompany.echo.repository.UserRepository;
import com.mycompany.echo.service.MRDetails;
import com.mycompany.echo.service.UserService;
import com.mycompany.echo.utils.CheckMessageType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * This class implements the functionality of the Bot.
 *
 * <p>
 * This is where application specific logic for interacting with the users would be added. For this
 * sample, the {@link #onMessageActivity(TurnContext)} echos the text back to the user. The {@link
 * #onMembersAdded(List, TurnContext)} will send a greeting to new conversation participants.
 * </p>
 */
@Component
public class BotController extends ActivityHandler {
    @Autowired
    public UserService userService;
    @Autowired
    public UserRepository userRepository;
    @Autowired
    public com.mycompany.echo.service.Assignees Assignees;
    @Autowired
    public com.mycompany.echo.service.Reviewers Reviewers;
    @Autowired
    public MRDetails mrDetails;
    @Autowired
    public com.mycompany.echo.service.AllMergeRequests AllMergeRequests;
    @Autowired
    public com.mycompany.echo.service.CodeChangesLinks CodeChangesLinks;
    @Autowired
    public com.mycompany.echo.service.GetUnitTestCount GetUnitTestCount;
    @Autowired
    public com.mycompany.echo.utils.Help Help;
    @Autowired
    public com.mycompany.echo.service.PipelineStatus PipelineStatus;
    public static BotFrameworkHttpAdapter Adapter;
    public BotController(BotFrameworkHttpAdapter adapter){
        super();
        Adapter = adapter;
    }
    public boolean isAllowed(String s, int x){
        String[] spaces = s.split("\\s+");
        if(x == 3 && spaces.length != 3) return false;
        if(x == 2 && spaces.length != 2) return false;
        if(x == 1 && spaces.length != 1) return false;
        if(x == 3){
            String query = spaces[1];
            if((Objects.equals(query, "opened"))
                    || (Objects.equals(query, "closed"))
                    || (Objects.equals(query, "merged"))
                    || (Objects.equals(query, "all"))){
                String count = spaces[2];
                try{
                    Integer.parseInt(count);
                } catch (Exception e){
                    return false;
                }
                return true;
            }
            return false;
        }
        String[] st = s.split("/");
        int n = st.length;
        String last = st[n - 1];
        try{
            Integer.parseInt(last);
        }
        catch (Exception e){
            return false;
        }
        return true;
    }
    public String setHyperlink(String linkText, String web_url){
        return "[" + linkText + "](" + web_url + ")";
    }
    public boolean checkPositiveInteger(String count){
        try {
            int cnt = Integer.parseInt(count);
            return cnt > 0;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean checkPositiveLong(String id){
        try {
            long num = Long.parseLong(id);
            return num > 0;
        } catch (Exception e) {
            return false;
        }
    }
    public Mention mentionCreator(TeamsChannelAccount teamsAcc, String name) {
        Mention mention = new Mention();
        mention.setMentioned(teamsAcc);
        mention.setText(
                "<at>" + name + "</at>"
        );
        return mention;
    }
    private void sendMentionMessageToSelf(TurnContext turnContext, String message, Mention mention){
        Activity replyActivity = MessageFactory.text(message);
        replyActivity.setMentions(Collections.singletonList(mention));
        turnContext.sendActivity(replyActivity).thenApply(resourceResponse -> null);
    }
    private void sendMentionMessageToSelfWithMultipleMentions(
            TurnContext turnContext, String message, ArrayList<Mention> mentions){
        Activity replyActivity = MessageFactory.text(message);
        replyActivity.setMentions(mentions);
        turnContext.sendActivity(replyActivity).thenApply(resourceResponse -> null);
    }
    public void sendMentionMessageToOthers(ConversationReference ownerConRef, String message, Mention mention) {
        Activity replyActivity = MessageFactory.text(message);
        replyActivity.setMentions(Collections.singletonList(mention));
        Adapter.continueConversation(
                "c9c2ec37-665f-4966-adc5-259180d07838",
                ownerConRef,
                turnContext -> turnContext.sendActivity(replyActivity)
                        .thenApply(resourceResponse -> null)
        );
    }
    public void proactiveMessage(String Email, Activity message) {
        if(userService.checkEmailExists(Email))
        {
            Adapter.continueConversation(
                    "c9c2ec37-665f-4966-adc5-259180d07838",
                    userService.getUserDetailsByEmail(Email).getConRef(),
                    turnContext -> turnContext.sendActivity(message)
                            .thenApply(resourceResponse -> null)
            );
        }
    }
    private void bot(TurnContext turnContext, String string){
        turnContext.sendActivity(MessageFactory.text(string)).thenApply(sendResult -> null);
    }
    private static ConversationReference conRef;
    public String project_id = "47405742";
    public int default_count = 10;
    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext){
        conRef = turnContext.getActivity().getConversationReference();
        if(turnContext.getActivity().isType(ActivityTypes.MESSAGE) && turnContext.getActivity().getText() != null){
            turnContext.getActivity().removeRecipientMention();
            ChannelAccount sentBy = turnContext.getActivity().getFrom();
            TeamsChannelAccount teamsAcc = TeamsInfo.getMember(turnContext, sentBy.getId()).join();
            String Email = teamsAcc.getEmail();
            String name = teamsAcc.getName();
            if(!userService.checkEmailExists(Email)){
                User user = new User();
                user.setEmail(Email);
                user.setConRef(conRef);
                user.setTeamsAcc(teamsAcc);
                userService.saveUserDetails(user);
            }
            String s = turnContext.getActivity().getText();

            if(CheckMessageType.isMergeRequest(s)){
                // syntax : !review <MR link>
                int n = s.split("\\s+").length;
                // check string length
                if(n == 1){
                    // length is less
                    bot(turnContext, "MR Link not specified.");
                }
                else if(isAllowed(s, 2)){
                    // MR exists
                    String website = s.split("\\s+")[1];
                    ThumbnailCard card = mrDetails.getCard(website);
                    ArrayList<String> reviewers = Reviewers.findReviewers(website);
                    ArrayList<Mention> reviewer_mentions = new ArrayList<>();
                    Mention self_mention = mentionCreator(teamsAcc, name);
                    String text = self_mention.getText() + " sent you a review request :-";
                    for(String reviewer : reviewers){
                        User reviewerUser = userRepository.findByEmail(reviewer);
                        TeamsChannelAccount reviewerTeamsAcc = reviewerUser.getTeamsAcc();
                        String reviewerName = reviewerTeamsAcc.getName();
                        Mention reviewerMention = mentionCreator(reviewerTeamsAcc, reviewerName);
                        ConversationReference reviewerConRef = reviewerUser.getConRef();
                        reviewer_mentions.add(reviewerMention);
                        sendMentionMessageToOthers(reviewerConRef, text, self_mention);
                        proactiveMessage(reviewer, MessageFactory.attachment(card.toAttachment()));
                    }
                    turnContext.sendActivity(MessageFactory.attachment(
                            card.toAttachment())).thenApply(sendResult -> null);
                    int sz = reviewer_mentions.size();
                    String word;
                    StringBuilder message = new StringBuilder();
                    if(sz == 1){
                        word = " has";
                        for(int i=0; i<sz; i++){
                            message.append(reviewer_mentions.get(i).getText());
                        }
                    }
                    else{
                        word = " have";
                        if(sz == 2){
                            message.append(reviewer_mentions.get(0).getText()).append(" and ");
                            message.append(reviewer_mentions.get(1).getText());
                        }
                        else{
                            for(int i=0; i<sz - 2; i++){
                                message.append(reviewer_mentions.get(i).getText()).append(", ");
                            }
                            message.append(reviewer_mentions.get(sz - 2).getText()).append(" and ");
                            message.append(reviewer_mentions.get(sz - 1).getText());
                        }
                    }
                    message.append(word).append(" been notified!");
                    sendMentionMessageToSelfWithMultipleMentions(turnContext, message.toString(), reviewer_mentions);
                } else {
                    // MR doesn't exist
                    bot(turnContext, "Invalid Message Format!\n\n"
                                        + "Valid Format : **!review** *\\<MR link>*");
                }
            }
            else if(CheckMessageType.isReviewers(s)){
                // syntax : !reviewers <MR link>
                int n = s.split("\\s+").length;
                // check string length
                if(n == 1){
                    // length is less
                    bot(turnContext, "MR Link not specified.");
                }
                else if(isAllowed(s, 2)){
                    // MR exists
                    String website = s.split("\\s+")[1];
                    ArrayList<String> reviewers = Reviewers.findReviewers(website);
                    ArrayList<Mention> reviewer_mentions = new ArrayList<>();
                    for(String reviewer : reviewers){
                        User reviewerUser = userRepository.findByEmail(reviewer);
                        TeamsChannelAccount reviewerTeamsAcc = reviewerUser.getTeamsAcc();
                        String reviewerName = reviewerTeamsAcc.getName();
                        Mention reviewerMention = mentionCreator(reviewerTeamsAcc, reviewerName);
                        reviewer_mentions.add(reviewerMention);
                    }
                    StringBuilder message = new StringBuilder();
                    message.append("**Here's a list of reviewers for this merge request :**\n");
                    int count = 0;
                    for(Mention reviewer : reviewer_mentions){
                        count++;
                        message.append("\n").append(count).append(". ").append(reviewer.getText());
                    }
                    if(reviewers.size() > 0) sendMentionMessageToSelfWithMultipleMentions(
                            turnContext, message.toString(), reviewer_mentions);
                    else bot(turnContext, "No reviewers for this merge request.");
                } else {
                    // MR doesn't exist
                    bot(turnContext, "Invalid Message Format!\n\n"
                            + "Valid Format : **!reviewers** *\\<MR link>*");
                }
            }
            else if(CheckMessageType.isAssignees(s)){
                // syntax : !assignees <MR link>
                int n = s.split("\\s+").length;
                // check string length
                if(n == 1){
                    // length is less
                    bot(turnContext, "MR Link not specified.");
                }
                else if(isAllowed(s, 2)){
                    // MR exists
                    String website = s.split("\\s+")[1];
                    ArrayList<String> assignees = Assignees.findAssignees(website);
                    ArrayList<Mention> assignee_mentions = new ArrayList<>();
                    for(String assignee : assignees){
                        User assigneeUser = userRepository.findByEmail(assignee);
                        TeamsChannelAccount assigneeTeamsAcc = assigneeUser.getTeamsAcc();
                        String assigneeName = assigneeTeamsAcc.getName();
                        Mention assigneeMention = mentionCreator(assigneeTeamsAcc, assigneeName);
                        assignee_mentions.add(assigneeMention);
                    }
                    StringBuilder message = new StringBuilder();
                    message.append("**Here's a list of assignees for this merge request :-**\n");
                    int count = 0;
                    for(Mention assignee : assignee_mentions){
                        count++;
                        message.append("\n").append(count).append(". ").append(assignee.getText());
                    }
                    if(assignees.size() > 0) sendMentionMessageToSelfWithMultipleMentions(
                            turnContext, message.toString(), assignee_mentions);
                    else bot(turnContext, "No assignees for this merge request.");
                } else {
                    // MR doesn't exist
                    bot(turnContext, "Invalid Message Format!\n\n"
                            + "Valid Format : **!assignees** *\\<MR link>*");
                }
            }
            else if(CheckMessageType.isGetAllMergeRequests(s)){
                // syntax : !get-mrs <state> <max_count>
                int n = s.split("\\s+").length;
                // check string length
                if(n == 1){
                    bot(turnContext, "State and max count not specified.");
                } else if(n == 2){
                    // default count
                    String email = teamsAcc.getEmail();
                    Pair<String, ArrayList<Mention>> response = AllMergeRequests.getMRs(s, email);
                    if(response.getRight() == null){
                        bot(turnContext, response.getLeft());
                    } else sendMentionMessageToSelfWithMultipleMentions(
                            turnContext, response.getLeft(), response.getRight());
                } else if(isAllowed(s, 3)){
                    // valid format
                    String email = teamsAcc.getEmail();
                    Pair<String, ArrayList<Mention>> response = AllMergeRequests.getMRs(s, email);
                    if(response.getRight() == null){
                        bot(turnContext, response.getLeft());
                    } else sendMentionMessageToSelfWithMultipleMentions(
                            turnContext, response.getLeft(), response.getRight());
                }
                else{
                    // length is more OR number is wrong.
                    bot(turnContext, "Invalid Message Format!\n\n"
                                        + "Valid Format : **!get-mrs** *\\<state>* *\\<max_count>*");
                }
            }
            else if(CheckMessageType.isCodeChangesLinks(s)){
                // syntax : !code-changes <MR link>
                int n = s.split("\\s+").length;
                // check string length
                if(n == 1){
                    // length is less
                    bot(turnContext, "MR Link not specified.");
                } else if(isAllowed(s, 2)){
                    // valid format
                    Pair<String, ArrayList<Mention>> response = CodeChangesLinks.handleCC(s);
                    if(response.getRight() == null){
                        bot(turnContext, response.getLeft());
                    } else {
                        sendMentionMessageToSelfWithMultipleMentions(
                                turnContext, response.getLeft(), response.getRight());
                    }
                } else{
                    // length is more
                    bot(turnContext, "Invalid Message Format!\n\n"
                                        + "Valid Format : **!changes-links** *\\<MR link>*");
                }
            }
            else if(CheckMessageType.isNetUnitTest(s)){
                // syntax : !unit-tests <MR link>
                int n = s.split("\\s+").length;
                // check string length
                if(n == 1){
                    // length is less
                    bot(turnContext, "MR Link not specified.");
                } else if(isAllowed(s, 2)){
                    // valid format
                    Pair<Integer, Integer> unitTests = GetUnitTestCount.unitTests(s);
                    String response = "Unit tests (+) : "
                            + unitTests.getLeft()
                            + "\n\n"
                            + "Unit tests (-) : "
                            + unitTests.getRight();
                    bot(turnContext, response);
                } else{
                    // length is more
                    bot(turnContext, "Invalid Message Format!\n\n"
                                        + "Valid Format : **!unit-tests** *\\<MR link>*");
                }
            }
            else if(CheckMessageType.isPipeline(s)){
                // syntax : !pipeline <MR link>
                int n = s.split("\\s+").length;
                // check string length
                if(n == 1){
                    // length is less
                    bot(turnContext, "MR Link not specified.");
                } else if(isAllowed(s, 2)){
                    // valid format
                    String response = PipelineStatus.handlePipeline(s);
                    bot(turnContext, response);
                }
                else{
                    // length is more
                    bot(turnContext, "Invalid Message Format!\n\n"
                                        + "Valid Format : **!pipeline** *\\<MR link>*");
                }
            }
            else if(CheckMessageType.isSetDefault(s)){
                // syntax : !set-default <max_count>
                int n = s.split("\\s+").length;
                if(n == 1){
                    bot(turnContext, "Time not specified.");
                } else if(n > 2){
                    bot(turnContext, "Invalid Time-setting format!");
                } else {
                    String count = s.split("\\s+")[1];
                    if(!checkPositiveInteger(count)){
                        // count is invalid
                        bot(turnContext, "Invalid Number Format!"); // correct format
                    } else {
                        // count is valid
                        default_count = Integer.parseInt(count);
                        bot(turnContext, "Default count set to " + default_count + ".");
                    }
                }
            }
            else if(CheckMessageType.isSetProjectID(s)){
                // syntax : !set-project <id>
                int n = s.split("\\s+").length;
                if(n == 1){
                    bot(turnContext, "Project ID not specified.");
                }
                else if(n == 2){
                    String id = s.split("\\s+")[1];
                    if(checkPositiveLong(id)){
                        project_id = id;
                        bot(turnContext, "Project ID set to " + project_id + ".");
                    }

                }
                else{
                    bot(turnContext, "Invalid Setting Format!");
                }
            }
            else if(CheckMessageType.isHelp(s)){
                // syntax : !help
                String response = Help.handleHelp(name);
                bot(turnContext, response);
            }
            else{
                // invalid command
                bot(turnContext, "Invalid Command!\n\n"
                + "Please refer to **!help** to get summary of commands!");
            }
        }
        return CompletableFuture.completedFuture(null);
    }
    @Override
    protected CompletableFuture<Void> onMembersAdded(
        List<ChannelAccount> membersAdded,
        TurnContext turnContext
    ) {
        conRef = turnContext.getActivity().getConversationReference();
        ChannelAccount sentBy = turnContext.getActivity().getFrom();
        TeamsChannelAccount teamsAcc = TeamsInfo.getMember(turnContext, sentBy.getId()).join();
        String Name = teamsAcc.getName();
        String TextString = "**Welcome **"
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

        return membersAdded.stream()
            .filter(
                member -> !StringUtils
                    .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
            ).map(channel -> turnContext.sendActivity(MessageFactory.text(TextString)))
            .collect(CompletableFutures.toFutureList()).thenApply(resourceResponses -> null);
    }
}
