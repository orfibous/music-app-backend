package edu.aueb.cs.distributedsystems;

import java.io.Serializable;

class Request implements Serializable {

    static final long serialVersionUID = 1L;
    String artist;
    String song;

    Request (String artist, String song) {
        this.artist = artist;
        this.song = song;
    }

    public Request(Request r) {
        this.artist = r.artist;
        this.song = r.song;
    }

    String getArtist() {
        return artist;
    }

    String getSong() {
        return song;
    }
}
