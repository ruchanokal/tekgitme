package com.ruchanokal.tekgitme.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ruchanokal.tekgitme.R
import com.ruchanokal.tekgitme.databinding.ActivityMainBinding
import com.ruchanokal.tekgitme.databinding.ActivitySignInBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

    }


}