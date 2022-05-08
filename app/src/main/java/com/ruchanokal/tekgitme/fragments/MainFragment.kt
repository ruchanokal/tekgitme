package com.ruchanokal.tekgitme.fragments

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ruchanokal.tekgitme.R
import com.ruchanokal.tekgitme.activities.SignInActivity
import com.ruchanokal.tekgitme.adapter.ListRecyclerAdapter
import com.ruchanokal.tekgitme.databinding.DialogNewDestinationBinding
import com.ruchanokal.tekgitme.databinding.FragmentMainBinding
import com.ruchanokal.tekgitme.model.Way


var yolculukDevamEdiyor = false

class MainFragment : Fragment() {

    private val TAG = "MainFragment"
    private var binding: FragmentMainBinding? = null
    lateinit var mAuth: FirebaseAuth
    lateinit var user: FirebaseUser
    private lateinit var db : FirebaseFirestore
    var definiteNumber = 0
    var userUid = ""
    var kullaniciAdi = ""
    var il = ""
    var ilce = ""
    var taksiDuragi = ""
    var adapter : ListRecyclerAdapter? = null
    var reference : ListenerRegistration? = null
    var reference2 : ListenerRegistration? = null
    private var distinct = listOf<String>()


    lateinit var alertDialog: AlertDialog.Builder
//    var stationList = arrayListOf("Menekşe Durağı", "Manolya Durağı",
//        "Papatya Durağı", "Gül Durağı","Karanfil Durağı", "Sümbül Durağı","Lale Durağı",
//        "Zambak Durağı","Nergis Durağı", "Nilüfer Durağı")

    var stationList = arrayListOf("Menekşe Durağı", "Manolya Durağı",
        "Papatya Durağı")

    var districtList = arrayListOf("Akyurt", "Altındağ",
        "Ayaş", "Bala","Beypazarı", "Çamlıdere","Çankaya",
        "Çubuk","Elmadağ", "Etimesgut","Evren","Gölbaşı",
        "Güdül","Haymana","Kahramankazan","Kalecik",
        "Keçiören","Kızılcahamam","Mamak","Nallıhan",
        "Polatlı","Pursaklar","Sincan","Şereflikoçhisar",
        "Yenimahalle")

    var nereyeString = ""
    var neredenString = ""
    var detayliNereyeString = ""
    var saat = ""
    var dakika = ""
    var recyclerlist = arrayListOf<Way>()
    var number = 0
    var yolculukUserUid = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentMainBinding.inflate(inflater, container, false)
        val view = binding!!.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefences = requireActivity().getSharedPreferences("com.ruchanokal.tekgitme", Context.MODE_PRIVATE)
        distinct = stationList.toSet().toList()

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userUid = mAuth.currentUser?.uid.toString()

        kullaniciGirisi()

        val intent = requireActivity().intent
        val deger = intent.getIntExtra("definite",0)

        yolculukDevamEdiyor = prefences.getBoolean("yolculukDevamEdiyor",false)
        number = prefences.getInt("number",0)
        yolculukUserUid = prefences.getString("userUid","userUid")!!


        if (yolculukDevamEdiyor) {
            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
            val navController = navHostFragment.navController

            val action = MainFragmentDirections
                .actionMainFragmentToYolculukFragment(yolculukUserUid,number)
            navController.navigate(action)
        }



        binding!!.recyclerViewList.layoutManager = LinearLayoutManager(requireContext())
        adapter = ListRecyclerAdapter(recyclerlist,userUid,db,requireActivity(),deger)
        binding!!.recyclerViewList.adapter = adapter


        getDataFromFirestore()

        fabClick()

        signOutButton()

        geriTusu()



    }

    private fun getDataFromFirestore() {

        db.collection("YeniYolculuk").addSnapshotListener { value, error ->

            if (error != null) {

                Toast.makeText(requireContext(),"Veriler yüklenemedi!",Toast.LENGTH_SHORT).show()
            }

            if (value != null) {

                if (!value.isEmpty) {

                    recyclerlist.clear()

                    val documents = value.documents

                    for (document in documents) {
                        val nereden = document.get("nereden") as String
                        val nereye = document.get("nereye") as String
                        val detayliNereye = document.get("detayliNereye") as String
                        val saat = document.get("saat") as String
                        val dakika = document.get("dakika") as String
                        val userUid2 = document.get("userUid") as String
                        val bosyer = document.get("bosyer") as Long

                        val way = Way(nereden,nereye,detayliNereye,saat,dakika,userUid2,bosyer)
                        recyclerlist.add(way)
                    }
                    adapter!!.notifyDataSetChanged()

                }
            }

        }
    }

    private fun fabClick() {

        binding!!.newDestinationFAB.setOnClickListener {

            val dialog = activity?.let { Dialog(it) }

            if (dialog != null) {

                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val binding2 : DialogNewDestinationBinding =
                    DialogNewDestinationBinding.inflate(layoutInflater)
                dialog.setContentView(binding2.root)

                lateinit var arrayAdapter: ArrayAdapter<String>
                arrayAdapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,distinct)
                binding2.neredenACTextView.setAdapter(arrayAdapter)

                lateinit var arrayAdapter2: ArrayAdapter<String>
                arrayAdapter2 = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,districtList)
                binding2.nereyeACTextView.setAdapter(arrayAdapter2)

                binding2.saatEditText.addTextChangedListener {

                    Log.i("MainFragment","saat sonuc " + it.toString())

                    if (!it.isNullOrEmpty()){

                        if (it.toString().toInt() >= 24) {
                            binding2.saatEditText.setText("23")
                        }
                    }

                }

                binding2.dakikaEditText.addTextChangedListener {

                    if (!it.isNullOrEmpty()) {

                        if (it.toString().toInt() >= 60) {
                            binding2.dakikaEditText.setText("59")
                        }
                    }

                }

                binding2.neredenACTextView.setOnItemClickListener { adapterView, view, i, l ->
                    neredenString = adapterView.getItemAtPosition(i).toString()
                }

                binding2.nereyeACTextView.setOnItemClickListener { adapterView, view, i, l ->
                    nereyeString = adapterView.getItemAtPosition(i).toString()
                }

                binding2.tamamButton.setOnClickListener { tamamButton ->

                    detayliNereyeString = binding2.textInputEditText.text.toString()
                    saat = binding2.saatEditText.text.toString()
                    dakika = binding2.dakikaEditText.text.toString()

                    Log.i("MainF","nereden: " + neredenString)
                    Log.i("MainF","nereye: " + nereyeString)

                    Log.i("MainF","nereden: " + neredenString.isEmpty())
                    Log.i("MainF","nereye: " + nereyeString.isEmpty())

                    if (neredenString.isEmpty()) {
                        Toast.makeText(requireContext(),"Lütfen Taksi Durağınızı seçiniz",Toast.LENGTH_SHORT).show()
                    } else if (nereyeString.isEmpty()) {
                        Toast.makeText(requireContext(),"Lütfen gideceğiniz ilçeyi seçiniz",Toast.LENGTH_SHORT).show()
                    } else if (detayliNereyeString.isEmpty()) {
                        Toast.makeText(requireContext(),"Lütfen gideceğiniz güzergahla alakalı detayları belirtiniz",Toast.LENGTH_SHORT).show()
                    } else if (saat.isEmpty() || dakika.isEmpty()) {
                        Toast.makeText(requireContext(),"Lütfen zaman bilgisi giriniz",Toast.LENGTH_SHORT).show()
                    } else{

                        val hashMap = hashMapOf<Any,Any>()
                        hashMap.put("nereden",neredenString)
                        hashMap.put("nereye",nereyeString)
                        hashMap.put("detayliNereye",detayliNereyeString)
                        hashMap.put("saat",saat)
                        hashMap.put("dakika",dakika)
                        hashMap.put("userUid",userUid)
                        hashMap.put("bosyer",3)


                        db.collection("YeniYolculuk").document(userUid).set(hashMap).addOnSuccessListener {

                            val navHostFragment = requireActivity().supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
                            val navController = navHostFragment.navController

                            val action = MainFragmentDirections
                                .actionMainFragmentToYolculukFragment(userUid,1)
                            navController.navigate(action)

                            neredenString = ""
                            nereyeString = ""
                            detayliNereyeString = ""
                            saat = ""
                            dakika = ""

                            dialog.dismiss()

                            Toast.makeText(requireContext(),"Yolculuk oluşturuldu",Toast.LENGTH_SHORT).show()

                        }.addOnFailureListener {

                            Toast.makeText(requireContext(),"Hata oluştu, lütfen tekrar deneyin",Toast.LENGTH_SHORT).show()

                        }

                    }

                }

                binding2.iptalButton.setOnClickListener {


                    neredenString = ""
                    nereyeString = ""
                    detayliNereyeString = ""
                    saat = ""
                    dakika = ""

                    dialog.dismiss()
                }


                dialog.show()
            }

        }

    }

    private fun kullaniciGirisi() {

        val intent = requireActivity().intent
        definiteNumber = intent.getIntExtra("definite",0)

        if ( definiteNumber == 1) {

            println("definite number : " + definiteNumber)

            kullaniciAdi = intent.getStringExtra("kullaniciAdi")!!
            userUid = mAuth.currentUser?.uid.toString()
            binding?.kullaniciAdiTextView?.setText(kullaniciAdi)


            val hashMap = hashMapOf<Any,Any>()
            mAuth.currentUser?.email?.let { hashMap.put("email", it) }
            if (kullaniciAdi != null) {
                hashMap.put("kullaniciAdi",kullaniciAdi)
            }

            db.collection("Users").document(userUid).set(hashMap)

            db.collection("TaksiDuragi").addSnapshotListener { value, error ->

                if (error!= null) {

                    Toast.makeText(context,"Bir hata oluştu", Toast.LENGTH_LONG).show()

                } else {
                    if (value != null) {
                        if (!value.isEmpty){

                            val documents = value.documents

                            for ( document in documents) {

                                var durakString = document.get("taksiDuragi") as String
                                stationList.add(durakString)

                            }

                            distinct = stationList.toSet().toList()


                        }
                    }
                }
            }

        } else if ( definiteNumber == 2) {

            println("definite number : " + definiteNumber)

            userUid = mAuth.currentUser?.uid.toString()

            user = mAuth.currentUser!!

            val email = user.email

            val name = user.displayName

            if (!name.isNullOrEmpty()){

                println("name: " + name)

                println("111")

                binding?.kullaniciAdiTextView?.text = user.displayName

            } else  {

                println("222")

                reference = db.collection("Users").whereEqualTo("email",email).addSnapshotListener { value, error ->

                    if (error != null){

                        Toast.makeText(context,"Bir hata oluştu", Toast.LENGTH_LONG).show()

                    } else {

                        if ( value != null) {
                            if ( !value.isEmpty ) {

                                val documents = value.documents

                                for ( document in documents) {

                                    kullaniciAdi = document.get("kullaniciAdi") as String

                                    binding?.kullaniciAdiTextView?.text = kullaniciAdi


                                }
                            }
                        }

                    }

                }

            }

            db.collection("TaksiDuragi").addSnapshotListener { value, error ->

                if (error!= null) {

                    Toast.makeText(context,"Bir hata oluştu", Toast.LENGTH_LONG).show()

                } else {
                    if (value != null) {
                        if (!value.isEmpty){

                            val documents = value.documents

                            for ( document in documents) {

                                var durakString = document.get("taksiDuragi") as String
                                stationList.add(durakString)

                            }

                            distinct = stationList.toSet().toList()


                        }
                    }
                }
            }

        } else if ( definiteNumber == 3) {

            binding!!.newDestinationFAB.visibility = View.GONE

            println("definite number : " + definiteNumber)

            il = intent.getStringExtra("il")!!
            ilce = intent.getStringExtra("ilce")!!
            taksiDuragi = intent.getStringExtra("taksiDuragi")!!
            userUid = mAuth.currentUser?.uid.toString()
            binding?.kullaniciAdiTextView?.setText(taksiDuragi)


            val hashMap = hashMapOf<Any,Any>()
            mAuth.currentUser?.email?.let { hashMap.put("email", it) }

            hashMap.put("taksiDuragi",taksiDuragi)
            hashMap.put("il",il)
            hashMap.put("ilce",ilce)
            hashMap.put("onay",false)


            db.collection("TaksiDuragi").document(userUid).set(hashMap).addOnSuccessListener {


                db.collection("TaksiDuragi").document(userUid).addSnapshotListener { value, error ->

                    Log.i(TAG,"addSnapShotListener")

                    if (error!= null)
                        Toast.makeText(context,"Bir hata oluştu", Toast.LENGTH_LONG).show()

                    if (value != null) {
                        if (value.exists()) {

                            val onayBool = value["onay"] as Boolean

                            Log.i(TAG,"onayBool: " + onayBool)

                            if (onayBool) {

                                Log.i(TAG,"onayBool-1: " + onayBool)

                                binding!!.adminOnayiLayout.visibility = View.GONE

                            } else {
                                Log.i(TAG,"onayBool-2: " + onayBool)
                                binding!!.adminOnayiLayout.visibility = View.VISIBLE
                            }

                        } else
                            Log.i(TAG,"data empty")
                    } else
                        Log.i(TAG,"data null")

                }

            }

        } else if ( definiteNumber == 4) {

            binding!!.newDestinationFAB.visibility = View.GONE

            println("definite number : " + definiteNumber)

            userUid = mAuth.currentUser?.uid.toString()

            user = mAuth.currentUser!!

            val email = user.email

            val name = user.displayName

            if (!name.isNullOrEmpty()){

                println("name: " + name)

                println("111")

                binding?.kullaniciAdiTextView?.text = user.displayName

            } else  {

                println("222")

                reference2 = db.collection("TaksiDuragi").whereEqualTo("email",email).addSnapshotListener { value, error ->

                    if (error != null){

                        Toast.makeText(context,"Bir hata oluştu", Toast.LENGTH_LONG).show()

                    } else {

                        if ( value != null) {
                            if ( !value.isEmpty ) {

                                val documents = value.documents

                                for ( document in documents) {

                                    taksiDuragi = document.get("taksiDuragi") as String
                                    val onayBool = document.get("onay") as Boolean
                                    Log.i(TAG,"onayBool: " + onayBool)

                                    binding?.kullaniciAdiTextView?.text = taksiDuragi

                                    if (onayBool) {

                                        Log.i(TAG,"onayBool-1: " + onayBool)

                                        binding!!.adminOnayiLayout.visibility = View.GONE

                                    } else {
                                        Log.i(TAG,"onayBool-2: " + onayBool)

                                        binding!!.adminOnayiLayout.visibility = View.VISIBLE
                                    }
                                }
                            }
                        }

                    }

                }

            }

        }
    }

    private fun signOutButton() {

        binding?.signOutButton?.setOnClickListener {


            alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Çıkış Yap")
            alertDialog.setMessage("Çıkış yapmak istediğinize emin misiniz?")
            alertDialog.setPositiveButton("Evet",
                DialogInterface.OnClickListener { dialogInterface, i ->

                if (mAuth.currentUser != null ){
                    mAuth.signOut()
                }

                val intent = Intent(requireActivity(), SignInActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)


            }).setNegativeButton("Hayır", DialogInterface.OnClickListener { dialogInterface, i ->

                println("Uygulamada kal")

            })

            alertDialog.create().show()



        }

    }

    private fun geriTusu() {

        val callback = object  : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }


}