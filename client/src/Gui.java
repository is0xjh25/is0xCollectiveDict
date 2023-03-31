// is0xCollectiveDict
// COMP90015: Assignment1 - Multi-threaded Dictionary Server
// Developed By Yun-Chi Hsiao (1074004)
// GitHub: https://github.com/is0xjh25

import javax.swing.*;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.*;

public class Gui extends JPanel {
    private DictionaryClient dc;
    private JFrame frame;
    private PageManager pm;
    private JPanel footer;

    public Gui(DictionaryClient dc) {
        setDc(dc);
    }

    public void run() {
        System.out.println("[Activated CollectiveDict]");
        getDc().openFile();
        setUIFont(new FontUIResource(new Font("Monospaced", Font.BOLD, 14))); // set the overall font.
        setFrame(new JFrame("CollectiveDict"));
        getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPm(new PageManager(getDc()));
        setFooter(new Footer());
        getFrame().add(getPm(), BorderLayout.NORTH);
        getFrame().add(getFooter(), BorderLayout.SOUTH);
        getFrame().pack();
        getFrame().setVisible(true); // making the frame visible.
        getFrame().setResizable(false);
        getFrame().setLocationRelativeTo(null); // set the window in the center of the screen.
        getFrame().setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        getFrame().setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        getFrame().addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                if (JOptionPane.showConfirmDialog(getFrame(),
                        "Are you sure you want to close CollectiveDict?", "Close APP?",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION){
                    getDc().closeFile();
                    System.out.println("[Terminate CollectiveDict]");
                    System.exit(0);
                }
            }
        });
        getPm().pageControl(getDc().getCurrentPage());
    }

    /* GETTERS & SETTERS */
    public DictionaryClient getDc() {
        return dc;
    }
    public void setDc(DictionaryClient dc) {
        this.dc = dc;
    }
    public JFrame getFrame() {
        return frame;
    }
    public void setFrame(JFrame frame) {
        this.frame = frame;
    }
    public PageManager getPm() {
        return pm;
    }
    public void setPm(PageManager pm) {
        this.pm = pm;
    }
    public JPanel getFooter() {
        return footer;
    }
    public void setFooter(JPanel footer) {
        this.footer = footer;
    }

    private void setUIFont(FontUIResource f) {
        Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                FontUIResource orig = (FontUIResource) value;
                Font font = new Font(f.getFontName(), orig.getStyle(), f.getSize());
                UIManager.put(key, new FontUIResource(font));
            }
        }
    }
}
