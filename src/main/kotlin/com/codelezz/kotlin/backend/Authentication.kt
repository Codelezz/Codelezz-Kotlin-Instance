package com.codelezz.kotlin.backend

import com.codelezz.kotlin.CodelezzBase.baseFolder
import com.codelezz.kotlin.CodelezzBase.firebaseApiKey
import com.codelezz.kotlin.CodelezzBase.gson
import com.codelezz.kotlin.utils.*
import kotlin.reflect.KProperty

val authToken: String by AuthTokenProvider()

private class AuthTokenProvider {

	val file by lazy { baseFolder["auth.json"] }

	var token: AuthToken? = null
		set(value) {
			value?.let {
				if (!file.parentFile.exists()) file.parentFile.mkdirs()
				file.writeText(gson.toJson(it))
			}
			field = value
		}

	fun loadTokenFromFile(): String {
		if (!file.exists()) return requestNewToken()
		val authToken = gson.fromJson<AuthToken>(file.bufferedReader(), genericTypeInline<AuthToken>())
		if (authToken.expireTime < System.currentTimeMillis()) return requestRefreshToken(authToken.refreshToken)
		token = authToken
		return authToken.token
	}

	fun requestRefreshToken(refreshToken: String): String {
		val data = post {
			url = "https://securetoken.googleapis.com/v1/token?key=$firebaseApiKey"
			json {
				+("grant_type" to "refresh_token")
				+("refresh_token" to refreshToken)
			}
		}.json()


		token = AuthToken(
			token = data["id_token"] as String? ?: throw Exception(),
			refreshToken = data["refresh_token"] as String? ?: throw Exception(),
			expireTime = ((data["expires_in"] as String?)?.toIntOrNull() ?: 3600) * 1000 + System.currentTimeMillis(),
		)

		return token!!.token
	}

	fun requestNewToken(): String {
		val instanceResult = post {
			url = "https://accounts.codelezz.com/create-instance"
			json {}
		}.json()

		val token = instanceResult["token"]
				?: throw Exception()

		val data = post {
			url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithCustomToken?key=$firebaseApiKey"
			json {
				+("token" to token)
				+("returnSecureToken" to true)
			}
		}.json()

		this.token = AuthToken(
			token = data["idToken"] as String? ?: throw Exception(),
			refreshToken = data["refreshToken"] as String? ?: throw Exception(),
			expireTime = ((data["expiresIn"] as String?)?.toIntOrNull() ?: 3600) * 1000 + System.currentTimeMillis(),
		)

		return this.token!!.token
	}

	operator fun getValue(thisRef: Any?, property: KProperty<*>): String {
		token?.apply {
			if (expireTime < System.currentTimeMillis()) {
				return requestRefreshToken(refreshToken)
			}
		}
		return token?.token ?: loadTokenFromFile()
	}
}

private data class AuthToken(
	val token: String,
	val expireTime: Long,
	val refreshToken: String,
)