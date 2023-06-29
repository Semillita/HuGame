package io.semillita.hugame.editor.customized;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.accessibility.Accessible;
import javax.accessibility.AccessibleContext;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.text.JTextComponent;

import static com.formdev.flatlaf.FlatClientProperties.*;
import com.formdev.flatlaf.ui.FlatTabbedPaneUI;
import com.formdev.flatlaf.util.StringUtils;

import io.semillita.hugame.editor.Widget;
import io.semillita.hugame.editor.Widget.Section;
import io.semillita.hugame.editor.customized.WidgetSpacePartition.Orientation;
import io.semillita.hugame.editor.menu.MenuItem;
import io.semillita.hugame.editor.menu.PopupMenu;

import static com.formdev.flatlaf.util.UIScale.*;

public class CustomTabbedPaneUI extends FlatTabbedPaneUI {
	private final Widget widget;
	private final JTabbedPane tabbedPane;
	private final WorkspaceInputManager inputManager;
	private CustomMoreTabsButton moreTabsButton;

	private WidgetSpacePartition spacePartition;

	// Can be present or null depending on whether the widget is hovered
	private boolean hovered;
	private int mouseX, mouseY;

	private int tabsToShow = 0;

	public CustomTabbedPaneUI(Widget widget, WorkspaceInputManager inputManager) {
		this.widget = widget;
		this.tabbedPane = widget.getTabbedPane();
		this.inputManager = inputManager;
	}

	public void paintHoverBorder(Graphics graphics) {
		super.paint(graphics, tabbedPane);

		if (hovered) {
			var g2d = (Graphics2D) graphics;
			g2d.setColor(Color.WHITE);
			g2d.setStroke(new BasicStroke(2));

			Consumer<Graphics> drawFunction = switch (getSectionFor(mouseX, mouseY)) {
			case TAB_AREA -> this::paintTabAreaHoverBorder;
			case TOP_HALF -> this::paintTopHalfHoverBorder;
			case BOTTOM_HALF -> this::paintBottomHalfHoverBorder;
			case RIGHT_HALF -> this::paintRightHalfHoverBorder;
			case LEFT_HALF -> this::paintLeftHalfHoverBorder;
			default -> g -> {
			};
			};

			drawFunction.accept(graphics);
		}
	}

	private void paintTabAreaHoverBorder(Graphics graphics) {
		graphics.drawRect(2, 2, tabbedPane.getWidth() - 4, getTabAreaHeight() - 1);
	}

	private void paintTopHalfHoverBorder(Graphics graphics) {
		graphics.drawRect(2, spacePartition.tabAreaHeight() + 3, tabbedPane.getWidth() - 4,
				spacePartition.topHalfHeight() - 3);
	}

	private void paintBottomHalfHoverBorder(Graphics graphics) {
		graphics.drawRect(2, spacePartition.bottomHalfY(), tabbedPane.getWidth() - 4,
				spacePartition.bottomHalfHeight() - 1);
	}

	private void paintRightHalfHoverBorder(Graphics graphics) {
		var tabAreaHeight = getTabAreaHeight();
		var borderWidth = tabbedPane.getWidth() / 2;
		graphics.drawRect(borderWidth + 1, tabAreaHeight + 3, spacePartition.rightHalfWidth() - 2,
				tabbedPane.getHeight() - tabAreaHeight - 5);
	}

	private void paintLeftHalfHoverBorder(Graphics graphics) {
		var tabAreaHeight = getTabAreaHeight();
		graphics.drawRect(2, tabAreaHeight + 3, spacePartition.leftHalfWidth() - 2,
				tabbedPane.getHeight() - tabAreaHeight - 5);
	}

	@Override
	protected void paintCardTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h) {
		Graphics2D g2 = (Graphics2D) g;

		float borderWidth = scale((float) contentSeparatorHeight);
		g.setColor((tabSeparatorColor != null) ? tabSeparatorColor : contentAreaColor);

		switch (tabPlacement) {
		default:
		case TOP:
		case BOTTOM:
			if (tabIndex != 0) {
				g2.fill(new Rectangle2D.Float(x, y, borderWidth, h));
			}

			g2.fill(new Rectangle2D.Float(x + w - borderWidth, y, borderWidth, h));
			break;
		case LEFT:
		case RIGHT:
			break;
		}

		if (cardTabSelectionHeight <= 0) {
			// if there is no tab selection indicator, paint a top border as well
			switch (tabPlacement) {
			default:
			case TOP:
//					g2.fill( new Rectangle2D.Float( x, y, w, borderWidth ) );
				break;
			case BOTTOM:
				g2.fill(new Rectangle2D.Float(x, y + h - borderWidth, w, borderWidth));
				break;
			case LEFT:
				g2.fill(new Rectangle2D.Float(x, y, borderWidth, h));
				break;
			case RIGHT:
				g2.fill(new Rectangle2D.Float(x + w - borderWidth, y, borderWidth, h));
				break;
			}
		}
	}

	@Override
	protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
		if (tabPane.getTabCount() <= 0 || contentSeparatorHeight == 0
				|| !clientPropertyBoolean(tabPane, TABBED_PANE_SHOW_CONTENT_SEPARATOR, showContentSeparator))
			return;

		Insets insets = tabPane.getInsets();
		Insets tabAreaInsets = getTabAreaInsets(tabPlacement);

		int x = insets.left;
		int y = insets.top;
		int w = tabPane.getWidth() - insets.right - insets.left;
		int h = tabPane.getHeight() - insets.top - insets.bottom;

		// remove tabs from bounds
		switch (tabPlacement) {
		case TOP:
		default:
			y += calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
			y -= tabAreaInsets.bottom;
			h -= (y - insets.top);
			break;

		case BOTTOM:
			h -= calculateTabAreaHeight(tabPlacement, runCount, maxTabHeight);
			h += tabAreaInsets.top;
			break;

		case LEFT:
			x += calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
			x -= tabAreaInsets.right;
			w -= (x - insets.left);
			break;

		case RIGHT:
			w -= calculateTabAreaWidth(tabPlacement, runCount, maxTabWidth);
			w += tabAreaInsets.left;
			break;
		}

		// compute insets for separator or full border
		boolean hasFullBorder = clientPropertyBoolean(tabPane, TABBED_PANE_HAS_FULL_BORDER, this.hasFullBorder);
		int sh = scale(contentSeparatorHeight * 100); // multiply by 100 because rotateInsets() does not use floats
		Insets ci = new Insets(0, 0, 0, 0);
		rotateInsets(hasFullBorder ? new Insets(sh, sh, sh, sh) : new Insets(sh, 0, 0, 0), ci, tabPlacement);

		// create path for content separator or full border
		Path2D path = new Path2D.Float(Path2D.WIND_EVEN_ODD);
		path.append(new Rectangle2D.Float(x, y, w, h), false);
		path.append(new Rectangle2D.Float(x + (ci.left / 100f), y + (ci.top / 100f),
				w - (ci.left / 100f) - (ci.right / 100f), h - (ci.top / 100f) - (ci.bottom / 100f)), false);

		// add gap for selected tab to path
		if (getTabType() == TAB_TYPE_CARD) {
			float csh = scale((float) contentSeparatorHeight);

			Rectangle tabRect = getTabBounds(tabPane, selectedIndex);
			Rectangle2D.Float innerTabRect = new Rectangle2D.Float(tabRect.x + csh, tabRect.y + csh,
					tabRect.width - (csh * 2), tabRect.height - (csh * 2));

			// Ensure that the separator outside the tabViewport is present (doesn't get
			// cutoff by the active tab)
			// If left unsolved the active tab is "visible" in the separator (the gap) even
			// when outside the viewport
			if (tabViewport != null)
				Rectangle2D.intersect(tabViewport.getBounds(), innerTabRect, innerTabRect);

			Rectangle2D.Float gap = null;
			if (isHorizontalTabPlacement()) {
				if (innerTabRect.width > 0) {
					float y2 = (tabPlacement == TOP) ? y : y + h - csh;
					gap = (selectedIndex == 0)
							? new Rectangle2D.Float(innerTabRect.x - 1, y2, innerTabRect.width + 1, csh)
							: new Rectangle2D.Float(innerTabRect.x, y2, innerTabRect.width, csh);
				}
			} else {
				if (innerTabRect.height > 0) {
					float x2 = (tabPlacement == LEFT) ? x : x + w - csh;
					gap = new Rectangle2D.Float(x2, innerTabRect.y, csh, innerTabRect.height);
				}
			}

			if (gap != null) {
				path.append(gap, false);

				// fill gap in case that the tab is colored (e.g. focused or hover)
				g.setColor(getTabBackground(tabPlacement, selectedIndex, true));
				((Graphics2D) g).fill(gap);
			}
		}

		// paint content separator or full border
		g.setColor(contentAreaColor);
		((Graphics2D) g).fill(path);
	}

	@Override
	protected void paintTabArea(Graphics g, int tabPlacement, int selectedIndex) {
		var width = tabbedPane.getWidth();
		var fontMetrics = g.getFontMetrics();

		int amount = 0;
		int totalWidth = 0;

		for (int tabIndex = 0; tabIndex < tabbedPane.getTabCount(); tabIndex++) {
			if (tabIndex < tabbedPane.getTabCount() - 1) {

			}

			var tabWidth = calculateTabWidth(JTabbedPane.TOP, tabIndex, fontMetrics);
			if (totalWidth + tabWidth < width - 3) {
				totalWidth += tabWidth;
				amount++;
			} else {
				break;
			}
		}

		this.tabsToShow = amount;

		super.paintTabArea(g, tabPlacement, selectedIndex);
	}

	@Override
	protected void paintTab(Graphics g, int tabPlacement, Rectangle[] rects, int tabIndex, Rectangle iconRect,
			Rectangle textRect) {
		if (tabIndex < tabsToShow) {
			super.paintTab(g, tabPlacement, rects, tabIndex, iconRect, textRect);
		}
	}

	@Override
	protected void installListeners() {
		// Setting super.tabViewport temporarily to null so that
		// code adding mouse wheel listeners to the JTabbedPane
		// doesn't run in the super implementation.
		var tabViewport = super.tabViewport;
		super.tabViewport = null;

		super.installListeners();

		if (tabViewport != null) {
			super.tabViewport = tabViewport;
			super.wheelTabScroller = new CustomWheelTabScroller();
			// ideally we would add the mouse listeners to the viewport, but then the
			// mouse listener of the tabbed pane would not receive events while
			// the mouse pointer is over the viewport
			tabPane.addMouseWheelListener(wheelTabScroller);
			tabPane.addMouseMotionListener(wheelTabScroller);
			tabPane.addMouseListener(wheelTabScroller);
		}

		var abstractMouseListener = new AbstractWidgetMouseListener(widget, inputManager);
		tabbedPane.addMouseListener(abstractMouseListener);
		tabbedPane.addMouseMotionListener(abstractMouseListener);
	}

	@Override
	protected void uninstallListeners() {
		super.uninstallListeners();

		Arrays.stream(tabbedPane.getMouseListeners()).filter(AbstractWidgetMouseListener.class::isInstance)
				.forEach(tabbedPane::removeMouseListener);

		Arrays.stream(tabbedPane.getMouseMotionListeners()).filter(AbstractWidgetMouseListener.class::isInstance)
				.forEach(tabbedPane::removeMouseMotionListener);
	}

	@Override
	protected JButton createMoreTabsButton() {
		moreTabsButton = new CustomMoreTabsButton();
		return moreTabsButton;
	}

	private boolean isLeftToRight() {
		return tabPane.getComponentOrientation().isLeftToRight();
	}

	public class CustomWheelTabScroller extends FlatWheelTabScroller {
		@Override
		public void mouseEntered(MouseEvent e) {
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mousePressed(MouseEvent e) {

		}

		@Override
		public void mouseReleased(MouseEvent e) {

		}

		@Override
		protected void setViewPositionAnimated(Point viewPosition) {
			// Do nothing to stop scrolling from happening
		}
	}

	@SuppressWarnings("serial")
	public class CustomMoreTabsButton extends FlatMoreTabsButton {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (CustomTabbedPaneUI.super.tabViewport == null) {
				return;
			}

			PopupMenu popupMenu = new PopupMenu();
			popupMenu.addPopupMenuListener(this);
			Rectangle viewRect = tabViewport.getViewRect();
			int lastIndex = -1;
			for (int i = 0; i < rects.length; i++) {
				if (!viewRect.contains(rects[i])) {
					if (lastIndex >= 0 && lastIndex + 1 != i)
						lastIndex = i;

					popupMenu.add(createTabMenuItem(i));
				}
			}

			int buttonWidth = getWidth();
			int buttonHeight = getHeight();
			Dimension popupSize = popupMenu.getPreferredSize();

			int x = isLeftToRight() ? buttonWidth - popupSize.width : 0;
			int y = buttonHeight - popupSize.height;
			switch (tabPane.getTabPlacement()) {
			default:
			case TOP:
				y = buttonHeight;
				break;
			case BOTTOM:
				y = -popupSize.height;
				break;
			case LEFT:
				x = buttonWidth;
				break;
			case RIGHT:
				x = -popupSize.width;
				break;
			}

			popupMenu.show(this, x, y);
		}

		protected MenuItem createTabMenuItem(int tabIndex) {
			String title = tabPane.getTitleAt(tabIndex);
			if (StringUtils.isEmpty(title)) {
				Component tabComp = tabPane.getTabComponentAt(tabIndex);
				if (tabComp != null)
					title = findTabTitle(tabComp);
				if (StringUtils.isEmpty(title))
					title = tabPane.getAccessibleContext().getAccessibleChild(tabIndex).getAccessibleContext()
							.getAccessibleName();
				if (StringUtils.isEmpty(title) && tabComp instanceof Accessible)
					title = findTabTitleInAccessible((Accessible) tabComp);
				if (StringUtils.isEmpty(title))
					title = (tabIndex + 1) + ". Tab";
			}

			MenuItem menuItem = new MenuItem(title);
			var icon = tabPane.getIconAt(tabIndex);
			if (icon != null) {
				menuItem.setIcon(icon);
			}

			menuItem.setDisabledIcon(tabPane.getDisabledIconAt(tabIndex));
			menuItem.setToolTipText(tabPane.getToolTipTextAt(tabIndex));

			menuItem.addActionListener(e -> selectTab(tabIndex));
			return menuItem;
		}

		private String findTabTitle(Component c) {
			String title = null;
			if (c instanceof JLabel)
				title = ((JLabel) c).getText();
			else if (c instanceof JTextComponent)
				title = ((JTextComponent) c).getText();

			if (!StringUtils.isEmpty(title))
				return title;

			if (c instanceof Container) {
				for (Component child : ((Container) c).getComponents()) {
					title = findTabTitle(child);
					if (title != null)
						return title;
				}
			}

			return null;
		}

		private String findTabTitleInAccessible(Accessible accessible) {
			AccessibleContext context = accessible.getAccessibleContext();
			if (context == null)
				return null;

			String title = context.getAccessibleName();
			if (!StringUtils.isEmpty(title))
				return title;

			int childrenCount = context.getAccessibleChildrenCount();
			for (int i = 0; i < childrenCount; i++) {
				title = findTabTitleInAccessible(context.getAccessibleChild(i));
				if (title != null)
					return title;
			}

			return null;
		}
	}

	public int getTabAreaHeight() {
		return super.calculateTabAreaHeight(JTabbedPane.TOP, super.runCount, super.maxTabHeight);
	}

	public int getTabIndexAt(int targetX) {
		var tabCount = tabbedPane.getTabCount();
		if (tabCount <= 0) {
			return -1;
		}

		var left = 0;
		var tabIndex = 0;
		while (true) {
			var tabWidth = super.calculateTabWidth(JTabbedPane.TOP, tabIndex, super.getFontMetrics());
			var right = left + tabWidth;
			if (targetX >= left && targetX < right) {
				return tabIndex;
			}

			if (tabIndex == tabCount - 1) {
				return -1;
			}

			tabIndex++;
			left = right;
		}
	}

	public boolean isInsideTabArea(int x, int y) {
		var tabAreaHeight = getTabAreaHeight();
		return x >= 0 && x < tabbedPane.getWidth() && y >= 0 && y < tabAreaHeight;
	}

	public Section getSectionFor(int x, int y) {
		if (isInsideTabArea(x, y)) {
			return Section.TAB_AREA;
		}

		var partition = WidgetSpacePartition.of(widget);

		if (partition.orientation() == Orientation.HORIZONTAL) {
			if (x >= partition.leftThirdX() && x < partition.leftThirdX() + partition.leftThirdWidth()) {
				return Section.LEFT_HALF;
			} else if (x >= partition.rightThirdX() && x < partition.rightThirdX() + partition.rightThirdWidth()) {
				return Section.RIGHT_HALF;
			}

			return (y < partition.bottomHalfY()) ? Section.TOP_HALF : Section.BOTTOM_HALF;
		}

		if (y >= partition.topThirdY() && y < partition.topThirdY() + partition.topThirdHeight()) {
			return Section.TOP_HALF;
		} else if (y >= partition.bottomThirdY() && y < partition.bottomThirdY() + partition.bottomThirdHeight()) {
			return Section.BOTTOM_HALF;
		}

		return (x < partition.rightHalfX()) ? Section.LEFT_HALF : Section.RIGHT_HALF;
	}

	public void setMouseCoordinates(int x, int y) {
		this.hovered = true;
		this.mouseX = x;
		this.mouseY = y;
	}

	public void clearHover() {
		this.hovered = false;
	}

	public void recalculateSpacePartition() {
		spacePartition = WidgetSpacePartition.of(widget);
	}

}
