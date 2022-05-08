package com.ruchanokal.tekgitme.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.ruchanokal.tekgitme.databinding.FragmentYolculukBinding


class YolculukFragment : Fragment() {

    private val TAG = "YolculukFragment"

    private var binding: FragmentYolculukBinding? = null
    lateinit var mAuth: FirebaseAuth
    lateinit var user: FirebaseUser
    private lateinit var db : FirebaseFirestore
    var userUid = ""
    var number = 0
    var bosyer = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentYolculukBinding.inflate(inflater, container, false)
        val view = binding!!.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val prefences = requireActivity().getSharedPreferences("com.ruchanokal.tekgitme", Context.MODE_PRIVATE)

        yolculukDevamEdiyor = true
        prefences.edit().putBoolean("yolculukDevamEdiyor", yolculukDevamEdiyor).apply()

        arguments?.let {

            userUid = YolculukFragmentArgs.fromBundle(it).userUid
            number = YolculukFragmentArgs.fromBundle(it).number

            prefences.edit().putInt("number",number).apply()
            prefences.edit().putString("userUid",userUid).apply()


            Log.i(TAG,"userUid: " + userUid)

            db.collection("YeniYolculuk").document(userUid).addSnapshotListener { value, error ->

                if (error != null) {
                    Toast.makeText(requireContext(),"Veriler yüklenemedi!", Toast.LENGTH_SHORT).show()
                }

                if (value != null) {

                    if (value.exists()) {

                        val nereden = value["nereden"] as String
                        val nereye = value["nereye"] as String
                        val detayli = value["detayliNereye"] as String
                        val saat = value["saat"] as String
                        val dakika = value["dakika"] as String
                        bosyer = value["bosyer"] as Long

                        binding!!.neredenCevap.text = nereden
                        binding!!.nereyeCevap.text = nereye
                        binding!!.ortakNoktaCevap.text = detayli
                        binding!!.kalkisSaatiCevap.text = saat + ":" + dakika

                    }

                }

            }


            binding!!.yolculuguBitirButton.setOnClickListener {


                binding!!.yolculuguBitirButton.isEnabled = false

                if (number == 1){

                    db.collection("YeniYolculuk").document(userUid).delete().addOnSuccessListener {


                        findNavController().popBackStack()

                        yolculukDevamEdiyor = false
                        prefences.edit().putBoolean("yolculukDevamEdiyor", yolculukDevamEdiyor).apply()
                        Toast.makeText(requireContext(),"Yolculuk sonlandırıldı!",Toast.LENGTH_SHORT).show()


                    }.addOnFailureListener {

                        binding!!.yolculuguBitirButton.isEnabled = true

                    }


                } else {

                    db.collection("YeniYolculuk").document(userUid).update("bosyer",bosyer+1).addOnSuccessListener {

                        yolculukDevamEdiyor = false
                        prefences.edit().putBoolean("yolculukDevamEdiyor", yolculukDevamEdiyor).apply()
                        Toast.makeText(requireContext(),"Yolculuk sonlandırıldı!",Toast.LENGTH_SHORT).show()

                        findNavController().popBackStack()

                    }.addOnFailureListener {

                        binding!!.yolculuguBitirButton.isEnabled = true

                    }

                }


            }


        }








    }
}