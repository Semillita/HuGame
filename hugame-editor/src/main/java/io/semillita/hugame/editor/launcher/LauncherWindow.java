package io.semillita.hugame.editor.launcher;

import io.semillita.hugame.editor.Colors;
import io.semillita.hugame.editor.Window;
import java.awt.*;
import java.util.function.Consumer;

public final class LauncherWindow extends Window {
  public static final int WIDTH = 960, HEIGHT = 540;

  private final Consumer<String> projectOpenListener;

  public LauncherWindow(Consumer<String> projectOpenListener) {
    super("", WIDTH, HEIGHT);

    var frame = super.getJFrame();
    // super.setBackgroundColor(Color.RED);
    this.projectOpenListener = projectOpenListener;

    var panel =
        new LauncherPanel(
            projectLocation -> {
              projectOpenListener.accept(projectLocation);
              super.close();
            });
    frame.add(panel);
    panel.setLayout(null);
    panel.setPreferredSize(new Dimension(960, 540));
    panel.setLocation(0, 0);

    /*var tabbedPane = new JTabbedPane();
    //frame.add(tabbedPane);
    tabbedPane.putClientProperty("JTabbedPane.tabHeight", 50);
    tabbedPane.putClientProperty("JTabbedPane.tabWidthMode", "equal");
    tabbedPane.setFont(new Font("Segoe UI Symbol", Font.PLAIN, 30));

    tabbedPane.setUI(new FlatTabbedPaneUI() {
        @Override
        protected int calculateTabWidth( int tabPlacement, int tabIndex, FontMetrics metrics ) {
            var tabPane = super.tabPane;
            var tabCount = tabPane.getTabCount();

            return (int) (tabPane.getWidth() / tabCount);
        }
    });

    var chooseProjectPanel = new ChooseProjectPanel();
    var newProjectPanel = new NewProjectPanel();

    tabbedPane.add("Existing project", chooseProjectPanel.getJPanel());
    tabbedPane.add("New project", newProjectPanel.getJPanel());
    tabbedPane.setBackground(Colors.GRADIENT_1);


          newProjectPanel.addComponents();*/

    setTitleBarColor(Colors.Launcher.GRADIENT_2, Colors.Launcher.LOGO);

    frame.pack();
    frame.setResizable(false);
    frame.setVisible(true);
  }
}
