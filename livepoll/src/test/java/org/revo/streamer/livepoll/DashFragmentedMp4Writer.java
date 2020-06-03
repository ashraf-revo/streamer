package org.revo.streamer.livepoll;

import org.mp4parser.Box;
import org.mp4parser.boxes.iso14496.part12.TrackFragmentBaseMediaDecodeTimeBox;
import org.mp4parser.streaming.StreamingTrack;
import org.mp4parser.streaming.extensions.TrackIdTrackExtension;
import org.mp4parser.streaming.output.mp4.FragmentedMp4Writer;
import org.mp4parser.tools.Path;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.Collections;
import java.util.logging.Logger;

/**
 *
 */
public class DashFragmentedMp4Writer extends FragmentedMp4Writer {
    private static final Logger LOG = Logger.getLogger(DashFragmentedMp4Writer.class.getName());
    private File representationBaseDir;
    private long adaptationSetId;
    private StreamingTrack source;


    public DashFragmentedMp4Writer(StreamingTrack source, File baseDir) throws IOException {
        super(Collections.<StreamingTrack>singletonList(source), new WritableByteChannel() {
            @Override
            public int write(ByteBuffer byteBuffer) throws IOException {
                return 0;
            }

            @Override
            public boolean isOpen() {
                return false;
            }

            @Override
            public void close() throws IOException {

            }
        });
        TrackIdTrackExtension trackIdTrackExtension = source.getTrackExtension(TrackIdTrackExtension.class);
        assert trackIdTrackExtension != null;

        this.source = source;
        this.adaptationSetId = trackIdTrackExtension.getTrackId();
        representationBaseDir = new File(baseDir, "dfs");
        representationBaseDir.mkdir();
    }

    public StreamingTrack getSource() {
        return source;
    }

    @Override
    public synchronized void close() throws IOException {
        super.close();
        isClosed = true;
    }

    boolean isClosed = false;

    public boolean isClosed() {
        return isClosed;
    }

    protected void writeHeader(Box... boxes) throws IOException {
        FileOutputStream fos = new FileOutputStream(new File(representationBaseDir, "init.mp4"));
        WritableByteChannel wbc = fos.getChannel();
        write(wbc, boxes);
        fos.close();
        wbc.close();
    }


    @Override
    protected void writeFragment(Box... boxes) throws IOException {
        TrackFragmentBaseMediaDecodeTimeBox tfdt = null;

        for (Box box : boxes) {
            if ("moof".equals(box.getType())) {
                tfdt = Path.getPath(box, "traf[0]/tfdt[0]");
                break;
            }
        }


        assert tfdt != null;
        File f = new File(representationBaseDir, "media-" + tfdt.getBaseMediaDecodeTime() + ".mp4");
        FileOutputStream fos = new FileOutputStream(f);
        WritableByteChannel wbc = fos.getChannel();
        write(wbc, boxes);
        fos.close();
        wbc.close();

    }

    long getTime(File f) {
        String n = f.getName().substring(6);
        n = n.substring(0, n.indexOf("."));
        return Long.parseLong(n);
    }


    public long getAdaptationSetId() {
        return adaptationSetId;
    }
}
