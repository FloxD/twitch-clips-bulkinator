package com.floxd.twitch.clips.downloader.model

data class GQLClipResponse(val data: Data?, val extensions: Any?)

data class Data(val user: User?)

data class User(val id: String?, val clips: Clips?, val __typename: Any?)

data class Clips(val pageInfo: Any?, val edges: List<Edge?>?)

data class Edge(val cursor: Any?, val node: Node?, val __typename: Any?)

data class Node(val id: String?,
                val slug: String?,
                val url: String?,
                val embedURL: Any?,
                val title: String?,
                val viewCount: Any?,
                val language: Any?,
                val curator: Any?,
                val game: Any?,
                val broadcaster: Any?,
                val thumbnailURL: String?,
                val createdAt: Any?,
                val duration: Any?,
                val champBadge: Any?,
                val __typename: Any?)
