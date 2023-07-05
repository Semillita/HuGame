package io.semillita.hugame.editor;

import javax.swing.JPanel;

public class ChooseProjectPanel {

	private final JPanel panel;
	
	public ChooseProjectPanel() {
		this.panel = new JPanel();
		panel.setBackground(Colors.GRADIENT_2);
	}
	
	public JPanel getJPanel() {
		return panel;
	}
	
}
