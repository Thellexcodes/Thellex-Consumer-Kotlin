package com.thellex.pos.settings


// --- Basic Settings ---
const val ENVIRONMENT = "beta"
const val DEBUG_MODE = true
const val LOG_LEVEL = "debug" // 'info' in staging, 'warn' in production

const val SHOW_BETA_BANNER = true
const val ENABLE_FEEDBACK_BUTTON = true

// --- Countries / Access Control ---
val SUPPORTED_COUNTRIES = listOf("NG") // Start with Nigeria
const val ENABLE_WHITELISTED_USERS_ONLY = true // Recommended for beta
const val ALLOW_TRANSACTIONS = true

// --- Feature Flags ---
const val ENABLE_CRYPTO_DEPOSITS = true
const val ENABLE_CRYPTO_WITHDRAWALS = false // Disable to avoid testnet leaks
const val ENABLE_TOKEN_SWAP = false
const val ENABLE_ESCROW_SERVICE = true
const val ENABLE_WALLET_CREATION = true

// --- Transaction Limits ---
const val MIN_TRANSACTION_AMOUNT = 1  // $1 minimum
const val MAX_TRANSACTION_AMOUNT = 1000
const val DAILY_LIMIT_PER_USER = 5000

// --- Security & KYC ---
const val ENABLE_KYC = true // Connect to sandbox KYC APIs
const val ENABLE_DEVICE_VERIFICATION = false // Optional
const val ENABLE_EMAIL_VERIFICATION = true

// --- Monitoring ---
const val ENABLE_TRANSACTION_LOGGING = true
const val ENABLE_TEST_MODE_RECEIPTS = true // Fake receipt for beta
const val ENABLE_EMAIL_ALERTS = false
const val ENABLE_ADMIN_OVERRIDE = true // Allow manual fix/debug

// --- Developer Only ---
object DevModeTools {
    const val enableFakeWallets = true
    const val enableSandboxPayments = true
    const val allowManualTokenCredit = true
    const val enableWebhookLogging = true
}

// --- Blockchain Types ---
enum class SupportedBlockchain {
    LISK, BASE, STELLAR, POLYGON, TRON, SOLANA, BEP20
}

// --- Blockchain Network Support ---
val SUPPORTED_BLOCKCHAINS = listOf(
    SupportedBlockchain.LISK,
    SupportedBlockchain.BASE,
    SupportedBlockchain.STELLAR,
    SupportedBlockchain.POLYGON,
    SupportedBlockchain.TRON,
    SupportedBlockchain.SOLANA
)

// --- Token Support ---
enum class Token {
    USDC, USDT, XLM, BTC
}

// --- Chain Tokens Support ---
val ChainTokens: Map<String, List<Token>> = mapOf(
    "lisk" to listOf(Token.USDC, Token.USDT),
    "base" to listOf(Token.USDC),
    "stellar" to listOf(Token.USDC, Token.XLM),
    "polygon" to listOf(Token.USDC),
    "tron" to listOf(Token.USDC, Token.USDT),
    "solana" to listOf(Token.USDC, Token.USDT),
    "bep20" to listOf(Token.USDC, Token.USDT)
)

// --- Testnet Flag ---
const val USE_TESTNET = true

// --- Fee Types ---
enum class Fee {
    FLAT,
    PERCENTAGE
}


//--- Payment Types ---
enum class PaymentType {
    REQUEST_FIAT,
    WITHDRAW_FIAT,
    REQUEST_CRYPTO,
}