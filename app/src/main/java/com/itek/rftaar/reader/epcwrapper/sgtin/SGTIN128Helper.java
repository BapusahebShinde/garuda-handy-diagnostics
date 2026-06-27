package com.itek.rftaar.reader.epcwrapper.sgtin;

import java.util.HashMap;
import java.util.Map;

import keonn.customepcapis.Infotek4EpcApi;

/**
 * The Sgtin 128 helper.
 * used for Operations for Non-Standard EAN
 * or EPC with length > 24
 */
public class SGTIN128Helper{
  
  //if Infotek4EpcApi class is used
  public static final boolean isAllowAlphanumericNonStdEan = true;
  //public static final String DIGITS_NUMERIC_EAN ="0123456789";
  //public static final String DIGITS_ALPHA_NUMERIC_EAN ="0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ!@#$%^&*";
  
  /**
   * Instantiates a new Sgtin 128 helper.
   */
  private SGTIN128Helper(){/*Empty constructor*/}
  //if InfotekEpcApi class is used
  
  /**
   * Get barcode from epc string.
   *
   * @param epc the epc
   * @return the string
   */
  public static String getBarcodeFromEpc(String epc){ return getBarcodeFromEpc(epc, true); }
  
  public static String getBarcodeFromEpc(String epc, boolean isAllowAlphanumericNonStdEan){
    
    String barcode = "";
    final Infotek4EpcApi epcapi = new Infotek4EpcApi();
    //final InfotekEpcApi epcapi = new InfotekEpcApi();
    //final InfotekEpc epcapi = new InfotekEpc(isDebugApp?SGTIN128Helper.isAllowAlphanumericNonStdEan:isAllowAlphanumericNonStdEan);
    //final InfotekEpc epcapi = new InfotekEpc(getAlphaNumericStr(isAllowAlphanumericNonStdEan));
    try{
      Map<String, Object> decoded = epcapi.decode(epc);
      barcode = (String) decoded.get("ean");
      
    }
    catch(Exception ee){
      ee.printStackTrace();
      barcode = "";
      //AppCommonMethods.showLog("EXCEPTION", ee.getMessage(), true);
    }
    return barcode;
  }
  
  public static Map<String, Object> getBarcodeAndSerialFromEpc(final String epc){ return getBarcodeAndSerialFromEpc(epc, false); }
  
  public static Map<String, Object> getBarcodeAndSerialFromEpc(final String epc, boolean isAllowAlphanumericNonStdEan){
    final Infotek4EpcApi epcapi = new Infotek4EpcApi();
    //final InfotekEpcApi epcapi = new InfotekEpcApi();
    
    //customized
    //final InfotekEpc epcapi = new InfotekEpc(getAlphaNumericStr(isAllowAlphanumericNonStdEan));
    //final InfotekEpc epcapi = new InfotekEpc(isDebugApp?SGTIN128Helper.isAllowAlphanumericNonStdEan:isAllowAlphanumericNonStdEan);
    try{
      return epcapi.decode(epc);
    }
    catch(Exception ee){
      ee.printStackTrace();
      //AppCommonMethods.showLog("EXCEPTION", ee.getMessage(), true);
    }
    return null;
  }
  
  /**
   * Get sgtin from barcode and srno string.
   *
   * @param barcode      the barcode
   * @param serialnumber the serialnumber
   * @return the string
   */
  public static String getSgtinFromBarcodeAndSrno(String barcode, String serialnumber){ return getSgtinFromBarcodeAndSrno(barcode, serialnumber, true); }
  
  public static String getSgtinFromBarcodeAndSrno(String barcode, String serialnumber, boolean isAllowAlphanumericNonStdEan){
    barcode = barcode.toUpperCase();
    String epc = "";
    try{
      //final InfotekEpcApi epcapi = new InfotekEpcApi();
      final Infotek4EpcApi epcapi = new Infotek4EpcApi();
      
      //customized
      //final InfotekEpc epcapi = new InfotekEpc(getAlphaNumericStr(isAllowAlphanumericNonStdEan));
      Map<String, Object> m = new HashMap<String, Object>();
      m.put("ean", barcode);
      m.put("serial", serialnumber);
      m.put("eas", "true");
      epc = epcapi.encode(m);
    }
    catch(Exception e){
      epc = "";
    }
    return epc;
  }
}
