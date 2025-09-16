package com.google.android.gms_mock

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms_mock.ui.theme.GoogleWalletMockTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "GoogleWalletMock"
    }
    
    // Variável para controlar o estado do alerta
    private var alertState by mutableStateOf(AlertState())
    
    data class AlertState(
        val show: Boolean = false,
        val title: String = "",
        val message: String = "",
        val resultCode: Int = -1
    )
    
    // ActivityResultLauncher para substituir o deprecated onActivityResult
    private val app2AppLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d(TAG, "✅ [GOOGLE] App Pefisa retornou com sucesso")
                result.data?.let { resultData ->
                    Log.d(TAG, "📄 [GOOGLE] Dados retornados: ${resultData.extras}")
                }
                showAlert(
                    title = "✅ Sucesso",
                    message = "App Pefisa retornou com sucesso!\n\nCódigo: ${result.resultCode}\nDados: ${result.data?.extras}",
                    resultCode = result.resultCode
                )
            }
            Activity.RESULT_CANCELED -> {
                Log.w(TAG, "⚠️ [GOOGLE] App Pefisa foi cancelado pelo usuário")
                showAlert(
                    title = "⚠️ Cancelado",
                    message = "App Pefisa foi cancelado pelo usuário.\n\nCódigo: ${result.resultCode}",
                    resultCode = result.resultCode
                )
            }
            else -> {
                Log.w(TAG, "⚠️ [GOOGLE] App Pefisa retornou com código: ${result.resultCode}")
                showAlert(
                    title = "⚠️ Resultado Inesperado",
                    message = "App Pefisa retornou com código inesperado.\n\nCódigo: ${result.resultCode}",
                    resultCode = result.resultCode
                )
            }
        }
    }
    
    private fun showAlert(title: String, message: String, resultCode: Int) {
        alertState = AlertState(
            show = true,
            title = title,
            message = message,
            resultCode = resultCode
        )
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
                
                // AlertDialog para mostrar o resultado
                if (alertState.show) {
                    AlertDialog(
                        onDismissRequest = { 
                            alertState = alertState.copy(show = false)
                        },
                        title = {
                            Text(text = alertState.title)
                        },
                        text = {
                            Text(text = alertState.message)
                        },
                        confirmButton = {
                            TextButton(
                                onClick = { 
                                    alertState = alertState.copy(show = false)
                                }
                            ) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }

    private fun simulateApp2App() {
        try {
            // Gerar dados simulados com panReferenceId e tokenReferenceId
            val simulatedJson = generateSimulatedData()
            val simulatedData = android.util.Base64.encodeToString(
                simulatedJson.toByteArray(Charsets.UTF_8),
                android.util.Base64.NO_WRAP
            )

            Log.d(TAG, "📋 [GOOGLE] JSON gerado: $simulatedJson")
            Log.d(TAG, "📋 [GOOGLE] Base64 gerado: $simulatedData")

            val intent = Intent("br.com.pefisa.pefisa.hml.action.ACTIVATE_TOKEN").apply {
                setPackage("br.com.pefisa.pefisa.hml")
                putExtra(Intent.EXTRA_TEXT, simulatedData)
            }

            // Tentar iniciar o app diretamente usando ActivityResultLauncher
            try {
                app2AppLauncher.launch(intent)
                Log.d(TAG, "🚀 [GOOGLE] App Pefisa iniciado com sucesso")
            } catch (e: Exception) {
                Log.e(TAG, "❌ [GOOGLE] Erro ao iniciar app: ${e.message}")
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao simular App 2 App: ${e.message}")
        }
    }

    private fun generateSimulatedData(): String {
        val timestamp = System.currentTimeMillis()
        val panReferenceId = "PAN_${timestamp}_${(1000..9999).random()}"
        val tokenReferenceId = "TOKEN_${timestamp}_${(10000..99999).random()}"

        return """
        {
            "panReferenceId": "$panReferenceId",
            "tokenReferenceId": "$tokenReferenceId",
        }
        """.trimIndent()
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
