package com.floxd.twitch.clips.downloader.model

data class AuthResponse(val access_token: String?,
                        val refresh_token: String?,
                        val expires_in: Long?,
                        val scope: List<String>?,
                        val token_type: String?)