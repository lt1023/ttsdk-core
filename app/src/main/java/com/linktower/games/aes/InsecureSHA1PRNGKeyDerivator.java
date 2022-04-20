package com.linktower.games.aes;

class InsecureSHA1PRNGKeyDerivator {
    public static byte[] deriveInsecureKey(byte[] seed, int keySizeInBytes) {
        InsecureSHA1PRNGKeyDerivator derivator = new InsecureSHA1PRNGKeyDerivator();
        derivator.setSeed(seed);
        byte[] key = new byte[keySizeInBytes];
        derivator.nextBytes(key);
        return key;
    }
    private static final int[] END_FLAGS = { 0x80000000, 0x800000, 0x8000, 0x80 };
    private static final int[] RIGHT1 = { 0, 40, 48, 56 };
    private static final int[] RIGHT2 = { 0, 8, 16, 24 };
    private static final int[] LEFT = { 0, 24, 16, 8 };
    private static final int[] MASK = { 0xFFFFFFFF, 0x00FFFFFF, 0x0000FFFF,
            0x000000FF };

    private static final int HASHBYTES_TO_USE = 20;
    private static final int FRAME_LENGTH = 16;

    private static final int COUNTER_BASE = 0;
    private static final int HASHCOPY_OFFSET = 0;
    private static final int EXTRAFRAME_OFFSET = 5;
    private static final int FRAME_OFFSET = 21;
    private static final int MAX_BYTES = 48;
    private static final int UNDEFINED = 0;
    private static final int SET_SEED = 1;
    private static final int NEXT_BYTES = 2;

    private transient int[] seed;
    private transient long seedLength;

    private transient int[] copies;
    // ready "next" bytes; needed because words are returned
    private transient byte[] nextBytes;
    // index of used bytes in "nextBytes" array
    private transient int nextBIndex;
    // variable required according to "SECURE HASH STANDARD"
    private transient long counter;
    // contains int value corresponding to engine's current state
    private transient int state;
    /**
     *  constant defined in "SECURE HASH STANDARD"
     */
    private static final int H0 = 0x67452301;
    /**
     *  constant defined in "SECURE HASH STANDARD"
     */
    private static final int H1 = 0xEFCDAB89;
    /**
     *  constant defined in "SECURE HASH STANDARD"
     */
    private static final int H2 = 0x98BADCFE;
    /**
     *  constant defined in "SECURE HASH STANDARD"
     */
    private static final int H3 = 0x10325476;
    /**
     *  constant defined in "SECURE HASH STANDARD"
     */
    private static final int H4 = 0xC3D2E1F0;
    /**
     * offset in buffer to store number of bytes in 0-15 word frame
     */
    private static final int BYTES_OFFSET = 81;
    /**
     * offset in buffer to store current hash value
     */
    private static final int HASH_OFFSET = 82;
    /**
     * # of bytes in H0-H4 words; <BR>
     * in this implementation # is set to 20 (in general # varies from 1 to 20)
     */
    private static final int DIGEST_LENGTH = 20;

    private InsecureSHA1PRNGKeyDerivator() {
        seed = new int[HASH_OFFSET + EXTRAFRAME_OFFSET];
        seed[HASH_OFFSET] = H0;
        seed[HASH_OFFSET + 1] = H1;
        seed[HASH_OFFSET + 2] = H2;
        seed[HASH_OFFSET + 3] = H3;
        seed[HASH_OFFSET + 4] = H4;
        seedLength = 0;
        copies = new int[2 * FRAME_LENGTH + EXTRAFRAME_OFFSET];
        nextBytes = new byte[DIGEST_LENGTH];
        nextBIndex = HASHBYTES_TO_USE;
        counter = COUNTER_BASE;
        state = UNDEFINED;
    }

    private void updateSeed(byte[] bytes) {
        updateHash(seed, bytes, 0, bytes.length - 1);
        seedLength += bytes.length;
    }

    private void setSeed(byte[] seed) {
        if (seed == null) {
            throw new NullPointerException("seed == null");
        }
        if (state == NEXT_BYTES) { // first setSeed after NextBytes; restoring hash
            System.arraycopy(copies, HASHCOPY_OFFSET, this.seed, HASH_OFFSET,
                    EXTRAFRAME_OFFSET);
        }
        state = SET_SEED;
        if (seed.length != 0) {
            updateSeed(seed);
        }
    }

    protected synchronized void nextBytes(byte[] bytes) {
        int i, n;
        long bits; // number of bits required by Secure Hash Standard
        int nextByteToReturn; // index of ready bytes in "bytes" array
        int lastWord; // index of last word in frame containing bytes
        // This is a bug since words are 4 bytes. Android used to keep it this way for backward
        // compatibility.
        final int extrabytes = 7;// # of bytes to add in order to computer # of 8 byte words
        if (bytes == null) {
            throw new NullPointerException("bytes == null");
        }
        // This is a bug since extraBytes == 7 instead of 3. Android used to keep it this way for
        // backward compatibility.
        lastWord = seed[BYTES_OFFSET] == 0 ? 0
                : (seed[BYTES_OFFSET] + extrabytes) >> 3 - 1;
        if (state == UNDEFINED) {
            throw new IllegalStateException("No seed supplied!");
        } else if (state == SET_SEED) {
            System.arraycopy(seed, HASH_OFFSET, copies, HASHCOPY_OFFSET,
                    EXTRAFRAME_OFFSET);

            for (i = lastWord + 3; i < FRAME_LENGTH + 2; i++) {
                seed[i] = 0;
            }
            bits = (seedLength << 3) + 64; // transforming # of bytes into # of bits
            // putting # of bits into two last words (14,15) of 16 word frame in
            // seed or copies array depending on total length after padding
            if (seed[BYTES_OFFSET] < MAX_BYTES) {
                seed[14] = (int) (bits >>> 32);
                seed[15] = (int) (bits & 0xFFFFFFFF);
            } else {
                copies[EXTRAFRAME_OFFSET + 14] = (int) (bits >>> 32);
                copies[EXTRAFRAME_OFFSET + 15] = (int) (bits & 0xFFFFFFFF);
            }
            nextBIndex = HASHBYTES_TO_USE; // skipping remaining random bits
        }
        state = NEXT_BYTES;
        if (bytes.length == 0) {
            return;
        }
        nextByteToReturn = 0;
        // possibly not all of HASHBYTES_TO_USE bytes were used previous time
        n = (HASHBYTES_TO_USE - nextBIndex) < (bytes.length - nextByteToReturn) ? HASHBYTES_TO_USE
                - nextBIndex
                : bytes.length - nextByteToReturn;
        if (n > 0) {
            System.arraycopy(nextBytes, nextBIndex, bytes, nextByteToReturn, n);
            nextBIndex += n;
            nextByteToReturn += n;
        }
        if (nextByteToReturn >= bytes.length) {
            return; // return because "bytes[]" are filled in
        }
        n = seed[BYTES_OFFSET] & 0x03;
        for (;;) {
            if (n == 0) {
                seed[lastWord] = (int) (counter >>> 32);
                seed[lastWord + 1] = (int) (counter & 0xFFFFFFFF);
                seed[lastWord + 2] = END_FLAGS[0];
            } else {
                seed[lastWord] |= (int) ((counter >>> RIGHT1[n]) & MASK[n]);
                seed[lastWord + 1] = (int) ((counter >>> RIGHT2[n]) & 0xFFFFFFFF);
                seed[lastWord + 2] = (int) ((counter << LEFT[n]) | END_FLAGS[n]);
            }
            if (seed[BYTES_OFFSET] > MAX_BYTES) {
                copies[EXTRAFRAME_OFFSET] = seed[FRAME_LENGTH];
                copies[EXTRAFRAME_OFFSET + 1] = seed[FRAME_LENGTH + 1];
            }
            computeHash(seed);
            if (seed[BYTES_OFFSET] > MAX_BYTES) {
                System.arraycopy(seed, 0, copies, FRAME_OFFSET, FRAME_LENGTH);
                System.arraycopy(copies, EXTRAFRAME_OFFSET, seed, 0,
                        FRAME_LENGTH);
                computeHash(seed);
                System.arraycopy(copies, FRAME_OFFSET, seed, 0, FRAME_LENGTH);
            }
            counter++;
            int j = 0;
            for (i = 0; i < EXTRAFRAME_OFFSET; i++) {
                int k = seed[HASH_OFFSET + i];
                nextBytes[j] = (byte) (k >>> 24); // getting first  byte from left
                nextBytes[j + 1] = (byte) (k >>> 16); // getting second byte from left
                nextBytes[j + 2] = (byte) (k >>> 8); // getting third  byte from left
                nextBytes[j + 3] = (byte) (k); // getting fourth byte from left
                j += 4;
            }
            nextBIndex = 0;
            j = HASHBYTES_TO_USE < (bytes.length - nextByteToReturn) ? HASHBYTES_TO_USE
                    : bytes.length - nextByteToReturn;
            if (j > 0) {
                System.arraycopy(nextBytes, 0, bytes, nextByteToReturn, j);
                nextByteToReturn += j;
                nextBIndex += j;
            }
            if (nextByteToReturn >= bytes.length) {
                break;
            }
        }
    }

    private static void computeHash(int[] arrW) {
        int  a = arrW[HASH_OFFSET   ];
        int  b = arrW[HASH_OFFSET +1];
        int  c = arrW[HASH_OFFSET +2];
        int  d = arrW[HASH_OFFSET +3];
        int  e = arrW[HASH_OFFSET +4];
        int temp;
        // In this implementation the "d. For t = 0 to 79 do" loop
        // is split into four loops. The following constants:
        //     K = 5A827999   0 <= t <= 19
        //     K = 6ED9EBA1  20 <= t <= 39
        //     K = 8F1BBCDC  40 <= t <= 59
        //     K = CA62C1D6  60 <= t <= 79
        // are hex literals in the loops.
        for ( int t = 16; t < 80 ; t++ ) {
            temp  = arrW[t-3] ^ arrW[t-8] ^ arrW[t-14] ^ arrW[t-16];
            arrW[t] = ( temp<<1 ) | ( temp>>>31 );
        }
        for ( int t = 0 ; t < 20 ; t++ ) {
            temp = ( ( a<<5 ) | ( a>>>27 )   ) +
                    ( ( b & c) | ((~b) & d)   ) +
                    ( e + arrW[t] + 0x5A827999 ) ;
            e = d;
            d = c;
            c = ( b<<30 ) | ( b>>>2 ) ;
            b = a;
            a = temp;
        }
        for ( int t = 20 ; t < 40 ; t++ ) {
            temp = ((( a<<5 ) | ( a>>>27 ))) + (b ^ c ^ d) + (e + arrW[t] + 0x6ED9EBA1) ;
            e = d;
            d = c;
            c = ( b<<30 ) | ( b>>>2 ) ;
            b = a;
            a = temp;
        }
        for ( int t = 40 ; t < 60 ; t++ ) {
            temp = (( a<<5 ) | ( a>>>27 )) + ((b & c) | (b & d) | (c & d)) +
                    (e + arrW[t] + 0x8F1BBCDC) ;
            e = d;
            d = c;
            c = ( b<<30 ) | ( b>>>2 ) ;
            b = a;
            a = temp;
        }
        for ( int t = 60 ; t < 80 ; t++ ) {
            temp = ((( a<<5 ) | ( a>>>27 ))) + (b ^ c ^ d) + (e + arrW[t] + 0xCA62C1D6) ;
            e = d;
            d = c;
            c = ( b<<30 ) | ( b>>>2 ) ;
            b = a;
            a = temp;
        }
        arrW[HASH_OFFSET   ] += a;
        arrW[HASH_OFFSET +1] += b;
        arrW[HASH_OFFSET +2] += c;
        arrW[HASH_OFFSET +3] += d;
        arrW[HASH_OFFSET +4] += e;
    }

    private static void updateHash(int[] intArray, byte[] byteInput, int fromByte, int toByte) {
        // As intArray contains a packed bytes
        // the buffer's index is in the intArray[BYTES_OFFSET] element
        int index = intArray[BYTES_OFFSET];
        int i = fromByte;
        int maxWord;
        int nBytes;
        int wordIndex = index >>2;
        int byteIndex = index & 0x03;
        intArray[BYTES_OFFSET] = ( index + toByte - fromByte + 1 ) & 077 ;
        // In general case there are 3 stages :
        // - appending bytes to non-full word,
        // - writing 4 bytes into empty words,
        // - writing less than 4 bytes in last word
        if ( byteIndex != 0 ) {       // appending bytes in non-full word (as if)
            for ( ; ( i <= toByte ) && ( byteIndex < 4 ) ; i++ ) {
                intArray[wordIndex] |= ( byteInput[i] & 0xFF ) << ((3 - byteIndex)<<3) ;
                byteIndex++;
            }
            if ( byteIndex == 4 ) {
                wordIndex++;
                if ( wordIndex == 16 ) {          // intArray is full, computing hash
                    computeHash(intArray);
                    wordIndex = 0;
                }
            }
            if ( i > toByte ) {                 // all input bytes appended
                return ;
            }
        }
        // writing full words
        maxWord = (toByte - i + 1) >> 2;           // # of remaining full words, may be "0"
        for ( int k = 0; k < maxWord ; k++ ) {
            intArray[wordIndex] = ( ((int) byteInput[i   ] & 0xFF) <<24 ) |
                    ( ((int) byteInput[i +1] & 0xFF) <<16 ) |
                    ( ((int) byteInput[i +2] & 0xFF) <<8  ) |
                    ( ((int) byteInput[i +3] & 0xFF)      )  ;
            i += 4;
            wordIndex++;
            if ( wordIndex < 16 ) {     // buffer is not full yet
                continue;
            }
            computeHash(intArray);      // buffer is full, computing hash
            wordIndex = 0;
        }
        // writing last incomplete word
        // after writing free byte positions are set to "0"s
        nBytes = toByte - i +1;
        if ( nBytes != 0 ) {
            int w =  ((int) byteInput[i] & 0xFF) <<24 ;
            if ( nBytes != 1 ) {
                w |= ((int) byteInput[i +1] & 0xFF) <<16 ;
                if ( nBytes != 2) {
                    w |= ((int) byteInput[i +2] & 0xFF) <<8 ;
                }
            }
            intArray[wordIndex] = w;
        }
        return ;
    }
}
