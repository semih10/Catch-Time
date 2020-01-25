package com.semihbarut.catchtime

import android.content.Context
import android.media.Image
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profil.*
import kotlinx.android.synthetic.main.tek_satir_puan.view.*

class PuanArrayAdapter(var gelenContext: Context, resource: Int, textViewResourceId: Int,var kullanıcıIsim:ArrayList<String>, var skorlar: ArrayList<String>,var resimYol:ArrayList<String>)
    : ArrayAdapter<String>(gelenContext, resource, textViewResourceId, kullanıcıIsim) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {

        var tekSatırView=convertView
        var viewHolder:ViewHolder?=null

        if (tekSatırView==null){

            val inflater=LayoutInflater.from((gelenContext))
            tekSatırView=inflater.inflate(R.layout.tek_satir_puan,parent,false)
            viewHolder=ViewHolder(tekSatırView)
            tekSatırView.tag=viewHolder

        }
        else{
            viewHolder=tekSatırView.tag as ViewHolder
        }



        Picasso.with(gelenContext).load(resimYol[position])
                .resize(400,400).into(viewHolder.resim)
        viewHolder.isim.setText(kullanıcıIsim[position])
        viewHolder.puan.setText(skorlar[position])
        viewHolder.sıra.text=((position+2).toString()+".")
        return tekSatırView!!
    }
    class ViewHolder(tek_satır_view:View){

        var isim:TextView
        var puan:TextView
        var sıra:TextView
        var resim:ImageView
        init {
            this.isim = tek_satır_view.tvUsName
            this.puan = tek_satır_view.tvUsPuan
            this.sıra = tek_satır_view.tvUsIndex
            this.resim=tek_satır_view.imgUsCircleProfil
        }

    }
}