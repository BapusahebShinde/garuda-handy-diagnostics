package com.itek.rftaar.core.common.utils

import android.app.Application
import org.acra.ACRA
import org.acra.BuildConfig
import org.acra.config.CoreConfigurationBuilder
import org.acra.config.MailSenderConfigurationBuilder

object CrashUtils {
  //private lateinit var context: Application

  fun init(context: Application) {
    //this.context = context
    setup(context)
  }

  fun setup(context: Application) {
   if(BaseUtils.isDebuggable())
    ACRA.init(
      context, CoreConfigurationBuilder()
        .withBuildConfigClass(BuildConfig::class.java)
        .withReportFormat(org.acra.data.StringFormat.JSON) // JSON format is recommended
        .withPluginConfigurations( // MailSender configuration:
          MailSenderConfigurationBuilder()
            .withMailTo("bhupen.morgaonkar@infoteksoftware.com") // Required: the destination email address
            .withSubject("App Crash Report") // Optional: email subject
            .withBody("Please find the crash report attached.") // Optional: email body
            .withReportAsFile(true) // Optional: send report as an attachment
            .withReportFileName("Crash.txt") // Optional: attachment file name
            .build()
        )
    )
    else ACRA.init(
       context,
       CoreConfigurationBuilder()
           .withBuildConfigClass(BuildConfig::class.java)
           .withReportFormat(org.acra.data.StringFormat.JSON)
   )
  }
}