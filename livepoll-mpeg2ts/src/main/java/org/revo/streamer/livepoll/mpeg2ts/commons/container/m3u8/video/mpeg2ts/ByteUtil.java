package org.revo.streamer.livepoll.mpeg2ts.commons.container.m3u8.video.mpeg2ts;

import java.util.ArrayList;
import java.util.List;

public class ByteUtil {
	public static List<Integer> kmp(byte[] src, byte[] pattern){
		List<Integer> indexes = new ArrayList<>();
		if(pattern.length > src.length)
			return indexes;
		
		//计算next[]
		int[] next = new int[pattern.length];
        next[0] = 0;
        for(int i = 1,j = 0; i < pattern.length; i++){
            while(j > 0 && pattern[j] != pattern[i]){
                j = next[j - 1];
            }
            if(pattern[i] == pattern[j]){
                j++;
            }
            next[i] = j;
        }
        
        for(int i = 0, j = 0; i < src.length; i++){
            while(j > 0 && src[i] != pattern[j]){
                j = next[j - 1];
            }
            if(src[i] == pattern[j]){
                j++;
            }
            if(j == pattern.length){
            	indexes.add(i-j+1);
            	j = 0;
            }
        }
        return indexes;
    }
}
