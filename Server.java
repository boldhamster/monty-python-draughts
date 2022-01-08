// 0904889y Emmet Young

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


// The Server manages the input/output between clients
public class Server implements Runnable{

    // The ClientRunner class allocates resources for each new client
    private class ClientRunner implements Runnable {
        private Socket socket = null;
        private Server parent = null;
        private ObjectInputStream inputStream = null;
        private ObjectOutputStream outputStream = null;

        public ClientRunner(Socket socket, Server parent) {
            this.socket = socket;
            this.parent = parent;
            try {
                outputStream = new ObjectOutputStream(this.socket.getOutputStream());
                inputStream = new ObjectInputStream(this.socket.getInputStream());
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

        // This method checks whether a GameUpdate is being outputted by any clients
        // If it receives a message it sends it to all the clients connected to the server
        public void run() {
            try {
                GameUpdate message = null;
                while((message = (GameUpdate)inputStream.readObject())!= null) {
                    this.parent.sendToClients(message);
                }
                inputStream.close();
            }catch(ClassNotFoundException e) {
                e.printStackTrace();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }

        // This method writes the game update object to the outputStream
        public void sendUpdate(GameUpdate gUp) {
            try {
                outputStream.writeObject(gUp);
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ServerSocket server;
    private ArrayList<ClientRunner> clients = new ArrayList<ClientRunner>();

    // The Server constructor connects the server to a ServerSocket port so it can output messages
    public Server() {
        try {
            server = new ServerSocket(8765);
        }catch(IOException e) {
            e.printStackTrace();
        }
    }

    // This run method searches for new clients and adds them to the ClientRunner array
    public void run() {
        while(true) {
            Socket clientSocket = null;
            try {
                // If there are incoming connections, accept them
                // Then pair them with a socket and an instance of ClientRunner
                // Add the new client to the ClientRunner array and pair them with a new thread
                    clientSocket = server.accept();
                    System.out.println("New client connected");
                    ClientRunner client = new ClientRunner(clientSocket,this);
                    clients.add(client);
                    new Thread(client).start();
            }catch(IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Send the Game Update to all clients on the server
    public void sendToClients(GameUpdate gUp) {
        for(ClientRunner c: clients) {
            if(c != null) {
                c.sendUpdate(gUp);
            }
        }
    }

    // Give a dedicated thread to the Server
    public static void main(String[] args) {
        Thread t = new Thread(new Server());
        t.start();
        try {
            t.join();
        }catch(InterruptedException e) {
            e.printStackTrace();
        }

    }
}
