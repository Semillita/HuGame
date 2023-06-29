package io.semillita.hugame.editor.customized;

import io.semillita.hugame.editor.Widget;
import javax.swing.JTabbedPane;

public class CustomTabbedPane extends JTabbedPane {
  private final Widget widget;

  public CustomTabbedPane(Widget widget) {
    this.widget = widget;
  }

  public Widget getSurroundingWidget() {
    return widget;
  }
}
