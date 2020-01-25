package com.semihbarut.catchtime


import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity: AppCompatActivity() {
    private lateinit var mInterstitialAd: InterstitialAd

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        internetBaglantısınıKontrolEt()
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#03152d")))
        window.statusBarColor=Color.parseColor("#03152d")
        BannerReklamGoster()

        var kullanıcıAdı=FirebaseAuth.getInstance().currentUser?.displayName.toString()

        if (kullanıcıAdı=="Semih"){
            btnSıfırla.visibility=View.VISIBLE
        }

        btnSıfırla.setOnClickListener {

            sıfırla()
        }


        btnSeviye1.setOnClickListener {
            var intent=Intent(this@MainActivity,GameActivity::class.java)
            var seviye1:Long=150
            var puan1:Int=1
            intent.putExtra("seviye",seviye1)
            intent.putExtra("puan",puan1)
            startActivity(intent)
        }
        btnSeviye2.setOnClickListener {
            var intent=Intent(this@MainActivity,GameActivity::class.java)
            var seviye2:Long=120
            var puan2:Int=2
            intent.putExtra("seviye",seviye2)
            intent.putExtra("puan",puan2)
            startActivity(intent)
        }
        btnSeviye3.setOnClickListener {
            var intent=Intent(this@MainActivity,GameActivity::class.java)
            var seviye3:Long=90
            var puan3:Int=3
            intent.putExtra("seviye",seviye3)
            intent.putExtra("puan",puan3)
            startActivity(intent)
        }
        btnSeviye4.setOnClickListener {
            var intent=Intent(this@MainActivity,GameActivity::class.java)
            var seviye4:Long=60
            var puan4:Int=4
            intent.putExtra("seviye",seviye4)
            intent.putExtra("puan",puan4)
            startActivity(intent)
        }
        btnSeviye5.setOnClickListener {
            var intent=Intent(this@MainActivity,GameActivity::class.java)
            var seviye5:Long=40
            var puan5:Int=5
            intent.putExtra("seviye",seviye5)
            intent.putExtra("puan",puan5)
            startActivity(intent)
        }
        btnSeviye6.setOnClickListener {
            var intent=Intent(this@MainActivity,GameActivity::class.java)
            var seviye6:Long=20
            var puan6:Int=6
            intent.putExtra("seviye",seviye6)
            intent.putExtra("puan",puan6)
            startActivity(intent)
        }

        MobileAds.initialize(this,
                "ca-app-pub-****")

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-****"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener=object :AdListener(){
            override fun onAdClosed() {
                var intent=Intent(this@MainActivity,PuanSiralamasiActivity::class.java)
                startActivity(intent)
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }

    }

    private fun sıfırla() {

        FirebaseDatabase.getInstance().reference.child("kullanicilar")
                .addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onCancelled(p0: DatabaseError) {
                        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                    }

                    override fun onDataChange(p0: DataSnapshot) {
                        for(singleSnapshot in p0.children){
                            var id=singleSnapshot.key.toString()
                            FirebaseDatabase.getInstance().reference.child("kullanicilar").child(id)
                                    .child("skor").setValue("0")
                        }
                    }

                })


    }

    private fun BannerReklamGoster() {
        lateinit var mAdView : AdView
        MobileAds.initialize(this,
                "ca-app-pub-****")
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-*****"

        mAdView = findViewById(R.id.bannerReklamm)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }

    override fun onStart() {

        super.onStart()

        var kullanıcı=FirebaseAuth.getInstance().currentUser
        if (kullanıcı==null){
            var intent=Intent(this@MainActivity,LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {

        var id=item?.itemId

        when(id){
            R.id.profil->{
                var intent=Intent(this,ProfilActivity::class.java)
                startActivity(intent)
            }
            R.id.puanSistemi->{
                var puanDialog=puanBilgisi()
                puanDialog.show(supportFragmentManager,"puanDialog")
            }
            R.id.sıralama->{
                if (mInterstitialAd.isLoaded){
                    mInterstitialAd.show()
                }
                else{
                    var intent=Intent(this,PuanSiralamasiActivity::class.java)
                    startActivity(intent)
                }
            }
            R.id.oturumuKapat->{
                FirebaseAuth.getInstance().signOut()

                var intent=Intent(this,LoginActivity::class.java)
                startActivity(intent)
            }

        }
        return super.onOptionsItemSelected(item)
    }
    private fun internetBaglantısınıKontrolEt() {

        val cm=this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo=cm.activeNetworkInfo

        if (!(networkInfo!=null&&networkInfo.isConnected)){

            Toast.makeText(this,"Internet Bağlantısını Kontrol Edin\n ve Uygulamayı Tekrar Başlatın", Toast.LENGTH_LONG).show()

            btnSeviye1.visibility=View.INVISIBLE
            btnSeviye2.visibility=View.INVISIBLE
            btnSeviye3.visibility=View.INVISIBLE
            btnSeviye4.visibility=View.INVISIBLE
            btnSeviye5.visibility=View.INVISIBLE
            btnSeviye6.visibility=View.INVISIBLE
        }
    }

}
