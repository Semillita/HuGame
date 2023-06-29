package io.semillita.hugame.editor.customized;

import io.semillita.hugame.editor.Widget;

public record WidgetSpacePartition(Orientation orientation, 
		int tabAreaHeight,
		int topHalfHeight,
		int bottomHalfHeight,
		int leftHalfWidth,
		int rightHalfWidth,
		int topThirdHeight,
		int bottomThirdHeight,
		int leftThirdWidth,
		int rightThirdWidth,
		int topHalfY,
		int bottomHalfY,
		int leftHalfX,
		int rightHalfX,
		int topThirdY,
		int bottomThirdY,
		int leftThirdX,
		int rightThirdX) {

	public static WidgetSpacePartition of(Widget widget) {
		var tabbedPane = widget.getTabbedPane();
		final var tabAreaHeight = widget.getUI().getTabAreaHeight();
		
		var contentAreaWidth = tabbedPane.getWidth() - 2;
		var contentAreaHeight = tabbedPane.getHeight() - tabAreaHeight - 3;
		
		final var orientation = (contentAreaWidth >= contentAreaHeight) ? Orientation.HORIZONTAL : Orientation.VERTICAL;
		
		final var topHalfHeight = contentAreaHeight / 2;
		final var bottomHalfHeight = contentAreaHeight - topHalfHeight;
		
		final var topHalfY = tabAreaHeight + 2;
		final var bottomHalfY = topHalfY + topHalfHeight;
		
		final var leftHalfWidth = contentAreaWidth / 2;
		final var rightHalfWidth = contentAreaWidth - leftHalfWidth;
		
		final var leftHalfX = 1;
		final var rightHalfX = leftHalfX + leftHalfWidth;
		
		final var topThirdHeight = contentAreaHeight / 3;
		final var bottomThirdHeight = topThirdHeight;
		
		final var topThirdY = tabAreaHeight + 2;
		final var bottomThirdY = topThirdY + contentAreaHeight - bottomThirdHeight;
		
		final var leftThirdWidth = contentAreaWidth / 3;
		final var rightThirdWidth = leftThirdWidth;
		
		final var leftThirdX = 1;
		final var rightThirdX = contentAreaWidth - rightThirdWidth + 1;
		
		return new WidgetSpacePartition(
				orientation, 
				tabAreaHeight, 
				topHalfHeight, 
				bottomHalfHeight, 
				leftHalfWidth, 
				rightHalfWidth, 
				topThirdHeight, 
				bottomThirdHeight, 
				leftThirdWidth, 
				rightThirdWidth, 
				topHalfY, 
				bottomHalfY, 
				leftHalfX, 
				rightHalfX, 
				topThirdY, 
				bottomThirdY, 
				leftThirdX, 
				rightThirdX);
	}

	public static enum Orientation {
		HORIZONTAL, VERTICAL;
	}

}
