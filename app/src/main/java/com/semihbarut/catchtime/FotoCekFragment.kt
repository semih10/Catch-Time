package com.semihbarut.catchtime


import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.DialogFragment
import android.support.v4.app.Fragment
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.fragment_foto_cek.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class FotoCekFragment : DialogFragment() {

    interface onProfilResimListener{

        fun getResimYolu(resimPath:Uri?)
        fun getResimBitmap(bitmap:Bitmap)
    }
    lateinit var mProfilResimListener:onProfilResimListener
    lateinit var tvGaleridenSec:TextView


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        var v=inflater.inflate(R.layout.fragment_foto_cek, container, false)

        tvGaleridenSec=v.findViewById(R.id.tvGaleriden)


        tvGaleridenSec.setOnClickListener {

            var intent=Intent(Intent.ACTION_GET_CONTENT)
            intent.type=("image/*")
            startActivityForResult(intent,100)
        }

        return v
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==100 && resultCode==Activity.RESULT_OK && data!=null) {

            var galeridenGelenResimYolu = data.data
            Log.e("YOL", ">>" + galeridenGelenResimYolu)
            mProfilResimListener.getResimYolu(galeridenGelenResimYolu)
            dismiss()

        }


    }

    override fun onAttach(context: Context?) {

        mProfilResimListener=activity as onProfilResimListener
        //fragmenti çağıran aktivitiye ilişkilendirdik
        super.onAttach(context)
    }


}
