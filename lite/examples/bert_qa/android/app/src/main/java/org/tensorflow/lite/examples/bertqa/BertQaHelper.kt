/*
 * Copyright 2022 The TensorFlow Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tensorflow.lite.examples.bertqa

import android.R
import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import android.view.View
import android.widget.Switch
import android.widget.TextView
import org.tensorflow.lite.gpu.CompatibilityList
import org.tensorflow.lite.task.core.BaseOptions
import org.tensorflow.lite.task.text.qa.BertQuestionAnswerer
import org.tensorflow.lite.task.text.qa.BertQuestionAnswerer.BertQuestionAnswererOptions
import org.tensorflow.lite.task.text.qa.QaAnswer


class BertQaHelper(
    val context: Context,
    var numThreads: Int = 2,
    var currentDelegate: Int = 0,
    val answererListener: AnswererListener?
) {
    private var useRais: Boolean = false //make this a toggle button
    private var bertQuestionAnswerer: BertQuestionAnswerer? = null
    private var raisQaHelper: RaisQaHelper? = null
    init {
        val toggle = (context as Activity).findViewById<View>(org.tensorflow.lite.examples.bertqa.R.id.switch1) as Switch
        useRais = toggle.isChecked
        if (useRais) {
            raisQaHelper = RaisQaHelper("192.168.1.64")
        } else {
            setupBertQuestionAnswerer()
        }
    }

    fun clearBertQuestionAnswerer() {
        bertQuestionAnswerer = null
    }

    private fun setupBertQuestionAnswerer() {
        val baseOptionsBuilder = BaseOptions.builder().setNumThreads(numThreads)
        when (currentDelegate) {
            DELEGATE_CPU -> {
                // Default
            }
            DELEGATE_GPU -> {
                if (CompatibilityList().isDelegateSupportedOnThisDevice) {
                    baseOptionsBuilder.useGpu()
                } else {
                    answererListener?.onError("GPU is not supported on this device")
                }
            }
            DELEGATE_NNAPI -> {
                baseOptionsBuilder.useNnapi()
            }
        }
        val options = BertQuestionAnswererOptions.builder()
            .setBaseOptions(baseOptionsBuilder.build())
            .build()
        try {
            bertQuestionAnswerer =
                BertQuestionAnswerer.createFromFileAndOptions(context, BERT_QA_MODEL, options)
        } catch (e: IllegalStateException) {
            answererListener
                ?.onError("Bert Question Answerer failed to initialize. See error logs for details")
            Log.e(TAG, "TFLite failed to load model with error: " + e.message)
        }
    }

    fun answer(contextOfQuestion: String, question: String) {
        var inferenceTime = SystemClock.uptimeMillis()
        if (!useRais && bertQuestionAnswerer == null) {
            setupBertQuestionAnswerer()
        }
        var answers: List<QaAnswer>? = null
        if (useRais && raisQaHelper != null) {
            //todo make switch, return result
            var result = raisQaHelper!!.run(contextOfQuestion, question)
            answers = listOf(result)
        } else {
            answers = bertQuestionAnswerer?.answer(contextOfQuestion, question)
        }
        inferenceTime = SystemClock.uptimeMillis() - inferenceTime
        answererListener?.onResults(answers, inferenceTime)
    }

    interface AnswererListener {
        fun onError(error: String)
        fun onResults(
            results: List<QaAnswer>?,
            inferenceTime: Long
        )
    }

    companion object {
        private const val BERT_QA_MODEL = "mobilebert.tflite"
        private const val TAG = "BertQaHelper"
        const val DELEGATE_CPU = 0
        const val DELEGATE_GPU = 1
        const val DELEGATE_NNAPI = 2
    }
}
