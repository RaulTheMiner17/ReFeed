package com.ran.refeed.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ran.refeed.R
import com.ran.refeed.viewmodels.AuthViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigate: (String) -> Unit,
    authViewModel: AuthViewModel
) {
    // Animation states
    var startAnimation by remember { mutableStateOf(false) }
    var startTaglineAnimation by remember { mutableStateOf(false) }

    // Load Lottie animation
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.food_animation)
    )

    // Animation alpha
    val animationAlpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000)
    )

    // Tagline animation
    val taglineAlpha by animateFloatAsState(
        targetValue = if (startTaglineAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 800)
    )

    val taglineOffset by animateDpAsState(
        targetValue = if (startTaglineAnimation) 0.dp else 30.dp,
        animationSpec = tween(durationMillis = 800, easing = EaseOutQuad)
    )

    // Animation sequence and navigation
    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(1000)
        startTaglineAnimation = true
        delay(2500) // Give more time to see the Lottie animation

        // Check if user is logged in and navigate accordingly
        if (authViewModel.isUserLoggedIn()) {
            onNavigate("home")
        } else {
            onNavigate("login")
        }
    }

    // Splash screen UI
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFEE1)),
        contentAlignment = Alignment.Center
    ) {
        // Lottie animation
        LottieAnimation(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            modifier = Modifier
                .size(300.dp)
                .alpha(animationAlpha),
            isPlaying = startAnimation
        )

        // Tagline with animations
        Text(
            text = "Reducing Food Waste, One Meal at a Time",
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp, start = 32.dp, end = 32.dp)
                .alpha(taglineAlpha)
                .offset(y = taglineOffset)
        )
    }
}
