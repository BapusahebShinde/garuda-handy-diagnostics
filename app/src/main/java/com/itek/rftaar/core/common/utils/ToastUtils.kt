package com.itek.rftaar.core.common.utils

import android.content.Context
import android.widget.Toast
import androidx.core.text.HtmlCompat
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.isNonEmpty

object ToastUtils {

  private lateinit var context: Context

  fun init(context: Context) {
    this.context = context
  }

  /**
   * Show short toast.
   * @param res     the res
   */
  fun showShortToast(res: Int) {
    if(context!=null && res!=0) showToast(context.getString(res), false)
  }

  /**
   * Show long toast.
   * @param res     the res
   */
  fun showLongToast(res: Int) {
    if(context!=null && res!=0) showToast(context.getString(res), true)
  }

  /**
   * Show short toast.
   * @param msg     the msg
   */
  fun showShortToast(msg: String?) {
    showToast(msg, false)
  }

  /**
   * Show long toast.
   * @param msg     the msg
   */
  fun showLongToast(msg: String?) {
    showToast(msg, true)
  }

  /**
   * Show toast.
   * @param msg         the msg
   * @param isLongToast the is long toast
   */
  private fun showToast(msg1: String?, isLongToast: Boolean) {
    val msg = chkNull(msg1,"")
    if (context != null && isNonEmpty(msg)) {
        Toast.makeText(context,
          if (msg.contains("<") && msg.contains(">"))
            HtmlCompat.fromHtml(msg, HtmlCompat.FROM_HTML_MODE_LEGACY)
          else msg,
          if (isLongToast) Toast.LENGTH_LONG else Toast.LENGTH_SHORT
        ).show()
    }
  }
}