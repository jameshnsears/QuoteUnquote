package com.github.jameshnsears.quoteunquote.cloud

class RequestTestHelper {
    companion object {
        const val code = "4EXRqu8N68"

        fun sendRequest(): RequestSave {
            val requestSave = RequestSave()
            requestSave.code = code

            val digests = ArrayList<String>()
            digests.add("d0")
            digests.add("d1")
            requestSave.digests = digests

            return requestSave
        }

        fun receiveRequest(): RequestReceive {
            val requestReceive = RequestReceive()
            requestReceive.code = code
            return requestReceive
        }
    }
}
