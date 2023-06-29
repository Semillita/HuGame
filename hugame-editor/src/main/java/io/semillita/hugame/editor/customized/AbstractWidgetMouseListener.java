package io.semillita.hugame.editor.customized;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import io.semillita.hugame.editor.Widget;

public class AbstractWidgetMouseListener extends MouseAdapter {
	private final Widget widget;
	private final WorkspaceInputManager inputManager;

	public AbstractWidgetMouseListener(Widget widget, WorkspaceInputManager inputManager) {
		this.widget = widget;
		this.inputManager = inputManager;
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		inputManager.mouseClicked(widget, e);
	}

	@Override
	public void mousePressed(MouseEvent e) {
		inputManager.mousePressed(widget, e);
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		inputManager.mouseReleased(widget, e);
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		inputManager.mouseEntered(widget, e);
	}

	@Override
	public void mouseExited(MouseEvent e) {
		inputManager.mouseExited(widget, e);
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		inputManager.mouseDragged(widget, e);
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		inputManager.mouseMoved(widget, e);
	}

}
