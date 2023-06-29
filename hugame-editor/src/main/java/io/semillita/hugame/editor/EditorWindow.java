package io.semillita.hugame.editor;

import io.semillita.hugame.editor.game.GameContainerPanel;
import io.semillita.hugame.editor.menu.Menu;
import io.semillita.hugame.editor.menu.MenuBar;
import io.semillita.hugame.editor.menu.MenuItem;
import io.semillita.hugame.editor.menu.Separator;
import io.semillita.hugame.editor.menu.SubMenu;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

public class EditorWindow extends Window {

  public EditorWindow(String path, int width, int height) {
    super(path, width, height);

    var iconUrl = EditorWindow.class.getResource("/folder.png");
    BufferedImage iconImage;
    try {
      iconImage = ImageIO.read(Objects.requireNonNull(iconUrl));
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    var closedFolderIcon = new ImageIcon(Utils.scale(iconImage, 16, 16));
    // UIManager.put("Tree.closedIcon", closedFolderIcon);
    // UIManager.put("Tree.openIcon", closedFolderIcon);
    // UIManager.put("Tree.leafIcon", closedFolderIcon);
    UIManager.put("Tree.rowHeight", 20);
    UIManager.put("ScrollBar.thumbArc", 5);
    UIManager.put("ScrollBar.thumbInsets", new Insets(3, 3, 3, 3));
    UIManager.put("ScrollBar.thumb", Colors.GRADIENT_4);
    UIManager.put("ScrollBar.background", Colors.TRANSPARENT);
    UIManager.put("ScrollBar.foreground", Color.PINK);
    // UIManager.put("ScrollBar.showButtons", true);

    setTitleBarColor(Colors.GRADIENT_1, Colors.WHITE);

    var menuBar = new MenuBar();
    var frame = super.getJFrame();
    frame.setJMenuBar(menuBar);

    var fileMenu = new Menu("File");

    menuBar.add(fileMenu);
    var newProjectItem = new MenuItem("New project");
    fileMenu.add(newProjectItem);
    var openProjectItem = new MenuItem("Open project");
    fileMenu.add(openProjectItem);

    var separator = new Separator();
    fileMenu.add(separator);

    var exportMenu = new SubMenu("Export");
    fileMenu.add(exportMenu);
    var exportJarItem = new MenuItem("JAR file (.jar)");
    exportMenu.add(exportJarItem);
    var exportExeItem = new MenuItem("Executable file (.exe)");
    exportMenu.add(exportExeItem);

    var helpMenu = new Menu("Help");
    menuBar.add(helpMenu);

    var workspace = new Workspace();
    frame.add(workspace.getContainer());

    var widgetInputManager = workspace.getInputManager();

    var left = new Widget(widgetInputManager);
    var explorerPanel = new ContentPanel();
    // explorerPanel.setBackground(Color.RED);
    explorerPanel.setLocation(0, 0);
    // explorerPanel.setLayout(new BoxLayout(explorerPanel, BoxLayout.X_AXIS));
    // explorerPanel.setLayout(new GridBagLayout());
    // explorerPanel.setLayout(null);
    explorerPanel.setLayout(new BorderLayout());
    left.addView(new View("Explorer", explorerPanel));
    var root = makeFileTree(Path.of(path));
    /*var root = new DefaultMutableTreeNode("Root");
    var a = new DefaultMutableTreeNode("A");
    root.add(a);
    a.add(new DefaultMutableTreeNode("a"));
    root.add(new DefaultMutableTreeNode("B"));

    for (int i = 0; i < 30; i++) {
        root.add(new DefaultMutableTreeNode("Raaah"));
    }*/

    var explorerTree = new JTree(root);
    explorerTree.setBackground(Colors.TRANSPARENT);
    explorerTree.setOpaque(false);
    explorerTree.setBorder(new EmptyBorder(10, 0, 10, 0));

    var explorerScrollPane = new JScrollPane(explorerTree);
    explorerScrollPane.setViewportBorder(new EmptyBorder(0, 10, 0, 10));
    explorerScrollPane.setBorder(BorderFactory.createEmptyBorder());
    explorerScrollPane.setBackground(Colors.TRANSPARENT);
    explorerScrollPane.setOpaque(false);

    // explorerScrollPane.getVerticalScrollBar().setBackground(Color.RED);
    explorerScrollPane.getVerticalScrollBar().setOpaque(false);

    explorerScrollPane.getViewport().setBackground(Colors.TRANSPARENT);
    explorerScrollPane.getViewport().setOpaque(false);

    explorerPanel.add(explorerScrollPane);
    // explorerPanel.add(explorerTree);

    var explorerTreeRenderer = (DefaultTreeCellRenderer) explorerTree.getCellRenderer();
    explorerTreeRenderer.setClosedIcon(closedFolderIcon);
    explorerTreeRenderer.setOpenIcon(closedFolderIcon);
    // explorerTree.setLocation(0, 0);
    System.out.println(explorerPanel.getBounds());
    explorerTree.setBounds(0, 0, 45, 100);

    var center = new Widget(widgetInputManager);
    var gameContainerPanel = new GameContainerPanel();
    center.addView(new View("Game", gameContainerPanel));
    SwingUtilities.invokeLater(
        () -> {
          System.out.println("Repainting");
          gameContainerPanel.startApplication();
        });

    var right = new Widget(widgetInputManager);
    right.addView(new View("Properties", new ContentPanel()));

    var leftCenter = new SplitPane();
    left.addToSplitPane(leftCenter);
    center.addToSplitPane(leftCenter);

    var leftCenterRight = new SplitPane();
    leftCenterRight.add(leftCenter);
    right.addToSplitPane(leftCenterRight);
    leftCenterRight.setDividerPosition(1300);

    workspace.add(leftCenterRight);

    frame.setVisible(true);
  }

  private DefaultMutableTreeNode makeFileTree(Path path) {
    var root = new DefaultMutableTreeNode(path.getFileName().toString());

    if (Files.isDirectory(path)) {
      try (var children = Files.list(path)) {
        children.sorted().forEach(child -> root.add(makeFileTree(child)));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }

    return root;
  }
}
