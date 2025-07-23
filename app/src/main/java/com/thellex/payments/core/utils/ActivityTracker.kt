package com.thellex.payments.core.utils

import android.app.Activity

object ActivityTracker {
    private val activities = mutableListOf<Activity>()

    @Synchronized
    fun add(activity: Activity) {
        activities.add(activity)
    }

    @Synchronized
    fun remove(activity: Activity) {
        activities.remove(activity)
    }

    @Synchronized
    fun finishActivity(clazz: Class<out Activity>) {
        activities.filter { it.javaClass == clazz }
            .forEach { it.finish() }
    }

    @Synchronized
    fun finishAll() {
        activities.forEach { it.finish() }
    }

    @Synchronized
    fun getActivities(): List<Activity> {
        return activities.toList() // Return a copy to prevent external modification
    }
}
