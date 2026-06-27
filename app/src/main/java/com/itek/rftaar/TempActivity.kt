package com.itek.rftaar

import android.os.Bundle
import com.itek.rftaar.databinding.ActivityTempBinding

class TempActivity : ReaderActivity() {

  private lateinit var binding: ActivityTempBinding

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    binding = ActivityTempBinding.inflate(layoutInflater)
    setContentView(binding.root)

    binding.scan.setOnClickListener({view ->
      //toggleInventory()
      //performPick()
      //performPick(true)
      //startScan()
      performPick()
    })
  }

  override fun onTriggerPressed(){
    binding.scan.performClick()
  }


}