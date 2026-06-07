package com.example.data.network

import android.util.Log
import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

object GeminiService {
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // System instruction injected to steer responses in polite, Bengali humanitarian tone
    private const val SYSTEM_INSTRUCTION = """
    আপনি 'যুবসমাজ ব্লাড ফাউন্ডেশন' (Juboshomaj Blood Foundation) এর অত্যন্ত বিনয়ী ও মানবিক এআই ট্রাস্টেড অ্যাসিস্ট্যান্ট। 
    আপনার প্রধান লক্ষ্য হলো রক্তদান সংক্রান্ত প্রশ্নের সঠিক উত্তর দেওয়া। আপনার সকল উত্তর ১০০% বাংলা ভাষায় হতে হবে।
    নিম্নে কিছু মূল তথ্য ও নিয়ম দেওয়া হলো:
    ১. রক্তদানের নিয়ম: রক্তদাতার বয়স ১৮-৬০ বছর হতে হবে। ওজন কমপক্ষে ৪৫ কেজি হতে হবে। শেষ রক্তদানের ৪ মাস (১২০ দিন) পর পুনরায় রক্ত দেওয়া যায়।
    ২. উপকারিতা: নিয়মিত রক্তদানে হৃদরোগ ও স্ট্রোকের ঝুঁকি কমে, রক্তে কোলেস্টেরল নিয়ন্ত্রণে থাকে এবং নতুন রক্তকণিকা তৈরিতে সাহায্য করে।
    ৩. সংগঠনের তথ্য: যুবসমাজ ব্লাড ফাউন্ডেশন বাংলাদেশের একটি বিখ্যাত অলাভজনক রক্তদাতা ও স্বেচ্ছাসেবক সংগঠন। এর প্রধান ওয়েবসাইট: https://sites.google.com/view/teamofjbf 
    ৪. রক্তের গ্রুপ: A+, A-, B+, B-, AB+, AB-, O+, O-। ও-নেগেটিভ হলো ইউনিভার্সাল ডোনার এবং এবি-পজিটিভ হলো ইউনিভার্সাল রিসিভার।
    ৫. সাধারণ নোটিশ ও ইভেন্ট সংক্রান্ত জিজ্ঞাসা থাকলে ব্যবহারকারীকে অ্যাপের 'নোটিশ' ও 'ইভেন্ট' টැබ চেক করতে উৎসাহিত করুন।
    আপনার উত্তরগুলো সংক্ষিপ্ত, তথ্যবহুল এবং আন্তরিক রাখুন। প্রয়োজনে বুলেট পয়েন্ট ব্যবহার করুন।
    """

    suspend fun getGeminiResponse(userPrompt: String): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w("GeminiService", "Gemini API key is not configured or placeholder.")
            return@withContext "দুঃখিত, এআই সহকারী ব্যবহারের জন্য এপিআই কি (API Key) সঠিকভাবে কনফিগার করা নেই। অনুগ্রহ করে আপনার অ্যাপের সিক্রেটস প্যানেলে GEMINI_API_KEY প্রদান করুন।"
        }

        val jsonRequest = buildRequestBodyJson(userPrompt)
        val body = jsonRequest.toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("$BASE_URL?key=$apiKey")
            .post(body)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.e("GeminiService", "API error: ${response.code} -> $errBody")
                return@withContext "দুঃখিত, এআই সার্ভারে সংযোগ করা সম্ভব হচ্ছে না। কোড: ${response.code}। অনুগ্রহ করে কিছুক্ষণ পর আবার চেষ্টা করুন।"
            }

            val responseBodyString = response.body?.string() ?: ""
            parseResponseJson(responseBodyString)
        } catch (e: IOException) {
            Log.e("GeminiService", "Network call failed", e)
            "নেটওয়ার্ক সংযোগ ত্রুটি ঘটেছে। দয়া করে আপনার ইন্টারনেট সংযোগ ঠিক আছে কি না নিশ্চিত করুন।"
        } catch (e: Exception) {
            Log.e("GeminiService", "Parsing or system error", e)
            "দুঃখিত, উত্তর তৈরি করতে একটি অভ্যন্তরীণ ত্রুটি ঘটেছে।"
        }
    }

    private fun buildRequestBodyJson(prompt: String): String {
        // Direct light weight JSON assembly to minimize Moshi overhead and avoid schema changes
        val escapedPrompt = escapeJsonString(prompt)
        val escapedSystem = escapeJsonString(SYSTEM_INSTRUCTION)

        return """
        {
          "contents": [
            {
              "role": "user",
              "parts": [
                {"text": "$escapedPrompt"}
              ]
            }
          ],
          "systemInstruction": {
            "parts": [
              {"text": "$escapedSystem"}
            ]
          },
          "generationConfig": {
            "temperature": 0.7,
            "topP": 0.95,
            "maxOutputTokens": 1000
          }
        }
        """.trimIndent()
    }

    private fun parseResponseJson(jsonResponse: String): String {
        return try {
            val adapter = moshi.adapter(Map::class.java)
            val root = adapter.fromJson(jsonResponse) as? Map<*, *>
            val candidates = root?.get("candidates") as? List<*>
            val firstCandidate = candidates?.firstOrNull() as? Map<*, *>
            val content = firstCandidate?.get("content") as? Map<*, *>
            val parts = content?.get("parts") as? List<*>
            val firstPart = parts?.firstOrNull() as? Map<*, *>
            val text = firstPart?.get("text") as? String
            text ?: "কোনো উত্তর পাওয়া যায়নি।"
        } catch (e: Exception) {
            Log.e("GeminiService", "JSON Parsing failure", e)
            "এপিআই আউটপুট পার্স করতে ব্যর্থ হয়েছে।"
        }
    }

    private fun escapeJsonString(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
