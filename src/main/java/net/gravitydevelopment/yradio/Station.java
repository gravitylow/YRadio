package net.gravitydevelopment.yradio;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.SortedMap;
import java.util.TreeMap;

public class Station {
    
    public enum StationType {
        PLAYLIST, 
        CHANNEL,
    }
    
    private String feed = null;
    private String stationTitle = "Unknown";
    private StationType type;
    private SortedMap<String,String> videos = new TreeMap<String, String>();
    private Streamer currentStream;
    private Streamer nextStream;
    
    public Station(StationType type, String string) {
        this.type = type;
        feed = type == StationType.CHANNEL ? Settings.getChannelFeed(string) : Settings.getPlaylistFeed(string);
    }
    
    public void play() {
        // If the stream isn't already loaded & waiting for play
        if(currentStream == null) {
            
            // Assign current stream to next available in queue
            currentStream = getNextStream();
            
            // Ensure we're not done with the station
            if(currentStream != null) {
                // Download audio and let us know when the downloading finishes
                new Thread(new DownloadRunnable(currentStream, this)).start();
            } else {
                // End play
                stationTitle = "None";
            }
        }
    }
    
    private void playDownloaded() {
        // Queue next song
        queueNext();

        // Set track in GUI
        YRadio.setTitle(currentStream.getTitle());
        // Play
        currentStream.play();
        // Wait for play to finish
        new Thread(new PlayRunnable(currentStream, this)).start();        
    }
    
    private void playNext() {
        if(nextStream != null) {
            currentStream = nextStream;
            playDownloaded();
        }        
    }
    
    private void queueNext() {
        nextStream = getNextStream();

        // Ensure we're not done playing
        if(nextStream != null) {
            // Download audio and let us know when the downloading finishes
            new Thread(new DownloadRunnable(nextStream, this)).start();
        }     
    }
    
    private class PlayRunnable extends Thread {
        
        private Streamer stream;
        private Station station;
        
        public PlayRunnable(Streamer stream, Station station) {
            this.stream = stream;
            this.station = station;
        }

        @Override
        public void run() {
            while(!stream.isFinished()) {
                
            }
            station.notifyFinishedPlay(stream);
        }
    } 
    
    private class DownloadRunnable extends Thread {
        
        private Streamer stream;
        private Station station;
        
        public DownloadRunnable(Streamer stream, Station station) {
            this.stream = stream;
            this.station = station;
        }

        @Override
        public void run() {
            stream.download();
            while(!stream.isDownloaded()) {
            }
            station.notifyFinishedDownload(stream);
        }
    }    
    
    public void notifyFinishedDownload(Streamer stream) {
        YRadio.logDone("Notification of download recieved");
        if(stream == currentStream) {
            playDownloaded();
        }
    }
    
    public void notifyFinishedPlay(Streamer stream) {
        YRadio.logDone("Notification of track finish received");
        playNext();
    }    
    
    private Streamer getNextStream() {
        if(videos.keySet().isEmpty()) {
            return null;
        } else {
            Streamer stream = new Streamer(getNextID(), getNextSong(), this);
            pop();
            return stream;
        }
    }
    
    private String getNextSong() {
        return videos.firstKey();
    }
    
    private String getNextID() {
        return videos.get(getNextSong());
    }
    
    private void pop() {
        videos.remove(getNextSong());
    }
    
    public void parse() {
        YRadio.logProgress("Parsing feed for " + type + " " + feed);
        YRadio.setStatus("Loading "+type.toString().toLowerCase()+"...");
        // Decided to go with the crappy but easier way of parsing the API until I decide something better is needed.
	    BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(new URL(feed).openStream()));
        } catch (MalformedURLException ex) {
            YRadio.logError(ex.getMessage());
        } catch (IOException ex) {
            YRadio.logError(ex.getMessage());
        }
        
	    String line;
        
        try {
            boolean title = false;
            String id = "";
            String video = "";
            while((line = reader.readLine()) != null) {
                if(type == StationType.CHANNEL) {
                    // Channel username
                    if(line.contains("<author>")) {
                        stationTitle = line.split("<author>")[1].split("</author>")[0];
                    }
                    for(String string : line.split("(?=<)")) {
                        // Video comes first
                        if(string.contains(":video:")) {
                            id = string.split(":video:")[1].split("</guid>")[0];
                            title = true;
                        } else if(title && string.contains("title>")) {
                            String t = string.split("title>")[1].split("</title>")[0];
                            title = false;
                            videos.put(cleanupTitle(t), id);
                        }
                    }
                // PLAYLIST
                } else {
                    // Playlist title
                    if(line.contains("#playlist'/><title type='text'>")) {
                        stationTitle = line.split("#playlist'/><title type='text'>")[1].split("</title>")[0];
                    }
                    // Video title comes first
                    for(String string : line.split("(?=<)")) {
                        if(string.contains("title type='text'>")) {
                            video = string.split("title type='text'>")[1].split("</title>")[0];
                            title = true;
                        } else if(title && string.contains("link rel='alternate' type='text/html' href='")) {
                            id = string.split("\\?")[1].split("&amp;")[0].split("=")[1].replaceAll("=", "");
                            title = false;
                            if(!id.endsWith("'/>")) {
                                videos.put(cleanupTitle(video), id);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            YRadio.logError(ex.getMessage());
        }
        YRadio.logDone("Obtained all necessary information");
        YRadio.setStatus(type.toString().toLowerCase()+" loaded.");
    }
    
    private String cleanupTitle(String title) {
        return title.replaceAll("&amp;", "&");
    }
    
    public String getTitle() {
        return stationTitle;
    }
    
    public Streamer getStream() {
        return currentStream;
    }
    
    public String pause() {
        if(currentStream != null) {
            currentStream.pause();
            return currentStream.isPaused() ? "Play" : "Pause";
        } else {
            return "Pause";
        }
    }
    
    public static Station parseStation(String url) {
        if(url.contains("list=")) {
            String id = url.split("list=")[1].split("&")[0];
            return new Station(StationType.PLAYLIST, id);
        } else if(url.contains("/user/")) {
            String id = url.split("/user/")[1].split("\\?")[0];
            return new Station(StationType.CHANNEL, id);
        } else if(url.contains("\\?feature=mhee")) {
            String id = url.split("youtube.com/")[1].split("\\?")[0];
            return new Station(StationType.CHANNEL, id);
        } else { 
            String[] test = url.split("youtube.com/");
            if(test.length == 2 && test[1].split("\\?").length == 1 && test[1].split("&").length == 1) {
                String id = url.split("youtube.com/")[1];
                return new Station(StationType.CHANNEL, id);
            } else {
                return null;
            }
        }
    }
}
