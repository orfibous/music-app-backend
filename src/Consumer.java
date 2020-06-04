import java.io.*;
import java.net.InetAddress;
import java.util.ArrayList;

class Consumer extends Node{

    static ArrayList<ArtistName> artistList = new ArrayList<ArtistName>(1);
    static ArrayList<ArtistName> artistsOfBroker1 = new ArrayList<>();
    static ArrayList<ArtistName> artistsOfBroker2 = new ArrayList<>();
    static ArrayList<ArtistName> artistsOfBroker3 = new ArrayList<>();

    //CONSTRUCTORS

    Consumer() {
        super();
    }

    Consumer(String ip, int port) throws IOException {
        super(ip, port);
    }

    //MAIN METHOD

    public static void main(String[] args) throws IOException, ClassNotFoundException, InterruptedException {

        Consumer c = new Consumer();
        Broker b = new Broker(Globals.broker_1_ip, Globals.consumer_accept_port1);
        c.connect(b);

        Request r = new Request("dogsounds", "Brandenburg Concerto III, Alle");
        //To check the version with an artist that the consumer doesn't know comment the line below
        //and change youw request
        //artistsOfBroker1.add(new ArtistName(r.artist));
        c.request(r);

        Thread.sleep(2000);

        r = new Request("Rafael Krux", "Barnville");
        c.request(r);

        Thread.sleep(2000);

        r = new Request("dogsounds", "Brandenburg Concerto III, Alle");
        c.request(r);

        Thread.sleep(99999);
    }

    //FUNCTIONS

    //the consumer requests a song with this method, checks if he can find the registered
    //artist in a broker, otherwise he waits for the keys.
    void request(Request req) throws IOException, ClassNotFoundException {
        boolean exists = artistFound(req.artist);
            System.out.println(this.getClass().getSimpleName() +  InetAddress.getByName(Globals.publisher_1_ip) + ":" + cs.getLocalPort() + " -> Sending request...");
            Value val = new Value(req);
            this.oocs.writeObject(val);
            this.oocs.flush();
            this.oocs.reset();
            //recieveKeys();
            //printArtistList();
            this.oocs.writeObject(null);
            System.out.println(this.getClass().getSimpleName() +  InetAddress.getByName(Globals.publisher_1_ip) + ":" + cs.getLocalPort() + " -> Request sent successfully!");

            //if (exists) {
                pull();
            //}else {
                //recieveKeys();
                //printAllArtistList();
            //}

    }

    //reads the chunks of the requested song
    void pull() throws IOException, ClassNotFoundException {

        try {
            int i=1;
            while (true) {
                System.out.print("\nPlaying chunk " + i + "...");
                Value v = (Value) this.oics.readObject();
                v.printValue();
                i++;
            }
        }
        catch (NullPointerException npe) {
            System.out.println("NULL\n--------------------\nMusic played successfully!\n--------------------\n");
        }
    }

    //checks is an artist is in a list
    boolean artistFound(String artist) {
        boolean found = false;
        for (ArtistName a : artistsOfBroker1) {
            if (artist.equals(a.artist)) {
                found = true;
            }
        }
        return found;
    }

    //reads the lists with the keys from broker
    void recieveKeys () throws IOException, ClassNotFoundException {
        /*artistsOfBroker1 = (ArrayList<ArtistName>) this.oics.readObject();
        artistsOfBroker2 = (ArrayList<ArtistName>) this.oics.readObject();
        artistsOfBroker3 = (ArrayList<ArtistName>) this.oics.readObject();*/
        artistList = (ArrayList<ArtistName>) this.oics.readObject();

    }

    //PRINTING METHODS

    static void printArtistList() {
        System.out.print("----------artistList----------" + "\n");
        artistList.forEach(ArtistName::printArtist);
        System.out.print("----------Brokers----------" + "\n");
        artistList.forEach(ArtistName::printPublisher);
        System.out.print("----------Songs----------" + "\n");
        artistList.forEach(ArtistName::printSongList);
    }

    static void printAllArtistList() {
        System.out.print("----------Artists Of Broker 1----------" + "\n" );
        artistsOfBroker1.forEach(ArtistName::printArtist);
        System.out.print("----------Artists Of Broker 2----------" + "\n" );
        artistsOfBroker2.forEach(ArtistName::printArtist);
        System.out.print("----------Artists Of Broker 3----------" + "\n" );
        artistsOfBroker3.forEach(ArtistName::printArtist);
    }











}
