package org.revo.streamer.livepoll.mpeg2ts.commons.container.m3u8.video.mpeg2ts;


public final class Bytes {
    public static int indexOf(byte[] array, byte[] target) {
        checkNotNull(array, "array");
        checkNotNull(target, "target");
        if (target.length == 0) {
            return 0;
        } else {
            label28:
            for (int i = 0; i < array.length - target.length + 1; ++i) {
                for (int j = 0; j < target.length; ++j) {
                    if (array[i + j] != target[j]) {
                        continue label28;
                    }
                }

                return i;
            }

            return -1;
        }
    }

    private static <T> void checkNotNull(T reference, Object errorMessage) {
        if (reference == null) {
            throw new NullPointerException(String.valueOf(errorMessage));
        }
    }

}
