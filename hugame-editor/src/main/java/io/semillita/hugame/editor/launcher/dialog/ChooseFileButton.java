package io.semillita.hugame.editor.launcher.dialog;

import io.semillita.hugame.editor.Colors;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.BorderFactory;
import javax.swing.JButton;

public class ChooseFileButton extends JButton {
  public ChooseFileButton(Runnable onClick) {
    setBackground(Colors.Launcher.MENU_BUTTON_BACKGROUND);
    setBorder(BorderFactory.createEmptyBorder());

    addMouseListener(
        new MouseAdapter() {
          @Override
          public void mouseEntered(MouseEvent e) {
            setBackground(Colors.Launcher.BACKGROUND_LIGHT);
            super.mouseEntered(e);
          }

          @Override
          public void mouseExited(MouseEvent e) {
            setBackground(Colors.Launcher.MENU_BUTTON_BACKGROUND);
            super.mouseExited(e);
          }
        });

    addActionListener(e -> onClick.run());
  }

  @Override
  public void paintComponent(Graphics g) {
    g.setColor(getBackground());
    g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);

    g.setColor(Colors.Launcher.SECONDARY_TEXT);

    int minX = 8, maxX = 32, sndX = 15, trdX = 20, minY = 10, maxY = 30, sndY = 14;

    g.drawLine(minX, minY, sndX, minY);
    g.drawLine(sndX, minY, trdX, sndY);
    g.drawLine(minX, sndY, maxX, sndY);

    g.drawLine(minX, minY, minX, maxY);
    g.drawLine(maxX, sndY, maxX, maxY);

    g.drawLine(minX, maxY, maxX, maxY);
  }
}
