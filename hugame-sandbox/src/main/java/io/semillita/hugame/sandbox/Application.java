package io.semillita.hugame.sandbox;

import java.awt.Dimension;
import java.util.List;
import java.util.ArrayList;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import dev.hugame.core.ApplicationListener;
import dev.hugame.core.HuGame;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.graphics.Batch;
import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.Model;
import dev.hugame.graphics.ModelBuilder;
import dev.hugame.graphics.Renderer;
import dev.hugame.graphics.Shader;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.Textures;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.MaterialCreateInfo;
import dev.hugame.graphics.material.Materials;
import dev.hugame.inject.Inject;
import dev.hugame.input.Key;
import dev.hugame.ui.Slider;
import dev.hugame.util.Transform;
import dev.hugame.window.WindowConfiguration;

public class Application extends ApplicationListener {

	public static void main(String[] args) {
		HuGame.start(new Application(),
				new WindowConfiguration().width(960).height(540).title("Hugo").x(500).y(300).decorated(true));
	}

	private Model cubeModel;

	private Texture[] grassSides;

	private Model playerModel;
	private Transform playerTransform;

	Material blueMat;
	Material redMat;
	Material greenMat;
	Material whiteMat;

	float playerX = 0, playerZ = 0;

	private Batch batch;
	private Camera2D camera2D;
	private Shader shader;

	private HugoButton button;
	private Slider slider;

	private Environment environment;

	private boolean firstFrame = true;

	@Inject
	Repository repository;

	@Inject
	Service service;

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

		builder.cube(grassSides[0], grassSides[1], grassSides[2], grassSides[3], grassSides[4], grassSides[5]);
		cubeModel = builder.generate();

		Texture side = Textures.get("/grass_bottom.png");
		builder.cube(side, side, side, side, side, side);
		playerModel = builder.generate();
		playerTransform = new Transform(new Vector3f(playerX, 1, playerZ), new Vector3f(0, 0, 0),
				new Vector3f(1, 2, 1));

		redMat = Materials.get(new MaterialCreateInfo(new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)));
		blueMat = Materials.get(new MaterialCreateInfo(new Vector4f(0.0f, 0.0f, 1.0f, 1.0f)));
		greenMat = Materials.get(new MaterialCreateInfo(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f)));
		whiteMat = Materials.get(new MaterialCreateInfo(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)));

//		renderer = new Renderer();

		batch = new Batch();
		camera2D = new Camera2D(new Vector2f(960, 540), new Dimension(1920, 1080));
		shader = Batch.getDefaultShader();

		button = new HugoButton();
		button.setScreenToWorldCoordinateMapping(camera2D::screenToWorldCoords);

		slider = new Slider();
		slider.setScreenToWorldCoordinateMapping(camera2D::screenToWorldCoords);

		HuGame.getInput().setMousePressCallback((mouseButton) -> {
			System.out.println("Mouse press");
			System.out.println(HuGame.getInput().getMousePosition().x);
			button.mouseDown();
			slider.mouseDown();

			camera2D.update();
		});
		HuGame.getInput().setMouseReleaseCallback((mouseButton) -> {
			System.out.println("Mouse release");
			button.mouseUp();
			slider.mouseUp();
		});

		HuGame.getWindow().setResizeListener((width, height) -> {
			System.out.println(width + ", " + height);
			camera2D.updateViewport();
			camera2D.update();
		});

		environment = new Environment();
		var pointLight1 = new PointLight(new Vector3f(2, 4, 2), new Vector3f(1, 1, 1));
		environment.add(pointLight1);
	}

	@Override
	public void onRender() {
		final Renderer renderer = HuGame.getRenderer();
		if (firstFrame) {
			renderer.updateEnvironment(environment);
			firstFrame = false;
			HuGame.inject(this);
		}

		List<Transform> cubeTransforms = new ArrayList<>();

		for (int x = (int) playerX - 12; x < playerX + 12; x++) {
			for (int z = (int) playerZ - 12; z < playerZ + 12; z++) {
				for (int y = -6; y < 1; y++) {
					var dis = Math.sqrt(Math.pow(x - playerX, 2) + Math.pow(z - playerZ, 2) + Math.pow(y - 0, 2));
					if (dis <= 9) {
						cubeTransforms.add(
								new Transform(new Vector3f(x, y, z), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)));
					}
				}
			}
		}

		for (var transform : cubeTransforms) {
			renderer.draw(cubeModel, transform, whiteMat);
		}
		renderer.draw(playerModel, playerTransform, whiteMat);

		renderer.renderModels();

		cubeTransforms.clear();

		var input = HuGame.getInput();
		if (input.isKeyPressed(Key.A))
			playerX -= 0.05f;
		if (input.isKeyPressed(Key.D))
			playerX += 0.05f;
		if (input.isKeyPressed(Key.W))
			playerZ -= 0.05f;
		if (input.isKeyPressed(Key.S))
			playerZ += 0.05f;

		var camera = renderer.getCamera();
		var cameraPos = camera.getPosition();
		if (input.isKeyPressed(Key.LEFT))
			cameraPos.x -= 0.05f;
		if (input.isKeyPressed(Key.RIGHT))
			cameraPos.x += 0.05f;
		if (input.isKeyPressed(Key.UP))
			cameraPos.z -= 0.05f;
		if (input.isKeyPressed(Key.DOWN))
			cameraPos.z += 0.05f;
		camera.setPosition(cameraPos);
		camera.update();

		playerTransform.position.x = playerX;
		playerTransform.position.z = playerZ;
		playerTransform.update();

		// Render 2D
		batch.setCamera(camera2D);
		batch.setShader(shader);
		batch.begin();

		button.update();
		slider.update();
		button.render(batch);
		slider.render(batch);
		// batch.drawQuad(groundTexture, 0, 0, 1920, 1080);

		batch.end();
	}

	@Override
	public boolean onClose() {
		return true;
	}

}