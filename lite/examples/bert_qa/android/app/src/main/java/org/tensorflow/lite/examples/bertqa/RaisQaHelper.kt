package org.tensorflow.lite.examples.bertqa

import android.util.Log
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.HttpClient
import com.google.firebase.crashlytics.buildtools.reloc.org.apache.http.client.methods.HttpPost
import org.json.JSONObject
import org.tensorflow.lite.task.text.qa.QaAnswer
import java.io.BufferedOutputStream
import java.io.IOException
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URL


class RaisQaHelper(endpoint: String) {
    var endpoint: String
    init {
        this.endpoint = endpoint
    }
    fun run(context: String,question: String): QaAnswer {
        var jsonObject = JSONObject()
        jsonObject.put("text",context)
        jsonObject.put("question",question)
        var jsonString = jsonObject.toString()
        var data_poster = PostData()
        data_poster.init()
        var answer = data_poster.send_request(jsonString)
        return answer
    }
}

