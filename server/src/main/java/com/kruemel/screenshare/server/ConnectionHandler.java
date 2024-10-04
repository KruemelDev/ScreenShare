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
		CommandHandler commandHandler = new CommandHandler(this.client);
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
