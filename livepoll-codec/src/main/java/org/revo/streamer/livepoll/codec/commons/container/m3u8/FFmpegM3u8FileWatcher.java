package org.revo.streamer.livepoll.codec.commons.container.m3u8;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.nio.file.*;
import java.util.function.Consumer;

@Slf4j
public class FFmpegM3u8FileWatcher extends Thread implements Closeable {
    private final Path path;
    private final WatchEvent.Kind<?>[] kinds;
    private WatchService watchService;
    private Consumer<Object> onCreate = System.out::println;
    private Consumer<Object> onDelete = System.out::println;
    private Consumer<Object> onUpdate = System.out::println;

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

    public FFmpegM3u8FileWatcher setOnCreate(Consumer<Object> onCreate) {
        this.onCreate = onCreate;
        return this;
    }

    public FFmpegM3u8FileWatcher setOnDelete(Consumer<Object> onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public FFmpegM3u8FileWatcher setOnUpdate(Consumer<Object> onUpdate) {
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
                    if (event.kind().equals(StandardWatchEventKinds.ENTRY_CREATE)) {
                        onCreate.accept(event.context());
                    }
                    if (event.kind().equals(StandardWatchEventKinds.ENTRY_DELETE)) {
                        onDelete.accept(event.context());
                    }
                    if (event.kind().equals(StandardWatchEventKinds.ENTRY_MODIFY)) {
                        onUpdate.accept(event.context());
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
