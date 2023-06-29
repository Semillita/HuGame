package io.semillita.hugame.editor.menu;

import java.awt.Color;
import java.awt.Graphics;
import java.util.Optional;

import javax.swing.JSeparator;
import javax.swing.UIManager;

import com.formdev.flatlaf.extras.components.FlatSeparator;

import io.semillita.hugame.editor.Colors;

public class Separator extends JSeparator {
	@Override
	public void paintComponent(Graphics g) {
		var width = super.getWidth();
		var height = super.getHeight();
		var thickness = (int) Optional.ofNullable(UIManager.get("Separator.stripeWidth")).orElse(1);
		var offset = (int) Optional.ofNullable(UIManager.get("Separator.stripeIndent"))
				.orElse((height - thickness) / 2);
		var foreground = super.getForeground();
		var background = Colors.GRADIENT_2;
		var sideInset = 10;
		g.setColor(background);
		g.fillRect(0, 0, width, height);
		g.setColor(foreground);
		g.fillRect(sideInset, offset, width - 2 * sideInset, thickness);
	}
}
