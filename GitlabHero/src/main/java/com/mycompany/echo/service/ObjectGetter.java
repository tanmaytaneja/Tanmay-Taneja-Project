package com.mycompany.echo.service;

import com.microsoft.bot.schema.Pair;
import com.mycompany.echo.controller.BotController;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
@Service
public class ObjectGetter {
    public Pair<Object, String> getObj(String website){
        try{
            // default
            URL url = new URL(website);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("PRIVATE-TOKEN", "glpat-xJ12p2tg3U95oDhTUG_o");
            conn.setRequestMethod("GET");
            conn.connect();
            int responseCode = conn.getResponseCode();
            if(responseCode != 200){
                throw new RuntimeException("HttpResponseCode: " + responseCode);
            } else {
                // default
                StringBuilder informationString = new StringBuilder();
                Scanner scanner = new Scanner(conn.getInputStream());
                while(scanner.hasNext()){
                    informationString.append(scanner.nextLine());
                }
                scanner.close();
                JSONParser parse = new JSONParser();
                String infoString = informationString.toString();
                Object obj = parse.parse(infoString);
                return new Pair<>(obj, "");
            }
        }
        catch(RuntimeException re){
            re.printStackTrace();
            return new Pair<>(null, "Runtime Exception!");
        }
        catch(Exception e){
            e.printStackTrace();
            return new Pair<>(null, ""); // handle differently for each
        }
    }
}
