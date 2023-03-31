// is0xCollectiveDict
// COMP90015: Assignment1 - Multi-threaded Dictionary Server
// Developed By Yun-Chi Hsiao (1074004)
// GitHub: https://github.com/is0xjh25

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class Header extends JPanel {
    private PageManager pm;
    private JLabel title;
    private JTextField searchBox;
    private JScrollPane scrollSearchBox;

    Header(PageManager pm) {
        this.setPreferredSize(new Dimension(400, 75));
        this.setBackground(Color.BLACK);
        setPm(pm);
        setTitle(new JLabel("", SwingConstants.CENTER));
        getTitle().setPreferredSize(new Dimension(200, 25));
        getTitle().setBorder(new EmptyBorder(15,0,0,0));
        getTitle().setForeground(Color.WHITE);
        setSearchBox(new JTextField());
        getSearchBox().setHorizontalAlignment(JTextField.CENTER);
        getSearchBox().setForeground(Color.WHITE);
        getSearchBox().setBackground(Color.BLACK);
        getSearchBox().setBorder(BorderFactory.createLineBorder(Color.GRAY));
        setScrollSearchBox(new JScrollPane(searchBox,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED));
        getScrollSearchBox().setPreferredSize(new Dimension(200, 30));
        getScrollSearchBox().getHorizontalScrollBar().setPreferredSize(new Dimension(0, 3));
        // immediately update the string.
        getSearchBox().addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                if (getPm().getDc().getCurrentPage() == DictionaryClient.Page.CONNECT || getPm().getDc().getCurrentPage() == DictionaryClient.Page.RECONNECT) {
                    getPm().getDc().setIpAndPort(((JTextField) e.getSource()).getText());
                    getPm().getButtons().showButtons();
                } else {
                    getPm().getDc().setWord(((JTextField) e.getSource()).getText());
                    getPm().getButtons().showButtons() ;
                }
            }
        });
        this.add(getTitle(), BorderLayout.NORTH);
        this.add(getScrollSearchBox(), BorderLayout.SOUTH);
    }

    /* GETTERS & SETTERS */
    public PageManager getPm() {
        return pm;
    }
    public void setPm(PageManager pm) {
        this.pm = pm;
    }
    public JLabel getTitle() {
        return title;
    }
    public void setTitle(JLabel title) {
        this.title = title;
    }
    public JTextField getSearchBox() {
        return searchBox;
    }
    public void setSearchBox(JTextField searchBox) {
        this.searchBox = searchBox;
    }
    public JScrollPane getScrollSearchBox() {
        return scrollSearchBox;
    }
    public void setScrollSearchBox(JScrollPane scrollSearchBox) {
        this.scrollSearchBox = scrollSearchBox;
    }
}
