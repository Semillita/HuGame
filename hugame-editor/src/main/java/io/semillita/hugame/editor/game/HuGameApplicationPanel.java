package io.semillita.hugame.editor.game;

import io.semillita.hugame.editor.Colors;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.Timer;

public class HuGameApplicationPanel extends JPanel {
  private final Canvas canvas;
  private ShowcaseApplication application;

  public HuGameApplicationPanel() {
    setBackground(Colors.GRADIENT_2);
    setBorder(BorderFactory.createLineBorder(Colors.GRADIENT_1, 2));
    setLayout(new BorderLayout(0, 0));
    setOpaque(true);

    this.canvas = new Canvas();
    add(canvas);
  }

  public void startApplication() {
    this.application = new ShowcaseApplication(canvas);
    var timer =
        new Timer(
            7,
            (e) -> {
              application.render();
            });
    timer.start();
  }

  @Override
  public void paintComponent(Graphics g) {
    System.out.println("HuGameApplicationPanel#paintComponent");
    if (application != null) {
      application.render();
    }

    // super.paintComponent(g);
  }
}
