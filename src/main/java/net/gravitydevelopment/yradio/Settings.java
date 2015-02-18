package net.gravitydevelopment.yradio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.gravitydevelopment.yradio.audio.Quality;

public class Settings {
    
    // START CONSTANTS
    private static final String PLAYLIST_FEED = "http://gdata.youtube.com/feeds/api/playlists/";
    private static final String CHANNEL_FEED_1 = "http://gdata.youtube.com/feeds/base/users/";
    private static final String CHANNEL_FEED_2 = "/uploads?alt=rss&v=2&orderby=published&client=ytapi-youtube-profile";
    private static final String VIDEO_URL = "http://www.youtube.com/watch?v=";
    private static final String DOWNLOAD_FOLDER = "dl";
    private static final String AUDIO_FOLDER = "audio";
    private static final String SETTINGS_FILE = "yradio.properties";
    private static final String YOUTUBEDL_FILE = "youtube-dl";
    private static final String LINE_SEP = System.getProperty("line.separator");
    // END CONSTANTS
    
    // START CONFIGS
    private static boolean gui = true;
    private static boolean quiet = false;
    private static Quality quality = Quality.p480;
    private static File workspace = new File(".yradio");
    private static File youtubedl = new File("/usr/bin/youtube-dl");
    private static String pythonPath = "python";
    private static String defaultStation = "";
    // END CONFIGS
    
    private static Station station = null;
    
    public static void setGUI(boolean b) {
        gui = b;
    }
    
    public static void setQuiet(boolean b) {
        quiet = b;
    }
    
    public static boolean useGUI() {
        return gui;
    }
    
    public static boolean isQuiet() {
        return quiet;
    }
    
    public static void setStation(Station s) {
        station = s;
    }
    
    public static Station getStation() {
        return station;
    }
    
    public static String getWorkspacePath() {
        return workspace.getAbsolutePath();
    }
    
    public static File getWorkspaceFile() {
        return workspace;
    }
    
    public static void setWorkspace(String s) {
        setWorkspace(new File(s));
    }
    
    public static void setWorkspace(File f) {
        workspace = f;
        checkWorkspace();
    }   
    
    public static void setYoutubeDL(String s) {
        setYoutubeDL(new File(s));
    }    
    
    public static void setYoutubeDL(File f) {
        youtubedl = f;
    }    
    
    public static String getDefaultStation() {
        return defaultStation;
    }
    
    public static void setDefaultStation(String s) {
        defaultStation = s;
        YRadio.updateDefaultStation(s);
    }    
    
    public static void setQuality(String string) {
        Quality q = safeValueOf(string);
        if(q != null) {
            quality = q;
        } else if ((q = safeValueOf("p" + string)) != null) {
            quality = q;
        }
    }
    
    public static void setQuality(Quality q) {
        quality = q;
    }
    
    public static void setPythonPath(String string) {
        pythonPath = string;
    }
    
    public static String getPythonPath() {
        return pythonPath;
    }
    
    private static Quality safeValueOf(String string) {
        try {
            return Quality.valueOf(string);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
    
    public static Quality getQuality() {
        return quality;
    }
    
    public static String getPlaylistFeed(String id) {
        return PLAYLIST_FEED + id;
    }
    
    public static String getChannelFeed(String username) {
        return CHANNEL_FEED_1 + username + CHANNEL_FEED_2;
    }
    
    public static String getVideoURL(String id) {
        return VIDEO_URL + id;
    }
    
    public static String getDownloadPath() {
        return getWorkspacePath() + File.separator + DOWNLOAD_FOLDER;
    }
    
    public static File getDownloadFile() {
        return new File(getDownloadPath());
    }
    
    public static String getAudioPath() {
        return getWorkspacePath() + File.separator + AUDIO_FOLDER;
    }
    
    public static File getAudioFile() {
        return new File(getAudioPath());
    }  
    
    public static String getSettingsPath() {
        return getWorkspacePath() + File.separator + SETTINGS_FILE;
    } 
    
    public static File getSettingsFile() {
        return new File(getSettingsPath());
    }
    
    public static String getYoutubedlPath() {
        return getWorkspacePath() + File.separator + YOUTUBEDL_FILE;
    } 
    
    public static String getYoutubedlRunnablePath() {
        return pythonPath + " " + getYoutubedlPath();
    }
    
    public static File getYoutubedlFile() {
        return new File(getYoutubedlPath());
    }    
    
    public static boolean isFirstRun() {
        return !getWorkspaceFile().exists();
    }
    
    public static boolean checkYoutubeDL() {
        // Test for youtube-dl
        if(!getYoutubedlFile().exists()) {
            new Resource("youtube-dl").saveTo(getYoutubedlFile());
            return false;
        } else {
            return true;
        }
    }
    
    public static boolean checkPython() {
        // Test for python
        boolean exists = false;
        try {
            Process proc = Runtime.getRuntime().exec(pythonPath + " --version");
            exists = true;             
        } catch (IOException ex) {
        }
        return exists;
    }
    
    public static void checkWorkspace() {
        YRadio.logProgress("Verifying integrity of workspace... ("+workspace.getAbsolutePath()+")");
        String [] folders = new String []{getWorkspacePath(), getDownloadPath(), getAudioPath()};
        for(String s : folders) {
            if(!new File(s).exists()) {
                YRadio.logProgress("Created folder \"" + s + "\" because it did not exist");
                new File(s).mkdir();
            }
        }
        
        if(!getSettingsFile().exists()) {
            try {
                getSettingsFile().createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        // Test for youtube-dl
        YRadio.logProgress(checkYoutubeDL() ? "Verified youtube-dl" : "Youtube-dl not found. Youtube-dl saved to workspace.");
        
        // Test for python
        YRadio.logProgress(checkPython() ? "Verified python usage" : "Python not found. Program will not download from youtube.");
    }
    
    public static void writeSettings() {
        String [] lines = new String[] {
            "gui:"+gui+LINE_SEP,
            "station:"+defaultStation+LINE_SEP,
            "workspace:"+workspace.getAbsolutePath()+LINE_SEP,
            "youtube-dl:"+youtubedl.getAbsolutePath()+LINE_SEP,
            "quality:"+quality.getValue()+LINE_SEP,
        };
        Writer output;
        try {
            output = new BufferedWriter(new FileWriter(getSettingsFile()));
            try {
                for(String string : lines) {
                    output.write(string);
                }
            } finally {
                output.close();
            }   
        } catch (IOException ex) {
            Logger.getLogger(Settings.class.getName()).log(Level.SEVERE, null, ex);
        }    
    }
    
    public static void readSettings() {
        try {
            BufferedReader input =  new BufferedReader(new FileReader(getSettingsFile()));
            try {
                String line = null;
                while (( line = input.readLine()) != null){
                    if(line.startsWith("gui:")) {
                        boolean n = Boolean.parseBoolean(line.split("gui:")[1]);
                        // If we've set it to false manually, keep it that way
                        if(n == true && gui == false) {
                            // ignore
                        } else {
                            gui = n;
                        }
                    } else if(line.startsWith("station:")) {
                        if(line.split("station:").length > 1) {
                            defaultStation = line.split("station:")[1];
                        }
                    } else if(line.startsWith("workspace:")) {
                        workspace = new File(line.split("workspace:")[1]);
                    } else if(line.startsWith("youtube-dl:")) {
                        youtubedl = new File(line.split("youtube-dl:")[1]);
                    } else if(line.startsWith("quality:")) {
                        quality = Quality.fromValue(Integer.parseInt(line.split("quality:")[1]));
                    }
                }
            } finally {
                input.close();
                checkWorkspace();
            }
        } catch (IOException ex){
            YRadio.logError(ex.getMessage());
        }       
    }    
    
}
