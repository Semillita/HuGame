package io.semillita.hugame.editor.customized;

import io.semillita.hugame.editor.Widget;
import java.awt.event.MouseEvent;

public record WidgetMouseEvent(Widget widget, MouseEvent event) {}
