package net.gravitydevelopment.yradio;

import it.sauronsoftware.jave.AudioAttributes;
import it.sauronsoftware.jave.Encoder;
import it.sauronsoftware.jave.EncoderException;
import it.sauronsoftware.jave.EncodingAttributes;
import it.sauronsoftware.jave.InputFormatException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javazoom.jl.decoder.JavaLayerException;
import net.gravitydevelopment.yradio.audio.PausablePlayer;
import net.gravitydevelopment.yradio.audio.Quality;

public class Streamer {
    
    private String url;
    private String id;
    private String title;
    private File target;
    private static boolean downloaded = false;
    private PausablePlayer player = null;
    private String video = null;
    private Quality quality = Settings.getQuality();
    private final Station parent;
    
    public Streamer(String id, String title, Station station) {
        this.id = id;
        this.url = Settings.getVideoURL(id);
        this.title = title;
        this.parent = station;
        YRadio.logDone("Streamer for " + title + " created");
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setDownloadedTitle(String t) {
        video = t;
    }

    public void download() {
        download(Settings.getQuality());
    }
    
    public void download(Quality quality) {
        this.quality = quality;
        target = getAudioFile();
        
        YRadio.logProgress("Trying to download file from: " + url);
        YRadio.setStatus("Looking for the next song...");
        
        if(audioExists()) {
            YRadio.setStatus("Prepared "+title);
            downloaded = true;
        } else {
            YRadio.logProgress("Downloading: " + title);
            YRadio.setStatus("Downloading " + title + "...");            
            try {
                YRadio.log("Attempting to run "+(Settings.getYoutubedlRunnablePath() + " -f " + quality.getValue() + " -o " + Settings.getDownloadPath() + File.separator + id + ".mp4 " + url + " --restrict-filenames"));
                Process p = Runtime.getRuntime().exec(Settings.getYoutubedlRunnablePath() + " -f " + quality.getValue() + " -o " + Settings.getDownloadPath() + File.separator + id + ".mp4 " + url + " --restrict-filenames");

                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while((line = bre.readLine()) != null) {
                    System.out.println("ERR: " + line);
                    if(line.contains("format not available")) {
                        Quality next = quality.getNextLowestQuality();
                        String s = "The request format (" + quality + ") wasn't available.";
                        s += next != null ? " Trying " + next + " instead." : " Skipping this song.";
                        YRadio.logError(s);
                        if (next != null) {
                            download(next);
                        } else {
                            parent.notifyFinishedPlay(this); // Skip
                        }
                        return;
                    }
                }
                while((line = br.readLine()) != null) {
                    System.out.println("PYTHON: " + line);
                    if(line.contains("Destination: ")) {
                        setDownloadedTitle(line.split("Destination: ")[1]);
                    }
                }
                try {
                    p.waitFor();
                } catch (InterruptedException ex) {
                    Logger.getLogger(Streamer.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (IOException ex) {
                Logger.getLogger(Streamer.class.getName()).log(Level.SEVERE, null, ex);
            }

            convert();
        }
    }        
    
    private boolean audioExists() {
        return getAudioFile().exists();
    }    
    
    private File getAudioFile() {
        return new File(Settings.getAudioFile() + File.separator + title + ".mp3");
    } 
    
    private void convert() {
        YRadio.logProgress("Extracting audio from " + title);
        File source = new File(video);
        
        YRadio.logProgress("Looking for video source at "+source.getAbsolutePath());
        YRadio.logProgress("Attempting to export to "+target.getAbsolutePath());
        YRadio.setStatus("Converting " + title + "...");
        
        AudioAttributes audio = new AudioAttributes();
        audio.setCodec("libmp3lame");
        audio.setBitRate(new Integer(128000));
        audio.setChannels(new Integer(2));
        audio.setSamplingRate(new Integer(44100));
        EncodingAttributes attrs = new EncodingAttributes();
        attrs.setFormat("mp3");
        attrs.setAudioAttributes(audio);
        
        Encoder encoder = new Encoder();
        
        try { 
            encoder.encode(source, target, attrs);
        } catch (IllegalArgumentException ex) {
            YRadio.logError(ex.getMessage());
        } catch (InputFormatException ex) {
            YRadio.logError(ex.getMessage());
        } catch (EncoderException ex) {
            YRadio.logError(ex.getMessage());
        }
        
        source.delete();
        
        downloaded = true;
        
        YRadio.setStatus("Prepared next song");
    }
    
    public void play() {
        if(player == null || player.isComplete() || player.isPaused()) {
            YRadio.logDone("Playing "+title);
            YRadio.setStatus("Playing "+title);
            try {
                player = new PausablePlayer(new FileInputStream(target));
                player.play();
            } catch (FileNotFoundException ex) {
                YRadio.logError(ex.getMessage());
            } catch (JavaLayerException ex) {
                YRadio.logError(ex.getMessage());
            }
        }
    }
    
    public void pause() {
        if(player.isPaused()) {
            YRadio.setStatus("Playing");
            player.resume();
        } else {
            YRadio.setStatus("Paused");
            player.pause();       
        }
    }
    
    public void stop() {
        YRadio.setTitle("None");
        YRadio.setStatus("Stopped");
        player.stop();
    }
    
    public boolean isPaused() {
        return player.isPaused();
    }
    
    public boolean isFinished() {
        return player.isComplete();
    }

    public Quality getQuality() {
        return quality;
    }
    
    public boolean isDownloaded() {
        return downloaded;
    }
}
