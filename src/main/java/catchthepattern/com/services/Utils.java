package catchthepattern.com.services;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;

import com.google.common.collect.HashBiMap;

import catchthepattern.com.models.DayRecord;

@Service
public class Utils {
    
    final static String EMAIL_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz!#$%&'*+-/=?^_`{|}~@.1234567890";
    final static String EMAIL_CHARS_ENCODED = "jA!#$FGDEwop56Bz78xyJKL`{@12STUVWXYZabMmn9N=?^_cdeHICfgO.P3|}~4RQqrstuv0%&'*+-/hikl";

    public static double getLargest(double[] arr) {
        double max = arr[0];
        for (int i = 1; i < arr.length; i++)
            if (arr[i] > max) max = arr[i];         
        return max;
    }
    
    public static double getSmallest(double[] arr) {
        double min = arr[0];
        for (int i = 1; i < arr.length; i++)
            if (arr[i] < min) min = arr[i];         
        return min;
    }
    
    // In the format of [[o,h,l c],...]
    public static double getLargest(double[][] arr) {
        double max = arr[0][1];
        for (int i = 1; i < arr.length; i++)
            if (arr[i][1] > max) max = arr[i][1];         
        return max;
    }
    
    // @param arr In the format of [[o,h,l c],...]
    public static double getSmallest(double[][] arr) {
        double min = arr[0][2];
        for (int i = 1; i < arr.length; i++)
            if (arr[i][2] < min) min = arr[i][2];         
        return min;
    }
    
    public static void reverseArray(Object[] array) {
        for (int i = 0; i < array.length / 2; i++) {
            Object temp = array[i];
            array[i] = array[array.length - 1 - i];
            array[array.length - 1 - i] = temp;
        }
    }
    
    public static void printDr(DayRecord dr) {
        if (dr != null) {
            
            System.out.println(
        
                dr.getDate() + 
                " O:" + dr.getOhlc()[0] + 
                " H: " + dr.getOhlc()[0] + 
                " L: " + dr.getOhlc()[0] + 
                " C:" + dr.getOhlc()[0] + 
                " T: " + dr.getTop() + 
                " B: " + dr.getBottom() +
                " M: " + dr.getMiddle() +
                " MA10: " + dr.getMa10() +
                " MA50: " + dr.getMa50() +
                " MA250: " + dr.getMa250());
                
        } else {
            System.out.println("printDr: dr is null");
        }
    }
    
    public static void printFirst5Rows(DayRecord[] drs) {  
        if (drs == null || drs.length == 0) {
            System.out.println("printFirst5Rows: dr is null or empty");
        } else {
            for (int i = 0; i< Math.min(5, drs.length); i++) {
                Utils.printDr(drs[i]);
            }
        }
    }
    
    public boolean containsNull(DayRecord[] drs) {  
        if (drs == null || drs.length == 0) {
            System.out.println("containsNull: dr is null or empty");
            return true;
        } else {
            for (int i = 0; i<  drs.length; i++) {
                if(drs[i] == null) return true;
            }
        }
        return false;
    }
    
    public static String encodeTicker(String ticker) {
    	
    	if (ticker.contains("*")) {
    		System.out.print("ENCODE TICKER GOT *");
    		return ticker;
    	}
    	
        String result = "";
        char[] arr = ticker.toCharArray();
        String resltStr = "";
        for (char c : arr) {
            try {
                resltStr += getCharNumberMap().get(c);
            } catch(Exception e) {
               // System.out.println("EXCEPTION Utils encodeTicker: " + c);
            }
        }
        
        try {
            result = "*" + (Long.parseLong(resltStr) + 9731)* 97;
        } catch(Exception e) {
            System.out.println("EXCEPTION Utils encodeTicker: " + ticker);
            return ticker;
        }
        
        return result; 
    }
    
    public static long encodePromo(long id) {
        return (id * 906355 + 1230) * 97 ;
    }
    
    public static long decodePromo(long promoCode) {
        if (promoCode % 97 != 0) return -1;
        if ((promoCode/97 - 1230) % 906355 != 0) return -1;
        return (promoCode/97 - 1230)/906355;
    }
    
    // generate devPromoCode from String "20230630"
    public String getDevPromoCode(String expireDate) {
        try {
            long dateNum = Long.parseLong(expireDate);
            return "" + encodePromo(dateNum);
        } catch(Exception e) {
            System.out.println("EXCEPTION: Utils getDevPromoCode()");
        }
        return "";
    }
    
    public String getDevPromoExpDate(String promoCode) {
        try {
            String _str = promoCode.substring(1);
            long dateNumber = decodePromo(Long.parseLong(_str));
            String dateString = "" + dateNumber;
            LocalDate expireDate = LocalDate.parse(dateString, DateTimeFormatter.BASIC_ISO_DATE);
            LocalDate currentDate = LocalDate.now();
    
            if (expireDate.isBefore(currentDate)) {
                System.out.println("The given date is before today's date.");
                return "Expired or invalid";
            } else {
                return "PromoCode Expires: " + expireDate;
            } 
        } catch(Exception e) {
            System.out.println("EXCEPTION: Utils getDevPromoExpDate()");
        }
        return "Expired or invalid";
    }
    
    public static String decodeTicker(String ticker) {
        if (ticker.startsWith("*")) {
            String result = "";
            ticker = ticker.substring(1);
            ticker = String.valueOf((Long.parseLong(ticker) / 97) - 9731);
            for (int i=0; i <= ticker.length()-2; i=i+2) {
                result += getNumberCharMap().get(ticker.substring(i, i+2));
            }
            return result;
        }
        return ticker;
    }
    
    public static List<String> removeStringsContainingChar(List<String> list, char c) {
        List<String> result = new ArrayList<String>();
        for (String str : list) {
            if (!str.contains(Character.toString(c))) {
                result.add(str);
            }
        }
        return result;
    }
    
    public static List<String> filterShortStrings(List<String> list) {
        List<String> result = new ArrayList<String>();
        for (String str : list) {
            if (str.length() < 5) {
                result.add(str);
            }
        }
        return result;
    }
    
    public static List<String> removeDup(List<String> list) {
        Set<String> set = new HashSet<>(list);
        list.clear();
        list.addAll(set);
        return list;
    }
    
    public double[] getAvgArr(double ori[], int range) {
        double[] result = new double[ori.length];
        if (range == 0 ) return null;
        if (range > ori.length) {
            for (double d : result) {d = -1;}
            return result;
        } else {
            double sum = 0;
            double avg0 = 0;
            for (int i=0; i< range; i++) { sum += ori[i]; }
            avg0 = sum/range;
            result[0] = avg0;
            
            for (int j=0; j<ori.length-1;j++) {
                if (j < ori.length - range) {
                    result[j+1] = result[j] - ori[j]/range + ori[j+1]/range;
                } else {
                    result[j+1] = -1;
                }
            }
            return result;
        }        
        
    }
    
    public double get2DecimalPlaces(double d) {
        DecimalFormat df = new DecimalFormat("0.00");
        String formattedNumber = df.format(d);
        try {
            return df.parse(formattedNumber).doubleValue();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return 0.00;
    }
    
    
    // !#$%&'*+-/=?^_`{|}~@.
    
    private static Map<Character, Integer> getCharNumberMap() {
        Map<Character, Integer> hm = new HashMap<>();
        hm.put('A', 11);
        hm.put('B', 12);
        hm.put('C', 13);
        hm.put('D', 14);
        hm.put('E', 15);
        hm.put('F', 16);
        hm.put('G', 17);
        hm.put('H', 18);
        hm.put('I', 19);
        hm.put('J', 20);
        hm.put('K', 21);
        hm.put('L', 22);
        hm.put('M', 23);
        hm.put('N', 24);
        hm.put('O', 25);
        hm.put('P', 26);
        hm.put('Q', 27);
        hm.put('R', 28);
        hm.put('S', 29);
        hm.put('T', 30);
        hm.put('U', 31);
        hm.put('V', 32);
        hm.put('W', 33);
        hm.put('X', 34);
        hm.put('Y', 35);
        hm.put('Z', 36);     
        hm.put('.', 37);
        
        return hm;
    }
    
    private static Map<Character, Character> getPromoEncodeCharMap() {
        Map<Character, Character> hm = new HashMap<>();
        for (int i=0; i < EMAIL_CHARS.length(); i++ ) {
            hm.put(EMAIL_CHARS.charAt(i), EMAIL_CHARS_ENCODED.charAt(i));
        }        
        return hm;
    }
    
    private static Map<Character, Character> getPromoDecodeCharMap() {
        Map<Character, Character> hm = new HashMap<>();
        for (int i=0; i < EMAIL_CHARS_ENCODED.length(); i++ ) {
            hm.put(EMAIL_CHARS_ENCODED.charAt(i), EMAIL_CHARS.charAt(i));
        }        
        return hm;
    }
    
    
    private static Map<String, Character> getNumberCharMap() {
        Map<String, Character> hm = new HashMap<>();
        for (Map.Entry<Character, Integer> entry : getCharNumberMap().entrySet()) {
            hm.put(String.valueOf(entry.getValue()), entry.getKey());
        }        
        return hm;
    }    
    

    public static String sanitize(String inputString) {
        final Pattern UNSAFE_CHARACTERS_PATTERN = Pattern.compile("[;'\"]");
        Matcher matcher = UNSAFE_CHARACTERS_PATTERN.matcher(inputString);
        return matcher.replaceAll("");
    }
    
    public static List<String> mergeLists(List<String> list1, List<String> list2) {
        // Create a HashSet to store the merged unique strings
        Set<String> uniqueStrings = new HashSet<>();

        // Add all strings from list1 to the HashSet
        uniqueStrings.addAll(list1);

        // Add all strings from list2 to the HashSet
        uniqueStrings.addAll(list2);

        // Create a new ArrayList to store the merged and de-duplicated strings
        List<String> mergedList = new ArrayList<>(uniqueStrings);

        return mergedList;
    }
    
    public static List<String> findMissingStrings(List<String> list1, List<String> list2) {
        // Create a new ArrayList to store the missing strings
        List<String> missingStrings = new ArrayList<>();

        // Loop through the strings in list1
        for (String str : list1) {
            // If the string is not in list2, add it to missingStrings
            if (!list2.contains(str)) {
                missingStrings.add(str);
            }
        }

        return missingStrings;
    }
    
    public static double variance(double[] arr) {
        double mean = 0.0;
        double sum = 0.0;
        double variance = 0.0;
        int n = arr.length;

        // Calculate the mean
        for (double num : arr) {
            mean += num;
        }
        mean /= n;

        // Calculate the sum of the squared differences from the mean
        for (double num : arr) {
            sum += Math.pow(num - mean, 2);
        }

        // Calculate the variance
        variance = sum / n;

        return variance;
    }
    
    public static double average(double[] arr) {
        if (arr == null || arr.length == 0) return 0.0;
        double sum = 0.0;
        int n = arr.length;

        // Calculate the sum of the values in the array
        for (double num : arr) {
            sum += num;
        }

        // Calculate the average
        double average = sum / n;

        return average;
    }
    
    public static Date getTodayInChicago() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        LocalDate localDate = getTodayInChicagoTime();
         Date date = null;
         try {
             date = dateFormat.parse(localDate.toString());
         } catch (ParseException e) {
             e.printStackTrace();
         }
         return date;
    }
    
    
    public static LocalDate getTodayInChicagoTime() {
        ZoneId chicagoZone = ZoneId.of("America/Chicago");
        ZonedDateTime chicagoTime = ZonedDateTime.now(chicagoZone);
        return chicagoTime.toLocalDate();
    }
    
    public static boolean isStringEqualsToDate(String dateString, Date compareDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String date2String = dateFormat.format(compareDate);
        return dateString.equals(date2String);
    }
    
    public static boolean isTradingDay() {
        LocalDate today = getTodayInChicagoTime();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            // Weekend, market closed
            return false;
        }
        
        int month = today.getMonthValue();
        int dayOfMonth = today.getDayOfMonth();
        
        switch (month) {
            case 1: // January
                if (dayOfMonth == 1) {
                    // New Year's Day, market closed
                    return false;
                } 
                /*
                   * if (dayOfWeek == DayOfWeek.MONDAY && dayOfMonth <= 7) {
                   * // Observed New Year's Day, market closed
                   * return false;
                   * }
                   */
                break;
            case 2: // February
                if (dayOfWeek == DayOfWeek.MONDAY && dayOfMonth >= 15 && dayOfMonth <= 21) {
                    // Presidents' Day, market closed
                    return false;
                }
                break;
            case 4: // April
                /*
                 * if (dayOfMonth == 2) {
                 * // Good Friday, market closed
                 * return false;
                 * }
                 * break;
                 */
            case 5: // May
                if (dayOfWeek == DayOfWeek.MONDAY && dayOfMonth > 24 && dayOfMonth < 32) {
                    return false;
                }
                
                break;
            case 7: // July
                if (dayOfMonth == 4) {
                    // Independence Day, market closed
                    return false;
                }
                break;
            case 9: // September
                if (dayOfWeek == DayOfWeek.MONDAY && dayOfMonth < 8) {
                    // Labor Day, market closed
                    return false;
                }
                break;
            case 11: // November
                if (dayOfWeek == DayOfWeek.THURSDAY && dayOfMonth >= 22 && dayOfMonth <= 28) {
                    // Thanksgiving Day, market closed
                    return false;
                }
                break;
            case 12: // December
                if (dayOfMonth == 25) {
                    // Christmas Day, market closed
                    return false;
                }               
                break;
            default:
                // All other days are trading days
                return true;
        }
        
        // If we got here, it's a holiday but not a market holiday, so it's a trading day
        return true;
    }
    
    public static long daysBetween(String startDateStr, String endDateStr) {
        try {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

			LocalDate startDate = LocalDate.parse(startDateStr, formatter);
			LocalDate endDate = LocalDate.parse(endDateStr, formatter);

			return ChronoUnit.DAYS.between(startDate, endDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
    }
    
    public static long daysBetween(Date startDate, Date endDate) {
        // Convert java.util.Date to java.time.LocalDate
        try {
			LocalDate startLocalDate = startDate.toInstant()
			                                    .atZone(ZoneId.systemDefault())
			                                    .toLocalDate();
			LocalDate endLocalDate = endDate.toInstant()
			                                .atZone(ZoneId.systemDefault())
			                                .toLocalDate();

			return ChronoUnit.DAYS.between(startLocalDate, endLocalDate);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
    }
    
    public static boolean areDoublesEqual(double a, double b, double epsilon) {
        return Math.abs(a - b) < epsilon;
    }
    
    public static boolean isDoubleZero(double value, double epsilon) {
        return Math.abs(value) < epsilon;
    }
    
    public static String getDateStrFromSqlDate(Date date) {
    	String dateStr = "";
    	try {
    	    Instant instant = Instant.ofEpochMilli(date.getTime());  // ✅ Safe alternative
    	    LocalDate localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate();
    	    dateStr = localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    	} catch (Exception e) {
    	    System.out.println("getDateStrFromSqlDate Exception: " + e.getMessage());
    	    e.printStackTrace();
    	}
    	return dateStr;
    }  
    
    public static double findMedian(List<Double> nums) {
        if (nums == null || nums.isEmpty()) {
            throw new IllegalArgumentException("List must not be null or empty.");
        }

        // Create a copy to avoid modifying the original list
        List<Double> sorted = new ArrayList<>(nums);
        Collections.sort(sorted);

        int n = sorted.size();
        if (n % 2 == 1) {
            // Odd number of elements
            return sorted.get(n / 2);
        } else {
            // Even number of elements
            return (sorted.get(n / 2 - 1) + sorted.get(n / 2)) / 2.0;
        }
    }
}
