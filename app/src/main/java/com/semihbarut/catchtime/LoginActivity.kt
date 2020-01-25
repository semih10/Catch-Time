package com.semihbarut.catchtime


import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*
import java.text.SimpleDateFormat
import java.util.*


class LoginActivity : AppCompatActivity() {

    lateinit var mAuthStateListener:FirebaseAuth.AuthStateListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)



        initMyAuthStateListener()

        tvKayıtOl.setOnClickListener {

            var intent=Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
        btnGiris.setOnClickListener {

            progressBarGöster()
            if (etMailLogin.text.toString().isNotEmpty()&&etSifreLogin.text.toString().isNotEmpty()){

                FirebaseAuth.getInstance().signInWithEmailAndPassword(etMailLogin.text.toString(),etSifreLogin.text.toString())
                        .addOnCompleteListener(object:OnCompleteListener<AuthResult>{
                            override fun onComplete(p0: Task<AuthResult>) {

                                if (p0.isSuccessful){
                                    progressBarGizle()

                                    Toast.makeText(this@LoginActivity,"Başarılı Giriş!", Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    progressBarGizle()
                                    Toast.makeText(this@LoginActivity,""+p0.exception?.message.toString(), Toast.LENGTH_SHORT).show()
                                }

                            }

                        })


            }
            else{
                Toast.makeText(this@LoginActivity,"Hiçbir Alan Boş Geçilemez!", Toast.LENGTH_SHORT).show()
                progressBarGizle()
            }

        }


    }

    override fun onBackPressed() {
        finishAffinity()
        super.onBackPressed()
    }


    private fun initMyAuthStateListener() {

        mAuthStateListener=object :FirebaseAuth.AuthStateListener{
            override fun onAuthStateChanged(p0: FirebaseAuth) {
                var kullanıcı=p0.currentUser

                if(kullanıcı!=null){//kullanıcının içinde bir şey varsa kullanıcı sisteme giriş yapmıştır
                    var intent=Intent(this@LoginActivity,MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                else{
                    FirebaseAuth.getInstance().signOut()
                }
            }

        }
    }

    private fun progressBarGöster(){
        progressBar2.visibility= View.VISIBLE
    }
    private fun progressBarGizle(){
        progressBar2.visibility= View.INVISIBLE
    }

    override fun onStart() {
        FirebaseAuth.getInstance().addAuthStateListener(mAuthStateListener)
        super.onStart()
    }

    override fun onStop() {
        FirebaseAuth.getInstance().removeAuthStateListener(mAuthStateListener)
        super.onStop()
    }

}
