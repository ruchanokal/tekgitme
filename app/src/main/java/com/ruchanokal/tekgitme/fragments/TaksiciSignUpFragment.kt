package com.ruchanokal.tekgitme.fragments

import android.R.attr
import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.database.Cursor
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ruchanokal.tekgitme.activities.MainActivity
import com.ruchanokal.tekgitme.databinding.FragmentTaksiciSignUpBinding
import java.io.File


class TaksiciSignUpFragment : Fragment() {

    private var binding : FragmentTaksiciSignUpBinding? = null
    private val TAG  = "TaksiciSignUpFragment"

    private lateinit var mAuth: FirebaseAuth
    private var sifre : String = ""
    private var sifre2 : String = ""
    var userUid = ""
    private lateinit var db : FirebaseFirestore
    private lateinit var hashMap : HashMap<Any,Any>
    private lateinit var il : String
    private lateinit var ilce : String
    private lateinit var taksiDuragi : String
    private lateinit var email : String
    private lateinit var listener : ListenerRegistration
    var numberArrayList = arrayListOf<Int>()
    private lateinit var distinct : List<Int>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaksiciSignUpBinding.inflate(inflater,container,false)
        val view = binding!!.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        distinct = emptyList()

        binding?.kayitOlButton?.setOnClickListener {

            binding?.progressBarSignUp?.visibility = View.VISIBLE

            email = binding?.editTextEmailKayit?.text.toString()
            taksiDuragi = binding?.editTextTaksiDuragi?.text.toString()
            il = binding?.editTextIl?.text.toString()
            ilce = binding?.editTextIlce?.text.toString()
            sifre = binding?.editTextSifreKayit?.text.toString()
            sifre2 = binding?.editTextSifreKayit2?.text.toString()


            hashMap = hashMapOf<Any,Any>()
            hashMap.put("email",email)
            hashMap.put("il",il)
            hashMap.put("ilce",ilce)
            hashMap.put("taksiDuragi",taksiDuragi)
            hashMap.put("onay",false)


            databaseCollection()

        }



        binding!!.motorluAracTrafikTescilBelgesiTextView
            .setPaintFlags(binding!!.motorluAracTrafikTescilBelgesiTextView.getPaintFlags()
                    or Paint.UNDERLINE_TEXT_FLAG)

        binding!!.kurumFaaliyetBelgesiTextView
            .setPaintFlags(binding!!.kurumFaaliyetBelgesiTextView.getPaintFlags()
                    or Paint.UNDERLINE_TEXT_FLAG)


        binding!!.motorluAracTrafikTescilBelgesiTextView.setOnClickListener {

            openFile1()

        }

        binding!!.kurumFaaliyetBelgesiTextView.setOnClickListener {

            openFile2()

        }

        val callback = object  : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    private fun databaseCollection() {


        val query = db.collection("TaksiDuragi").whereEqualTo("taksiDuragi",taksiDuragi)

        listener  = query.addSnapshotListener { value, error ->

            if (value !=null) {
                println("null değil")

                if ( !value.isEmpty) {
                    println("empty değil")

                    Toast.makeText(context,"Bu isimde taksi durağı vardır! Lütfen başka bir isim deneyin",
                        Toast.LENGTH_LONG).show()

                    binding?.progressBarSignUp?.visibility = View.GONE

                } else {

                    println("empty")

                    kontroller()

                }

            } else {

                println("null")

                kontroller()

            }

        }

    }

    private fun kontroller() {

        Log.i(TAG,"distinct size: " + distinct.size)

        if (email.equals("") || il.equals("") || ilce.equals("") || taksiDuragi.equals("") || sifre.equals("")){

            listener.remove()

            Toast.makeText(activity,"Lütfen gerekli alanları doldurunuz",Toast.LENGTH_LONG).show()

            binding?.progressBarSignUp?.visibility = View.GONE

        } else if (distinct.size < 2) {

            listener.remove()

            Toast.makeText(activity,"Lütfen sizden istenilen belgeleri sisteme yükleyiniz!",Toast.LENGTH_LONG).show()

            binding?.progressBarSignUp?.visibility = View.GONE

        } else if (!sifre.equals(sifre2)) {

            listener.remove()

            Toast.makeText(activity,"Şifreler aynı olmalıdır!",Toast.LENGTH_LONG).show()

            binding?.progressBarSignUp?.visibility = View.GONE

        }  else  {

            mAuth.createUserWithEmailAndPassword(email,sifre).addOnCompleteListener { task ->

                if (task.isSuccessful){

                    listener.remove()

                    userUid = mAuth.currentUser?.uid.toString()

                    binding?.progressBarSignUp?.visibility = View.GONE

                    val intent = Intent(activity, MainActivity::class.java)
                    intent.putExtra("taksiDuragi",taksiDuragi)
                    intent.putExtra("il",il)
                    intent.putExtra("ilce",ilce)
                    intent.putExtra("definite",3)
                    startActivity(intent)
                    requireActivity().finish()

                }

            }.addOnFailureListener { exception ->


                try {
                    throw exception
                }   catch (e: FirebaseAuthUserCollisionException) {

                    listener.remove()

                    Toast.makeText(activity,"Bu e-posta adresi zaten başka bir hesap tarafından kullanılıyor",Toast.LENGTH_LONG).show()
                    binding?.progressBarSignUp?.visibility = View.GONE

                } catch(e : FirebaseAuthWeakPasswordException) {

                    listener.remove()

                    Toast.makeText(activity,"Lütfen en az 6 haneli bir şifre giriniz",Toast.LENGTH_LONG).show()
                    binding?.progressBarSignUp?.visibility = View.GONE

                } catch (e: FirebaseNetworkException) {

                    listener.remove()

                    Toast.makeText(activity,"Lütfen internet bağlantınızı kontrol edin",Toast.LENGTH_LONG).show()
                    binding?.progressBarSignUp?.visibility = View.GONE

                } catch(e : FirebaseAuthInvalidCredentialsException) {

                    listener.remove()

                    Toast.makeText(activity,e.localizedMessage,Toast.LENGTH_LONG).show()
                    binding?.progressBarSignUp?.visibility = View.GONE

                } catch (e: Exception) {

                    listener.remove()

                    Toast.makeText(activity,e.localizedMessage,Toast.LENGTH_LONG).show()
                    binding?.progressBarSignUp?.visibility = View.GONE
                }

            }


        }


    }


    private fun openFile1() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.DIRECTORY_DOWNLOADS)
        startActivityForResult(intent,0)
    }

    private fun openFile2() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/pdf"

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Environment.DIRECTORY_DOWNLOADS)
        startActivityForResult(intent,1)
    }

    @SuppressLint("Range")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.i(TAG,"onActivityResult")


        if (resultCode === RESULT_OK) {

            Log.i(TAG,"RESULT OK")

            // Get the Uri of the selected file
            val uri: Uri? = data?.data
            val uriString = uri.toString()
            val myFile = File(uriString)
            val path: String = myFile.getAbsolutePath()
            var displayName: String? = null
            if (uriString.startsWith("content://")) {
                var cursor: Cursor? = null
                try {
                    cursor = uri?.let { requireActivity().contentResolver.query(it, null, null, null, null) }
                    if (cursor != null && cursor.moveToFirst()) {
                        displayName =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close()
                    }
                }


            } else if (uriString.startsWith("file://")) {
                displayName = myFile.getName()
            }


            when(requestCode){

                0 -> {
                    binding!!.motorluAracTrafikTescilBelgesiTextView.text = displayName
                    numberArrayList.add(0)
                    distinct = numberArrayList.toSet().toList()
                }

                1 -> {
                    binding!!.kurumFaaliyetBelgesiTextView.text = displayName
                    numberArrayList.add(1)
                    distinct = numberArrayList.toSet().toList()
                }
                else -> {
                    print("0 da 1 de degil")
                }
            }


        }

    }
}