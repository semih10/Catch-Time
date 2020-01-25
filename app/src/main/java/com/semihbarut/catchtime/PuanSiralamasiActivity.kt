package com.semihbarut.catchtime

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_puan_siralamasi.*
import java.util.ArrayList

class PuanSiralamasiActivity : AppCompatActivity() {
    var flag=0
    var sıralıIsım= arrayListOf<String>()
    var sıralıPuan= arrayListOf<String>()
    var sıralıResim= arrayListOf<String>()
    var ysıralıIsım= arrayListOf<String>()
    var ysıralıPuan= arrayListOf<String>()
    var ysıralıResim= arrayListOf<String>()
    var tumUserlar:ArrayList<Kullanıcı> = ArrayList()
    var sıralıUserlar:ArrayList<Kullanıcı> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_puan_siralamasi)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#39b7f6")))
        window.statusBarColor=Color.parseColor("#39b7f6")
        internetBaglantısınıKontrolEt()
        isimVePuanıOku()


    }

    private fun internetBaglantısınıKontrolEt() {

        val cm=this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo=cm.activeNetworkInfo

        if (!(networkInfo!=null&&networkInfo.isConnected)){

            Toast.makeText(this,"Internet Bağlantısını Kontrol Edin\n ve Uygulamayı Tekrar Başlatın",Toast.LENGTH_LONG).show()
            listViewSıralama.visibility=View.INVISIBLE
            imgCircleBirinciProfil.visibility=View.INVISIBLE
            tvBirincSkor.visibility=View.INVISIBLE
            tvIndex.visibility=View.INVISIBLE
            tvBirinciName.visibility=View.INVISIBLE
            tvSıram.visibility=View.INVISIBLE
            imageView12.visibility=View.INVISIBLE

        }
    }

    private fun isimVePuanıOku() {
        var referans = FirebaseDatabase.getInstance().reference
        var kullanıcı = FirebaseAuth.getInstance().currentUser
        var atanacakUserlar:Kullanıcı
        var sayaç=0
        var sorgu=referans.child("kullanicilar")//.orderByKey().equalTo(kullanıcı?.uid)
        var gecici: Kullanıcı

        sorgu.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {

                for (singleSnapshot in p0.children) {
                    var okunanKullanıcı = singleSnapshot.getValue(Kullanıcı::class.java)

                    sayaç++

                    atanacakUserlar = Kullanıcı(okunanKullanıcı?.user_uid!!, okunanKullanıcı?.user_name!!, okunanKullanıcı?.skor!!,okunanKullanıcı.profil_resmi)
                    tumUserlar.add(atanacakUserlar)
                    if (sayaç == p0.children.count()) {

                        sıralıUserlar = tumUserlar

                        for (i in 0 until sayaç-1) {
                           for (j in 0 until (sayaç - i-1)) {
                                if (sıralıUserlar[j].skor.toString().toInt() < sıralıUserlar[j+1].skor.toString().toInt()) {

                                    gecici = sıralıUserlar[j + 1]
                                    sıralıUserlar[j + 1] = sıralıUserlar[j]
                                    sıralıUserlar[j] = gecici

                               }

                           }

                        }

                       for (i in 0 until sayaç){
                            sıralıIsım.add(sıralıUserlar[i].user_name.toString())
                            sıralıPuan.add(sıralıUserlar[i].skor.toString())
                            sıralıResim.add(sıralıUserlar[i].profil_resmi)

                        }

                        tvBirinciName.text=sıralıIsım[0]
                        tvBirincSkor.text=sıralıPuan[0]
                        Picasso.with(this@PuanSiralamasiActivity).load(sıralıResim[0])
                                .resize(400,400).into(imgCircleBirinciProfil)

                        for (i in 0 until sayaç-1){
                            ysıralıIsım.add(sıralıUserlar[i+1].user_name.toString())
                            ysıralıPuan.add(sıralıUserlar[i+1].skor.toString())
                            ysıralıResim.add(sıralıUserlar[i+1].profil_resmi)
                        }
                        var myAdapter = PuanArrayAdapter(this@PuanSiralamasiActivity, R.layout.tek_satir_puan, R.id.tvUsName, ysıralıIsım, ysıralıPuan,ysıralıResim)
                        listViewSıralama.adapter = myAdapter


                        for(i in 0 until sayaç){

                            if (sıralıIsım[i]==FirebaseAuth.getInstance().currentUser?.displayName){
                                flag=i
                            }
                        }
                        tvSıram.text=""+(flag+1).toString()+". Sıradasın"


                    }
                }

            }

        })

    }


    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }
}
