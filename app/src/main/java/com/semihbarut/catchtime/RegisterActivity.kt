package com.semihbarut.catchtime


import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_register.*


class RegisterActivity : AppCompatActivity()  {

    var flag=0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)



        btnKayıtOl.setOnClickListener {

            if (etMailRegister.text.toString().isNotEmpty() && etSifreRegister.text.toString().isNotEmpty() && etSifreTekrarRegister.text.toString().isNotEmpty()) {

                if (etSifreRegister.text.toString().equals(etSifreTekrarRegister.text.toString())) {

                    if ((etKullanıcıAdı.text.toString().isEmpty() || etKullanıcıAdı.text.toString().length < 3)||etKullanıcıAdı.text.toString().length >=10) {
                        Toast.makeText(this, "Kullanıcı Adı 3-9 Harften Oluşmalı", Toast.LENGTH_SHORT).show()
                    }

                    else {

                        isUsernameTaken()
                        //yeniÜyeKayıt(etMailRegister.text.toString(), etSifreRegister.text.toString())
                    }

                }
                else {
                    Toast.makeText(this, "Şifreler Uyuşmuyor", Toast.LENGTH_SHORT).show()
                }

            }
            else {
                Toast.makeText(this, "Hiçbir Alan Boş Geçilemez!", Toast.LENGTH_SHORT).show()
            }

        }

    }

    override fun onBackPressed() {
        FirebaseAuth.getInstance().signOut()
        super.onBackPressed()
    }
    override fun onResume() {

        FirebaseAuth.getInstance().signOut()

        super.onResume()
    }
    override fun onStop() {
        FirebaseAuth.getInstance().signOut()
        super.onStop()
    }

    override fun onDestroy() {
        FirebaseAuth.getInstance().signOut()
        super.onDestroy()
    }
    private fun isUsernameTaken() {
        progressBarGöster()
        var sayaç=0
        flag=0
        var list:ArrayList<String> = ArrayList()
        var referans = FirebaseDatabase.getInstance().reference
        var sorgu=referans.child("kullanicilar")//.orderByKey().equalTo(kullanıcı?.uid)

        sorgu.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onDataChange(p0: DataSnapshot) {

                for (singleSnapshot in p0.children) {
                    var okunanKullanıcı = singleSnapshot.getValue(Kullanıcı::class.java)
                    list.add(okunanKullanıcı?.user_name!!)
                    sayaç++

                    if (sayaç == p0.children.count()) {

                        for (i in 0 until sayaç) {
                             if (list[i]==etKullanıcıAdı.text.toString()) {
                                 flag = 1
                                 break
                             }
                            else {
                                 flag = 0
                            }

                        }

                        if (flag == 1) {
                            progressBarGizle()
                            Toast.makeText(this@RegisterActivity, "Kullanıcı adı alınmış!!!", Toast.LENGTH_SHORT).show()
                        } else {

                            yeniÜyeKayıt(etMailRegister.text.toString(), etSifreRegister.text.toString())
                            FirebaseAuth.getInstance().signOut()
                        }
                    }
                }
            }
        })


    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        FirebaseAuth.getInstance().signOut()
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }


    private fun yeniÜyeKayıt(mail: String, sifre: String) {

        progressBarGöster()

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(mail,sifre)
                .addOnCompleteListener(object :OnCompleteListener<AuthResult>{
            override fun onComplete(p0: Task<AuthResult>) {

                if(p0.isSuccessful){
                    progressBarGizle()


                    var kullanıcıAdıOluştur = UserProfileChangeRequest.Builder()
                            .setDisplayName(etKullanıcıAdı.text.toString()).build()
                    FirebaseAuth.getInstance().currentUser!!.updateProfile(kullanıcıAdıOluştur)

                    var eklenecekKullanıcı=Kullanıcı()
                    eklenecekKullanıcı.user_name=etKullanıcıAdı.text.toString()
                    eklenecekKullanıcı.user_uid=FirebaseAuth.getInstance().currentUser!!.uid
                    eklenecekKullanıcı.skor="0"
                    //eklenecekKullanıcı.user_mail=etMailRegister.text.toString()
                   // eklenecekKullanıcı.puan_dk=0

                    FirebaseDatabase.getInstance().reference.child("kullanicilar")
                            .child(FirebaseAuth.getInstance().currentUser!!.uid).setValue(eklenecekKullanıcı)
                            .addOnCompleteListener(object :OnCompleteListener<Void>{
                                override fun onComplete(p0: Task<Void>) {
                                    if(p0.isSuccessful){
                                        Toast.makeText(this@RegisterActivity,"Kayıt Olundu",Toast.LENGTH_SHORT).show()
                                    }
                                    else{
                                        Toast.makeText(this@RegisterActivity,"Kayıt Olunamadı"+p0.exception?.message.toString(),Toast.LENGTH_SHORT).show()
                                    }
                                }

                            })


                }
                else{
                    progressBarGizle()
                    Toast.makeText(this@RegisterActivity,""+p0.exception?.message,Toast.LENGTH_SHORT).show()
                }

            }

        })


    }
    private fun progressBarGöster(){
        progressBar.visibility=View.VISIBLE
    }
    private fun progressBarGizle(){
        progressBar.visibility=View.INVISIBLE
    }
}
