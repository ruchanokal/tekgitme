package com.ruchanokal.tekgitme.fragments

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.ruchanokal.tekgitme.activities.MainActivity
import com.ruchanokal.tekgitme.databinding.FragmentSignInModeBinding


class SignInModeFragment : Fragment() {

    private var binding: FragmentSignInModeBinding? = null
    private lateinit var mAuth : FirebaseAuth
    private lateinit var user : FirebaseUser
    var girisSekli = ""
    var value = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSignInModeBinding.inflate(inflater, container, false)
        val view = binding!!.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefences = requireActivity().getSharedPreferences("com.ruchanokal.tekgitme", Context.MODE_PRIVATE)



        mAuth = FirebaseAuth.getInstance()

        if (mAuth.currentUser != null ) {

            user = mAuth.currentUser!!

            if (user != null) {

                girisSekli = prefences.getString("giris","")!!

                if (girisSekli.equals("musteri")) {
                    value = 2
                } else if (girisSekli.equals("taksici")){
                    value = 4
                } else
                    value = 0

                val intent = Intent(activity, MainActivity::class.java)
                intent.putExtra("definite",value)
                startActivity(intent)
                requireActivity().finish()

            }

        }

        binding!!.musteriGirisiButton.setOnClickListener {

            val action = SignInModeFragmentDirections.actionSignInModeToSignInFragment()
            Navigation.findNavController(it).navigate(action)
            prefences.edit().putString("giris","musteri").apply()

        }

        binding!!.taksiciGirisiButton.setOnClickListener {

            val action = SignInModeFragmentDirections.actionSignInModeToTaksiciSignInFragment()
            Navigation.findNavController(it).navigate(action)
            prefences.edit().putString("giris","taksici").apply()

        }


        val callback = object  : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)

    }


}