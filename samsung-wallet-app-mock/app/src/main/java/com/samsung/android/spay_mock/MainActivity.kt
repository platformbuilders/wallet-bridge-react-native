package com.samsung.android.spay_mock

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samsung.android.spay_mock.ui.theme.SamsungwalletappmockTheme

class MainActivity : ComponentActivity() {
    companion object {
        private const val TAG = "SamsungWalletMock"
        internal const val DEFAULT_SIMULATED_DATA = "9e4eeb4e-71af-4024-b3ff-05c7a2d4460d"
    }

    // Estado de alerta
    private var alertState by mutableStateOf(AlertState())

    // Estado de resultado Samsung
    private var samsungResultState by mutableStateOf(SamsungResultState())

    // Estado do input de dados simulados
    private var simulatedDataInput by mutableStateOf(DEFAULT_SIMULATED_DATA)

    data class AlertState(
        val show: Boolean = false,
        val title: String = "",
        val message: String = "",
        val resultCode: Int = -1
    )

    data class SamsungResultState(
        val hasResult: Boolean = false,
        val stepUpResponse: String? = null,
        val activationCode: String? = null,
        val resultCode: Int = -1,
        val timestamp: String = ""
    )

    // ActivityResultLauncher para Samsung Wallet
    private val samsungApp2AppLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d(TAG, "✅ [SAMSUNG] App example retornou com sucesso")

                val stepUpResponse = result.data?.getStringExtra("STEP_UP_RESPONSE")
                val activationCode = result.data?.getStringExtra("ACTIVATION_CODE")

                Log.d(TAG, "📄 [SAMSUNG] Step Up Response: $stepUpResponse")
                Log.d(TAG, "📄 [SAMSUNG] Activation Code: $activationCode")
                Log.d(TAG, "📄 [SAMSUNG] Todos os extras: ${result.data?.extras}")

                samsungResultState = SamsungResultState(
                    hasResult = true,
                    stepUpResponse = stepUpResponse,
                    activationCode = activationCode,
                    resultCode = result.resultCode,
                    timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                )

                val message = buildSamsungActivationMessage(stepUpResponse, activationCode, result.resultCode)

                showAlert(
                    title = "✅ Sucesso Samsung",
                    message = message,
                    resultCode = result.resultCode
                )
            }
            Activity.RESULT_CANCELED -> {
                Log.w(TAG, "⚠️ [SAMSUNG] App example foi cancelado pelo usuário")
                showAlert(
                    title = "⚠️ Cancelado Samsung",
                    message = "App example Samsung foi cancelado pelo usuário.\n\nCódigo: ${result.resultCode}",
                    resultCode = result.resultCode
                )
            }
            else -> {
                Log.w(TAG, "⚠️ [SAMSUNG] App example retornou com código: ${result.resultCode}")
                showAlert(
                    title = "⚠️ Resultado Inesperado Samsung",
                    message = "App example Samsung retornou com código inesperado.\n\nCódigo: ${result.resultCode}",
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

    private fun buildSamsungActivationMessage(stepUpResponse: String?, activationCode: String?, resultCode: Int): String {
        val message = StringBuilder()
        message.append("App example Samsung retornou com sucesso!\n\n")
        message.append("Código de Resultado: $resultCode\n\n")

        when (stepUpResponse) {
            "accepted" -> {
                message.append("✅ Status: ACEITO\n")
                if (!activationCode.isNullOrEmpty()) {
                    message.append("🔑 Código de Ativação: $activationCode\n")
                } else {
                    message.append("ℹ️ Sem código de ativação\n")
                }
                message.append("\n🎉 Token Samsung ativado com sucesso!")
            }
            "declined" -> {
                message.append("❌ Status: RECUSADO\n")
                message.append("\n🚫 Ativação do token Samsung foi recusada")
            }
            "failure" -> {
                message.append("💥 Status: FALHA\n")
                message.append("\n⚠️ Falha na ativação do token Samsung")
            }
            "appNotReady" -> {
                message.append("⚠️ Status: APP NÃO PRONTO\n")
                message.append("\n⏳ App não está pronto para ativação")
            }
            null -> {
                message.append("⚠️ Status: NÃO INFORMADO\n")
                message.append("\n❓ Nenhum status de ativação Samsung foi retornado")
            }
            else -> {
                message.append("❓ Status: DESCONHECIDO ($stepUpResponse)\n")
                message.append("\n⚠️ Status de ativação Samsung não reconhecido")
            }
        }

        return message.toString()
    }

    private fun simulateSamsungApp2App(simulatedData: String) {
        try {
            Log.d(TAG, "📋 [SAMSUNG] Simulated Data: $simulatedData")

            val intent = Intent(BuildConfig.SAMSUNG_TARGET_APP_ACTION).apply {
                setPackage(BuildConfig.SAMSUNG_TARGET_APP_PACKAGE)
                putExtra(Intent.EXTRA_TEXT, simulatedData)
            }

            try {
                samsungApp2AppLauncher.launch(intent)
                Log.d(TAG, "🚀 [SAMSUNG] App example iniciado com sucesso")
            } catch (e: Exception) {
                Log.e(TAG, "❌ [SAMSUNG] Erro ao iniciar app: ${e.message}")
                showAlert(
                    title = "❌ Erro ao Abrir App Samsung",
                    message = "Não foi possível abrir o app example Samsung.\n\nErro: ${e.message}\n\nPackage: ${BuildConfig.SAMSUNG_TARGET_APP_PACKAGE}\nAction: ${BuildConfig.SAMSUNG_TARGET_APP_ACTION}",
                    resultCode = -1
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ [SAMSUNG] Erro ao simular App 2 App: ${e.message}")
            showAlert(
                title = "❌ Erro Geral Samsung",
                message = "Erro inesperado ao simular App 2 App Samsung.\n\nErro: ${e.message}\n\nVerifique se o package está correto e se o app está instalado.",
                resultCode = -1
            )
        }
    }

    private fun clearResults() {
        samsungResultState = SamsungResultState()
        Log.d(TAG, "🧹 [SAMSUNG] Resultados limpos")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SamsungwalletappmockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SamsungApp2AppSimulator(
                        simulatedDataInput = simulatedDataInput,
                        onSimulatedDataChange = { simulatedDataInput = it },
                        onSamsungSimulateClick = { simulateSamsungApp2App(simulatedDataInput) },
                        onClearClick = { clearResults() },
                        samsungResultState = samsungResultState,
                        modifier = Modifier.padding(innerPadding)
                    )
                }

                if (alertState.show) {
                    AlertDialog(
                        onDismissRequest = { alertState = alertState.copy(show = false) },
                        title = { Text(text = alertState.title) },
                        text = { Text(text = alertState.message) },
                        confirmButton = {
                            TextButton(onClick = { alertState = alertState.copy(show = false) }) {
                                Text("OK")
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SamsungApp2AppSimulator(
  simulatedDataInput: String,
  onSimulatedDataChange: (String) -> Unit,
  onSamsungSimulateClick: () -> Unit,
  onClearClick: () -> Unit,
  samsungResultState: MainActivity.SamsungResultState,
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
            text = "Samsung Wallet Mock",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        TextField(
            value = simulatedDataInput,
            onValueChange = onSimulatedDataChange,
            label = { Text("Token Reference ID") },
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyMedium,
            trailingIcon = {
                if (simulatedDataInput.isNotEmpty()) {
                    IconButton(onClick = { onSimulatedDataChange("") }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpar"
                        )
                    }
                }
            }
        )

        Button(
            onClick = onSamsungSimulateClick,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF1428A0)
            )
        ) {
            Text("Simular Samsung Wallet App 2 App")
        }

        Text(
            text = "Package: ${BuildConfig.SAMSUNG_TARGET_APP_PACKAGE}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp)
        )

        Text(
            text = "Action: LAUNCH_A2A_IDV",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 4.dp)
        )

        if (samsungResultState.hasResult) {
            SamsungResultDisplay(
                resultState = samsungResultState,
                onClearClick = onClearClick,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

@Composable
fun SamsungResultDisplay(
  resultState: MainActivity.SamsungResultState,
  onClearClick: () -> Unit,
  modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "📋 Resultado Samsung Wallet",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFFE3F2FD)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "⏰ Timestamp: ${resultState.timestamp}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "📊 Código: ${resultState.resultCode}",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                when (resultState.stepUpResponse) {
                    "accepted" -> {
                        Text(
                            text = "✅ Status: ACEITO",
                            style = MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.ui.graphics.Color(0xFF4CAF50),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        if (!resultState.activationCode.isNullOrEmpty()) {
                            Text(
                                text = "🔑 Código: ${resultState.activationCode}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                    "declined" -> {
                        Text(
                            text = "❌ Status: RECUSADO",
                            style = MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.ui.graphics.Color(0xFFFF9800),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    "failure" -> {
                        Text(
                            text = "💥 Status: FALHA",
                            style = MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.ui.graphics.Color(0xFFF44336),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    "appNotReady" -> {
                        Text(
                            text = "⚠️ Status: APP NÃO PRONTO",
                            style = MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.ui.graphics.Color(0xFF9E9E9E),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    null -> {
                        Text(
                            text = "⚠️ Status: NÃO INFORMADO",
                            style = MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.ui.graphics.Color(0xFFFF9800),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                    else -> {
                        Text(
                            text = "❓ Status: ${resultState.stepUpResponse}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.ui.graphics.Color(0xFF9E9E9E),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }

        Button(
            onClick = onClearClick,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF1428A0)
            )
        ) {
            Text("🧹 Limpar Resultados Samsung")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SamsungApp2AppSimulatorPreview() {
    SamsungwalletappmockTheme {
        SamsungApp2AppSimulator(
            simulatedDataInput = MainActivity.DEFAULT_SIMULATED_DATA,
            onSimulatedDataChange = { },
            onSamsungSimulateClick = { },
            onClearClick = { },
            samsungResultState = MainActivity.SamsungResultState()
        )
    }
}