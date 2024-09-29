package com.kruemel.screenshare.server;

import java.io.IOException;
import java.util.ArrayList;

public class ConnectionHandler implements Runnable{
	
	public static ArrayList<ClientData> clients = new ArrayList<ClientData>();
	public ClientData client;
	
	public ConnectionHandler(ClientData client) {
		this.client = client;
	}


	@Override
	public void run() {
		this.client.status = ClientData.statusModes.AVAILABLE;
		CommandHandler commandHandler = new CommandHandler(this);
		commandHandler.HandleCommands(this.client);
	}

	public static ArrayList<String> getAvailableClientsName() {
		ArrayList<String> clientList = new ArrayList<>();
		for(ClientData client : clients) {
			if (client.status == ClientData.statusModes.AVAILABLE){
				clientList.add(client.name);
			}

		}
		return clientList;
	}

	public void WriteMessage(String message) {
		try {
			if (this.client.out != null) {
				this.client.out.writeUTF(message);
				this.client.out.flush();
			}
		} catch (IOException e) {

            try {
				this.client.socket.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
			finally {
				removeClient(client);
			}
        }
	}
	synchronized public static void removeClient(ClientData client) {
		clients.remove(client);
	}

	public static boolean DuplicateName(String name){
        for (ClientData client : clients) {
            if (client.name.equals(name)) {
                return true;
            }
        }
		return false;

	}
}
