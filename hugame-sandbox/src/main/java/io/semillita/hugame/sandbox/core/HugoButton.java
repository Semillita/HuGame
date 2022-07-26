package io.semillita.hugame.sandbox.core;

import io.semillita.hugame.graphics.Batch;
import io.semillita.hugame.graphics.Texture;
import io.semillita.hugame.graphics.Textures;
import io.semillita.hugame.ui.Button;

public class HugoButton extends Button {

	private final Texture texture;
	private final Texture hover;
	
	public HugoButton() {
		texture = Textures.get("/button.png");
		hover = Textures.get("/hover.png");
	}
	
	public void render(Batch batch) {
		batch.drawQuad(texture, 0, 0, 300, 100);
		if (super.isHovered()) {
			batch.drawQuad(hover, 0, 0, 300, 100);
		}
	}
	
	@Override
	public boolean isInside(int x, int y) {
		return x >= 0 && x < 300 && y >= 0 && y < 100;
	}
}