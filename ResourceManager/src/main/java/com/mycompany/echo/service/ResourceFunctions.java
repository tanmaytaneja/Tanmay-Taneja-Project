package com.mycompany.echo.service;

import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.Mention;
import com.microsoft.bot.schema.Pair;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import com.mycompany.echo.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ResourceFunctions {
    @Autowired
    public ResourceService resourceService;

    public Mention mentionCreator(TeamsChannelAccount teamsAcc, String name) {
        Mention mention = new Mention();
        mention.setMentioned(teamsAcc);
        mention.setText(
                "<at>" + name + "</at>"
        );
        return mention;
    }
    public Pair<String, Mention> check(String s){
        String resource_name = s.split("\\s+")[1];
        Resource resource = resourceService.getResourceByName(resource_name);
        if(!resource.isLocked()){
            String message = "Resource **"
                    + resource_name
                    + "** is available for use.";
            return new Pair<>(message, null);
        } else {
            String time = resource.getExpirationTime().toLocalTime().toString().substring(0, 8);
            String date = resource.getExpirationTime().toString().substring(0, 10);
            date = dateConverter(date);
            String finalTime = time + " (" + date + ")";
            Mention ownerMention;
            ownerMention = mentionCreator(resource.getTeamsAcc(), resource.getUser());
            String message = "Resource **"
                    + resource_name
                    + "** is currently locked by **"
                    + ownerMention.getText()
                    + "** till **"
                    + finalTime
                    + "**.\n\n Notify them by **!notify** *\\<resource>*.";
            return new Pair<>(message, ownerMention);
        }
    }

    public String notify(String s, String email){
        String resource_name = s.split("\\s+")[1];
        Resource resource = resourceService.getResourceByName(resource_name);
        if(resource == null) return "Resource **"
                                + resource_name
                                + "** does not exist.";
        if(!resource.isLocked()) return "Resource **"
                                + resource_name
                                + "** is available for use.";
        String ownerEmail = resource.getTeamsAcc().getEmail();
        if(Objects.equals(ownerEmail, email)) return "Resource **"
                                + resource_name
                                + "** is currently locked by you.";
        return "OK";
    }
    public Pair<String, Mention> lock(long totalTime, String s, TeamsChannelAccount teamsAcc, ConversationReference conRef){
        String resource_name = s.split("\\s+")[1];
        Resource resource = resourceService.getResourceByName(resource_name);
        LocalDateTime currentTime = LocalDateTime.now();
        if(resource == null){
            String message = "Resource **"
                    + resource_name
                    + "** does not exist.";
            return new Pair<>(message, null);
        }
        if(!resource.isLocked()){
            String name = teamsAcc.getName();
            resource.setLocked(true);
            resource.setExpirationTime(currentTime.plusMinutes(totalTime));
            System.out.println(resource.getExpirationTime());
            resource.setUser(name);
            resource.setTeamsAcc(teamsAcc);
            resource.setConRef(conRef);
            resourceService.saveResource(resource);
            String time = resource.getExpirationTime().toLocalTime().toString().substring(0, 8);
            String date = resource.getExpirationTime().toString().substring(0, 10);
            date = dateConverter(date);
            String finalTime = time + " (" + date + ")";
            System.out.println(finalTime);

            String message = "Lock granted for resource **"
                    + resource_name
                    + "** till **"
                    + finalTime
                    + "**.";
            return new Pair<>(message, null);
        } else {
            String time = resource.getExpirationTime().toLocalTime().toString().substring(0, 8);
            String date = resource.getExpirationTime().toString().substring(0, 10);
            date = dateConverter(date);
            String finalTime = time + " (" + date + ")";
            Mention ownerMention = mentionCreator(resource.getTeamsAcc(), resource.getUser());
            String message = "Resource **"
                    + resource_name
                    + "** is locked by **"
                    + ownerMention.getText()
                    + "** till **"
                    + finalTime
                    + "**.";
            return new Pair<>(message, ownerMention);
        }
    }

    private String dateConverter(String date) {
        // date = year-month-date (4 + 1 + 2 + 1 + 2)
        String year = date.substring(0, 4);
        String month = date.substring(5, 7);
        String day = date.substring(8, 10);
        if(day.charAt(0) == '0') day = day.substring(1, 2);
        return day + " " + toMonth(month) + ", " + year;
    }
    private String toMonth(String month) {
        if(Objects.equals(month, "01")) return "January";
        else if(Objects.equals(month, "02")) return "February";
        else if(Objects.equals(month, "03")) return "March";
        else if(Objects.equals(month, "04")) return "April";
        else if(Objects.equals(month, "05")) return "May";
        else if(Objects.equals(month, "06")) return "June";
        else if(Objects.equals(month, "07")) return "July";
        else if(Objects.equals(month, "08")) return "August";
        else if(Objects.equals(month, "09")) return "September";
        else if(Objects.equals(month, "10")) return "October";
        else if(Objects.equals(month, "11")) return "November";
        else return "December";
    }

    public Pair<String, Mention> unlock(String s, TeamsChannelAccount teamsAcc){
        String resource_name = s.split("\\s+")[1];
        Resource resource = resourceService.getResourceByName(resource_name);
        if (resource==null){
            String message = "Resource **"
                + resource_name
                + "** does not exist.";
            return new Pair<>(message, null);
        }
        if(!resource.isLocked()){
            String message = "Resource **"
                    + resource_name
                    + "** is available for use.";
            return new Pair<>(message, null);
        } else {
            String user = resource.getUser();
            String name = teamsAcc.getName();
            if(user.equals(name)){
                resource.setLocked(false);
                resource.setUser(null);
                resource.setTeamsAcc(null);
                resource.setExpirationTime(null);
                resource.setConRef(null);
                resourceService.saveResource(resource);
                String message =  "Resource **"
                        + resource_name
                        + "** has been unlocked and is available for use.";
                return new Pair<>(message, null);
            } else {
                String time = resource.getExpirationTime().toLocalTime().toString().substring(0, 8);
                String date = resource.getExpirationTime().toString().substring(0, 10);
                date = dateConverter(date);
                String finalTime = time + " (" + date + ")";
                Mention ownerMention = mentionCreator(resource.getTeamsAcc(), resource.getUser());
                String message =  "Resource **"
                        + resource_name
                        + "** already locked by **"
                        + ownerMention.getText()
                        + "** till **"
                        + finalTime
                        + "**.";
                return new Pair<>(message, ownerMention);
            }
        }
    }
    public Pair<String, ArrayList<Mention>>get(String s) {
        List<Resource> resources = resourceService.getAll();
        String state = s.split("\\s+")[1];
        System.out.println(state);
        if((!Objects.equals(state, "all"))
            && (!Objects.equals(state, "locked"))
            && (!Objects.equals(state, "unlocked"))){
            String message = "Invalid state. Valid states are: \n\n"
                    + "1. all\n\n"
                    + "2. locked\n\n"
                    + "3. unlocked\n\n";
            return new Pair<>(message, null);
        }
        System.out.println("reached");
        String toReturn = "Here's a list of " + state + " resources :\n\n";
        int count = 0;
        if(Objects.equals(state, "all")){
            ArrayList<Mention> mentions = new ArrayList<>();
            for(Resource resource : resources){
                count++;
                String availability = "Available";
                if(resource.isLocked()){
                    Mention ownerMention = mentionCreator(resource.getTeamsAcc(), resource.getUser());
                    mentions.add(ownerMention);
                    String time = resource.getExpirationTime().toLocalTime().toString().substring(0, 8);
                    String date = resource.getExpirationTime().toString().substring(0, 10);
                    date = dateConverter(date);
                    String finalTime = time + " (" + date + ")";
                    availability = "Locked by : **"
                            + ownerMention.getText()
                            + "** till **"
                            + finalTime
                            + "**.";
                }
                toReturn += count
                        + ". **"
                        + resource.getResourceName()
                        + "** - "
                        + availability
                        + "\n\n";
            }
            if(count == 0){
                String message = "There are no resources in the database.";
                return new Pair<>(message, null);
            }
            return new Pair<>(toReturn, mentions); // have to send list
        }  else if(Objects.equals(state, "locked")){
            ArrayList<Mention> mentions = new ArrayList<>();
            for(Resource resource : resources){
                if(resource.isLocked()){
                    Mention ownerMention = mentionCreator(resource.getTeamsAcc(), resource.getUser());
                    mentions.add(ownerMention);
                    count++;
                    String time = resource.getExpirationTime().toLocalTime().toString().substring(0, 8);
                    String date = resource.getExpirationTime().toString().substring(0, 10);
                    date = dateConverter(date);
                    String finalTime = time + " (" + date + ")";
                    String availability = "Locked by : **"
                            + ownerMention.getText()
                            + "** till **"
                            + finalTime
                            + "**.";
                    toReturn += count
                            + ". **"
                            + resource.getResourceName()
                            + "** - "
                            + availability
                            + "\n\n";
                }
            }
            if(count == 0){
                String message = "There are no locked resources in the database.";
                return new Pair<>(message, null);
            }
            return new Pair<>(toReturn, mentions); // have to send list
        } else {
            for(Resource resource : resources){
                String availability = "Available";
                if(resource.isLocked()){
                    continue;
                }
                count++;
                toReturn += count
                        + ". **"
                        + resource.getResourceName()
                        + "** - "
                        + availability
                        + "\n\n";
            }
            if(count == 0){
                String message = "There are no unlocked resources in the database.";
                return new Pair<>(message, null);
            }
            return new Pair<>(toReturn, null);
        }
    }
    public String add(String s) {
        String resource_name = s.split("\\s+")[1];
        if(!resourceService.checkResourceExists(resource_name)){
            Resource r = new Resource();
            r.setResourceName(resource_name);
            resourceService.saveResource(r);
            return "Resource **" + resource_name + "** added successfully.";
        } else {
            return "Resource **" + resource_name + "** already exists.";
        }
    }
    public Pair<String, Mention> delete(String s){
        String resource_name = s.split("\\s+")[1];
        if(!resourceService.checkResourceExists(resource_name)){
            String message = "Resource **" + resource_name + "** does not exist in the database.";
            return new Pair<>(message, null);
        } else {
            Resource resource = resourceService.getResourceByName(resource_name);
            if (!resource.isLocked()) {
                resourceService.deleteByName(resource_name);
                String message = "Resource **" + resource_name + "** deleted successfully.";
                return new Pair<>(message, null);
            } else {
                Mention ownerMention = mentionCreator(resource.getTeamsAcc(), resource.getUser());
                String time = resource.getExpirationTime().toLocalTime().toString().substring(0, 8);
                String date = resource.getExpirationTime().toString().substring(0, 10);
                date = dateConverter(date);
                String finalTime = time + " (" + date + ")";
                String message = "Resource **"
                        + resource_name
                        + "** is locked by user **"
                        + ownerMention.getText()
                        + "** till **"
                        + finalTime
                        + "**.";
                return new Pair<>(message, ownerMention);
            }
        }
    }
}
