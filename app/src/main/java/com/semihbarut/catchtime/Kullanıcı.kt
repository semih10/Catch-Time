package com.semihbarut.catchtime

class Kullanıcı {

    var user_name:String?=null
    var skor:String?=null
    var user_uid:String?=null
    var puan_dk:Int=0
    var profil_resmi:String="https://firebasestorage.googleapis.com/v0/b/catchtimer-5e1ae.appspot.com/o/images%2FdefaultPP%2FdefaultPP.jpg?alt=media&token=c86890da-96ff-4323-806d-7674f79ac82b"
     constructor(uid:String,kullanıcıAdı:String,puan:String,resim:String/*,dk:Int*/){
         user_name=kullanıcıAdı
         skor=puan
         user_uid=uid
         profil_resmi=resim

     }

    constructor(){

    }
}