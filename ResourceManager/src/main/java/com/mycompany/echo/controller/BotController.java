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
import com.mycompany.echo.utils.Help;
import com.mycompany.echo.service.ResourceFunctions;
import com.mycompany.echo.repository.ResourceRepository;
import com.mycompany.echo.service.ResourceService;
import com.mycompany.echo.model.Resource;
import com.mycompany.echo.utils.CheckMessageType;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
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
public class BotController extends ActivityHandler {

    @Autowired
    public ResourceService resourceService;
    @Autowired
    public ResourceFunctions resourceFunctions;
    public static BotFrameworkHttpAdapter Adapter;
    public BotController(BotFrameworkHttpAdapter adapter){
        super();
        Adapter = adapter;
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
    public boolean checkPositiveLong(String duration){
        try {
            long time = Long.parseLong(duration);
            return time > 0;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean timeFormatChecker(String[] times, int idx){
        int n = times.length;
        boolean D = false, H = false, M = false;
        for(int i=idx; i<n; i++){
            int len = times[i].length();
            char type = times[i].charAt(len - 1);
            if((type != 'd' && type != 'h' && type != 'm') || (len == 1)) return false;
            String duration = times[i].substring(0, len - 1);
            System.out.println(duration);
            if(checkPositiveLong(duration)){
                long time = Long.parseLong(duration);
                if(type == 'd'){
                    if(D) return false;
                    D = true;
                    if(time > 6) return false;
                } else if(type == 'h'){
                    if(H) return false;
                    D = true;
                    H = true;
                    if(time > 23) return false;
                } else {
                    if(M) return false;
                    D = true;
                    H = true;
                    M = true;
                    if(time > 59) return false;
                }
            } else return false;
        } return true;
    }
    public boolean isAllowed(String s){
        String[] spaces = s.split("\\s+");
        return resourceService.checkResourceExists(spaces[1]);
    }
    public void autoUnlockResource(long duration, TurnContext turnContext, String resource_name){
        Timer timer = new Timer();

        // Create a `TimerTask` object that will send the message.
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                Resource resource = resourceService.getResourceByName(resource_name);
                if(resource.isLocked()){
                    resource.setLocked(false);
                    resource.setExpirationTime(null);
                    resource.setUser(null);
                    resource.setTeamsAcc(null);
                    resource.setConRef(null);
                    resourceService.saveResource(resource);
                    String output = "Lock for resource **"
                            + resource_name
                            + "** has expired.\n"
                            + "Resource **"
                            + resource_name
                            + "** is now available for use.";
                    bot(turnContext, output);
                }
            }
        };
        // Schedule the `TimerTask` object to run after 5 minutes.
        timer.schedule(task, duration * 60 * 1000); // 5 minutes in milliseconds
    }
    public void notificationSender(String response, long duration, TurnContext turnContext, String resource_name){
        String secondWord = response.split("\\s+")[1];
        if(secondWord.equals("granted") && duration > 0){
            String[] list = response.split("\\s+");
            int n = list.length;
            String expirationTime = list[n - 4] + " "
                                + list[n - 3] + " "
                                + list[n - 2] + " "
                                + list[n - 1];
            Timer timer = new Timer();

            // Create a `TimerTask` object that will send the message.
            TimerTask task = new TimerTask() {
                @Override
                public void run() {
                    Resource resource = resourceService.getResourceByName(resource_name);
                    if(resource.isLocked()){
                        String output = "Lock for resource **"
                                + resource_name
                                + "** will expire in 5 minutes at "
                                + expirationTime;
                        bot(turnContext, output);
                        autoUnlockResource(5, turnContext, resource_name);
                    }
                }
            };
            // Schedule the `TimerTask` object to run 5 minutes before scheduled unlock.
            timer.schedule(task, duration * 60 * 1000); // 5 minutes in milliseconds
        } else if(secondWord.equals("granted")){
            autoUnlockResource(duration + 5, turnContext, resource_name);
        }
    }
    private Long findTotalTime(String[] times, int idx) {
        int n = times.length;
        long time = 0;
        for(int i=idx; i<n; i++){
            int len = times[i].length();
            long duration = Long.parseLong(times[i].substring(0, len - 1));
            char type = times[i].charAt(len - 1);
            if(type == 'd'){
                time += duration * 24 * 60;
            } else if(type == 'h'){
                time += duration * 60;
            } else{
                time += duration;
            }
        } return time;
    }
    public Mention mentionCreator(TeamsChannelAccount teamsAcc, String name) {
        Mention mention = new Mention();
        mention.setMentioned(teamsAcc);
        mention.setText(
                "<at>" + name + "</at>"
        );
        return mention;
    }
    private void bot(TurnContext turnContext, String message){
        turnContext.sendActivity(MessageFactory.text(message)).thenApply(sendResult -> null);
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
    public long default_time = 120;
    private static ConversationReference conRef;
    @Override
    protected CompletableFuture<Void> onMessageActivity(TurnContext turnContext){
        conRef = turnContext.getActivity().getConversationReference();
        if(turnContext.getActivity().isType(ActivityTypes.MESSAGE) && turnContext.getActivity().getText() != null){
            turnContext.getActivity().removeRecipientMention();
            ChannelAccount sentBy = turnContext.getActivity().getFrom();
            TeamsChannelAccount teamsAcc = TeamsInfo.getMember(turnContext, sentBy.getId()).join();
            String email = teamsAcc.getEmail();
            String s = turnContext.getActivity().getText();
            String name = teamsAcc.getName();
            if(CheckMessageType.isCheck(s)){
                // syntax : !check <resource>
                int n = s.split("\\s+").length;
                // check string length
                if(n < 2){
                    bot(turnContext, "Resource not specified.");
                } else if(n > 2){
                    bot(turnContext, "Invalid Checking Format!");
                } else if(isAllowed(s)){
                    // resource exists
                    Pair<String, Mention> status = resourceFunctions.check(s);
                    if(status.getRight() == null){
                        bot(turnContext, status.getLeft());
                    } else {
                        sendMentionMessageToSelf(turnContext, status.getLeft(), status.getRight());
                    }
                } else {
                    // resource does not exist
                    String resource_name = s.split("\\s+")[1];
                    bot(turnContext, "Resource **" + resource_name + "** does not exist in the database.\n\n"
                            + "Get list of resources by **!get** *\\<type>*.");
                }
            }
            else if(CheckMessageType.isLocker(s)){
                // syntax : !lock <resource> <duration(minutes)>
                int n = s.split("\\s+").length;
                // check string length
                if(n == 1){
                    bot(turnContext, "Resource and duration not specified.");
                } else if(n == 2){
                    Pair<String, Mention> response = resourceFunctions.lock(default_time, s, teamsAcc, conRef);
                    if(response.getRight() == null){
                        bot(turnContext, response.getLeft());
                    } else {
                        sendMentionMessageToSelf(turnContext, response.getLeft(), response.getRight());
                    }
                    String resource_name = s.split("\\s+")[1];
                    notificationSender(response.getLeft(), default_time - 5, turnContext, resource_name);
                }  else if(n > 5){
                    bot(turnContext, "Invalid Locking Format!");
                } else if(isAllowed(s)){
                    // if resource exists
                    String[] times = s.split("\\s+");
                    if(!timeFormatChecker(times, 2)){
                        // time is invalid
                        bot(turnContext, "Invalid Time Format!"); // correct format
                    } else {
                        // time is valid
                        long totalTime = findTotalTime(times, 2);
                        Pair<String, Mention> response = resourceFunctions.lock(totalTime, s, teamsAcc, conRef);
                        if(response.getRight() == null){
                            bot(turnContext, response.getLeft());
                        } else {
                            sendMentionMessageToSelf(turnContext, response.getLeft(), response.getRight());
                        }
                        String resource_name = s.split("\\s+")[1];
                        notificationSender(response.getLeft(), totalTime - 5, turnContext, resource_name);
                    }
                } else {
                    // if resource does not exist
                    String resource_name = s.split("\\s+")[1];
                    bot(turnContext, "Resource **" + resource_name + "** does not exist in the database.\n\n"
                            + "Get list of resources by **!get** *\\<type>*.");
                }
            }
            else if(CheckMessageType.isUnlocker(s)){
                // syntax : !unlock <resource>
                int n = s.split("\\s+").length;
                // check string length
                if(n < 2){
                    // length is less
                    bot(turnContext, "Resource not specified.");
                } else if(n > 2){
                    // length is more
                    bot(turnContext, "Invalid Unlocking Format!");
                } else if(isAllowed(s)){
                    // resource exists
                    Pair<String, Mention> response = resourceFunctions.unlock(s, teamsAcc);
                    if(response.getRight() == null){
                        bot(turnContext, response.getLeft());
                    } else {
                        sendMentionMessageToSelf(turnContext, response.getLeft(), response.getRight());
                    }
                } else {
                    // resource does not exist
                    String resource_name = s.split("\\s+")[1];
                    bot(turnContext, "Resource **" + resource_name + "** does not exist in the database.\n\n"
                            + "Get list of resources by **!get** *\\<type>*.");
                }
            }
            else if(CheckMessageType.isNotifier(s)){
                // syntax : !notify <resource>
                int n = s.split("\\s+").length;
                // check string length
                if(n < 2){
                    // length is less
                    bot(turnContext, "Resource not specified.");
                } else if(n > 2){
                    // length is more
                    bot(turnContext, "Invalid Notifying Format!");
                } else if(isAllowed(s)){
                    // resource exists
                    String response = resourceFunctions.notify(s, email);
                    if(!Objects.equals(response, "OK")) bot(turnContext, response);
                    else{
                        String resource_name = s.split("\\s+")[1];
                        Resource resource = resourceService.getResourceByName(resource_name);
                        ConversationReference ownerConRef = resource.getConRef();
                        Mention ownerMention = mentionCreator(resource.getTeamsAcc(), resource.getUser());
                        Mention requesterMention = mentionCreator(teamsAcc, name);
                        String message = "**"
                                + requesterMention.getText()
                                + "** is requesting to access **"
                                + resource_name
                                + "** from you!";
                        sendMentionMessageToOthers(ownerConRef, message, requesterMention);
                        String msg = "**"
                                + ownerMention.getText()
                                + "** has been notified!";
                        sendMentionMessageToSelf(turnContext, msg, ownerMention);
                    }
                } else {
                    // resource does not exist
                    String resource_name = s.split("\\s+")[1];
                    bot(turnContext, "Resource **" + resource_name + "** does not exist in the database.\n\n"
                    + "Get list of resources by **!get** *\\<type>*.");
                }
            }
            else if(CheckMessageType.isGet(s)){
                // syntax : !get <status>
                int n = s.split("\\s+").length;
                // check string length
                if(n == 1){
                    // length is less
                    bot(turnContext, "Status not specified.");
                } else if(n == 2) {
                    // valid command
                    Pair<String, ArrayList<Mention>> response = resourceFunctions.get(s);
                    if(response.getRight() == null){
                        // resources do not exist
                        bot(turnContext, response.getLeft());
                    } else {
                        // resources exist
                        sendMentionMessageToSelfWithMultipleMentions(turnContext, response.getLeft(), response.getRight());
                    }
                } else {
                    // length is more
                    bot(turnContext, "Invalid Get Format!");
                }

            }
            else if(CheckMessageType.isAdd(s)){
                // syntax : !add <resource>
                int n = s.split("\\s+").length;
                // check string length
                if(n == 1){
                    // length is less
                    bot(turnContext, "Resource not specified.");
                } else if(n > 2){
                    // length is more
                    bot(turnContext, "Invalid adding format!");
                } else{
                    // valid command
                    String response = resourceFunctions.add(s);
                    bot(turnContext, response);
                }
            }
            else if(CheckMessageType.isDelete(s)){
                // syntax : !delete <resource>
                int n = s.split("\\s+").length;
                if(n == 1){
                    bot(turnContext, "Resource not specified.");
                } else if(n > 2){
                    bot(turnContext, "Invalid deleting format!");
                } else {
                    Pair<String, Mention> response = resourceFunctions.delete(s);
                    if(response.getRight() == null){
                        bot(turnContext, response.getLeft());
                    } else sendMentionMessageToSelf(turnContext, response.getLeft(), response.getRight());
                }
            }
            else if(CheckMessageType.isSetDefault(s)){
                // syntax : !set-default <duration(Xd Yh Zm)>
                int n = s.split("\\s+").length;
                if(n == 1){
                    bot(turnContext, "Time not specified.");
                } else if(n > 2){
                    bot(turnContext, "Invalid Time-setting format!");
                } else {
                    String[] times = s.split("\\s+");
                    if(!timeFormatChecker(times, 1)){
                        // time is invalid
                        bot(turnContext, "Invalid Time Format!"); // correct format
                    } else {
                        // time is valid
                        default_time = findTotalTime(times, 1);
                        bot(turnContext, "Default time set to " + default_time + ".");
                    }
                }
            }
            else if(CheckMessageType.isHelp(s)){
                // syntax : !help
                String response = Help.handleHelp(name);
                bot(turnContext, response);
            }
            else{
                // invalid command
                bot(turnContext, "Invalid Command!\n\nPlease refer to **!help** to get summary of commands!");
            }
        }
        return CompletableFuture.completedFuture(null);
    }
    @Override
    protected CompletableFuture<Void> onMembersAdded(
            List<ChannelAccount> membersAdded,
            TurnContext turnContext
    ) {
        ChannelAccount sentBy = turnContext.getActivity().getFrom();
        TeamsChannelAccount teamsAcc = TeamsInfo.getMember(turnContext, sentBy.getId()).join();
        conRef = turnContext.getActivity().getConversationReference();
        String Name = teamsAcc.getName();
        String TextString = "**Welcome **"
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

        return membersAdded.stream()
                .filter(
                        member -> !StringUtils
                                .equals(member.getId(), turnContext.getActivity().getRecipient().getId())
                ).map(channel -> turnContext.sendActivity(MessageFactory.text(TextString)))
                .collect(CompletableFutures.toFutureList()).thenApply(resourceResponses -> null);
    }
}
