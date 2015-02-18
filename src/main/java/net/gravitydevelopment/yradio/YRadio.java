package net.gravitydevelopment.yradio;

import java.util.logging.Level;
import java.util.logging.Logger;
import net.gravitydevelopment.yradio.Station.StationType;
import net.gravitydevelopment.yradio.gui.GUIConsole;
import net.gravitydevelopment.yradio.gui.GUIHome;
import net.gravitydevelopment.yradio.gui.GUISettings;

public class YRadio {
    
    private static final Logger LOGGER = Logger.getLogger(YRadio.class.getName());
    private static GUIHome home = null;
    private static GUISettings settings;
    private static GUIConsole console = new GUIConsole();
    private static String status = "Idle";
    private static String title = "None";

    public static void main(String[] args) {
       
        for(String string : args) {
            if(string.equalsIgnoreCase("-nogui")) {
                Settings.setGUI(false);
            } else if(string.equalsIgnoreCase("-quiet")) {
                Settings.setQuiet(true);                
            } else if(string.startsWith("-workspace=")) {
                Settings.setWorkspace(string.replaceAll("-workspace=", ""));
            } else if(string.startsWith("-quality=")) {
                Settings.setQuality(string.replaceAll("-quality=", ""));
            } else if(string.startsWith("-playlist=")) {
                Settings.setStation(new Station(StationType.PLAYLIST, string.replaceAll("-playlist=", "")));
            } else if(string.startsWith("-channel=")) {
                Settings.setStation(new Station(StationType.CHANNEL, string.replaceAll("-channel=", "")));
            } else if(string.startsWith("-station=")) {
                String s = string.replaceAll("-station=", "");
                Station station = Station.parseStation(s);
                if(station != null) {
                    Settings.setStation(station);
                } else {
                    logError("Couldn't parse url to a station type: "+s);
                    if(!Settings.useGUI()) {
                        // They have to restart the process
                        return;
                    }
                }
            } else if (string.startsWith("-help")) {
                System.out.println("YRadio options:");
                System.out.println("-help");
                System.out.println("  Access this page");
                System.out.println("-nogui");
                System.out.println("  Starts YRadio without a GUI");
                System.out.println("-quiet");
                System.out.println("  Starts YRadio without console logging");
                System.out.println("-workspace=<path>");
                System.out.println("  Starts YRadio with a specific workspace");
                System.out.println("-quality=<path>");
                System.out.println("  Starts YRadio with a specific preferred quality");
                System.out.println("  Valid qualities: '480', '720', '1080'");
                System.out.println("-playlist=<id>");
                System.out.println("  Starts YRadio with a specific playlist to stream");
                System.out.println("-channel=<id>");
                System.out.println("  Starts YRadio with a specific channel to stream");
                System.out.println("-station=<id>");
                System.out.println("  Starts YRadio with a specific station to stream");
                System.out.println("  A station may be either a channel or playlist url");
                return;
            }
        }
        
        if(Settings.useGUI()) {
            console = new GUIConsole();
        }
        
        if(Settings.isFirstRun()) {
            if(!Settings.useGUI()) {
                Settings.checkWorkspace();
                
                // Start playing (threaded)
                if(Settings.getStation() != null) {
                    new Thread(new StationRunnable(Settings.getStation())).start();
                } else {
                    logError("No station passed.");
                }
            }
        } else {
            Settings.readSettings();
            if(Settings.useGUI()) {
                startGUI();
            } else {
                // Start playing (threaded)
                if(Settings.getStation() != null) {
                    new Thread(new StationRunnable(Settings.getStation())).start();
                } else {
                    logError("No station passed.");
                }                
            }
        }
        settings = new GUISettings(Settings.isFirstRun(), Settings.useGUI());
    }
    
    public static void resetSettingsGUI() {
        settings = new GUISettings();
    }
    
    public static void updateDefaultStation(String s) {
        if(home != null) {
            home.setStation(s);
        }
    }
    
    public static void startGUI() {
        if(Settings.useGUI()) {
            home = new GUIHome();
        }        
    }
    
    public static void log(String message) {
        if(!Settings.isQuiet()) {
            LOGGER.log(Level.INFO, message);
        }
        if(Settings.useGUI()) {
            console.log(message);
        }
    }
    
    public static void logProgress(String message) {
        log("[...] " + message);
    }
    
    public static void logDone(String message) {
        log("[OK] " + message);
    }  
    
    public static void logError(String message) {
        log("[ERROR] " + message);
    }      
    
    public static void setStatus(String message) {
        status = message;
        if(home != null) {
            home.setStatus(message);
        }
    }
    
    public static void setTitle(String t) {
        title = t;
        if(home != null) {
            home.setTrack(t);
        }
    }
    
    public static String getStatus() {
        return status;
    }
    
    public static GUIConsole getConsole() {
        return console;
    }
    
    public static GUISettings getSettings() {
        return settings;
    }    
}
