package io.semillita.hugame.sandbox;

import dev.hugame.application.HuGameApplicationContext;
import dev.hugame.application.SimpleApplicationConfiguration;
import dev.hugame.application.SimpleApplicationListener;
import dev.hugame.application.SimpleHuGameApplication;
import dev.hugame.assimp.AssimpModelLoader;
import dev.hugame.core.Input;
import dev.hugame.core.Renderer;
import dev.hugame.core.Window;
import dev.hugame.desktop.gl.GLBatch;
import dev.hugame.desktop.gl.GLGraphics;
import dev.hugame.desktop.gl.GLRenderer;
import dev.hugame.environment.DirectionalLight;
import dev.hugame.environment.Environment;
import dev.hugame.environment.PointLight;
import dev.hugame.environment.SpotLight;
import dev.hugame.graphics.Camera2D;
import dev.hugame.graphics.Shader;
import dev.hugame.graphics.Texture;
import dev.hugame.graphics.material.Material;
import dev.hugame.graphics.material.Materials;
import dev.hugame.graphics.model.Model;
import dev.hugame.input.Key;
import dev.hugame.io.FileHandle;
import dev.hugame.io.FileLocation;
import dev.hugame.model.spec.ModelLoader;
import dev.hugame.ui.Slider;
import dev.hugame.util.TextureLoader;
import dev.hugame.util.Transform;
import dev.hugame.window.DesktopInput;
import dev.hugame.window.DesktopWindow;
import dev.hugame.window.WindowConfiguration;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL;

import java.awt.*;
import java.util.List;
import java.util.function.Supplier;

import static org.lwjgl.opengl.GL45.*;

public class Application implements SimpleApplicationListener {
	public static void main(String[] args) {
		var windowConfig = new WindowConfiguration()
				.width(960)
				.height(540)
				.title("App")
				.x(500)
				.y(300)
				.decorated(true);

		Supplier<SimpleApplicationConfiguration> configurer = () -> {
			var window = new DesktopWindow(windowConfig, (width, height) -> glViewport(0, 0, width, height));
			var graphics = new GLGraphics();
			var input = new DesktopInput(window);

			var assimpModelLoader = new AssimpModelLoader(new TextureLoader(graphics));

			return new SimpleApplicationConfiguration(graphics, window, input, List.of(assimpModelLoader));
		};

		var application = new SimpleHuGameApplication(new Application(), configurer);
		application.start();
	}

	private Transform playerTransform;

	Material blueMat;

	float playerX = 0, playerY = 0, playerZ = 0;

	private GLBatch batch;
	private Camera2D camera2D;
	private Shader shader;

	private HugoButton button;
	private Slider slider;

	private Environment environment;

	private Model model;

	private Window window;
	private Input input;
	private Renderer renderer;
	private ModelLoader modelLoader;

	private long lastNano;

	private Texture groundTexture;
	private TextureLoader textureLoader;

	@Override
	public void onCreate(HuGameApplicationContext applicationContext) {
		this.window = applicationContext.getWindow();
		this.input = applicationContext.getInput();

		var graphics = applicationContext.getGraphics();
		this.renderer = graphics.getRenderer();

		this.modelLoader = applicationContext.getModelLoader();

		textureLoader = new TextureLoader(graphics);

		playerTransform = new Transform(new Vector3f(0, 0, 0), new Vector3f(0, 0, 0),
				new Vector3f(1, 1, 1f));

		// TODO: Change into graphics.createBatch()
		batch = new GLBatch((GLRenderer) renderer);
		//HuGame.inject(batch);
		camera2D = new Camera2D(window, new Vector2f(960, 540), new Dimension(1920, 1080));
		shader = GLBatch.getDefaultShader();

		button = new HugoButton(textureLoader.get("/button.png"), textureLoader.get("/hover.png"));
		button.setScreenToWorldCoordinateMapping(camera2D::screenToWorldCoords);

		slider = new Slider(textureLoader.get("/slider_background.png"), textureLoader.get("/slider_thumb.png"));
		slider.setScreenToWorldCoordinateMapping(camera2D::screenToWorldCoords);

		input.setMouseButtonListener((event) -> {
			switch (event.action()) {
				case PRESS -> {
					button.mouseDown();
					slider.mouseDown();
				}
				case RELEASE -> {
					button.mouseUp();
					slider.mouseUp();
				}
			}
		});

		window.setResizeListener((width, height) -> {
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

		blueMat = Materials.get(new Vector3f(0, 0, 1), new Vector3f(1, 1, 1), new Vector3f(1, 1, 1), 0.5f, -1, -1, -1,
				-1, -1, -1);

		var modelFile = new FileHandle("deccer_cubes_tex.fbx", FileLocation.INTERNAL);
		var resolvedModel = modelLoader.load(modelFile);

		model = graphics.createModel(resolvedModel.orElseThrow());
		groundTexture = textureLoader.get("/ground.png");
	}

	@Override
	public void onRender() {
		renderer.draw(model, playerTransform);

		renderer.flush();

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
	}

	@Override
	public boolean shouldClose() {
		return true;
	}

	@Override
	public void onDestroy() {
		System.out.println("Destroying...");
	}
}
