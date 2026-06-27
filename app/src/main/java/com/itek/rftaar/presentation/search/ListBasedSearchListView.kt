package com.itek.rftaar.presentation.search

import android.content.Context
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.google.gson.Gson
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.model.ListTypeInfo
import com.itek.rftaar.mqtt.constants.SearchListTypeConstant
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.movement.BadgeText
import com.itek.rftaar.presentation.movement.clearSavedSessionValues
import com.itek.rftaar.presentation.navigation.Screen
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.RedColor
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListBasedSearchListView(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    menuCode: String,
    searchParams: Map<String, Any> = emptyMap(),
    apiViewModel: ApiViewModel = hiltViewModel(),
){
    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = if(activity!=null) activity.findReaderViewModel() else hiltViewModel()
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)
    val isProcessOn = readerViewModel.isProcessOn().observeAsState(false)
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        if (apiViewModel!=null) {
            val map = HashMap<String, String>()
            map.put(ParameterConstants.CUSTOMER_ID,DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, ""))
            map.put(ParameterConstants.BUSINESS_LINE_ID,DataStoreManager.readFromPreferences(ParameterConstants.BUSINESS_LINE_ID, ""))
            map.put(ParameterConstants.LOCATION_ID,DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, ""))
            map.put(ParameterConstants.DEVICE_ID,DataStoreManager.readFromPreferences(ParameterConstants.DEVICE_ID, ""))
            map.put(ParameterConstants.SEARCH_TYPE_NAME, SearchListTypeConstant.LIST_BASED_SEARCH)
            apiViewModel.callApi(UrlConstants.PRODUCT_SEARCH_LIST, queryMap = map)
        }
    }

    BackHandler(enabled = true) {
        scope.launch {
            if (chkTrue(isProcessOn.value) && chkTrue(isApiLoading.value) ) return@launch
            if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                navController.popBackStack()
        }
    }

    Box() {

        Scaffold(
            bottomBar = {
                ListViewBottomBar()
            }
        ){ innerPadding ->

            Box(
                modifier = modifier
                    .background(WhiteColor)
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                ListViewContent(context,label,menuCode,navController,apiViewModel)
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListViewContent(
    context: Context,
    label: String,
    menuCode: String,
    navController: NavHostController,
    apiViewModel: ApiViewModel
) {

    val decodedList = remember { mutableStateListOf<ListTypeInfo>() }

    val response = apiViewModel.apiResult.collectAsState(initial = null)
    val apiError = apiViewModel.apiErrorMsg.observeAsState("")
    LaunchedEffect(response.value) {
        LogUtils.showLog("view", "ListBasedSearchListView")
        val result = response.value ?: return@LaunchedEffect
        if (result?.isSuccess != true) {
            val message = result?.errMsg
            if (!message.isNullOrBlank()) {
                //snackbarController.show(ErrorAppSnackBarData(result.errMsg.toString()))
            }
        }
        else if (result.response != null) {
            val jsonResponse = result.response

            when (result.url) {
                UrlConstants.PRODUCT_SEARCH_LIST -> {
                  val message = ParseUtils.extractString(jsonResponse, ParameterConstants.MESSAGE,"")
                  val listSearchListType = ArrayList<ListTypeInfo>(0)
                  val responseArray = ParseUtils.extractJSONArray(jsonResponse, ParameterConstants.DATA, JSONArray())
                  if(responseArray!=null && responseArray.length()>0){
                      for (i in 0 until responseArray.length()) {
                          val obj = responseArray.getJSONObject(i)
                          if(obj==null) continue
                          val listTypeObj = Gson().fromJson(obj.toString(), ListTypeInfo::class.java)
                          if(listTypeObj!=null && listTypeObj.isValid()) listSearchListType.add(listTypeObj)
                      }
                      if(listSearchListType.isNotEmpty()){
                          decodedList.clear()
                          decodedList.addAll(listSearchListType)
                      }
                      else{
                        //TODO give custom error
                      }
                  }
                  else {
                      //TODO give custom error
                  }
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier, verticalArrangement = Arrangement.Top) {
                TopBarContent(
                    label,
                    onBackClickL = {
                        navController.popBackStack()
                    }, onSettingClick = {

                    },
                    isSetting = false
                )
            }
            HorizontalDivider(thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault)

            Column(modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.dp_16)), horizontalAlignment = Alignment.Start) {

                if (decodedList.isEmpty()) {
                    Text(
                        text = chkNull(apiError.value,""),//stringResource(R.string.err_no_data)),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                        style = CommonTypography.current.noteText,
                        color = RedColor
                    )

                } else {
                    LazyColumn {
                        items(decodedList) { item ->
                            SearchListItem(
                                item = item,
                                onClick = {
                                    //Changes for existing list from db
                                    CoroutineScope(Dispatchers.IO).launch {
                                        val topic = TopicConstants.SEARCH_LIST_UPDATE
                                        val transactionType = item.childSearchTypeName
                                        clearSavedSessionValues(
                                            context,
                                            menuCode,
                                            transactionType,
                                            false,
                                            topic
                                        )
                                        AppDatabase.getDbInstance(context).tagInfoDao().deleteActual(topic, menuCode, transactionType)
                                        AppDatabase.getDbInstance(context).productZoneDataDao().deleteAll(topic, menuCode, transactionType)
                                    }

                                    val selectionLabel = item.getDisplayName()
                                    val params = mapOf(
                                        "originalTitle" to selectionLabel,
                                        item::class.java.simpleName to item,
                                    )
                                    navController.navigate(
                                        Screen.ListBasedSearch.createRoute(
                                            label = selectionLabel,
                                            code = menuCode,
                                            params = params
                                        )
                                    )
                                }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                                color = OutlineDefault,
                                thickness = 1.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchListItem(item: ListTypeInfo, onClick: () -> Unit) {

    Column {

        Text(
            text = item.getDisplayLabel(), style = CommonTypography.current.textSemiBold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = item.getDisplayDate(),
                style = CommonTypography.current.smallTxt,
                modifier = Modifier.weight(1f) // ✅ FIXED weight
            )

            Spacer(modifier = Modifier.size(4.dp))

            Column(modifier = Modifier.background(color = BackGround , shape = RoundedCornerShape(24.dp))) {
                Text(
                    text = stringResource(R.string.txt_qty,item.qty.toString()),
                    style = CommonTypography.current.noteText.copy(TextSubtext),
                    textAlign = TextAlign.End,
                    modifier = Modifier.padding(vertical = 2.dp , horizontal = 10.dp)
                )
            }

            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_8)))
            Icon(
                painter = painterResource(R.drawable.icon__next), contentDescription = null
            )
        }
    }
}

@Composable
fun ListViewBottomBar() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WhiteColor)
    ) {
        HorizontalDivider(color = OutlineDefault)

        Row(
            modifier = Modifier.padding(top =dimensionResource(R.dimen.dp_16)),
            horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_10)),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItekFooter()
        }
    }
}


@Composable
fun ListItem(item: ListTypeInfo, onClick: () -> Unit) {

    Column {

        Text(
            text = item.getSafeReferenceNumber(), style = CommonTypography.current.textSemiBold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    onClick()
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = stringResource(id = R.string.quantity)+ item.getSafeQty().toString(),
                style = CommonTypography.current.smallTxt,
                modifier = Modifier.weight(1f) // ✅ FIXED weight
            )

            Spacer(modifier = Modifier.size(4.dp))

            BadgeText(item.bucket)
            /*Text(
                text = item.bucket,
                style = CommonTypography.current.noteText.copy(TextSubtext),
                textAlign = TextAlign.End
            )*/

            Icon(
                painter = painterResource(R.drawable.icon__next), contentDescription = null
            )
        }
    }
}