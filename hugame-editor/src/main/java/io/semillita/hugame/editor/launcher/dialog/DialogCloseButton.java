package io.semillita.hugame.editor.launcher.dialog;

import io.semillita.hugame.editor.Colors;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;

public class DialogCloseButton extends JButton {
  private final Runnable onClick;

  public DialogCloseButton(Runnable onClick) {
    this.onClick = onClick;
    setOpaque(false);
    setBackground(new Color(0, 0, 0, 0));
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
            setBackground(Colors.TRANSPARENT);
            super.mouseEntered(e);
          }
        });

    addActionListener(e -> onClick.run());
  }

  @Override
  public void paint(Graphics g) {
    super.paint(g);
    g.setColor(Colors.Launcher.PRIMARY_TEXT);
    g.drawLine(10, 10, getWidth() - 10, getHeight() - 10);
    g.drawLine(10, getHeight() - 10, getWidth() - 10, 10);
  }
}
