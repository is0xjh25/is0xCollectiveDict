// is0xCollectiveDict
// COMP90015: Assignment1 - Multi-threaded Dictionary Server
// Developed By Yun-Chi Hsiao (1074004)
// GitHub: https://github.com/is0xjh25

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Footer extends JPanel {
    Footer() {
        this.setLayout(new BorderLayout());
        this.setPreferredSize(new Dimension(400, 50));
        this.setBackground(Color.BLACK);
        JLabel report = new JLabel("Find a problem? Tell us.", SwingConstants.LEFT);
        report.setPreferredSize(new Dimension(150, 50));
        report.setBorder(new EmptyBorder(0,25,0,0));
        report.setFont(new Font("Arial", Font.ITALIC, 10));
        report.setForeground (Color.GRAY);
        report.setCursor(new Cursor(Cursor.HAND_CURSOR));
        sendMail(report);
        JLabel copyright = new JLabel("Developed by is0xjh25©", SwingConstants.RIGHT);
        copyright.setPreferredSize(new Dimension(150, 50));
        copyright.setBorder(new EmptyBorder(0,0,0,25));
        copyright.setFont(new Font("Arial", Font.ITALIC, 10));
        copyright.setForeground (Color.GRAY);
        copyright.setBackground(Color.BLUE);
        copyright.setCursor(new Cursor(Cursor.HAND_CURSOR));
        goWebsite(copyright);
        this.add(report, BorderLayout.WEST);
        this.add(copyright, BorderLayout.EAST);
    }

    /* HELPER FUNCTIONS */
    private void goWebsite(JLabel website) {
        website.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().browse(new URI("https://is0xjh25.github.io"));
                } catch (URISyntaxException | IOException ex) {
                    System.out.println("[ERROR] -> " + ex.getMessage());
                }
            }
        });
    }

    private void sendMail(JLabel contact) {
        contact.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    Desktop.getDesktop().mail(new URI("mailto:is0.jimhsiao@gmail.com?subject=Problem%20With%20is0xCollectiveDict"));
                } catch (URISyntaxException | IOException ex) {
                    System.out.println("[ERROR] -> " + ex.getMessage());
                }
            }
        });
    }
}
