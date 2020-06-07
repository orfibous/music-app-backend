import com.mpatric.mp3agic.*;
import java.io.*;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class Publisher2 extends Node{

    static List<String> filePathList = new ArrayList<String>(1);
    static ArrayList<MusicFile> musicList = new ArrayList<MusicFile>(1);
    static ArrayList<ArtistName> artistList = new ArrayList<ArtistName>(1);

    //CONSTRUCTORS

    Publisher2(String ip, int port) throws InvalidDataException, IOException, UnsupportedTagException {
        super(ip, port);
    }

    Publisher2() throws InvalidDataException, IOException, UnsupportedTagException {

    }

    Publisher2(String ip, int port, String musicFolder) throws InvalidDataException, IOException, UnsupportedTagException {
        super(ip, port);
        scanFolder(musicFolder);
        importMusic(filePathList);
        fillArtistSongList();

        //printArtistSongList();
    }

    Publisher2(String musicFolder) throws InvalidDataException, IOException, UnsupportedTagException {
        scanFolder(musicFolder);
        importMusic(filePathList);
        fillArtistSongList();
        //printArtistSongList();
    }

    //MAIN METHOD
    public static void main(String[] args) throws IOException, InvalidDataException, UnsupportedTagException, ClassNotFoundException, InterruptedException {

        Publisher2 p = new Publisher2(Globals.publisher_2_ip, Globals.publisher_accept_port2, Globals.publisher_2_datapath);

        Broker b = new Broker(Globals.broker_1_ip, Globals.broker_server_port2);
        p.connect(b);
        p.notifyBroker(artistList);
        p.clientDisconnect();

        p.listen();
    }

    //FUNCTIONS

    //The publisher opens and listens for song requests
    void listen() throws IOException, ClassNotFoundException, InvalidDataException, UnsupportedTagException {
        this.init();
        System.out.println("-------------------Listening for music requests --------------------");
        Value v = new Value();
        try {
            while (true) {
                v = (Value)this.ois.readObject();
                v.printValue();
                System.out.println(v.req.artist);
                locate(v.req);
            }
        }
        catch (NullPointerException npe) {
            System.out.println("-------------------Request received successfully!-------------------");
        }
        System.out.println("qqqqqqq");
    }

    //Locates the artist and song requested and then pushes it to Broker
    void locate(Request req) throws InvalidDataException, IOException, UnsupportedTagException, ClassNotFoundException {
        System.out.println("\n" + this.oics.getClass().getSimpleName() + InetAddress.getByName(Globals.publisher_1_ip) + " -> Got Request for \"" + req.getArtist() + " - " + req.getSong() + "\" from Broker. Locating...");
        for (MusicFile m : musicList) {
            if (m.getArtist().equals(req.artist) && m.getTitle().equals(req.song)) {
                System.out.println("\n" + this.oics.getClass().getSimpleName() + InetAddress.getByName(Globals.publisher_1_ip) + " -> Located \"" + req.getArtist() + " - " + req.getSong() + "\" Pushing...");
                m.printMusic();
                push(m);
            }
        }
    }

    //pushes the given MusicFile in chunks of 512
    void push(MusicFile m) throws IOException, InvalidDataException, UnsupportedTagException, ClassNotFoundException {

        ByteArrayInputStream bis = new ByteArrayInputStream(m.extract);
        byte[] chunk = new byte[524288];
        int count = 0;
        int i = 1;
        while ((count = bis.read(chunk)) != -1) {
            System.out.print("\nSending chunk " + i + "...");
            MusicFile mus = new MusicFile(m.artist, m.title, m.album, m.genre, chunk);
            mus.printMusic();
            Value val = new Value(mus);
            this.oos.writeObject(val);
            this.oos.flush();
            this.oos.reset();
            i++;
        }
        this.oos.writeObject(null);
        this.oos.flush();
        this.oos.reset();
        System.out.println("Publisher_CLIENT" + InetAddress.getByName(Globals.publisher_1_ip) + ":" + this.s.getLocalPort() + " -> Song sent successfully!");
        this.serverDisconnect();
        this.listen();
    }




    //scanning the directory for all mp3 files and collecting them in filePathList
    static void scanFolder(String folderPath) throws IOException {

        System.out.print("\n" + "----------Scanning dataset directory " + folderPath + " for files ending in .mp3----------" + "\n");
        Stream<Path> pathStream = Files.walk(Paths.get(folderPath));
        filePathList = pathStream.map(x -> x.toString())
                .filter(f -> f.endsWith(".mp3"))
                .collect(Collectors.toList());
        printFilePathList();
    }

    //importing all files found into MusicFile objects and then stored in MusicList
    static void importMusic(List<String> filePathList) throws InvalidDataException, IOException, UnsupportedTagException {

        System.out.print("\n" + "----------Importing music with non-empty tag fields----------" + "\n");
        for (String filePath : filePathList) {

            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            byte[] extract = new byte[(int)file.length()];
            fis.read(extract);

            Mp3File mp3file = new Mp3File(filePath);

            if (mp3file.hasId3v1Tag()) {
                ID3v1 tag =  mp3file.getId3v1Tag();
                if ((tag.getArtist()!=null && !tag.getArtist().isEmpty())
                        && (tag.getTitle()!=null && !tag.getTitle().isEmpty())
                        && (tag.getAlbum()!=null && !tag.getAlbum().isEmpty())
                        && (tag.getGenreDescription()!=null)) {
                    MusicFile musicFile = new MusicFile(tag.getArtist(), tag.getTitle(), tag.getAlbum(), tag.getGenreDescription(), extract);
                    ArtistName artistName = new ArtistName(tag.getArtist());
                    musicList.add(musicFile);
                    fillArtistList();
                }
            }
            else if (mp3file.hasId3v2Tag()) {
                ID3v2 tag = mp3file.getId3v2Tag();
                if ((tag.getArtist()!=null && !tag.getArtist().isEmpty())
                        && (tag.getTitle()!=null && !tag.getTitle().isEmpty())
                        && (tag.getAlbum()!=null && !tag.getAlbum().isEmpty())
                        && (tag.getGenreDescription()!=null)) {
                    MusicFile musicFile = new MusicFile(tag.getArtist(), tag.getTitle(), tag.getAlbum(), tag.getGenreDescription(), extract);
                    ArtistName artistName = new ArtistName(tag.getArtist());
                    musicList.add(musicFile);
                    fillArtistList();
                }
            }
            else {
                System.out.println("No tags found on this file, skipping...");
                continue;
            }
        }
        printMusicList();
    }

    //identify all artists and store them in artistList with no doubles
    public static void fillArtistList() throws IOException, InvalidDataException, UnsupportedTagException {

        for (MusicFile musicFile : musicList) {
            ArtistName a = new ArtistName(musicFile.artist);
            boolean exists = false;
            for (ArtistName artistName : artistList) {
                if (artistName.artist.equals(musicFile.artist)) {
                    //System.out.println("Artist already in list, skipping...");
                    exists = true;
                    break;
                }
            }
            if (!exists) {
                artistList.add(a);
            }
        }
    }

    //each artist have a library with the songs available
    public static void fillArtistSongList() throws InvalidDataException, IOException, UnsupportedTagException {

        for (int t=0; t<artistList.size(); t++) {
            for (int n=0; n<musicList.size(); n++) {
                if (artistList.get(t).getArtist().equals(musicList.get(n).getArtist())) {
                    artistList.get(t).songList.add(musicList.get(n).getTitle());
                }
            }
        }

        setPublisherIDs();
        printArtistSongList();
    }

    //Publisher sends the artistList to Broker
    void notifyBroker(ArrayList<ArtistName> artistList) throws IOException {
        this.oocs.writeObject(artistList);
        this.oocs.flush();
    }




    //PRINTING METHODS

    static void printFilePathList() {
        filePathList.forEach(System.out::println);
        System.out.print("----------Files ending in .mp3 found: " + filePathList.size() + "----------" + "\n");
    }

    static void printMusicList() {
        musicList.forEach(MusicFile::printMusic);
        System.out.println("----------Music with non-empty tag fields imported: " + musicList.size() + "----------" + "\n");
    }

    static void printArtistList() {
        System.out.println("---------- ArtistList ----------" + "\n");
        artistList.forEach(ArtistName::printArtist);
        System.out.println("----------Unique artists imported: " + artistList.size() + " ----------" + "\n");
    }

    static void printArtistSongList() {
        System.out.print("----------ArtistSongList ----------" + "\n");
        for (ArtistName artistName : artistList) {
            System.out.print("--------------Library of " + artistName.getArtist() + "is on ");
            artistName.printPublisher();
            System.out.print("--------------\n");
            artistName.printSongList();
        }
    }

    public static void setPublisherIDs () throws InvalidDataException, IOException, UnsupportedTagException {
        for (ArtistName artistName : artistList) {
            artistName.setPublisherID(Globals.publisher_1_ip, Globals.publisher_accept_port1);
            artistName.printPublisher();
        }
    }














}


