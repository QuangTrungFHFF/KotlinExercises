package com.example.stripesecondtestproject

import java.io.IOException
import java.lang.ref.WeakReference

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson

import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

import com.google.gson.GsonBuilder
import org.json.JSONObject

import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.StripeIntent
import com.stripe.android.view.CardInputWidget
import java.util.concurrent.TimeUnit


class CheckoutActivity : AppCompatActivity() {

    /**
     * This example collects card payments, implementing the guide here: https://stripe.com/docs/payments/accept-a-payment#android
     *
     * To run this app, follow the steps here: https://github.com/stripe-samples/accept-a-card-payment#how-to-run-locally
     */
    // 10.0.2.2 is the Android emulator's alias to localhost
    private val backendUrl = "https://nameless-fortress-13129.herokuapp.com/"
    private var httpClient = OkHttpClient()
    private lateinit var publishableKey: String
    private lateinit var paymentIntentClientSecret: String
    private lateinit var stripe: Stripe

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)
        stripe = Stripe(applicationContext, "pk_test_51IhAu2EYP2XsA89m5vqvoeyFVniVcI881wi53n8QdChng5nBUd9tDstbVE0GV103PZs2pRU7PKYpQ3YAWzxcnVy300OG92K2eN")

        startCheckout()
    }

    private fun displayAlert(activity: Activity?, title: String, message: String, restartDemo: Boolean = false) {
        if (activity == null) {
            return
        }
        runOnUiThread {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(title)
            builder.setMessage(message)
            if (restartDemo) {
                builder.setPositiveButton("Restart demo") { _, _ ->
                    val cardInputWidget =
                        findViewById<CardInputWidget>(R.id.cardInputWidget)
                    cardInputWidget.clear()
                    startCheckout()
                }
            }
            else {
                builder.setPositiveButton("Ok", null)
            }
            val dialog = builder.create()
            dialog.show()
        }
    }

    private fun startCheckout() {
        val weakActivity = WeakReference<Activity>(this)
        // Create a PaymentIntent by calling the sample server's /create-payment-intent endpoint.
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
                    displayAlert(weakActivity.get(), "Failed to load page", "Error: $e")
                }

                override fun onResponse(call: Call, response: Response) {
                    if (!response.isSuccessful) {
                        displayAlert(weakActivity.get(), "Failed to load page", "Error: $response")
                    } else {
                        val responseData = response.body?.string()
                        var json = JSONObject(responseData)

                        // The response from the server includes the Stripe publishable key and
                        // PaymentIntent details.
                        // For added security, our sample app gets the publishable key from the server
                        //publishableKey = json.getString("publishableKey")
                        paymentIntentClientSecret = json.getString("clientSecret")

                        // Configure the SDK with your Stripe publishable key so that it can make requests to the Stripe API
                        //stripe = Stripe(applicationContext, publishableKey)
                    }
                }
            })

        // Hook up the pay button to the card widget and stripe instance
        val payButton: Button = findViewById(R.id.payButton)
        payButton.setOnClickListener {
            val cardInputWidget =
                findViewById<CardInputWidget>(R.id.cardInputWidget)
            val params = cardInputWidget.paymentMethodCreateParams
            if (params != null) {
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
                    displayAlert(weakActivity.get(), "Payment succeeded", gson.toJson(paymentIntent), restartDemo = true)
                } else if (status == StripeIntent.Status.RequiresPaymentMethod) {
                    displayAlert(weakActivity.get(), "Payment failed", paymentIntent.lastPaymentError?.message ?: "")
                }
            }

            override fun onError(e: Exception) {
                displayAlert(weakActivity.get(), "Payment failed", e.toString())
            }
        })
    }

}