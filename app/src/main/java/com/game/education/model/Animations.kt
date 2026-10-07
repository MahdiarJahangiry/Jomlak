package com.game.education.model

import android.widget.TextView
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.SpringAnimation

class Animations
{
    private var stiffness = 400f
    private var dampingRatio = 0.75f

    fun SetAnim(img: TextView,initialX:Float,initialY:Float)
    {
        SpringAnimation(img, DynamicAnimation.Y, initialY)
            .apply {
                spring.stiffness = stiffness
                spring.dampingRatio = dampingRatio
                start()
            }
        SpringAnimation(img, DynamicAnimation.X, initialX)
            .apply {
                spring.stiffness = stiffness
                spring.dampingRatio = dampingRatio
                start()
            }
    }





}//class