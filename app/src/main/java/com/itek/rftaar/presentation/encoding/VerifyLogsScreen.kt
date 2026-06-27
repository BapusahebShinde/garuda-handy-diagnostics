package com.itek.rftaar.presentation.encoding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.mqtt.constants.SearchTypeConstant
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.GroupEanToggle
import com.itek.rftaar.presentation.commonComp.ImageViewFullScreen
import com.itek.rftaar.presentation.commonComp.MessageComponent
import com.itek.rftaar.presentation.commonComp.RowViewWithHSI
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.decoding.ProductDetailsByEan
import com.itek.rftaar.presentation.decoding.handleProductDetails
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.WhiteColor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

enum class VerifyResult {
    NONE,
    SUCCESS,
    PENDING
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyLogsScreen(
    modifier: Modifier,
    navController: NavHostController,
    searchParams: Map<String, Any> = emptyMap(),
    menuCode: String,
    label: String,
    apiViewModel: ApiViewModel = hiltViewModel()
) {
    val transactionType = searchParams["transactionType"] as? String ?: ""
    val tagCount = searchParams["tagCount"] as? String ?: ""
    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = if (activity != null) activity.findReaderViewModel() else hiltViewModel()

    LaunchedEffect(Unit) {
        if (activity != null) {
            readerViewModel.onCreate()
            readerViewModel.setSessionAndTransactionType(menuCode)
        }
    }
    val db = AppDatabase.getDbInstance(context)
    val tagEncodedCount = db.tagInfoDao().getTagWriteCount(menuCode, transactionType).observeAsState(initial = 0)
    LogUtils.showLog("tagEncodedCount", "SingleEncodingContent: $tagEncodedCount")
    val tagVerifiedCount = db.tagInfoDao().getTagWriteVerifiedCount(menuCode, transactionType).observeAsState(initial = 0)
    LogUtils.showLog("tagVerifiedCount", "SingleEncodingContent: $tagVerifiedCount")
    val searchQuery = remember { mutableStateOf("") }
    val eanList = db.tagInfoDao().getEncodedTagTime(menuCode, transactionType, "").collectAsState(initial = emptyList())
    val selectedEan = remember {mutableStateOf<TagTime?>(null)}
    val selectedSearchType = remember{ mutableStateOf("")}
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val verifyResult = remember { mutableStateOf(VerifyResult.NONE) }
    val isVerifyingTags = readerViewModel.isTagVerifyOn().observeAsState(false)
    //val verificationSession = remember { mutableIntStateOf(0) }
    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)
    LogUtils.showLog("triggerPressed", "VerifyLogsContent: ${triggerPressed.value}")





    /*  LaunchedEffect(tagVerifiedCount.value) {
          LogUtils.showLog("tagVerifyCount",tagVerifiedCount.value.toString())
          if (tagEncodedCount.value > 0 && tagVerifiedCount.value > 0 && tagVerifiedCount.value == tagEncodedCount.value) {
              readerViewModel.stopOperations()
              openSheet(
                  scope = scope,
                  sheetState = sheetState,
                  showSheet = showSheet,
                  currentSheet = currentSheet,
                  sheet = BottomSheetType.MESSAGE
              )
          }
      }*/



    LaunchedEffect(
        tagVerifiedCount.value,
        isVerifyingTags.value
    ) {
        if (
            isVerifyingTags.value &&
            tagVerifiedCount.value == tagEncodedCount.value &&
            verifyResult.value == VerifyResult.NONE
        ) {
            readerViewModel.stopOperations()
            verifyResult.value = VerifyResult.SUCCESS
        }
        else if(!isVerifyingTags.value && tagVerifiedCount.value<tagEncodedCount.value &&
            verifyResult.value == VerifyResult.NONE)
            verifyResult.value == VerifyResult.PENDING
    }



    LaunchedEffect(verifyResult.value) {
        when (verifyResult.value) {

            VerifyResult.SUCCESS -> {
                openSheet(
                    scope = scope,
                    sheetState = sheetState,
                    showSheet = showSheet,
                    currentSheet = currentSheet,
                    sheet = BottomSheetType.MESSAGE
                )
            }

            VerifyResult.PENDING -> {
                openSheet(
                    scope = scope,
                    sheetState = sheetState,
                    showSheet = showSheet,
                    currentSheet = currentSheet,
                    sheet = BottomSheetType.NOTE
                )
            }

            VerifyResult.NONE -> Unit
        }
    }


    BackHandler(enabled = true) {
        if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
            readerViewModel.clearSessionAndTransactionType()
            navController.popBackStack()
        }
    }
    Box() {
        Scaffold(
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BackGround),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    VerifyLogsBottomBar(
                        readerViewModel,
                        tagVerifiedCount,
                        tagEncodedCount,
                        scope,
                        isVerifyingTags,
                    )
                }
            },
            modifier = modifier.background(BackGround)
        ) { innerPadding ->

            Box(
                modifier = Modifier
                    .background(brush = Brush.verticalGradient(listOf(Color(0xFFF3F3F3), Color(0xFFFFFFFF), Color(0xFFFFFFFF), Color(0xFFFFFFFF))))
                    .padding(innerPadding)
                    .fillMaxSize()
            ) {
                VerifyLogsContent(
                    navController,
                    menuCode,
                    readerViewModel,
                    transactionType,
                    tagCount,
                    tagVerifiedCount,
                    tagEncodedCount,
                    isVerifyingTags,
                    verifyResult,
                    triggerPressed,
                    scope,
                    sheetState,
                    showSheet,
                    currentSheet,
                    eanList,
                    searchQuery,
                    selectedSearchType,
                    selectedEan,
                    apiViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyLogsContent(
    navController: NavHostController,
    menuCode: String,
    readerViewModel: ReaderViewModel,
    transactionType: String,
    tagCount: String,
    tagVerifiedCount: State<Int>,
    tagEncodedCount: State<Int>,
    isVerifyingTags: State<Boolean>,
    verifyResult: MutableState<VerifyResult>,
    triggerPressed: State<Boolean>,
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    currentSheet: MutableState<BottomSheetType>,
    eanList: State<List<TagTime>>,
    searchQuery: MutableState<String>,
    selectedSearchType: MutableState<String>,
    selectedEan: MutableState<TagTime?>,
    apiViewModel: ApiViewModel
) {

    val response = apiViewModel.apiResult.collectAsState(null)
    val infoList = remember { mutableStateListOf<Pair<String, String>>() }
    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }

    LaunchedEffect(triggerPressed.value) {
        if(!triggerPressed.value) return@LaunchedEffect
        readerViewModel.setTriggerValue(false)
        if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
        if (triggerPressed.value) {
            //write condition for search dialog
            if(showSheet.value!=false && currentSheet.value == BottomSheetType.SEARCH){
              //Based on Selected Epc or TID
              if(selectedSearchType.value.isNotEmpty() && selectedEan.value!=null)
               readerViewModel.toggleSearch(if(selectedSearchType.value.equals("epc",true)) SearchTypeConstant.EPC else SearchTypeConstant.TID,if(selectedSearchType.value.equals("epc",true)) selectedEan.value!!.epc else selectedEan.value!!.tid)
            }
            else {
                if (tagVerifiedCount.value.toString() == tagCount) return@LaunchedEffect
                if (showSheet.value != false && currentSheet.value != BottomSheetType.NONE) return@LaunchedEffect
                //Code for Auto clearing
                /*{
                  sheetState.hide()
                  showSheet.value = false
                  currentSheet.value = BottomSheetType.NONE
                }*/
                else readerViewModel.toggleTagVerify()
            }
            /*if(!isVerifyingTags.value) {
                verifyResult.value = if (tagVerifiedCount.value == tagEncodedCount.value) VerifyResult.SUCCESS else VerifyResult.PENDING
            }*/
        }
    }

    LaunchedEffect(response.value) {
        if(response.value==null)return@LaunchedEffect
        val result = response.value!!
            if (!result.isSuccess) {
                LogUtils.showLog("response", "DecodingTagListContent: ${result.response}")
            }
            else if (response.value != null) {
                when (result.url) {
                    UrlConstants.PRODUCTS ->
                        handleProductDetails(
                            apiResult = result,
                            fieldList = infoList,
                        )
                }
            }
    }

    val focusManager = LocalFocusManager.current

    val expandedEans = remember { mutableStateMapOf<String, Boolean>() }
    val isGrouped = remember { mutableStateOf(false) }
    val visibleItemCount = remember { mutableIntStateOf(10) }
    val listState = rememberLazyListState()

    //val filteredBrands = if (searchQuery.value.isEmpty())  eanList.value  else eanList.value.filter { ean -> ean.barcode.contains(searchQuery.value, ignoreCase = true) }
    val isAllVerified = if (tagVerifiedCount.value.toString() == tagCount) painterResource(R.drawable.property_1_success) else painterResource(R.drawable.successywellocolor)


    val isChecked = remember { mutableStateOf(false) }

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index
        }.collect { lastVisibleIndex ->
            if (
                lastVisibleIndex != null &&
                lastVisibleIndex >= visibleItemCount.value - 3
            ) {
                visibleItemCount.value += 10
            }
        }
    }

    val filteredBrands = remember(
        searchQuery.value,
        eanList.value,
        visibleItemCount.value,
        isGrouped.value
    ) {

        derivedStateOf {

            val filtered = if (searchQuery.value.isBlank()) {

                eanList.value

            } else {

                eanList.value.filter {

                    it.barcode.contains(
                        searchQuery.value,
                        ignoreCase = true
                    )
                }
            }

            if (isGrouped.value) {
                filtered

            } else {
                filtered.take(visibleItemCount.value)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.Top) {
                TopBarContent(label = stringResource(R.string.verification_logs), onBackClickL = {
                    if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                        readerViewModel.clearSessionAndTransactionType()
                        navController.popBackStack()
                    }
                }, onSettingClick = {

                })
                HorizontalDivider(
                    thickness = dimensionResource(R.dimen.dp_1), color = OutlineDefault
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(dimensionResource(id = R.dimen.dp_16))
                        .clickable(
                            onClick = {},
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {


                    CounterText(
                        current = tagVerifiedCount.value, total = tagCount
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center , verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            isAllVerified,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_5)))
                        Text(
                            text = stringResource(id = R.string.tags_encoded),
                            style = CommonTypography.current.noteText,
                            color = BlackColor,
                            modifier = Modifier
                        )
                    }

                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_12), horizontal = dimensionResource(R.dimen.dp_100)),
                        thickness = dimensionResource(R.dimen.dp_1),
                        color = OutlineDefault
                    )

                    GroupEanToggle(
                        checked = isChecked.value,
                        onCheckedChange = {
                            isChecked.value = it
                            isGrouped.value = !isGrouped.value
                            expandedEans.clear()
                        },
                        label = stringResource(R.string.group_By_ean),
                    )

                    CommonTextField(
                        config = TextFieldConfig(
                            value = searchQuery.value,
                            onValueChange = { searchQuery.value = it },
                            label = stringResource(id = R.string.search_for_ean),
                            isSearch = true,
                            imeAction = ImeAction.Done,
                            onImeAction = {
                                focusManager.clearFocus()
                            }),
                        modifier = Modifier.padding(vertical = dimensionResource(R.dimen.dp_24))
                    )

                    RowViewWithHSI(
                            onClick = {},
                            epcList = filteredBrands.value,
                            icon = painterResource(R.drawable.icon__next),
                            grouped = isGrouped.value,
                            isIcon = true,
                            expandedEans = expandedEans,
                            clickableIconSearch = R.drawable.property_gieger_og,
                            clickableIconDetails = R.drawable.property_know_more,
                            onSearchClick = { tagTime ->
                                 selectedEan.value = tagTime
                                 openSheet(
                                    scope = scope,
                                    sheetState = sheetState,
                                    showSheet = showSheet,
                                    currentSheet = currentSheet,
                                    sheet = BottomSheetType.SEARCH
                                )

                            },
                            onDetailsClick = { barcode ->
                            selectedEan.value = barcode

                            callProductDetails(
                                barCodeNo = barcode.barcode,
                                apiViewModel = apiViewModel,
                                tagTime = barcode
                            )

                            openSheet(
                                scope = scope,
                                sheetState = sheetState,
                                showSheet = showSheet,
                                currentSheet = currentSheet,
                                sheet = BottomSheetType.PRODUCT_DETAILS
                            )

                        },
                            listState=listState
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
                    BottomSheetType.MESSAGE ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .fillMaxHeight(0.42f)
                        ) {
                            MessageComponent(
                                icon = R.drawable.success_white,
                                backGroundColor = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFF048204),
                                        Color(0xFF02AC02)
                                    )
                                ),
                                heading = stringResource(R.string.tags_verified_successfully),
                                description = stringResource(
                                    R.string.tag_verified_count,
                                    tagVerifiedCount.value,
                                    tagCount
                                ),
                                buttonText = stringResource(R.string.ok),
                                onClick = {
                                    if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) {
                                        readerViewModel.clearSessionAndTransactionType()
                                        navController.popBackStack()
                                    }
                                },
                                onClose = {
                                    scope.launch {
                                        sheetState.hide()
                                        showSheet.value = false
                                    }
                                },
                                tintColor = WhiteColor
                            )
                        }

                    BottomSheetType.NOTE ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .fillMaxHeight(0.42f)
                        ) {
                            MessageComponent(
                                icon = R.drawable.property_know_more,
                                backGroundColor = Brush.verticalGradient(
                                    colors = listOf(
                                        Color(0xFFF3B100),
                                        Color(0xFFFECF53)
                                    )
                                ),
                                heading = stringResource(R.string.tags_verified_pending),
                                description = stringResource(
                                    R.string.tag_verified_count,
                                    tagVerifiedCount.value,
                                    tagCount
                                ),
                                buttonText = stringResource(R.string.reverify),
                                onClick = {
                                    scope.launch {
                                        sheetState.hide()
                                        showSheet.value = false

                                        verifyResult.value = VerifyResult.NONE
                                        //verificationSession.intValue++       // 🔥 KEY

                                        readerViewModel.toggleTagVerify()
                                    }
                                },
                                onClose = {
                                    scope.launch {
                                        sheetState.hide()
                                        showSheet.value = false
                                    }
                                },
                                tintColor = WhiteColor
                            )
                        }

                    BottomSheetType.PRODUCT_DETAILS ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .fillMaxHeight(0.55f)
                        ) {
                            selectedEan.value?.let { tagTime ->
                                ProductDetailsByEan(
                                    tagTime = tagTime,
                                    infoList = infoList,
                                    title = stringResource(R.string.product_details_),
                                    sheetState = sheetState,
                                    scope = scope,
                                    showSheet = showSheet,
                                    isTrillingIcon = false,
                                    showImagePreview = showImagePreview,
                                    selectedPainter = selectedPainter,
                                    readerViewModel = readerViewModel
                                )
                            }
                        }
                    BottomSheetType.SEARCH -> Box(
                        modifier = Modifier
                            .background(WhiteColor)
                            .fillMaxWidth()
                            .fillMaxHeight(0.55f)
                    ) {
                        SearchBottomSheetView(
                            showSheet = showSheet,
                            sheetState = sheetState,
                            scope = scope,
                            selectedSearchType = selectedSearchType,
                            latestEan = selectedEan,
                            readerViewModel = readerViewModel
                        )
                    }

                    else -> {
                        currentSheet.value = BottomSheetType.NONE
                    }
                }


            }
        }
        Box(modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center){
            if (showImagePreview.value) {
                ImageViewFullScreen(showImagePreview , selectedPainter)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyLogsBottomBar(
    readerViewModel: ReaderViewModel,
    tagVerifiedCount: State<Int>,
    tagEncodedCount: State<Int>,
    scope: CoroutineScope,
    isVerifyingTags: State<Boolean>,
) {

    Box() {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(WhiteColor)
        ) {
            HorizontalDivider(color = OutlineDefault)
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(R.dimen.dp_16))) {

                CommonButton(
                    text = stringResource(id = if (isVerifyingTags.value) R.string.stop_verifying else R.string.start_verifying),
                    onClick = {
                        scope.launch {
                            readerViewModel.setTriggerValue(true)
                            /*if (!isVerifyingTags.value) {
                                //verificationSession.intValue++   // 🔥 KEY
                                verifyResult.value = VerifyResult.NONE
                                if(tagVerifiedCount.value==tagEncodedCount.value) verifyResult.value = VerifyResult.SUCCESS
                                else readerViewModel.toggleTagVerify()
                            } else {
                                readerViewModel.stopOperations()
                                verifyResult.value =
                                    if (tagVerifiedCount.value == tagEncodedCount.value)
                                        VerifyResult.SUCCESS
                                    else
                                        VerifyResult.PENDING
                            }*/
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = tagVerifiedCount.value < tagEncodedCount.value,
                    contentColor = WhiteColor
                )
            }
        }
    }
}
