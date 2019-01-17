package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class TextEditor extends Thread {
	private String edited, paragraph;
	JFrame frame;
	JButton button;
	JTextArea textArea;
	JLabel label;
	String fileName;
	Boolean status = true;

	public String getEditedParagraph() {
		return edited;
	}

	public boolean getStatus() {
		return status;
	}

	public TextEditor(String paragraph, String fileName) {
		this.paragraph = paragraph;
		this.fileName = fileName;
		edited = paragraph;
	}

	public void run() {
		frame = new JFrame("Editor");
		label = new JLabel(fileName);
		label.setBounds(10, 20, 110, 20);
		button = new JButton("Save and Close");
		button.setBounds(195, 420, 110, 20);
		textArea = new JTextArea(paragraph);
		textArea.setBounds(10, 50, 480, 350);
		frame.add(textArea);
		frame.add(label);
		frame.add(button);
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				edited = textArea.getText();
				status = false;
				frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
			}
		});
		frame.setSize(500, 500);
		frame.setLayout(null);
		frame.setVisible(true);
		frame.setAlwaysOnTop(true);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent windowEvent) {
				status = false;
			}
		});
	}

}
