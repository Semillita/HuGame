package io.semillita.hugame.editor;

import java.awt.Font;
import java.awt.FontMetrics;
import java.util.function.Consumer;

import javax.swing.JTabbedPane;

import com.formdev.flatlaf.ui.FlatTabbedPaneUI;

public final class StartupWindow extends Window {

	private final Consumer<String> projectOpenListener;
	
	public StartupWindow(Consumer<String> projectOpenListener) {
		super("HuGame", 960, 540);
		
		var frame = super.getJFrame();
		super.setBackgroundColor(Colors.GRADIENT_1);
		this.projectOpenListener = projectOpenListener;
		
		var tabbedPane = new JTabbedPane();
		frame.add(tabbedPane);
		tabbedPane.putClientProperty("JTabbedPane.tabHeight", 50);
		tabbedPane.putClientProperty("JTabbedPane.tabWidthMode", "equal");
		tabbedPane.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 30));
		
		tabbedPane.setUI(new FlatTabbedPaneUI() {
			@Override
			protected int calculateTabWidth( int tabPlacement, int tabIndex, FontMetrics metrics ) {
				var tabPane = super.tabPane;
				var tabCount = tabPane.getTabCount();
				
				return (int) (tabPane.getWidth() / tabCount);
			}
		});
		
		var chooseProjectPanel = new ChooseProjectPanel();
		var newProjectPanel = new NewProjectPanel();
		
		tabbedPane.add("Existing project", chooseProjectPanel.getJPanel());
		tabbedPane.add("New project", newProjectPanel.getJPanel());
		tabbedPane.setBackground(Colors.GRADIENT_1);
		
		frame.pack();
		
		newProjectPanel.addComponents();
		
		frame.setVisible(true);
	}

}
