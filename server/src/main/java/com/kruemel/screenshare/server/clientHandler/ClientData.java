package com.kruemel.screenshare.server.clientHandler;

import com.kruemel.screenshare.server.ConnectionHandler;

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

	public String base64ImagePiece = "";
	public ClientData currentScreenWatching;
	public ClientData mouseControlClient;
	public ArrayList<ClientData> screenShareAllowed = new ArrayList<>();

	public boolean error = false;
	public ClientData(String name, Socket socket) {
		this.name = name;
		this.socket = socket;
		try {
			this.in = new DataInputStream(this.socket.getInputStream());
			this.out = new DataOutputStream(this.socket.getOutputStream());
		}
		catch (IOException e) {
			ConnectionHandler.removeClient(this);
			this.error = true;
		}

	}
	@Override
public boolean equals(Object obj) {
    if (this == obj) {
        return true;
    }
    if (obj == null || this.getClass() != obj.getClass()) {
        return false;
    }
    ClientData client = (ClientData) obj;
    return this.name != null && this.name.equals(client.name);
}

public void WriteMessage(String message) {
    try {
        if (this.out != null) {
            this.out.writeUTF(message);
            this.out.flush();
        }
    } catch (IOException | NullPointerException e) {
        try {
            if (this.socket != null) {
                this.socket.close();
            }
        } catch (IOException closeException) {

        } finally {
            ConnectionHandler.removeClient(this);
            this.error = true;
        }
    }
}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
