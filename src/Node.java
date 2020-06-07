import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

class Node implements Serializable {

    String ip;
    int port;
    //server
    ServerSocket ss;
    Socket s;
    ObjectOutputStream oos;
    ObjectInputStream ois;
    //client
    Socket cs;
    ObjectOutputStream oocs;
    ObjectInputStream oics;

    Node() {

    }

    Node(String ip) throws IOException {
        this.ip = ip;
    }

    Node(String ip, int port) throws IOException {
        this.ip = ip;
        this.port = port;
    }

    //initializes a node as a server
    void init() throws IOException {
        ss = new ServerSocket(this.port);
        System.out.print("\n" + this.getClass().getSimpleName() +  InetAddress.getByName(Globals.publisher_1_ip) + ":" + ss.getLocalPort() + " -> Waiting for client connection...\n");
        s = ss.accept();
        System.out.println(this.getClass().getSimpleName() +  InetAddress.getByName(Globals.publisher_1_ip) + ":" + ss.getLocalPort() + " -> Connected with client" + s.getRemoteSocketAddress());
        oos = new ObjectOutputStream(s.getOutputStream());
        ois = new ObjectInputStream(s.getInputStream());
    }

    //the client nodes uses this to connect to the server
    void connect(Node server) throws IOException {
        System.out.println("\n" + this.getClass().getSimpleName() + InetAddress.getByName(Globals.publisher_1_ip) + " -> Connecting to " + server.getClass().getSimpleName() + InetAddress.getByName(Globals.broker_1_ip));
        cs = new Socket(server.ip, server.port);
        System.out.println(this.getClass().getSimpleName() + InetAddress.getByName(Globals.publisher_1_ip) + ":" + cs.getLocalPort() + " -> Connected to " +  server.getClass().getSimpleName() + cs.getRemoteSocketAddress());
        oocs = new ObjectOutputStream(cs.getOutputStream());
        oics = new ObjectInputStream(cs.getInputStream());

    }

    void serverDisconnect() throws IOException {
        System.out.println("\n" + this.getClass().getSimpleName() + InetAddress.getByName(Globals.publisher_1_ip) + ":" + ss.getLocalPort() + " -> Closing connection...");
        oos.close();
        ois.close();
        s.close();
        ss.close();
    }

    void clientDisconnect() throws IOException {
        System.out.println("\n" + this.getClass().getSimpleName() + InetAddress.getByName(Globals.publisher_1_ip) + ":" + cs.getLocalPort() + " -> Closing connection...");
        oocs.close();
        oics.close();
        cs.close();
    }
}
