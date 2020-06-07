import com.mpatric.mp3agic.InvalidDataException;
import com.mpatric.mp3agic.UnsupportedTagException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

class ArtistName implements Serializable {

    public String artist;
    public ArrayList<String> songList;
    public Publisher publisher_id; //which publishers has this artist

    public ArtistName(String artist) {
        this.artist = artist;
        songList = new ArrayList<>();
    }

    public void setPublisherID(String ip, int port) throws IOException, InvalidDataException, UnsupportedTagException {
        publisher_id = new Publisher(ip,port);
    }

    public String getArtist() {
        return artist;
    }

    public void printArtist() {
        System.out.println(artist);
    }

    public void printPublisher() {
        System.out.print("Publisher_SERVER " + publisher_id.ip + ":" + publisher_id.port + "\n");
    }

    public void printSongList() {
        for (String s : songList) {
            System.out.println(s + "\n");
        }
    }

}