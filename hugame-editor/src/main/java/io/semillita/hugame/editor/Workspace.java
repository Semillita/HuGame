package io.semillita.hugame.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.OverlayLayout;

import io.semillita.hugame.editor.customized.WorkspaceInputManager;

public class Workspace implements SplitContainer {
	private final JPanel container;
	private final WorkspaceInputManager inputManager;

	private SplitNode child;

	public Workspace() {
		container = new JPanel() {
			@Override
			public void paintComponent(Graphics graphics) {
				super.paintComponent(graphics);
			}

			@Override
			public void paint(Graphics graphics) {
				super.paint(graphics);
				drawFloatingTab((Graphics2D) graphics);
			}
		};

		container.setLayout(new BorderLayout());
		container.setBackground(Colors.GRADIENT_1);
		container.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));

		inputManager = new WorkspaceInputManager(this);
	}

	public JPanel getContainer() {
		return container;
	}

	public void add(SplitNode child) {
		container.add(child.getJComponent());
		if (child instanceof Widget widget) {
			widget.setWorkspace(this);
		}

		this.child = child;

		child.setParent(this);
		child.setWorkspace(this);
	}

	public WorkspaceInputManager getInputManager() {
		return inputManager;
	}

	private void drawFloatingTab(Graphics2D graphics) {
		if (inputManager.getDragEvent() != null) {
			var mousePosition = container.getMousePosition();
			if (mousePosition != null) {
				graphics.setColor(Color.WHITE);
				graphics.drawRect(mousePosition.x - 25, mousePosition.y - 10, 50, 20);
			}
		}
	}

	@Override
	public String toString() {
		return String.format("Workspace[%s]", (child == null) ? null : child.toString());
	}

	private static class OverlayPanel extends JPanel {
		@Override
		public boolean isOpaque() {
			return false;
		}
	}

	@Override
	public void remove(SplitNode childToRemove) {
		var componentToRemove = childToRemove.getJComponent();
		if (this.child.getJComponent() == componentToRemove) {
			container.remove(componentToRemove);
		}

	}

	@Override
	public void replace(SplitNode oldSplitNode, SplitNode newSplitNode) {
		var oldComponent = oldSplitNode.getJComponent();
		if (child.getJComponent() == oldComponent) {
			container.remove(oldComponent);
		} else {
			throw new RuntimeException("Trying to remove wrong child");
		}

		container.add(newSplitNode.getJComponent());
		this.child = newSplitNode;
	}
}
