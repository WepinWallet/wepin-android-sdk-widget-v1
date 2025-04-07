package com.wepin.sample

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.wepin.android.widgetlib.WepinWidget
import com.wepin.android.widgetlib.types.LoginProviderInfo
import com.wepin.android.widgetlib.types.WepinAccount
import com.wepin.android.widgetlib.types.WepinWidgetAttribute
import com.wepin.android.widgetlib.types.WepinWidgetParams
import com.wepin.sample.ui.theme.WepinAndroidSDKTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WepinAndroidSDKTheme {
                WepinWidgetTestScreen()
            }
        }
    }
}

@Composable
fun WepinWidgetTestScreen() {
    var status by remember { mutableStateOf("Not Initialized") }
    val context = LocalContext.current

    var selectedLanguage by remember { mutableStateOf("en") }
    var appId by remember { mutableStateOf("WEPIN_APP_ID") }
    var appKey by remember { mutableStateOf("WEPIN_APP_KEY") }

    var showSettingsPanel by remember { mutableStateOf(false) }

    var wepinWidget by remember {
        mutableStateOf(
            WepinWidget(
                WepinWidgetParams(
                    context = context,
                    appId = appId,
                    appKey = appKey,
                )
            )
        )
    }

    val providerInfos = listOf(
        LoginProviderInfo(
            provider = "google",
            clientId = "GOOGLE_CLIENT_ID"
        ),
        LoginProviderInfo(provider = "apple", clientId = "APPLE_CLIENT_ID"),
        LoginProviderInfo(provider = "discord", clientId = "DISCORD_CLIENT_ID"),
        LoginProviderInfo(provider = "naver", clientId = "NAVER_CLIENT_ID"),
        LoginProviderInfo(provider = "facebook", clientId = "FACEBOOK_CLIENT_ID"),
        LoginProviderInfo(provider = "line", clientId = "LINE_CLIENT_ID"),
    )

    var accountList by remember { mutableStateOf(emptyList<WepinAccount>()) }
    var showAccountDialog by remember { mutableStateOf(false) }
    var selectedAccount by remember { mutableStateOf<WepinAccount?>(null) }
    var actionType by remember { mutableStateOf<ActionType?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE3F2FD))
            .padding(16.dp)
    ) {
        Text("Wepin Widget Test", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(20.dp))

        // 🔹 버튼과 설정 패널을 담은 스크롤 가능한 영역
        Column(
            modifier = Modifier
                .weight(2f) // 화면의 2/3 차지
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Wepin Widget Test", style = MaterialTheme.typography.headlineMedium)

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 설정 버튼 추가
            Button(
                onClick = { showSettingsPanel = !showSettingsPanel },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showSettingsPanel) "Close Settings" else "Open Settings")
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 🔹 설정 패널 (버튼 클릭 시 표시)
            if (showSettingsPanel) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Settings", style = MaterialTheme.typography.titleMedium)

                        // 언어 선택 드롭다운
                        Text("Select Language")
                        var expanded by remember { mutableStateOf(false) }
                        Box {
                            Button(
                                onClick = { expanded = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(selectedLanguage)
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                listOf("en", "ko", "ja").forEach { language ->
                                    DropdownMenuItem(
                                        text = { Text(language) },
                                        onClick = {
                                            selectedLanguage = language
                                            try {
                                                wepinWidget.changeLanguage(language)
                                            } catch (error: Exception) {
                                                status = "Error: $error"
                                            }
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // App ID 입력
                        Text("App ID")
                        TextField(
                            value = appId,
                            onValueChange = { appId = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // App Key 입력
                        Text("App Key")
                        TextField(
                            value = appKey,
                            onValueChange = { appKey = it },
                            modifier = Modifier.fillMaxWidth()
                        )

                        // 변경 적용 버튼
                        Button(
                            onClick = {
                                wepinWidget = WepinWidget(
                                    WepinWidgetParams(
                                        context = context,
                                        appId = appId,
                                        appKey = appKey,
                                    )
                                )
                                status = "Settings Applied"
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Apply Changes")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // 기능 버튼들
            Button(
                onClick = {
                    wepinWidget.initialize(attributes = WepinWidgetAttribute(defaultLanguage = selectedLanguage))
                        .thenApply {
                            status = if (it) "Initialized" else "Initialization Failed"
                        }.exceptionally {
                            status = "Error: ${it.message}"
                        }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Initialize")
            }

            Button(
                onClick = {
                    status =
                        if (wepinWidget.isInitialized()) "Widget is Initialized" else "Not Initialized"
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Check Initialization Status")
            }

            Button(
                onClick = {
                    wepinWidget.getStatus()?.thenApply {
                        status = "Status: $it"
                    }?.exceptionally {
                        status = "Error: ${it.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("GetStatus")
            }

            Button(
                onClick = {
                    wepinWidget.loginWithUI(context, listOf()).thenApply {
                        status = "logged in: $it"
                    }.exceptionally {
                        status = "Error: ${it.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("loginWithUI")
            }

            Button(
                onClick = {
                    wepinWidget.loginWithUI(context, listOf(), "EMAIL").thenApply {
                        status = "logged in: $it"
                    }.exceptionally {
                        status = "Error: ${it.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("loginWithUI(with Email)")
            }

            Button(
                onClick = {
                    wepinWidget.loginWithUI(context, providerInfos).thenApply {
                        status = "logged in: $it"
                    }.exceptionally {
                        status = "Error: ${it.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("loginWithUI(With Providers)")
            }

            Button(
                onClick = {
                    wepinWidget.register(context).thenApply {
                        status = "Registered: $it"
                    }.exceptionally {
                        status = "Error: ${it.message}"
//                        null
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("register")
            }

            Button(
                onClick = {
                    wepinWidget.getAccounts().thenApply {
                        status = "getAccount: $it"
                        accountList = it
                    }.exceptionally {
                        status = "Error: ${it.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("getAccount")
            }

            Button(
                onClick = {
                    wepinWidget.getBalance().thenApply {
                        status = "getBalance in: $it"
                    }.exceptionally {
                        status = "Error: ${it.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("getBalance")
            }

            Button(
                onClick = {
                    wepinWidget.getNFTs(false).thenApply {
                        status = "getNFTs in: $it"
                    }.exceptionally {
                        status = "Error: ${it.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("getNFTs")
            }

            Button(
                onClick = {
                    wepinWidget.getNFTs(true).thenApply {
                        status = "getNFTs(Refresh) in: $it"
                    }.exceptionally {
                        status = "Error: ${it.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("getNFTs(Refresh)")
            }
            // 🔹 계정 선택 버튼 추가 (Send & Receive)
            AccountSelectionButton(
                onAccountsFetched = { accounts, type ->
                    accountList = accounts
                    actionType = type
                    showAccountDialog = true
                },
                onError = { errorMessage -> status = "Error: $errorMessage" },
                wepinWidget = wepinWidget
            )

            if (showAccountDialog) {
                AccountSelectionDialog(
                    accountList = accountList,
                    onAccountSelected = { account ->
                        selectedAccount = account
                        showAccountDialog = false
                        when (actionType) {
                            ActionType.SEND -> executeSend(context, wepinWidget, account) {
                                status = it
                            }

                            ActionType.RECEIVE -> executeReceive(
                                context,
                                wepinWidget,
                                account
                            ) { status = it }

                            else -> status = "No action selected"
                        }
                    },
                    onDismiss = { showAccountDialog = false }
                )
            }

            Button(
                onClick = {
                    wepinWidget.openWidget(context).thenApply {
                        status = "Widget Opened"
                    }.exceptionally {
                        status = "Error: ${it.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Open Widget")
            }

            Button(
                onClick = {
                    try {
                        wepinWidget.closeWidget()
                        status = "Widget Closed"
                    } catch (e: Exception) {
                        status = "Error: ${e.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Close Widget")
            }

            Button(
                onClick = {
                    wepinWidget.finalize().thenApply {
                        status = "Finalized"
                    }.exceptionally {
                        status = "Error: ${it.message}"
                    }
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text("Finalize")
            }
        }

        // 🔹 결과 텍스트 영역 (화면의 1/3 차지)
        Box(
            modifier = Modifier
                .weight(1f) // 화면의 1/3 차지
                .fillMaxWidth()
                .padding(top = 10.dp)
                .background(Color.White)
        ) {
            Card(
                modifier = Modifier.fillMaxSize(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Result", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = status, style = MaterialTheme.typography.bodyLarge)
                }
            }
        }
    }
}

/**
 * 계정 조회 및 선택 버튼 (Send & Receive)
 */
@Composable
fun AccountSelectionButton(
    onAccountsFetched: (List<WepinAccount>, ActionType) -> Unit,
    onError: (String) -> Unit,
    wepinWidget: WepinWidget
) {
    Column {
        Button(
            onClick = {
                wepinWidget.getAccounts().thenApply {
                    onAccountsFetched(it, ActionType.SEND)
                }.exceptionally {
                    onError(it.message ?: "Unknown error")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Select Account & Send")
        }

        Button(
            onClick = {
                wepinWidget.getAccounts().thenApply {
                    onAccountsFetched(it, ActionType.RECEIVE)
                }.exceptionally {
                    onError(it.message ?: "Unknown error")
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Text("Select Account & Receive")
        }
    }
}

/**
 * 계정 선택 다이얼로그
 */
@Composable
fun AccountSelectionDialog(
    accountList: List<WepinAccount>,
    onAccountSelected: (WepinAccount) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select an Account") },
        text = {
            LazyColumn {
                items(accountList) { account ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAccountSelected(account) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${account.network}: ${account.address}")
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * 계정을 선택한 후 send 실행하는 함수
 */
private fun executeSend(
    context: Context,
    wepinWidget: WepinWidget,
    account: WepinAccount,
    updateStatus: (String) -> Unit
) {
    wepinWidget.send(context, account)
        .thenApply { result -> updateStatus("Send success: $result") }
        .exceptionally { error ->
            updateStatus("Error: ${error.message}")
        }
}

/**
 * 계정을 선택한 후 receive 실행하는 함수
 */
private fun executeReceive(
    context: Context,
    wepinWidget: WepinWidget,
    account: WepinAccount,
    updateStatus: (String) -> Unit
) {
    wepinWidget.receive(context, account)
        .thenApply { result -> updateStatus("Receive success: $result") }
        .exceptionally { error ->
            updateStatus("Error: ${error.message}")
        }
}

/**
 * Send & Receive 구분을 위한 Enum
 */
enum class ActionType {
    SEND, RECEIVE
}

@Preview(showBackground = true)
@Composable
fun WepinWidgetTestScreenPreview() {
    WepinAndroidSDKTheme {
        WepinWidgetTestScreen()
    }
}