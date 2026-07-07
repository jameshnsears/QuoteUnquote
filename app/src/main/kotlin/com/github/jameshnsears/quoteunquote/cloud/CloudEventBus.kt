package com.github.jameshnsears.quoteunquote.cloud

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

object CloudEventBus {
    private val eventSubject = PublishSubject.create<String>()

    @JvmStatic
    fun post(event: String) {
        eventSubject.onNext(event)
    }

    @JvmStatic
    fun getEvents(): Observable<String> {
        return eventSubject
    }
}
