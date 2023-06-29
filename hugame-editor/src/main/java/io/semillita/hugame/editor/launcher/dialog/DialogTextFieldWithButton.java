package io.semillita.hugame.editor.launcher.dialog;

import io.semillita.hugame.editor.Colors;
import java.awt.Graphics;
import javax.swing.BorderFactory;

public class DialogTextFieldWithButton extends DialogTextField {
  private static final int BUTTON_SIZE = 40;

  public DialogTextFieldWithButton(Runnable onClick, int width) {
    super(width);

    var chooseFileButton = new ChooseFileButton(onClick);
    add(chooseFileButton);
    chooseFileButton.setLocation(getTextFieldWidth(), 0);
    chooseFileButton.setSize(BUTTON_SIZE, BUTTON_SIZE);
    chooseFileButton.setBorder(BorderFactory.createEmptyBorder());
  }

  @Override
  protected int getTextFieldWidth() {
    return getWidth() - BUTTON_SIZE;
  }

  @Override
  public void paintBorder(Graphics g) {
    super.paintBorder(g);

    g.setColor(Colors.Launcher.OUTLINES);
    g.drawLine(getTextFieldWidth(), 0, getTextFieldWidth(), getHeight());
  }
}
