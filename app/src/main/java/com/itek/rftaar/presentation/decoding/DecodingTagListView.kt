package com.itek.rftaar.presentation.decoding

import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.key
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.ReaderActivity
import com.itek.rftaar.api.ApiResult
import com.itek.rftaar.api.constants.ParameterConstants
import com.itek.rftaar.api.constants.UrlConstants
import com.itek.rftaar.core.common.utils.LogUtils
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONArray
import com.itek.rftaar.core.common.utils.ParseUtils.extractJSONObject
import com.itek.rftaar.core.common.utils.ParseUtils.extractString
import com.itek.rftaar.core.database.AppDatabase
import com.itek.rftaar.core.database.DataStoreManager
import com.itek.rftaar.data.model.LabelName
import com.itek.rftaar.data.model.TagTime
import com.itek.rftaar.mqtt.constants.SearchTypeConstant
import com.itek.rftaar.mqtt.constants.TopicConstants
import com.itek.rftaar.presentation.commonComp.BottomSheetType
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.CounterText
import com.itek.rftaar.presentation.commonComp.GroupEanToggle
import com.itek.rftaar.presentation.commonComp.ImageViewFullScreen
import com.itek.rftaar.presentation.commonComp.ItekFooter
import com.itek.rftaar.presentation.commonComp.RowViewWithHSI
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.presentation.commonComp.TopBarContent
import com.itek.rftaar.presentation.commonComp.VerifyInfoCard
import com.itek.rftaar.presentation.commonComp.openSheet
import com.itek.rftaar.presentation.encoding.SearchBottomSheetView
import com.itek.rftaar.presentation.encoding.VerificationResult
import com.itek.rftaar.presentation.encoding.VerificationUiState
import com.itek.rftaar.presentation.encoding.callProductDetails
import com.itek.rftaar.presentation.viewmodel.ApiViewModel
import com.itek.rftaar.presentation.viewmodel.ReaderViewModel
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.BlackColor
import com.itek.rftaar.ui.theme.BorderGreen
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.Green
import com.itek.rftaar.ui.theme.OutlineDefault
import com.itek.rftaar.ui.theme.TabColor
import com.itek.rftaar.ui.theme.TextGrey
import com.itek.rftaar.ui.theme.WhiteColor
import com.itek.rftaar.utils.CommonUtils.chkNull
import com.itek.rftaar.utils.CommonUtils.chkTrue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnrememberedMutableState")
@Composable
fun DecodingTagListScreen(
    modifier: Modifier,
    navController: NavHostController,
    searchParams: Map<String, Any> = emptyMap(),
    menuCode: String,
    label: String,
    apiViewModel:ApiViewModel = hiltViewModel(),
) {
    val decodeList =
        navController.previousBackStackEntry
            ?.savedStateHandle
            ?.get<SnapshotStateList<LabelName>>("decodeList")
            ?: mutableStateListOf()

    val topic = extractString(searchParams,"topic", TopicConstants.DECODE)
    val transactionType = searchParams["transactionType"] as? String ?: ""
    val tagCount = searchParams["tagCount"] as? String ?: ""
    val selectedEan = remember {mutableStateOf<TagTime?>(null)}

    val context = LocalContext.current
    val activity = context as? ReaderActivity
    val readerViewModel = if (activity != null) activity.findReaderViewModel() else hiltViewModel()
    if (activity != null) {
        readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode, transactionType,topic= topic)
    }

    val isProcessOn = readerViewModel.isProcessOn().observeAsState()
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { sheetValue ->
            sheetValue != SheetValue.Hidden
        }
    )
    val showSheet = remember { mutableStateOf(false) }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }


    BackHandler(enabled = true) {
        scope.launch {
            if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@launch
            if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
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
                    DecodingTagListBottomBar()
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
                DecodingTagListContent(
                    navController,
                    label,
                    menuCode,
                    transactionType,
                    decodeList,
                    selectedEan = selectedEan,
                    scope,
                    sheetState,
                    showSheet,
                    currentSheet,
                    readerViewModel,
                    apiViewModel,
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DecodingTagListContent(
    navController: NavHostController,
    label: String,
    menuCode: String,
    transactionType: String,
    decodeList: List<LabelName>,
    selectedEan: MutableState<TagTime?>,
    scope: CoroutineScope,
    sheetState: SheetState,
    showSheet: MutableState<Boolean>,
    currentSheet: MutableState<BottomSheetType>,
    readerViewModel: ReaderViewModel,
    apiViewModel: ApiViewModel
) {
    val context = LocalContext.current
    val db = AppDatabase.getDbInstance(context)
    val selectedType = remember { mutableStateOf(transactionType) }
    val searchQuery = remember { mutableStateOf("") }
    val eanList = db.tagInfoDao()
        .getEncodedTagTime(menuCode, selectedType.value, "")
        .collectAsState(initial = emptyList())

    val focusManager = LocalFocusManager.current

    val expandedEans = remember { mutableStateMapOf<String, Boolean>() }
    val isGrouped = remember { mutableStateOf(false) }

    //val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    //val showSheet = remember { mutableStateOf(false) }
    /*val scope = rememberCoroutineScope()
    val activity = context as? ReaderActivity
    val readerViewModel = activity?.findReaderViewModel() ?: hiltViewModel()
    if (activity != null) {
        readerViewModel.onCreate()
        readerViewModel.setSessionAndTransactionType(menuCode)
    }*/
    val infoList = remember { mutableStateListOf<Pair<String, String>>() }
    val currentSheet = remember { mutableStateOf(BottomSheetType.NONE) }
    val response = apiViewModel.apiResult.collectAsState(null)
    val isChecked = remember { mutableStateOf(false) }
    val showImagePreview = remember { mutableStateOf(false) }
    val selectedPainter = remember { mutableStateOf<Painter?>(null) }

    val selectedSearchType = remember{ mutableStateOf("")}

    val triggerPressed = readerViewModel.isTriggerPressed().observeAsState(false)

    val isProcessOn = readerViewModel.isProcessOn().observeAsState()
    val isApiLoading = apiViewModel.isLoading.observeAsState(false)

    val selectedItem = decodeList.firstOrNull {
        it.name == selectedType.value
    }

    val visibleItemCount = remember { mutableIntStateOf(10) }
    val listState = rememberLazyListState()

    /*LaunchedEffect(tagInfoData.value) {
        if (tagInfoData.value != null) {
            eanList.value.forEach { barcode ->
                callProductDetails(
                    barCodeNo = barcode.barcode,
                    apiViewModel = apiViewModel,
                    tagInfoData = tagInfoData.value
                )
            }
        }
    }*/

    LaunchedEffect(response.value) {
        if(response.value==null) return@LaunchedEffect
        val result = response.value!!
            if (!result.isSuccess) {
                LogUtils.showLog("response", "DecodingTagListContent: ${result.response}")
            } else if (response.value != null) {
                when (result.url) {
                    UrlConstants.PRODUCTS ->
                        handleProductDetails(
                            apiResult = result,
                            fieldList = infoList,
                        )
                }
            }
        }

    LaunchedEffect(triggerPressed.value) {
        if (triggerPressed.value != true) return@LaunchedEffect
        readerViewModel.setTriggerValue(false)
        if (chkTrue(isApiLoading.value)) return@LaunchedEffect
        if(navController.currentBackStackEntry?.lifecycle?.currentState != Lifecycle.State.RESUMED) return@LaunchedEffect
        if(transactionType.isNullOrEmpty()) return@LaunchedEffect
        if (showSheet.value != false && currentSheet.value == BottomSheetType.PRODUCT_DETAILS && selectedEan.value!=null) {
           //readerViewModel.performReturn(selectedEan.value!!)
           /* val tagTime = selectedEan.value
            if(tagTime!=null && tagTime.tagInfoId>0) {
                LogUtils.showLog("selTagTimeId",""+tagTime.tagInfoId)
                CoroutineScope(Dispatchers.IO).launch {
                    val tagInfo = db.tagInfoDao().getById(tagTime.tagInfoId)
                    LogUtils.showLog("selTagInfo",""+tagInfo)
                    if(tagInfo!=null) {
                        scope.launch { readerViewModel.performReturn(tagInfo) }
                    }
                }
            }*/
        }
        else if (showSheet.value != false && currentSheet.value == BottomSheetType.SEARCH) {
            //Based on Selected Epc or TID
            if(selectedSearchType.value.isNotEmpty() && selectedEan.value!=null)
                readerViewModel.toggleSearch(if(selectedSearchType.value.equals("epc",true)) SearchTypeConstant.EPC else SearchTypeConstant.TID,if(selectedSearchType.value.equals("epc",true)) selectedEan.value!!.epc else selectedEan.value!!.tid)
        }
    }

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
        eanList.value,
        searchQuery.value,
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
                TopBarContent(label = stringResource(R.string.__lists,label), onBackClickL = {
                    scope.launch {
                        if (chkTrue(isProcessOn.value) || chkTrue(isApiLoading.value)) return@launch
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
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
                        current = eanList.value.size, total = "", isLimitShow = false
                    )

                    selectedItem?.let { data ->
                        Text(
                            text = stringResource(
                                id = R.string.tags_decoded,
                                data.label
                            ),
                            style = CommonTypography.current.noteText,
                            color = BlackColor,
                            modifier = Modifier.padding(
                                bottom = dimensionResource(R.dimen.dp_12)
                            )
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            vertical = dimensionResource(R.dimen.dp_12),
                            horizontal = dimensionResource(R.dimen.dp_100)
                        ),
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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = dimensionResource(R.dimen.dp_12),
                                bottom = dimensionResource(R.dimen.dp_24)
                            ),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {

                            Row(
                                modifier = Modifier
                                    .horizontalScroll(rememberScrollState())
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.dp_8))
                            )
                            {
                                decodeList.forEach { data ->

                                    val isSelected = selectedType.value == data.name

                                    key(data.name) {
                                        Card(
                                            modifier = Modifier
                                                .width(dimensionResource(R.dimen.dp_76))
                                                .height(dimensionResource(R.dimen.dp_68))
                                                .border(
                                                    width = dimensionResource(R.dimen.dp_2),
                                                    color = if (isSelected) BorderGreen else TabColor,
                                                    shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                                                )
                                                .clickable(
                                                    onClick = {
                                                        if (selectedType.value != data.name) {
                                                            selectedType.value = data.name
                                                        }
                                                    },
                                                    indication = null,
                                                    interactionSource = remember { MutableInteractionSource() }
                                                ),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            shape = RoundedCornerShape(dimensionResource(R.dimen.dp_16))
                                        ) {
                                            Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .wrapContentHeight()
                                                    .padding(vertical = dimensionResource(R.dimen.dp_12)),
                                                horizontalAlignment = Alignment.CenterHorizontally,
                                                verticalArrangement = Arrangement.Center
                                            ) {
                                                Image(
                                                    painter = painterResource(
                                                        if (isSelected) R.drawable.success
                                                        else getTypeIcon(data.name)
                                                    ),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(dimensionResource(R.dimen.dp_24))
                                                )

                                                Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_4)))

                                                Text(
                                                    text = data.label,
                                                    style = CommonTypography.current.noteText.copy(
                                                        if (isSelected) Green else TextGrey
                                                    ),
                                                    textAlign = TextAlign.Center,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Visible,
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .basicMarquee()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }

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
                        onSearchClick = {tagTime ->
                            selectedEan.value = tagTime
                            openSheet(
                                scope = scope,
                                sheetState = sheetState,
                                showSheet = showSheet,
                                currentSheet = currentSheet,
                                sheet = BottomSheetType.SEARCH
                            )
                        },
                        iconBackGroundColor = SolidColor(TabColor),
                        listState = listState
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
                    BottomSheetType.PRODUCT_DETAILS ->
                        Box(
                            modifier = Modifier
                                .background(WhiteColor)
                                .fillMaxWidth()
                                .wrapContentHeight()
//                                .fillMaxHeight(0.55f)
                        ) {
                            selectedEan.value?.let { tagTime ->
                                ProductDetailsByEan(
                                    tagTime = tagTime,
                                    infoList=infoList,
                                    title = stringResource(R.string.product_details_),
                                    sheetState =sheetState,
                                    scope = scope,
                                    showSheet= showSheet,
                                    isTrillingIcon = false,
                                    showImagePreview = showImagePreview,
                                    selectedPainter = selectedPainter,
                                    readerViewModel=readerViewModel,
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

                    else ->
                        BottomSheetType.NONE
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
fun DecodingTagListBottomBar() {
    ItekFooter()
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailsByEan(
    barcode: String="",
    tagTime: TagTime?=null,
    infoList: SnapshotStateList<Pair<String, String>>,
    title: String = stringResource(R.string.product_details_),
    sheetState: SheetState,
    scope: CoroutineScope,
    showSheet: MutableState<Boolean>,
    isTrillingIcon: Boolean = true,
    showImagePreview: MutableState<Boolean>,
    selectedPainter: MutableState<Painter?>,
    readerViewModel: ReaderViewModel
) {

    val isShowReturnButton = false//tagTime!=null && chkNull(tagTime.epc,"").startsWith(DataStoreManager.readFromPreferences("decodeBits","0"))
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(dimensionResource(R.dimen.dp_16)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = CommonTypography.current.textSemiBold,
                modifier = Modifier.align(Alignment.Center)
            )


            Image(
                    painter = painterResource(R.drawable.clear),
                    contentDescription = "Close",
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .clickable {
                            scope.launch {
                                sheetState.hide()
                                showSheet.value = false
                            }
                        }
                )

        }

        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_28)))

        VerifyInfoCard(
            modifier = Modifier.fillMaxWidth().weight(1f ,false),
            value = if(tagTime!=null) tagTime.barcode else barcode,
            label = DataStoreManager.getBarcodeLabel(),
            onValueChange = {},
            onScanClick = {},
            verificationUiState = VerificationUiState.NONE,
            verificationResult = VerificationResult.NONE,
            infoList = infoList,
            isTrillingIcon = isTrillingIcon,
            showImagePreview = showImagePreview,
            selectedPainter = selectedPainter
        )

        if (isShowReturnButton){
            Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_24)))

            CommonButton(
                text = stringResource(id = R.string.return_),
                onClick = {
                  readerViewModel.setTriggerValue(true)
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}


fun saveProductHeaders(rootJson: JSONObject): List<String> {
    val data = extractJSONObject(rootJson, ParameterConstants.DATA, rootJson)
    val headerArray = extractJSONArray(data, ParameterConstants.HEADERS, JSONArray())
    val listProductLabels = ArrayList<String>()
    if (headerArray != null && headerArray.length() > 0)
        for (i in 0 until headerArray.length()) {
            val key = headerArray.getString(i)
            if (key.isNullOrEmpty() && !key.equals(DataStoreManager.getImageLabel(),true)) continue
            listProductLabels.add(key)
        }
    if (listProductLabels.isNotEmpty()) DataStoreManager.saveListStr("listProductLabels", listProductLabels)
    return DataStoreManager.getListStr("listProductLabels")
}

fun parseProductDetails(rootJson: JSONObject): ArrayList<Pair<String, String>> {
    val data = extractJSONObject(rootJson, ParameterConstants.DATA, rootJson)
    val headerArray = extractJSONArray(data, ParameterConstants.HEADERS, JSONArray())
    val productsArray = extractJSONArray(
        data,
        ParameterConstants.PRODUCTS,
        extractJSONArray(data, ParameterConstants.DATA, JSONArray())
    )

    val listProductLabels = saveProductHeaders(rootJson)
    val listProductValues = ArrayList<Pair<String, String>>()
    if (productsArray != null && productsArray.length() > 0) {
        for (i in 0 until productsArray.length()) {
            val product = productsArray.getJSONObject(i)
            val keys = product.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                if (key.isNullOrEmpty()) continue
                if(listProductLabels.isNotEmpty() && !listProductLabels.contains(key)) continue
                val value = chkNull(extractString(product, key, ""), "")
                listProductValues.add(key to value)
            }
        }
    }
    else if (headerArray != null && headerArray.length() > 0) {
        for (i in 0 until headerArray.length()) {
            val key = headerArray.getString(i)
            if (key.isNullOrEmpty() && !key.equals(DataStoreManager.getImageLabel(),true)) continue
            listProductValues.add(key to "")
        }
    }
    if (listProductValues.isEmpty()) listProductValues.add("Product" to "Data Not Available")

    return listProductValues;
}

fun handleProductDetails(
    apiResult: ApiResult?,
    fieldList: SnapshotStateList<Pair<String, String>>
) {
    val rootJson = apiResult?.response ?: return
    val listProductValues = parseProductDetails(rootJson)
    /*val data = extractJSONObject(rootJson, ParameterConstants.DATA, rootJson)
    val headerArray = extractJSONArray(data, ParameterConstants.HEADERS, JSONArray())
    val productsArray = extractJSONArray(
        data,
        ParameterConstants.PRODUCTS,
        extractJSONArray(data, ParameterConstants.DATA, JSONArray())
    )

    val listProductLabels = ArrayList<String>()
    if (headerArray != null && headerArray.length() > 0)
        for (i in 0 until headerArray.length()) {
            val key = headerArray.getString(i)
            if (key.isNullOrEmpty() && !key.equals(DataStoreManager.getImageLabel(),true)) continue
            listProductLabels.add(key)
        }
    if (listProductLabels.isNotEmpty()) DataStoreManager.saveToPreferences("listProductLabels", listProductLabels.toString())
    val listProductValues = ArrayList<Pair<String, String>>()
    if (productsArray != null && productsArray.length() > 0) {
        for (i in 0 until productsArray.length()) {
            val product = productsArray.getJSONObject(i)
            val keys = product.keys()

            while (keys.hasNext()) {
                val key = keys.next()
                if (key.isNullOrEmpty()) continue
                val value = chkNull(extractString(product, key, ""), "")
                listProductValues.add(key to value)
            }
        }
    }
    else if (headerArray != null && headerArray.length() > 0) {
        for (i in 0 until headerArray.length()) {
            val key = headerArray.getString(i)
            if (key.isNullOrEmpty() && !key.equals(DataStoreManager.getImageLabel(),true)) continue
            listProductValues.add(key to "")
        }
    }
    if (listProductValues.isEmpty()) listProductValues.add("Product" to "Data Not Available")*/

    fieldList.clear()
    fieldList.addAll(listProductValues)
}