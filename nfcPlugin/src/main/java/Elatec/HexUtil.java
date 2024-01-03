package elatec;

public class HexUtil {
    //Hexadecimal to binary
    public static String hex2bin(String input) {
        StringBuilder sb = new StringBuilder();
        int len = input.length();
        for (int i = 0; i < len; i++){
            //Convert each hexadecimal digit to 4 binary digits
            String temp = input.substring(i, i + 1);
            int tempInt = Integer.parseInt(temp, 16);
            String tempBin = Integer.toBinaryString(tempInt);
            //If the binary number is less than 4 digits, add 0
            if (tempBin.length() < 4){
                int num = 4 - tempBin.length();
                for (int j = 0; j < num; j++){
                    sb.append("0");
                }
            }
            sb.append(tempBin);
        }

        return sb.toString();
    }

    //Binary to hexadecimal
    public static String bin2Hex(String binary){
        StringBuffer sbuf = new StringBuffer();
        int bLength = binary.length() % 4;
        if(bLength != 0){
            String first = binary.substring(0,bLength);
            sbuf.append(Integer.toHexString(Integer.parseInt(first,2)));
            binary = binary.substring(bLength);
        }
        int cnum = binary.length() / 4;
        for(int i=0;i<cnum;i++){
            sbuf.append(Integer.toHexString(Integer.parseInt(binary.substring(i*4,4*(i+1)),2)));
        }
        return sbuf.toString().toUpperCase();
    }

    //Bytes to hexadecimal
    public static String byteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : inBytArr) {
            strBuilder.append(byte2Hex(Byte.valueOf(valueOf)));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    public static String byte2Hex(Byte inByte) {
        return String.format("%02x", new Object[]{inByte}).toUpperCase();
    }

    public static byte[] hexToByteArr(String inHex) {
        byte[] result;
        int hexlen = inHex.length();
        if (isOdd(hexlen) == 1) {
            hexlen++;
            result = new byte[(hexlen / 2)];
            inHex = "0" + inHex;
        } else {
            result = new byte[(hexlen / 2)];
        }
        int j = 0;
        for (int i = 0; i < hexlen; i += 2) {
            result[j] = hexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    public static int isOdd(int num) {
        return num & 1;
    }

    public static byte hexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }
}
