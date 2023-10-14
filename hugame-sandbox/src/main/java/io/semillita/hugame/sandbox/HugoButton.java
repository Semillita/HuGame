package io.semillita.hugame.sandbox;

import dev.hugame.desktop.gl.GLBatch;
import dev.hugame.desktop.gl.GLTexture;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.Textures;
import dev.hugame.ui.Button;

public class HugoButton extends Button {

	private final Texture texture;
	private final Texture hover;
	
	public HugoButton(Texture baseTexture, Texture hoverTexture) {
		this.texture = baseTexture;
		this.hover = hoverTexture;

		/*texture = Textures.get("/button.png");
		hover = Textures.get("/hover.png");*/
	}
	
	public void render(GLBatch batch) {
		batch.draw(texture, 0, 200, 300, 100);
		if (super.isHovered()) {
			batch.draw(hover, 0, 200, 300, 100);
		}
		if (super.isPressed()) {
			batch.draw(hover, 0, 200, 300, 100);
		}
	}
	
	@Override
	public boolean isInside(int x, int y) {
		return x >= 0 && x < 300 && y >= 200 && y < 300;
	}
	
	@Override
	public void onPressed(int x, int y) {
		System.out.println("Button pressed");
	}
}
