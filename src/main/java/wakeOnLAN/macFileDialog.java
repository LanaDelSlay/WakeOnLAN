package wakeOnLAN;

import java.awt.FileDialog;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class macFileDialog extends Thread{ 
	
	TextField ipTextF;
	TextField macTextF;
	Label feedbackLabel;
	FileInputStream fileIn;
	Label loadedComputerLabel;
	
	public macFileDialog(TextField ipTextF, TextField macTextF, Label feedbackLabel, Label loadedComputerLabel) {
		super();
		this.ipTextF = ipTextF;
		this.macTextF = macTextF;
		this.feedbackLabel = feedbackLabel;
		this.loadedComputerLabel = loadedComputerLabel;
	}


	
	public void run(){
		String dataFolder = System.getProperty("user.home") + File.separator + "WOL_data";
		JFrame frame = new JFrame();
		FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
		fd.setDirectory(dataFolder);
		fd.setVisible(true);
		ObjectInputStream objectIn;
		try {
			fileIn = new FileInputStream(fd.getDirectory()+fd.getFile());
			objectIn = new ObjectInputStream(fileIn);
			Object obj = objectIn.readObject();
			lanConnection lc = (lanConnection) obj;
			ipTextF.setText(lc.getIP());
			macTextF.setText(lc.getMac());
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					loadedComputerLabel.setText(lc.getNickname());
					feedbackLabel.setText("File loaded");
				}
				});

			
			
		} catch (EOFException e) {
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					feedbackLabel.setTextFill(Color.RED);
					feedbackLabel.setVisible(true);
					feedbackLabel.setText("File is corrupt");
				}
				});			
		} catch (FileNotFoundException e) {
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					feedbackLabel.setTextFill(Color.RED);
					feedbackLabel.setVisible(true);
					feedbackLabel.setText("No file selected");
				}
				});		

		}
		
		catch (IOException e) {
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					feedbackLabel.setTextFill(Color.RED);
					feedbackLabel.setVisible(true);
					feedbackLabel.setText("Error loading file");
				}
				});
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			Platform.runLater(new Runnable(){
				@Override
				public void run() {
					feedbackLabel.setTextFill(Color.RED);
					feedbackLabel.setVisible(true);
					feedbackLabel.setText("Error loading file");
				}
				});			e.printStackTrace();
		} 
		
		
	    }
	
	
}
 
