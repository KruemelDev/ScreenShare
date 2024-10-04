package com.kruemel.screenshare.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class ClientData {
	public String name;
	public Socket socket;
	public DataInputStream in;
	public DataOutputStream out;

	public ArrayList<ClientData> screenShareAllowed = new ArrayList<>();
	public enum statusModes{
		AVAILABLE,
		TRANSFER_SCREEN,

	}

	public statusModes status;

	public ClientData(String name, Socket socket) {
		this.name = name;
		this.socket = socket;
		try {
			this.in = new DataInputStream(this.socket.getInputStream());
			this.out = new DataOutputStream(this.socket.getOutputStream());
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

	}
	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		if(obj == null || this.getClass() != obj.getClass()){
			return false;
		}
		ClientData client = (ClientData) obj;
		return this.name.equals(client.name);
	}
	public void WriteMessage(String message) {
		try {
			if (this.out != null) {
				this.out.writeUTF(message);
				this.out.flush();
			}
		} catch (IOException e) {

			try {
				this.socket.close();
			} catch (IOException ex) {
				throw new RuntimeException(ex);
			}
			finally {
				ConnectionHandler.removeClient(this);
			}
		}
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
