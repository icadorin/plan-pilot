package com.israel.planpilot

import android.content.Context
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat

class IconRepository(private val context: Context) {
    private val iconCache = HashMap<Int, Drawable>()

    suspend fun getIcon(iconResource: Int): Drawable? {
        if (iconCache.containsKey(iconResource)) {
            return iconCache[iconResource]
        }

        val iconDrawable = ContextCompat.getDrawable(context, iconResource)

        if (iconDrawable != null) {
            iconCache[iconResource] = iconDrawable
            return iconDrawable
        }

        return null
    }
}
