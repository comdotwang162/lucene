package org.apache.lucene.backward_codecs.lucene84;

import org.apache.lucene.backward_codecs.store.EndiannessReverserUtil;
import org.apache.lucene.store.*;
import org.apache.lucene.tests.util.LuceneTestCase;

import java.io.IOException;

public class TestMLN extends LuceneTestCase {

    public void testHello(){

        System.out.println("hello world");
    }


    public void testEncod() throws IOException {
        MLN mln = new MLN();
        long[][] outcx = new long[8][8];
        mln.decode(3, null, outcx);

        System.exit(0);



        long[] in = new long[]{1, 2, 3, 2, 1, 2, 1, 2, 8, 2, 9, 2};
        //long[] in = new long[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
        for (long l: in){
            System.out.print(l + " ");
        }
        System.out.println();
        long[] out = new long[in.length];
        long[][]decodTable = MLN.encode(in, out);

        long[] mycode = new long[64];
        int x = 0;
        for (int i = 0;i< 8; i++){
            for (int j= 0 ;j< 8;j++){
                mycode[x++]= decodTable[i][j];
            }
        }


        //mln.encode(mycode, 3, null);
        long[][] outc = new long[8][8];
        mln.decode(3, null, outc);
        for (int i = 0; i<64; i ++){

            System.out.print(outc[i] + " ");
            if ((i+1)%8 == 0){
                System.out.println();
            }
        }

    }

    public void testPureMln(){

        long[] in = new long[]{1, 2, 3, 2, 1, 2, 1, 2, 8, 2, 9, 2,4};
        //long[] in = new long[]{2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2};
        for (long l: in){
            System.out.print(l + " ");
        }
        System.out.println();
        long[] out = new long[in.length];
        long[][]decodTable = MLN.encode(in, out);
        for (long l: out){
            System.out.print(l + " ");
        }
        long[] result = new long[in.length];
        MLN.decode(out, result, decodTable);
        System.out.println();
        for (long l: result){
            System.out.print(l + " ");
        }

    }

    public void testMyPfor() throws IOException {
        final PForUtil pforUtil = new PForUtil(new ForUtil());
        final Directory d = new ByteBuffersDirectory();
        IndexOutput out = EndiannessReverserUtil.createOutput(d, "test.bin", IOContext.DEFAULT);
       long[] values = new long[]{12,5,2,8,7,7,1,9,11,7,6,6,6,6,3,3,1,1,1,6,3,7,5,4,5,10,7,8,6,3,6,7,3,11,12,4,4,7,14,1,14,7,6,6,12,11,3,13,11,6,11,8,11,2,3,8,11,14,1,5,9,6,11,10,13,7,11,9,4,1,8,3,14,11,7,4,1,4,4,8,7,3,2,7,2,14,2,10,4,3,14,5,7,13,12,6,12,4,13,13,9,13,11,14,5,11,11,2,6,2,12,5,14,12,10,3,10,6,3,14,3,6,3,13,1,13,8,10};

       for (int i = 0; i< 128; i++){
           values[i] = 0;
       }

        pforUtil.encode(values, out);
        long endPointer = out.getFilePointer();
        System.out.println(endPointer);
        out.close();


        IndexInput in = EndiannessReverserUtil.openInput(d, "test.bin", IOContext.READONCE);
        final long[] restored = new long[ForUtil.BLOCK_SIZE];
        pforUtil.decode(in, restored);

        System.out.println();
        for (int i = 0 ;i< restored.length; i++){
            System.out.print(restored[i] + " ");
        }

        System.out.println();


    }


}
