// is0xCollectiveDict
// COMP90015: Assignment1 - Multi-threaded Dictionary Server
// Developed By Yun-Chi Hsiao (1074004)
// GitHub: https://github.com/is0xjh25

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;

public class MainContent extends JPanel {
    private static final String MENU_GIF_URL = "/resources/menu.gif";
    private static final String LOADING_GIF_URL = "/resources/loading.gif";
    private static final String CONNECTION_GIF_URL = "/resources/connection.gif";

    private PageManager pm;
    private JLabel connectionGIF;
    private JLabel menuGIF;
    private JLabel loadingGIF;
    private JLabel messageLabel;
    private JLabel suggestionLabel;
    private JTextArea definitionLabel;
    private JScrollPane scrollDefinitionLabel;

    MainContent(PageManager pm) {
        this.setPreferredSize(new Dimension(400, 250));
        this.setBackground(Color.BLACK);
        setPm(pm);
        setConnectionGIF(new JLabel(scaleImageIcon(getClass().getResource(CONNECTION_GIF_URL), 400, 400)));
        getConnectionGIF().setPreferredSize(new Dimension(400, 250));
        setMenuGIF(new JLabel(scaleImageIcon(getClass().getResource(MENU_GIF_URL), 250, 250)));
        getMenuGIF().setPreferredSize(new Dimension(400, 250));
        setLoadingGIF(new JLabel(scaleImageIcon(getClass().getResource(LOADING_GIF_URL), 400, 400)));
        getLoadingGIF().setPreferredSize(new Dimension(400, 250));
        setMessageLabel(new JLabel());
        getMessageLabel().setPreferredSize(new Dimension(400, 125));
        getMessageLabel().setHorizontalAlignment(SwingConstants.CENTER);
        getMessageLabel().setVerticalAlignment(SwingConstants.BOTTOM);
        setSuggestionLabel(new JLabel());
        getSuggestionLabel().setPreferredSize(new Dimension(400, 125));
        getSuggestionLabel().setHorizontalAlignment(SwingConstants.CENTER);
        getSuggestionLabel().setVerticalAlignment(SwingConstants.CENTER);
        getSuggestionLabel().setForeground(Color.GRAY);
        setDefinitionLabel(new JTextArea());
        getDefinitionLabel().setLineWrap(true);
        getDefinitionLabel().setForeground(Color.WHITE);
        getDefinitionLabel().setBackground(Color.DARK_GRAY);
        setScrollDefinitionLabel(new JScrollPane(getDefinitionLabel()));
        getScrollDefinitionLabel().setPreferredSize(new Dimension(250, 225));
        // immediately update the string
        getDefinitionLabel().addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                getPm().getDc().setDefinition(((JTextArea) e.getSource()).getText());
                getPm().getButtons().showButtons() ;
            }
        });
        this.add(getConnectionGIF());
        this.add(getMenuGIF());
        this.add(getLoadingGIF());
        this.add(getMessageLabel());
        this.add(getSuggestionLabel());
        this.add(getScrollDefinitionLabel());
    }

    void resetContent() {
        getConnectionGIF().setVisible(false);
        getMenuGIF().setVisible(false);
        getLoadingGIF().setVisible(false);
        getScrollDefinitionLabel().setVisible(false);
        getMessageLabel().setVisible(false);
        getSuggestionLabel().setVisible(false);
    }

    public void setErrorMessageLabel(String msg) {
        getPm().getDc().setMessage(msg);
        getMessageLabel().setText(msg);
        getMessageLabel().setForeground(Color.RED);
    }

    public void setSuccessMessageLabel(String msg) {
        getPm().getDc().setMessage(msg);
        getMessageLabel().setText(msg);
        getMessageLabel().setForeground(Color.GREEN);
    }

    public void setDefinitionLabel(String msg) {
        getPm().getDc().setDefinition(msg);
        getDefinitionLabel().setText(msg);
    }

    public void setSuggestionLabel(String msg) {
        getPm().getDc().setSuggestion(msg);
        getSuggestionLabel().setText(msg);
    }

    /* GETTERS & SETTERS */
    public PageManager getPm() {
        return pm;
    }
    public void setPm(PageManager pm) {
        this.pm = pm;
    }
    public JLabel getConnectionGIF() {
        return connectionGIF;
    }
    public void setConnectionGIF(JLabel connectionGIF) {
        this.connectionGIF = connectionGIF;
    }
    public JLabel getMenuGIF() {
        return menuGIF;
    }
    public void setMenuGIF(JLabel menuGIF) {
        this.menuGIF = menuGIF;
    }
    public JLabel getLoadingGIF() {
        return loadingGIF;
    }
    public void setLoadingGIF(JLabel loadingGIF) {
        this.loadingGIF = loadingGIF;
    }
    public JLabel getMessageLabel() {
        return messageLabel;
    }
    public void setMessageLabel(JLabel messageLabel) {
        this.messageLabel = messageLabel;
    }
    public JLabel getSuggestionLabel() {
        return suggestionLabel;
    }
    public void setSuggestionLabel(JLabel suggestionLabel) {
        this.suggestionLabel = suggestionLabel;
    }
    public JTextArea getDefinitionLabel() {
        return definitionLabel;
    }
    public void setDefinitionLabel(JTextArea definitionLabel) {
        this.definitionLabel = definitionLabel;
    }
    public JScrollPane getScrollDefinitionLabel() {
        return scrollDefinitionLabel;
    }
    public void setScrollDefinitionLabel(JScrollPane scrollDefinitionLabel) {
        this.scrollDefinitionLabel = scrollDefinitionLabel;
    }

    /* HELPER FUNCTIONS */
    private ImageIcon scaleImageIcon(URL url, int w, int h) {
        ImageIcon imageIcon = new ImageIcon(url);
        Image image = imageIcon.getImage();
        Image newImage = image.getScaledInstance(w, h, java.awt.Image.SCALE_DEFAULT);
        return new ImageIcon(newImage);
    }
}
