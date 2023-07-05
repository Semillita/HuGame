package io.semillita.hugame.editor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;

import javax.swing.BoxLayout;
import javax.swing.JPanel;

public class NewProjectPanel {
	private final JPanel panel;

	public NewProjectPanel() {
		this.panel = new JPanel();
		panel.setBackground(Colors.GRADIENT_2);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	}
	
	public void addComponents() {
		var panelWidth = panel.getWidth();
		System.out.println("panel width: " + panelWidth);
		
		var nameField = new TextField();
		nameField.setMargin(new Insets(20, 0, 20, 0));
		nameField.setBounds(50, 50, 200, 30);
		nameField.setPreferredSize(new Dimension(200, 30));
		panel.add(nameField);
		
		var descriptionField = new TextField();
		descriptionField.setMargin(new Insets(20, 0, 20, 0));
		descriptionField.setBounds(50, 100, 200, 30);
		panel.add(descriptionField);
	}
	
	public JPanel getJPanel() {
		return panel;
	}
}
