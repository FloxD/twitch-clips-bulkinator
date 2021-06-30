package com.floxd.twitch.clips.downloader.service

import com.floxd.twitch.clips.downloader.model.GQLClipResponse
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import java.io.File
import java.io.InputStream
import java.net.URL
import java.nio.file.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@Service
class DownloadService : CommandLineRunner {

    val logger: Logger = LoggerFactory.getLogger(DownloadService::class.java)
    val restTemplate: RestTemplate = RestTemplate()

    override fun run(vararg args: String?) {
        download()
    }

    fun download() {

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss")
        val folderName = LocalDateTime.now().format(formatter)
        Files.createDirectories(Paths.get(folderName))

        downloadSingle(folderName)

        logger.info("finished")
    }

    fun downloadSingle(folderName: String) {
        val url = "https://gql.twitch.tv/gql"
        logger.info("fetch clips from api. $url")

        val httpHeaders = HttpHeaders()
        httpHeaders.set("Client-Id", "kimne78kx3ncx6brgo4mv6wki5h1ko")

        val httpEntity = HttpEntity(
            """[{"operationName":"ClipsCards__User","variables":{"login":"elina","limit":60,"criteria":{"filter":"LAST_MONTH"}},"extensions":{"persistedQuery":{"version":1,"sha256Hash":"b73ad2bfaecfd30a9e6c28fada15bd97032c83ec77a0440766a56fe0bd632777"}}}]""",
            httpHeaders
        )

        val response = restTemplate.exchange(
            "https://gql.twitch.tv/gql",
            HttpMethod.POST,
            httpEntity,
            arrayOf<GQLClipResponse>()::class.java
        )

        logger.info("clips fetched api. found ${response.body?.get(0)?.data?.user?.clips?.edges?.size} clips")

        response.body?.get(0)?.data?.user?.clips?.edges?.forEach { clip ->

            val thumbnailUrl: String? = clip?.node?.thumbnailURL
            thumbnailUrl?.let {
                val videoUrl = getVideoUrl(thumbnailUrl)

                val inputStream: InputStream = URL(videoUrl).openStream()
                val title: String? = clip.node.title
                val path = getPath(folderName, title)
                logger.info("downloading clip with title: $title. $videoUrl")
                Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING)
            }
        }
    }

    private fun getPath(folderName: String, title: String?): Path {
        val sanitizedTitle = sanitizeTitle(title)
        val path = try {
            Paths.get("$folderName${File.separator}$sanitizedTitle.mp4")
        } catch (e: InvalidPathException) {
            Paths.get("$folderName${File.separator}${UUID.randomUUID()}.mp4")
        }

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

    private fun getVideoUrl(thumbnailUrl: String): String {
        return thumbnailUrl.replace("-preview-260x147.jpg", ".mp4")
    }
}