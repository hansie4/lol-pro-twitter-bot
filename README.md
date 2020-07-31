# lol-pro-twitter-bot

## Introduction

 A Java program that uses the Riot Games, Twitch, and Twitter Api's to tweet out whenever a pro League Of Legends player, or any player who's information is included in the player roster file, gets into a solo queue game that is has a "GameScore", read below for more info, value that is greater than or equal to the MINIMUM_GAMESCORE_TO_TWEET value in the config file and has an active streamer in the game. You can find my personal twitter bot [here](https://twitter.com/lol_pro_watch).

## How to use lol-pro-twitter-bot

 To use the twitter bot, run the main method in the Main class with the location of the config file and how long you would like the bot to run for in seconds(enter a negative value for the bot to run till forcably forcibly), passed in as command line arguments. The location of the config file first and an integer for the time to run in seconds, second.
 
## Files
#### Player Roster File
 The player roster file is a .csv file containing information about players that you want to track. The first line of the file should be:
 ```
 Player Name,Position,Team,Twitter Handle,Twitch Name,League Accounts,,
 ```
 denoting each entry in the csv file.The first line of the file should be these lables as the code ignores the first line of the file. The extra commas at the end are for multiple accounts. You can add as many accounts as you would like for each player. If you do not have a twitch name or a twitter handle then enter a '-' instead of leaving the space empty. To avoid any other problems account names should be placed from left to right without blank spaces in between:
 Correct:
 ```
 Poome,SUPPORT,100 Thieves Academy,poomelol,poome,100 Poome,,
 ```
 Incorrect:
 ```
 Poome,SUPPORT,100 Thieves Academy,poomelol,poome,,100 Poome,
 ```
 If you would like to use my file for the LCS you can find it [here](https://docs.google.com/spreadsheets/d/1ej2HGbZBQM48YklQkzNFfpLEbk0hqklUUVV7C5-eges/edit?usp=sharing). I just download it as a .csv and place it in the data folder.
 The position for each player for should be either TOP, JUNGLE, MID, ADC, or SUPPORT. If something other than these is input, it will default to SUPPORT. The twitter handles in the file should not include the '@'. The location of this file should be put in the config file.

#### Config File
 The config file is a .properties file that contains all the vital information the twitter bot needs to run. It contains 15 different key value pairs.
 ex)
 ```
 LOCATION_FOR_LOG_FILE=data\\logs.log
 PLAYER_ROSTER_FILE_LOCATION=data\\LoL_Pro_Roster - NA_Summer_2020.csv
 RIOT_API_KEY=
 RIOT_API_REGION=na1
 TWITCH_CLIENT_ID=
 TWITCH_CLIENT_SECRET=
 TWITTER_CONSUMER_KEY=
 TWITTER_CONSUMER_SECRET=
 TWITTER_ACCESS_TOKEN=
 TWITTER_ACCESS_TOKEN_SECRET=
 MINIMUM_GAMESCORE_TO_TWEET=3500
 INTERVAL_TO_SCAN_ACTIVE_GAMES_IN_SECONDS=480
 SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED_RIOT_API=30
 SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED_TWITCH_API=30
 SECONDS_TO_WAIT_AFTER_RATE_LIMIT_REACHED_TWITTER_API=30
 ```
 The order of the key value pairs does not matter. You would just fill in all of your information and pass in this file's location when you run the program as a command line argument.
 
 #### Log File
 The log file is a .log file that if it does not exist, will be created at the start of the program. It just contains logged information from when the twitter bot is running. The location you want the log file should be put in the config file.
 
 ## How the "GameScore" is calculated
 
 The "GameScore" is a value assigned to an active solo queue game that is scanned to determine how entertaining the game would be to watch. Using this system is good because the twitter bot does not spam tweets of uninteresting games. Logic to "GameScore":
 1. Games with no one streaming are assigned a gamescore of 0
 2. Games with only one player from the player roster file are assigned a gamescore of 0
 3. Players without "academy" or "Academy" in their team are assigned a value of 1000 and added up
 4. Players with= "academy" or "Academy" in their team are assigned a value of 500 and added up
 5. The heighest viewed streamer's viewcount is then put to the power of 0.6 and then multiplied by 8.5, then added to the gamescore
 6. The gamescore is if step 1 and 2 are passed is the sum of stepes 3, 4, and 5
 
 The reason for the heightest viewed streamer's viewcount being put through that function is so that gamescores are not heavily skewed towards high view streamers but favored more towards games with many players from the player roster file. I got the function for the streamers viewcount to gamescore by graphing the points of what I think some values of the viewcount should be worth and then making trendlines and taking the one with the highest R^2 value.
 
 ## Why I made this and what I learned

 I made this twitter bot because I was interest in working with api's and it would give me something I wanted as I enjoy watching the game League of Legends at a high level. This project taught me a lot more than I thought it would when I started. Some of the things I learned include:
 - Making api calls with a RESTful client
 - Authentican protocals like OAuth1.0a(Twitter api), OAuth2.0(Twitch api), Basic(Riot Games api)
 - Percent encoding
 - HMAC-SHA1 hashing
 
 ## Libraries Used
 - org.json json parser library which can be found [here](https://github.com/stleary/JSON-java).

 ## Resources Used
 - https://developer.riotgames.com/apis
 - https://dev.twitch.tv/docs/api/
 - https://developer.twitter.com/en/docs
 - https://stackoverflow.com/questions/10786042/java-url-encoding-of-query-string-parameters
 - https://stackoverflow.com/questions/27220327/how-to-percent-encode-in-java
 - https://stackoverflow.com/questions/1609899/java-equivalent-to-phps-hmac-sha1
 - https://openjdk.java.net/groups/net/httpclient/recipes.html
 
