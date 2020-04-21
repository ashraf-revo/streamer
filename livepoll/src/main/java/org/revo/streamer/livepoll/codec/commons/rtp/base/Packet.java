package org.revo.streamer.livepoll.codec.commons.rtp.base;


abstract class Packet implements Raw, Payload {
   abstract int headerSize();
}
