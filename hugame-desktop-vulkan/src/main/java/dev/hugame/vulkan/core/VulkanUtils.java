package dev.hugame.vulkan.core;

import dev.hugame.util.TriConsumer;
import java.nio.IntBuffer;
import java.util.Collection;
import java.util.function.BiFunction;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

public class VulkanUtils {
  public static <COMPONENT, RESULT_BUFFER> RESULT_BUFFER query(
      MemoryStack memoryStack,
      COMPONENT component,
      TriConsumer<COMPONENT, IntBuffer, RESULT_BUFFER> query,
      BiFunction<Integer, MemoryStack, RESULT_BUFFER> makeResultBuffer) {
    var countBuffer = memoryStack.callocInt(1);
    query.accept(component, countBuffer, null);
    var resultBuffer = makeResultBuffer.apply(countBuffer.get(0), memoryStack);
    query.accept(component, countBuffer, resultBuffer);

    return resultBuffer;
  }

  public static PointerBuffer asPointerBuffer(MemoryStack memoryStack, Collection<String> values) {
    var pointerBuffer = memoryStack.mallocPointer(values.size());
    values.stream().map(memoryStack::UTF8).forEach(pointerBuffer::put);

    return pointerBuffer.rewind();
  }
}
