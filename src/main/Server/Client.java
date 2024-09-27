package main.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.*;

public class Client {
	public String name;
	public Socket socket;
	public DataInputStream in;
	public DataOutputStream out;


	public enum statusModes{
		AVAILABLE,
		TRANSFER_SCREEN,

	}

	public statusModes status;
	
	public Client(String name, Socket socket) {
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
		Client client = (Client) obj;
		return this.name.equals(client.name);
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}
}
