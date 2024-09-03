package com.example.ratensaveandroidapp.utils

import android.content.Context
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import java.io.File

object CacheManager {

    private const val CACHE_SIZE = 100 * 1024 * 1024L // 100 MB
    private var simpleCache: SimpleCache? = null
    private val lock = Any()

    fun getSimpleCache(context: Context): SimpleCache {
        if (simpleCache == null) {
            synchronized(lock) {
                if (simpleCache == null) {
                    val cacheDirectory = File(context.cacheDir, "video-cache")
                    val lruCacheEvictor = LeastRecentlyUsedCacheEvictor(CACHE_SIZE)
                    simpleCache = SimpleCache(cacheDirectory, lruCacheEvictor)
                }
            }
        }
        return simpleCache!!
    }

    fun releaseCache() {
        synchronized(lock) {
            simpleCache?.release()
            simpleCache = null
        }
    }
}
