package org.apache.lucene.codecs.lucene84;

import org.apache.lucene.store.DataInput;
import org.apache.lucene.store.DataOutput;

import java.io.IOException;

public class MLN extends ForUtil{
    public static final int MAX_V = 8;
    public static final int BLOCK_SIZE = MAX_V*MAX_V;
    private final long[] tmp = new long[BLOCK_SIZE / 2];
    public static long[][] generateCompTab(long[] in){
        int size = in.length;
        long[][] freqTable = new long[8][8];
        for(int i = 0; i< size -1; ++i){
            long current = in[i];
            long next = in[i+1];
            if (current <= MAX_V && next <= MAX_V){
                freqTable[(int)current - 1][(int)next - 1] += 1;
            }
        }
        long[][] encodeTable  = new long[8][8];
        for(int i = 0; i< freqTable.length; i++){
            long[] freqRow = freqTable[i];
            int[] idx = arraySort(freqRow);
            for(int j = 0; j < 8; j++){
                encodeTable[i][idx[j]] = j + 1;
            }
        }
        return encodeTable;
    }

    public static int[] arraySort(long[]arr) {
        long temp;
        int index;
        int size = arr.length;
        int[] Index = new int[size];
        for (int i = 0; i < size; i++) {
            Index[i] = i;
        }
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (arr[j] < arr[j + 1]) {
                    temp = arr[j];
                    arr[j] = arr[j + 1];
                    arr[j + 1] = temp;

                    index = Index[j];
                    Index[j] = Index[j + 1];
                    Index[j + 1] = index;
                }
            }
        }
        return Index;
    }


    public static long[][] generateDecomTable(long[][] encodeTabel){
        long[][] decomTable = new long[encodeTabel.length][encodeTabel.length];
        for(int i = 0; i< encodeTabel.length; i++){
            for (int j = 0; j < encodeTabel.length; j++){
                //decomTable[i][(int)encodeTabel[i][j] - 1] = j+1;

                //为了节省存储 减一
                decomTable[i][(int)encodeTabel[i][j] - 1] = j;
            }
        }
        return decomTable;
    }


    public static long[][] encode(long[] in, long[] out){
        for (int i = 0; i< in.length; i++){
            in[i] += 1;
        }
        long[][] encodeTable = generateCompTab(in);
        int size = in.length;
        out[0] = in[0];
        for (int i = 1; i< size; i++){
            long prev = in[i-1];
            long current = in[i];
            if (prev <= MAX_V && current <= MAX_V){
                out[i] = encodeTable[(int)prev - 1][(int)current - 1];
            }else {
                out[i] = in[i];
            }
        }
        return generateDecomTable(encodeTable);
    }

    public static void decode(long[] in, long[] out, long[][]decodeTable){
        out[0] = in[0];
        long pre = out[0];
        for (int i = 1; i< in.length; i++){
            long current = in[i];
            if (pre <= MAX_V && current <= MAX_V){
                //out[i] = decodeTable[(int)pre - 1][(int)current - 1];
                // 为了节省存储 加回来
                out[i] = decodeTable[(int)pre - 1][(int)current - 1] + 1;
            }else {
                out[i] = in[i];
            }
            pre = out[i];
        }
        for (int i = 0 ;i<out.length;i++){
            out[i] -= 1;
        }

    }


    /** Encode 128 integers from {@code longs} into {@code out}. */
    void encode(long[][] decodTable, int bitsPerValue, DataOutput out) throws IOException {
        long[] longs = new long[64];
        int x = 0;
        for (int i = 0;i<8; i++){
            for (int j= 0 ;j<8;j++){
                longs[x++]= decodTable[i][j];
            }
        }

        final int nextPrimitive;
        final int numLongs;

        nextPrimitive = 8;
        numLongs = BLOCK_SIZE / 8;
        collapse8(longs);


        final int numLongsPerShift = bitsPerValue;
        int idx = 0;
        int shift = nextPrimitive - bitsPerValue;
        for (int i = 0; i < numLongsPerShift; ++i) {
            tmp[i] = longs[idx++] << shift;
        }
        for (shift = shift - bitsPerValue; shift >= 0; shift -= bitsPerValue) {
            for (int i = 0; i < numLongsPerShift; ++i) {
                tmp[i] |= longs[idx++] << shift;
            }
        }

        final int remainingBitsPerLong = shift + bitsPerValue;
        final long maskRemainingBitsPerLong;
        if (nextPrimitive == 8) {
            maskRemainingBitsPerLong = MASKS8[remainingBitsPerLong];
        } else if (nextPrimitive == 16) {
            maskRemainingBitsPerLong = MASKS16[remainingBitsPerLong];
        } else {
            maskRemainingBitsPerLong = MASKS32[remainingBitsPerLong];
        }

        int tmpIdx = 0;
        int remainingBitsPerValue = bitsPerValue;
        while (idx < numLongs) {
            if (remainingBitsPerValue >= remainingBitsPerLong) {
                remainingBitsPerValue -= remainingBitsPerLong;
                tmp[tmpIdx++] |= (longs[idx] >>> remainingBitsPerValue) & maskRemainingBitsPerLong;
                if (remainingBitsPerValue == 0) {
                    idx++;
                    remainingBitsPerValue = bitsPerValue;
                }
            } else {
                final long mask1, mask2;
                if (nextPrimitive == 8) {
                    mask1 = MASKS8[remainingBitsPerValue];
                    mask2 = MASKS8[remainingBitsPerLong - remainingBitsPerValue];
                } else if (nextPrimitive == 16) {
                    mask1 = MASKS16[remainingBitsPerValue];
                    mask2 = MASKS16[remainingBitsPerLong - remainingBitsPerValue];
                } else {
                    mask1 = MASKS32[remainingBitsPerValue];
                    mask2 = MASKS32[remainingBitsPerLong - remainingBitsPerValue];
                }
                tmp[tmpIdx] |= (longs[idx++] & mask1) << (remainingBitsPerLong - remainingBitsPerValue);
                remainingBitsPerValue = bitsPerValue - remainingBitsPerLong + remainingBitsPerValue;
                tmp[tmpIdx++] |= (longs[idx] >>> remainingBitsPerValue) & mask2;
            }
        }

        for (int i = 0; i < numLongsPerShift; ++i) {
            // Java longs are big endian and we want to read little endian longs, so we need to reverse
            // bytes
            long l = Long.reverseBytes(tmp[i]);
            out.writeLong(l);
        }
    }
    private static void collapse8(long[] arr) {
        for (int i = 0; i < 8; ++i) {
            arr[i] =
                    (arr[i] << 56)
                            | (arr[8 + i] << 48)
                            | (arr[16 + i] << 40)
                            | (arr[24 + i] << 32)
                            | (arr[32 + i] << 24)
                            | (arr[40 + i] << 16)
                            | (arr[48 + i] << 8)
                            | arr[56 + i];
        }
    }

    private static final long[] MASKS8 = new long[8];
    private static final long[] MASKS16 = new long[16];
    private static final long[] MASKS32 = new long[32];

    static {
        for (int i = 0; i < 8; ++i) {
            MASKS8[i] = mask8(i);
        }
        for (int i = 0; i < 16; ++i) {
            MASKS16[i] = mask16(i);
        }
        for (int i = 0; i < 32; ++i) {
            MASKS32[i] = mask32(i);
        }
    }

    private static long mask32(int bitsPerValue) {
        return expandMask32((1L << bitsPerValue) - 1);
    }

    private static long mask16(int bitsPerValue) {
        return expandMask16((1L << bitsPerValue) - 1);
    }

    private static long mask8(int bitsPerValue) {
        return expandMask8((1L << bitsPerValue) - 1);
    }

    private static long expandMask32(long mask32) {
        return mask32 | (mask32 << 32);
    }

    private static long expandMask16(long mask16) {
        return expandMask32(mask16 | (mask16 << 16));
    }

    private static long expandMask8(long mask8) {
        return expandMask16(mask8 | (mask8 << 8));
    }

    /** Decode 128 integers into {@code longs}. */
    void decode(int bitsPerValue, DataInput in, long[][] decodeTable) throws IOException {
        long[] longs = new long[64];
        decode3(in, tmp, longs);
        expand8(longs);
        for (int i = 0;i < 64;i++){
            decodeTable[i/8][i%8] = longs[i];
        }

    }

    static void decode3(DataInput in, long[] tmp, long[] longs) throws IOException {
        in.readLELongs(tmp, 0, 3);
        shiftLongs(tmp, 3, longs, 0, 5, MASK8_3);
        shiftLongs(tmp, 3, longs, 3, 2, MASK8_3);
        for (int iter = 0, tmpIdx = 0, longsIdx = 6; iter < 1; ++iter, tmpIdx += 3, longsIdx += 2) {
            long l0 = (tmp[tmpIdx + 0] & MASK8_2) << 1;
            l0 |= (tmp[tmpIdx + 1] >>> 1) & MASK8_1;
            longs[longsIdx + 0] = l0;
            long l1 = (tmp[tmpIdx + 1] & MASK8_1) << 2;
            l1 |= (tmp[tmpIdx + 2] & MASK8_2) << 0;
            longs[longsIdx + 1] = l1;
        }
    }

    static void expand8(long[] arr) {
        for (int i = 0; i < 8; ++i) {
            long l = arr[i];
            arr[i] = (l >>> 56) & 0xFFL;
            arr[8 + i] = (l >>> 48) & 0xFFL;
            arr[16 + i] = (l >>> 40) & 0xFFL;
            arr[24 + i] = (l >>> 32) & 0xFFL;
            arr[32 + i] = (l >>> 24) & 0xFFL;
            arr[40 + i] = (l >>> 16) & 0xFFL;
            arr[48 + i] = (l >>> 8) & 0xFFL;
            arr[56 + i] = (l) & 0xFFL;
        }
    }


    public static void main(String[] args) throws IOException {
        //long[] in = new long[]{1, 2, 3, 2, 1, 2, 1, 2, 8, 2, 9, 2};
        long[] in = new long[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
        for (long l: in){
            System.out.print(l + " ");
        }
        System.out.println();
        long[] out = new long[in.length];
        long[][]decodTable = encode(in, out);

        long[] mycode = new long[64];
        int x = 0;
        for (int i = 0;i<decodTable.length; i++){
            for (int j= 0 ;j<decodTable.length;j++){
                mycode[x++]= decodTable[i][j];
            }
        }


        new MLN().encode(mycode, 3, null);

        for (long l: out){
            System.out.print(l + " ");
        }
        long[] result = new long[in.length];
        decode(out, result, decodTable);
        System.out.println();
        for (long l: result){
            System.out.print(l + " ");
        }

    }

}
