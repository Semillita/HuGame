package io.semillita.hugame.editor;

public interface SplitContainer {

	public void add(SplitNode child);

	public void remove(SplitNode node);

	public void replace(SplitNode oldSplitNode, SplitNode newSplitNode);
}
