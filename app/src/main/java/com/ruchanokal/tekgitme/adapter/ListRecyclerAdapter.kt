package com.ruchanokal.tekgitme.adapter

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.ruchanokal.tekgitme.R
import com.ruchanokal.tekgitme.databinding.DialogLayoutBinding
import com.ruchanokal.tekgitme.databinding.RecyclerRowBinding
import com.ruchanokal.tekgitme.fragments.MainFragmentDirections
import com.ruchanokal.tekgitme.model.Way


class ListRecyclerAdapter(private val list : ArrayList<Way>,
                          private val userUid : String,
                          private val db : FirebaseFirestore,
                          private val activity : FragmentActivity,
                          private val deger : Int
) : RecyclerView.Adapter<ListRecyclerAdapter.ListHolder>() {



    class ListHolder(val binding : RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root) {

    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListRecyclerAdapter.ListHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ListHolder(binding)
    }

    override fun onBindViewHolder(holder: ListRecyclerAdapter.ListHolder, position: Int) {

        holder.binding.taksiDuragiTextView.text = list.get(position).nereden
        holder.binding.guzergahTextView.text = list.get(position).nereye
        holder.binding.zamanTextView.text = list.get(position).saat+ ":" + list.get(position).dakika
        holder.binding.detayliNereyeTextView.text = list.get(position).detayliNereye

        if (list.get(position).bosyer == 0L)
            holder.binding.bosyerTextView.text = "Boş yer yok"
        else
            holder.binding.bosyerTextView.text = list.get(position).bosyer.toString() + " kişilik boş yer"

        if (userUid.equals(list.get(position).userUid))
            holder.binding.cardView.setCardBackgroundColor(Color.parseColor("#F2F6F5"))
        else
            holder.binding.cardView.setCardBackgroundColor(Color.parseColor("#FDB813"))

        holder.itemView.setOnClickListener {

            Log.i("Adapter","deger: " + deger)

            if (deger != 3 && deger != 4) {

                if (!userUid.equals(list.get(position).userUid)) {

                    if (list.get(position).bosyer >= 1) {

                        val dialog = holder.itemView.context?.let { Dialog(it) }

                        if (dialog != null) {

                            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                            val binding2 : DialogLayoutBinding =
                                DialogLayoutBinding.inflate(LayoutInflater.from(holder.itemView.context))
                            dialog.setContentView(binding2.root)

                            binding2.neredenTextView.text = list.get(position).nereden
                            binding2.nereyeTextView.text = list.get(position).nereye
                            binding2.detayliNereyeTextView.text = list.get(position).detayliNereye
                            binding2.timeTextView.text = list.get(position).saat+ ":" + list.get(position).dakika

                            binding2.iptalButton.setOnClickListener {
                                dialog.dismiss()
                            }

                            binding2.tamamButton.setOnClickListener {tamam ->

                                val newbosyer= list.get(position).bosyer - 1

                                db.collection("YeniYolculuk").document(list.get(position).userUid).update("bosyer",newbosyer).addOnSuccessListener {
                                    dialog.dismiss()
                                    val action = MainFragmentDirections
                                        .actionMainFragmentToYolculukFragment(list.get(position).userUid,2)
                                    val navHostFragment = activity.supportFragmentManager.findFragmentById(R.id.fragmentContainerView2) as NavHostFragment
                                    val navController = navHostFragment.navController
                                    navController.navigate(action)

                                }

                            }

                            dialog.show()

                        }

                    } else
                        Toast.makeText(holder.itemView.context,"Araç dolu!",Toast.LENGTH_SHORT).show()

                }

            }
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }
}