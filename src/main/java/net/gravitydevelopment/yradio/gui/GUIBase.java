package net.gravitydevelopment.yradio.gui;

import java.awt.BorderLayout;
import javax.swing.*;

public class GUIBase extends JFrame {
    
    public GUIBase() {
        this(400, 200);
    }
    
    public GUIBase(int x, int y) {
        this("YRadio", x, y);
    }
    
    public GUIBase(final String name, int x, int y) {
        this(name, x, y, false);
    }
    
    public GUIBase(final String name, final int x, final int y, final boolean main) {
        super(name);
        
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                setSize(x, y);
                setLayout(new BorderLayout());
                setDefaultCloseOperation("YRadio".equalsIgnoreCase(name) ? WindowConstants.EXIT_ON_CLOSE : WindowConstants.DISPOSE_ON_CLOSE);
                setup();
                setVisible(main);
            }
        });        
    }
    
    public void setup() {

    }
}
