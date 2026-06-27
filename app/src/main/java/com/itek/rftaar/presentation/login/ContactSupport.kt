package com.itek.rftaar.presentation.login

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavHostController
import com.itek.rftaar.R
import com.itek.rftaar.presentation.commonComp.CircularIcon
import com.itek.rftaar.presentation.commonComp.CommonButton
import com.itek.rftaar.presentation.commonComp.CommonTextField
import com.itek.rftaar.presentation.commonComp.GenericDropdown
import com.itek.rftaar.presentation.commonComp.TextFieldConfig
import com.itek.rftaar.ui.theme.BackGround
import com.itek.rftaar.ui.theme.CommonTypography
import com.itek.rftaar.ui.theme.LightGray
import com.itek.rftaar.ui.theme.TextSubtext
import com.itek.rftaar.ui.theme.WhiteColor

@Composable
fun ContactSupport(modifier: Modifier, navController: NavHostController) {
    BackHandler(enabled = true) {
        if(navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED)
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
                ContactSupportBottomBar()
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
            ContactSupportContent(navController)
        }
    }
}

@Composable
fun ContactSupportContent(navController: NavHostController) {

    val userName = remember { mutableStateOf("") }
    val cities = listOf("Pune", "Mumbai", "Bangalore")
    val focusManager = LocalFocusManager.current
    val comment = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .clickable(onClick = {
                focusManager.clearFocus()
            }, indication = null, interactionSource = remember { MutableInteractionSource() })
            .padding(horizontal = dimensionResource(id = R.dimen.dp_16))
            .padding(bottom = dimensionResource(id = R.dimen.dp_16)),
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = dimensionResource(R.dimen.dp_16)),
            horizontalAlignment = Alignment.Start
        ) {
            CircularIcon(
                iconRes = R.drawable.property_arrow_back, modifier = Modifier.clickable(
                    onClick = {
                        if (navController.currentBackStackEntry?.lifecycle?.currentState == Lifecycle.State.RESUMED) navController.popBackStack()
                    },
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() })
            )

            Text(
                text = stringResource(id = R.string.raise_request),
                style = CommonTypography.current.headingH1,
                modifier = Modifier.padding(vertical = dimensionResource(id = R.dimen.dp_10))
            )

            Text(
                text = stringResource(id = R.string.provide_details),
                style = CommonTypography.current.textMedium,
                color = TextSubtext,
                modifier = Modifier.padding(
                    vertical = dimensionResource(id = R.dimen.dp_14)
                )
            )
        }

        Spacer(modifier = Modifier.size(dimensionResource(R.dimen.dp_16)))

        CommonTextField(
            config = TextFieldConfig(
                value = userName.value,
                onValueChange = { userName.value = it },
                label = stringResource(id = R.string.username),
                isUserName = true,
                imeAction = ImeAction.Next,
            ), modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(id = R.string.issue_about),
            style = CommonTypography.current.textMedium,
            color = TextSubtext,
            modifier = Modifier.padding(
                top = dimensionResource(id = R.dimen.dp_12),
                bottom = dimensionResource(id = R.dimen.dp_8)
            )
        )


        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .zIndex(10f),
            verticalArrangement = Arrangement.Top
        ) {
            GenericDropdown(items = cities, labelMapper = { it }, onItemSelected = {})
        }


        Text(
            text = stringResource(id = R.string.briefly_explain),
            style = CommonTypography.current.textMedium,
            color = TextSubtext,
            modifier = Modifier.padding(
                top = dimensionResource(id = R.dimen.dp_12),
                bottom = dimensionResource(id = R.dimen.dp_8)
            )
        )


        CommonTextField(
            config = TextFieldConfig(
                value = comment.value,
                onValueChange = { comment.value = it },
                label = stringResource(id = R.string.comment),
                maxLine = true,
                imeAction = ImeAction.Next,
                singleLine = false
            ), modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 120.dp, max = 200.dp)
        )

        Text(
            text = stringResource(id = R.string.enter_email),
            style = CommonTypography.current.textMedium,
            color = TextSubtext,
            modifier = Modifier.padding(
                top = dimensionResource(id = R.dimen.dp_12),
                bottom = dimensionResource(id = R.dimen.dp_8)
            )
        )

        CommonTextField(
            config = TextFieldConfig(
                value = email.value,
                onValueChange = { email.value = it },
                label = stringResource(id = R.string.email),
                imeAction = ImeAction.Next,
            ), modifier = Modifier.fillMaxWidth()
        )

        Text(
            text = stringResource(id = R.string.upload_image),
            style = CommonTypography.current.textMedium,
            color = TextSubtext,
            modifier = Modifier.padding(
                top = dimensionResource(id = R.dimen.dp_12),
                bottom = dimensionResource(id = R.dimen.dp_8)
            )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clickable {
                    // Open gallery / camera
                },
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, LightGray),
            colors = CardDefaults.cardColors(
                containerColor = WhiteColor
            )
        ) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {

                Icon(
                    painter = painterResource(R.drawable.image),
                    contentDescription = null,
                    tint = TextSubtext,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(id = R.string.upload_image),
                    style = CommonTypography.current.textMedium,
                    color = TextSubtext
                )
            }
        }

        Spacer(modifier = Modifier.size(24.dp))
    }
}

@Composable
fun ContactSupportBottomBar() {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = dimensionResource(id = R.dimen.dp_16))
        .padding(horizontal = dimensionResource(id = R.dimen.dp_16))) {
        CommonButton(
            text = stringResource(id  = R.string.submit_request),
            onClick = {
            }
        )
    }
}