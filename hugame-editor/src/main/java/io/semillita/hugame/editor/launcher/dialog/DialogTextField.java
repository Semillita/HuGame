package io.semillita.hugame.editor.launcher.dialog;

import io.semillita.hugame.editor.Colors;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTextField;

public class DialogTextField extends JComponent {
  private static final int HEIGHT = 40;
  private final JTextField jTextField;

  public DialogTextField(int width) {
    setSize(width, HEIGHT);
    setBackground(Colors.Launcher.MENU_BUTTON_BACKGROUND);
    setLayout(null);

    this.jTextField = new JTextField();
    add(jTextField);
    jTextField.setLocation(10, 0);
    jTextField.setSize(getTextFieldWidth() - 40, HEIGHT);
    jTextField.setOpaque(false);
    jTextField.setBackground(Colors.TRANSPARENT);
    jTextField.setBorder(BorderFactory.createEmptyBorder());
    jTextField.setForeground(Colors.Launcher.PRIMARY_TEXT);
    jTextField.setSelectionColor(Colors.Launcher.OUTLINES);

    setText("");
  }

  public void setText(String text) {
    jTextField.setText(text);
  }

  public void setFontSize(int size) {
    jTextField.setFont(new Font("Segoe UI Variable Small Semibold", Font.PLAIN, size));
  }

  public void setFont(Font font) {
    jTextField.setFont(font);
  }

  public String getText() {
    return jTextField.getText();
  }

  @Override
  protected void paintComponent(Graphics g) {
    var g2d = (Graphics2D) g;

    var radius = 15;
    g2d.setColor(getBackground());
    var rect = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, radius, radius);
    g2d.fill(rect);
  }

  @Override
  protected void paintBorder(Graphics g) {
    var g2d = (Graphics2D) g;
    g2d.setColor(Colors.Launcher.OUTLINES);
    var rect = new RoundRectangle2D.Double(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
    g2d.draw(rect);
  }

  protected int getTextFieldWidth() {
    return getWidth();
  }
}
