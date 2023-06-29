package io.semillita.hugame.editor.menu;

import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;

public class MenuItem extends JMenuItem {

	public MenuItem(String name) {
		super(name);
		super.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 12));
		super.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
		super.setOpaque(true);
	}

}