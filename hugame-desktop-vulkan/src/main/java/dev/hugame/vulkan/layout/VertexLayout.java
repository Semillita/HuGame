package dev.hugame.vulkan.layout;

// TODO: Good structure, move it out of the vulkan project later
public class VertexLayout {
  // TODO: In this class, define vertex attributes, SSBOs, UBOs and such
  //  through calls to methods like .addAttribute(...).addSomething(...)
  //  And they are "implemented" with descriptor stuff in a
  //  VertexLayoutDescriptors which deals with all descriptor-related things'
  //  creation

  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    protected Builder addAttribute() {
      // TODO: Add attribute
      return this;
    }
  }

  public static class Attribute {
    // TODO: Add all necessary info about attributes, add enums for structure type
    //  (not vulkan specific)
  }
}
