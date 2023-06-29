package io.semillita.hugame.editor;

import java.util.Arrays;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

public class SplitPane implements SplitNode, SplitContainer {

	private final JSplitPane jSplitPane;
	private Workspace workspace;

	private SplitNode firstChild, secondChild;
	private int index = 0;

	private SplitContainer parent;

	public SplitPane() {
		this(Orientation.HORIZONTAL);
	}

	public SplitPane(Orientation orientation) {
		jSplitPane = new JSplitPane(orientation.getCode(), true);
	}

	public void add(SplitNode child) {
		var component = child.getJComponent();
		jSplitPane.add(component, index);
		if (jSplitPane.getComponent(1) == component) {
			firstChild = child;
		} else {
			secondChild = child;
		}

		index++;

		child.setParent(this);
		child.setWorkspace(workspace);
	}

	@Override
	public void remove(SplitNode childToRemove) {
		var nodeToKeep = (jSplitPane.getComponent(1) == childToRemove.getJComponent()) ? secondChild : firstChild;

		jSplitPane.remove(2);
		jSplitPane.remove(1);

		parent.replace(this, nodeToKeep);
	}

	@Override
	public JComponent getJComponent() {
		return jSplitPane;
	}

	@Override
	public String toString() {
		return String.format("SplitPane[%s, %s]", toString(firstChild), toString(secondChild));
	}

	private String toString(SplitNode splitNode) {
		return (splitNode == null) ? null : splitNode.toString();
	}

	public static enum Orientation {
		HORIZONTAL(JSplitPane.HORIZONTAL_SPLIT), VERTICAL(JSplitPane.VERTICAL_SPLIT);

		private final int code;

		Orientation(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}

	@Override
	public void setParent(SplitContainer parent) {
		this.parent = parent;
	}

	@Override
	public void replace(SplitNode oldSplitNode, SplitNode newSplitNode) {
		var indexToChange = (jSplitPane.getComponent(1) == oldSplitNode.getJComponent()) ? 1 : 2;
		jSplitPane.remove(indexToChange);
		jSplitPane.add(newSplitNode.getJComponent(), indexToChange);

		newSplitNode.setParent(this);
	}

	@Override
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	@Override
	public SplitContainer getParent() {
		return parent;
	}

}
