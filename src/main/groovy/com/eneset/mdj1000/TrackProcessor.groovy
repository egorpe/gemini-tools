package com.eneset.mdj1000

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import javafx.scene.image.PixelReader
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.imgscalr.Scalr
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.Tag
import org.jaudiotagger.tag.images.Artwork
import org.slf4j.LoggerFactory;
import org.slf4j.Logger

import javax.imageio.ImageIO
import javafx.scene.paint.Color
import java.awt.image.BufferedImage
import java.awt.image.BufferedImageOp

import com.google.common.io.Files

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

    void updateArtwork() {
        Tag tag = mp3.getID3v2Tag()
        Artwork artwork = tag.firstArtwork
        if (artwork) {
            log.info('Found artwork: "{}"', artwork.mimeType)
            BufferedImage srcImage = ImageIO.read(new ByteArrayInputStream(artwork.binaryData))
            Image artworkImage = SwingFXUtils.toFXImage(
                Scalr.resize(srcImage, Scalr.Method.AUTOMATIC, 85, new BufferedImageOp[0]), null)
            srcImage.flush()
            PixelReader pixelReader = artworkImage.pixelReader
            byte[] rawData = new byte[(int)(artworkImage.width * artworkImage.height * 3.0D)]

            int ptr = 0;
            for (int j =  0; j < artworkImage.width; j++) {
                for (int i = 0; i < artworkImage.height; i++) {
                   Color tempColor = pixelReader.getColor(i, j)
                    rawData[(ptr++)] = ((byte)(int)(tempColor.red * 255.0D))
                    rawData[(ptr++)] = ((byte)(int)(tempColor.green * 255.0D))
                    rawData[(ptr++)] = ((byte)(int)(tempColor.blue * 255.0D))
                }
            }
            Files.write(rawData, new File(path + File.separator + 'GEMINI/covers' + File.separator + track.artworkFilePath))
            db.updateCover(track.id, true)
        } else {
            log.info('Artwork not found in MP3 file')
            db.updateCover(track.id, false)
        }

    }

    void updateFilename() {
        String fileName = FilenameUtils.getName(track.originalPath)
        String filePath = FilenameUtils.getPath(track.originalPath)
        String newFilePath = (this.path + File.separator + filePath + track.bpm + ',' + track.key + '-' + fileName)
        if (fileName.startsWith(track.bpm + ',' + track.key)) {
            log.info('File has already been renamed')
        } else {
            log.info('New filename is "{},{}-{}"', track.bpm, track.key, fileName)
            log.info('Moving to {}', newFilePath)
            FileUtils.moveFile(new File(this.path + File.separator + track.originalPath), new File(newFilePath))
            db.updateFilename(track.id, File.separator + filePath + track.bpm + ',' + track.key + '-' + fileName)
        }
    }

}
