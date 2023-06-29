package io.semillita.hugame.editor.customized;

import io.semillita.hugame.editor.Widget;
import io.semillita.hugame.editor.Workspace;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.event.MouseEvent;
import javax.swing.JTabbedPane;

public class WorkspaceInputManager {
  private final Workspace workspace;

  private Widget hoveredWidget;
  private TabDragEvent dragEvent;

  private int draggedTabIndex = -1;

  public WorkspaceInputManager(Workspace workspace) {
    this.workspace = workspace;
  }

  public void mouseClicked(Widget widget, MouseEvent event) {}

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

    if (dragSource.getTabbedPane().getTabCount() == 0) {}

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
      var positionRelativeToTarget =
          translateMouseCoordinates(
              source.getTabbedPane(),
              hoveredWidget.getTabbedPane(),
              new Point(event.getX(), event.getY()));
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

  private static record TabDragEvent(Widget source, int tabIndex) {}
}
