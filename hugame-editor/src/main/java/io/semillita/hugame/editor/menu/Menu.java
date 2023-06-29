package io.semillita.hugame.editor.menu;

import io.semillita.hugame.editor.Colors;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JMenu;

public class Menu extends JMenu {

  public Menu(String name) {
    super(name);
    super.setForeground(Color.WHITE);
    super.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 12));
    super.setOpaque(true);
    super.setBackground(Colors.GRADIENT_1);
    super.setBorder(
        BorderFactory.createCompoundBorder(
            super.getBorder(), BorderFactory.createEmptyBorder(3, 10, 3, 10)));
    super.getPopupMenu().setBorder(null);
  }
}
