/**
 * Java RTP Library (jlibrtp)
 * Copyright (C) 2006 Arne Kepp
 * <p>
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * <p>
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.revo.streamer.livepoll.codec.commons.utils;

/**
 * Generic functions for converting between unsigned integers and byte[]s.
 *
 * @author Arne Kepp
 */
public class StaticProcs {

    /**
     * Converts an integer into an array of bytes.
     * Primarily used for 16 bit unsigned integers, ignore the first two octets.
     *
     * @param i a 16 bit unsigned integer in an int
     * @return byte[2] representing the integer as unsigned, most significant bit first.
     */
    public static byte[] uIntIntToByteWord(int i) {
        byte[] byteWord = new byte[2];
        byteWord[0] = (byte) ((i >> 8) & 0x000000FF);
        byteWord[1] = (byte) (i & 0x00FF);
        return byteWord;
    }

    /**
     * Combines two bytes (most significant bit first) into a 16 bit unsigned integer.
     *
     * @param index of most significant byte
     * @return int with the 16 bit unsigned integer
     */
    public static int bytesToUIntInt(byte[] bytes, int index) {
        int accum = 0;
        int i = 1;
        for (int shiftBy = 0; shiftBy < 16; shiftBy += 8) {
            accum |= ((long) (bytes[index + i] & 0xff)) << shiftBy;
            i--;
        }
        return accum;
    }

    /**
     * Combines four bytes (most significant bit first) into a 32 bit unsigned integer.
     *
     * @param bytes
     * @param index of most significant byte
     * @return long with the 32 bit unsigned integer
     */
    public static long bytesToUIntLong(byte[] bytes, int index) {
        long accum = 0;
        int i = 3;
        for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
            accum |= ((long) (bytes[index + i] & 0xff)) << shiftBy;
            i--;
        }
        return accum;
    }

    /**
     * Get the bits of a byte
     *
     * @param aByte the byte you wish to convert
     * @return a String of 1's and 0's
     */
    public static String bitsOfByte(byte aByte) {
        int temp;
        String out = "";
        for (int i = 7; i >= 0; i--) {
            temp = (aByte >>> i);
            temp &= 0x0001;
            out += ("" + temp);
        }
        return out;
    }

}
