package io.semillita.hugame.editor.menu;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.JPopupMenu;

import io.semillita.hugame.editor.Colors;

public class PopupMenu extends JPopupMenu {

	public PopupMenu() {
		super.setBackground(Color.MAGENTA);
		super.setOpaque(true);
		super.setBorder(BorderFactory.createEmptyBorder());
	}

}