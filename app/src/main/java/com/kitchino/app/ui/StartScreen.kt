package com.kitchino.app.ui


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Yellow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kitchino.app.ui.theme.KitchinoTheme
import com.kitchino.app.R
import com.kitchino.app.navigation.Destination
import com.kitchino.app.ui.theme.StartScreenButtons
import com.kitchino.app.ui.theme.StartScreenColor

@Composable
fun StartScreen(onLoginClick: () -> Unit,
onRegisterClick: () -> Unit){
    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(color = StartScreenColor),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = "Логотип Кухняо",
                modifier = Modifier
                    .padding(top = 180.dp)
                    .size(160.dp),
                contentScale = ContentScale.Fit
            )
            Spacer(modifier = Modifier.height(4.dp))

            Text(text = "Kitchino",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.width(200.dp)
                    .height(50.dp),
                onClick = onLoginClick,
                colors = ButtonDefaults.buttonColors(StartScreenButtons),

                ) {
                Text(
                    text = "Вход",
                    color = StartScreenColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                modifier = Modifier.width(200.dp)
                    .height(50.dp),
                onClick = onRegisterClick,
                colors = ButtonDefaults.buttonColors(StartScreenButtons),

                ) {
                Text(
                    text = "Регистрация",
                    color = StartScreenColor,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartScreenPreview() {
    KitchinoTheme() {
        StartScreen(onLoginClick = {},
            onRegisterClick = {})
    }
}
