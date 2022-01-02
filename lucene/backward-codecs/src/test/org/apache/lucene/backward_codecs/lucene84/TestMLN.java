package org.apache.lucene.backward_codecs.lucene84;

import org.apache.lucene.tests.util.LuceneTestCase;

import java.io.IOException;

public class TestMLN extends LuceneTestCase {

    public void testHello(){

        System.out.println("hello world");
    }


    public void testEncod() throws IOException {
        MLN mln = new MLN();
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
        long[] outc = new long[64];
        mln.decode(3, null, outc);
        for (int i = 0; i<64; i ++){

            System.out.print(outc[i] + " ");
            if ((i+1)%8 == 0){
                System.out.println();
            }
        }

    }


}
