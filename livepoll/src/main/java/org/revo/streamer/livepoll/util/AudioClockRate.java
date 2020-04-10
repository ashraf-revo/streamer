package org.revo.streamer.livepoll.util;

import java.util.Arrays;
import java.util.Optional;

public enum AudioClockRate {

    S_96000k(0, 96000),
    S_88200k(1, 88200),
    S_64000k(2, 64000),
    S_48000k(3, 48000),
    S_44100k(4, 44100),
    S_32000k(5, 32000),
    S_22050k(6, 22050),
    S_16000k(7, 16000),
    S_12000k(8, 12000),
    S_11025k(9, 11025),
    S_8000k(10, 8000),
    S_7350k(11, 7350);

    private final int index;
    private final int frequency;

    AudioClockRate(int index, int frequency) {
        this.index = index;
        this.frequency = frequency;
    }

    public Optional<AudioClockRate> getByIndex(int index) {
        return Arrays.stream(AudioClockRate.values()).filter(it -> it.index == index).findAny();
    }

    public Optional<AudioClockRate> getByFrequency(int frequency) {
        return Arrays.stream(AudioClockRate.values()).filter(it -> it.frequency == frequency).findAny();
    }
}
