package com.example.chatapp.Data

open class Event<out T>(val content:T) { // take ganearic type
 var hasBeenHandel = false
    fun getContentOrNot():T?{
      return  if(hasBeenHandel)null
      else{
          hasBeenHandel = true
           content
      }
    }
}