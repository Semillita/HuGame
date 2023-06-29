package io.semillita.hugame.editor.launcher.dialog;

import io.semillita.hugame.editor.Colors;
import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JComponent;

public abstract class Dialog extends JComponent {
  private final DialogCloseButton closeButton;

  public Dialog(Runnable onClose) {
    this.closeButton = new DialogCloseButton(onClose);
    closeButton.setLocation(550, 20);
    closeButton.setSize(40, 40);
    add(closeButton);

    setLayout(null);
  }

  public void setX(int x) {
    setLocation(x, getY());
  }

  @Override
  protected void paintComponent(Graphics g) {
    g.setColor(Color.RED);

    int maxEndSlant = 620;
    int slantEnd = getWidth();
    int slantStart = slantEnd - 100;

    g.setColor(Colors.Launcher.BACKGROUND_LIGHT);
    g.fillPolygon(
        new int[] {0, slantEnd, slantStart, 0}, new int[] {0, 0, getHeight(), getHeight()}, 4);

    g.setColor(Colors.Launcher.OUTLINES);
    g.drawLine(slantEnd, 0, slantStart, getHeight());
  }
}
