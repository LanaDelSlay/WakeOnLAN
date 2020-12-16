package wakeOnLAN;

import java.io.Serializable;

public class lanConnection implements Serializable{

	private static final long serialVersionUID = -462256676304920776L;
	private String ip;
	private String mac;
	public lanConnection(String ip, String mac) {
		super();
		this.ip = ip;
		this.mac = mac;
	}
	
	public String getIP() {
		return ip;
	}
	
	public String getMac() {
		return mac;
	}
	
}
