package com.floxd.twitch.clips.downloader.controller

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Controller
@RequestMapping("/auth")
class AuthController {

    @GetMapping
    fun get(): String {
        return "download.html"
    }
}