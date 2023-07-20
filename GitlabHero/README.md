# Gitlab Hero
## MS Teams bot to track Gitlab merge requests for Sprinklr's projects
Gitlab Hero is a one stop solution to manage your Merge Request related issues. This tool is designed to streamline and simplify the process of managing and reviewing merge requests in Sprinklr's projects. With a wide range of commands at your disposal, this bot automates various tasks related to merge requests, making collaboration more efficient and organized.

Sending review requests has been automated with a simple command, assisting both the author and the reviewers to send and recieve merge request reviews in a jiffy! Other functionalities include :
- fetching the status of pipelines
- retrieving net unit tests (added and subtracted)
- viewing assignees and reviewers
- notifying reviewers
- and more

*Watch [this](https://drive.google.com/file/d/1uyyRegvZWCWMIthYdxqoTZOC5er2bxeY/view?usp=drive_link) demo video for a quick overview*

## How to run it
1. Setting up your bot in Azure
    1. Follow along [this](https://www.youtube.com/watch?v=mASUW4Hxxc0&list=PLWZJrkeLOrbbh9EgDsFylRx4XmXbWaG4B&index=2&t=330s) video upto 5:42
    2. Make sure to save you Bot ID and Password
    3. The bot runs on port 3978. Use Ngrok to tunnel from you localhost using the ./ngrok http 3978 command
    4. Paste the forwarding link from ngrok in messaging endpoint in configuration of your bot in azure portal
    5. Append ```/api/messages``` in the messaging endpoint
    6. Open application.properties in "mrGenie Final" and update the App ID and Password
2. Setting up your database
    1. Create a cluster and database in MongoDB
    2. Add a collection called ```users```
    3. Open application.properties in "Gitlab Hero" and update spring.data.mongodb.uri and spring.data.mongodb.database with your connection string and database name respectively
3. Uploading the bot on teams
    1. Open manifest.json file in "Gitlab Hero" in the "src" folder
    2. Update "id" and "botId" with your Bot ID from Azure
    3. Select "manifest.json", "icon-color.png", "icon-outline.png" and compress
    4. Under Apps section in teams select "Manage apps"
    5. Select "Upload an App"
    6. Upload the zip file generated in step iii
4. Running the java program
    1. Download the repo
    2. Open "Gitlab Hero" in an IDE of your choice
    3. Update the constants in the GitlabHero.java file to your preference
    4. Use maven to package the project into a .jar file using the ```mvn package``` command
    5. Run the .jar file in target using ```java -jar ./target/mr-genie-1.0.0.jar```

