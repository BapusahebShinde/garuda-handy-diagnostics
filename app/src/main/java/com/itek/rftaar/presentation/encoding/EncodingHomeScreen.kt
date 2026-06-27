package com.itek.rftaar.presentation.encoding

import android.annotation.SuppressLint
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.constants.LoginConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.entity.MenuEntity
import com.itek.rftaar.data.model.ActiveSessionQty
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.AppSnackBar
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.ErrorAppSnackBarData
import com.itek.rftaar.presentation.commonComp.GenericIconCard
import com.itek.rftaar.presentation.commonComp.InfoActionRow
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.MessageBottomSheet
import com.itek.rftaar.presentation.commonComp.MessageUiState
import com.itek.rftaar.presentation.commonComp.SessionUiActions
import com.itek.rftaar.presentation.commonComp.SnackbarController
import com.itek.rftaar.presentation.commonComp.StatItem
import com.itek.rftaar.presentation.commonComp.StatsCard
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.dashBoard.navigateMenu
import com.itek.rftaar.presentation.inventory.InventoryMenuCycle
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.ui.theme.Yellow
import com.itek.rftaar.utils.CommonUtils.chkTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EncodingHomeScreen(
    modifier: Modifier,
    navController: NavHostController,
    label: String,
    code: String,
    apiViewModel: ApiViewModel = hiltViewModel(),
    searchParams: Map<String, Any> = emptyMap()
) {

  LaunchedEffect(Unit) {
    val map = HashMap<String, String>()
    map.put(
      ParameterConstants.CUSTOMER_ID,
      DataStoreManager.readFromPreferences(ParameterConstants.CUSTOMER_ID, "")
    )
    map.put(
      ParameterConstants.OPERATION_LOCATION_ID,
      DataStoreManager.readFromPreferences(LoginConstants.DEVICE_LOCATION_ID, "")
    )
    apiViewModel.callApi(UrlConstants.GET_ENCODE_COUNT, queryMap = map)
  }

  BackHandler(enabled = true) {
    if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
      navController.popBackStack()
  }
  Scaffold(
    bottomBar = {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(BackGround),
        contentAlignment = Alignment.BottomCenter
      ) {
        EncodingBottomBar()
      }
    },
    modifier = modifier.background(BackGround)
  ) { innerPadding ->

    Box(
      modifier = Modifier
        .background(BackGround)
        .padding(innerPadding)
        .fillMaxSize()
    ) {
      EncodingScreenContent(navController, code, label, apiViewModel)
    }
  }
}

@RequiresApi(Build.VERSION_CODES.O)
@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun EncodingScreenContent(
  navController: NavHostController,
  code: String,
  label: String,
  apiViewModel: ApiViewModel
) {

  val cardWidth = (LocalConfiguration.current.screenWidthDp.dp - 64.dp) / 3
  val context = LocalContext.current
  val db = AppDatabase.getDbInstance(context)
  val subMenus = db.menuDao().getSubMenus(code).collectAsState(initial = emptyList())
  val activeSession = db.tagInfoDao().getTopicWiseActiveSessionList(topic = TopicConstants.ENCODE).collectAsState(initial = emptyList())
  LogUtils.showLog("activeSession", "EncodingScreenContent: ${activeSession.value} ")
  val scope = rememberCoroutineScope()
  val sheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = true,
    confirmValueChange = { sheetValue ->
      sheetValue != SheetValue.Hidden
    }
  )
  val showSheet = remember { mutableStateOf(false) }
  val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
  val activity = context as? ReaderActivity
  val readerViewModel = activity?.findReaderViewModel() ?: hiltViewModel()
  val selectedSession = remember { mutableStateOf<ActiveSessionQty?>(null) }

  val snackbarController = remember { SnackbarController() }
  val encodeCount = remember {mutableStateOf(0)}
  val sessionCount = remember {mutableStateOf(0)}
  val avgTime = remember {mutableStateOf(0.0)}

  val selectedMenu = remember { mutableStateOf<MenuEntity?>(null) }


    LaunchedEffect(currentSheet.value) {
        if (currentSheet.value == BottomSheetType.SUB_MENUS) {
            sheetState.expand()
        }
    }

  val response = apiViewModel.apiResult.collectAsState(initial = null)
  LaunchedEffect(response.value) {
    if(response.value==null) return@LaunchedEffect
    val result = response.value
    if (result?.isSuccess != true) {
      val message = result?.errMsg
      if (!message.isNullOrBlank()) {
        snackbarController.show(
          ErrorAppSnackBarData(result.errMsg.toString())
        )
      }
    } else if (result.response != null) {
      val jsonResponse = result.response

      when (result.url) {
        UrlConstants.GET_ENCODE_COUNT -> {
          val data = ParseUtils.extractJSONObject(jsonResponse, ParameterConstants.DATA, JSONObject())
          encodeCount.value = ParseUtils.extractInt(data,"encodeCount",0)
          sessionCount.value = ParseUtils.extractInt(data,"sessionCount",0)
          avgTime.value = ParseUtils.extractDouble(data,"avgTime",0.0)
        }
      }
    }
  }

    val avgTimeInSeconds = String.format("%.2f", avgTime.value / 1000)

  Box {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
        .padding(dimensionResource(id = R.dimen.dp_16))
        .clickable(
          onClick = {},
          indication = null,
          interactionSource = remember { MutableInteractionSource() }
        ),
      horizontalAlignment = Alignment.Start,
      verticalArrangement = Arrangement.Top
    )
    {
      Column(modifier = Modifier.fillMaxWidth()) {
        CircularIcon(
          iconRes = R.drawable.property_arrow_back,
          modifier = Modifier.clickable(
            onClick = {
              if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
                navController.popBackStack()
            },
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
          )

        )
      }

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(
            top = dimensionResource(R.dimen.dp_10),
            bottom = dimensionResource(R.dimen.dp_16)
          ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {

        Text(
          text = label,
          style = CommonTypography.current.headingH1
        )
      }

      StatsCard(
        items = listOf(
          StatItem(
            value = encodeCount.value.toString(),
            label = stringResource(R.string.encode_summary)
          ),
          StatItem(
            value = sessionCount.value.toString(),
            label = stringResource(R.string.session_count)
          ),
          StatItem(
            value = avgTimeInSeconds,
            label = stringResource(R.string.average_time_tag),
            valueColor = Yellow
          )
        )
      )

      Spacer(modifier = Modifier.padding(top = dimensionResource(R.dimen.dp_16)))


      FlowRow(
        modifier = Modifier
          .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        maxItemsInEachRow = 3
      ) {

        subMenus.value.forEach { menu ->
          val activeMenuSession = db.tagInfoDao().hasActiveMenuSession(menuCode = menu.code).observeAsState(initial = false)
          val subMenus = db.menuDao().getSubMenus(menu.code).collectAsState(initial = emptyList())
          GenericIconCard(
              menu = menu,
              modifier = Modifier.clickable(
                enabled = menu.isActive && menu.isEnabled,
                onClick = {
                  if(!menu.isActive || !menu.isEnabled) return@clickable
                  if (activeMenuSession.value) {
                    selectedSession.value = activeSession.value
                      .firstOrNull { it.menuCode == menu.code }

                    openSheet(
                      scope = scope,
                      sheetState = sheetState,
                      showSheet = showSheet,
                      currentSheet = currentSheet,
                      sheet = BottomSheetType.MESSAGE
                    )
                  }
                  else {
                      if (subMenus.value.size > 0 && !menu.hasDashboard) {
                          if (subMenus.value.size > 1) {
                              selectedMenu.value = menu
                              openSheet(
                                  scope = scope,
                                  sheetState = sheetState,
                                  showSheet = showSheet,
                                  currentSheet = currentSheet,
                                  sheet = BottomSheetType.SUB_MENUS
                              )
                          }
                          else if (subMenus.value.size == 1) {
                              val subMenu = subMenus.value.get(0)
                              navigateMenu(navController, subMenu)
                          }
                          return@clickable
                      }
                      else navigateMenu(navController,menu)
                  }
                },
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
              ),
              showRedDot = activeMenuSession.value,
          )
        }
      }

      Spacer(modifier = Modifier.padding(top = dimensionResource(R.dimen.dp_16)))

      if(activeSession.value.size>0) {
        Text(
          text = String.format(stringResource(R.string.active_session), activeSession.value.size),
          style = CommonTypography.current.textMedium,
          color = BlackColor,
          fontSize = dimensionResource(R.dimen.sp_16).value.sp
        )

        activeSession.value.forEach { session ->
          val preHeader = TopicConstants.ENCODE + "_" + session.menuCode + "_"
          val limit = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.LIMIT, 0)
          InfoActionRow(
            title = session.menuName,
            subtitle = stringResource(
              id = R.string.item_encoded,
              session.scanCount.toString(),
              if (limit > 0) "/$limit" else ""
            ),
            icon = painterResource(id = R.drawable.property_power),
            actionText = stringResource(id = R.string.continue_),
            onActionClick = {
              selectedSession.value = session
              openSheet(
                scope = scope,
                sheetState = sheetState,
                showSheet = showSheet,
                currentSheet = currentSheet,
                sheet = BottomSheetType.MESSAGE
              )
            },
            isIcon = false
          )
          HorizontalDivider(
            color = OutlineDefault,
            modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_10))
          )
        }
      }
    }

    if (showSheet.value) {
      ModalBottomSheet(
        sheetState = sheetState,
        onDismissRequest = {

        },
        dragHandle = { },
        modifier = Modifier
          .fillMaxWidth()
          .background(Color.Transparent)
      ) {
        when (currentSheet.value) {

          BottomSheetType.SUB_MENUS -> Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .background(WhiteColor)
                    .imePadding()
                    .navigationBarsPadding()
            ) {
            InventoryMenuCycle(
             showSheet = showSheet,
             sheetState = sheetState,
             scope = scope,
             menu = selectedMenu.value!!,
             navController = navController,
             //activeSessionList = activeSessionList
            )
          }

          BottomSheetType.MESSAGE ->
            Box(
              modifier = Modifier
                .background(WhiteColor)
                .fillMaxWidth()
                .wrapContentHeight()
            ) {
              SessionAlert(
                showSheet = showSheet,
                sheetState = sheetState,
                scope = scope,
                currentSheet = currentSheet,
                selectedSession = selectedSession.value,
                navController = navController,
              )
            }

          BottomSheetType.SESSION -> {
            selectedSession.value?.let { session ->
              val preHeader = TopicConstants.ENCODE + "_" + session.menuCode + "_"
              val limit = DataStoreManager.readFromPreferences(preHeader + ParameterConstants.LIMIT, 0)
              val tagCount = session.scanCount
              val subHeading = if (tagCount > 0) {
                val countText = if (limit > 0) "${tagCount}/${limit}" else "${tagCount}"
                buildAnnotatedString {
                  append(stringResource(R.string.encoded_end_the_session))
                  withStyle(
                    style = SpanStyle(fontWeight = FontWeight.Bold)
                  ) {
                    append(" $countText ")
                  }

                  append(stringResource(R.string.please_confirm))
                }
              } else {
                AnnotatedString(stringResource(R.string.encoded_end_the_session_0))
              }
              SessionContent(
                showSheet = showSheet,
                sheetState = sheetState,
                scope = scope,
                navController = navController,
                tagCount = tagCount,
                subHeading = subHeading,
                onStopSession = {
                  if (chkTrue(readerViewModel.isProcessOn().value)) return@SessionContent

                  CoroutineScope(Dispatchers.IO).launch {
                    val preHeader = session.topic + "_" + session.menuCode + "_"
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.ID, "")
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.DEVICE_SESSION_ID, "")
                    DataStoreManager.saveToPreferences(preHeader + ParameterConstants.LIMIT, 0)
                    db.tagInfoDao().deleteBySessionTypeAndTransactionType(session.menuCode, session.transactionType)
                  }

                  scope.launch {
                    readerViewModel.clearSessionAndTransactionType()
                    sheetState.hide()
                    showSheet.value = false
                    currentSheet.value = BottomSheetType.NONE
                    selectedSession.value = null
                  }
                },
                readerViewModel = null,
                isAllowContinue = false
                //limit = limit
              )
            }
          }

          else -> {
            currentSheet.value = BottomSheetType.NONE
          }
        }
      }
    }

    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      AppSnackBar(snackbarController)
    }

  }
}

@Composable
fun EncodingBottomBar() {
  ItekFooter()
}

/**fun getMenuIcon(code: String): Int {
  return when (code) {
    "CHILD_MENU_NORMAL_ENCODE" -> R.drawable.start_encoding
    "CHILD_MENU_BULK_ENCODE" -> R.drawable.multi_encoding
    "CHILD_MENU_VERIFY_ENCODE" -> R.drawable.verify_encoding
    "CHILD_MENU_SCAN_SCAN_ENCODE" -> R.drawable.scan_encoding
    else -> R.drawable.property_info
  }
}*/

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionAlert(
  showSheet: MutableState<Boolean>,
  sheetState: SheetState,
  scope: CoroutineScope,
  currentSheet: MutableState<BottomSheetType>,
  selectedSession: ActiveSessionQty?,
  navController: NavHostController,
) {

  val formatedText = buildAnnotatedString {
    append(stringResource(R.string.alert_encoding_session, selectedSession?.menuName ?: ""))
  }

  val uiState = MessageUiState(
    title = stringResource(R.string.encoding_alert),
    heading = stringResource(R.string.session_in_progress),
    subHeading = formatedText,
    primaryButtonText = stringResource(R.string.stop_session_),
    secondaryButtonText = stringResource(R.string.continue_),
    icon = R.drawable.property_know_more,
    color1 = Color(0xFFF3B100),
    color2 = Color(0xFFFECF53)
  )

  val actions = SessionUiActions(
    onClose = {
      scope.launch {
        sheetState.hide()
        showSheet.value = false
      }
    }, onSecondaryAction = {
      scope.launch {
          if(selectedSession!=null){
              navigateMenu(navController,selectedSession)
        /*when (selectedSession?.menuCode) {
          "CHILD_MENU_NORMAL_ENCODE",
          "CHILD_MENU_BULK_ENCODE",
          "CHILD_MENU_VERIFY_ENCODE",
          "CHILD_MENU_SCAN_SCAN_ENCODE" -> {*/

            // 1. Create the screen object
            /*val dashboard = Screen.DynamicDashboard(
              code = selectedSession.menuCode,
              label = selectedSession.menuName,
              params = emptyMap()
            )

            // 2. Navigate using the helper that builds the 4-segment URL
            val route = dashboard.createRoute(selectedSession.parentCode)
            navController.navigate(route)*/
          //}
        }

      }
    }, onPrimaryAction = {
      scope.launch {
        currentSheet.value = BottomSheetType.SESSION
      }
    }
  )

  MessageBottomSheet(
    uiState = uiState, actions = actions
  )
}

/*@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EncodeEndSessionAlert(
  showSheet: MutableState<Boolean>,
  sheetState: SheetState,
  scope: CoroutineScope,
  navController: NavHostController,
  tagCount: Int,
  onStopSession: () -> Unit,
  limit: Int
) {

  val subHeading = if (tagCount > 0) {

    val countText =
      if (limit > 0) "${tagCount}/${limit}"
      else "${tagCount}"

    buildAnnotatedString {

      append(stringResource(R.string.encoded_end_the_session))

      withStyle(
        style = SpanStyle(fontWeight = FontWeight.Bold)
      ) {
        append(" $countText ")
      }

      append(stringResource(R.string.please_confirm))
    }

  } else {
    AnnotatedString(stringResource(R.string.encoded_end_the_session_0))
  }
  val uiState = SessionUiState(
    title = stringResource(R.string.stop_session),
    heading = stringResource(R.string.stop_current_session),
    subHeading = subHeading,
    primaryButtonText = stringResource(R.string.stop_session_),
    secondaryButtonText = stringResource(R.string.continue_later)
  )

  val actions = SessionUiActions(
    onClose = {
      scope.launch {
        sheetState.hide()
        showSheet.value = false
      }
    }, onSecondaryAction = {}, onPrimaryAction = onStopSession
  )

  SessionBottomSheet(
    uiState = uiState, actions = actions, isSecondaryButton = false
  )
}*/
