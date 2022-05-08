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
import com.ruchanokal.tekgitme.databinding.FragmentSignInBinding


class SignInFragment : Fragment() {

    private var binding: FragmentSignInBinding? = null
    private lateinit var mAuth : FirebaseAuth
    private lateinit var user : FirebaseUser
    private lateinit var db : FirebaseFirestore
    private lateinit var listener : ListenerRegistration
    private var emailList = arrayListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        val view = binding!!.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        if (mAuth.currentUser != null ) {

            user = mAuth.currentUser!!

            if (user != null) {

                val intent = Intent(activity, MainActivity::class.java)
                intent.putExtra("definite",2)
                startActivity(intent)
                requireActivity().finish()

            }

        }

        binding?.girisYapButton?.setOnClickListener { signIn()}

        binding?.kayitOlText?.setOnClickListener {

            val action = SignInFragmentDirections.actionSignInFragmentToSignUpFragment()
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

            val query = db.collection("Users")

            listener = query.addSnapshotListener { value, error ->

                if (error!= null)
                    Toast.makeText(requireContext(),"Hata oluştu! Lütfen tekrar deneyin",Toast.LENGTH_SHORT).show()


                if (value != null) {

                    if (!value.isEmpty) {

                        val documents = value.documents

                        for ( document in documents) {

                            val testEmail = document.get("email") as String
                            emailList.add(testEmail)

                            if (testEmail.equals(email)) {

                                mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {

                                    if (it.isSuccessful){

                                        listener.remove()

                                        val intent = Intent(requireActivity(),MainActivity::class.java)
                                        intent.putExtra("definite",2)
                                        startActivity(intent)
                                        requireActivity().finish()
                                        binding?.progressBarSignIn?.visibility = View.GONE


                                    } else {

                                        try {
                                            throw it.getException()!!
                                        }   catch (e: FirebaseAuthUserCollisionException) {

                                            listener.remove()

                                            println("kullanıcılar eşleşmiyor")

                                            Toast.makeText(activity,e.localizedMessage, Toast.LENGTH_LONG).show()
                                            binding?.progressBarSignIn?.visibility = View.GONE

                                        } catch (e: FirebaseAuthEmailException) {

                                            listener.remove()

                                            println("email hatası")

                                            Toast.makeText(activity,e.localizedMessage, Toast.LENGTH_LONG).show()
                                            binding?.progressBarSignIn?.visibility = View.GONE

                                        } catch (e: FirebaseAuthInvalidUserException) {

                                            listener.remove()

                                            println(e)

                                            Toast.makeText(activity,"Bu e-posta ile eşleşen bir kullanıcı yok. Lütfen tekrar deneyin!",
                                                Toast.LENGTH_LONG).show()
                                            binding?.progressBarSignIn?.visibility = View.GONE

                                        } catch (e: FirebaseNetworkException) {
                                            listener.remove()

                                            println(e)

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
                                Log.i("SignInFragment","data bulunamadı-1")



                        }


                        val distinct = emailList.toSet().toList()

                        Log.i("SignInFragment","distinct: " + distinct)
                        Log.i("SignInFragment","email: " + email)

                        if (distinct.size > 0 && !distinct.contains(email)){

                            Log.i("SignInFragment","email-2: " + email)

                            listener.remove()
                            binding?.progressBarSignIn?.visibility = View.GONE

                            Toast.makeText(activity,"Bu e-posta ile eşleşen bir kullanıcı yok. Lütfen tekrar deneyin!",
                                Toast.LENGTH_SHORT).show()

                        }

                    } else {

                        listener.remove()

                        Log.i("SignInFragment","data bulunamadı")

                        binding?.progressBarSignIn?.visibility = View.GONE
                        Toast.makeText(activity,"Bu e-posta ile eşleşen bir kullanıcı yok, lütfen tekrar deneyin!",
                            Toast.LENGTH_SHORT).show()


                    }

                } else {


                    listener.remove()

                    Log.i("SignInFragment","data null")

                    binding?.progressBarSignIn?.visibility = View.GONE
                    Toast.makeText(activity,"Böyle bir kullanıcı yok. Lütfen tekrar deneyin!",
                        Toast.LENGTH_SHORT).show()
                }

            }



        }
    }


}