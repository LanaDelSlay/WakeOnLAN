package wakeOnLAN;

import java.awt.FileDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.swing.JFrame;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

public class macFileDialog extends Thread{ 
	
	public macFileDialog(TextField ipTextF, TextField macTextF, Label feedbackLabel) {
		super();
		this.ipTextF = ipTextF;
		this.macTextF = macTextF;
		this.feedbackLabel = feedbackLabel;
	}

	TextField ipTextF;
	TextField macTextF;
	Label feedbackLabel;
	FileInputStream fileIn;
	
//Bringin up UI just fine
	public void run(){
		String dataFolder = System.getProperty("user.home") + File.separator + "WOL_data";
		JFrame frame = new JFrame();
		FileDialog fd = new FileDialog(frame, "Choose a file", FileDialog.LOAD);
		fd.setDirectory(dataFolder);
		fd.setVisible(true);
		String filename = fd.getFile();
		
		System.out.println(filename);
		System.out.println(fd.getDirectory()+fd.getFile());		
		
		ObjectInputStream objectIn;
		try {
			
			fileIn = new FileInputStream(fd.getDirectory()+fd.getFile());
			objectIn = new ObjectInputStream(fileIn);
			Object obj = objectIn.readObject();
			lanConnection lc = (lanConnection) obj;
			ipTextF.setText(lc.getIP());
			macTextF.setText(lc.getMac());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	    }
	
	
}
 
