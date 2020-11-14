package com.floxd.twitch.clips.downloader.controller

import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
class CustomErrorController : ErrorController {

    @GetMapping("/error")
    fun get(): String {
        return "error.html"
    }

    override fun getErrorPath(): String {
        return "/error"
    }
}