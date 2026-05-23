package com.yadroapp.deleteduplicateapp;

oneway interface IContactCleanerCallback {
    void onSuccess();
    void onError(String message);
    void onNoDuplicatesFound();
}