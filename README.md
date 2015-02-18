YRadio
=============

YRadio is YouTube Radio program written in Java that is capable of streaming music from YouTube users or playlists.

I wrote this program for personal use but the sourcecode is published under the GPLv3 license for public use.
I don't plan on supporting this code currently, so use at your own risk. Bug fixes and improvements are welcome.

Prerequisites
-------
* You must have Java 6 or 7 installed.
* You must have [youtube-dl](http://rg3.github.com/youtube-dl/download.html) installed and on your PATH.

Usage
-------
To run the program without launch options, simply double-click the jar folder.

To run the program with launch options or from a script, follow the "Options" section below.
 
Options
-------
All options are prefaced with '-', followed by the option name. If the option needs a value, add a '=' followed by a value.

* nogui : launch the program without an interface (command line).
* workspace (string) : the disk location for the program to store data. The location will be created if it doesn't exist.
* playlist (string) : the ID of the playlist to play.
* channel (string) : the user ID of the channel to play.
* quality (string) : the quality to download audio/video in.
  * Possible values:
    * 480 (default)
    * 720
    * 1080

### Example:

	java -jar YRadio.jar -nogui -workspace=/home/user/yradio -playlist=PL84C29E7EFBBCC874

The above code will start the program without an interface, using the workspace "/home/user/yradio" (unix file path, for windows use C:/LOCATION) and playing the playlist https://www.youtube.com/playlist?list=PL84C29E7EFBBCC874

Playing a channel, such as https://www.youtube.com/user/MrSuicideSheep would loook like:

	java -jar YRadio.jar -nogui -workspace=/home/user/yradio -channel=MrSuicideSheep

If you use both a channel and a playlist, the channel will take priority and the playlist ignored. I can't think of a logical reason why that would be necessary, so just don't do it.
