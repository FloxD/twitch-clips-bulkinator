# twitch-clips-bulkinator

This scuffed application is for downloading twitch clips in bulk in case you wanna make a backup.
There's a lot of room for improvement when it comes to the code especially when it comes to the downloading part itself.
But hey it works at least.

# running

I recommend using java 11 or above.
It could be that it also works with java 8 but I didn't test it.
To check if you have java 11+ properly installed open a cmd/terminal window and execute command `java -version`.

1. figure out the twitch user id from this page https://www.streamweasels.com/support/convert-twitch-username-to-user-id. You'll need this id for step 4 at `INSERT_BROADCASTER_ID`
1. figure out the batch-count. The clips will be downloaded in batches and 1 batch includes 20 clips. So that means a batch count of 5 means that 100 clips will be downloaded. Set your desired batch count in step 4 at `INSERT_BATCH_COUNT` 
1. download the jar from the release page
1. open a console window and run the jar using command `java -jar downloader-0.0.1.jar --twitch.broadcaster-id=INSERT_BROADCASTER_ID --twitch.batch-count=INSERT_BATCH_COUNT`
1. the program is ready once you see `Started DownloaderApplicationKt in X.XXX seconds`
1. open `http://localhost:8080` in your browser to start. You need to login with your twitch account there and then the download will start.

When you run the jar the application will create a folder in the same folder with the current date and time as the folder name.
You know when the download is done once no more log messages are being printed out and at the very end it should say just `finished`

# building

* install java 11 or above and maven 3.6
* run `mvn clean install`