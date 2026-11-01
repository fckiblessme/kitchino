package com.kitchino.app.ui

import android.widget.Space
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kitchino.app.auth.AuthViewModel
import com.kitchino.app.ui.theme.StartScreenButtons
import com.kitchino.app.ui.theme.StartScreenColor

@Composable
fun LoginScreen(onMenuClick: () -> Unit = {}, viewModel: AuthViewModel) {
    var name by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state) {
        if(state is AuthViewModel.State.LoggedIn){
            onMenuClick()
            viewModel.reset()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(StartScreenColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Вход",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 96.dp, bottom = 16.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Имя") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StartScreenColor,
                    focusedLabelColor = StartScreenColor
                )
            )

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = pin,
                onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) pin = it },
                label = { Text("PIN") },
                modifier = Modifier
                    .fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = StartScreenColor,
                    focusedLabelColor = StartScreenColor
                )
            )

            Button(
                onClick = { viewModel.login(name, pin) },
                modifier = Modifier
                    .width(200.dp)
                    .height(80.dp)
                    .padding(top = 24.dp),
                colors = ButtonColors(
                    contentColor = Color.White,
                    containerColor = StartScreenButtons,
                    disabledContentColor = Color.White,
                    disabledContainerColor = StartScreenButtons
                )

            ) {
                Text("Войти", fontSize = 18.sp)
            }

            when (val s = state) {
                is AuthViewModel.State.Loading -> CircularProgressIndicator(
                    modifier = Modifier.padding(top = 16.dp)
                )
                is AuthViewModel.State.Error -> Text(
                    s.message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 16.dp)
                )
                else -> {}
            }
        }
    }
}