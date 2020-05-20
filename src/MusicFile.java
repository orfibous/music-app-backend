import sun.swing.MenuItemLayoutHelper;

import java.io.Serializable;

class MusicFile implements Serializable {

    String artist;
    String title;
    String album;
    String genre;
    byte[] extract;

    MusicFile() {

    }

    MusicFile(String artist, String title, String album, String genre, byte[] extract) {
        this.artist = artist;
        this.title = title;
        this.album = album;
        this.genre = genre;
        this.extract = extract;
    }

    String getArtist() {
        return artist;
    }

    String getTitle() {
        return title;
    }

    String getAlbum() {
        return album;
    }

    String getGenre() {
        return genre;
    }

    byte[] getExtract() {
        return extract;
    }

    void setArtist(String artist) {
        this.artist = artist;
    }

    void setTitle(String title) {
        this.title = title;
    }

    void setAlbum(String album) {
        this.album = album;
    }

    void setGenre(String genre) {
        this.genre = genre;
    }

    void setExtract(byte[] extract) {
        this.extract = extract;
    }

    void printMusic() {
        System.out.println(artist + ";" + title + ";" + album + ";" + genre);
        //System.out.print(new String(extract));
    }

    void printTitle() {
        System.out.println(title);
    }
}