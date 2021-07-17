package com.plcoding.spotifyclone.other

/**
 * Created by Dhruv Limbachiya on 15-07-2021.
 */
/**
 * Used as a wrapper for data that is exposed via a LiveData that represents an event.
 */
open class Event<out T>(private val content: T) {

    var hasBeenHandled = false // Allow external Read
    private set // Disallow external write.

    /**
     * Get the content if it is not handled already.
     */
    fun getContentIfNotHandled(): T?{
        return if(hasBeenHandled){
            null
        }else{
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content if it is already handled.
     */
    fun peekContent(): T = content
}