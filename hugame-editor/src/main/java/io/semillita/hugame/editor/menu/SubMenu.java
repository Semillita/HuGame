package io.semillita.hugame.editor.menu;

import io.semillita.hugame.editor.Colors;
import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;

public class SubMenu extends Menu {

  public SubMenu(String name) {
    super(name);
    super.setForeground(Color.WHITE);
    super.setBackground(Colors.GRADIENT_2);
    super.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
    super.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 12));

    super.getPopupMenu().setBorder(null);
  }
}
