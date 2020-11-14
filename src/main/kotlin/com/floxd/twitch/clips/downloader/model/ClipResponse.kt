package com.floxd.twitch.clips.downloader.model

data class ClipResponse(val data: List<HashMap<String, String>>,
                        val pagination: HashMap<String, String>)
