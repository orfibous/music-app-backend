package edu.aueb.cs.distributedsystems;

import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;

class Broker extends Node{

    static final long serialVersionUID = 1L;
    static HashMap<String, Node> nodes = new HashMap<String, Node>();
    static ArrayList<ArtistName> artistList = new ArrayList<ArtistName>(1);
    static ArrayList<ArtistName> artistsOfBroker1 = new ArrayList<>();
    static ArrayList<ArtistName> artistsOfBroker2 = new ArrayList<>();
    static ArrayList<ArtistName> artistsOfBroker3 = new ArrayList<>();

    //CONSTRUCTORS

    Broker(String ip) throws IOException {
        super(ip);
    }

    Broker(String ip, int port) throws IOException {
        super(ip, port);
    }

    public Broker() {
        super();
    }

    //MAIN METHOD
    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException, InvalidDataException, UnsupportedTagException {

        Broker b1 = new Broker(Globals.broker_1_ip, Globals.broker_server_port1);
        //nodes.put(b1.ip + ":" + b1.port, b1);

        Broker b2 = new Broker(Globals.broker_1_ip, Globals.broker_server_port2);
        //nodes.put(b2.ip + ":" + b2.port, b2);

        Broker b3 = new Broker(Globals.broker_1_ip, Globals.consumer_accept_port1);
        //nodes.put(b3.ip + ":" + b3.port, b3);

        //the broker initializes as server and receives the artists from both Publishers
        b1.init();
        b1.getArtistList();
        b1.serverDisconnect();

        b2.init();
        b2.getArtistList();
        b2.serverDisconnect();

        //calculateKeys();
        //printKeys();

        //then initializes as server for the users
        b3.init();
        //sends the artistList to the consumer so he can see all songs
        b3.notifyUser(artistList);
        //Mode upload is activated, waits for requests, pushes them to publishers, receives
        //chunks and send them to user.
        b3.upload();

    }

    //FUNCTIONS

    void upload() throws IOException, ClassNotFoundException, InvalidDataException, UnsupportedTagException {
        System.out.print("\n--------------------\nWaiting for transfers...\n--------------------\n");
        Publisher p = new Publisher();
        try {
            int i=1;
            while (true) {
                System.out.print("\nTransferring chunk " + i + "...");
                Value v = (Value) this.ois.readObject();
                v.printValue();
                if (i==1) {
                    System.out.println(v.req.artist);
                    p = locatePublisher(v.req.artist);
                    //System.out.println(p.ip);
                    this.connect(p);
                }

                this.oocs.writeObject(v);
                this.oocs.flush();
                i++;
            }

        }
        catch (NullPointerException npe) {
            System.out.print("NULL\n--------------------\nValue transferred successfully!\n--------------------\n");
        }
        //this.oocs.writeObject(null);
        this.download();

    }



    void download() throws IOException, ClassNotFoundException, InvalidDataException, UnsupportedTagException {

        System.out.println("--------------------Waiting for transfers...-------------------");
        try {
            int i=1;
            while (true) {
                System.out.print("\nTransferring chunk " + i + "...");
                Value v = (Value) this.oics.readObject();
                v.printValue();
                this.oos.writeObject(v);
                this.oos.flush();
                i++;
            }
        }
        catch (NullPointerException npe) {
            System.out.println("NULL\n--------------------\nValue transferred successfully!\n--------------------\n");
        }
        this.oos.writeObject(null);
        this.clientDisconnect();
        //After downloading the song to user mode upload is activated again like an eternal loop
        this.upload();
    }

    //sends the artists to the consumer
    void notifyUser(ArrayList<ArtistName> artistList) throws IOException {
       /* this.oos.writeObject(artistsOfBroker1);
        this.oos.writeObject(artistsOfBroker2);
        this.oos.writeObject(artistsOfBroker3);*/
        this.oos.writeObject(artistList);
        this.oos.flush();
    }

    //receives the artistList from the Publisher
    void getArtistList() throws IOException, ClassNotFoundException {
        ArrayList<ArtistName> temp;
        temp = (ArrayList<ArtistName>) this.ois.readObject();
        artistList.addAll(temp);
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

    //the broker access the artists'info to see in which publisher he belongs
    static Publisher locatePublisher(String a) {
        Publisher p = new Publisher();
        for (int t=0; t<artistList.size(); t++) {
            if (artistList.get(t).getArtist().equals(a)) {
                p = artistList.get(t).publisher_id;
            }
            else {
                //System.out.println("No publisher for this artist");
            }
        }
        return p;
    }


    //PRINTING METHODS

    static void printArtistList() {
        System.out.print("--------------------artistList--------------------" + "\n");
        artistList.forEach(ArtistName::printArtist);
        artistList.forEach(ArtistName::printPublisher);
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
