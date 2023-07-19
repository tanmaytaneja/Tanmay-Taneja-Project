package com.mycompany.echo.model;

import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Document(collection = "collection2.0")
@Component
public class Resource {
    @Id
    private String resource;
    private boolean isLocked = false;
    private LocalDateTime expirationTime;
    private TeamsChannelAccount teamsAcc;
    private String user;
    private ConversationReference conRef;

    public ConversationReference getConRef() {
        return conRef;
    }

    public void setConRef(ConversationReference conRef) {
        this.conRef = conRef;
    }

    public void setResourceName(String name){
        this.resource = name;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public LocalDateTime getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(LocalDateTime expirationTime) {
        this.expirationTime = expirationTime;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean locked) {
        isLocked = locked;
    }

    public String getResourceName() {
        return resource;
    }

    public TeamsChannelAccount getTeamsAcc() {
        return teamsAcc;
    }
    public void setTeamsAcc(TeamsChannelAccount teamsAcc) {
        this.teamsAcc = teamsAcc;
    }
}
