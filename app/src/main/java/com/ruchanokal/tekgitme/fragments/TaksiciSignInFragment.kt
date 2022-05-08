package com.ruchanokal.tekgitme.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ruchanokal.tekgitme.R
import com.ruchanokal.tekgitme.activities.MainActivity
import com.ruchanokal.tekgitme.databinding.FragmentTaksiciSignInBinding


class TaksiciSignInFragment : Fragment() {

    private var binding : FragmentTaksiciSignInBinding? = null
    private lateinit var mAuth : FirebaseAuth
    private lateinit var user : FirebaseUser
    private lateinit var db : FirebaseFirestore
    var userUid = ""
    private lateinit var listener : ListenerRegistration
    private var emailList = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaksiciSignInBinding.inflate(inflater,container,false)
        val view = binding!!.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        userUid = mAuth.currentUser?.uid.toString()


        if (mAuth.currentUser != null ) {

            user = mAuth.currentUser!!

            if (user != null) {

                val intent = Intent(activity, MainActivity::class.java)
                intent.putExtra("definite",4)
                startActivity(intent)
                requireActivity().finish()

            }

        }

        binding?.girisYapButton?.setOnClickListener { signIn()}

        binding?.kayitOlText?.setOnClickListener {

            val action = TaksiciSignInFragmentDirections.actionTaksiciSignInFragmentToTaksiciSignUpFragment3()
            Navigation.findNavController(it).navigate(action)

        }

        val callback = object  : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)

    }

    private fun signIn() {


        binding?.progressBarSignIn?.visibility = View.VISIBLE
        binding?.progressBarSignIn?.translationZ = 2F
        binding?.progressBarSignIn?.elevation = 10F

        val email  = binding?.editTextEmail?.text.toString()
        val password = binding?.editTextSifre?.text.toString()


        if ( email.equals("") && password.equals("")) {

            Toast.makeText(activity,"Lütfen gerekli alanları doldurunuz!",
                Toast.LENGTH_LONG).show()

            binding?.progressBarSignIn?.visibility = View.GONE

        } else if ( password.equals("")) {

            Toast.makeText(activity,"Lütfen şifrenizi giriniz!",
                Toast.LENGTH_LONG).show()

            binding?.progressBarSignIn?.visibility = View.GONE

        } else if(email.equals("") ) {

            Toast.makeText(activity,"Lütfen kayıtlı e-posta adresinizi giriniz!",
                Toast.LENGTH_LONG).show()

            binding?.progressBarSignIn?.visibility = View.GONE

        } else {


            val query = db.collection("TaksiDuragi")

            listener = query.addSnapshotListener { value, error ->

                if (error!= null)
                    Toast.makeText(requireContext(),"Hata oluştu! Lütfen tekrar deneyin",Toast.LENGTH_SHORT).show()

                if (value != null) {

                    if (!value.isEmpty){

                        val documents = value.documents

                        for ( document in documents) {

                            val testEmail = document.get("email") as String
                            emailList.add(testEmail)

                            if (testEmail.equals(email)) {

                                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {

                                    if (it.isSuccessful){

                                        listener.remove()

                                        val intent = Intent(requireActivity(),MainActivity::class.java)
                                        intent.putExtra("definite",4)
                                        startActivity(intent)
                                        requireActivity().finish()
                                        binding?.progressBarSignIn?.visibility = View.GONE


                                    } else {

                                        try {
                                            throw it.getException()!!
                                        }   catch (e: FirebaseAuthUserCollisionException) {

                                            println("kullanıcılar eşleşmiyor")
                                            listener.remove()

                                            Toast.makeText(activity,e.localizedMessage, Toast.LENGTH_LONG).show()
                                            binding?.progressBarSignIn?.visibility = View.GONE

                                        } catch (e: FirebaseAuthEmailException) {

                                            println("email hatası")
                                            listener.remove()

                                            Toast.makeText(activity,e.localizedMessage, Toast.LENGTH_LONG).show()
                                            binding?.progressBarSignIn?.visibility = View.GONE

                                        } catch (e: FirebaseAuthInvalidUserException) {

                                            println(e)
                                            listener.remove()

                                            Toast.makeText(activity,"Bu e-posta ile eşleşen bir kullanıcı yok. Lütfen tekrar deneyin!",
                                                Toast.LENGTH_LONG).show()
                                            binding?.progressBarSignIn?.visibility = View.GONE

                                        } catch (e: FirebaseNetworkException) {

                                            println(e)
                                            listener.remove()

                                            Toast.makeText(activity,"Lütfen internet bağlantınızı kontrol ediniz",
                                                Toast.LENGTH_LONG).show()
                                            binding?.progressBarSignIn?.visibility = View.GONE

                                        } catch (e: FirebaseAuthInvalidCredentialsException) {

                                            listener.remove()

                                            Toast.makeText(activity,e.localizedMessage, Toast.LENGTH_LONG).show()
                                            binding?.progressBarSignIn?.visibility = View.GONE

                                        } catch (e: Exception) {

                                            listener.remove()

                                            Toast.makeText(activity,e.localizedMessage, Toast.LENGTH_LONG).show()
                                            binding?.progressBarSignIn?.visibility = View.GONE

                                        }

                                    }

                                }

                                break


                            } else
                                Log.i("TaksiciSignInFragment","data bulunamadı-1")


                        }


                        val distinct = emailList.toSet().toList()

                        Log.i("TaksiciSignInFragment","distinct: " + distinct)
                        Log.i("TaksiciSignInFragment","email: " + email)

                        if (distinct.size > 0 && !distinct.contains(email)){

                            Log.i("TaksiciSignInFragment","email-2: " + email)

                            listener.remove()
                            binding?.progressBarSignIn?.visibility = View.GONE

                            Toast.makeText(activity,"Bu e-posta ile eşleşen bir taksi durağı yok. Lütfen tekrar deneyin!",
                                Toast.LENGTH_SHORT).show()

                        }

                    } else {

                        listener.remove()

                        Log.i("TaksiciSignInFragment","data bulunamadı")

                        binding?.progressBarSignIn?.visibility = View.GONE
                        Toast.makeText(activity,"Bu e-posta ile eşleşen bir taksi durağı yok. Lütfen tekrar deneyin!",
                            Toast.LENGTH_SHORT).show()
                    }
                } else {

                    listener.remove()

                    Log.i("TaksiciSignInFragment","data null")

                    binding?.progressBarSignIn?.visibility = View.GONE
                    Toast.makeText(activity,"Bu e-posta ile eşleşen bir taksi durağı yok. Lütfen tekrar deneyin!",
                        Toast.LENGTH_SHORT).show()
                }


            }



        }
    }
}