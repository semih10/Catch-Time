package com.semihbarut.catchtime


import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.AsyncTask
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profil.*
import java.io.ByteArrayOutputStream
import java.io.IOException

class ProfilActivity : AppCompatActivity(), FotoCekFragment.onProfilResimListener {

    val MB=1000000.toDouble()
    var galeridenGelenUri:Uri?=null
    var kameradanGelenBitmap:Bitmap?=null
    private var izinlerVerildi=false

    override fun getResimYolu(resimPath: Uri?) {

        galeridenGelenUri=resimPath

        Picasso.with(this).load(galeridenGelenUri).resize(400,400).into(imgCircleProfil)
    }

    override fun getResimBitmap(bitmap: Bitmap) {

        kameradanGelenBitmap=bitmap
        imgCircleProfil.setImageBitmap(bitmap)
    }


    inner class BackgroundResimCompress:AsyncTask<Uri,Double,ByteArray?>{

        var myBitmap:Bitmap?=null

        constructor(){}

        constructor(bm:Bitmap){

            if (bm!=null){
                myBitmap=bm
            }
        }
        override fun onPreExecute() {
            super.onPreExecute()
        }
        override fun doInBackground(vararg params: Uri?): ByteArray? {
            Log.e("TEST","doInBackground Starting")
            if (myBitmap==null) {

                myBitmap = MediaStore.Images.Media.getBitmap(this@ProfilActivity.contentResolver, params[0])
                //Log.e("TEST", "Resmin Boyutu:" + (myBitmap!!.byteCount).toDouble() / MB + " MB")

            }

            var resimByte:ByteArray?=null

            try {
                for(i in 1 until 10){
                    resimByte=convertBitmapToByte(myBitmap,100/i)
                     publishProgress(resimByte!!.size.toDouble())
                }
            }

            catch (e: IOException){
                Log.e("TEST","Hata2:"+e.message)
            }

            return resimByte

        }

        override fun onProgressUpdate(vararg values: Double?) {
            super.onProgressUpdate(*values)
            //Toast.makeText(this@ProfilActivity,"Şuanki boyut:"+(values[0]!!)/MB+" MB",Toast.LENGTH_SHORT).show()
        }

        override fun onPostExecute(result: ByteArray?) {
            super.onPostExecute(result)
           uploadResimToFirebase(result)

        }

    }
    fun convertBitmapToByte(myBitmap: Bitmap?, i: Int): ByteArray? {

        var stream=ByteArrayOutputStream()
        myBitmap?.compress(Bitmap.CompressFormat.JPEG,i,stream)

        return stream.toByteArray()
    }
    private fun uploadResimToFirebase(result: ByteArray?) {

        progressBarPpGoster()
        val storageReferans=FirebaseStorage.getInstance().reference
        var resimEklenecekYer = storageReferans.child("images/users/" + FirebaseAuth.getInstance().currentUser?.uid + "/profil_resim")
        // var resimEklenecekYer = storageReferans.child("images/defaultPP/profil_resim")
        var uploadGorevi = resimEklenecekYer.putBytes(result!!)
        uploadGorevi.addOnSuccessListener(object : OnSuccessListener<UploadTask.TaskSnapshot> {
            override fun onSuccess(p0: UploadTask.TaskSnapshot?) {
                progressBarPpGizle()
                Toast.makeText(this@ProfilActivity,"Profil Resmi Seçildi",Toast.LENGTH_SHORT).show()

                val urlTask = uploadGorevi.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                    if (!task.isSuccessful) {
                        task.exception?.let {
                            throw it
                        }
                    }
                    return@Continuation resimEklenecekYer.downloadUrl
                }).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val downloadUri = task.result

                        FirebaseDatabase.getInstance().reference.child("kullanicilar")
                                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                                .child("profil_resmi")
                                .setValue(downloadUri.toString())
                    } else {
                        // Handle failures
                        // ...
                    }
                }
            }

        }).addOnFailureListener {
            progressBarPpGizle()
            Toast.makeText(this@ProfilActivity,"Profil Resmi Seçilemedi:"+it.message.toString(),Toast.LENGTH_LONG).show()
        }

    }

    private fun progressBarPpGoster() {
        progressBarPP.visibility=View.VISIBLE
    }
    private fun progressBarPpGizle() {
        progressBarPP.visibility=View.INVISIBLE
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profil)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.parseColor("#17b6ee")))
        window.statusBarColor= Color.parseColor("#17b6ee")
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        tvProfilKullanıcıAdı.text=FirebaseAuth.getInstance().currentUser?.displayName
        databasedenResmiYükle()
        imgCircleProfil.setOnClickListener {

            if (izinlerVerildi){
                var dialog=FotoCekFragment()
                dialog.show(supportFragmentManager,"profilResim")
            }
            else{
                izinleriIste()
            }

        }

        btnResmiKaydet.setOnClickListener {

            if (galeridenGelenUri!=null){

                fotografCompressed(galeridenGelenUri!!)
            }
            else if (kameradanGelenBitmap!=null){

                fotografCompressed(kameradanGelenBitmap!!)
            }

        }
    }

    private fun databasedenResmiYükle() {
        var referans=FirebaseDatabase.getInstance().reference
        var kullanıcı = FirebaseAuth.getInstance().currentUser
        var sorgu=referans.child("kullanicilar").orderByKey().equalTo(kullanıcı?.uid)

        sorgu.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {
                for (singleSnapshot in p0.children){
                    var okunanKullanıcı=singleSnapshot.getValue(Kullanıcı::class.java)
                    Picasso.with(this@ProfilActivity).load(okunanKullanıcı?.profil_resmi)
                            .resize(400,400).into(imgCircleProfil)
                }
            }

        })
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return super.onSupportNavigateUp()
    }
    private fun fotografCompressed(galeridenGelenUri: Uri) {
        var compressed=BackgroundResimCompress()
        compressed.execute(galeridenGelenUri)
    }
    private fun fotografCompressed(kameradanGelenBitmap: Bitmap) {
        var compressed=BackgroundResimCompress(kameradanGelenBitmap)
        var uri:Uri?=null
        compressed.execute(uri)
    }

    private fun izinleriIste() {

        var izinler= arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (ContextCompat.checkSelfPermission(this,izinler[0])==PackageManager.PERMISSION_GRANTED&&
                ContextCompat.checkSelfPermission(this,izinler[1])==PackageManager.PERMISSION_GRANTED){

            izinlerVerildi=true
        }
        else{
            ActivityCompat.requestPermissions(this,izinler,150)
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode==150){
            if (grantResults[0]==PackageManager.PERMISSION_GRANTED&&grantResults[1]==PackageManager.PERMISSION_GRANTED){
                var dialog=FotoCekFragment()
                dialog.show(supportFragmentManager,"profilResim")
            }
            else{
                Toast.makeText(this,"Hepsine izin vermelisin",Toast.LENGTH_SHORT).show()
            }
        }


    }
}
