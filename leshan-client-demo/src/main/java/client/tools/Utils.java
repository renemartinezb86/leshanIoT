package client.tools;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by eremtas on 2017-08-28.
 */
public class Utils {

    public static boolean stringEquals( String str1, String str2 ){
            return str1 == null ? str2 == null : str1.equals(str2);
    }

    public static boolean doubleEqual( double a, double b, double eps){

        return Math.abs(a-b) < Math.abs(eps);

    }

    public static boolean doubleEqual( double a, double b){

        return doubleEqual(a, b, 1e-1);
    }

    public static double getTwoDigitValue(double value) {
        BigDecimal toBeTruncated = BigDecimal.valueOf(value);
        return toBeTruncated.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }


    public static double getOneDigitValue(double value) {
        BigDecimal toBeTruncated = BigDecimal.valueOf(value);
        return toBeTruncated.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}
