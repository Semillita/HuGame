package io.semillita.hugame.editor;

import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JFrame;

import io.semillita.hugame.editor.menu.Menu;
import io.semillita.hugame.editor.menu.MenuBar;
import io.semillita.hugame.editor.menu.MenuItem;
import io.semillita.hugame.editor.menu.Separator;
import io.semillita.hugame.editor.menu.SubMenu;

public class Window {

	private final JFrame frame;
	private final MenuBar menuBar;

	public Window(String title, int width, int height) {
		frame = new JFrame();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setPreferredSize(new Dimension(width, height));
		frame.pack();
		frame.setTitle(title);
		frame.setLocationRelativeTo(null);
		
		setTitleBarColor(Colors.GRADIENT_1, Colors.WHITE);

		this.menuBar = new MenuBar();
		frame.setJMenuBar(menuBar);
	}

	public void repaint() {
		frame.repaint();
	}

	public void setBackgroundColor(Color background) {
		frame.getContentPane().setBackground(background);
	}

	public void setTitleBarColor(Color background, Color foreground) {
		frame.getRootPane().putClientProperty("JRootPane.titleBarBackground", background);
		frame.getRootPane().putClientProperty("JRootPane.titleBarForeground", foreground);
	}

	protected JFrame getJFrame() {
		return frame;
	}
	
	protected MenuBar getMenuBar() {
		return menuBar;
	}
	
}
