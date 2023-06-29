package io.semillita.hugame.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;

import javax.swing.UIManager;

import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatIntelliJLaf;

public class Launcher {

	public static void main(String... args) {
		setupUI();
		var window = new Window("Hugo", 960, 540);

		window.setTitleBarColor(Colors.GRADIENT_1, Colors.WHITE);
		window.setBackgroundColor(Color.MAGENTA);

		window.repaint();
	}

	private static void setupUI() {
		FlatDarkLaf.setup();
		setupTitlePaneUI();
		setupMenuBarUI();
		setupTabbedPaneUI();
		setupSplitPaneUI();
		setupPopupMenuUI();
		setupMenuItemUI();
		setupSeparatorUI();
	}

	private static void setupTitlePaneUI() {
		UIManager.put("TitlePane.buttonSize", new Dimension(50, 26));
		UIManager.put("TitlePane.buttonPressedBackground", Colors.BRIGHT_FOREGROUND_2);
		UIManager.put("TitlePane.closePressedBackground", Colors.BRIGHT_FOREGROUND_2);
		UIManager.put("TitlePane.borderColor", Color.WHITE);
		UIManager.put("TitlePane.titleMargins", new Insets(0, 0, 0, 0));
		UIManager.put("TitlePane.iconMargins", new Insets(0, 0, 0, 0));
		UIManager.put("TitlePane.iconSize", new Dimension(20, 20));
		UIManager.put("TitlePane.noIconLeftGap", 0);
	}

	private static void setupMenuBarUI() {
		UIManager.put("MenuBar.hoverBackground", Colors.GRADIENT_2);
		UIManager.put("MenuBar.selectionBackground", Colors.GRADIENT_2);
		UIManager.put("MenuBar.selectionForeground", Color.WHITE);
		UIManager.put("MenuBar.foreground", Color.WHITE);
		UIManager.put("MenuBar.underlineSelectionHeight", 25);
	}

	private static void setupTabbedPaneUI() {
		UIManager.put("TabbedPane.selectedBackground", Colors.GRADIENT_3);
		UIManager.put("TabbedPane.height", 15);
		UIManager.put("TabbedPane.focusColor", Color.RED);
		UIManager.put("TabbedPane.focus", new Color(0, true));

		UIManager.put("TabbedPane.cardTabSelectionHeight", 0);

		UIManager.put("TabbedPane.tabAreaBackground", Color.CYAN);
		UIManager.put("TabbedPane.contentSeparatorHeight", 1);
		UIManager.put("TabbedPane.tabSeparatorColor", Color.BLACK);
		UIManager.put("TabbedPane.contentAreaColor", Color.BLACK);
		UIManager.put("TabbedPane.focus", new Color(0, 0, 0, 0));
		UIManager.put("TabbedPane.hoverColor", Colors.GRADIENT_3);
	}

	private static void setupSplitPaneUI() {
		UIManager.put("SplitPane.background", Colors.GRADIENT_1);
		UIManager.put("SplitPane.dividerSize", 8);
	}

	private static void setupPopupMenuUI() {
		UIManager.put("PopupMenu.background", Colors.GRADIENT_3);
	}

	private static void setupMenuItemUI() {
		UIManager.put("MenuItem.background", Colors.GRADIENT_2);
		UIManager.put("MenuItem.foreground", Color.WHITE);
		UIManager.put("MenuItem.margin", new Insets(3, 3, 3, 3));
		UIManager.put("MenuItem.minimunIconSize", 0);
		UIManager.put("MenuItem.iconTextGap", 0);
		UIManager.put("MenuItem.selectionBackground", Colors.GRADIENT_4);

		UIManager.put("Menu.selectionBackground", Colors.GRADIENT_4);
	}

	private static void setupSeparatorUI() {
		UIManager.put("Separator.foreground", Color.WHITE);
		UIManager.put("Separator.height", 9);
		UIManager.put("Separator.stripeWidth", 1);
		UIManager.put("Separator.stripeIndent", 4);
	}

}
