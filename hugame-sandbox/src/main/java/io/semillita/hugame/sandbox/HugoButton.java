package io.semillita.hugame.sandbox;

import dev.hugame.graphics.GLBatch;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.Textures;
import dev.hugame.ui.Button;

public class HugoButton extends Button {

	private final Texture texture;
	private final Texture hover;
	
	public HugoButton() {
		texture = Textures.get("/button.png");
		hover = Textures.get("/hover.png");
	}
	
	public void render(GLBatch batch) {
		batch.draw(texture, 0, 0, 300, 100);
		if (super.isHovered()) {
			batch.draw(hover, 0, 0, 300, 100);
		}
		if (super.isPressed()) {
			batch.draw(hover, 0, 0, 300, 100);
		}
	}
	
	@Override
	public boolean isInside(int x, int y) {
		return x >= 0 && x < 300 && y >= 0 && y < 100;
	}
	
	@Override
	public void onPressed(int x, int y) {
		System.out.println("Button pressed");
	}
}
