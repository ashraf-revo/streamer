package org.revo.streamer.livepoll.codec.commons.container.hls;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.Closeable;
import java.io.File;
import java.nio.file.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

@Slf4j
public class HlsFileWatcher extends Thread implements Closeable {
    private final Path path;
    private final WatchEvent.Kind<?>[] kinds;
    private WatchService watchService;
    private final Queue<WatchEvent<?>> events = new LinkedList<>();
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


    public HlsFileWatcher(Path path) {
        this.path = path;
        this.kinds = new WatchEvent.Kind[]{
                StandardWatchEventKinds.ENTRY_CREATE,
                StandardWatchEventKinds.ENTRY_DELETE,
                StandardWatchEventKinds.ENTRY_MODIFY};
    }

    public HlsFileWatcher(Path path, WatchEvent.Kind<?>[] kinds) {
        this.path = path;
        this.kinds = kinds;
    }

    public HlsFileWatcher setOnCreate(Function<Object, Boolean> onCreate) {
        this.onCreate = onCreate;
        return this;
    }

    public HlsFileWatcher setOnDelete(Function<Object, Boolean> onDelete) {
        this.onDelete = onDelete;
        return this;
    }

    public HlsFileWatcher setOnUpdate(Function<Object, Boolean> onUpdate) {
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
                    this.events.add(event);
                    this.triggerEventListener();
                }
                key.reset();
            }
        } catch (ClosedWatchServiceException | InterruptedException e) {
            log.info("MediaFileWatcher interrupted");
        }
    }

    private void triggerEventListener() {
        WatchEvent<?> current = this.events.peek();
        if (current != null) {
            processEvent(current);
            this.events.poll();
        }
    }

    private void processEvent(WatchEvent<?> event) {
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


    @SneakyThrows
    @Override
    public void close() {
        this.processRemainingEvents();
        this.watchService.close();
        this.clean();
    }

    private void processRemainingEvents() {
        for (WatchEvent<?> event : this.events) {
            processEvent(event);
        }
    }

    public void clean() {
        File file = this.path.toFile();
        File[] contents = file.listFiles();
        if (contents != null) {
            for (File f : contents) {
                log.info("will clean {}", f.toString());
                f.delete();
            }
        }
        file.delete();
    }

}
