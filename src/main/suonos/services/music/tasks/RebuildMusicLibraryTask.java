/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package suonos.services.music.tasks;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.lucene.search.Query;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.TagField;
import org.jaudiotagger.tag.TagTextField;

import com.github.am0e.commons.AntLib;
import com.github.am0e.commons.utils.StringUtil;

import suonos.app.utils.FileWalker;
import suonos.app.utils.FilesUtils;
import suonos.app.utils.TagUtils;
import suonos.imagems.Imagems;
import suonos.lucene.Statement;
import suonos.models.TaskError;
import suonos.models.media.MediaTag;
import suonos.models.media.MediaTagValue;
import suonos.models.media.MediaTags;
import suonos.models.media.MetaDataFolder;
import suonos.models.music.AlbumMetaData;
import suonos.models.music.MusicAlbum;
import suonos.models.music.MusicLib;
import suonos.models.music.MusicTrack;
import suonos.models.music.TrackMetaData;
import suonos.services.tasks.AbstractTask;

/**
 * Get mp4 file information: mplayer -vo null -ao null -frames 0 -identify
 * videofile.mp4
 * 
 * @author anthony
 *
 */
public class RebuildMusicLibraryTask extends AbstractTask {

    private MediaTags tags = lib.instanceOf(MediaTags.class);
    private MusicLib musicLib;
    private Statement stmt;
    private AlbumMetaData curMetaData;
    private AlbumMetaData newMetaData;
    private String curPath;
    private String musicLibBuildId;
    private List<MusicTrack> tracksList = AntLib.newList();
    private Map<String, MusicAlbum> albumsMap = AntLib.newHashMap();
    private boolean rebuildMetaData;
    private Date importDate = new Date();
    private Imagems imagems = lib.instanceOf(Imagems.class);

    private String[] coverFileNames = new String[] { "cover.jpg", "cover.jpeg", "folder.jpg", "folder.jpeg",
            "artwork.jpg", "artwork.jpeg", "album.jpg", "album.jpeg" };

    public void run(MusicLib mediaLib) throws IOException {
        this.musicLib = mediaLib;
        this.musicLibBuildId = suonos.app.utils.Uids.newUID();

        // a1/a2
        // a1/a2/t1
        // a1/a2/a3/t1
        // a1/a2/a3/t2
        // a1/a2/a3/t3
        // a1/a2/t2
        //
        stmt = lib.stmt();

        buildIndex();

        // musicLibBuildId = "7Bgc9XhpYNdF";
        cleanupIndex();
    }

    private void buildIndex() throws IOException {
        // Walk the files, depth first.
        //
        new FileWalker() {

            @Override
            protected void processFiles(Path folder, List<Path> files) {
                try {
                    processTracks(folder, files);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            protected boolean isKnownFile(Path file) {
                return isKnownMediaFile(file);
            }

        }.walk(musicLib.getRoot());

    }

    private void cleanupIndex() throws IOException {
        // Cleanup the previous objects that have been updated.
        //
        deletePreviousObjects("musicLib");
        stmt.mergeAndCommit();
    }

    public void deletePreviousObjects(String prefix) throws IOException {

        Query query = stmt.queryHelper().createFmtQuery("+{}AutoBuilt:1 -{}BuildId:{}", prefix, prefix,
                musicLibBuildId);

        stmt.deleteObjects(query);
    }

    private boolean isKnownMediaFile(Path file) {
        if (file.toString().endsWith(".mp3"))
            return true;
        if (file.toString().endsWith(".ogg"))
            return true;
        if (file.toString().endsWith(".aif"))
            return true;
        if (file.toString().endsWith(".flac"))
            return true;
        if (file.toString().endsWith(".wav"))
            return true;
        return false;
    }

    protected void processTracks(Path folder, List<Path> trackFiles) throws IOException {
        System.out.println(folder);

        Path path = musicLib.getRelativePath(folder);

        // Load/Create the meta data object for the folder. This is used to
        // generate unique ids for each track.
        //
        curMetaData = getMetaData(path, rebuildMetaData);
        curPath = path.toString();
        newMetaData = new AlbumMetaData();

        for (Path trackFile : trackFiles) {
            processTrack(trackFile);
        }

        // Save meta data object.
        //
        newMetaData.updateFrom(curMetaData);
        newMetaData.save();

        // Save the objects to the data store.
        //
        stmt.saveObjects(albumsMap.values());
        stmt.saveObjects(tracksList);
        stmt.commit();

        // Clear down lists for the next folder.
        //
        tracksList.clear();
        albumsMap.clear();
    }

    private AlbumMetaData getMetaData(Path relativePath, boolean rebuild) throws IOException {
        MetaDataFolder folder = new MetaDataFolder(musicLib.resolvePath(relativePath));

        // Make sure "./suonos" exists.
        //
        Files.createDirectories(folder.getPath());

        AlbumMetaData meta;

        Path path = folder.getMetaData();
        if (rebuild || !Files.isReadable(path)) {
            meta = new AlbumMetaData();
            meta.setFolderId(suonos.app.utils.Uids.newUID());
            meta.setImportDate(importDate);
            meta.setPath(path);
            meta.save();
            Statement.log.debug("New Album Folder ID {}", meta.getFolderId());
        } else {
            meta = FilesUtils.readObject(AlbumMetaData.class, path);
            meta.setPath(path);
        }

        return meta;
    }

    protected void processTrack(Path trackFilePath) {
        try {
            // Process tags in mp3/ogg files.
            //
            String trackFileName = trackFilePath.getFileName().toString();
            AudioFile file = AudioFileIO.read(trackFilePath.toFile());
            Tag fileTags = file.getTag();

            // Get the track title from the tags. This is not the file name!
            //
            String albumTitle = TagUtils.getTagValue(fileTags, FieldKey.ALBUM);

            // Ensure the tracks are registered in the meta data object.
            //
            TrackMetaData trackMetaData = newMetaData.addTrack(trackFileName, curMetaData);

            MusicAlbum album = getAlbum(albumTitle, trackMetaData);
            trackMetaData.setAlbumId(album.getId());

            // Process the track.
            //
            MusicTrack track = getTrack(file, fileTags, trackFilePath, trackMetaData, album);

            tracksList.add(track);

        } catch (CannotReadException | IOException | TagException | ReadOnlyFileException | InvalidAudioFrameException
                | InterruptedException e) {
            addError(new TaskError(trackFilePath.toString(), e));
        }
    }

    private MusicAlbum getAlbum(String albumTitle, TrackMetaData trackMetaData)
            throws IOException, InterruptedException {

        // Album key.
        // The album title by itself is not unique.
        // The album folder is unique but the tracks in the folder can have
        // different album names.
        // Therefore we construct an album key using the path and album title.
        // Therefore a folder containing multiple tracks can be split into many
        // albums.
        //
        String albumKey = curPath.concat("|").concat(albumTitle);

        // Create an album.
        //
        MusicAlbum album = albumsMap.get(albumKey);
        if (album == null) {

            album = new MusicAlbum();
            if (trackMetaData.getAlbumId() == null) {
                album.setId(suonos.app.utils.Uids.newUID());
                Statement.log.debug("New Album UID {}", album.getId());
            } else {
                album.setId(trackMetaData.getAlbumId());
            }

            album.setTitle(albumTitle);
            album.setMusicLibBuildId(musicLibBuildId);
            album.setMusicLibAutoBuilt(true);
            album.setPath(curPath);
            album.setRating(curMetaData.getRating());
            album.setImportDate(curMetaData.getImportDate());
            findCoverArt(album);

            albumsMap.put(albumKey, album);
        }

        return album;
    }

    private void findCoverArt(MusicAlbum album) throws IOException, InterruptedException {
        Path albumPath = musicLib.resolvePath(Paths.get(album.getPath()));

        for (String it : coverFileNames) {
            Path coverFile = albumPath.resolve(it);
            if (Files.exists(coverFile)) {
                importCoverArt(album, coverFile);
                return;
            }
        }
    }

    private void importCoverArt(MusicAlbum album, Path sourceImageFile) throws IOException, InterruptedException {
        Path path = Paths.get(album.getPath());
        MetaDataFolder folder = new MetaDataFolder(musicLib.resolvePath(path));

        imagems.convertImage(folder, sourceImageFile);
        album.setCoverArtwork(true);
    }

    private MusicTrack getTrack(AudioFile file, Tag fileTags, Path trackFilePath, TrackMetaData trackMetaData,
            MusicAlbum album) {
        AudioHeader fileHdr = file.getAudioHeader();

        // Process the audio file.
        //
        MusicTrack track = new MusicTrack();
        track.setId(trackMetaData.getTrackId());
        track.setAlbumId(trackMetaData.getAlbumId());
        track.setRating(trackMetaData.getRating());
        track.setLength(fileHdr.getTrackLength());
        track.setEncoding(fileHdr.getEncodingType());
        track.setFormat(fileHdr.getFormat());
        track.setBitRate((int) fileHdr.getBitRateAsNumber());
        track.setSampleRate(fileHdr.getSampleRateAsNumber());
        track.setPath(musicLib.getRelativePath(trackFilePath).toString());
        track.setMusicLibBuildId(musicLibBuildId);
        track.setMusicLibAutoBuilt(true);

        // Process the mp3/ogg fields.
        //
        for (MediaTag mediaTag : tags.getMediaTags()) {
            if (mediaTag.getAudioTaggerKey() == null) {
                continue;
            }
            // Using jaudiotagger to get the fields from the file.
            // audioTaggerKey is the mapping between our tag and jaudiotagger.
            //
            List<TagField> fields = fileTags.getFields(mediaTag.getAudioTaggerKey());

            for (TagField tf : fields) {
                if (tf instanceof TagTextField) {
                    TagTextField ttf = (TagTextField) tf;
                    String val = ttf.getContent();

                    // Look for known tags and set fields in the track.
                    //
                    if (mediaTag == MediaTags.TITLE) {
                        track.setTitle(val);

                    } else if (mediaTag == MediaTags.TRACKNUMBER) {
                        track.setTrackNumber(Integer.parseInt(val));

                    } else {
                        if (mediaTag.isIdentifier() && val.indexOf(",") != -1) {
                            String[] vals = StringUtil.split(val, ",");
                            for (String v : vals) {
                                addTagToTrack(album, track, mediaTag, v);
                            }
                        } else {
                            // Add to the tags.
                            //
                            addTagToTrack(album, track, mediaTag, val);
                        }
                    }
                }
            }
        }

        if (track.getTrackNumber() == 0) {
            String field = fileTags.getFirst("TRACK");
            if (field.isEmpty()) {
                field = fileTags.getFirst("TRACKNUMBER");
            }

            if (!field.isEmpty()) {
                track.setTrackNumber(Integer.parseInt(field));
            }
        }

        return track;
    }

    private void addTagToTrack(MusicAlbum album, MusicTrack track, MediaTag tag, String value) {

        MediaTagValue tagValue = tags.createTagValue(tag, value);

        track.getTags().add(tagValue);

        if (tag.addToContainer())
            album.getTags().add(tagValue);
    }

    /**
     * @param rebuildMetaData
     *            the rebuildMetaData to set
     */
    public void setRebuildMetaData() {
        this.rebuildMetaData = true;
    }
}
