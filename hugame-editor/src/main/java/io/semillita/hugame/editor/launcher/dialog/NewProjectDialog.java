package io.semillita.hugame.editor.launcher.dialog;

import io.semillita.hugame.editor.Colors;
import io.semillita.hugame.editor.launcher.ProjectGenerator;
import java.awt.Font;
import java.awt.Graphics;
import javax.swing.JComponent;
import javax.swing.JLabel;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.util.nfd.NativeFileDialog;

public class NewProjectDialog extends Dialog {
  private final DialogTextField titleTextField;
  private final DialogTextFieldWithButton locationTextField;

  public NewProjectDialog(Runnable onClose) {
    super(onClose);

    int y = 80;

    this.titleTextField = new DialogTextField(350);
    titleTextField.setFont(new Font("Microsoft YaHei Light", Font.BOLD, 18));
    addComponent(titleTextField, "Title", y);
    y += 80;

    var artifactIdTextField = new DialogTextField(350);
    artifactIdTextField.setFont(new Font("Microsoft YaHei Light", Font.BOLD, 18));
    addComponent(artifactIdTextField, "Artifact ID", y);
    y += 80;

    var groupIdTextField = new DialogTextField(350);
    groupIdTextField.setFont(new Font("Microsoft YaHei Light", Font.BOLD, 18));
    addComponent(groupIdTextField, "Group ID", y);
    y += 80;

    this.locationTextField = new DialogTextFieldWithButton(this::chooseFile, 390);
    locationTextField.setFont(new Font("Microsoft YaHei Light", Font.BOLD, 15));
    addComponent(locationTextField, "Parent directory", y);

    var createProjectButton =
        new CreateProjectButton(
            () ->
                ProjectGenerator.createProject(
                    titleTextField.getText(),
                    locationTextField.getText(),
                    artifactIdTextField.getText(),
                    groupIdTextField.getText()));
    add(createProjectButton);
    createProjectButton.setLocation(70, 440);
    createProjectButton.setSize(200, 40);
  }

  @Override
  protected void paintComponent(Graphics g) {
    super.paintComponent(g);

    g.setColor(Colors.Launcher.PRIMARY_TEXT);
    g.setFont(new Font("Trebuchet MS", Font.PLAIN, 45));
    g.drawString("New project", 150, 60);
  }

  private void chooseFile() {
    var outBuffer = PointerBuffer.allocateDirect(1);
    NativeFileDialog.NFD_PickFolder(outBuffer, "C:\\Users\\hugob\\programmering");
    var location = MemoryUtil.memUTF8(outBuffer.get(0));
    locationTextField.setText(location);
  }

  private void addComponent(JComponent component, String labelText, int y) {
    var label = new JLabel(labelText);
    add(label);
    label.setLocation(75, y);
    label.setSize(150, 30);
    // label.setFont(new Font("Segoe UI Variable Small Semibold", Font.BOLD, 15));
    label.setFont(new Font("Microsoft YaHei Light", Font.BOLD, 18));

    add(component);
    component.setLocation(70, y + 30);
  }
}
