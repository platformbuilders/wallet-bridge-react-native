package com.google.android.gms_mock

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms_mock.ui.theme.GoogleWalletMockTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "GoogleWalletMock"
        private const val REQUEST_CODE_APP2APP = 1001
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoogleWalletMockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App2AppSimulator(
                        onSimulateClick = { simulateApp2App() },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
    
    private fun simulateApp2App() {
        try {
            // Dados simulados em base64
            val simulatedData = "eyJ0b2tlbiI6InNpbXVsYXRlZF90b2tlbl8xMjM0NTY3ODkwIiwiYWN0aXZhdGlvbl9kYXRhIjp7InVzZXJfaWQiOiIxMjM0NTY3ODkwIiwiY2FyZF9pZCI6ImNhcmRfYWJjZGVmZ2hpaiIsInRpbWVzdGFtcCI6MTcwMzA0ODAwMDAwMH19"
            
            val intent = Intent("br.com.pefisa.pefisa.hml.action.ACTIVATE_TOKEN").apply {
                setPackage("br.com.pefisa.pefisa.hml")
                putExtra(Intent.EXTRA_TEXT, simulatedData)
            }
            
            // Tentar iniciar o app diretamente
            try {
                startActivityForResult(intent, REQUEST_CODE_APP2APP)
                Log.d(TAG, "🚀 [GOOGLE] App Pefisa iniciado com sucesso")
            } catch (e: Exception) {
                Log.e(TAG, "❌ [GOOGLE] Erro ao iniciar app: ${e.message}")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao simular App 2 App: ${e.message}")
        }
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        when (requestCode) {
            REQUEST_CODE_APP2APP -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        Log.d(TAG, "✅ [GOOGLE] App Pefisa retornou com sucesso")
                        data?.let { resultData ->
                            Log.d(TAG, "📄 [GOOGLE] Dados retornados: ${resultData.extras}")
                        }
                    }
                    Activity.RESULT_CANCELED -> {
                        Log.w(TAG, "⚠️ [GOOGLE] App Pefisa foi cancelado pelo usuário")
                    }
                    else -> {
                        Log.w(TAG, "⚠️ [GOOGLE] App Pefisa retornou com código: $resultCode")
                    }
                }
            }
        }
    }
}

@Composable
fun App2AppSimulator(
    onSimulateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Google Wallet Mock",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        Button(
            onClick = onSimulateClick
        ) {
            Text("Simular App 2 App")
        }
        
        Text(
            text = "Package: br.com.pefisa.pefisa.hml",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )
        
        Text(
            text = "Action: ACTIVATE_TOKEN",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun App2AppSimulatorPreview() {
    GoogleWalletMockTheme {
        App2AppSimulator(
            onSimulateClick = { }
        )
    }
}