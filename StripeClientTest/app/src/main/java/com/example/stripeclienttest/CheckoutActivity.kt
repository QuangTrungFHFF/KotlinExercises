package com.example.stripeclienttest

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.StripeIntent
import com.stripe.android.view.CardInputWidget
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit

class CheckoutActivity : AppCompatActivity() {
    // 10.0.2.2 is the Android emulator's alias to localhost
    private val backendUrl = "https://nameless-fortress-13129.herokuapp.com/"
    private var httpClient = OkHttpClient()
    private lateinit var paymentIntentClientSecret: String
    private lateinit var stripe: Stripe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        // Configure the SDK with your Stripe publishable key so it can make requests to Stripe
        stripe = Stripe(applicationContext, "pk_test_51IhAu2EYP2XsA89m5vqvoeyFVniVcI881wi53n8QdChng5nBUd9tDstbVE0GV103PZs2pRU7PKYpQ3YAWzxcnVy300OG92K2eN")
        httpClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()
        startCheckout()
    }

    private fun displayAlert(
        activity: Activity,
        title: String,
        message: String,
        restartDemo: Boolean = false
    ) {
        runOnUiThread {
            val builder = AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)

            builder.setPositiveButton("Ok", null)
            builder.create().show()
        }
    }

    private fun startCheckout() {
        val weakActivity = WeakReference<Activity>(this)
        // Create a PaymentIntent by calling your server's endpoint.
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val jsonString : String = getJsonString()
        
        val body = jsonString.toRequestBody(mediaType)
        val request = Request.Builder()
            .url(backendUrl + "create-payment-intent")
            .post(body)
            .build()
        httpClient.newCall(request)
            .enqueue(object: Callback {
                override fun onFailure(call: Call, e: IOException) {
                    weakActivity.get()?.let { activity ->
                        displayAlert(activity, "Failed to load page 1", "Error: $e")
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        weakActivity.get()?.let { activity ->
                            displayAlert(
                                activity,
                                "Failed to load page 2",
                                "Error: $response"
                            )
                        }
                    } else {
                        val responseData = response.body?.string()
                        val responseJson =
                            responseData?.let { JSONObject(it) } ?: JSONObject()

                        // For added security, our sample app gets the publishable key
                        // from the server.
                        paymentIntentClientSecret = responseJson.getString("clientSecret")
                    }
                }
            })

        // Hook up the pay button to the card widget and stripe instance
        val payButton: Button = findViewById(R.id.payButton)
        payButton.setOnClickListener {
            val cardInputWidget =
                findViewById<CardInputWidget>(R.id.cardInputWidget)
            cardInputWidget.paymentMethodCreateParams?.let { params ->
                val confirmParams = ConfirmPaymentIntentParams
                    .createWithPaymentMethodCreateParams(params, paymentIntentClientSecret)
                stripe.confirmPayment(this, confirmParams)
            }
        }
    }

    private fun getJsonString(): String {
        val payMap = mutableMapOf<String,Any>()
        val itemMap = mutableMapOf<String,Any>()
        val itemList = mutableListOf<MutableMap<String,Any>>()
        payMap["currency"] = "usd"
        itemMap["id"] = "M0001"
        itemMap["price"] = 2000
        itemList.add(itemMap)
        payMap["items"] = itemList
        val gson = Gson()
        return gson.toJson(payMap)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val weakActivity = WeakReference<Activity>(this)

        // Handle the result of stripe.confirmPayment
        stripe.onPaymentResult(requestCode, data, object : ApiResultCallback<PaymentIntentResult> {
            override fun onSuccess(result: PaymentIntentResult) {
                val paymentIntent = result.intent
                val status = paymentIntent.status
                if (status == StripeIntent.Status.Succeeded) {
                    val gson = GsonBuilder().setPrettyPrinting().create()
                    weakActivity.get()?.let { activity ->
                        displayAlert(
                            activity,
                            "Payment succeeded",
                            gson.toJson(paymentIntent)
                        )
                    }
                } else if (status == StripeIntent.Status.RequiresPaymentMethod) {
                    weakActivity.get()?.let { activity ->
                        displayAlert(
                            activity,
                            "Payment failed",
                            paymentIntent.lastPaymentError?.message.orEmpty()
                        )
                    }
                }
            }
            override fun onError(e: Exception) {
                weakActivity.get()?.let { activity ->
                    displayAlert(
                        activity,
                        "Payment failed",
                        e.toString()
                    )
                }
            }
        })
    }
}
