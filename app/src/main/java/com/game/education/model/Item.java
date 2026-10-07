package com.game.education.model;

import android.widget.TextView;

public class Item
{
    public boolean magnet = false;
    public boolean active;
    public float x,y;
    public float Mx,My;
    public String word;
    public TextView btn;

    public Item()
    {
        this(false,0f,0f,"",null,0f,0f);
    }

    public Item(Boolean active, float x, float y, String word,TextView btn, float Mx, float My){
        this.active = active;
        this.x = x;
        this.y = y;
        this.word = word;
        this.btn = btn;
        this.Mx = Mx;
        this.My = My;
    }

    public float getMx() {
        return Mx;
    }

    public float getMy() {
        return My;
    }

    public void setMx(float mx) {
        Mx = mx;
    }

    public void setMy(float my) {
        My = my;
    }

    public TextView getBtn() {
        return btn;
    }

    public void setBtn(TextView btn) {
        this.btn = btn;
    }

    public Boolean getActive() {
        return active;
    }

    public Boolean getMagnet() {
        return magnet;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public void setMagnet(Boolean magnet) {
        this.magnet = magnet;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }


}
