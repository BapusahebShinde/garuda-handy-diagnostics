package com.itek.rftaar.reader.epcwrapper.sgtin;

import java.math.BigInteger;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

/**
 * The Sgtin 96.
 * used for Operations for Standard EAN
 * or EPC with length 24
 */
public class SGTIN96{
  
  //https://www.gs1.org/sites/default/files/docs/epc/tds_1_1_rev_1_27-standard-20050510.pdf
  //https://www.gs1us.org/tools/epc-encoder-decoder
  // Table 6. The EPC SGTIN-96 bit allocation, header, and maximum decimal values. page 27.
  private final static Integer BIN = 2;
  private final static Integer HEX = 16;
  private final static String SGTIN_96_HEADER = "00110000";
  private final static Integer SGTIN_96_FILTER_VALUE = 3;
  private final static Integer SGTIN_96_PARTITION_VALUE = 3;
  private final static Integer SGTIN_96_SERIAL_NUMBER = 38;
  private final static Integer SGTIN_96_LENGTH = 96;
  private final static Integer SGTIN_96_LENGTH_HEX = 24;
  
  public static String epcLengthError = "EPC must be 24 characters long";
  public static String epcHeaderError = "EPC header does not correlate to SGTIN-96";
  public static String partionValueMaxError = "Partition value cannot be greater than 6";
  public static String companyPrefixExceedError = "Company Prefix exceeded specified length";
  public static String itemReferenceAndIndicatorExceedError = "Item Reference and Indicator exceeded specified length";
  private static HashMap<Integer, int[]> sgtin96CompanyPrefixLenPartitions;
  private static String HEX_VALIDATION = "^([A-Fa-f0-9]{2})+$";
  
  static{
    // Table 7. SGTIN-96 Partitions. page 28.
    // Column order: (L), P, M, N Nd
    sgtin96CompanyPrefixLenPartitions = new HashMap<Integer, int[]>();
    sgtin96CompanyPrefixLenPartitions.put(12, new int[]{0, 40, 4, 1});
    sgtin96CompanyPrefixLenPartitions.put(11, new int[]{1, 37, 7, 2});
    sgtin96CompanyPrefixLenPartitions.put(10, new int[]{2, 34, 10, 3});
    sgtin96CompanyPrefixLenPartitions.put(9, new int[]{3, 30, 14, 4});
    sgtin96CompanyPrefixLenPartitions.put(8, new int[]{4, 27, 17, 5});
    sgtin96CompanyPrefixLenPartitions.put(7, new int[]{5, 24, 20, 6});
    sgtin96CompanyPrefixLenPartitions.put(6, new int[]{6, 20, 24, 7});
  }
  
  /**
   * Get partitions by company prefix length in digits int [ ].
   *
   * @param company_prefix_length the company prefix length
   * @return the int [ ]
   */
  private static int[] getPartitionsByCompanyPrefixLengthInDigits(int company_prefix_length){
    // column 3 (L)
    return sgtin96CompanyPrefixLenPartitions.get(company_prefix_length);
  }
  
  /**
   * Long to binary with fill string.
   *
   * @param number the number
   * @param bits   the bits
   * @return the string
   */
  private static String longToBinaryWithFill(Long number, int bits){
    // This is never called with any number larger than 40 bits, so Long is a good choice
    return zeroFill(Long.toBinaryString(number), bits);
  }
  
  /**
   * Zero fill string.
   *
   * @param s the s
   * @param n the n
   * @return the string
   */
  private static String zeroFill(String s, int n){
    int fill = n - s.length();
    String Zeroes = "";
    if(fill > 0){
      Zeroes = new String(new char[fill]).replace("\0", "0");
    }
    return Zeroes + s;
  }
  
  /**
   * Binary to hex string.
   *
   * @param bin the bin
   * @return the string
   */
  private static String binaryToHex(String bin){
    // bin_epc is 96 bits, so need to use BitInt for hex conversion
    return new BigInteger(bin, BIN).toString(HEX);
  }
  
  /**
   * Hex to binary string.
   *
   * @param hex the hex
   * @return the string
   */
  private static String hexToBinary(String hex){
    return new BigInteger(hex, HEX).toString(BIN);
  }
  
  /**
   * Binary to long long.
   *
   * @param bin the bin
   * @return the long
   */
  private static Long binaryToLong(String bin){
    return Long.parseLong(bin, BIN);
  }
  
  /**
   * Binary to int integer.
   *
   * @param bin the bin
   * @return the integer
   */
  private static Integer binaryToInt(String bin){
    return Integer.parseInt(bin, BIN);
  }
  
  /**
   * Encode upc string.
   *
   * @param upc           the upc
   * @param serial_number the serial number
   * @return the string
   */
  public static String encodeUPC(String upc, Long serial_number){
    if(upc.length() != 12){
      throw new IllegalArgumentException("UPC must be 12 digits long");
    }
    // UPC-12 barcodes in this use case have 6 digit company identifier
    // Table 5. SGTIN Filter Values. page 24.
    return convertToSGTIN96(zeroFill(upc, 14), 6, serial_number, 0);
  }
  
  /**
   * Convert to sgtin 96 string.
   *
   * @param gtin14                the gtin 14
   * @param company_prefix_length the company prefix length
   * @param serial_number         the serial number
   * @param filter_value          the filter value
   * @return the string
   */
  public static String convertToSGTIN96(String gtin14, Integer company_prefix_length, Long serial_number, Integer filter_value){
    if(chkNull(gtin14, "").length() < 14) gtin14 = getZeroAppendedBarcode(gtin14, 14);
    // 3.4.2.1 SGTIN-96 Encoding Procedure. page 28.
    if(gtin14.length() < 14){
      gtin14 = getZeroAppendedBarcode(gtin14, 14);
    }
    if(serial_number.toString().length() > 1 && serial_number.toString().charAt(0) == '0'){
      throw new IllegalArgumentException("serial number may not begin with 0");
    }
    int[] partitions = getPartitionsByCompanyPrefixLengthInDigits(company_prefix_length);
    if(partitions == null){
      throw new IllegalArgumentException("company prefix length must be <=12 and >= 6");
    }
    int partition_value = partitions[0];
    int company_prefix_bits = partitions[1];
    int item_reference_and_indicator_bits = partitions[2];
    
    if(gtin14.length() != 14){
      throw new IllegalArgumentException("GTIN must be 14 digits long");
    }
    
    Long company_prefix = Long.valueOf(gtin14.substring(1, (company_prefix_length + 1)));
    Long item_reference_and_indicator = Long.valueOf(gtin14.charAt(0) + gtin14.substring(company_prefix_length + 1, 13));
    
    String bin_filter_value = longToBinaryWithFill((long) filter_value, SGTIN_96_FILTER_VALUE);
    String bin_partition_value = longToBinaryWithFill((long) partition_value, SGTIN_96_PARTITION_VALUE);
    String bin_company_prefix = longToBinaryWithFill(company_prefix, company_prefix_bits);
    String bin_item_reference = longToBinaryWithFill(item_reference_and_indicator, item_reference_and_indicator_bits);
    String bin_serial_number = longToBinaryWithFill(serial_number, SGTIN_96_SERIAL_NUMBER);
    
    String bin_epc = SGTIN_96_HEADER + bin_filter_value + bin_partition_value + bin_company_prefix + bin_item_reference + bin_serial_number;
    
    return zeroFill(binaryToHex(bin_epc), SGTIN_96_LENGTH_HEX);
  }
  
  public static Map<String, Object> getBarcodeAndSerialFromEpc(String sgtin96_epc){
    //if(sgtin96_epc.length() >= 24) sgtin96_epc = sgtin96_epc.substring(0, 24);
    // 3.4.2.2 SGTIN-96 Decoding Procedure. page 29.
    if(sgtin96_epc.length() != SGTIN_96_LENGTH_HEX){
      throw new IllegalArgumentException(epcLengthError);
    }
    String binary = zeroFill(hexToBinary(sgtin96_epc), SGTIN_96_LENGTH);//epc,96
    
    String header = binary.substring(0, 8);
    if(!header.equals(SGTIN_96_HEADER)){
      throw new IllegalArgumentException(epcHeaderError);
    }
    
    Integer filter_value = binaryToInt(binary.substring(8, 11));
    Integer partition_value = binaryToInt(binary.substring(11, 14));
    if(partition_value > 6){
      throw new IllegalArgumentException(partionValueMaxError);
    }
    
    int company_prefix_len_bits = 24;
    int company_prefix_len_digits = 7;
    
    Long long_company_prefix_value = binaryToLong(binary.substring(14, 14 + company_prefix_len_bits));
    if(long_company_prefix_value >= Math.pow(10, company_prefix_len_digits)){
      throw new IllegalArgumentException(companyPrefixExceedError);
    }
    String company_prefix = zeroFill(long_company_prefix_value.toString(), company_prefix_len_digits);
    
    int item_reference_and_indicator_len_digits = 13 - company_prefix_len_digits;
    Integer int_item_reference_and_indicator = binaryToInt(binary.substring(14 + company_prefix_len_bits, 58));
    if(int_item_reference_and_indicator >= Math.pow(10, item_reference_and_indicator_len_digits)){
      throw new IllegalArgumentException(itemReferenceAndIndicatorExceedError);
    }
    String item_reference_and_indicator = zeroFill(int_item_reference_and_indicator.toString(), item_reference_and_indicator_len_digits);
    
    String thirteen = item_reference_and_indicator.substring(0, 1) + company_prefix + item_reference_and_indicator.substring(1);
    int termA = 0;
    int termB = 0;
    for(int i = 0; i < thirteen.length(); i++){
      String c = thirteen.substring(i, i + 1);
      if(i % 2 == 0){
        termB = termB + Integer.parseInt(c);//ODD
      }
      else{
        termA = termA + Integer.parseInt(c);/***EVEN*/
      }
    }
    
    int check_digit = Math.abs(((-3 * termB) - termA) % 10);
    int s = Integer.parseInt(thirteen.substring(0, 1));
    if(check_digit != 0){
      s = s + 10;
      
    }
    
    if(check_digit < 10){
      check_digit = 10 - check_digit;
    }
    if(check_digit == 10){
      check_digit = 0;
    }
    String gtin14 = thirteen + String.valueOf(check_digit);
    Long serial_number = binaryToLong(binary.substring(58));
    
    String serialNo = serial_number.toString();
    serialNo = serialNo.length()<12? getZeroAppendedBarcode(serialNo,12) : serialNo;
    
    //inventory.tagtype = 1;
    Map<String, Object> tagInfo = new HashMap<>();
    tagInfo.put("ean", gtin14);
    tagInfo.put("serial", serialNo);
    //inventory.setEanAndSerial(sgtin96_epc,gtin14, serial_number.toString());
    return tagInfo;
  }
  
  /**
   * Get sku from epc string.
   *
   * @param sgtin96_epc the sgtin 96 epc
   * @return the string
   */
  public static String getSkuFromEpc(String sgtin96_epc){
    if(sgtin96_epc.length() >= 24) sgtin96_epc = sgtin96_epc.substring(0, 24);
    // 3.4.2.2 SGTIN-96 Decoding Procedure. page 29.
    if(sgtin96_epc.length() != SGTIN_96_LENGTH_HEX){
      throw new IllegalArgumentException(epcLengthError);
    }
    String binary = zeroFill(hexToBinary(sgtin96_epc), SGTIN_96_LENGTH);//epc,96
    
    String header = binary.substring(0, 8);
    if(!header.equals(SGTIN_96_HEADER)){
      throw new IllegalArgumentException(epcHeaderError);
    }
    
    Integer filter_value = binaryToInt(binary.substring(8, 11));
    Integer partition_value = binaryToInt(binary.substring(11, 14));
    if(partition_value > 6){
      throw new IllegalArgumentException(partionValueMaxError);
    }
    
    int company_prefix_len_bits = 24;
    int company_prefix_len_digits = 7;
    
    Long long_company_prefix_value = binaryToLong(binary.substring(14, 14 + company_prefix_len_bits));
    if(long_company_prefix_value >= Math.pow(10, company_prefix_len_digits)){
      throw new IllegalArgumentException(companyPrefixExceedError);
    }
    String company_prefix = zeroFill(long_company_prefix_value.toString(), company_prefix_len_digits);
    
    int item_reference_and_indicator_len_digits = 13 - company_prefix_len_digits;
    Integer int_item_reference_and_indicator = binaryToInt(binary.substring(14 + company_prefix_len_bits, 58));
    if(int_item_reference_and_indicator >= Math.pow(10, item_reference_and_indicator_len_digits)){
      throw new IllegalArgumentException(itemReferenceAndIndicatorExceedError);
    }
    String item_reference_and_indicator = zeroFill(int_item_reference_and_indicator.toString(), item_reference_and_indicator_len_digits);
    
    String thirteen = item_reference_and_indicator.substring(0, 1) + company_prefix + item_reference_and_indicator.substring(1);
    int termA = 0;
    int termB = 0;
    for(int i = 0; i < thirteen.length(); i++){
      String c = thirteen.substring(i, i + 1);
      if(i % 2 == 0){
        termB = termB + Integer.parseInt(c);//ODD
      }
      else{
        termA = termA + Integer.parseInt(c);/***EVEN*/
      }
    }
    
    int check_digit = Math.abs(((-3 * termB) - termA) % 10);
    int s = Integer.parseInt(thirteen.substring(0, 1));
    if(check_digit != 0){
      s = s + 10;
      
    }
    
    if(check_digit < 10){
      check_digit = 10 - check_digit;
    }
    if(check_digit == 10){
      check_digit = 0;
    }
    String gtin14 = thirteen + String.valueOf(check_digit);
    Long serial_number = binaryToLong(binary.substring(58));
    
    return gtin14;
  }
  
  /**
   * Get sku from epc 13 string.
   *
   * @param sgtin96_epc the sgtin 96 epc
   * @return the string
   */
  public static String getSkuFromEpc13(String sgtin96_epc){
    if(sgtin96_epc.length() >= 24) sgtin96_epc = sgtin96_epc.substring(0, 24);
    // 3.4.2.2 SGTIN-96 Decoding Procedure. page 29.
    if(sgtin96_epc.length() != SGTIN_96_LENGTH_HEX){
      throw new IllegalArgumentException(epcLengthError);
    }
    String binary = zeroFill(hexToBinary(sgtin96_epc), SGTIN_96_LENGTH);//epc,96
    
    String header = binary.substring(0, 8);
    if(!header.equals(SGTIN_96_HEADER)){
      throw new IllegalArgumentException(epcHeaderError);
    }
    
    Integer filter_value = binaryToInt(binary.substring(8, 11));
    Integer partition_value = binaryToInt(binary.substring(11, 14));
    if(partition_value > 6){
      throw new IllegalArgumentException(partionValueMaxError);
    }
    
    int company_prefix_len_bits = 24;
    int company_prefix_len_digits = 7;
    
    Long long_company_prefix_value = binaryToLong(binary.substring(14, 14 + company_prefix_len_bits));
    if(long_company_prefix_value >= Math.pow(10, company_prefix_len_digits)){
      throw new IllegalArgumentException(companyPrefixExceedError);
    }
    String company_prefix = zeroFill(long_company_prefix_value.toString(), company_prefix_len_digits);
    
    int item_reference_and_indicator_len_digits = 13 - company_prefix_len_digits;
    Integer int_item_reference_and_indicator = binaryToInt(binary.substring(14 + company_prefix_len_bits, 58));
    if(int_item_reference_and_indicator >= Math.pow(10, item_reference_and_indicator_len_digits)){
      throw new IllegalArgumentException(itemReferenceAndIndicatorExceedError);
    }
    String item_reference_and_indicator = zeroFill(int_item_reference_and_indicator.toString(), item_reference_and_indicator_len_digits);
    
    String thirteen = item_reference_and_indicator.substring(0, 1) + company_prefix + item_reference_and_indicator.substring(1);
    int termA = 0;
    int termB = 0;
    for(int i = 0; i < thirteen.length(); i++){
      String c = thirteen.substring(i, i + 1);
      if(i % 2 == 0){
        termB = termB + Integer.parseInt(c);//ODD
      }
      else{
        termA = termA + Integer.parseInt(c);//EVEN
      }
    }
    int check_digit = Math.abs(((-3 * termB) - termA) % 10);
    int s = Integer.parseInt(thirteen.substring(0, 1));
    if(check_digit != 0){
      s = s + 10;
      
    }
    
    if(check_digit < 10){
      check_digit = 10 - check_digit;
    }
    if(check_digit == 10){
      check_digit = 0;
    }
    String gtin14 = thirteen + String.valueOf(check_digit);
    
    if(gtin14.length() == 14){
      gtin14 = getLeftZeroReplacedString(gtin14);
    }
    return gtin14;
  }
  
  private static String getLeftZeroReplacedString(String barcode){
    return chkNull(barcode, "").replaceFirst("^0+(?!$)", "");
  }
  
  /**
   * Get serial from epc string.
   *
   * @param sgtin96_epc the sgtin 96 epc
   * @return the string
   */
  public static String getSerialFromEPC(String sgtin96_epc){
    if(sgtin96_epc.length() >= 24) sgtin96_epc = sgtin96_epc.substring(0, 24);
    // 3.4.2.2 SGTIN-96 Decoding Procedure. page 29.
    if(sgtin96_epc.length() != SGTIN_96_LENGTH_HEX){
      throw new IllegalArgumentException(epcLengthError);
    }
    String binary = zeroFill(hexToBinary(sgtin96_epc), SGTIN_96_LENGTH);//epc,96
    
    String header = binary.substring(0, 8);
    if(!header.equals(SGTIN_96_HEADER)){
      throw new IllegalArgumentException(epcHeaderError);
    }
    
    Integer filter_value = binaryToInt(binary.substring(8, 11));
    Integer partition_value = binaryToInt(binary.substring(11, 14));
    if(partition_value > 6){
      throw new IllegalArgumentException(partionValueMaxError);
    }
    
    int company_prefix_len_bits = 24;
    int company_prefix_len_digits = 7;
    
    Long long_company_prefix_value = binaryToLong(binary.substring(14, 14 + company_prefix_len_bits));
    if(long_company_prefix_value >= Math.pow(10, company_prefix_len_digits)){
      throw new IllegalArgumentException(companyPrefixExceedError);
    }
    String company_prefix = zeroFill(long_company_prefix_value.toString(), company_prefix_len_digits);
    
    int item_reference_and_indicator_len_digits = 13 - company_prefix_len_digits;
    Integer int_item_reference_and_indicator = binaryToInt(binary.substring(14 + company_prefix_len_bits, 58));
    if(int_item_reference_and_indicator >= Math.pow(10, item_reference_and_indicator_len_digits)){
      throw new IllegalArgumentException(itemReferenceAndIndicatorExceedError);
    }
    String item_reference_and_indicator = zeroFill(int_item_reference_and_indicator.toString(), item_reference_and_indicator_len_digits);
    
    String thirteen = item_reference_and_indicator.substring(0, 1) + company_prefix + item_reference_and_indicator.substring(1);
    int termA = 0;
    int termB = 0;
    for(int i = 0; i < thirteen.length(); i++){
      String c = thirteen.substring(i, i + 1);
      if(i % 2 == 0){
        termB = termB + Integer.parseInt(c);//ODD
      }
      else{
        termA = termA + Integer.parseInt(c);//EVEN
      }
    }
    
    int check_digit = Math.abs(((-3 * termB) - termA) % 10);
    int s = Integer.parseInt(thirteen.substring(0, 1));
    if(check_digit != 0){
      s = s + 10;
      
    }
    if(check_digit < 10){
      check_digit = 10 - check_digit;
    }
    if(check_digit == 10){
      check_digit = 0;
    }
    String gtin14 = thirteen + String.valueOf(check_digit);
    Long serial_number = binaryToLong(binary.substring(58));
    
    return serial_number.toString();
  }
  
  /**
   * Decode hex char int.
   *
   * @param c the c
   * @return the int
   */
  private static int DecodeHexChar(char c){
    if(c >= '0' && c <= '9'){
      return (int) (c - '0');
    }
    else if(c >= 'A' && c <= 'F'){
      return (int) (10 + c - 'A');
    }
    else if(c >= 'a' && c <= 'f'){
      return (int) (10 + c - 'a');
    }
    else{
      throw new NumberFormatException();
    }
  }
  
  /**
   * Binary string to bit array bit set.
   *
   * @param epcCode the epc code
   * @return the bit set
   */
  public static BitSet BinaryStringToBitArray(String epcCode){
    BitSet bits = new BitSet(epcCode.length() * 4);
    for(int i = 0; i < epcCode.length(); i++){
      int b = DecodeHexChar(epcCode.charAt(i));
      for(int bit = 0; bit < 4; bit++){
        bits.set(i * 4 + bit, (b & (0x08 >> bit)) != 0);
      }
    }
    return bits;
  }
  
  /**
   * Decode string string.
   *
   * @param bits     the bits
   * @param firstBit the first bit
   * @param bitCount the bit count
   * @return the string
   */
  public static String DecodeString(BitSet bits, int firstBit, int bitCount){
    
    int bitPos = firstBit;
    int lastBit = firstBit + bitCount - 1;
    
    StringBuilder sb = new StringBuilder(40);
    while(bitPos < lastBit){
      char c = (char) DecodeUInt32(bits, bitPos, 7);
      if(c == 0){
        break;
      }
      
      if(c == '%'){
        int ascii = (DecodeHexChar((char) DecodeUInt32(bits, bitPos + 7, 7)) << 4) + DecodeHexChar((char) DecodeUInt32(bits, bitPos + 14, 7));
        c = (char) ascii;
        bitPos += 21;
      }
      else{
        bitPos += 7;
      }
      
      sb.append(c);
    }
    
    return sb.toString();
  }
  
  /**
   * Decode u int 32 int.
   *
   * @param bits     the bits
   * @param firstBit the first bit
   * @param bitCount the bit count
   * @return the int
   * @throws IndexOutOfBoundsException the index out of bounds exception
   */
  public static int DecodeUInt32(BitSet bits, int firstBit, int bitCount) throws IndexOutOfBoundsException{
    if(firstBit < 0 || firstBit + bitCount > bits.length()){
      /** throw new IndexOutOfBoundsException("firstBit", "firstBit must be in [0...bits.Length-length]");*/
    }
    
    int result = 0;
    for(int i = 0; i < bitCount; i++){
      if(bits.get(firstBit + bitCount - 1 - i)){
        result |= (1 << i);
      }
    }
    return result;
  }
  
  /**
   * Is valid ean boolean.
   *
   * @param code the code
   * @return the boolean
   */
  public static boolean IsValidEan(String code){
    if(chkNull(code, "").matches("[0-9]+$")){
      EANType articleNumberType = AnalyzeArticleNumberType(code);
      if(articleNumberType == EANType.EAN13){
        return true;
      }
    }
    return false;
  }
  
  /**
   * Is valid gtin boolean.
   *
   * @param code the code
   * @return the boolean
   */
  //Only For Std Barcode
  public static boolean IsValidGtin(String code){
    final boolean is11DigitStdEan = false;
    if(chkNull(code, "").length() > 14 || !code.matches("^[0-9]+$") || !String.valueOf(chkNull(code, "").trim().length()).matches(is11DigitStdEan ? "(8|11|12|13|14)" : "(8|12|13|14)"))
      return false;
    //code for handling other then GTIN type Barcodes (EAN8/UPC/EAN13)
    if(chkNull(code, "").length() <= 8 && AnalyzeArticleNumberType(getZeroAppendedBarcode(code, 8)) == EANType.EAN8)
      return true;
    if(chkNull(code, "").length() <= 12 && AnalyzeArticleNumberType(getZeroAppendedBarcode(code, 12)) == EANType.UPC)
      return true;
    if(chkNull(code, "").length() <= 13 && AnalyzeArticleNumberType(getZeroAppendedBarcode(code, 13)) == EANType.EAN13)
      return true;
    if(chkNull(code, "").length() < 14) code = getZeroAppendedBarcode(code, 14);
    return AnalyzeArticleNumberType(code) == EANType.GTIN;
  }
  
  private static String getZeroAppendedBarcode(String barcode, int length){
    if(isNullOrEmpty(barcode)) return barcode;
    int len = length - barcode.length();
    if(len > 0) for(int i = 0; i < len; i++)
      barcode = "0" + barcode;
    return barcode;
  }
  
  /**
   * Analyze article number type ean type.
   *
   * @param code the code
   * @return the ean type
   */
  private static EANType AnalyzeArticleNumberType(String code){
    EANType articleNumberType = EANType.UNKNOWN;
    
    long temp;
    OutObject<Long> tempOut_temp = new OutObject<Long>();
    if(!TryParseHelper.tryParseLong(code, tempOut_temp)){
      temp = tempOut_temp.argValue;
      return articleNumberType;
    }
    else{
      temp = tempOut_temp.argValue;
    }
    switch(code.length()){
      case 8: //EAN-8
        articleNumberType = EANType.EAN8;
        break;
      case 12: //UPC
        articleNumberType = EANType.UPC;
        break;
      case 13: //EAN-13
        articleNumberType = EANType.EAN13;
        break;
      case 14: //GTIN
        articleNumberType = EANType.GTIN;
        break;
      default:
        //wrong number of digits
        return articleNumberType;
    }
    code = String.format("%014d", temp);
    
    int a[] = new int[13];
    a[0] = (code.charAt(0) - '0') * 3;
    a[1] = code.charAt(1) - '0';
    a[2] = (code.charAt(2) - '0') * 3;
    a[3] = code.charAt(3) - '0';
    a[4] = (code.charAt(4) - '0') * 3;
    a[5] = code.charAt(5) - '0';
    a[6] = (code.charAt(6) - '0') * 3;
    a[7] = code.charAt(7) - '0';
    a[8] = (code.charAt(8) - '0') * 3;
    a[9] = code.charAt(9) - '0';
    a[10] = (code.charAt(10) - '0') * 3;
    a[11] = code.charAt(11) - '0';
    a[12] = (code.charAt(12) - '0') * 3;
    int sum = 0;
    for(int i = 0; i < a.length; i++){
      sum = sum + a[i];
    }
    double check = (10 - (sum % 10)) % 10;
    // last is check digit
    if(check == code.charAt(13) - '0'){
      return articleNumberType;
    }
    return EANType.UNKNOWN;
  }
  
  private static String chkNull(String str, String def){
    return isNonEmpty(str) ? str.trim() : def;
  }
  
  private static boolean isNonEmpty(String str){
    return str != null && str.trim().length() > 0 && !str.trim().equalsIgnoreCase("null");
  }
  
  private static boolean isNullOrEmpty(String str){ return !isNonEmpty(str); }
  
  /**
   * The enum Ean type.
   */
  public enum EANType{
    UNKNOWN, ASIN, //Amazon Standard Identification Number
    EAN8, //European Article Number
    EAN13, //European Article Number
    GTIN, //Global Trade Item Number (previously EAN - European Article Number)
    ISBN10, //International Standard Book Number
    ISBN13, //International Standard Book Number
    SKU, //Stock keeping unit
    UPC, //Universal Product Code
    ISSN, //International Standard Serial Number
  }
  
  /**
   * The Out object.
   *
   * @param <T> the type parameter
   */
  public static final class OutObject<T>{
    
    public T argValue;
  }
  
  /**
   * The Try parse helper.
   */
  public static final class TryParseHelper{
    
    /**
     * Try parse long boolean.
     *
     * @param s      the s
     * @param result the result
     * @return the boolean
     */
    public static boolean tryParseLong(String s, OutObject<Long> result){
      try{
        result.argValue = Long.parseLong(s);
        return true;
      }
      catch(NumberFormatException e){
        return false;
      }
    }
    
    /**
     * Try parse int boolean.
     *
     * @param s      the s
     * @param result the result
     * @return the boolean
     */
    public boolean tryParseInt(String s, OutObject<Integer> result){
      try{
        result.argValue = Integer.parseInt(s);
        return true;
      }
      catch(NumberFormatException e){
        return false;
      }
    }
    
    /**
     * Try parse short boolean.
     *
     * @param s      the s
     * @param result the result
     * @return the boolean
     */
    public boolean tryParseShort(String s, OutObject<Short> result){
      try{
        result.argValue = Short.parseShort(s);
        return true;
      }
      catch(NumberFormatException e){
        return false;
      }
    }
    
    /**
     * Try parse byte boolean.
     *
     * @param s      the s
     * @param result the result
     * @return the boolean
     */
    public boolean tryParseByte(String s, OutObject<Byte> result){
      try{
        result.argValue = Byte.parseByte(s);
        return true;
      }
      catch(NumberFormatException e){
        return false;
      }
    }
    
    /**
     * Try parse double boolean.
     *
     * @param s      the s
     * @param result the result
     * @return the boolean
     */
    public boolean tryParseDouble(String s, OutObject<Double> result){
      try{
        result.argValue = Double.parseDouble(s);
        return true;
      }
      catch(NumberFormatException e){
        return false;
      }
    }
    
    /**
     * Try parse boolean boolean.
     *
     * @param s      the s
     * @param result the result
     * @return the boolean
     */
    public boolean tryParseBoolean(String s, OutObject<Boolean> result){
      try{
        result.argValue = Boolean.parseBoolean(s);
        return true;
      }
      catch(NumberFormatException e){
        return false;
      }
    }
  }
  
}

