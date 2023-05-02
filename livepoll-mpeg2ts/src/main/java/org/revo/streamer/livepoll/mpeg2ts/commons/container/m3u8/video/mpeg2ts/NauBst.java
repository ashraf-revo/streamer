package org.revo.streamer.livepoll.mpeg2ts.commons.container.m3u8.video.mpeg2ts;

class NauBst {
	byte[] buff;                //缓冲区
	int buffPtr;
	int left;                 // p所指字节当前还有多少 “位” 可读写 count number of available(可用的)位
}
