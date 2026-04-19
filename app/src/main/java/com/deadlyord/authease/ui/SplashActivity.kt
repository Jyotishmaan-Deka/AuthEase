package com.deadlyord.authease.ui

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.deadlyord.authease.databinding.ActivitySplashBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Animate icon and tagline in
        binding.splashIcon.alpha = 0f
        binding.splashAppName.alpha = 0f
        binding.splashTagline.alpha = 0f

        binding.splashIcon.animate().alpha(1f).scaleX(1f).scaleY(1f).setDuration(600).start()
        binding.splashAppName.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(200).start()
        binding.splashTagline.animate().alpha(1f).translationY(0f).setDuration(600).setStartDelay(400).start()

        lifecycleScope.launch {
            delay(3000L)
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
            // Smooth cross-fade transition
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }
}
