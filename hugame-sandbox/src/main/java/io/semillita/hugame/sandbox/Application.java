package io.semillita.hugame.sandbox;

import java.awt.Dimension;

import dev.hugame.assimp.AssimpModelLoader;
import dev.hugame.io.FileHandle;
import dev.hugame.io.FileLocation;
import org.joml.Vector2f;
import org.joml.Vector3f;

import dev.hugame.core.ApplicationListener;
import dev.hugame.core.HuGame;
import dev.hugame.core.Input;
import dev.hugame.core.Renderer;
import dev.hugame.core.Window;
import dev.hugame.desktop.gl.DesktopGLContext;
import dev.hugame.desktop.gl.GLBatch;
import dev.hugame.environment.DirectionalLight;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;
import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.Shader;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.Textures;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.Materials;
import dev.hugame.graphics.model.Model;
import dev.hugame.inject.Inject;
import dev.hugame.input.Key;
import dev.hugame.ui.Slider;
import dev.hugame.util.Transform;
import dev.hugame.window.WindowConfiguration;

public class Application extends ApplicationListener {

	public static void main(String[] args) {
		HuGame.start(
				new DesktopGLContext(
						new WindowConfiguration().width(960).height(540).title("App").x(500).y(300).decorated(true)),
				new Application());
	}

	private Model cubeModel;

	private Texture[] grassSides;

	private Model playerModel;
	private Transform playerTransform;

	Material blueMat;
	Material redMat;
	Material greenMat;
	Material whiteMat;
	Material donutMat;

	float playerX = 0, playerY = 0, playerZ = 0;

	private GLBatch batch;
	private Camera2D camera2D;
	private Shader shader;

	private HugoButton button;
	private Slider slider;

	private Environment environment;

	private Model model;

	@Inject
	Window window;
	@Inject
	Input input;
	@Inject
	Renderer renderer;

	private long lastNano;

	private Texture groundTexture;

	@Override
	public void onCreate() {
		System.out.println("Sandbox onCreate");
		HuGame.inject(this);
		playerTransform = new Transform(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0),
				new Vector3f(1, 1, 1f));

		batch = new GLBatch();
		HuGame.inject(batch);
		camera2D = new Camera2D(new Vector2f(960, 540), new Dimension(1920, 1080));
		shader = GLBatch.getDefaultShader();

		button = new HugoButton();
		button.setScreenToWorldCoordinateMapping(camera2D::screenToWorldCoords);

		slider = new Slider();
		slider.setScreenToWorldCoordinateMapping(camera2D::screenToWorldCoords);

		input.setMouseButtonListener((event) -> {
			switch (event.action()) {
			case PRESS:
				button.mouseDown();
				slider.mouseDown();
				break;
			case RELEASE:
				button.mouseUp();
				slider.mouseUp();
			}
		});

		window.setResizeListener((width, height) -> {
//			System.out.println(width + ", " + height);
			camera2D.updateViewport();
			camera2D.update();
		});

		environment = new Environment();
		var pointLight1 = new PointLight(new Vector3f(5, 5, 5), new Vector3f(1, 1, 1), 5);
		var spotLight1 = new SpotLight(new Vector3f(0, 4, 0), new Vector3f(-1f, -1, 0), new Vector3f(1, 1, 0), 2, 0.5f);
		var directionalLight1 = new DirectionalLight(new Vector3f(-1f, -1f, -1f), new Vector3f(1, 1, 1), 0.5f);
		environment.add(pointLight1);
		environment.add(spotLight1);
		environment.add(directionalLight1);
		renderer.updateEnvironment(environment);

		input.setKeyListener((key, action) -> {
//			System.out.println(key);
		});

		blueMat = Materials.get(new Vector3f(0, 0, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), 0.5f, -1, -1, -1,
				-1, -1, -1);

		var modelFile = new FileHandle("deccer_cubes_tex.fbx", FileLocation.INTERNAL);
		var resolvedModel = new AssimpModelLoader().load(modelFile);
		model = new Model(resolvedModel.orElseThrow());
		/*System.out.println("Model has " + assimpModel.meshes().size() + " meshes and " + assimpModel.materials().size()
				+ " materials");*/
		groundTexture = Textures.get("/ground.png");
	}

	@Override
	public void onRender() {
		System.out.println("onRender start");
		var currentNano = System.nanoTime();
		var elapsed = (currentNano - lastNano) / 1_000_000_000d;
		lastNano = currentNano;

//<<<<<<< HEAD
//		List<Transform> cubeTransforms = new ArrayList<>();
//
//		for (int x = (int) playerX - 12; x < playerX + 12; x++) {
//			for (int z = (int) playerZ - 12; z < playerZ + 12; z++) {
//				for (int y = -6; y < 1; y++) {
//					var dis = Math.sqrt(Math.pow(x - playerX, 2) + Math.pow(z - playerZ, 2) + Math.pow(y - 0, 2));
//					if (dis <= 9) {
//						cubeTransforms.add(
//								new Transform(new Vector3f(x, y, z), new Vector3f(0, 0, 0), new Vector3f(1, 1, 1)));
//					}
//				}
//			}
//		}
//
//		for (var transform : cubeTransforms) {
////			renderer.draw(donut, transform, whiteMat);
//		}
////		renderer.draw(playerModel, playerTransform, whiteMat);
//=======
		renderer.draw(model, playerTransform);

		// renderer.draw(model, playerTransform);

		renderer.flush();

		// cubeTransforms.clear();

//		var input = HuGame.getInput();
		if (input.isKeyPressed(Key.A))
			playerX -= 1f;
		if (input.isKeyPressed(Key.D))
			playerX += 1f;
		if (input.isKeyPressed(Key.W))
			playerZ -= 1f;
		if (input.isKeyPressed(Key.S))
			playerZ += 1f;

		var camera = renderer.getCamera();
		var cameraPos = camera.getPosition();
		if (input.isKeyPressed(Key.LEFT))
			cameraPos.x -= 1f;
		if (input.isKeyPressed(Key.RIGHT))
			cameraPos.x += 1f;
		if (input.isKeyPressed(Key.UP))
			cameraPos.z -= 1f;
		if (input.isKeyPressed(Key.DOWN))
			cameraPos.z += 1f;
		if (input.isKeyPressed(Key.SPACE))
			cameraPos.y += 1f;
		if (input.isKeyPressed(Key.ENTER))
			cameraPos.y -= 1f;
		camera.setPosition(cameraPos);
		camera.update();

		playerTransform.position.x = playerX;
		playerTransform.position.z = playerZ;
		playerTransform.update();

		// Render 2D
		//batch.setCamera(camera2D);
		//batch.setShader(shader);
		//batch.begin();

//		button.update();
//		slider.update();
//		button.render(batch);
//		slider.render(batch);
		// batch.draw(groundTexture, 0, 0, 100, 100);

		//batch.end();
		System.out.println("onRender stop");
	}

	@Override
	public boolean onClose() {
		return true;
	}

}