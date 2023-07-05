package io.semillita.hugame.editor;

import io.semillita.hugame.editor.menu.Menu;
import io.semillita.hugame.editor.menu.MenuBar;
import io.semillita.hugame.editor.menu.MenuItem;
import io.semillita.hugame.editor.menu.Separator;
import io.semillita.hugame.editor.menu.SubMenu;

public class EditorWindow extends Window {

	public EditorWindow(String title, int width, int height) {
		super(title, width, height);
		
		setTitleBarColor(Colors.GRADIENT_1, Colors.WHITE);

		var menuBar = new MenuBar();
		var frame = super.getJFrame();
		frame.setJMenuBar(menuBar);

		var fileMenu = new Menu("File");

		menuBar.add(fileMenu);
		var newProjectItem = new MenuItem("New project");
		fileMenu.add(newProjectItem);
		var openProjectItem = new MenuItem("Open project");
		fileMenu.add(openProjectItem);

		var separator = new Separator();
		fileMenu.add(separator);

		var exportMenu = new SubMenu("Export");
		fileMenu.add(exportMenu);
		var exportJarItem = new MenuItem("JAR file (.jar)");
		exportMenu.add(exportJarItem);
		var exportExeItem = new MenuItem("Executable file (.exe)");
		exportMenu.add(exportExeItem);

		var helpMenu = new Menu("Help");
		menuBar.add(helpMenu);

		var workspace = new Workspace();
		frame.add(workspace.getContainer());

		var widgetInputManager = workspace.getInputManager();

		var left = new Widget(widgetInputManager);
		left.addView(new View("Explorer", new ContentPanel()));

		var center = new Widget(widgetInputManager);
		center.addView(new View("Hugo 1", new ContentPanel()));
		center.addView(new View("Hugo 22", new ContentPanel()));
		center.addView(new View("Hugo 333", new ContentPanel()));
		center.addView(new View("Hugo 4444", new ContentPanel()));
		center.addView(new View("Hugo 55555", new ContentPanel()));

		var right = new Widget(widgetInputManager);
		right.addView(new View("Properties", new ContentPanel()));

		var leftCenter = new SplitPane();
		left.addToSplitPane(leftCenter);
		center.addToSplitPane(leftCenter);

		var leftCenterRight = new SplitPane();
		leftCenterRight.add(leftCenter);
		right.addToSplitPane(leftCenterRight);

		workspace.add(leftCenterRight);

		frame.setVisible(true);
	}
	
}
