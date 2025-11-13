package com.example.layoutlab2

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.layoutlab2.ui.theme.LayoutExperimentTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LayoutExperimentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LayoutSelectionScreen()
                }
            }
        }
    }
}

@Composable
fun LayoutSelectionScreen() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Layout Lab 2",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(32.dp))

        // 布局选择按钮
        LayoutButton("Linear Layout") {
            context.startActivity(Intent(context, LinearActivity::class.java))
        }

        LayoutButton("Table Layout") {
            context.startActivity(Intent(context, TableActivity::class.java))
        }

        LayoutButton("Constraint Layout 1") {
            context.startActivity(Intent(context, Constraint1Activity::class.java))
        }

        LayoutButton("Constraint Layout 2") {
            context.startActivity(Intent(context, Constraint2Activity::class.java))
        }
    }
}

@Composable
fun LayoutButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Preview(showBackground = true)
@Composable
fun LayoutSelectionScreenPreview() {
    LayoutExperimentTheme {
        LayoutSelectionScreen()
    }
}