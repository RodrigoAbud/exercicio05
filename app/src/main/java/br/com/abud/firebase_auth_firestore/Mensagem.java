package br.com.abud.firebase_auth_firestore;

import android.widget.ImageView;

import java.util.Date;

class Mensagem implements Comparable <Mensagem> {

    @Override
    public int compareTo(Mensagem mensagem) {
        return this.date.compareTo(mensagem.date);
    }

    private String texto;
    private Date date;
    private String email;
    private int tipoMsg;
    //private ImageView photo;

    public Mensagem () {

    }

    public Mensagem(String texto, Date date, String email, int tipoMsg/*, ImageView photo*/) {
        this.texto = texto;
        this.date = date;
        this.email = email;
        this.tipoMsg = tipoMsg;
        //this.photo = photo;
    }

    public String getTexto() {
        return texto;
    }

    public Date getDate() {
        return date;
    }

    public String getEmail() {
        return email;
    }

    public int getTipoMsg() { return tipoMsg; }

    //public ImageView getPhoto() { return photo; }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setTipoMsg(int tipoMsg) {
        this.tipoMsg = tipoMsg;
    }

    /*public void setPhoto(ImageView photo) {
        this.photo = photo;
    }*/
}
