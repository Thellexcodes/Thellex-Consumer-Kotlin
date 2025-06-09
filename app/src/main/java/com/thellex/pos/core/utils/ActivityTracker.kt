package com.thellex.pos.core.utils

import android.app.Activity


object ActivityTracker {
    private val activities = mutableListOf<Activity>()

    fun add(activity: Activity) {
        activities.add(activity)
    }

    fun remove(activity: Activity) {
        activities.remove(activity)
    }

    fun finishActivity(clazz: Class<out Activity>) {
        activities.filter { it.javaClass == clazz }
            .forEach { it.finish() }
    }

    fun finishAll() {
        activities.forEach { it.finish() }
    }
}
