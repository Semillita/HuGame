package io.semillita.hugame.editor;

import javax.swing.JComponent;

public interface SplitNode {

	public void setWorkspace(Workspace workspace);

	public SplitContainer getParent();

	public void setParent(SplitContainer parent);

	public JComponent getJComponent();

}
