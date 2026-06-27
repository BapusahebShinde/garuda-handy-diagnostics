package com.itek.rftaar.reader.epcwrapper.sgtin;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class iTEKGID96{
  
  private static final List<Integer> gidBarcodeLengthList = Arrays.asList(new Integer[]{13, 14, 16});
  private static long srNoLimit = 68719476735l;
  private static long managerLimit = 268435455l;
  private static long classLimit = 16777215l;
  private static String HEX_VALIDATION = "^([A-Fa-f0-9]{2})+$";
  private static String errorMessage = "";
  // boolean isHex = s.matches("[0-9A-F]+");
  private String gidHeader = "00110101";
  
  public static boolean isValidGID(final String barcode){
    return barcode.matches("^[0-9]+$") && gidBarcodeLengthList.contains(barcode.length());
  }
  
  public static String GetLastError(){
    return errorMessage;
  }
  
  private static boolean Validate(int intManager, int intClass, long longSerialNumber){
    try{
      if(intManager > managerLimit){
        errorMessage = "Manager number value exceed the defined limit. " + managerLimit;
        return false;
      }
      else if(intClass > classLimit){
        errorMessage = "Class number value exceed the defined limit. " + classLimit;
        return false;
      }
      if(longSerialNumber > srNoLimit){
        errorMessage = "Serial number value exceed the defined limit. " + srNoLimit;
        return false;
      }
      
      return true;
    }
    catch(Exception ex){
      errorMessage = "Validation Exception1: " + ex.getMessage();
      return false;
    }
  }
  
  public static String getBarcodeFromEpc(String epc){
    errorMessage = "";
    
    //String result = "Invalid EPC Format";
    String result = null;
    if(!epc.matches(HEX_VALIDATION)){
      errorMessage = "Invalid EPC Format";
      return result;
    }
    if(epc.length() != 24){
      errorMessage = "Invalid EPC Format";
      return result;
    }
    if(!epc.substring(0, 2).equalsIgnoreCase("35")){
      errorMessage = "Invalid EPC Header Format";
      return result;
    }
    try{
      String binaryEpc = null;
      StringBuilder binaryString = new StringBuilder();
      for(int i = 0; i < epc.length(); i++){
        int decimal = Integer.parseInt(Character.toString(epc.charAt(i)), 16);
        String binary = String.format("%4s", Integer.toBinaryString(decimal)).replace(' ', '0');
        binaryString.append(binary);
      }
      binaryEpc = binaryString.toString();
      //}
      
      String binaryHeader = binaryEpc.substring(0, 8);//header
      String binaryManager = binaryEpc.substring(8, 8 + 28);//manager
      String binaryClass = binaryEpc.substring(36, 36 + 24);//class
      String binarySerialNumber = binaryEpc.substring(60, 60 + 36);//serialnumber
      String textManager = Integer.parseInt(binaryManager, 2) + "";
      String textClass = Integer.parseInt(binaryClass, 2) + "";
      
      if(textManager.length() == 7){
        textManager += "0";
      }
      if(textManager.length() == 9 && textClass.length() < 7){
        textClass = String.format("%7s", textClass).replace(' ', '0');
      }
      if(textManager.length() == 8 && textClass.length() < 6){
        textClass = String.format("%6s", textClass).replace(' ', '0');
      }
      if(textManager.length() == 6 && textClass.length() < 7){
        textClass = String.format("%7s", textClass).replace(' ', '0');
      }
      result = textManager + textClass;
      return result;
    }
    catch(Exception ex){
      ex.printStackTrace();
      errorMessage = "Validation Exception: " + ex.getMessage();
      return result;
    }
  }
  
  public static Map<String,Object> getBarcodeAndSerialFromEpc(String epc){
    errorMessage = "";
    Map result = null;
    if(!epc.matches(HEX_VALIDATION)){
      errorMessage = "Invalid EPC Format";
      return result;
    }
    if(epc.length() != 24){
      errorMessage = "Invalid EPC Format";
      return result;
    }
    if(!epc.substring(0, 2).equalsIgnoreCase("35")){
      errorMessage = "Invalid EPC Header Format";
      return result;
    }
    try{
      String binaryEpc = null;
      StringBuilder binaryString = new StringBuilder();
      for(int i = 0; i < epc.length(); i++){
        int decimal = Integer.parseInt(Character.toString(epc.charAt(i)), 16);
        String binary = String.format("%4s", Integer.toBinaryString(decimal)).replace(' ', '0');
        binaryString.append(binary);
      }
      binaryEpc = binaryString.toString();
      System.out.println("DATA2 : " + binaryEpc);
      System.out.println("DATA2 Length: " + binaryEpc.length());
      
      //}
      
      String binaryHeader = binaryEpc.substring(0, 8);//header
      String binaryManager = binaryEpc.substring(8, 8 + 28);//manager
      String binaryClass = binaryEpc.substring(36, 36 + 24);//class
      String binarySerialNumber = binaryEpc.substring(60, 60 + 36);//serialnumber
      String textManager = Integer.parseInt(binaryManager, 2) + "";
      String textClass = Integer.parseInt(binaryClass, 2) + "";
      String textSerialNumber = Long.parseLong(binarySerialNumber, 2) + "";
      textSerialNumber = textSerialNumber.length()<11? getZeroAppendedBarcode(textSerialNumber,11) : textSerialNumber;
      
      
      if(textManager.length() == 7){
        textManager += "0";
      }
      if(textManager.length() == 9 && textClass.length() < 7){
        textClass = String.format("%7s", textClass).replace(' ', '0');
      }
      if(textManager.length() == 8 && textClass.length() < 6){
        textClass = String.format("%6s", textClass).replace(' ', '0');
      }
      if(textManager.length() == 6 && textClass.length() < 7){
        textClass = String.format("%7s", textClass).replace(' ', '0');
      }
      //result = textManager + textClass;
      
      Map<String, Object> m = new HashMap();
      String ean = textManager + textClass;
      m.put("ean", ean);
      m.put("serial", textSerialNumber);
      m.put("manager", textManager);
      m.put("class", textClass);
      return m;
    }
    catch(Exception ex){
      ex.printStackTrace();
      return result;
    }
  }
  
  private static String chkNull(String str, String def){
    return isNonEmpty(str) ? str.trim() : def;
  }
  
  private static boolean isNonEmpty(String str){
    return str != null && str.trim().length() > 0 && !str.trim().equalsIgnoreCase("null");
  }
  
  private static boolean isNullOrEmpty(String str){ return !isNonEmpty(str); }
  
  private static String getZeroAppendedBarcode(String barcode, int length){
    if(isNullOrEmpty(barcode)) return barcode;
    int len = length - barcode.length();
    if(len > 0) for(int i = 0; i < len; i++)
      barcode = "0" + barcode;
    return barcode;
  }
  
  public static String GetEpc(String barcode, long sr){
    errorMessage = "";
    boolean flag = false;
    String textManager = "";
    String textClass = "";
    try{
      if(barcode.length() == 13){
        textManager = barcode.substring(0, 6);
        textClass = barcode.substring(6, 6 + 7);
        flag = true;
      }
      else if(barcode.length() == 14){
        textManager = barcode.substring(0, 8);
        textClass = barcode.substring(8, 8 + 6);
        flag = true;
      }
      else if(barcode.length() == 16){
        textManager = barcode.substring(0, 9);
        textClass = barcode.substring(9, 9 + 7);
        flag = true;
      }
      if(flag){
        int numManager = Integer.parseInt(textManager);
        int numClass = Integer.parseInt(textClass);
        
        if(!Validate(numManager, numClass, sr)){
          return null;
        }
        String finalManager = String.format("%28s", Integer.toBinaryString(numManager)).replace(' ', '0');
        String finalClass = String.format("%24s", Integer.toBinaryString(numClass)).replace(' ', '0');
        String finalSerialNumber = String.format("%36s", Long.toBinaryString(sr)).replace(' ', '0');
        return BinaryStringToHexString("00110101" + finalManager + finalClass + finalSerialNumber);
      }
      return null;
    }
    catch(Exception ex){
      return ex.getMessage();
    }
  }
  
  private static String BinaryStringToHexString(String binary){
    if(binary == null || binary.isEmpty()){
      return binary;
    }
    StringBuilder stringBuilder = new StringBuilder(binary.length() / 8 + 1);
    if(binary.length() % 8 != 0){
      binary = String.format("%-" + (binary.length() / 8 + 1) * 8 + "s", binary).replace(' ', '0');
    }
    for(int i = 0; i < binary.length(); i += 8){
      String value = binary.substring(i, i + 8);
      stringBuilder.append(String.format("%02X", Integer.parseInt(value, 2)));
    }
    return stringBuilder.toString();
  }
  
  public static void main(String args[]){
    Map sq = getBarcodeAndSerialFromEpc("355F5E10000AC1F0000026FC");
    String epcc = "355f5e10000abcc2540be40a";
    Map s = getBarcodeAndSerialFromEpc(epcc);
    if(s != null){
      System.out.println("DATA: " + s.toString());
    }
    else{
      System.out.println("DATA ERROR: " + GetLastError());
    }
  }
}
