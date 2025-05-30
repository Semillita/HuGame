package dev.hugame.window;

import static org.lwjgl.glfw.GLFW.*;

import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import dev.hugame.core.HuGame;
import dev.hugame.core.Input;
import dev.hugame.core.Window;
import dev.hugame.inject.Inject;
import dev.hugame.input.Key;

// TODO: Make the window implementation create the input implementation.
public class DesktopInput implements Input {

	private final Map<Key, Boolean> pressedKeys;

	private Point mousePosition;
	private Optional<Consumer<MouseEvent>> maybeMouseButtonListener;
	private Optional<Consumer<Integer>> maybeMousePressListener;
	private Optional<Consumer<Integer>> maybeMouseReleaseListener;
	private Optional<BiConsumer<Key, KeyAction>> maybeKeyListener = Optional.empty();

	private final Window window;

	public DesktopInput(DesktopWindow window) {
		this.window = window;
		var windowHandle = window.getHandle();

		pressedKeys = new HashMap<>();
		mousePosition = new Point(0, 0);
		maybeMousePressListener = Optional.empty();
		maybeMouseReleaseListener = Optional.empty();

		glfwSetKeyCallback(windowHandle, this::keyCallback);
		glfwSetCursorPosCallback(windowHandle, this::mouseMoveCallback);
		glfwSetMouseButtonCallback(windowHandle, this::mouseButtonCallback);
	}

	@Override
	public Point getMousePosition() {
		return mousePosition;
	}

	@Override
	public boolean isKeyPressed(Key key) {
		return pressedKeys.getOrDefault(key, false);
	}

	@Override
	public void acceptMousePosition(BiConsumer<Integer, Integer> consumer) {
		consumer.accept(mousePosition.x, mousePosition.y);
	}

	@Override
	public void setMouseButtonListener(Consumer<MouseEvent> listener) {
		
	}
	
	@Override
	public void setKeyListener(BiConsumer<Key, KeyAction> listener) {
		this.maybeKeyListener = Optional.ofNullable(listener);
	}

	
	private void keyCallback(long window, int keyCode, int scancode, int action, int mods) {
		var key = getKey(keyCode);
		var keyAction = getKeyAction(action);

		if (keyAction == null) return;

		switch (keyAction) {
			case PRESS -> pressedKeys.put(key, true);
			case RELEASE -> pressedKeys.put(key, false);
		}
		
		maybeKeyListener.ifPresent(listener -> listener.accept(key, keyAction));
	}

	private KeyAction getKeyAction(int action) {
		return switch (action) {
		case GLFW_PRESS -> KeyAction.PRESS;
		case GLFW_RELEASE -> KeyAction.RELEASE;
		default -> null;
		};
	}

	private void mouseMoveCallback(long windowHandle, double x, double y) {
		mousePosition = new Point((int) x, window.getHeight() - 1 - ((int) y));
	}

	private void mouseButtonCallback(long window, int button, int action, int mods) {
		switch (action) {
			case GLFW_PRESS -> maybeMousePressListener.ifPresent(listener -> listener.accept(button));
			case GLFW_RELEASE -> maybeMouseReleaseListener.ifPresent(listener -> listener.accept(button));
		}
	}

	private Key getKey(int glfwKeyCode) {
		switch (glfwKeyCode) {
		case GLFW_KEY_SPACE:
			return Key.SPACE;
//		 case GLFW_KEY_APOSTROPHE:
//		 return Key.APOSTROPHE;
		case GLFW_KEY_COMMA:
			return Key.COMMA;
		case GLFW_KEY_MINUS:
			return Key.MINUS;
		case GLFW_KEY_PERIOD:
			return Key.PERIOD;
		case GLFW_KEY_SLASH:
			return Key.SLASH;
		case GLFW_KEY_0:
			return Key.NUM_0;
		case GLFW_KEY_1:
			return Key.NUM_1;
		case GLFW_KEY_2:
			return Key.NUM_2;
		case GLFW_KEY_3:
			return Key.NUM_3;
		case GLFW_KEY_4:
			return Key.NUM_4;
		case GLFW_KEY_5:
			return Key.NUM_5;
		case GLFW_KEY_6:
			return Key.NUM_6;
		case GLFW_KEY_7:
			return Key.NUM_7;
		case GLFW_KEY_8:
			return Key.NUM_8;
		case GLFW_KEY_9:
			return Key.NUM_9;
		case GLFW_KEY_SEMICOLON:
			return Key.SEMICOLON;
		case GLFW_KEY_EQUAL:
			return Key.EQUALS;
		case GLFW_KEY_A:
			return Key.A;
		case GLFW_KEY_B:
			return Key.B;
		case GLFW_KEY_C:
			return Key.C;
		case GLFW_KEY_D:
			return Key.D;
		case GLFW_KEY_E:
			return Key.E;
		case GLFW_KEY_F:
			return Key.F;
		case GLFW_KEY_G:
			return Key.G;
		case GLFW_KEY_H:
			return Key.H;
		case GLFW_KEY_I:
			return Key.I;
		case GLFW_KEY_J:
			return Key.J;
		case GLFW_KEY_K:
			return Key.K;
		case GLFW_KEY_L:
			return Key.L;
		case GLFW_KEY_M:
			return Key.M;
		case GLFW_KEY_N:
			return Key.N;
		case GLFW_KEY_O:
			return Key.O;
		case GLFW_KEY_P:
			return Key.P;
		case GLFW_KEY_Q:
			return Key.Q;
		case GLFW_KEY_R:
			return Key.R;
		case GLFW_KEY_S:
			return Key.S;
		case GLFW_KEY_T:
			return Key.T;
		case GLFW_KEY_U:
			return Key.U;
		case GLFW_KEY_V:
			return Key.V;
		case GLFW_KEY_W:
			return Key.W;
		case GLFW_KEY_X:
			return Key.X;
		case GLFW_KEY_Y:
			return Key.Y;
		case GLFW_KEY_Z:
			return Key.Z;
		case GLFW_KEY_LEFT_BRACKET:
			return Key.LEFT_BRACKET;
		case GLFW_KEY_BACKSLASH:
			return Key.BACKSLASH;
		case GLFW_KEY_RIGHT_BRACKET:
			return Key.RIGHT_BRACKET;
		case GLFW_KEY_GRAVE_ACCENT:
			return Key.GRAVE;
		case GLFW_KEY_WORLD_1:
		case GLFW_KEY_WORLD_2:
			return Key.UNKNOWN;
		case GLFW_KEY_ESCAPE:
			return Key.ESCAPE;
		case GLFW_KEY_ENTER:
			return Key.ENTER;
		case GLFW_KEY_TAB:
			return Key.TAB;
		case GLFW_KEY_BACKSPACE:
			return Key.DEL;
		case GLFW_KEY_INSERT:
			return Key.INSERT;
		case GLFW_KEY_DELETE:
			return Key.FORWARD_DEL;
		case GLFW_KEY_RIGHT:
			return Key.RIGHT;
		case GLFW_KEY_LEFT:
			return Key.LEFT;
		case GLFW_KEY_DOWN:
			return Key.DOWN;
		case GLFW_KEY_UP:
			return Key.UP;
		case GLFW_KEY_PAGE_UP:
			return Key.PAGE_UP;
		case GLFW_KEY_PAGE_DOWN:
			return Key.PAGE_DOWN;
		case GLFW_KEY_HOME:
			return Key.HOME;
		case GLFW_KEY_END:
			return Key.END;
		case GLFW_KEY_CAPS_LOCK:
			return Key.CAPS_LOCK;
		case GLFW_KEY_SCROLL_LOCK:
			return Key.SCROLL_LOCK;
		case GLFW_KEY_PRINT_SCREEN:
			return Key.PRINT_SCREEN;
		case GLFW_KEY_PAUSE:
			return Key.PAUSE;
		case GLFW_KEY_F1:
			return Key.F1;
		case GLFW_KEY_F2:
			return Key.F2;
		case GLFW_KEY_F3:
			return Key.F3;
		case GLFW_KEY_F4:
			return Key.F4;
		case GLFW_KEY_F5:
			return Key.F5;
		case GLFW_KEY_F6:
			return Key.F6;
		case GLFW_KEY_F7:
			return Key.F7;
		case GLFW_KEY_F8:
			return Key.F8;
		case GLFW_KEY_F9:
			return Key.F9;
		case GLFW_KEY_F10:
			return Key.F10;
		case GLFW_KEY_F11:
			return Key.F11;
		case GLFW_KEY_F12:
			return Key.F12;
		case GLFW_KEY_F13:
			return Key.F13;
		case GLFW_KEY_F14:
			return Key.F14;
		case GLFW_KEY_F15:
			return Key.F15;
		case GLFW_KEY_F16:
			return Key.F16;
		case GLFW_KEY_F17:
			return Key.F17;
		case GLFW_KEY_F18:
			return Key.F18;
		case GLFW_KEY_F19:
			return Key.F19;
		case GLFW_KEY_F20:
			return Key.F20;
		case GLFW_KEY_F21:
			return Key.F21;
		case GLFW_KEY_F22:
			return Key.F22;
		case GLFW_KEY_F23:
			return Key.F23;
		case GLFW_KEY_F24:
			return Key.F24;
		case GLFW_KEY_F25:
			return Key.UNKNOWN;
		case GLFW_KEY_NUM_LOCK:
			return Key.NUM_LOCK;
		case GLFW_KEY_KP_0:
			return Key.NUMPAD_0;
		case GLFW_KEY_KP_1:
			return Key.NUMPAD_1;
		case GLFW_KEY_KP_2:
			return Key.NUMPAD_2;
		case GLFW_KEY_KP_3:
			return Key.NUMPAD_3;
		case GLFW_KEY_KP_4:
			return Key.NUMPAD_4;
		case GLFW_KEY_KP_5:
			return Key.NUMPAD_5;
		case GLFW_KEY_KP_6:
			return Key.NUMPAD_6;
		case GLFW_KEY_KP_7:
			return Key.NUMPAD_7;
		case GLFW_KEY_KP_8:
			return Key.NUMPAD_8;
		case GLFW_KEY_KP_9:
			return Key.NUMPAD_9;
		case GLFW_KEY_KP_DECIMAL:
			return Key.NUMPAD_DOT;
		case GLFW_KEY_KP_DIVIDE:
			return Key.NUMPAD_DIVIDE;
		case GLFW_KEY_KP_MULTIPLY:
			return Key.NUMPAD_MULTIPLY;
		case GLFW_KEY_KP_SUBTRACT:
			return Key.NUMPAD_SUBTRACT;
		case GLFW_KEY_KP_ADD:
			return Key.NUMPAD_ADD;
		case GLFW_KEY_KP_ENTER:
			return Key.NUMPAD_ENTER;
		case GLFW_KEY_KP_EQUAL:
			return Key.NUMPAD_EQUALS;
		case GLFW_KEY_LEFT_SHIFT:
			return Key.SHIFT_LEFT;
		case GLFW_KEY_LEFT_CONTROL:
			return Key.CONTROL_LEFT;
		case GLFW_KEY_LEFT_ALT:
			return Key.ALT_LEFT;
		case GLFW_KEY_LEFT_SUPER:
			return Key.SYM;
		case GLFW_KEY_RIGHT_SHIFT:
			return Key.SHIFT_RIGHT;
		case GLFW_KEY_RIGHT_CONTROL:
			return Key.CONTROL_RIGHT;
		case GLFW_KEY_RIGHT_ALT:
			return Key.ALT_RIGHT;
		case GLFW_KEY_RIGHT_SUPER:
			return Key.SYM;
		case GLFW_KEY_MENU:
			return Key.MENU;
		default:
			return Key.UNKNOWN;
		}
	}
	
}
