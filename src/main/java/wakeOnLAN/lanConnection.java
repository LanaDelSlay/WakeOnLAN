package wakeOnLAN;

import java.io.Serializable;

public class lanConnection implements Serializable{

	private static final long serialVersionUID = -462256676304920776L;
	private String ip;
	private String mac;
	private String nickname;
	
	public lanConnection(String ip, String mac, String nickname) {
		super();
		this.ip = ip;
		this.mac = mac;
		this.nickname = nickname;
	}
	
	public String getIP() {
		return ip;
	}
	
	public String getNickname() {
		return nickname;
	}
	
	public String getMac() {
		return mac;
	}
	
}
