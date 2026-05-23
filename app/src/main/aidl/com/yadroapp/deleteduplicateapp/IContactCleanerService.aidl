package com.yadroapp.deleteduplicateapp;

import com.yadroapp.deleteduplicateapp.IContactCleanerCallback;


interface IContactCleanerService {

    void removeDuplicates(IContactCleanerCallback callback);
}