package Elatec;

public class HexUtil {
    //十六进制转二进制
    public static String hex2bin(String input) {
        StringBuilder sb = new StringBuilder();
        int len = input.length();
        for (int i = 0; i < len; i++){
            //每1个十六进制位转换为4个二进制位
            String temp = input.substring(i, i + 1);
            int tempInt = Integer.parseInt(temp, 16);
            String tempBin = Integer.toBinaryString(tempInt);
            //如果二进制数不足4位，补0
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

    //二进制转十六进制
    public static String bin2Hex(String binary){
        StringBuffer sbuf = new StringBuffer();
        int blength = binary.length() % 4;
        if(blength != 0){
            String first = binary.substring(0,blength);
            sbuf.append(Integer.toHexString(Integer.parseInt(first,2)));
            binary = binary.substring(blength);
        }
        int cnum = binary.length() / 4;
        for(int i=0;i<cnum;i++){
            sbuf.append(Integer.toHexString(Integer.parseInt(binary.substring(i*4,4*(i+1)),2)));
        }
        return sbuf.toString().toUpperCase();
    }

    //字节转hex
    public static String ByteArrToHex(byte[] inBytArr) {
        StringBuilder strBuilder = new StringBuilder();
        for (byte valueOf : inBytArr) {
            strBuilder.append(Byte2Hex(Byte.valueOf(valueOf)));
            strBuilder.append(" ");
        }
        return strBuilder.toString();
    }

    public static String Byte2Hex(Byte inByte) {
        return String.format("%02x", new Object[]{inByte}).toUpperCase();
    }

    public static byte[] HexToByteArr(String inHex) {
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
            result[j] = HexToByte(inHex.substring(i, i + 2));
            j++;
        }
        return result;
    }

    public static int isOdd(int num) {
        return num & 1;
    }

    public static byte HexToByte(String inHex) {
        return (byte) Integer.parseInt(inHex, 16);
    }
}
