package com.etrak.scaleusb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.etrak.scaleusb.dao.CustomerDao
import com.etrak.scaleusb.data.Customer

@Database(
    entities = [
        Customer::class
    ],
    version = 1
)
abstract class Db : RoomDatabase() {

    abstract val customerDao: CustomerDao

}