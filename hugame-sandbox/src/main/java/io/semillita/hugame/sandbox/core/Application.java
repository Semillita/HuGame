package io.semillita.hugame.sandbox.core;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import io.semillita.hugame.core.ApplicationListener;
import io.semillita.hugame.core.HuGame;
import io.semillita.hugame.graphics.Batch;
import io.semillita.hugame.graphics.Camera;
import io.semillita.hugame.graphics.Model;
import io.semillita.hugame.graphics.ModelBuilder;
import io.semillita.hugame.graphics.OrthographicCamera;
import io.semillita.hugame.graphics.Renderer;
import io.semillita.hugame.graphics.Shader;
import io.semillita.hugame.graphics.Texture;
import io.semillita.hugame.graphics.Textures;
import io.semillita.hugame.graphics.material.Material;
import io.semillita.hugame.graphics.material.MaterialCreateInfo;
import io.semillita.hugame.graphics.material.Materials;
import io.semillita.hugame.input.Key;
import io.semillita.hugame.util.Transform;
import io.semillita.hugame.window.WindowConfiguration;

public class Application extends ApplicationListener {

	public static void main(String[] args) {
		var app = new Application();

		HuGame.start(new Application(), new WindowConfiguration().width(960).height(540).title("Hugo").x(500).y(300).decorated(true));
	}

//	private Renderer renderer;
	private Model cubeModel;
	
	private Texture[] grassSides;
	private Texture groundTexture;
	
	private Model playerModel;
	private Transform playerTransform;
	
	Material blockMat;
	Material playerMat;
	
	float playerX = 0, playerZ = 0;
	
	private Batch batch;
	private Camera camera2D;
	private Shader shader;
	
	@Override
	public void onCreate() {
		ModelBuilder builder = new ModelBuilder();
		
		grassSides = new Texture[6];
		grassSides[0] = Textures.get("/grass_top.png");
		grassSides[1] = Textures.get("/grass_bottom.png");
		grassSides[2] = Textures.get("/grass_front.png");
		grassSides[3] = Textures.get("/grass_back.png");
		grassSides[4] = Textures.get("/grass_left.png");
		grassSides[5] = Textures.get("/grass_right.png");
		
		groundTexture = Textures.get("/ground.png");
		
		builder.cube(grassSides[0], grassSides[1], grassSides[2], grassSides[3], grassSides[4], grassSides[5]);
		cubeModel = builder.generate();
		
		Texture side = Textures.get("/grass_bottom.png");
		builder.cube(side, side, side, side, side, side);
		playerModel = builder.generate();
		playerTransform = new Transform(new Vector3f(playerX, 1, playerZ), new Vector3f(0, 0, 0), new Vector3f(1, 2, 1));
		
		blockMat = Materials.get(new MaterialCreateInfo(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f)));
		playerMat = Materials.get(new MaterialCreateInfo(new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)));
//		renderer = new Renderer();
		
		batch = new Batch();
		camera2D = new OrthographicCamera(new Vector2f(0, 0));
		shader = new Shader("/shaders/batch_shader.glsl");
		shader.compile();
	}

	@Override
	public void onRender() {
		final Renderer renderer = HuGame.getRenderer();

		var camera = renderer.getCamera();
		camera.position.x += 1;
		
		List<Transform> cubeTransforms = new ArrayList<>();
		
		for (int x = (int) playerX - 12; x < playerX + 12; x++) {
			for (int z = (int) playerZ - 12; z < playerZ + 12; z++) {
				for (int y = -6; y < 1; y++) {
					var dis = Math.sqrt(Math.pow(x - playerX, 2) + Math.pow(z - playerZ, 2) + Math.pow(y - 0, 2));
					if (dis <= 9) {
						cubeTransforms.add(new Transform(new Vector3f(x, y, z), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)));
					}
				}
			}
		}
		
		for (var transform : cubeTransforms) {
			renderer.draw(cubeModel, transform);
		}
		//renderer.draw(cubeModel, transforms[0]);
		//renderer.draw(groundModel, groundTransform);
		renderer.draw(playerModel, playerTransform);
		renderer.renderModels();
		
		cubeTransforms.clear();
		
		var input = HuGame.getInput();
		if (input.isKeyPressed(Key.A)) playerX -= 0.05f;
		if (input.isKeyPressed(Key.D)) playerX += 0.05f;
		if (input.isKeyPressed(Key.W)) playerZ -= 0.05f;
		if (input.isKeyPressed(Key.S)) playerZ += 0.05f;
		
		playerTransform.position.x = playerX;
		playerTransform.position.z = playerZ;
		playerTransform.update();
		
		// Render 2D
		batch.useCamera(camera2D);
		batch.submitShader(shader);
		batch.begin();
		
		for (int i = 0; i < 10; i++) {
			batch.drawQuad(groundTexture, i * 100, i * 100, 100, 100);
		}
		
		for (int i = 0; i < 10; i++) {
			batch.drawQuad(groundTexture, 200 + i * 100, i * 100, 100, 100);
		}
		
		for (int i = 0; i < 10; i++) {
			batch.drawQuad(groundTexture, 400 + i * 100, i * 100, 100, 100);
		}
		
		for (int i = 0; i < 10; i++) {
			batch.drawQuad(groundTexture, 600 + i * 100, i * 100, 100, 100);
		}
		
		batch.end();
	}

	@Override
	public boolean onClose() {
		return true;
	}

}