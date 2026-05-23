package com.yadroapp.deleteduplicateapp

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import java.util.concurrent.Executors


class ContactCleanerService : Service(){
    private val executor = Executors.newSingleThreadExecutor()
    companion object {
        private const val TAG = "ContactCleaner"
    }

    override fun onBind(intent: Intent?): IBinder? = binder

    private val binder = object : IContactCleanerService.Stub() {
        override fun removeDuplicates(callback: IContactCleanerCallback?) {

            callback ?: return
            executor.execute { performCleanup(callback) }
        }
    }




    private fun performCleanup(callback: IContactCleanerCallback) {
        val cr = contentResolver
        val fingerprints = mutableMapOf<String, Long>()
        val duplicatesToDelete = mutableListOf<Long>()

        cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID),
            null, null, null
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndex(ContactsContract.Contacts._ID)
            while (cursor.moveToNext()) {
                val contactId = cursor.getLong(idIdx)
                val fingerprint = buildContactFingerprint(cr, contactId)
                if (fingerprint.isEmpty()) continue

                if (fingerprints.containsKey(fingerprint)) {
                    duplicatesToDelete.add(contactId)

                } else {
                    fingerprints[fingerprint] = contactId
                }
            }
        } ?: run {
            notifyError(callback, "Не удалось прочитать контакты")
            return
        }

        if (duplicatesToDelete.isEmpty()) {
            notifyNoDuplicates(callback)
            return
        }

        try {
            duplicatesToDelete.forEach { id ->
                cr.delete(
                    ContactsContract.RawContacts.CONTENT_URI,
                    "${ContactsContract.RawContacts.CONTACT_ID} = ?",
                    arrayOf(id.toString())
                )
            }
            notifySuccess(callback)
        } catch (e: SecurityException) {
            notifyError(callback, "Нет прав на изменение контактов: ${e.message}")
        }
    }




    private fun buildContactFingerprint(cr: ContentResolver, contactId: Long): String {
        val rows = mutableListOf<String>()
        cr.query(
            ContactsContract.Data.CONTENT_URI,
            null,
            "${ContactsContract.Data.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )?.use { cursor ->
            val mimeIdx = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE)
            val dataIndices = (1..15).map { i ->
                cursor.getColumnIndex("data $i")
            }

            while (cursor.moveToNext()) {
                val sb = StringBuilder(cursor.getString(mimeIdx))

                for (idx in dataIndices) {
                    if (idx >= 0) {
                        cursor.getString(idx)?.let { value ->
                            if (value.isNotEmpty()) {
                                sb.append("|").append(value)

                            }
                        }
                    }

                }
                rows.add(sb.toString())
            }
        }

        rows.sort()
        return rows.joinToString("\n")
    }




    private fun notifySuccess(callback: IContactCleanerCallback) {
        try {
            callback.onSuccess()
        } catch (e: Exception) {
            Log.e(TAG, "onSuccess error", e)
        } finally {
            stopSelf()
        }
    }

    private fun notifyError(callback: IContactCleanerCallback, msg: String) {
        try {
            callback.onError(msg)
        } catch (e: Exception) {
            Log.e(TAG, "onError error", e)
        } finally {
            stopSelf()
        }
    }

    private fun notifyNoDuplicates(callback: IContactCleanerCallback) {
        try {
            callback.onNoDuplicatesFound()
        } catch (e: Exception) {
            Log.e(TAG, "onNoDuplicatesFound error", e)
        } finally {
            stopSelf()
        }
    }




    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        executor.shutdownNow()
        super.onDestroy()
    }
}