package com.eneset.mdj1000

import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File

import org.slf4j.LoggerFactory;
import org.slf4j.Logger


class TrackProcessor {
    static Logger log = LoggerFactory.getLogger(TrackProcessor.class)

    String path
    Track track
    GeminiDatabase db
    MP3File mp3

    TrackProcessor(path, track, db) {
        this.path = path
        this.track = track
        this.db = db
        log.info('Processing track "{}"', FilenameUtils.getName(track.originalPath))
        mp3 = AudioFileIO.read(new File(path + File.separator + track.originalPath))
    }

    void updateFilename() {
        String fileName = FilenameUtils.getName(track.originalPath)
        String filePath = FilenameUtils.getPath(track.originalPath)
        String newFilePath = (this.path + File.separator + filePath + track.key + ',' + track.bpm + '-' + fileName)
        if (fileName.startsWith(track.key + ',' + track.bpm)) {
            log.info('File has already been renamed')
        } else {
            log.info('New filename is "{},{}-{}"', track.key, track.bpm, fileName)
            log.info('Moving to {}', newFilePath)
            FileUtils.moveFile(new File(this.path + File.separator + track.originalPath), new File(newFilePath))
            db.updateFilename(track.id, File.separator + filePath + track.key + ',' + track.bpm + '-' + fileName)
        }
    }

}
