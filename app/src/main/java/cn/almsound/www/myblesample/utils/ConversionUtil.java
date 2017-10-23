package cn.almsound.www.myblesample.utils;

/**
 * @author alm
 * 转换工具（进制转换或类型转换）
 * Created by ALM on 2016/8/11.
 */
@SuppressWarnings("unused")
public class ConversionUtil {
    /**
     * 字符串转换成十六进制字符串
     *
     * @param str 待转换的ASCII字符串
     * @return String 每个Byte之间空格分隔，如: [61 6C 6B]
     */
    public static String strToHexStr(String str) {

        char[] chars = "0123456789ABCDEF".toCharArray();
        StringBuilder sb = new StringBuilder("");
        byte[] bs = str.getBytes();
        int bit;

        for (byte b : bs) {
            bit = (b & 0x0f0) >> 4;
            sb.append(chars[bit]);
            bit = b & 0x0f;
            sb.append(chars[bit]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }


    /**
     * 字符串转换成byte数组（数组长度最长为20）
     *
     * @param s 要转换成byte[]的字符串
     * @return 转换后获得的byte[]
     */
    public static byte[] getBytes(String s) {
        int devNameMaxLen = 20;
        byte[] data;

        if (s.length() > devNameMaxLen) {
            data = new byte[devNameMaxLen];
            System.arraycopy(s.getBytes(), 0, data, 0, devNameMaxLen);
        } else {
            data = s.getBytes();
        }
        return data;
    }

    /**
     * 十六进制转换字符串
     *
     * @param hexStr Byte字符串(Byte之间无分隔符 如:[616C6B])
     * @return String 对应的字符串
     */
    public static String hexStrToStr(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;

        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * bytes转换成十六进制字符串
     *
     * @param bytes byte数组
     * @return String 每个Byte值之间空格分隔
     */
    public static String bytesToHexStr(byte[] bytes) {
        String stmp;
        StringBuilder sb = new StringBuilder("");
        for (byte aByte : bytes) {
            stmp = Integer.toHexString(aByte & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    public static byte[] hexStrToBytes(String src) {
        int m, n;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            ret[i] = Byte.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
        }
        return ret;
    }

    /**
     * String的字符串转换成unicode的String
     *
     * @param strText 全角字符串
     * @return String 每个unicode之间无分隔符
     */
    public static String strToUnicode(String strText)
            throws Exception {
        char c;
        StringBuilder str = new StringBuilder();
        int intAsc;
        String strHex;
        for (int i = 0; i < strText.length(); i++) {
            c = strText.charAt(i);
            intAsc = (int) c;
            strHex = Integer.toHexString(intAsc);
            if (intAsc > 128) {
                str.append("\\u").append(strHex);
            }
            // 低位在前面补00
            else {
                str.append("\\u00").append(strHex);
            }
        }
        return str.toString();
    }

    /**
     * unicode的String转换成String的字符串
     *
     * @param hex 16进制值字符串 （一个unicode为2byte）
     * @return String 全角字符串
     */
    public static String unicodeToString(String hex) {
        int t = hex.length() / 6;
        StringBuilder str = new StringBuilder();
        for (int i = 0; i < t; i++) {
            String s = hex.substring(i * 6, (i + 1) * 6);
            // 高位需要补上00再转
            String s1 = s.substring(2, 4) + "00";
            // 低位直接转
            String s2 = s.substring(4);
            // 将16进制的string转为int
            int n = Integer.valueOf(s1, 16) + Integer.valueOf(s2, 16);
            // 将int转换为字符
            char[] chars = Character.toChars(n);
            str.append(new String(chars));
        }
        return str.toString();
    }

    /**
     * 将高位的byte和低位的byte拼接成一个int
     *
     * @param highByte 高位的byte
     * @param lowByte  低位的byte
     * @return 拼接好的int
     */
    public static int bytesToInt(byte highByte, byte lowByte) {
        byte[] bytes = new byte[2];
        bytes[0] = highByte;
        bytes[1] = lowByte;
        String s = bytesToHexStr(bytes);
        String[] split = s.split(" ");
        int high = Integer.parseInt(split[0], 16);
        int low = Integer.parseInt(split[1], 16);
        return high * 256 + low;
    }

    private static final int TWO = 2;

    /**
     * 将一个整数转换成16进制的分为高低位的数组
     *
     * @param i 整数
     * @return 16进制的分为高低位的数组
     */
    public static byte[] intToBytes(int i) {
        LogUtil.w("ConversionUtil", "i = " + i);
        String hexString = intToHexStr(i);
        LogUtil.w("ConversionUtil", "hexString = " + hexString);
        byte highByte;
        byte lowByte;
        if (hexString.length() > TWO) {
            String substring = hexString.substring(0, hexString.length() - 2);
            LogUtil.w("ConversionUtil", "highByteHex = " + substring);
            highByte = (byte) Integer.parseInt(substring, 16);
            substring = hexString.substring(hexString.length() - 2, hexString.length());
            LogUtil.w("ConversionUtil", "lowByteHex = " + substring);
            lowByte = (byte) Integer.parseInt(substring, 16);
        } else {
            highByte = 0;
            LogUtil.w("ConversionUtil", "highByteHex = " + 0);
            lowByte = (byte) Integer.parseInt(hexString, 16);
            LogUtil.w("ConversionUtil", "lowByteHex = " + hexString);
        }
        return new byte[]{highByte, lowByte};
    }

    /**
     * 将整数转换成16进制字符串
     *
     * @param i 整数
     * @return 16进制字符串
     */
    @SuppressWarnings("WeakerAccess")
    public static String intToHexStr(int i) {
        return Integer.toHexString(i);
    }
}
