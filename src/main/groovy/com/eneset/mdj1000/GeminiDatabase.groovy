package com.eneset.mdj1000

import org.slf4j.LoggerFactory;
import org.slf4j.Logger

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet
import java.sql.Statement;

class GeminiDatabase {
    static Logger log = LoggerFactory.getLogger(GeminiDatabase.class)
    static String NAME = 'gemini_bd.sqlite'

    Connection conn

    GeminiDatabase(path) {
        Class.forName("org.sqlite.JDBC");
        log.info("Looking for MDJ-1000 database at {}/{}", path, NAME)
        conn = DriverManager.getConnection("jdbc:sqlite:" + path + File.separator + NAME);
        log.info("Database connection successfull")
    }

    int getFileCount() {
        int result = 0
        Statement stat = conn.createStatement()
        ResultSet rs = stat.executeQuery("select count(*) as tracks from outer_table")
        if (rs.next()) {
            result = rs.getInt('tracks')
        }
        rs.close()
        stat.close()
        return result
    }

    List<Track> getTracks() {
        def tracks = []
        Statement stat = conn.createStatement()
        ResultSet rs = stat.executeQuery("select * from outer_table")
        while (rs.next()) {
            Track track = new Track()
            track.id = rs.getInt('id')
            track.originalPath = rs.getString('absolute_path')
            track.artworkFilePath = rs.getString('artwork_file_path')
            track.bpm = Math.round(new Double(rs.getString('bpm')))
            track.key = rs.getString('key')
            tracks += track
        }
        rs.close()
        stat.close()
        return tracks
    }

    void close() {
        conn.close()
    }

    void updateCover(String id, boolean haveCover) {
        String haveCoverValue = haveCover ? 'yes' : 'no'
        Statement stat = conn.createStatement()
        stat.executeUpdate('update outer_table set have_cover="' + haveCoverValue + '" where id="' + id + '"')
        stat.close()
    }

    void updateFilename(String id, String fileName) {
        Statement stat = conn.createStatement()
        stat.executeUpdate('update outer_table set absolute_path="' + fileName + '" where id="' + id + '"')
        stat.close()
    }
}
