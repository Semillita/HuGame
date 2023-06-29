package io.semillita.hugame.editor;

public record View(String title, ContentPanel contentPanel) {
	@Override
	public String toString() {
		return String.format("View[%s]", title);
	}
}
