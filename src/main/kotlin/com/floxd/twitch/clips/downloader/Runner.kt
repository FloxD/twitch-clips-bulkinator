package com.floxd.twitch.clips.downloader

import com.floxd.twitch.clips.downloader.model.AuthResponse
import com.floxd.twitch.clips.downloader.model.ClipResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Component
class Runner(@Value("\${twitch.auth.client-id}") val clientId: String?,
             @Value("\${twitch.auth.client-secret}") val clientSecret: String?,
             @Value("\${twitch.broadcaster-id}") val broadcasterId: Long?,
             @Value("\${twitch.batch-count:5}") val batchCount: Int) : CommandLineRunner {

    val logger: Logger = LoggerFactory.getLogger(Runner::class.java)
    val restTemplate: RestTemplate = RestTemplate()

    override fun run(vararg args: String?) {

        if (broadcasterId == null) {
            throw IllegalArgumentException("The broadcaster-id is not set! Make sure that you set it through the argument -Dtwitch.broadcaster-id=INSERT_BROADCASTER_ID_HERE")
        }

        val authResponse = restTemplate.postForObject(getTokenUrl(), null, AuthResponse::class.java)

        if (authResponse?.access_token == null) {
            throw RuntimeException("access token is null")
        }

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        val folderName = LocalDateTime.now().format(formatter)
        Files.createDirectories(Paths.get(folderName))

        var cursor: String? = null

        for (i in 0..batchCount) {
            cursor = downloadBatch(authResponse.access_token, cursor, folderName)
        }

        logger.info("finished")
    }

    private fun downloadBatch(accessToken: String, cursor: String?, folderName: String): String {
        logger.info("start batch")

        val url = getClipsApiUrl(cursor)
        logger.info("fetch clips from api. $url")
        val headers = HttpHeaders()
        headers.set("Authorization", "Bearer $accessToken")
        headers.set("Client-Id", clientId)
        val httpEntity = HttpEntity(null, headers)

        val clipResponse = restTemplate.exchange(
                url,
                HttpMethod.GET,
                httpEntity,
                ClipResponse::class.java)

        logger.info("clips fetched api. found ${clipResponse.body?.data?.size} clips")

        clipResponse.body?.data?.forEach { clipData ->
            val thumbnailUrl: String? = clipData.get("thumbnail_url")
            thumbnailUrl?.let {
                val videoUrl = getVideoUrl(thumbnailUrl)

                val inputStream: InputStream = URL(videoUrl).openStream()
                val title: String? = clipData.get("title")
                val path = getPath(folderName, title)
                logger.info("downloading clip with title: $title")
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING)
            }
        }

        logger.info("finished batch")

        return clipResponse.body?.pagination?.get("cursor") ?: throw RuntimeException("cursor couldn't be fetched")
    }

    private fun getPath(folderName: String, title: String?): Path {
        val sanitizedTitle = sanitizeTitle(title)
        val path = Paths.get("$folderName${File.separator}$sanitizedTitle.mp4")

        if (Files.exists(path)) {
            val randomFilename = Paths.get("$folderName${File.separator}$sanitizedTitle ${UUID.randomUUID()}.mp4")
            if (Files.exists(randomFilename)) {
                throw RuntimeException("file already exists even with random file name")
            }
            return randomFilename
        }

        return path
    }

    private fun sanitizeTitle(title: String?): String {
        return title?.replace("[^a-zA-Z0-9\\s]".toRegex(), "_") ?: UUID.randomUUID().toString()
    }

    private fun getTokenUrl(): String {
        return "https://id.twitch.tv/oauth2/token" +
                "?client_id=$clientId" +
                "&client_secret=$clientSecret" +
                "&grant_type=client_credentials" +
                "&scope="
    }

    private fun getClipsApiUrl(cursor: String?): String {
        val baseUrl = "https://api.twitch.tv/helix/clips?broadcaster_id=$broadcasterId&first=20"
        if (cursor == null) {
            return baseUrl
        } else {
            return "$baseUrl&after=$cursor"
        }
    }

    private fun getVideoUrl(thumbnailUrl: String): String {
        return thumbnailUrl.replace("-preview-480x272.jpg", ".mp4")
    }
}