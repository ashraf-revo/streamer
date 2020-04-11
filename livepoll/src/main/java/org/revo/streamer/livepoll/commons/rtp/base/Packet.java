package org.revo.streamer.livepoll.commons.rtp.base;


abstract class Packet implements Raw, Payload {
   abstract int headerSize();
}
