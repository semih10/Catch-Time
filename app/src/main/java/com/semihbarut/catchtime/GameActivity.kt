package com.semihbarut.catchtime



import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.ConnectivityManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.ads.*
import com.google.android.gms.ads.reward.RewardItem
import com.google.android.gms.ads.reward.RewardedVideoAd
import com.google.android.gms.ads.reward.RewardedVideoAdListener

import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_game.*
import kotlinx.android.synthetic.main.activity_login.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.jar.Manifest


class GameActivity : AppCompatActivity(), RewardedVideoAdListener {
    private var isIncrease:Boolean=false

    override fun onRewardedVideoAdClosed() {

        if(isIncrease==false) {
            loadRewardedVideoAd()
        }

    }

    override fun onRewardedVideoAdLeftApplication() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRewardedVideoAdLoaded() {

        var time= Calendar.getInstance().time
        var zaman= SimpleDateFormat("mm")
        var dk=zaman.format(time).toInt()
        var dat_dk:Int=0
        var referans=FirebaseDatabase.getInstance().reference
        var kullanıcı = FirebaseAuth.getInstance().currentUser
        var sorgu=referans.child("kullanicilar").orderByKey().equalTo(kullanıcı?.uid)

        sorgu.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (singleSnapshot in p0.children) {
                    var okunanKullanıcı = singleSnapshot.getValue(Kullanıcı::class.java)

                     dat_dk=okunanKullanıcı!!.puan_dk
                }

                if (dk-dat_dk>=10||dk-dat_dk<=-10){//reklamı izledikten 10 dk önce ve sonrasındaki dakikalarda btn aktif
                    btnÖdül.visibility=View.VISIBLE
                    //loadRewardedVideoAd()

                }
            }

        })
    }

    override fun onRewardedVideoAdOpened() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRewardedVideoCompleted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRewarded(p0: RewardItem?) {

        btnÖdül.visibility=View.INVISIBLE
        //loadRewardedVideoAd()
        isIncrease=true
        tvSkor.text=(tvSkor.text.toString().toInt()+20).toString()
        Toast.makeText(this,"+20 puan kazandınız",Toast.LENGTH_SHORT).show()
        FirebaseDatabase.getInstance().reference.child("kullanicilar")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).child("skor").setValue(tvSkor.text.toString())
                .addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        if(p0.isSuccessful){
                            // Toast.makeText(this@RegisterActivity,"Üye Kaydedildi",Toast.LENGTH_SHORT).show()
                        }
                        else{
                            // Toast.makeText(this@RegisterActivity,"Hata database"+p0.exception?.message.toString(),Toast.LENGTH_SHORT).show()
                        }
                    }

                })
        var time= Calendar.getInstance().time
        var zaman= SimpleDateFormat("mm")
        var dk=zaman.format(time)
        FirebaseDatabase.getInstance().reference.child("kullanicilar")
                .child(FirebaseAuth.getInstance().currentUser!!.uid).child("puan_dk").setValue(dk.toInt())
                .addOnCompleteListener(object : OnCompleteListener<Void> {
                    override fun onComplete(p0: Task<Void>) {
                        if(p0.isSuccessful){
                            // Toast.makeText(this@RegisterActivity,"Üye Kaydedildi",Toast.LENGTH_SHORT).show()
                        }
                        else{
                            // Toast.makeText(this@RegisterActivity,"Hata database"+p0.exception?.message.toString(),Toast.LENGTH_SHORT).show()
                        }
                    }

                })
    }

    override fun onRewardedVideoStarted() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun onRewardedVideoAdFailedToLoad(p0: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private lateinit var mInterstitialAd: InterstitialAd
    private lateinit var mRewardedVideoAd: RewardedVideoAd

    var gelenSeviye:Long=0
    var gelenPuan:Int=0
    private var isPaused:Boolean=false
    private var isCanceled:Boolean=false
    private var remainingTimeSalise:Long=0
    private lateinit var geriSayım: CountDownTimer
    private lateinit var timerSalise: CountDownTimer
    private lateinit var timerSalise2: CountDownTimer
    private var geriSay: Long = 6
    private var salise: Long = 99
    private var can: Int = 5
    private val tamCan=5
    private var skor=0
    private var toplamSkor=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)
        internetBaglantısınıKontrolEt()
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#ffb155")))
        window.statusBarColor=Color.parseColor("#ffb155")

        veriTabanındanPuanıOku()
        BannerReklamGoster()
        oyunaBasla()

        MobileAds.initialize(this,
                "ca-app-pub-******")

        mInterstitialAd = InterstitialAd(this)
        mInterstitialAd.adUnitId = "ca-app-pub-*****"
        mInterstitialAd.loadAd(AdRequest.Builder().build())
        mInterstitialAd.adListener=object :AdListener(){
            override fun onAdClosed() {
                var intent=Intent(this@GameActivity,PuanSiralamasiActivity::class.java)
                startActivity(intent)
                mInterstitialAd.loadAd(AdRequest.Builder().build())
            }
        }
        //Rewarded
        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this)
        mRewardedVideoAd.rewardedVideoAdListener = this

        loadRewardedVideoAd()
        btnÖdül.setOnClickListener {
            btnÖdül.visibility=View.INVISIBLE
            if (mRewardedVideoAd.isLoaded) {
                mRewardedVideoAd.show()
            }
            else{
                Toast.makeText(this,"Sonra Tekrar Deneyin!",Toast.LENGTH_SHORT).show()
            }
        }

        var gelenBilgiSeviye=intent.extras
        gelenSeviye=gelenBilgiSeviye.getLong("seviye")
        var gelenBilgiPuan=intent.extras
        gelenPuan=gelenBilgiPuan.getInt("puan")

    }

    private fun loadRewardedVideoAd() {
        mRewardedVideoAd.loadAd("ca-app-pub-****",
                AdRequest.Builder().build())
    }

    private fun BannerReklamGoster() {
        lateinit var mAdView : AdView
        MobileAds.initialize(this,
                "ca-app-pub-****")
        val adView = AdView(this)
        adView.adSize = AdSize.BANNER
        adView.adUnitId = "ca-app-pub-****"

        mAdView = findViewById(R.id.bannerReklam)
        val adRequest = AdRequest.Builder().build()
        mAdView.loadAd(adRequest)
    }


    override fun onPause() {
        super.onPause()
        mRewardedVideoAd.pause(this)
    }
    override fun onResume() {
        super.onResume()
        mRewardedVideoAd.resume(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mRewardedVideoAd.destroy(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.ana_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        var id=item?.itemId

        when(id){
            R.id.profil->{
                var intent=Intent(this@GameActivity,ProfilActivity::class.java)
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
                   var intent=Intent(this@GameActivity,PuanSiralamasiActivity::class.java)
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
    private fun oyunaBasla() {

        var randomSalise=(1..99).shuffled().first()

        geriSayım = object : CountDownTimer(geriSay*1000 , 1000) {
            override fun onFinish() {
                tvGeriSayım.text=""
                tvGeriSayım.visibility=View.INVISIBLE
                btnDurdur.isEnabled=true
                btnDurdur.setBackgroundResource(R.drawable.pause)

                oyun(gelenSeviye)
            }
            override fun onTick(millisUntilFinished: Long) {
                geriSay = millisUntilFinished / 1000
               if(geriSay.toInt()>3){
                   tvGeriSayım.text="HAZIRLAN!"

               }
               else if(geriSay.toInt()<=3&&geriSay.toInt()>0){
                   tvGeriSayım.text=geriSay.toString()



                   tvTargetSalise.text=randomSalise.toString()
               }
                else{
                   tvGeriSayım.text="BAŞLA"
               }
            }
        }.start()
        btnDevam.isEnabled=false
        btnDevam.setBackgroundResource(R.drawable.play_tiklanmis)


    }
    private fun veriTabanındanPuanıOku() {

        var referans = FirebaseDatabase.getInstance().reference
        var kullanıcı = FirebaseAuth.getInstance().currentUser

        var sorgu=referans.child("kullanicilar").orderByKey().equalTo(kullanıcı?.uid)

        sorgu.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (singleSnapshot in p0.children){
                    var okunanKullanıcı=singleSnapshot.getValue(Kullanıcı::class.java)
                    tvSkor.setText(okunanKullanıcı?.skor)
                    toplamSkor=okunanKullanıcı?.skor.toString().toInt()
                }
            }

        })


    }
    private fun oyun(x:Long) {

        isCanceled=false
        isPaused=false



        timerSalise = object : CountDownTimer(salise*x , x) {
            override fun onFinish(){

                timerSalise.start()
            }
            override fun onTick(millisUntilFinished: Long) {
                if(isPaused||isCanceled){
                    cancel()
                }
                else{
                    tvSalise.setText((""+millisUntilFinished / x))
                    remainingTimeSalise=millisUntilFinished

                }

            }
        }.start()


        btnDurdur.setOnClickListener {

            oyunuDurdur()

        }

        btnDevam.setOnClickListener {

            oyunaDevam(gelenSeviye)

        }
        btnTekrar.setOnClickListener {

            oyunTekrar()

        }

    }

    private fun oyunTekrar() {

        geriSay=6
        can=tamCan
        tvTargetSalise.text="00"
        tvSalise.text="00"
        tvCan.text=can.toString()
        tvGeriSayım.visibility=View.VISIBLE
        btnTekrar.visibility=View.INVISIBLE
        btnDurdur.visibility=View.VISIBLE
        btnDevam.visibility=View.VISIBLE

        btnDurdur.isEnabled=false
        btnDurdur.setBackgroundResource(R.drawable.pause_tiklanmis)
        btnDevam.isEnabled=false
        btnDevam.setBackgroundResource(R.drawable.play_tiklanmis)

        oyunaBasla()
    }

    private fun oyunaDevam(x:Long) {
        isPaused=false
        isCanceled=false


        timerSalise2 = object : CountDownTimer(remainingTimeSalise , x) {
            override fun onFinish(){

                timerSalise.start()
            }
            override fun onTick(millisUntilFinished: Long) {
                if(isPaused||isCanceled){
                    cancel()
                }
                else{
                    tvSalise.setText(""+millisUntilFinished / x)
                    remainingTimeSalise=millisUntilFinished

                }

            }
        }.start()

        btnDurdur.isEnabled=true
        btnDurdur.setBackgroundResource(R.drawable.pause)
        btnDevam.isEnabled=false
        btnDevam.setBackgroundResource(R.drawable.play_tiklanmis)

    }

    private fun oyunuDurdur() {

        isPaused = true
        isCanceled = false



        if (tvSalise.text == tvTargetSalise.text) {
            Toast.makeText(this, "Tebrikler", Toast.LENGTH_LONG).show()
            alkıscal()
            titret()
            if(gelenPuan==1){
                skor=can*2
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor+skor
                tvSkor.text=toplamSkor.toString()
            }
            if(gelenPuan==2){
                skor=can*3
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor+skor
                tvSkor.text=toplamSkor.toString()
            }
            if(gelenPuan==3){
                skor=can*4
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor+skor
                tvSkor.text=toplamSkor.toString()
            }
            if(gelenPuan==4){
                skor=can*5
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor+skor
                tvSkor.text=toplamSkor.toString()
            }
            if (gelenPuan==5){
                skor=can*7
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor+skor
                tvSkor.text=toplamSkor.toString()
            }
            if (gelenPuan==6){
                skor=can*10
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor+skor
                tvSkor.text=toplamSkor.toString()
            }

            FirebaseDatabase.getInstance().reference.child("kullanicilar")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid).child("skor").setValue(toplamSkor.toString())
                    .addOnCompleteListener(object : OnCompleteListener<Void> {
                        override fun onComplete(p0: Task<Void>) {
                            if(p0.isSuccessful){
                                // Toast.makeText(this@RegisterActivity,"Üye Kaydedildi",Toast.LENGTH_SHORT).show()
                            }
                            else{
                                // Toast.makeText(this@RegisterActivity,"Hata database"+p0.exception?.message.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }

                    })

            can=tamCan
            btnDevam.visibility=View.INVISIBLE
            btnDurdur.visibility=View.INVISIBLE
            btnTekrar.visibility=View.VISIBLE


        }
        else{
            can--
            tvCan.text = can.toString()
        }
        if (can == 0) {
            can++
            btnDevam.visibility=View.INVISIBLE
            btnDurdur.visibility=View.INVISIBLE
            btnTekrar.visibility=View.VISIBLE

            if (gelenPuan==1){
                skor=8
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor-skor
                tvSkor.text=toplamSkor.toString()
            }
            if (gelenPuan==2){
                skor=6
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor-skor
                tvSkor.text=toplamSkor.toString()
            }
            if (gelenPuan==3){
                skor=4
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor-skor
                tvSkor.text=toplamSkor.toString()
            }
            if (gelenPuan==4||gelenPuan==5){
                skor=1
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor-skor
                tvSkor.text=toplamSkor.toString()
            }
            if (gelenPuan==6){
                skor=0
                toplamSkor=tvSkor.text.toString().toInt()
                toplamSkor=toplamSkor-skor
                tvSkor.text=toplamSkor.toString()
            }

            FirebaseDatabase.getInstance().reference.child("kullanicilar")
                    .child(FirebaseAuth.getInstance().currentUser!!.uid).child("skor").setValue(toplamSkor.toString())
                    .addOnCompleteListener(object : OnCompleteListener<Void> {
                        override fun onComplete(p0: Task<Void>) {
                            if(p0.isSuccessful){
                                // Toast.makeText(this@RegisterActivity,"Üye Kaydedildi",Toast.LENGTH_SHORT).show()
                            }
                            else{
                                // Toast.makeText(this@RegisterActivity,"Hata database"+p0.exception?.message.toString(),Toast.LENGTH_SHORT).show()
                            }
                        }

                    })
            Toast.makeText(this, "Kaybettiniz", Toast.LENGTH_LONG).show()
            üzgünSesÇal()
        }

        btnDurdur.isEnabled=false
        btnDurdur.setBackgroundResource(R.drawable.pause_tiklanmis)
        btnDevam.isEnabled=true
        btnDevam.setBackgroundResource(R.drawable.play)
    }

    private fun üzgünSesÇal() {
        var üzgün=MediaPlayer.create(this,R.raw.sad)
        üzgün.start()
    }

    private fun titret() {
        val vib = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vib.vibrate(500)
    }

    private fun alkıscal() {

        var alkıs=MediaPlayer.create(this,R.raw.win)
        alkıs.start()


    }
    private fun internetBaglantısınıKontrolEt() {

        val cm=this.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo=cm.activeNetworkInfo

        if (!(networkInfo!=null&&networkInfo.isConnected)){

            Toast.makeText(this,"Internet Bağlantısını Kontrol Edin\n ve Uygulamayı Tekrar Başlatın",Toast.LENGTH_LONG).show()
            btnDurdur.visibility=View.INVISIBLE
            btnDevam.visibility=View.INVISIBLE
            tvSalise.visibility=View.INVISIBLE
            tvGeriSayım.visibility=View.INVISIBLE
            imageView6.visibility=View.INVISIBLE
            imageView9.visibility=View.INVISIBLE
            tvCan.visibility=View.INVISIBLE
            tvTarget.visibility=View.INVISIBLE
            tvTargetSalise.visibility=View.INVISIBLE
        }
    }


}
