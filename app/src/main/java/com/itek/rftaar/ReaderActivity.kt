package com.itek.rftaar

import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import com.itek.rftaar.data.entity.TagInfoEntity
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import kotlin.random.Random


open class ReaderActivity : CommonActivity() {

  private val readerViewModel: ReaderViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    readerViewModel.init(this)
    //setObservers()
  }

  fun setObservers(isRemoveOnly: Boolean = false) {

    //Barcode
    readerViewModel.isBarcodeOn().removeObservers(this)
    if (!isRemoveOnly) readerViewModel.isBarcodeOn().observe(this, Observer { isOn ->
      showLog("isBarcodeOn", "" + isOn)
    })
    readerViewModel.barcodeData().removeObservers(this)
    if (!isRemoveOnly) readerViewModel.barcodeData().observe(this, Observer { data ->
      showLog("barcodeData", data)
    })

    //Trigger
    readerViewModel.isTriggerPressed().removeObservers(this)
    if (!isRemoveOnly) readerViewModel.isTriggerPressed().observe(this, { isPressed ->
      showLog("isTriggerPressed", "" + isPressed)
      onTriggerPressed()
    })

    //Error
    readerViewModel.error().removeObservers(this)
    if(!isRemoveOnly) readerViewModel.error().observe(this, Observer { data ->
      showLog("error", "" + data)
    })

    //Pick
    readerViewModel.isPickOn().removeObservers(this)
    if(!isRemoveOnly) readerViewModel.isPickOn().observe(this, Observer { isOn ->
      showLog("isPickOn", "" + isOn)
    })
    readerViewModel.pickData().removeObservers(this)
    if(!isRemoveOnly) readerViewModel.pickData().observe(this, Observer { data ->
      showLog("pickData", "" + data.toString())
      val passwords = ArrayList<String>(0)
      passwords.add("00000000")
      passwords.add("12345678")
      passwords.add("88888888")
      if (data is TagInfoEntity) {
        data.newEpc = "30361E73F44D6A80000000"+ Random(0).nextInt(90).toString()
        readerViewModel.performEncoding(data,"",passwords)
      }
    })

    //Inventory
    readerViewModel.isInventoryOn().removeObservers(this)
    if(!isRemoveOnly) readerViewModel.isInventoryOn().observe(this, Observer { isOn ->
      showLog("isInventoryOn", "" + isOn)
    })

    //Tag Verify
    readerViewModel.isTagVerifyOn().removeObservers(this)
    if(!isRemoveOnly) readerViewModel.isTagVerifyOn().observe(this, Observer { isOn ->
      showLog("isTagVerifyOn", "" + isOn)
    })

    //TagWrite
    readerViewModel.isTagWriteOn().removeObservers(this)
    if(!isRemoveOnly) readerViewModel.isTagWriteOn().observe(this, Observer { isOn ->
      showLog("isTagWriteOn", "" + isOn)
    })
    readerViewModel.isTagWriteDone().removeObservers(this)
    if(!isRemoveOnly) readerViewModel.isTagWriteDone().observe(this, Observer { isDone ->
      showLog("isTagWriteDone", "" + isDone)
    })

  }

  fun findReaderViewModel():ReaderViewModel{
    return readerViewModel
  }

  open fun onTriggerPressed() {

  }

  override fun onResume() {
    super.onResume()
    readerViewModel.onResume()
  }

  override fun onPause() {
    readerViewModel.onPause()
    super.onPause()
  }

  override fun onStart() {
    super.onStart()
  }

  override fun onStop() {
    super.onStop()
  }

  public fun startScan() {
    readerViewModel.scanBarcode("")
  }

  public fun performInventory() {
    readerViewModel.performInventory()
  }

  public fun performTagVerify() {
    readerViewModel.performTagVerify()
  }

  public fun performPick(isDecodeOnPick: Boolean = false) {
    readerViewModel.performPick(isDecodeOnPick = isDecodeOnPick)
  }

  public fun performDecoding(tagInfoEntity: TagInfoEntity) {
    readerViewModel.performDecoding(tagInfoEntity)
  }

  public fun toggleInventory() {
    readerViewModel.toggleInventory()
  }

  override fun finish() {
    //setObservers(true)
    readerViewModel.onDestroy()
    super.finish()
  }



  override fun onDestroy() {
    //setObservers(true)
    readerViewModel.onDestroy()
    super.onDestroy()
  }
}