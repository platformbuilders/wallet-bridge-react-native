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
import androidx.compose.foundation.layout.fillMaxWidth
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

    // Variável para controlar o estado do resultado na tela
    private var resultState by mutableStateOf(ResultState())

    data class AlertState(
        val show: Boolean = false,
        val title: String = "",
        val message: String = "",
        val resultCode: Int = -1
    )

    data class ResultState(
        val hasResult: Boolean = false,
        val activationResponse: String? = null,
        val activationCode: String? = null,
        val resultCode: Int = -1,
        val timestamp: String = ""
    )

    // ActivityResultLauncher para substituir o deprecated onActivityResult
    private val app2AppLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            Activity.RESULT_OK -> {
                Log.d(TAG, "✅ [GOOGLE] App example retornou com sucesso")

                // Processar extras de ativação
                val activationResponse = result.data?.getStringExtra("BANKING_APP_ACTIVATION_RESPONSE")
                val activationCode = result.data?.getStringExtra("BANKING_APP_ACTIVATION_CODE")

                Log.d(TAG, "📄 [GOOGLE] Activation Response: $activationResponse")
                Log.d(TAG, "📄 [GOOGLE] Activation Code: $activationCode")
                Log.d(TAG, "📄 [GOOGLE] Todos os extras: ${result.data?.extras}")

                // Atualizar estado do resultado na tela
                resultState = ResultState(
                    hasResult = true,
                    activationResponse = activationResponse,
                    activationCode = activationCode,
                    resultCode = result.resultCode,
                    timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
                )

                // Construir mensagem baseada no status de ativação
                val message = buildActivationMessage(activationResponse, activationCode, result.resultCode)

                showAlert(
                    title = "✅ Sucesso",
                    message = message,
                    resultCode = result.resultCode
                )
            }
            Activity.RESULT_CANCELED -> {
                Log.w(TAG, "⚠️ [GOOGLE] App example foi cancelado pelo usuário")
                showAlert(
                    title = "⚠️ Cancelado",
                    message = "App example foi cancelado pelo usuário.\n\nCódigo: ${result.resultCode}",
                    resultCode = result.resultCode
                )
            }
            else -> {
                Log.w(TAG, "⚠️ [GOOGLE] App example retornou com código: ${result.resultCode}")
                showAlert(
                    title = "⚠️ Resultado Inesperado",
                    message = "App example retornou com código inesperado.\n\nCódigo: ${result.resultCode}",
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

    private fun buildActivationMessage(activationResponse: String?, activationCode: String?, resultCode: Int): String {
        val message = StringBuilder()
        message.append("App example retornou com sucesso!\n\n")
        message.append("Código de Resultado: $resultCode\n\n")

        when (activationResponse) {
            "approved" -> {
                message.append("✅ Status: APROVADO\n")
                if (!activationCode.isNullOrEmpty()) {
                    message.append("🔑 Código de Ativação: $activationCode\n")
                } else {
                    message.append("ℹ️ Sem código de ativação\n")
                }
                message.append("\n🎉 Token ativado com sucesso!")
            }
            "declined" -> {
                message.append("❌ Status: RECUSADO\n")
                message.append("\n🚫 Ativação do token foi recusada")
            }
            "failure" -> {
                message.append("💥 Status: FALHA\n")
                message.append("\n⚠️ Falha na ativação do token")
            }
            null -> {
                message.append("⚠️ Status: NÃO INFORMADO\n")
                message.append("\n❓ Nenhum status de ativação foi retornado")
            }
            else -> {
                message.append("❓ Status: DESCONHECIDO ($activationResponse)\n")
                message.append("\n⚠️ Status de ativação não reconhecido")
            }
        }

        return message.toString()
    }

    private fun clearResults() {
        resultState = ResultState()
        Log.d(TAG, "🧹 [GOOGLE] Resultados limpos")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoogleWalletMockTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App2AppSimulator(
                        onSimulateClick = { simulateApp2App() },
                        onClearClick = { clearResults() },
                        resultState = resultState,
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
            // Gerar dados simulados com tokenReferenceId
            val simulatedData = "9e4eeb4e-71af-4024-b3ff-05c7a2d4460d"

            Log.d(TAG, "📋 [GOOGLE] Simulated Data: $simulatedData")

            val intent = Intent(BuildConfig.TARGET_APP_ACTION).apply {
                setPackage(BuildConfig.TARGET_APP_PACKAGE)
                putExtra(Intent.EXTRA_TEXT, simulatedData)
            }

            // Tentar iniciar o app diretamente usando ActivityResultLauncher
            try {
                app2AppLauncher.launch(intent)
                Log.d(TAG, "🚀 [GOOGLE] App example iniciado com sucesso")
            } catch (e: Exception) {
                Log.e(TAG, "❌ [GOOGLE] Erro ao iniciar app: ${e.message}")
                showAlert(
                    title = "❌ Erro ao Abrir App",
                    message = "Não foi possível abrir o app example.\n\nErro: ${e.message}\n\nPackage: ${BuildConfig.TARGET_APP_PACKAGE}\nAction: ${BuildConfig.TARGET_APP_ACTION}",
                    resultCode = -1
                )
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ [GOOGLE] Erro ao simular App 2 App: ${e.message}")
            showAlert(
                title = "❌ Erro Geral",
                message = "Erro inesperado ao simular App 2 App.\n\nErro: ${e.message}\n\nVerifique se o package está correto e se o app está instalado.",
                resultCode = -1
            )
        }
    }

}

@Composable
fun App2AppSimulator(
  onSimulateClick: () -> Unit,
  onClearClick: () -> Unit,
  resultState: MainActivity.ResultState,
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
            text = "Package: ${BuildConfig.TARGET_APP_PACKAGE}",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 16.dp)
        )

        Text(
            text = "Action: ACTIVATE_TOKEN",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp)
        )

        // Seção de resultados
        if (resultState.hasResult) {
            ResultDisplay(
                resultState = resultState,
                onClearClick = onClearClick,
                modifier = Modifier.padding(top = 32.dp)
            )
        }
    }
}

@Composable
fun ResultDisplay(
  resultState: MainActivity.ResultState,
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
            text = "📋 Resultado da Ativação",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Card de resultado
        androidx.compose.material3.Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
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

                when (resultState.activationResponse) {
                    "approved" -> {
                        Text(
                            text = "✅ Status: APROVADO",
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
                            text = "❓ Status: ${resultState.activationResponse}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = androidx.compose.ui.graphics.Color(0xFF9E9E9E),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }
            }
        }

        // Botão de limpar
        Button(
            onClick = onClearClick,
            colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                containerColor = androidx.compose.ui.graphics.Color(0xFF607D8B)
            )
        ) {
            Text("🧹 Limpar Resultados")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun App2AppSimulatorPreview() {
    GoogleWalletMockTheme {
        App2AppSimulator(
            onSimulateClick = { },
            onClearClick = { },
            resultState = MainActivity.ResultState()
        )
    }
}
