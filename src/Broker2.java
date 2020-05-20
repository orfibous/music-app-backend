import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

class Broker2 extends Node{

    static HashMap<String, Node> nodes = new HashMap<String, Node>();
    static ArrayList<ArtistName> artistList = new ArrayList<ArtistName>(1);
    static ArrayList<ArtistName> artistsOfBroker1 = new ArrayList<>();
    static ArrayList<ArtistName> artistsOfBroker2 = new ArrayList<>();
    static ArrayList<ArtistName> artistsOfBroker3 = new ArrayList<>();

    //CONSTRUCTORS

    Broker2(String ip) throws IOException {
        super(ip);
    }

    Broker2(String ip, int port) throws IOException {
        super(ip, port);
    }

    public Broker2() {
        super();
    }

    //MAIN METHOD
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, InvalidDataException, UnsupportedTagException {

        Broker2 b = new Broker2();
        b.acceptPublisherConnections();
        b.getArtistList();
        calculateKeys();
        printKeys();
        b.letArtistKnow();

        for (int t = 0; t<artistList.size(); t++) {
            System.out.println("-----" + t + "  " +  artistList.get(t).getArtist());
            artistList.get(t).printBroker();
        }

        b.acceptConsumerConnections();

        b.upload(b.getNode("c0"), b.getNode("p0"));

    }

    //FUNCTIONS

    //broker opens and listens for publishers to connect, each node is stored to the nodes list
    //the next one will connect to the next port
    void acceptPublisherConnections() throws IOException {
        Node p = new Node(Globals.broker_2_ip, Globals.publisher_accept_port2);
        int i = 0;
        while (i < 1) {
            Node n = new Node(p.ip, Globals.publisher_accept_port2);
            n.init();
            nodes.put("p"+i, n);

            if (n.s.isConnected()) {
                Globals.publisher_accept_port2++;
                i++;
            }
        }
    }

    //Same with Consumers, each one is laso stored in the nodes list
    void acceptConsumerConnections() throws IOException, ClassNotFoundException {
        Node c = new Node(Globals.broker_2_ip, Globals.consumer_accept_port1);
        int i = 0;
        while (i < 1) {
            Node n = new Node("c"+i, Globals.consumer_accept_port1);
            n.init();
            nodes.put("c"+i, n);

            if (n.s.isConnected()) {
                Globals.consumer_accept_port1++;
                i++;
            }
        }
    }

    //receives the artistList from the Publisher
    void getArtistList() throws IOException, ClassNotFoundException {
        artistList = (ArrayList<ArtistName>) this.getNode("p0").ois.readObject();
        printArtistList();
    }

    //receives a String and returns the hash with SHA-1 method
    public static String hashThis(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            byte[] message = md.digest(input.getBytes());
            BigInteger no = new BigInteger(1, message);
            String hashtext = no.toString(16);
            while (hashtext.length() < 32) {
                hashtext = "0" + hashtext;
            }
            return hashtext;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    //After receiving the list the broker splits all the artist in 3 brokers
    //Since all brokers have the same ip and port, it works with an id. In the lab
    //it will be calculated with the ip and port of each broker
    static void calculateKeys() {
        System.out.print("---------- Allocating keys ----------" + "\n");
        ArrayList<String> ipList = new ArrayList<>();
        ArrayList<String> hashBrokers = new ArrayList<String>();
        ArrayList<String> hashArtists = new ArrayList<String>();
        ipList.add(Globals.broker_1_ip + " " + Globals.publisher_accept_port1);
        ipList.add(Globals.broker_2_ip + " " + Globals.publisher_accept_port2);
        ipList.add(Globals.broker_3_ip + " " + Globals.publisher_accept_port3);

        for (String ip : ipList) {
            hashBrokers.add(hashThis(ip));
        }

        for (int k = 0; k < artistList.size(); k++) {
            hashArtists.add(hashThis(artistList.get(k).getArtist()));
            int comparisonResult1 = hashArtists.get(k).compareTo(hashBrokers.get(0));
            int comparisonResult2 = hashArtists.get(k).compareTo(hashBrokers.get(1));
            int comparisonResult3 = hashArtists.get(k).compareTo(hashBrokers.get(2));

            if(comparisonResult1 < 0 && artistsOfBroker1.size() < artistList.size()/3 ) {
                artistsOfBroker1.add(artistList.get(k));

            } else if (comparisonResult1 > 0 &&  comparisonResult2 < 0 && artistsOfBroker2.size() < artistList.size()/3) {
                artistsOfBroker2.add(artistList.get(k));

            } else if (comparisonResult2 > 0 &&  comparisonResult3 < 0 && artistsOfBroker3.size() < artistList.size()/3) {
                artistsOfBroker3.add(artistList.get(k));

            } else {
                manipulate(k);
            }
        }

    }

    //method which manipulates the allocation of the keys in case one of the brokers
    //remains with 0 or just 1 key.
    static void manipulate(int index) {
        if (artistsOfBroker1.size() < artistList.size()/3) {
            artistsOfBroker1.add(artistList.get(index));
        } else if (artistsOfBroker2.size() < artistList.size()/3) {
            artistsOfBroker2.add(artistList.get(index));
        }else {
            artistsOfBroker3.add(artistList.get(index));
        }
    }

    void letArtistKnow() throws IOException {
        for(int t = 0; t<artistsOfBroker1.size(); t++) {
            //System.out.println("----------Broker1---------");
            artistsOfBroker1.get(t).setNode(Globals.broker_1_ip, Globals.consumer_accept_port1);
            /*System.out.println("-----" + t + "  " +  artistsOfBroker1.get(t).getArtist());
            artistsOfBroker1.get(t).printBroker();*/

        }

        for(int v = 0; v<artistsOfBroker2.size(); v++) {
            //System.out.println("----------Broker2---------");
            artistsOfBroker2.get(v).setNode(Globals.broker_2_ip, Globals.consumer_accept_port1);
            /*System.out.println("-----" + v + "  " +  artistsOfBroker2.get(v).getArtist());
            artistsOfBroker2.get(v).printBroker();*/
        }

        for(int n = 0; n<artistsOfBroker3.size(); n++) {
            //System.out.println("----------Broker3---------");
            artistsOfBroker3.get(n).setNode(Globals.broker_3_ip, Globals.consumer_accept_port1);
            /*System.out.println("-----" + n + "  " +  artistsOfBroker3.get(n).getArtist());
            artistsOfBroker3.get(n).printBroker();*/
        }


    }

    static Boolean exists(ArrayList<ArtistName> list, ArtistName a) {
        boolean exists = false;
        for(int t = 0; t<list.size(); t++) {
            if (list.get(t).artist.equals(a.artist)) {
                exists = true;
            } else {
                exists = false;
            }
        }
        return exists;
    }

    //checks if the given artist if registered in this broker
    static boolean artistFoundInBroker(String artist) {
        boolean found = false;
        for (ArtistName a : artistsOfBroker1) {
            if (artist.equals(a.artist)) {
                found = true;
            }
        }
        return found;
    }

    //sends the keys to the consumers, 3 lists with the artists registered to each broker
    void sendKeys(Node destination) throws IOException {
        destination.oos.writeObject(artistsOfBroker1);
        destination.oos.writeObject(artistsOfBroker2);
        destination.oos.writeObject(artistsOfBroker3);
    }

    //sends the chunks of a musicFile right after the broker reads them from the publisher
    void download(Node source, Node destination) throws IOException, ClassNotFoundException {

        System.out.println("--------------------Waiting for transfers...-------------------");
        try {
            int i=1;
            while (true) {
                System.out.print("\nTransferring chunk " + i + "...");
                Value v = (Value) source.ois.readObject();
                v.printValue();
                destination.oos.writeObject(v);
                destination.oos.flush();
                i++;
            }
        }
        catch (NullPointerException npe) {
            System.out.println("NULL\n--------------------\nValue transferred successfully!\n--------------------\n");
        }
        destination.oos.writeObject(null);
        this.acceptConsumerConnections();
    }

    //check is the requested artist is allocated here then forward the request to the publisher
    void upload(Node source, Node destination) throws IOException, ClassNotFoundException {

        System.out.print("\n--------------------\nWaiting for transfers...\n--------------------\n");
        try {
            int i=1;
            while (true) {
                System.out.print("\nTransferring chunk " + i + "...");
                Value v = (Value) source.ois.readObject();
                v.printValue();
                boolean exists = artistFoundInBroker(v.req.getArtist());
                if (exists) {
                    destination.oos.writeObject(v);
                    destination.oos.flush();
                }else {
                    sendKeys(source);
                }
                i++;
            }

        }
        catch (NullPointerException npe) {
            System.out.print("NULL\n--------------------\nValue transferred successfully!\n--------------------\n");
        }
        destination.oos.writeObject(null);
        download(destination,source);

    }


    Node getNode(String nodeKey) {
        return this.nodes.get(nodeKey);
    }


    //PRINTING METHODS

    static void printArtistList() {
        System.out.print("--------------------artistList--------------------" + "\n");
        artistList.forEach(ArtistName::printArtist);
    }

    static void printKeys() {
        System.out.println("----------Broker1----------");
        artistsOfBroker1.forEach(ArtistName::printArtist);
        System.out.println("----------Broker2----------");
        artistsOfBroker2.forEach(ArtistName::printArtist);
        System.out.println("----------Broker3----------");
        artistsOfBroker3.forEach(ArtistName::printArtist);
    }






















}
