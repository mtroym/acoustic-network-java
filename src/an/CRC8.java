package an;

/*
 * https://stackoverflow.com/questions/28877881/java-code-for-crc-calculation?answertab=active#tab-top
 * http://www.sunshine2k.de/coding/javascript/crc/crc_js.html
 *
 * Calculate CRC8 based on a lookup table.
 * CRC-8     : CRC-8K/3 (HD=3, 247 bits max)
 * polynomial: 0xa6 = x^8 + x^6 + x^3 + x^2 + 1  (0x14d) <=> (0xb2; 0x165)
 * init = 0
 *
 * There are two ways to define a CRC, forward or reversed bits.
 * The implementations of CRCs very frequently use the reversed bits convention,
 * which this one does. 0xb2 is 0x4d reversed. The other common convention is
 * to invert all of the bits of the CRC, which avoids a sequence of zeros on
 * a zero CRC resulting in a zero CRC. The code below does that as well.
 *
 * usage:
 * new Crc8().update("123456789").getHex() == "D8"
 * new Crc8().update("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ").getHex() == "EF"
 *
 */
class CRC8 {
    final static int CRC_POLYNOM = 0xB2;
    final static byte  CRC_INITIAL = (byte) 0xFF;
    final static boolean CRC_LOOKUPT = true;
    private final byte[]   crcTable = new byte[256];

    private final boolean useLookupTable;
    private byte crc8;

    /**
     * Construct a CRC8 specifying the polynomial and initial value.
     * @param polynomial Polynomial, typically one of the POLYNOMIAL_* constants.
     * @param init Initial value, typically either 0xff or zero.
     */
    public CRC8(){
        useLookupTable = CRC_LOOKUPT;
        for(int i=0; i<256; i++){
            int rem = i; // remainder from polynomial division
            for(int j=0; j<8; j++){
                if((rem & 1) == 1){
                    rem >>= 1;
                    rem ^= CRC_POLYNOM;
                }else {
                    rem >>= 1;
                }
            }
            crcTable[i] = (byte)rem;
        }
        reset();
    }

    public void update(byte[] buffer, int offset, int len){
        for(int i=offset; i < len; i++){
            update(buffer[i]);
        }
    }

    public void update(byte[] buffer){
        for(int i=0; i < buffer.length; i++){
            update(buffer[i]);
        }
    }

    public void update(byte b){
        if (useLookupTable){
//	    		System.out.printf("debug 0x%02x -> 0x%02x \n", (crc8 ^ b) & 0xFF, crcTable[(crc8 ^ b) & 0xFF]);
            crc8 = crcTable[(crc8 ^ b) & 0xFF];
        }else{
            crc8 ^= b & 0xFF;
            crc8 &= 0xFF;
            for(int j=0; j<8; j++){
                if((crc8 & 1) == 1){
                    crc8 >>= 1;
                    crc8 ^= CRC_POLYNOM;
                }
                else{
                    crc8 >>= 1;
                }
            }
        }
    }

    public byte getValue(){
        return (byte) (crc8 ^ 0xff);
    }

    public void reset(){
        crc8 = CRC_INITIAL;
    }
}