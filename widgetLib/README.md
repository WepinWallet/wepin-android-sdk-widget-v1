<br/>

<p align="center">
  <a href="https://wepin.io">
      <picture>
        <source media="(prefers-color-scheme: dark)" srcset="https://github.com/WepinWallet/wepin-web-sdk-v1/blob/main//assets/wepin_logo_white.png">
        <img bg_color="white" alt="wepin logo" src="https://github.com/WepinWallet/wepin-web-sdk-v1/blob/main//assets/wepin_logo_color.png" width="250" height="auto">
      </picture>
  </a>
</p>

<br>

# Wepin Android Widget SDK v1

[![platform - android](https://img.shields.io/badge/platform-Android-3ddc84.svg?logo=android&style=for-the-badge)](https://www.android.com/)
[![SDK Version](https://img.shields.io/jitpack/version/com.github.WepinWallet/wepin-android-sdk-widget-v1.svg?logo=jitpack&style=for-the-badge)](https://jitpack.io/v/com.github.WepinWallet/wepin-android-sdk-widget-v1)

Wepin Widget SDK for Android. This package is exclusively available for use in Android environments.

## ⏩ Get App ID and Key

After signing up for [Wepin Workspace](https://workspace.wepin.io/), navigate to the development
tools menu, and enter the required information for each app platform to receive your App ID and App
Key.

## ⏩ Requirements

- **Android**: API version **24** or newer is required.

## ⏩ Install

1. Add JitPack repository in your project-level build gradle file

- kts
  ```kotlin
   dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
           google()
           mavenCentral()
           maven("https://jitpack.io") // <= Add JitPack Repository
       }
   }
  ```

2. Add implementation in your app-level build gradle file

- kts
  ```
  dependencies {
    // ...
    implementation("com.github.WepinWallet:wepin-android-sdk-widget-v1:vX.X.X") 
  }
  ```
  > **<span style="font-size: 35px;"> !!Caution!! </span>**  We recommend
  using [the latest released version of the SDK](https://github.com/WepinWallet/wepin-android-sdk-widget-v1/releases)

## ⏩ Getting Started

### Config Deep Link

he Deep Link configuration is required for logging into Wepin. Setting up the Deep Link Scheme
allows your app to handle external URL calls.

The format for the Deep Link scheme is `wepin. + Your Wepin App ID`

When a custom scheme is used, the WepinWidget SDK can be easily configured to capture all redirects
using this custom scheme through a manifest placeholder in the `build.gradle (app)` file::

```kotlin
// For Deep Link => RedirectScheme Format: wepin. + Wepin App ID
android.defaultConfig.manifestPlaceholders = [
    'appAuthRedirectScheme': 'wepin.{{YOUR_WEPIN_APPID}}'
]
```

### Add Permssion

To use this SDK, camera access permission is required. The camera function is essential for
recognizing addresses in QR code format.
Add the below line in your app's `AndroidMainfest.xml` file

```xml

<manifest xmlns:android="http://schemas.android.com/apk/res/android">
    <uses-permission android:name="android.permission.CAMERA" />
    <uses-permission android:name="android.permission.INTERNET" />
    <!-- ... -->
</manifest>
```

## ⏩ Import SDK

```kotlin
  import com.wepin.android.widgetlib.WepinWidget
```

## ⏩ Initialize

```kotlin
    val wepinWidgetParams = WepinWidgetParams(
    context = this,
    appId = "Wepin-App-ID",
    appKey = "Wepin-App-Key",
)
var wepinWidget = WepinWidget(wepinWidgetParams)
```

### init

```kotlin
    val res = wepinWidget.initialize(attributes)
```

#### Parameters

- `attributes` \<WepinWidgetAttribute>
    - `defaultLanguage` \<String> - The language to be displayed on the widget (default: 'en').
      Currently, only 'ko', 'en', and 'ja' are supported.
    - `defaultCurrency` \<String> - The currency to be displayed on the widget. Currently, only '
      KRW', 'USD', 'JPY' are suppored.

#### Returns

- CompletableFuture\<Boolean>
    - Returns `true` if success

#### Example

```kotlin
      var attributes = WepinWidgetAttribute("en", "USD")
val res = wepinWidget.initialize(attributes)
res?.whenComplete { infResponse, error ->
    if (error == null) {
        println(infResponse)
    } else {
        println(error)
    }
}
```

### isInitialized

```kotlin
wepinWidget.isInitialized()
```

The `isInitialized()` method checks if the Wepin Widget SDK is initialized.

#### Returns

- \<Boolean> - Returns `true` if Wepin Widget SDK is already initialized, otherwise false.

### changeLanguage

```kotlin
wepinWidget.changeLanguage("ko", "KRW")
```

The `changeLanguage()` method changes the language of the widget.

#### Parameters

- `language` \<String> - The language to be displayed on the widget. Currently, only 'ko', 'en',
  and 'ja' are supported.
- `currency` \<String> - __optional__ The currency to be displayed on the widget. Currently, only '
  KRW', 'USD', 'JPY' are suppored.

#### Example

```kotlin
try {
    wepinWidget.changeLanguage("ko", "KRW")
} catch (e: Exception) {
    println(err)
}
```

### getStatus

```kotlin
wepinWidget.getStatus()
```

The `getStatus()` method returns the lifecycle status of Wepin Widget SDK.

#### Parameters

- None

#### Returns

- Future\<WepinLifeCycle> - Returns the current lifecycle of the Wepin SDK, which is defined as
  follows:
    - `NOT_INITIALIZED`:  Wepin is not initialized.
    - `INITIALIZING`: Wepin is in the process of initializing.
    - `INITIALIZED`: Wepin is initialized.
    - `BEFORE_LOGIN`: Wepin is initialized but the user is not logged in.
    - `LOGIN`:The user is logged in.
    - `LOGIN_BEFORE_REGISTER`: The user is logged in but not registered in Wepin.

#### Example

```kotlin
      val res = wepinWidget.getStatus(attributes)
res?.whenComplete { status, error ->
    if (error == null) {
        println(status)
    } else {
        println(error)
    }
}
```

## ⏩ Method & Variable

Methods and Variables can be used after initialization of Wepin Widget SDK.

### login

The `login` variable is a Wepin login library that includes various authentication methods, allowing
users to log in using different approaches. It supports email and password login, OAuth provider
login, login using ID tokens or access tokens, and more. For detailed information on each method,
please refer to the official library documentation
at [wepin_android_login_lib](https://github.com/WepinWallet/wepin-android-sdk-login-v1).

#### Available Methods

- `loginWithOauthProvider`
- `signUpWithEmailAndPassword`
- `loginWithEmailAndPassword`
- `loginWithIdToken`
- `loginWithAccessToken`
- `getRefreshFirebaseToken`
- `loginWepin`
- `getCurrentWepinUser`
- `logout`
- `getSignForLogin`

These methods support various login scenarios, allowing you to select the appropriate method based
on your needs.

For detailed usage instructions and examples for each method, please refer to the official library
documentation. The documentation includes explanations of parameters, return values, exception
handling, and more.

#### Example

```kotlin
// Login using an OAuth provider
val oauthResult =
    wepinWidget.login.loginWithOauthProvider(provider: 'google', clientId: 'your-client-id')
oauthResult?.whenComplete { res, error ->
    if (error == null) {
        println(res)
    } else {
        println(error)
    }
}

// Sign up and log in using email and password
val signUpResult =
    wepinWidget.login.signUpWithEmailAndPassword(email: 'example@example.com', password: 'password123')
signUpResult?.whenComplete { res, error ->
    if (error == null) {
        println(res)
    } else {
        println(error)
    }
}

// Log in using an ID token
val idTokenResult = wepinWidget.login.loginWithIdToken(idToken: 'your-id-token', sign: 'your-sign')
idTokenResult?.whenComplete { res, error ->
    if (error == null) {
        println(res)
    } else {
        println(error)
    }
}

// Log in to Wepin
val wepinLoginResult = wepinWidget.login.loginWepin(idTokenResult)
wepinLoginResult?.whenComplete { res, error ->
    if (error == null) {
        println(res)
    } else {
        println(error)
    }
}

// Get the currently logged-in user
val currentUser = wepinWidget.login.getCurrentWepinUser()
currentUser?.whenComplete { res, error ->
    if (error == null) {
        println(res)
    } else {
        println(error)
    }
}

// Logout
wepinWidget.login.logout()

```

For more details on each method and to see usage examples, please visit the
official  [wepin_android_login_lib documentation](https://github.com/WepinWallet/wepin-android-sdk-login-v1).

### loginWithUI

```kotlin
wepinWidget.loginWithUI(context: Context, loginProviders:List< LoginProvider > , email: String?})
```

The loginWithUI() method provides the functionality to log in using a widget and returns the
information of the logged-in user. If a user is already logged in, the widget will not be displayed,
and the method will directly return the logged-in user's information. To perform a login without the
widget, use the loginWepin() method from the login variable instead.

> [!CAUTION]
> This method can only be used after the authentication key has been deleted from
> the [Wepin Workspace](https://workspace.wepin.io/).
> (Wepin Workspace > Development Tools menu > Login tab > Auth Key > Delete)
> > * The Auth Key menu is visible only if an authentication key was previously generated.

#### Parameters

- context \<Context> - The `Context` parameter is required in Android as it provides access to
  application-specific resources and classes, as well as information about the application
  environment. It is used to start new activities, access application assets, retrieve system
  services, and more.

When you call methods such as loginWithUI(context), make sure to pass the appropriate Context (such
as an `Activity` or `Application context`) to ensure proper operation. For UI-related actions (e.g.,
opening a new screen), an Activity context is recommended.

- loginProviders \<List\<LoginProvider>> - An array of login providers to configure the widget. If
  an empty array is provided, only the email login function is available.
    - provider \<String> - The OAuth login provider (e.g., 'google', 'naver', 'discord', 'apple').
    - clientId \<String> - The client ID of the OAuth login provider.
- email \<String> - __optional__ The email parameter allows users to log in using the specified
  email address when logging in through the widget.

> [!NOTE]
> For details on setting up OAuth providers, refer to
> the [OAuth Login Provider Setup section](#oauth-login-provider-setup).

#### Returns

- CompletableFuture\<WepinUser>
    - status \<'success'|'fail'>
    - userInfo \<WepinUserInfo> __optional__
        - userId \<String>
        - email \<String>
        - provider \<'google'|'apple'|'naver'|'discord'|'email'|'external_token'>
        - use2FA \<bool>
    - userStatus: \<WepinUserStatus> - The user's status in Wepin login, including:
        - loginStatus: \<'complete' | 'pinRequired' | 'registerRequired'> - If the
          user's `loginStatus` value is not complete, registration in Wepin is required.
        - pinRequired?: <bool>
    - walletId \<String> __optional__
    - token \<WepinToken> - Wepin Token

#### Exception

- [WepinError](#WepinError)

#### Example

```kotlin
// google, apple, discord, naver login
val providerInfos = listOf(
    LoginProviderInfo(provider = "google", clientId = "google-client-id"),
    LoginProviderInfo(provider = "apple", clientId = "apple-client-id"),
    LoginProviderInfo(provider = "discord", clientId = "discord-client-id"),
    LoginProviderInfo(provider = "naver", clientId = "naver-client-id"),
    LoginProviderInfo(provider = "facebook", clientId = "facebook-client-id"),
    LoginProviderInfo(provider = "line", clientId = "line-client-id"),
)

val userInfoRes = wepinWidget.loginWithUI(context, loginProviders)

// only email login
val userInfoRes = wepinWidget.loginWithUI(
    context,
    loginProviders: listOf()
)

//with specified email address
val userInfoRes = wepinWidget.loginWithUI(
    context,
    loginProviders: listOf(), email: 'abc@abc.com')

userInfoRes.thenApply {
    println("logged in: $it")
}.exceptionally {
    println("" Error : $ { it.message }")
    null
}
```

### openWidget

```kotlin
wepinWidget.openWidget(context: Context)
```

The `openWidget()` method displays the Wepin widget. If a user is not logged in, the widget will not
open. Therefore, you must log in to Wepin before using this method. To log in to Wepin, use
the `loginWithUI` method or `loginWepin` method from the `login` variable.

#### Parameters

- context \<Context> - The `Context` parameter is required in Android as it provides access to
  application-specific resources and classes, as well as information about the application
  environment. It is used to start new activities, access application assets, retrieve system
  services, and more.

When you call methods such as openWidget(context), make sure to pass the appropriate Context (such
as an `Activity` or `Application context`) to ensure proper operation. For UI-related actions (e.g.,
opening a new screen), an Activity context is recommended.

#### Returns

- CompletableFuture \<Boolean> - A `future` that completes when the widget is successfully opened.

#### Example

```kotlin
wepinWidget.openWidget(context)
```

### closeWidget

```kotlin
wepinWidget.closeWidget()
```

The `closeWidget()` method closes the Wepin widget.

#### Parameters

- None - This method does not take any parameters.

#### Returns

- None

#### Example

```kotlin
wepinWidget.closeWidget()
```

### register

```kotlin
wepinWidget.register(context:Context)
```

The `register` method registers the user with Wepin. After joining and logging in, this method opens
the Register page of the Wepin widget, allowing the user to complete registration (wipe and account
creation) for the Wepin service.

This method is only available if the lifecycle of the WepinSDK
is `WepinLifeCycle.loginBeforeRegister`. After calling the `loginWepin()` method in the `login`
variable, if the `loginStatus` value in the userStatus is not 'complete', this method must be
called.

#### Parameters

- context \<Context> - The `Context` parameter is required in Android as it provides access to
  application-specific resources and classes, as well as information about the application
  environment. It is used to start new activities, access application assets, retrieve system
  services, and more.

When you call methods such as register(context), make sure to pass the appropriate Context (such
as an `Activity` or `Application context`) to ensure proper operation. For UI-related actions (e.g.,
opening a new screen), an Activity context is recommended.

#### Returns

- CompletableFuture\<WepinUser>
    - status \<'success'|'fail'>
    - userInfo \<WepinUserInfo> __optional__
        - userId \<String>
        - email \<String>
        - provider \<'google'|'apple'|'naver'|'discord'|'email'|'external_token'>
        - use2FA \<bool>
    - userStatus: \<WepinUserStatus> - The user's status in Wepin login, including:
        - loginStatus: \<'complete' | 'pinRequired' | 'registerRequired'> - If the
          user's `loginStatus` value is not complete, registration in Wepin is required.
        - pinRequired?: <bool>
    - walletId \<String> __optional__
    - token \<WepinToken> - Wepin Token

#### Exception

- [WepinError](#WepinError)

#### Example

```kotlin
wepinWidget.register(context).thenApply {
    println("Registered: $it")
}.exceptionally {
    println("Error: ${it.message}")
}
```

### getAccounts

```kotlin
wepinWidget.getAccounts(networks:List< String >?, withEoa: Boolean?)
```

The `getAccounts()` method returns user accounts. It is recommended to use this method without
arguments to retrieve all user accounts. It can only be used after widget login.

##### Parameters

- networks: \<List\<String>> __optional__ A list of network names to filter the accounts.
- withEoa: \<Boolean> __optional__ Whether to include EOA accounts if AA accounts are included.

#### Returns

- CompletableFuture \<List\<WepinAccount>> - A future that resolves to a list of the user's
  accounts.
    - address \<String>
    - network \<String>
    - contract \<String> __optional__ The token contract address.
    - isAA \<bool> __optional__  Whether it is an AA account or not.

#### Exception

- [WepinError](#WepinError)

#### Example

```kotlin
wepinWidget.getAccounts().thenApply {
    println("getAccount: $it")
}.exceptionally {
    println"Error: ${it.message}")
}
```

- response

```kotlin
[
    WepinAccount(
        address = "0x0000001111112222223333334444445555556666",
        network = "Ethereum",
    ),
    WepinAccount(
        address = "0x0000001111112222223333334444445555556666",
        network = "Ethereum",
        contract = "0x777777888888999999000000111111222222333333",
    ),
    WepinAccount(
        address = "0x4444445555556666000000111111222222333333",
        network = "Ethereum",
        isAA = true,
    ),
]
```

### getBalance

```kotlin
wepinWidget.getBalance(accounts:List< WepinAccount >?)
```

The `getBalance()` method returns the balance information for specified accounts. It can only be
used after the widget is logged in. To get the balance information for all user accounts, use
the `getBalance()` method without any arguments.

#### Parameters

- accounts \<List\<WepinAccount>> __optional__ - A list of accounts for which to retrieve balance
  information.
    - network \<String> - The network associated with the account.
    - address \<String> - The address of the account.
    - isAA \<bool> __optional__ - Indicates whether the account is an AA (Account Abstraction)
      account.

#### Returns

- CompletableFuture \<List\<WepinAccountBalanceInfo>> - A future that resolves to a list of balance
  information for the specified accounts.
    - network \<String> - The network associated with the account.
    - address \<String> - The address of the account.
    - symbol \<String> - The symbol of the account's balance.
    - balance \<String> - The balance of the account.
    - tokens \<List\<WepinTokenBalanceInfo>> - A list of token balance information for the account.
        - symbol \<String> - The symbol of the token.
        - balance \<String> - The balance of the token.
        - contract \<String> - The contract address of the token.

#### Exception

- [WepinError](#WepinError)

#### Example

```kotlin
wepinWidget.getBalance().thenApply {
    println("getBalance in: $it")
}.exceptionally {
    println("Error: ${it.message}")
}
```

- response

```kotlin
[
    WepinAccountBalanceInfo(
        network = "Ethereum",
        address = "0x0000001111112222223333334444445555556666",
        symbol = "ETH",
        balance = "1.1",
        tokens = [
            WepinTokenBalanceInfo(
                contract = "0x123...213",
                symbol = "TEST",
                balance = "10"
            ),
        ]
    )
]
```

### getNFTs

```kotlin
wepinWidget.getNFTs(refresh:Boolean, networks: List< String >?})
```

The `getNFTs()` method returns user NFTs. It is recommended to use this method without the networks
argument to get all user NFTs. This method can only be used after the widget is logged in.

##### Parameters

- refresh \<Boolean> - A required parameter to indicate whether to refresh the NFT data.
- networks \<List\<String>> __optional__ - A list of network names to filter the NFTs.

#### Returns

- CompletableFuture \<List\<WepinNFT>> - A future that resolves to a list of the user's NFTs.
    - account \<WepinAccount>
        - address \<String> - The address of the account associated with the NFT.
        - network \<String> - The network associated with the NFT.
        - contract \<String> __optional__ The token contract address.
        - isAA \<bool> __optional__ Indicates whether the account is an AA (Account Abstraction)
          account.
    - contract \<WepinNFTContract>
        - name \<String> - The name of the NFT contract.
        - address \<String> - The contract address of the NFT.
        - scheme \<String> - The scheme of the NFT.
        - description \<String> __optional__ - A description of the NFT contract.
        - network \<String> - The network associated with the NFT contract.
        - externalLink \<String> __optional__  - An external link associated with the NFT contract.
        - imageUrl \<String> __optional__ - An image URL associated with the NFT contract.
    - name \<String> - The name of the NFT.
    - description \<String> - A description of the NFT.
    - externalLink \<String> - An external link associated with the NFT.
    - imageUrl \<String> - An image URL associated with the NFT.
    - contentUrl \<String> __optional__ - A URL pointing to the content associated with the NFT.
    - quantity \<int> - The quantity of the NFT.
    - contentType \<String> - The content type of the NFT.
    - state \<int> - The state of the NFT.

#### Exception

- [WepinError](#WepinError)

#### Example

```kotlin
wepinWidget.getNFTs(false).thenApply {
    println("getNFTs in: $it")
}.exceptionally {
    pirntln("Error: ${it.message}")
}
```

- response

```kotlin
[
    WepinNFT(
        account = WepinAccount(
            address = "0x0000001111112222223333334444445555556666",
            network = "Ethereum",
            contract = "0x777777888888999999000000111111222222333333",
            isAA = true,
        ),
        contract = WepinNFTContract(
            name = "NFT Collection",
            address = "0x777777888888999999000000111111222222333333",
            scheme = "ERC721",
            description = "An example NFT collection",
            network = "Ethereum",
            externalLink = "https://example.com",
            imageUrl = "https://example.com/image.png",
        ),
        name = "Sample NFT",
        description = "A sample NFT description",
        externalLink = "https://example.com/nft",
        imageUrl = "https://example.com/nft-image.png",
        contentUrl = "https://example.com/nft-content.png",
        quantity = 1,
        contentType = "image/png",
        state = 0,
    ),
]
```

### send

```kotlin
wepinWidget.send(context: Context, account:WepinAccount, txData: WepinTxData?)
```

The `send()` method sends a transaction and returns the transaction ID information. This method can
only be used after the widget is logged in.

#### Parameters

- context \<Context> - The `Context` parameter is required in Android as it provides access to
  application-specific resources and classes, as well as information about the application
  environment. It is used to start new activities, access application assets, retrieve system
  services, and more.

When you call methods such as send(), make sure to pass the appropriate Context (such
as an `Activity` or `Application context`) to ensure proper operation. For UI-related actions (e.g.,
opening a new screen), an Activity context is recommended.

- account \<WepinAccount> - The account from which the transaction will be sent.
    - network \<String> - The network associated with the account.
    - address \<String>  - The address of the account.
    - contract \<String> __optional__ The contract address of the token.
- txData \<WepinTxData> __optional__ - The transaction data to be sent.
    - to \<String> - The address to which the transaction is being sent.
    - amount \<String> - The amount of the transaction.

#### Returns

- Future \<WepinSendResponse> - A future that resolves to a response containing the transaction ID.
    - txId \<String> - The ID of the sent transaction.

#### Exception

- [WepinError](#WepinError)

#### Example

```kotlin
wepinWidget.send(
    context, WepinAccount(
        address = "0x0000001111112222223333334444445555556666",
        network = "Ethereum",
    )
)
    .thenApply { result -> println("Send success: $result") }
    .exceptionally { error ->
        println("Error: ${error.message}")
    }

// token send
wepinWidget.send(
    context, WepinAccount(
        address = "0x0000001111112222223333334444445555556666",
        network = "Ethereum",
        contract: "0x9999991111112222223333334444445555556666"
))
.thenApply { result -> println("Send success: $result") }
    .exceptionally { error ->
        println("Error: ${error.message}")
    }

// with TxData
wepinWidget.send(
    context, WepinAccount(
        address = "0x0000001111112222223333334444445555556666",
        network = "Ethereum",
    ),
    WepinTxData(
        to = "0x9999991111112222223333334444445555556666",
        amount = "0.1",
    )
)
    .thenApply { result -> println("Send success: $result") }
    .exceptionally { error ->
        println("Error: ${error.message}")
    }
```

- response

```kotlin
WepinSendResponse(
    txId = "0x76bafd4b700ed959999d08ab76f95d7b6ab2249c0446921c62a6336a70b84f32"
)
```

### receive

```kotlin
wepinWidget.receive(context: Context, account:WepinAccount)
```

The `receive` method opens the account information page associated with the specified account. This
method can only be used after logging into Wepin.

#### Parameters

- context \<Context> - The `Context` parameter is required in Android as it provides access to
  application-specific resources and classes, as well as information about the application
  environment. It is used to start new activities, access application assets, retrieve system
  services, and more.

When you call methods such as receive(), make sure to pass the appropriate Context (such
as an `Activity` or `Application context`) to ensure proper operation. For UI-related actions (e.g.,
opening a new screen), an Activity context is recommended.

- account \<WepinAccount> - Provides the account information for the page that will be opened.
    - network \<String> - The network associated with the account.
    - address \<String>  - The address of the account.
    - contract \<String> __optional__ The contract address of the token.

#### Returns

- Future \<WepinReceiveResponse> - A future that resolves to a `WepinReceiveResponse` object
  containing the information about the opened account.
    - account \<WepinAccount> - The account information of the page that was opened.
        - network \<String> - The network associated with the account.
        - address \<String>  - The address of the account.
        - contract \<String> __optional__ The contract address of the token.

#### Exception

- [WepinError](#WepinError)

#### Example

```kotlin
// Opening an account page
wepinWidget.receive(
    context, WepinAccount(
        address = "0x0000001111112222223333334444445555556666",
        network = "Ethereum"
    )
)
    .thenApply { result -> println("Receive success: $result") }
    .exceptionally { error ->
        println("Error: ${error.message}")
    }

// Opening a token page
wepinWidget.receive(
    context, WepinAccount(
        address = "0x0000001111112222223333334444445555556666",
        network = "Ethereum",
        contract = "0x9999991111112222223333334444445555556666"
    )
)
    .thenApply { result -> println("Receive success: $result") }
    .exceptionally { error ->
        println("Error: ${error.message}")
    }
```

- response

```kotlin
WepinReceiveResponse(
    account = WepinAccount(
        address = "0x0000001111112222223333334444445555556666",
        network = "Ethereum",
        contract = "0x9999991111112222223333334444445555556666"
    )
)
```

### finalize

```kotlin
wepinWidget.finalize()
```

The `finalize()` method finalizes the Wepin SDK, releasing any resources or connections it has
established.

#### Parameters

- None - This method does not take any parameters.
-

#### Returns

- CompletableFuture\<Boolean> - A future that completes when the SDK has been finalized.

#### Example

```kotlin
wepinWidget.finalize()
```

### WepinError

This section provides descriptions of various error codes that may be encountered while using the
Wepin SDK functionalities. Each error code corresponds to a specific issue, and understanding these
can help in debugging and handling errors effectively.

| Error Code                      | Error Message                      | Error Description                                                                                                                                                                                      |
|---------------------------------|------------------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `INVALID_APP_KEY`          	    | "Invalid app key"            	     | The Wepin app key is invalid.                                                                                                                                                                        	 |
| `INVALID_PARAMETER` `    	      | "Invalid parameter" 	 	            | One or more parameters provided are invalid or missing.                                                                                                                                              	 |
| `INVALID_LOGIN_PROVIDER`        | "Invalid login provider"     	     | The login provider specified is not supported or is invalid.                                                                                                                                         	 |
| `INVALID_TOKEN`                 | "InvalidToken"            	        | The token does not exist.                                                                                                                                                                            	 |
| `INVALID_LOGIN_SESSION`    	    | "InvalidLoginSession"      	       | The login session information does not exist.                                                                                                                                                        	 |
| `NOT_INITIALIZED_ERROR`         | "NotInitialized"       	           | The WepinLoginLibrary has not been properly initialized.                                                                                                                                             	 |
| `ALREADY_INITIALIZED_ERROR`     | "AlreadyInitialized"               | The WepinLoginLibrary is already initialized, so the logout operation cannot be performed again.                                                                                                     	 |
| `USER_CANCELLED`           	    | "User cancelled"           	       | The user has cancelled the operation.                                                                                                                                                                	 |
| `UNKNOWN_ERROR`            	    | "UnKnown Error"       	            | An unknown error has occurred, and the cause is not identified.                                                                                                                                      	 |
| `NOT_CONNECTED_INTERNET`   	    | "No internet connection"     	     | The system is unable to detect an active internet connection.                                                                                                                                        	 |
| `FAILED_LOGIN`             	    | "Failed Oauth log in"     	 	      | The login attempt has failed due to incorrect credentials or other issues.                                                                                                                           	 |
| `ALREADY_LOGOUT`           	    | "Already logged out"           	   | The user is already logged out, so the logout operation cannot be performed again.                                                                                                                   	 |
| `INVALID_EMAIL_DOMAIN`     	    | "Invalid email domain"       	     | The provided email address's domain is not allowed or recognized by the system.                                                                                                                      	 |
| `FAILED_SEND_EMAIL`         	   | "Failed to send email"           	 | The system encountered an error while sending an email. This is because the email address is invalid or we sent verification emails too often. Please change your email or try again after 1 minute. 	 |
| `REQUIRED_EMAIL_VERIFIED`  	    | "Email verification required"      | Email verification is required to proceed with the requested operation.                                                                                                                              	 |
| `INCORRECT_EMAIL_FORM`      	   | "Incorrect email form"        	    | The provided email address does not match the expected format.                                                                                                                                      	  |
| `INCORRECT_PASSWORD_FORM`   	   | "Incorrect password form"     	    | The provided password does not meet the required format or criteria.                                                                                                                                 	 |
| `NOT_INITIALIZED_NETWORK`       | "Network Manager not initialized." | The network or connection required for the operation has not been properly initialized.                                                                                                              	 |
| `REQUIRED_SIGNUP_EMAIL`     	   | "Email required for sign up"       | The user needs to sign up with an email address to proceed.                                                                                                                                          	 |
| `FAILED_EMAIL_VERIFICATION`     | "Failed email verification"        | The WepinLoginLibrary encountered an issue while attempting to verify the provided email address.                                                                                                    	 |
| `FAILED_PASSWORD_STATE_SETTING` | "Failed password state setting"    | Failed to set the password state. This error may occur during password management operations, potentially due to invalid input or system issues.                                                   	   |
| `FAILED_PASSWORD_SETTING` 	     | "Failed password setting"          | Failed to set the password. This could be due to issues with the provided password or internal errors during the password setting process.                                                             |
| `EXISTED_EMAIL`                 | "Existed email"           	        | The provided email address is already registered. This error occurs when attempting to sign up with an email that is already in use.                               					                               |
| `API_REQUEST_ERROR`             | "API Request Error"                | There was an error while making the API request. This can happen due to network issues, invalid endpoints, or server errors.                                                                           |
| `INCORRECT_LIFE_CYCLE`          | "Incorrect Life Cycle"             | The lifecycle of the Wepin SDK is incorrect for the requested operation. Ensure that the SDK is in the correct state (e.g., `initialized` and `login`) before proceeding.                              |
| `FAILED_REGISTER`               | "Failed Register"           	      | Failed to register the user. This can occur due to issues with the provided registration details or internal errors during the registration process.                                                   |
| `ACCOUNT_NOT_FOUND`             | "Account not found"           	    | The specified account was not found. This error is returned when attempting to access an account that does not exist in the Wepin.                                                                     |
| `NFT_NOT_FOUND`             	   | "NFT not found"           	        | The specified NFT was not found. This error occurs when the requested NFT does not exist or is not accessible within the user's account.                                                               |
| `NO_BALANCES_FOUND`             | "Balances not found"               | No balance information was found for the requested accounts. This error can occur if the user has no funds or if there was an issue retrieving the balance data.                                       
| `FAILED_SEND`             	     | "Failed to send"           	       | Failed to send the required data or request. This error could be due to network issues, incorrect data, or internal server errors.                                                                     |
| `FAILED_RECEIVE`                | "Failed to receive"                | Failed to receive the required data or request. This error could be due to network issues, incorrect data, or internal server errors.                                                                  
| `NOT_ACTIVITY`                  | "Context is not activity"          | The provided context is not an instance of an activity. This error occurs when an activity context is required but a different type of context was provided.                                           


