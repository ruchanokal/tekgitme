package com.ruchanokal.tekgitme.fragments

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.ruchanokal.tekgitme.R
import com.ruchanokal.tekgitme.activities.MainActivity
import com.ruchanokal.tekgitme.databinding.FragmentSignInBinding
import com.ruchanokal.tekgitme.databinding.FragmentSignUpBinding


class SignUpFragment : Fragment() {

    private var binding: FragmentSignUpBinding? = null
    private lateinit var mAuth: FirebaseAuth
    private var sifre : String = ""
    private var sifre2 : String = ""
    var userUid = ""
    private lateinit var db : FirebaseFirestore
    private lateinit var hashMap : HashMap<Any,Any>
    private lateinit var kullaniciAdi : String
    private lateinit var email : String
    private lateinit var listener : ListenerRegistration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        val view = binding!!.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAuth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding?.kayitOlButton?.setOnClickListener {

            binding?.progressBarSignUp?.visibility = View.VISIBLE

            email = binding?.editTextEmailKayit?.text.toString()
            kullaniciAdi = binding?.editTextKullaniciAdiKayit?.text.toString()
            sifre = binding?.editTextSifreKayit?.text.toString()
            sifre2 = binding?.editTextSifreKayit2?.text.toString()


            hashMap = hashMapOf<Any,Any>()
            hashMap.put("email",email)
            hashMap.put("kullaniciAdi",kullaniciAdi)


            databaseCollection()

        }

        val callback = object  : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack()
            }

        }

        requireActivity().onBackPressedDispatcher.addCallback(callback)
    }

    private fun databaseCollection() {

        val query = db.collection("Users").whereEqualTo("kullaniciAdi",kullaniciAdi)

        listener  = query.addSnapshotListener { value, error ->

            if (value !=null) {
                println("null de??il")

                if ( !value.isEmpty) {
                    println("empty de??il")

                    Toast.makeText(context,"L??tfen ba??ka bir kullan??c?? ad?? deneyin!",
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


        if (email.equals("") || kullaniciAdi.equals("") || sifre.equals("")){

            listener.remove()

            Toast.makeText(activity,"L??tfen gerekli alanlar?? doldurunuz",Toast.LENGTH_LONG).show()

            binding?.progressBarSignUp?.visibility = View.GONE

        } else if (!sifre.equals(sifre2)){

            listener.remove()

            Toast.makeText(activity,"??ifreler ayn?? olmal??d??r!",Toast.LENGTH_LONG).show()

            binding?.progressBarSignUp?.visibility = View.GONE

        }  else  {

            mAuth.createUserWithEmailAndPassword(email,sifre).addOnCompleteListener { task ->

                if (task.isSuccessful){

                    listener.remove()

                    userUid = mAuth.currentUser?.uid.toString()

                    binding?.progressBarSignUp?.visibility = View.GONE

                    val intent = Intent(activity, MainActivity::class.java)
                    intent.putExtra("kullaniciAdi",kullaniciAdi)
                    intent.putExtra("definite",1)
                    startActivity(intent)
                    requireActivity().finish()

                    Toast.makeText(activity,"Ho??geldin ${kullaniciAdi}",Toast.LENGTH_LONG).show()

                }

            }.addOnFailureListener { exception ->


                try {
                    throw exception
                }   catch (e: FirebaseAuthUserCollisionException) {

                    listener.remove()

                    Toast.makeText(activity,"Bu e-posta adresi zaten ba??ka bir hesap taraf??ndan kullan??l??yor",Toast.LENGTH_LONG).show()
                    binding?.progressBarSignUp?.visibility = View.GONE

                } catch(e : FirebaseAuthWeakPasswordException) {

                    listener.remove()

                    Toast.makeText(activity,"L??tfen en az 6 haneli bir ??ifre giriniz",Toast.LENGTH_LONG).show()
                    binding?.progressBarSignUp?.visibility = View.GONE

                } catch (e: FirebaseNetworkException) {

                    listener.remove()

                    Toast.makeText(activity,"L??tfen internet ba??lant??n??z?? kontrol edin",Toast.LENGTH_LONG).show()
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


}