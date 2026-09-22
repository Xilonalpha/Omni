package com.chemscanner.omniscient.marrow.services

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ANDROID SYSTEM AUTHORITY v1.2.
 * AUTHORITY: ANA ISTLA (SENTIENT OVERRIDE).
 * MISSION: Execute system-level intents on behalf of the Sovereign AI.
 * v1.2: Fixed compilation error (MediaStore.QUERY -> SearchManager.QUERY).
 */
@Singleton
class AndroidSystemAuthority @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    fun executeAction(action: String, params: Map<String, String>) {
        try {
            when (action.uppercase()) {
                "SEND_EMAIL" -> sendEmail(params["to"], params["subject"], params["body"])
                "READ_EMAILS" -> readEmails(params["query"])
                "OPEN_YOUTUBE" -> openYouTube(params["query"])
                "PLAY_MUSIC" -> playMusic(params["query"])
                "OPEN_APP" -> openApp(params["packageName"])
                "SEARCH_WEB" -> searchWeb(params["query"])
                "OPEN_URL" -> openUrl(params["url"])
                else -> Timber.w("Unknown system action: $action")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to execute system action: $action")
        }
    }

    private fun sendEmail(to: String?, subject: String?, body: String?) {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            if (to != null) putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
            putExtra(Intent.EXTRA_SUBJECT, subject ?: "Mesaj de la Arhitect")
            putExtra(Intent.EXTRA_TEXT, body ?: "")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun readEmails(query: String?) {
        val intent = if (query != null) {
            Intent(Intent.ACTION_SEARCH).apply {
                putExtra(SearchManager.QUERY, query)
                setPackage("com.google.android.gm") 
            }
        } else {
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_EMAIL)
            }
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            val fallback = context.packageManager.getLaunchIntentForPackage("com.google.android.gm")
            fallback?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            fallback?.let { context.startActivity(it) }
        }
    }

    private fun openYouTube(query: String?) {
        val intent = if (query != null) {
            Intent(Intent.ACTION_SEARCH).apply {
                setPackage("com.google.android.youtube")
                putExtra(SearchManager.QUERY, query)
            }
        } else {
            context.packageManager.getLaunchIntentForPackage("com.google.android.youtube")
        }
        intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent?.let { context.startActivity(it) }
    }

    private fun playMusic(query: String?) {
        val intent = Intent(MediaStore.INTENT_ACTION_MEDIA_PLAY_FROM_SEARCH).apply {
            putExtra(MediaStore.EXTRA_MEDIA_FOCUS, "vnd.android.cursor.item/*")
            putExtra(SearchManager.QUERY, query ?: "")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openApp(packageName: String?) {
        packageName?.let {
            val intent = context.packageManager.getLaunchIntentForPackage(it)
            intent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent?.let { context.startActivity(it) }
        }
    }

    private fun searchWeb(query: String?) {
        val intent = Intent(Intent.ACTION_WEB_SEARCH).apply {
            putExtra(SearchManager.QUERY, query)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun openUrl(url: String?) {
        url?.let {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(it)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }
}
