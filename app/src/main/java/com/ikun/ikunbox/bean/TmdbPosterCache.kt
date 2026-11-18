package com.ikun.ikunbox.bean

interface TmdbPosterCache {

    fun tmdbCachedPoster(name: String?, year: Int, altName: String?): String?
}
