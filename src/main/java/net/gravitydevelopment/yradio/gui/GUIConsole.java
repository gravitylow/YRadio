package net.gravitydevelopment.yradio.gui;

import java.awt.BorderLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class GUIConsole extends GUIBase {
    
    private String text = "";
    private JScrollPane scrollPane = new JScrollPane();
    private JTextArea console = new JTextArea(text);
    
    public GUIConsole() {
        super("Console", 1000, 600);
    }
    
    @Override
    public void setup() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        
        console = new JTextArea(text);
        console.setEditable(false);
        
        scrollPane = new JScrollPane(console);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        add(panel);
    }
    
    public void log(String message) {
        text+= message + "\n";
        console.setText(text);
    }
}
