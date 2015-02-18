package net.gravitydevelopment.yradio.gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import net.gravitydevelopment.yradio.Settings;
import net.gravitydevelopment.yradio.YRadio;
import net.gravitydevelopment.yradio.audio.Quality;

public class GUISettings extends GUIBase implements ActionListener {
    
    private boolean startup = false;
    
    public GUISettings() {
        this(false);
    }
    
    public GUISettings(boolean startup) {
        this(startup, false);
    }
    
    public GUISettings(boolean startup, boolean show) {
        super("Settings", 400, 400, show && startup);
        
        this.startup = startup;
    }    
    
    private JTextField stationField;
    private JTextField workspaceField;
    private JTextField pythonField;
    private JComboBox qualityBox;
    private JButton saveButton;

    @Override
    public void setup() {
        
        stationField = new JTextField(Settings.getDefaultStation(), 20);
        workspaceField = new JTextField(Settings.getWorkspacePath(), 20);
        pythonField = new JTextField(Settings.getPythonPath(), 20);
        qualityBox = new JComboBox(Quality.values());
        qualityBox.setSelectedItem(Settings.getQuality());    
        saveButton = new JButton("Save"); 
        saveButton.addActionListener(this);
        
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints cont1 = new GridBagConstraints();
        
        Map<String, Component> map = new TreeMap<String, Component>();
        map.put("Default station:", stationField);
        map.put("Workspace:", workspaceField);
        map.put("Python path:", pythonField);
        map.put("Quality:", qualityBox);
        map.put("null", saveButton);
        
        cont1.fill = GridBagConstraints.HORIZONTAL;
        for(int i=0;i<map.size();i++) {
            cont1.gridx = 0;
            String currentString = map.keySet().toArray(new String[map.size()])[i];
            Component currentComp = map.values().toArray(new Component[map.size()])[i];
            
            if(!currentString.equals("null")) {
                panel.add(new JLabel(currentString), cont1);
            }
            cont1.gridx = 1;
            panel.add(currentComp, cont1);
            cont1.gridy+= i == 2 ? 4 : 2;
        }

        add(panel);
    }  
    
    public void actionPerformed(ActionEvent event) {
        if(event.getSource() == saveButton) {
            Settings.setDefaultStation(stationField.getText());
            Settings.setWorkspace(workspaceField.getText());
            Settings.setQuality((Quality)qualityBox.getSelectedItem());
            Settings.setPythonPath(pythonField.getText());
            Settings.writeSettings();
            
            if(!Settings.checkPython()) {
                JOptionPane.showMessageDialog(this,"<html>Your settings were saved, but we couldn't find python at the path you specified.<br>You need python for YRadio to work with youtube properly, so please ensure it exists before continuing.<br>If you don't have python installed, please download it from python.org</html>", "Specified python path not found", JOptionPane.WARNING_MESSAGE);
            } else if(startup) {
                Settings.checkWorkspace();
                setVisible(false);
                YRadio.startGUI();
            }
        }
    }
    
}
