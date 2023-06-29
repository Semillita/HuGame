package io.semillita.hugame.editor.customized;

import java.awt.event.MouseEvent;

import io.semillita.hugame.editor.Widget;

public record WidgetMouseEvent(Widget widget, MouseEvent event) {}
