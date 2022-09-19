package dev.hugame.ui;

import java.awt.Point;
import java.util.function.Function;

public interface GUIElement {

	public void setScreenToWorldCoordinateMapping(Function<Point, Point> mapping);
	
	public void mouseDown();
	
	public void mouseUp();
	
	public void update();
	
}
