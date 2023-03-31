// is0xCollectiveDict
// COMP90015: Assignment1 - Multi-threaded Dictionary Server
// Developed By Yun-Chi Hsiao (1074004)
// GitHub: https://github.com/is0xjh25

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class PageManager extends JPanel {
    DictionaryClient dc;
    Header header;
    MainContent content;
    ButtonGroup buttons;
    
    PageManager(DictionaryClient dc) {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(400, 350));
        setDc(dc);
        setHeader(new Header(this));
        setContent(new MainContent(this));
        setButtons(new ButtonGroup(this));
        // merge panels.
        this.add(content, BorderLayout.CENTER);
        this.add(buttons, BorderLayout.SOUTH);
        this.add(header, BorderLayout.NORTH);
    }

    // called by buttons.
    public void pageControl(DictionaryClient.Page stage) {
        getDc().setCurrentPage(stage);
        getContent().resetContent();
        getButtons().showButtons();

        switch (stage) {
            case CONNECT -> setConnectionPage();
            case MENU -> setMenuPage();
            case UPDATE -> setUpdatePage();
            case ADD -> setAddPage();
            case SEARCH -> setSearchPage();
            case REMOVE -> setRemovePage();
            case STATUS -> setStatusPage();
            case WAITING -> setWaitingPage();
            case CONNECTING -> setConnectingPage();
            case RECONNECT -> setReconnectPage();
        }
    }

    private void reset() {
        getDc().reset();
        getHeader().getSearchBox().setText("");
        getContent().getMessageLabel().setText("");
        getContent().getDefinitionLabel().setText("");
        getContent().getSuggestionLabel().setText("");
        getButtons().showButtons();
    }
    
    /* PAGES */
    private void setConnectionPage() {
        getHeader().getTitle().setText("<IP ADDRESS:PORT>");
        getHeader().getSearchBox().setText(dc.getIpAndPort());
        getHeader().getSearchBox().setEnabled(true);
        getContent().getConnectionGIF().setVisible(true);
    }

    private void setMenuPage() {
        reset();
        getHeader().getTitle().setText("~ DISCOVER THE MEANING ~");
        getHeader().getSearchBox().setEnabled(true);
        getContent().getMenuGIF().setVisible(true);
    }

    private void setUpdatePage() {
        getHeader().getTitle().setText("~ RE-DEFINE ~");
        getHeader().getSearchBox().setEnabled(false);
        getContent().getDefinitionLabel().setText(getDc().getDefinition());
        getContent().getDefinitionLabel().setEditable(true);
        getContent().getScrollDefinitionLabel().setVisible(true);
    }

    private void setAddPage() {
        getHeader().getTitle().setText("~ NEW WORD ~");
        getHeader().getSearchBox().setEnabled(false);
        getContent().getDefinitionLabel().setEditable(true);
        getContent().getScrollDefinitionLabel().setVisible(true);
    }

    private void setSearchPage() {
        getHeader().getTitle().setText("~ RESULT ~");
        getHeader().getSearchBox().setEnabled(false);
        getContent().getDefinitionLabel().setEditable(false);
        getContent().getScrollDefinitionLabel().setVisible(true);
    }

    private void setRemovePage() {
        getHeader().getTitle().setText("X DELETE X");
        getHeader().getSearchBox().setEnabled(false);
        getContent().getDefinitionLabel().setText(getDc().getDefinition());
        getContent().getDefinitionLabel().setEditable(false);
        getContent().getScrollDefinitionLabel().setVisible(true);
    }

    private void setStatusPage() {
        getHeader().getTitle().setText("~ QUERY STATUS ~");
        getHeader().getSearchBox().setText(getDc().getAction() + ": " + getDc().getWord());
        getHeader().getSearchBox().setEnabled(false);
        getContent().getMessageLabel().setVisible(true);
        getContent().getSuggestionLabel().setText(getDc().getSuggestion());
        getContent().getSuggestionLabel().setVisible(true);
    }

    private void setWaitingPage() {
        String s = getDc().getAction().toString();
        s = s.charAt(s.length()-1) == 'e' ? s.substring(0, s.length() - 1) : s;
        getHeader().getTitle().setText("... " + s + "ING" + " ...");
        getHeader().getSearchBox().setText(getDc().getWord());
        getHeader().getSearchBox().setEnabled(false);
        getContent().getLoadingGIF().setVisible(true);
    }

    private void setConnectingPage() {
        getHeader().getTitle().setText("... CONNECTING ...");
        getHeader().getSearchBox().setText(getDc().getIpAndPort());
        getHeader().getSearchBox().setEnabled(false);
        getContent().getLoadingGIF().setVisible(true);
    }

    private void setReconnectPage() {
        getHeader().getTitle().setText("X NO CONNECTION X");
        getHeader().getSearchBox().setText(getDc().getIpAndPort());
        getHeader().getSearchBox().setEnabled(false);
        getContent().getMessageLabel().setVisible(true);
        getContent().getSuggestionLabel().setVisible(true);
        getButtons().showWarning("Try another IP & Port");
        ActionListener taskPerformer = evt -> {
            getHeader().getTitle().setText("<IP ADDRESS:PORT>");
            getHeader().getSearchBox().setText(dc.getIpAndPort());
            getHeader().getSearchBox().setEnabled(true);
            getContent().getConnectionGIF().setVisible(true);
            getButtons().showButtons();
        };
        setTimer(3000, taskPerformer);
    }

    /* GETTERS & SETTERS */
    public DictionaryClient getDc() {
        return dc;
    }
    public void setDc(DictionaryClient dc) {
        this.dc = dc;
    }
    public Header getHeader() {
        return header;
    }
    public void setHeader(Header header) {
        this.header = header;
    }
    public MainContent getContent() {
        return content;
    }
    public void setContent(MainContent content) {
        this.content = content;
    }
    public ButtonGroup getButtons() {
        return buttons;
    }
    public void setButtons(ButtonGroup buttons) {
        this.buttons = buttons;
    }
    
    /* HELPER FUNCTIONS */
    public void setTimer(int time, ActionListener a) {
        Timer timer = new Timer(time, a);
        timer.setRepeats(false);
        timer.start();
    }
}
