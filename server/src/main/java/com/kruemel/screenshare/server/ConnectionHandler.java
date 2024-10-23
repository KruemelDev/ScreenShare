package com.kruemel.screenshare.server;

import com.kruemel.screenshare.server.clientHandler.ClientData;

import java.util.ArrayList;

public class ConnectionHandler {

	public static ArrayList<ClientData> clients = new ArrayList<ClientData>();

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
