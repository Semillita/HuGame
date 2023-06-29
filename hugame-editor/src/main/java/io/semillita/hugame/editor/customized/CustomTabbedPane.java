package io.semillita.hugame.editor.customized;

import javax.swing.JTabbedPane;

import io.semillita.hugame.editor.Widget;

public class CustomTabbedPane extends JTabbedPane {
	private final Widget widget;

	public CustomTabbedPane(Widget widget) {
		this.widget = widget;
	}

	public Widget getSurroundingWidget() {
		return widget;
	}

}
