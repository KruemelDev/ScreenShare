package com.kruemel.screenshare.server;

import java.util.ArrayList;

public class ConnectionHandler implements Runnable{

	public static ArrayList<ClientData> clients = new ArrayList<ClientData>();
	public ClientData client;

	public ConnectionHandler(ClientData client) {
		this.client = client;
	}


	@Override
	public void run() {
		CommandHandler commandHandler = new CommandHandler(this.client);
		commandHandler.HandleCommands(this.client);
	}

	public static ArrayList<String> getAvailableClientsName() {
		ArrayList<String> clientList = new ArrayList<>();
		for(ClientData client : clients) {
			clientList.add(client.name);
		}
		return clientList;
	}


	synchronized public static void removeClient(ClientData client) {
		clients.remove(client);
	}

	public static boolean ClientOnline(String name){
		for (ClientData client : clients) {
			if (client.name.equals(name)) {
				return true;
			}
		}
		return false;

	}
}
