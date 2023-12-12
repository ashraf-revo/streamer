package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.nio.file.*;
import java.util.function.Function;

@Slf4j
public class FFmpegM3u8FileWatcher extends Thread implements Closeable {
    private final Path path;
    private final WatchEvent.Kind<?>[] kinds;
    private WatchService watchService;
    private Function<Object, Boolean> onCreate = (it) -> {
        System.out.println(it);
        return Boolean.TRUE;
    };
    private Function<Object, Boolean> onDelete = (it) -> {
        System.out.println(it);
        return Boolean.TRUE;
    };

    private Function<Object, Boolean> onUpdate = (it) -> {
        System.out.println(it);
        return Boolean.TRUE;
    };


    public FFmpegM3u8FileWatcher(Path path) {
        this.path = path;
        this.kinds = new WatchEvent.Kind[]{
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY};
    }

    public FFmpegM3u8FileWatcher(Path path, WatchEvent.Kind<?>[] kinds) {
        this.path = path;
        this.kinds = kinds;
    }

    public FFmpegM3u8FileWatcher setOnCreate(Function<Object, Boolean> onCreate) {
        this.onCreate = onCreate;
        return this;
    }

    public FFmpegM3u8FileWatcher setOnDelete(Function<Object, Boolean> onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public FFmpegM3u8FileWatcher setOnUpdate(Function<Object, Boolean> onUpdate) {
        this.onUpdate = onUpdate;
        return this;
    }

    @SneakyThrows
    @Override
    public void run() {
        this.watchService = FileSystems.getDefault().newWatchService();
        this.path.register(watchService, kinds);
        try {
            WatchKey key;
            while ((key = watchService.take()) != null) {
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path eventPath = this.path.resolve((event.context().toString()));
                    Boolean shouldDelete = Boolean.FALSE;
                    try {
                        if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                            shouldDelete = onCreate.apply(eventPath);
                        }
                        if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                            shouldDelete = onDelete.apply(eventPath);
                        }
                        if (event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                            shouldDelete = onUpdate.apply(eventPath);
                        }
                    } catch (Exception e) {
                        shouldDelete = Boolean.TRUE;
                    }
                    if (shouldDelete == Boolean.TRUE && Files.exists(eventPath)) {
                        // Files.delete(eventPath);
                    }
                }
                key.reset();
            }
        } catch (ClosedWatchServiceException | InterruptedException e) {
            log.info("MediaFileWatcher interrupted");
        }
    }

    @SneakyThrows
    @Override
    public void close() {
        this.watchService.close();
    }
}
