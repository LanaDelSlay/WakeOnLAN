package wakeOnLAN;

import java.awt.FileDialog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class coolController {

    @FXML
    private Button loadBtn;

    @FXML
    private Button wakeBtn;

    @FXML
    private Button saveBtn;
    
    @FXML
    private TextField ipTextF;

    @FXML
    private TextField macTextF;

    @FXML
    private Label feedbackLabel;

    @FXML
    void load(ActionEvent event) {
        JFileChooser f = new JFileChooser();
        String dataFolder = System.getProperty("user.home") + File.separator + "WOL_data";
        File file = new File(dataFolder);
        String OS = System.getProperty("os.name");
        System.out.println(OS);
        if (OS.contains("Windows")) {
        	  f.setCurrentDirectory(file);
              f.setFileSelectionMode(JFileChooser.FILES_ONLY); 
              f.showSaveDialog(null);
              
              try {
      			FileInputStream fileIn = new FileInputStream(f.getSelectedFile());
      			ObjectInputStream objectIn = new ObjectInputStream(fileIn);
      			Object obj = objectIn.readObject();
      			lanConnection lc = (lanConnection) obj;
      			ipTextF.setText(lc.getIP());
      			macTextF.setText(lc.getMac());
      			feedbackLabel.setText("Loaded!");
      			
      		} catch (FileNotFoundException e) {
      			feedbackLabel.setText("ERROR: File not found");
      			e.printStackTrace();
      		} catch (IOException e) {
      			// TODO Auto-generated catch block
      			feedbackLabel.setText("ERROR: File may be corrupt.");
      			e.printStackTrace();
      		} catch (ClassNotFoundException e) {
      			// TODO Auto-generated catch block
      			feedbackLabel.setText("ERROR");
      			e.printStackTrace();
      		} catch (NullPointerException e) {
      			feedbackLabel.setText("No File selected");
      		}
        } else {
        	JFrame frame = new JFrame();
        	FileDialog fd = new FileDialog(frame, "Choose a device", FileDialog.LOAD);
        	fd.setDirectory(dataFolder);
        	//fd.setFile(file);
        	fd.setVisible(true);
        	FileInputStream fileIn;
			try {
				fileIn = new FileInputStream(fd.getFile());
				ObjectInputStream objectIn = new ObjectInputStream(fileIn);
	  			Object obj = objectIn.readObject();
	  			lanConnection lc = (lanConnection) obj;
	  			ipTextF.setText(lc.getIP());
	  			macTextF.setText(lc.getMac());
	  			feedbackLabel.setText("Loaded!");
			} catch (FileNotFoundException e) {
      			feedbackLabel.setText("ERROR: File not found");
				e.printStackTrace();
			} catch (IOException e) {
      			feedbackLabel.setText("ERROR: File may be corrupt.");
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
      			feedbackLabel.setText("ERROR");
				e.printStackTrace();
			} catch (NullPointerException e) {
      			feedbackLabel.setText("No File selected");
      		}
  		
        }
      
        
        

    }

    @FXML
    void save(ActionEvent event) throws IOException {
		feedbackLabel.setTextFill(Color.GREEN);
    	String ip = ipTextF.getText(); 
    	String mac = macTextF.getText();
    	String userDir = System.getProperty("user.home");
    	String dataFolder = userDir + File.separator + "WOL_data";
    	System.out.println(dataFolder);
    	Files.createDirectories(Paths.get(dataFolder));
    		if(verifyInput(ip, mac, feedbackLabel)) {
                FileOutputStream fileOut = new FileOutputStream(dataFolder + File.separator + ip);
                ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
                lanConnection lc = new lanConnection(ip,mac);
                objectOut.writeObject(lc);
                objectOut.close();
                feedbackLabel.setText("Saved!");
    		}
        
    }

    @FXML
    void wake(ActionEvent event) {
		feedbackLabel.setTextFill(Color.RED);
		feedbackLabel.setVisible(false);
    	String ip = ipTextF.getText(); 
    	String mac = macTextF.getText();
    	String ipReg = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"; 
    	String macReg = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})|([0-9a-fA-F]{4}\\\\.[0-9a-fA-F]{4}\\\\.[0-9a-fA-F]{4})$";
    	Pattern ipPattern = Pattern.compile(ipReg);
    	Pattern macPattern = Pattern.compile(macReg);
    	Matcher ipMatch = ipPattern.matcher(ip);
    	Matcher macMatch = macPattern.matcher(mac);
    	
    	if(verifyInput(ip,mac,feedbackLabel)) 
        	try {
                byte[] macBytes = getMacBytes(mac);
                byte[] bytes = new byte[6 + 16 * macBytes.length];
                for (int i = 0; i < 6; i++) {
                    bytes[i] = (byte) 0xff;
                }
                for (int i = 6; i < bytes.length; i += macBytes.length) {
                    System.arraycopy(macBytes, 0, bytes, i, macBytes.length);
                }
                
                InetAddress address = InetAddress.getByName(ip);
                DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, 9);
                DatagramSocket socket = new DatagramSocket();
                socket.send(packet);
                socket.close();
        		feedbackLabel.setTextFill(Color.GREEN);
                feedbackLabel.setText("Wake-on-LAN packet sent.");
            }
            catch (UnknownHostException e) {
        		feedbackLabel.setTextFill(Color.RED);
            	feedbackLabel.setText("Failed to send Wake-on-LAN packet:");
                System.out.println(e);
                System.exit(1);
            } catch (SocketException e) {
            	feedbackLabel.setTextFill(Color.RED);
            	feedbackLabel.setText("Failed to send Wake-on-LAN packet:");
            	e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	

    	}
    	
    private static byte[] getMacBytes(String macStr) throws IllegalArgumentException {
        byte[] bytes = new byte[6];
        String[] hex = macStr.split("(\\:|\\-)");
        if (hex.length != 6) {
            throw new IllegalArgumentException("Invalid MAC address.");
        }
        try {
            for (int i = 0; i < 6; i++) {
                bytes[i] = (byte) Integer.parseInt(hex[i], 16);
            }
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid hex digit in MAC address.");
        }
        return bytes;
    }
    
    private static Boolean verifyInput(String ip, String mac, Label feedbackLabel) {
		feedbackLabel.setTextFill(Color.RED);
    	feedbackLabel.setVisible(false);
    	String ipReg = "^(?:[0-9]{1,3}\\.){3}[0-9]{1,3}$"; 
    	String macReg = "^([0-9A-Fa-f]{2}[:-]){5}([0-9A-Fa-f]{2})|([0-9a-fA-F]{4}\\\\.[0-9a-fA-F]{4}\\\\.[0-9a-fA-F]{4})$";
    	Pattern ipPattern = Pattern.compile(ipReg);
    	Pattern macPattern = Pattern.compile(macReg);
    	Matcher ipMatch = ipPattern.matcher(ip);
    	Matcher macMatch = macPattern.matcher(mac);
    	
    	//Is IP Field Empty
    	if (ip.isBlank()) {
    		feedbackLabel.setVisible(true);
    		feedbackLabel.setText("Get fucked nerd");
    		return false;
    	}  else
    	//Is it a valid IP?
    	if (!ipMatch.matches()) {
    		feedbackLabel.setVisible(true);
    		feedbackLabel.setText("IP not valid!");
    		return false;

    	} else
    	//Is MAC Blank
    	if (mac.isBlank()) {
    		feedbackLabel.setVisible(true);
    		feedbackLabel.setText("Mac is blank!");
    		return false;
    	} else
    		//Is mac valid?
    	if (!macMatch.matches()) {
    		feedbackLabel.setVisible(true);
    		feedbackLabel.setText("Mac is invalid!");
    		return false;
    	}  else {
    		feedbackLabel.setVisible(true);
    		feedbackLabel.setTextFill(Color.GREEN);
    		feedbackLabel.setText("Saving info!");
    		return true;
    		
    }
}
}