package com.accessibility.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Administrator on 2018/9/3.
 */

public class ValidateUtil {

    /**
     * 13* 字符串是否符合正则表达式的规则14*15* @param text 匹配文本16* @param format 匹配规则17* @return true 匹配成功 flase 匹配失败18
     */
    public static boolean isMatches(String text, String format) {
        Pattern pattern = Pattern.compile(format);
        Matcher m = pattern.matcher(text);
        return m.matches();

    }

    /**
     * 26* 匹配帐号类型是否正确（只能输入大小写字母和数字，最大不超过20个字符）27*28* @param str 帐号29* @return true= 符合 false=不符合30
     */
    public static boolean isAccount(String str) {
        String format = "[a-zA-Z0-9]{0,20}";
        return isMatches(str, format);
    }

    /**
     * 37* 匹配金额是否符合要求（99999999.99）38*39* @param money 金额字符串40* @return true= 符合 false=不符合41
     */

    public static boolean isMoney(String money) {
//         String regex = "(^[1-9][0-9]{0,7}(\\.[0-9]{0,2})?)|(^0(\\.[0-9]{0,2})?)";//网上找的金融输入框是最大可以有小数点前面8位数，或者0开头
//        String regex = "(^[1-9][0-9]{0,3}(\\.[0-9]{0,2})?)|(^1(\\.[0-9]{0,2})?)"; //小数点前面数字不能超过10000，输入从1开始不能输入0
        String regex = "(^[1-9][0-9]{0,8}[0-8]{0,1}(\\.[0-9]{0,2})?)|(^0(\\.[0-9]{0,2})?)|(^[1-9][0-8]{0,8}[9]{0,1}(\\.[0-9]{0,2})?)";//网上找的金融输入框是最大可以有小数点前面8位数，或者0开头
        return isMatches(money, regex);
    }


    public static boolean isZcMoney(String money) {
//         String regex = "(^[1-9][0-9]{0,7}(\\.[0-9]{0,2})?)|(^0(\\.[0-9]{0,2})?)";//网上找的金融输入框是最大可以有小数点前面8位数，或者0开头
        String regex = "(^[1-9][0-9]{0,3}(\\.[0-9]{0,2})?)|(^1(\\.[0-9]{0,2})?)";
//        String regex = "(^[1-9][0-9]{0,3}(\\.[0-9]{0,2})?)|(^0(\\.[0-9]{0,2})?)|(^[1-9][0-9]{0,3}[0-8]{0,1}(\\.[0-9]{0,2})?)|(^[1-9][0-9]{0,3}[9](\\.[0]{0,2})?)";//网上找的金融输入框是最大可以有小数点前面8位数，或者0开头
        return isMatches(money, regex);
    }


    //只能输入非0的正数字
    public static boolean isLicheng(String money) {
        String regex = "[1-9]\\d*";
        return isMatches(money, regex);
    }

    //只能输入非0的一位小数
    public static boolean isPl(String money) {
        String regex = "(^[1-9][0-8]{0,1}(\\.[0-9]{0,1})?)|(^1(\\.[0-9]{0,1})?)|(^[1-8][9](\\.[0-9]{0,1})?)|(^[9][9](\\.[0]{0,1})?)";
        return isMatches(money, regex);
    }

    /**
     * 验证输入的名字是否为“中文”或者是否包含“·”
     */
    public static boolean isLegalName(String name) {
        if (name.contains("·") || name.contains("•")) {
            if (name.matches("^[\\u4e00-\\u9fa5]+[·•][\\u4e00-\\u9fa5]+$")) {
                return true;
            } else {
                return false;
            }
        } else {
            if (name.matches("^[\\u4e00-\\u9fa5]+$")) {
                return true;
            } else if (name.matches("^[a-zA-Z]+$")) {
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * 正则表达式:验证身份证
     */
    public static final String REGEX_ID_CARD = "(^\\d{15}$)|(^\\d{17}([0-9]|X)$)";

    /**
     * 验证身份证号码
     *
     * @param idCard 居民身份证号码15位或18位，最后一位可能是数字或字母
     * @return 验证成功返回true，验证失败返回false
     */
    public static boolean checkIdCard(String idCard) {
        //这里只支持18位身份证
        if (idCard.length() < 18) {
            return false;
        }
        return Pattern.matches(REGEX_ID_CARD, idCard);
    }

    //判断统一社会信用代码是否合规则
    public static boolean isUnifiedCode(String unified) {
        try {
            String str = "^[A-HJ-NP-RT-UW-Y0-9]{2}[0-9]{6}[A-HJ-NP-RT-UW-Y0-9]{10}$";
            return isMatches(unified, str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 社会统一码
     *
     * @param unified
     * @return
     */
    public static boolean isUnified(String unified) {
        try {
            String str = "^[A-HJ-NP-RT-UW-Y0-9]+$";
            return isMatches(unified, str);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * luhm校验
     * 从卡号最后一位数字开始，逆向将奇数位相加
     * 从卡号最后一位数字开始，逆向将偶数位数字，先乘以2（如果乘积为两位数，则将其减去9），再求和
     * 奇偶相加为10的倍数表示通过
     */
    public static boolean validateluhm(String bankCode) {
        //验证
        if (!bankCode.matches("^\\d{16,19}$")) {
            //throw new IllegalArgumentException("bankCode must be 16-19 number,bankCode is : " + bankCode);
            return  false;
        }
        //倒转
        String reverseCode = new StringBuffer(bankCode).reverse().toString();
        char[] array = reverseCode.toCharArray();
        int sumOdd = 0;
        int sumEven = 0;
        for (int i = 0; i < reverseCode.length(); i++) {
            int num = Integer.parseInt(String.valueOf(array[i]));
            if (i % 2 == 0) { //奇数位
                sumEven += num;
            } else { //偶数位
                num = num * 2;
                if (num > 9) {
                    num = num - 9;
                }
                sumOdd += num;
            }
        }
        if ((sumOdd + sumEven) % 10 == 0) {
            return true;
        }
        return false;
    }

    public static boolean isPassword(String password) {

        String pattern = "[A-Za-z0-9~!@#$%^&*()_+;',.:<>]{6,16}";
        return matcher(password, pattern);
    }

    public static boolean isBackCardPassword(String password) {

        String pattern = "[0-9]{6}";
        return matcher(password, pattern);
    }

    public static boolean isDecimal(String str) {
        String pattern = "([1-9]+[0-9]*|0)(\\.[\\d]+)?";
        return matcher(str, pattern);

    }

    public static boolean isMobileNO(String str) {
        String pattern = "^[1][3-8]\\d{9}";
        return matcher(str, pattern);
    }

    public static boolean isIdCard(String str) {
        String pattern = "([0-9]{17}([0-9]|X))|([0-9]{15})";
        return matcher(str, pattern);
    }

    public static boolean isBankNo(String cardId) {
        char bit = getBankCardCheckCode(cardId.substring(0, cardId.length() - 1));
        if (bit == 'N') {
            return false;
        }
        return cardId.charAt(cardId.length() - 1) == bit;
    }

    public static char getBankCardCheckCode(String nonCheckCodeCardId) {
        if (nonCheckCodeCardId == null || nonCheckCodeCardId.trim().length() == 0 || !nonCheckCodeCardId.matches("\\d+")) {
//如果传的不是数据返回N
            return 'N';
        }
        char[] chs = nonCheckCodeCardId.trim().toCharArray();
        int luhmSum = 0;
        for (int i = chs.length - 1, j = 0; i >= 0; i--, j++) {
            int k = chs[i] - '0';
            if (j % 2 == 0) {
                k *= 2;
                k = k / 10 + k % 10;
            }
            luhmSum += k;
        }
        return (luhmSum % 10 == 0) ? '0' : (char) ((10 - luhmSum % 10) + '0');
    }

    private static boolean matcher(String str, String pattern) {
        try {
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher(str);
            return m.matches();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}



