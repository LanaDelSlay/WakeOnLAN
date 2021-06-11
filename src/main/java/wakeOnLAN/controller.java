package wakeOnLAN;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import impl.org.controlsfx.autocompletion.AutoCompletionTextFieldBinding;
import impl.org.controlsfx.autocompletion.SuggestionProvider;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class controller {

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
	private TextField compNameTextF;

	@FXML
	private Label loadedComputerLabel;

	String userDir = System.getProperty("user.home");
	String dataFolder = userDir + File.separator + "WOL_data";
	ArrayList<String> ipStringArr = new ArrayList<String>();
	ArrayList<String> macStringArr = new ArrayList<String>();
	ArrayList<String> nameStringArr = new ArrayList<String>();

	public static void main(String args[]) {
	}

	public void initialize() throws IOException {
		populateSuggestions();

		ipTextF.textProperty().addListener((obs, oldText, newText) -> {
			if (ipStringArr.contains(newText)) {
				int index = ipStringArr.indexOf(newText);
				String nameStr = nameStringArr.get(index).trim();
				String nameCapped = nameStr.substring(0, 1).toUpperCase() + nameStr.substring(1);
				macTextF.setText(macStringArr.get(index));
				feedbackLabel.setText(nameCapped + " loaded!");
				feedbackLabel.setTextFill(Color.GREEN);
				feedbackLabel.setVisible(true);
			}
		});

		macTextF.textProperty().addListener((obs, oldText, newText) -> {
			if (macStringArr.contains(newText)) {
				int index = macStringArr.indexOf(newText);
				ipTextF.setText(ipStringArr.get(index));
				String nameStr = nameStringArr.get(index).trim();
				String nameCapped = nameStr.substring(0, 1).toUpperCase() + nameStr.substring(1);
				feedbackLabel.setText(nameCapped.toUpperCase() + " loaded!");
				feedbackLabel.setTextFill(Color.GREEN);
				feedbackLabel.setVisible(true);
			}
		});
	}

	@SuppressWarnings("unchecked")
	@FXML
	void populateSuggestions() {
		String pattern = "[^,]+";
		File dataFile = new File(dataFolder + File.separator + "savedIPs.dat");
		URI dataURI = dataFile.toURI();
		List<String> result;
		try {
			result = Files.readAllLines(Paths.get(dataURI));
			Pattern p = Pattern.compile(pattern);

			result.forEach(text -> {
				Matcher m = p.matcher(text);
				int i = 1;
				while (m.find()) {
					// Format is IP, MAC, then name!
					switch (i) {
					case 1:
						ipStringArr.add(m.group());
						break;
					case 2:
						macStringArr.add(m.group());
						break;
					case 3:
						nameStringArr.add(m.group());
						break;
					}
				i++;
				}

			});

		
			
		} catch (NoSuchFileException e1) {
			// TODO Auto-generated catch block
		} catch (Exception e) {
			e.printStackTrace();

		}
 
			@SuppressWarnings("rawtypes")
			SuggestionProvider ipSuggestionProvider = SuggestionProvider.create(new ArrayList());
			new AutoCompletionTextFieldBinding<>(ipTextF, ipSuggestionProvider);
			ipSuggestionProvider.addPossibleSuggestions("192.168.0.");
			ipSuggestionProvider.addPossibleSuggestions("10.0.0.");
			ipSuggestionProvider.addPossibleSuggestions(ipStringArr);

			@SuppressWarnings("rawtypes")
			SuggestionProvider macSuggestionProvider = SuggestionProvider.create(new ArrayList());
			new AutoCompletionTextFieldBinding<>(macTextF, macSuggestionProvider);
			macSuggestionProvider.addPossibleSuggestions(macStringArr);

		
		ipTextF.setText("");
		macTextF.setText("");
 	
	}

	@FXML
	void save(ActionEvent event) throws IOException {
		String name;
		feedbackLabel.setTextFill(Color.GREEN);
		String ip = ipTextF.getText();
		String mac = macTextF.getText();
		Files.createDirectories(Paths.get(dataFolder));
		if (verifyInput(ip, mac, feedbackLabel)) {
			if (compNameTextF.getText().isBlank() || !compNameTextF.isVisible()) {
				compNameTextF.setVisible(true);
				loadedComputerLabel.setText("Please enter a name");
			} else {
				name = compNameTextF.getText();
				try (Writer writer = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(dataFolder + File.separator + "savedIPs.dat", true), "utf-8"))) {
					writer.append("\n" + ip + "," + mac + "," + name);
				}
				feedbackLabel.setVisible(true);
				feedbackLabel.setTextFill(Color.GREEN);
				feedbackLabel.setText("Saved!");
				compNameTextF.setVisible(false);
				loadedComputerLabel.setText("");
			}
		}
	}

	@FXML
	void wake(ActionEvent event) {
		compNameTextF.setVisible(false);
		loadedComputerLabel.setText("");
		feedbackLabel.setTextFill(Color.RED);
		feedbackLabel.setVisible(false);
		String ip = ipTextF.getText();
		String mac = macTextF.getText();

		if (verifyInput(ip, mac, feedbackLabel))
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
				feedbackLabel.setVisible(true);
			} catch (UnknownHostException e) {
				feedbackLabel.setTextFill(Color.RED);
				feedbackLabel.setText("Failed to send Wake-on-LAN packet:");
				feedbackLabel.setVisible(true);
				System.out.println(e);
				System.exit(1);
			} catch (SocketException e) {
				feedbackLabel.setTextFill(Color.RED);
				feedbackLabel.setText("Failed to send Wake-on-LAN packet:");
				feedbackLabel.setVisible(true);
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
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
		} catch (NumberFormatException e) {
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
		// Is IP Field Empty
		if (ip.isBlank()) {
			feedbackLabel.setVisible(true);
			return false;
		} else
		// Is it a valid IP?
		if (!ipMatch.matches()) {
			feedbackLabel.setVisible(true);
			feedbackLabel.setText("IP not valid!");
			return false;
		} else
		// Is MAC Blank
		if (mac.isBlank()) {
			feedbackLabel.setVisible(true);
			feedbackLabel.setText("Mac is blank!");
			return false;
		} else
		// Is mac valid?
		if (!macMatch.matches()) {
			feedbackLabel.setVisible(true);
			feedbackLabel.setText("Mac is invalid!");
			return false;
		} else {
			return true;
		}
	}
}