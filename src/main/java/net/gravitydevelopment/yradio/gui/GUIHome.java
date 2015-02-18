package net.gravitydevelopment.yradio.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.ToolTipManager;
import net.gravitydevelopment.yradio.Settings;
import net.gravitydevelopment.yradio.Station;
import net.gravitydevelopment.yradio.YRadio;
import net.gravitydevelopment.yradio.audio.Quality;

public class GUIHome extends GUIBase implements ActionListener {
    
    public GUIHome() {
        super("YRadio", 400, 200, true);
    }
    
    private JPanel top;
    private JLabel status = new JLabel(YRadio.getStatus());
    private JButton playButton = new JButton("Play");
    private JButton pauseButton = new JButton("Pause");
    private JTextField stationTextField = new JTextField(Settings.getDefaultStation(), 20);
    private JLabel station = new JLabel("Station: None");
    private JLabel track = new JLabel("Track: None");
    // Menu
    private JMenuBar menuBar = new JMenuBar();
    private JMenu menu = new JMenu();
    // Menu items
    private JMenuItem settings = new JMenuItem();
    private JMenuItem console = new JMenuItem();
    

    @Override
    public void setup() {
        
        // BEGIN MENU BAR
        
        menu = new JMenu("Program");
        menu.getAccessibleContext().setAccessibleDescription("Control YRadio");
        System.out.println("Menu: " + menu);
        System.out.println("Bar: " + menuBar);
        menuBar.add(menu);

        settings = new JMenuItem("Settings");
        settings.getAccessibleContext().setAccessibleDescription("Edit radio settings");
        menu.add(settings);   
        settings.addActionListener(this);
        
        console = new JMenuItem("Console");
        console.getAccessibleContext().setAccessibleDescription("View the developer console");
        menu.add(console);  
        console.addActionListener(this);
        
        setJMenuBar(menuBar);
        
        // END MENU BAR
        
        // BEGIN TOP (INPUT)
        
        top = new JPanel();
        JLabel stationLabel = new JLabel("<html><b>Station</b></html>");
        top.add(stationLabel);
        
        ToolTipManager.sharedInstance().setDismissDelay(10000);
        stationTextField.setToolTipText("<html>You may enter a channel <i>(e.g. http://www.youtube.com/mrsuicidesheep)</i><br> or a playlist <i>(e.g. http://www.youtube.com/playlist?list=PL84C29E7EFBBCC874)</i></html>");
        top.add(stationTextField);
        
        playButton.addActionListener(this);
        top.add(playButton);
        
        add(top, BorderLayout.NORTH);
        
        // END TOP
        
        // BEGIN CENTER (PLAYER)
        
        JPanel center = new JPanel();
        center.setLayout(new GridBagLayout());
        GridBagConstraints cont = new GridBagConstraints();
        
        station.setAlignmentX(Component.CENTER_ALIGNMENT);
        center.add(station, cont);
        cont.gridy+=2;
        center.add(track, cont);
        cont.gridy+=2;
        pauseButton.addActionListener(this);
        center.add(pauseButton, cont);
        
        add(center, BorderLayout.CENTER);
        
        // END CENTER
        
        // BEGIN BOTTOM (STATUS)
        
        JPanel bottom = new JPanel();
        bottom.setLayout(new GridBagLayout());
        GridBagConstraints cont1 = new GridBagConstraints();
        
        cont1.fill = GridBagConstraints.HORIZONTAL;
        JSeparator sep = new JSeparator(SwingConstants.HORIZONTAL);
        bottom.add(sep, cont1);

        cont1.gridy+=2;
        cont1.anchor = GridBagConstraints.PAGE_END;
        bottom.add(status, cont1);
        
        add(bottom, BorderLayout.SOUTH);
        
        // END BOTTOM
        
        registerConsoleAction();
    }
    
    private void registerConsoleAction(){
        Action consoleAction = new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                YRadio.getConsole().setVisible(true);
            }
        };
        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        top.getActionMap().put("Open Console", consoleAction);
        top.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "Open Console");
    }
    
    public void setStatus(String s) {
        status.setText(s);
    }
    
    public void setTrack(String track) {
        this.track.setText("Track: "+track);
    }    
    
    public void setStation(String s) {
        stationTextField.setText(s);
    }
    
    public void actionPerformed(ActionEvent event) {
        if(event.getSource() == playButton) {
            Station station = Station.parseStation(stationTextField.getText());
            if(station != null) {
                if (Settings.getStation() != null) {
                    Settings.getStation().pause();
                }
                Settings.setStation(station);
                station.parse();
                this.station.setText("Station: "+station.getTitle());
                station.play();
            } else {
                JOptionPane.showMessageDialog(this, "<html>That url doesn't seem to link to a playlist or station..<br>Try another?</html>");
            }
        } else if(event.getSource() == pauseButton && Settings.getStation() != null) {
            pauseButton.setText(Settings.getStation().pause());
        } else if(event.getSource() == settings) {
            YRadio.getSettings().setVisible(true);
        } else if(event.getSource() == console) {
            YRadio.getConsole().setVisible(true);
        }
    }
    
}
