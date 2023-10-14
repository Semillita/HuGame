package dev.hugame.ui;

import dev.hugame.core.HuGame;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.Textures;

public class Slider extends Button {

	private Texture background, thumb;
	private float value = 0.5f;
	
	public Slider(Texture baseTexture, Texture thumbTexture) {
		this.background = baseTexture;
		this.thumb = thumbTexture;
	}
	
	public void render(Batch batch) {
		batch.draw(background, 0, -200, 400, 100);
		batch.draw(thumb, (int) (value * 400) - 20, -210, 40, 120);
	}
	
	@Override
	public void mouseMoved(int x, int y) {
		super.mouseMoved(x, y);
		if (super.isPressed()) {
			value = Math.min(1, Math.max(0, (float) (x - 0) / 400));
		}
	}
	
	@Override
	public void mouseDown() {
		var pos = super.screenToWorldCoordinates.get().apply(HuGame.getInput().getMousePosition());
		int x = pos.x;
		int y = pos.y;
		super.mouseDown();
		if (super.isInside(x, y)) {
			value = (float) (x - 0) / 400;
		}
	}
	
	@Override
	public boolean isInside(int x, int y) {
		return x > 0 && x < 400 && y > -200 && y < -100;
	}
	
}
