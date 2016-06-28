package com.eneset.mdj1000

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

class Main {
    static Logger log = LoggerFactory.getLogger(Main.class)

    static main(String[] args) {

        if (args.size() != 1 || args[0].isEmpty()) {
            println 'Usage: gemini-tools <path to usb stick>'
        }

        log.info('Gemini MDJ-1000 Tool, Version 1.0')

        GeminiDatabase db = new GeminiDatabase(args[0] + File.separator + 'GEMINI')

        log.info('Found {} tracks', db.fileCount)

        def tracks = db.tracks

        tracks.each { Track track ->
            TrackProcessor proc = new TrackProcessor(args[0], track, db)
            proc.updateArtwork()
            proc.updateFilename()
        }

        db.close()
    }
}

