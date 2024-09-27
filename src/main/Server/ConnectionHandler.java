package main.Server;

import main.Client.WindowManager;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

public class ConnectionHandler implements Runnable{
	
	public static ArrayList<Client> clients = new ArrayList<Client>();
	public Client client;
	
	public ConnectionHandler(Client client) {
		this.client = client;
	}


	@Override
	public void run() {
		this.client.status = Client.statusModes.AVAILABLE;
		CommandHandler commandHandler = new CommandHandler(this);
		commandHandler.HandleCommands(this.client);
	}

	public static ArrayList<String> getAvailableClientsName() {
		ArrayList<String> clientList = new ArrayList<>();
		for(Client client : clients) {
			if (client.status == Client.statusModes.AVAILABLE){
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
	synchronized public static void removeClient(Client client) {
		clients.remove(client);
	}

	public static boolean DuplicateName(String name){
        for (Client client : clients) {
            if (client.name.equals(name)) {
                return true;
            }
        }
		return false;

	}
}
