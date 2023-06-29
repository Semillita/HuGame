package io.semillita.hugame.editor.customized;

import static com.formdev.flatlaf.FlatClientProperties.STYLE;
import static com.formdev.flatlaf.FlatClientProperties.STYLE_CLASS;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_HAS_FULL_BORDER;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_HIDE_TAB_AREA_WITH_ONE_TAB;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_LEADING_COMPONENT;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_MAXIMUM_TAB_WIDTH;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_MINIMUM_TAB_WIDTH;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_SCROLL_BUTTONS_PLACEMENT;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_SCROLL_BUTTONS_POLICY;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_SHOW_CONTENT_SEPARATOR;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_SHOW_TAB_SEPARATORS;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TABS_POPUP_POLICY;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_ALIGNMENT;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_AREA_ALIGNMENT;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_AREA_INSETS;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_CLOSABLE;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_CLOSE_TOOLTIPTEXT;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_HEIGHT;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_ICON_PLACEMENT;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_INSETS;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_TYPE;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TAB_WIDTH_MODE;
import static com.formdev.flatlaf.FlatClientProperties.TABBED_PANE_TRAILING_COMPONENT;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ContainerEvent;
import java.awt.event.ContainerListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Optional;
import java.util.function.Consumer;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.synth.SynthTabbedPaneUI;

import io.semillita.hugame.editor.View;
import io.semillita.hugame.editor.Widget;
import io.semillita.hugame.editor.Workspace;

public class WorkspaceInputManager {
	private final Workspace workspace;

	private Widget hoveredWidget;
	private TabDragEvent dragEvent;

	private int draggedTabIndex = -1;

	public WorkspaceInputManager(Workspace workspace) {
		this.workspace = workspace;
	}

	public void mouseClicked(Widget widget, MouseEvent event) {
	}

	public void mousePressed(Widget widget, MouseEvent event) {
		var insideTabArea = widget.getUI().isInsideTabArea(event.getX(), event.getY());
		if (insideTabArea) {
			var tabIndex = widget.getUI().getTabIndexAt(event.getX());
			draggedTabIndex = tabIndex;
		}
	}

	public void mouseReleased(Widget widget, MouseEvent event) {
		if (dragEvent == null) {
			return;
		}

		var tabbedPane = widget.getTabbedPane();
		var absoluteX = event.getX() + tabbedPane.getX();
		var absoluteY = event.getY() + tabbedPane.getY();

		var target = (hoveredWidget != null) ? hoveredWidget : widget;

		tabDroppedInWidget(target, new Point(absoluteX, absoluteY));

		widget.getUI().clearHover();

		workspace.getContainer().repaint();
	}

	private void tabDroppedInWidget(Widget target, Point position) {
		var tabbedPane = target.getTabbedPane();

		var relativeX = position.x - tabbedPane.getX();
		var relativeY = position.y - tabbedPane.getY();

		var section = target.getUI().getSectionFor(relativeX, relativeY);

		var dragSource = dragEvent.source();
		var view = dragSource.popView(dragEvent.tabIndex());

		if (dragSource.getTabbedPane().getTabCount() == 0) {

		}

		target.dropView(view, section);

		dragEvent = null;
		draggedTabIndex = -1;

		workspace.getContainer().repaint();
	}

	public void mouseEntered(Widget widget, MouseEvent event) {
		hoveredWidget = widget;

		if (dragEvent != null) {
			widget.getTabbedPane().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

			var dragSource = dragEvent.source();
			dragSource.getUI().clearHover();
		}

		workspace.getContainer().repaint();
		widget.getTabbedPane().repaint();

	}

	public void mouseExited(Widget widget, MouseEvent e) {
		if (hoveredWidget == widget) {
			hoveredWidget = null;
		}
		widget.getUI().clearHover();
		widget.getTabbedPane().repaint();
		workspace.getContainer().repaint();
	}

	public void mouseDragged(Widget source, MouseEvent event) {
		var insideTabArea = source.getUI().isInsideTabArea(event.getX(), event.getY());
		if (!insideTabArea) {
			if (draggedTabIndex != -1) {
				dragEvent = new TabDragEvent(source, draggedTabIndex);
				draggedTabIndex = -1;

				var tabbedPane = source.getTabbedPane();
				tabbedPane.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
				tabbedPane.repaint();
			}
		}

		if (hoveredWidget != null && hoveredWidget != source) {
			var positionRelativeToTarget = translateMouseCoordinates(source.getTabbedPane(),
					hoveredWidget.getTabbedPane(), new Point(event.getX(), event.getY()));
			mouseDraggedOverWidget(hoveredWidget, positionRelativeToTarget.x, positionRelativeToTarget.y);

			return;
		}

		mouseDraggedOverWidget(source, event.getX(), event.getY());
		workspace.getContainer().repaint();
	}

	public void mouseDraggedOverWidget(Widget widget, int x, int y) {
		if (dragEvent != null) {
			widget.getUI().setMouseCoordinates(x, y);
			workspace.getContainer().repaint();
		}
	}

	public void mouseMoved(Widget widget, MouseEvent event) {
		if (dragEvent != null) {
			var x = event.getX();
			var y = event.getY();
			widget.getUI().setMouseCoordinates(x, y);
			widget.getTabbedPane().repaint();
		}
	}

	public TabDragEvent getDragEvent() {
		return dragEvent;
	}

	private Point translateMouseCoordinates(JTabbedPane source, JTabbedPane target, Point position) {
		var absoluteX = position.x + source.getX();
		var absoluteY = position.y + source.getY();

		var targetRelativeX = absoluteX - target.getX();
		var targetRelativeY = absoluteY - target.getY();

		return new Point(targetRelativeX, targetRelativeY);
	}

	private static record TabDragEvent(Widget source, int tabIndex) {
	}
}
