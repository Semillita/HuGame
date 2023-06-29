package io.semillita.hugame.editor;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JTabbedPane;
import javax.swing.JViewport;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import io.semillita.hugame.editor.SplitPane.Orientation;
import io.semillita.hugame.editor.customized.CustomTabbedPaneUI;
import io.semillita.hugame.editor.customized.WorkspaceInputManager;

public class Widget implements SplitNode {
	private final JTabbedPane tabbedPane;
	private final CustomTabbedPaneUI ui;
	private final WorkspaceInputManager inputManager;

	private Workspace workspace;
	private SplitContainer parent;

	public Widget(WorkspaceInputManager inputManager) {
		this.tabbedPane = new JTabbedPane(JTabbedPane.TOP, JTabbedPane.SCROLL_TAB_LAYOUT) {
			@Override
			public void paint(Graphics graphics) {
				super.paint(graphics);

				Widget.this.ui.paintHoverBorder(graphics);
			}

			@Override
			public String toString() {
				var tabTitles = IntStream.range(0, super.getTabCount()).mapToObj(super::getTitleAt).toList();

				var titlesString = (tabTitles.size() <= 0) ? "" : tabTitles.get(0);
				for (int i = 1; i < tabTitles.size(); i++) {
					titlesString += ", " + tabTitles.get(i);
				}

				return "TabbedPane[" + titlesString + "]";
			}
		};
		tabbedPane.setMinimumSize(new Dimension(200, 100));
		tabbedPane.setPreferredSize(new Dimension(300, 100));
		setClientProperties();
		tabbedPane.setFocusable(false);
		tabbedPane.setForeground(Color.WHITE);
		tabbedPane.setBackground(Colors.GRADIENT_1);

		this.ui = new CustomTabbedPaneUI(this, inputManager);

		tabbedPane.setUI(ui);

		tabbedPane.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent event) {
				ui.recalculateSpacePartition();
			}
		});

		ui.recalculateSpacePartition();

		this.inputManager = inputManager;
	}

	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

	public void addView(View view) {
		tabbedPane.add(view.title(), view.contentPanel());
	}

	private final void setClientProperties() {
		tabbedPane.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		// TODO: Check which of these do nothing (we have UI defaults)
		tabbedPane.putClientProperty("JTabbedPane.tabType", "card");
		tabbedPane.putClientProperty("JTabbedPane.showTabSeparators", false);
		tabbedPane.putClientProperty("JTabbedPane.showContentSeparator", true);
		tabbedPane.putClientProperty("JTabbedPane.hasFullBorder", false);
		tabbedPane.putClientProperty("JTabbedPane.tabHeight", 20);
		tabbedPane.putClientProperty("JTabbedPane.scrollButtonsPolicy", "never");
		tabbedPane.putClientProperty("JTabbedPane.tabAreaAlignment", "leading");
	}

	@Override
	public JComponent getJComponent() {
		return tabbedPane;
	}

	public CustomTabbedPaneUI getUI() {
		return ui;
	}

	public View popView(int index) {
		var title = tabbedPane.getTitleAt(index);
		var panel = tabbedPane.getComponentAt(index);
		if (!(panel instanceof ContentPanel)) {
			return null;
		}

		var contentPanel = (ContentPanel) panel;

		var view = new View(title, contentPanel);
		tabbedPane.remove(index);

		if (tabbedPane.getTabCount() == 0) {
			parent.remove(this);
		}

		return view;
	}

	public void addToSplitPane(SplitPane splitPane) {
		this.parent = splitPane;
		splitPane.add(this);
	}

	@Override
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public void dropView(View view, Section section) {

		switch (section) {
		case TAB_AREA:
			dropViewInTabArea(view);
			break;
		case TOP_HALF:
			dropViewInTopHalf(view);
			break;
		case BOTTOM_HALF:
			dropViewInBottomHalf(view);
			break;
		case LEFT_HALF:
			dropViewInLeftHalf(view);
			break;
		case RIGHT_HALF:
			dropViewInRightHalf(view);
			break;
		}
	}

	private void dropViewInTabArea(View view) {
		addView(view);
	}

	private void dropViewInTopHalf(View view) {
		var newParentSplitPane = new SplitPane(Orientation.VERTICAL);

		parent.replace(this, newParentSplitPane);

		var newWidget = new Widget(inputManager);
		newWidget.addView(view);
		newWidget.addToSplitPane(newParentSplitPane);

		addToSplitPane(newParentSplitPane);
	}

	private void dropViewInBottomHalf(View view) {
		var newParentSplitPane = new SplitPane(Orientation.VERTICAL);

		parent.replace(this, newParentSplitPane);

		addToSplitPane(newParentSplitPane);

		var newWidget = new Widget(inputManager);
		newWidget.addView(view);
		newWidget.addToSplitPane(newParentSplitPane);
	}

	private void dropViewInRightHalf(View view) {
		var newParentSplitPane = new SplitPane(Orientation.HORIZONTAL);

		parent.replace(this, newParentSplitPane);

		addToSplitPane(newParentSplitPane);

		var newWidget = new Widget(inputManager);
		newWidget.addView(view);
		newWidget.addToSplitPane(newParentSplitPane);
	}

	private void dropViewInLeftHalf(View view) {
		var newParentSplitPane = new SplitPane(Orientation.HORIZONTAL);

		parent.replace(this, newParentSplitPane);

		var newWidget = new Widget(inputManager);
		newWidget.addView(view);
		newWidget.addToSplitPane(newParentSplitPane);

		addToSplitPane(newParentSplitPane);
	}

	@Override
	public String toString() {
		var tabTitles = IntStream.range(0, tabbedPane.getTabCount()).mapToObj(tabbedPane::getTitleAt).toList();

		var titlesString = (tabTitles.size() <= 0) ? "" : tabTitles.get(0);
		for (int i = 1; i < tabTitles.size(); i++) {
			titlesString += ", " + tabTitles.get(i);
		}

		return "Widget[" + titlesString + "]";
	}

	public static enum Section {
		TAB_AREA, TOP_HALF, BOTTOM_HALF, RIGHT_HALF, LEFT_HALF;
	}

	@Override
	public SplitContainer getParent() {
		return parent;
	}

	@Override
	public void setParent(SplitContainer parent) {
		this.parent = parent;
	}

}
