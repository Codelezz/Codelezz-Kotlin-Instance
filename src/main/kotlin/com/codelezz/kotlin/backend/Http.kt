package com.codelezz.kotlin.backend

import com.codelezz.kotlin.CodelezzBase.gson
import java.io.*
import java.net.HttpURLConnection
import java.net.URL

fun request(f: HttpRequest.() -> Unit): String = HttpRequest().apply(f).run()
fun get(f: HttpRequest.() -> Unit): String = HttpRequest().apply(f).apply { requestMethod = "GET" }.run()
fun post(f: HttpRequest.() -> Unit): String = HttpRequest().apply(f).apply { requestMethod = "POST" }.run()
fun postAuthorized(f: HttpRequest.() -> Unit): String =
	HttpRequest().apply(f).apply { requestMethod = "POST"; auth() }.run()

class HttpRequest {
	var url: String? = null
	var requestMethod: String? = null
	var contentType: String = "application/json"

	private var body: String? = null
	private var auth: String? = null

	fun auth() {
		auth = "Bearer $authToken"
	}

	fun json(f: JsonBody.() -> Unit) {
		body = JsonBody().apply(f).build()
		contentType = "application/json"
	}

	fun run(): String {
		if (url.isNullOrBlank()) throw IllegalStateException("Url can not be null or empty")
		val u = URL(url)

		var response = ""

		with(u.openConnection() as HttpURLConnection) {
			if (auth != null) setRequestProperty("Authorization", auth)
			setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 5.1) AppleWebKit/535.1 (KHTML, like Gecko) Chrome/13.0.782.112 Safari/535.1"
			)
			setRequestProperty("Content-Type", this@HttpRequest.contentType)

			doInput = true
			doOutput = true
			requestMethod = this@HttpRequest.requestMethod

			if (body != null || requestMethod == "POST") {
				val wr = OutputStreamWriter(outputStream)
				wr.write(body ?: "")
				wr.flush()
			}

			if (responseCode != 200) {
				throw Exception(
					"Could not load: $requestMethod $u ($responseCode) - ${
						errorStream.reader().readLines().joinToString(separator = System.lineSeparator())
					}"
				)
			}


			BufferedReader(InputStreamReader(inputStream)).use {
				var inputLine = it.readLine()
				while (inputLine != null) {
					response += inputLine
					inputLine = it.readLine()
				}
			}
		}

		return response
	}
}

interface HttpBody {
	fun build(): String
}

class JsonBody : HttpBody {

	private val map = mutableMapOf<String, Any>()

	operator fun Pair<String, Any>.unaryPlus() {
		map[first] = second
	}

	override fun build(): String = gson.toJson(map)
}