import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

class ArtistName implements Serializable {

    public String artist;
    public ArrayList<String> songList;
    public Node broker_id;

    public ArtistName(String artist) {
        this.artist = artist;
        songList = new ArrayList<>();
    }

    public ArtistName(ArtistName a) {
        this.artist = a.getArtist();
        songList = new ArrayList<>();
    }

    public void setNode(String ip, int port) throws IOException {
        broker_id = new Node(ip,port);
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public void printArtist() {
        System.out.println(artist);
    }

    public void printBroker() {
        System.out.println("Broker " + broker_id.ip + " in " + broker_id.port );
    }

    public void printSongList() {
        for (String s : songList) {
            System.out.println(s);
        }
    }

}