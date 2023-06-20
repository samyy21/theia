/**
 * 
 */
package com.paytm.pgplus.cashier.util;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * @author afaq
 */
public class InputValidator {

    // validate String - all characters.
    public static boolean validateString(final String name) {
        return name.matches("[a-zA-Z]*");
    }

    // validate User Name - all characters only '@' allowed as special
    // character.
    public static boolean validateName(final String name) {
        return name.matches("^[A-Za-z0-9][A-Za-z0-9-_]*[A-Za-z0-9]${5,20}");
    }

    // validate User Full Name can have space
    public static boolean validateFullName(final String name) {
        return name.matches("[^<%>|\\[\\]\\\\?@&^{},#*+;:~!'$/()\"=-]{5,75}");
    }

    // validate Password
    public static boolean validatePassword(final String password) {
        boolean isValid = false;
        boolean isAlpha = false;
        boolean isNumbers = false;
        boolean isSplChars = false;

        if (password.matches("[^<%>` .\\[\\]\\\\?&^{},*+;:~'/()\"=-]{8,20}")) {
            final int len = password.length();
            for (int cnt = 0; cnt < len; cnt++) {
                if (((('a' <= password.charAt(cnt)) && ('z' >= password.charAt(cnt))) || (('A' <= password.charAt(cnt)) && ('Z' >= password
                        .charAt(cnt))))) {
                    isAlpha = true;
                }
                if (('0' <= password.charAt(cnt)) && ('9' >= password.charAt(cnt))) {
                    isNumbers = true;
                }

                switch (password.charAt(cnt)) {
                case '@':
                case '#':
                case '!':
                case '|':
                case '?':
                case '$':
                    isSplChars = true;
                }

            }
            if (isAlpha && isNumbers && isSplChars) {
                isValid = true;
            }
        } else {
            isValid = false;
        }
        return isValid;
    }

    public static boolean validateCVV(final String cvv) {
        return cvv.matches("[0-9]{3,4}");
    }

    public static boolean validateItzPassword(final String pwd) {
        return pwd.matches("[0-9]{3,6}");
    }

    public static boolean validateDonePassword(final String pwd) {
        return pwd.matches("[0-9]{3,4}");
    }

    public static boolean validateCardNumber(final String cardNumber) {
        return cardNumber.matches("[0-9]{13,19}");
    }

    public static boolean validateItzCashCardNumber(final String cardNumber) {
        return cardNumber.matches("[0-9]{10,12}");
    }

    public static boolean validateDoneCardNumber(final String cardNumber) {
        return cardNumber.matches("[0-9]{12,16}");
    }

    public static boolean validateAmount(final String amount) {
        return amount.matches("[0-9]{1,10}|[0-9]{0,10}[.][0-9]{0,2}");
    }

    public static boolean validateNick(final String name) {
        return name.matches("[a-zA-Z0-9]*[.]{0,1}[a-zA-Z0-9]*");
    }

    public static boolean validateNumber(final String number) {
        return number.matches("[0-9]*");
    }

    // validate String - all characters.
    public static boolean validateAlphaNumeric(final String name) {
        return name.matches("[a-zA-Z0-9]*");
    }

    // validate String - all characters.
    public static boolean validateFlag(final String name) {
        return name.matches("[YN]{1}");
    }

    // Validate mobile number
    public static boolean validateMobileNo(final String number) {
        return number.matches("^([1-9]{1})([0-9]{9})$");
    }

    // validate String - Email.
    public static boolean validateEmail(final String email) {
        return email.matches(EMAIL_PATTERN);
    }

    // validate String - Email.

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
            + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private static final String URL_PATTERN = "^(http|https|ftp)\\://([a-zA-Z0-9\\.\\-]+(\\:[a-zA-Z0-9\\.&amp;%\\$\\-]+)*@)?((25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9])\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]{1}[0-9]{2}|[1-9]{1}[0-9]{1}|[0-9])|([a-zA-Z0-9\\-]+\\.)*[a-zA-Z0-9\\-]+\\.[a-zA-Z]{2,4})(\\:[0-9]+)?(/[^/][a-zA-Z0-9\\.\\,\\?\\'\\/\\+&amp;%\\$#\\=~_\\-@]*)*$";

    public static boolean validateURL(final String url) {
        return url.matches(URL_PATTERN);
    }

    // validate String - Email.
    public static boolean validateCurrDisplay(final String currDisplay) {
        return currDisplay.matches("[a-zA-Z.$]*");
    }

    public static boolean validateMonth(final String month) {

        if (month == null) {
            return false;
        }

        try {
            final int mon = Integer.parseInt(month.trim());
            if ((mon >= 1) && (mon <= 12)) {
                return true;
            }
            return false;
        } catch (final NumberFormatException nfe) {
            return false;
        }
    }

    /**
     * @param year
     * @param criterion
     *            Criteria might have two values BEFORE and AFTER and method
     *            will validate year on the given criterion
     * @return
     */
    public static boolean validateYear(final String year, final String criterion) {
        try {
            if (year == null) {
                return false;
            }
            /*
             * if(criterion == null || !( criterion.equalsIgnoreCase("BEFORE")
             * || criterion.equalsIgnoreCase("AFTER"))){ criterion = "AFTER"; }
             */

            final int iYear = Integer.parseInt(year.length() == 2 ? "20" + year.trim() : year.trim());
            final int currYear = Calendar.getInstance().get(Calendar.YEAR);

            if (criterion.equalsIgnoreCase("BEFORE")) {
                if ((currYear >= iYear) && (iYear > 0)) {
                    return true;
                }
                return false;
            } else if (criterion.equalsIgnoreCase("AFTER")) {
                if (currYear <= iYear) {
                    return true;
                }
                return false;
            }

        } catch (final NumberFormatException nfe) {
            return false;
        }
        return false;
    }

    /**
     * Validate Card Number
     * 
     * @param cardNumber
     * @return
     */
    public static boolean isValidCC(final String cardNumber) {

        int sum = 0;
        int digit = 0;
        int addend = 0;
        boolean timesTwo = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            digit = Integer.parseInt(cardNumber.substring(i, i + 1));
            if (timesTwo) {
                addend = digit * 2;
                if (addend > 9) {
                    addend -= 9;
                }
            } else {
                addend = digit;
            }
            sum += addend;
            timesTwo = !timesTwo;
        }

        final int modulus = sum % 10;
        return modulus == 0;

    }

    private static final String[] tensNames = { "", " Ten", " Twenty", " Thirty", " Forty", " Fifty", " Sixty",
            " Seventy", " Eighty", " Ninety" };

    private static final String[] numNames = { "", " One", " Two", " Three", " Four", " Five", " Six", " Seven",
            " Eight", " Nine", " Ten", " Eleven", " Twelve", " Thirteen", " Fourteen", " Fifteen", " Sixteen",
            " Seventeen", " Eighteen", " Nineteen" };

    private static String convertLessThanOneThousand(final int no) {
        String soFar;
        int number = no;
        if ((number % 100) < 20) {
            soFar = numNames[number % 100];
            number /= 100;
        } else {
            soFar = numNames[number % 10];
            number /= 10;

            soFar = tensNames[number % 10] + soFar;
            number /= 10;
        }
        if (number == 0) {
            return soFar;
        }
        {
            return numNames[number] + " Hundred" + soFar;
        }
    }

    public static String convert(final long number) {
        // 0 to 999 999 999 999
        if (number == 0) {
            return "Zero";
        }

        String snumber = Long.toString(number);

        // pad with "0"
        final String mask = "000000000000";
        final DecimalFormat df = new DecimalFormat(mask);
        snumber = df.format(number);

        // XXXnnnnnnnnn
        final int billions = Integer.parseInt(snumber.substring(0, 3));
        // nnnXXXnnnnnn
        final int millions = Integer.parseInt(snumber.substring(3, 6));
        // nnnnnnXXXnnn
        final int hundredThousands = Integer.parseInt(snumber.substring(6, 9));
        // nnnnnnnnnXXX
        final int thousands = Integer.parseInt(snumber.substring(9, 12));

        String tradBillions;
        switch (billions) {
        case 0:
            tradBillions = "";
            break;
        case 1:
            tradBillions = convertLessThanOneThousand(billions) + " Billion ";
            break;
        default:
            tradBillions = convertLessThanOneThousand(billions) + " Billion ";
        }
        String result = tradBillions;

        String tradMillions;
        switch (millions) {
        case 0:
            tradMillions = "";
            break;
        case 1:
            tradMillions = convertLessThanOneThousand(millions) + " Million ";
            break;
        default:
            tradMillions = convertLessThanOneThousand(millions) + " Million ";
        }
        result = result + tradMillions;

        String tradHundredThousands;
        switch (hundredThousands) {
        case 0:
            tradHundredThousands = "";
            break;
        case 1:
            tradHundredThousands = "One Thousand";
            break;
        default:
            tradHundredThousands = convertLessThanOneThousand(hundredThousands) + " Thousand";
        }
        result = result + tradHundredThousands;

        String tradThousand;
        tradThousand = convertLessThanOneThousand(thousands);
        result = result + tradThousand;

        // remove extra spaces!
        return result.replaceAll("^\\s+", "").replaceAll("\\b\\s{2,}\\b", " ");
    }

    /**
     * testing
     * 
     * @param args
     */
    /*
     * public static void main(String[] args) {** zero** one** sixteen** one
     * hundred** one hundred eighteen** two hundred** two hundred nineteen**
     * eight hundred** eight hundred one** one thousand three hundred sixteen**
     * one million** two millions** three millions two hundred** seven hundred
     * thousand** nine millions** nine millions one thousand** one hundred
     * twenty three millions four hundred* fifty six thousand seven hundred
     * eighty nine** two billion one hundred forty seven millions* four hundred
     * eighty three thousand six hundred forty seven** three billion ten
     * System.out.println(converToWord("100.89"));
     * System.out.println(converToWord("100.00"));
     * System.out.println(converToWord("1000"));
     * System.out.println(converToWord("1000.87"));
     * System.out.println(converToWord("100.01"));
     * System.out.println(converToWord("100.")); }
     */
    public static String converToWord(final String number) {
        String num = "0";
        String deci = "0";
        String inWords = "";
        if (number.indexOf('.') > 0) {
            num = number.substring(0, number.indexOf('.'));
            if ((number.indexOf('.') + 1) < number.length()) {
                deci = number.substring(number.indexOf('.') + 1, number.length());
            }
        } else {
            num = number.substring(0, number.length());
        }
        inWords = convert(Long.parseLong(num));

        if (Long.parseLong(deci) > 0) {
            inWords += " Rupees And " + convert(Long.parseLong(deci)) + " Paisa";
        }
        return inWords + " Only";
    }

    public static boolean isValidIPAddress(final String ipaddresses) {
        if ((ipaddresses == null) || ipaddresses.equals("") || ipaddresses.equals("NA")) {
            return true;
        }

        // String ptrn = "^\\d{1,3}.\\d{1,3}.\\d{1,3}.\\d{1,3}";
        final String ptrn = "^.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}";

        final String[] arrIp = ipaddresses.split(",");
        for (final String ipaddr : arrIp) {
            if (ipaddr.matches(ptrn)) {
                final String[] parts = ipaddr.split("\\.");
                if (Integer.parseInt(parts[0]) == 0) {
                    return false;
                }
                for (final String part : parts) {
                    if (Integer.parseInt(part) > 255) {
                        return false;
                    }
                }
                // return true;
            } else {
                return false;
            }
        }

        return true;

    }

    public static String getCountryCode(final String IPAddress) {
        System.out.println("The IP Address passed in is " + IPAddress);
        // Chop off everything from the comma onwards
        StringBuffer buffer = new StringBuffer(IPAddress);
        final int index = buffer.indexOf(",");
        // See if there is comma
        if (index > 0) {
            final int length = buffer.length();
            buffer = buffer.delete(index, length);
        }
        final StringTokenizer tokens = new StringTokenizer(buffer.toString(), ".", false);
        long answer = 0L;
        int counter = 3;
        while (tokens.hasMoreTokens() && (counter >= 0)) {
            final long read = new Long(tokens.nextToken()).longValue();
            final long calculated = new Double(read * (Math.pow(256, counter))).longValue();
            answer += calculated;
            counter--;
            System.out.println("Iteratrions read backward - 3,2,1 no: " + (counter + 1));
        }
        final Long IPValue = new Long(answer);
        System.out.println("The IP Address value is: " + IPValue.toString());
        /*
         * try { IP2CountryCMPLocalHome ip2countryLocalHome =
         * IP2CountryCMPUtil.getLocalHome(); return
         * ip2countryLocalHome.getCountryCode(IPValue); } catch (NamingException
         * e) { System.out.println(e.getMessage()); return "GB"; }
         */
        return String.valueOf(IPValue);
    }

    public static List<String> checkDuplicateValues(final String[] type, final String[] values) {
        List<String> msg = new ArrayList<String>();

        for (int j = 0; j < values.length; j++) {
            final String tmpType = type[j];
            final String tmpUsr = values[j];
            for (int i = j + 1; i < values.length; i++) {
                if (tmpType.equalsIgnoreCase(type[i]) && tmpUsr.equalsIgnoreCase(values[i])
                        && !type[i].trim().equals("") && !values[i].trim().equals("")) {
                    msg.add("[" + values[i] + "]");
                }

            }
            msg = new ArrayList<String>(new TreeSet<String>(msg));

        }
        return msg;
    }

    /**
     * Validates the name of the web skin
     * 
     * @param skinName
     * @return
     */
    public static boolean isWebSkinNameValid(final String skinName) {

        if ((skinName == null) || (skinName.trim().length() > 15)) {
            return false;
        }
        return true;
    }

    public static String formatDeviceId(final String consumer) {

        if (consumer == null) {
            return "";
        }

        if (consumer.length() == 13) {
            return consumer.substring(3);
        } else if (consumer.length() == 12) {
            return consumer.substring(2);
        } else if (consumer.length() == 11) {
            return consumer.substring(1);
        } else if (consumer.length() == 10) {
            return consumer;
        } else {
            return consumer;
        }
    }

    // validate Blank String
    public static boolean isBlank(final String str) {

        if ((str == null) || str.trim().equals("")) {
            return true;
        }
        return false;
    }

    public static void main(final String[] a) {

        System.out.println(isRuPay("508999"));

    }

    public static boolean isRuPay(final String binNumber) {
        boolean isRuPay = false;

        try {
            final int bin = Integer.parseInt(binNumber);

            if ((bin >= 508500) && (bin <= 508999))// 1
            {
                isRuPay = true;
            } else if ((bin >= 606985) && (bin <= 607384))// 2
            {
                isRuPay = true;
            } else if ((bin >= 607385) && (bin <= 607484))// 3
            {
                isRuPay = true;
            } else if ((bin >= 607485) && (bin <= 607984))// 4
            {
                isRuPay = true;
            } else if ((bin >= 608001) && (bin <= 608100))// 5
            {
                isRuPay = true;
            } else if ((bin >= 608101) && (bin <= 608200))// 6
            {
                isRuPay = true;
            } else if ((bin >= 608201) && (bin <= 608300))// 7
            {
                isRuPay = true;
            } else if ((bin >= 608301) && (bin <= 608350))// 8
            {
                isRuPay = true;
            } else if ((bin >= 608351) && (bin <= 608500))// 9
            {
                isRuPay = true;
            } else if ((bin >= 652150) && (bin <= 652849))// 10
            {
                isRuPay = true;
            } else if ((bin >= 652850) && (bin <= 653049))// 11
            {
                isRuPay = true;
            } else if ((bin >= 653050) && (bin <= 653149))// 12
            {
                isRuPay = true;
            }

        } catch (final Exception e) {

        }
        return isRuPay;
    }
}
