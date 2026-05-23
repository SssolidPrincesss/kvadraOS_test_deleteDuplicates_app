package com.yadroapp.deleteduplicateapp

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private var service: IContactCleanerService? = null
    private var bound = false
    private lateinit var statusText: TextView
    private lateinit var removeButton: Button



    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            service = IContactCleanerService.Stub.asInterface(binder)
            bound = true
            removeButton.isEnabled = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            service = null
            bound = false
        }
    }

    private val callback = object : IContactCleanerCallback.Stub() {
        override fun onSuccess() {
            runOnUiThread {
                statusText.text = getString(R.string.Contacts_deleted)
                removeButton.isEnabled = true
            }
        }

        override fun onError(message: String?) {
            runOnUiThread {
                statusText.text = getString(R.string.OnErrorMistake, message)
                removeButton.isEnabled = true
            }
        }

        override fun onNoDuplicatesFound() {
            runOnUiThread {
                statusText.text = getString(R.string.Cant_found_contact)
                removeButton.isEnabled = true
            }
        }
    }





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.status_text)
        removeButton = findViewById(R.id.remove_button)
        removeButton.isEnabled = false
        removeButton.setOnClickListener { startCleanUp() }

        bindService(
            Intent(this, ContactCleanerService::class.java),
            connection,
            BIND_AUTO_CREATE
        )
        checkAndRequestPermissions()
    }




    private fun startCleanUp() {
        val svc = service
        if (svc == null) {
            Toast.makeText(this, getString(R.string.servis_isnot_done), Toast.LENGTH_SHORT).show()
            return
        }
        if (!hasPermissions()) {
            checkAndRequestPermissions()
            return
        }

        removeButton.isEnabled = false
        statusText.text = getString(R.string.duplicate_deleting)

        try {
            svc.removeDuplicates(callback)
        } catch (e: Exception) {
            statusText.text = getString(R.string.server_connection_error, e.message)
            removeButton.isEnabled = true
        }
    }


    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
    }

    private fun checkAndRequestPermissions() {
        if (!hasPermissions()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS),
                PERMISSIONS_REQUEST
            )
        }
    }




    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST) {
            if (hasPermissions()) {
                Toast.makeText(this, getString(R.string.permissions_received), Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, getString(R.string.noworking_without_perm), Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        private const val PERMISSIONS_REQUEST = 101
    }



}

