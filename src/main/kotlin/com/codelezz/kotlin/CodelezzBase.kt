package com.codelezz.kotlin

import com.codelezz.kotlin.backend.Backend
import com.codelezz.kotlin.utils.Exclude
import com.codelezz.kotlin.utils.getExclusionStrategy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.io.File

object CodelezzBase {

	/// Firebase api keys can be publicly available.
	const val firebaseApiKey: String = "AIzaSyAxuGMDUrIqBb-Xnn4AzyuKeMqWj8xQtd0"
	lateinit var gson: Gson
	lateinit var baseFolder: File

	fun initialize(baseFolder: File) {
		this.baseFolder = baseFolder
		gson = GsonBuilder()
			.enableComplexMapKeySerialization()
			.addSerializationExclusionStrategy(getExclusionStrategy(Exclude.During.SERIALIZATION))
			.addDeserializationExclusionStrategy(getExclusionStrategy(Exclude.During.DESERIALIZATION))
			.setExclusionStrategies(getExclusionStrategy())
			.serializeNulls()
			.create()

		Backend.init()
	}
	fun reInitialize() {
		Backend.init()
	}
}