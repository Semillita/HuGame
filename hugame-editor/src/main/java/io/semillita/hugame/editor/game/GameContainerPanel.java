package io.semillita.hugame.editor.game;

import io.semillita.hugame.editor.ContentPanel;
import java.awt.BorderLayout;
import javax.swing.BorderFactory;

public class GameContainerPanel extends ContentPanel {
  private final HuGameApplicationPanel applicationPanel;

  public GameContainerPanel() {
    setLayout(new BorderLayout(0, 0));
    // setBackground(Color.RED);
    setBorder(BorderFactory.createEmptyBorder(40, 15, 15, 15));

    this.applicationPanel = new HuGameApplicationPanel();
    add(applicationPanel);
  }

  public void startApplication() {
    applicationPanel.startApplication();
  }
}
