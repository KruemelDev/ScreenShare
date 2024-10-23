package com.kruemel.screenshare.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.kruemel.screenshare.dto.Packet;
import com.kruemel.screenshare.server.clientHandler.ClientData;
import com.kruemel.screenshare.server.clientHandler.CommandListener;

public class Server {

    public static void main(String[] args){
        ServerSocket server;

        try {
            server = new ServerSocket();
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress("0.0.0.0", Integer.parseInt(args[0])));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while(true) {
            try {

                Socket socket = server.accept();
                DataInputStream in = new DataInputStream(socket.getInputStream());
                String namePacket = in.readUTF();

                ObjectMapper objectMapper = new ObjectMapper();
                Packet packet = objectMapper.readValue(namePacket, Packet.class);

                String name = packet.getData();

                if(Objects.equals(packet.getCommand(), "name") && !ConnectionHandler.ClientOnline(name) && !name.contains("|")) {
                    ClientData client = new ClientData(name, socket);
                    ConnectionHandler.clients.add(client);
                    Thread commandListener = new Thread(new CommandListener(client));
                    commandListener.start();

                }
                else{
                    DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                    Packet closePacket = new Packet("closeConnection");
                    ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                    String json = ow.writeValueAsString(closePacket);
                    try{
                        out.writeUTF(json);
                    }
                    catch(EOFException e){
                        continue;
                    }

                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }



    }
}