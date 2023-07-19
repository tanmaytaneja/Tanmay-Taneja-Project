package com.mycompany.echo.models;

import com.microsoft.bot.schema.ConversationReference;
import com.microsoft.bot.schema.teams.TeamsChannelAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Document(collection = "collection1.0")
@Component
public class User {
    @Id
    private String email;
    private ConversationReference conRef;
    private TeamsChannelAccount teamsAcc;
    public void setConRef(ConversationReference conRef) {
        this.conRef = conRef;
    }

    public ConversationReference getConRef() {
        return conRef;
    }

    public TeamsChannelAccount getTeamsAcc() {
        return teamsAcc;
    }
    public void setTeamsAcc(TeamsChannelAccount teamsAcc) {
        this.teamsAcc = teamsAcc;
    }
    public void setEmail(String email) {
        this.email = email;
    }
}
