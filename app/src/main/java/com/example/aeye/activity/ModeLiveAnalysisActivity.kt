package com.example.aeye.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.aeye.databinding.ActivityImageAnalysisBinding

class ModeImageAnalysisActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageAnalysisBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityImageAnalysisBinding.inflate(layoutInflater)
        setContentView(binding.root)



    }
}