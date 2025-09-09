package com.builders.wallet

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReadableMap

interface WalletModuleInterface {
    // Check wallet availability and initialize
    fun checkWalletAvailability(promise: Promise)
    
    // Get wallet information for secure transactions
    fun getSecureWalletInfo(promise: Promise)
    
    // Get card status by last digits
    fun getCardStatusBySuffix(lastDigits: String, promise: Promise)
    
    // Get card status by identifier (Token Reference ID for Android)
    fun getCardStatusByIdentifier(identifier: String, tsp: String, promise: Promise)
    
    // Add card to wallet (Push Provisioning)
    fun addCardToWallet(cardData: ReadableMap, promise: Promise)
    
    // Utility methods
    fun getConstants(): MutableMap<String, Any>
    fun getName(): String
    fun isAvailable(): Boolean
}