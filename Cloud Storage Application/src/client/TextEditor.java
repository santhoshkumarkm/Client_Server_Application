package client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextArea;

public class TextEditor extends Thread {
	String mode;
	private String edited, paragraph;
	JFrame frame;
	JButton button1, button2;
	JTextArea textArea;
	JLabel label, label2;
	String fileName;
	Boolean status = true, edit = true;

	public String getEditedParagraph() {
		return edited;
	}

	public boolean getStatus() {
		return status;
	}

	public boolean isEdited() {
		return edit;
	}

	public TextEditor(String mode, String paragraph, String fileName) {
		this.mode = mode;
		this.paragraph = paragraph;
		this.fileName = fileName;
		edited = paragraph;
	}

	public void run() {
		frame = new JFrame("Editor");

		label = new JLabel(fileName);
		label.setBounds(10, 20, 110, 20);

		label2 = new JLabel("* EDIT MODE *");
		label2.setBounds(195, 20, 110, 20);
		label2.setVisible(false);
		
		button1 = new JButton("Save and Close");
		button1.setBounds(195, 420, 110, 20);

		textArea = new JTextArea(paragraph);
		textArea.setBounds(10, 50, 480, 350);

		frame.add(textArea);
		frame.add(label);
		frame.add(button1);
		frame.add(label2);

		if (mode.equals("DISPLAY")) {
			edit = false;
			button2 = new JButton("Edit");
			button2.setBounds(400, 20, 60, 20);
			frame.add(button2);
			button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					edit = true;
					label2.setVisible(true);
					textArea.setEditable(true);
				}
			});
			textArea.setEditable(false);
		}

		button1.addActionListener(new ActionListener() {
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
