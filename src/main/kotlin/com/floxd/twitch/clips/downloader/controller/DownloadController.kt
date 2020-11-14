package com.floxd.twitch.clips.downloader.controller

import com.floxd.twitch.clips.downloader.service.DownloadService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import javax.servlet.http.HttpServletResponse

@Controller
@RequestMapping("/download")
class DownloadController(private val downloadService: DownloadService) {

    @GetMapping
    fun get(@RequestParam("access_token") accessToken: String, response: HttpServletResponse): String {
        downloadService.download(accessToken)

        // doing a scuffed redirect here to avoid leaking
        // the access token into the browser address bar
        response.sendRedirect("/download/finished")

        return ""
    }

    @GetMapping("/finished")
    fun get(): String {
        return "finished.html"
    }
}