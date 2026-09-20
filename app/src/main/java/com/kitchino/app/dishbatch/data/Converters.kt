package com.kitchino.app.dishbatch.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun statusToString(status: Status) : String {
        return status.name
    }

    @TypeConverter
    fun stringToStatus(string: String) : Status {
        return Status.valueOf(string)
    }

    @TypeConverter
    fun decisionToString(decisionType: DecisionType) : String {
        return decisionType.name
    }

    @TypeConverter
    fun stringToDecision(string: String) : DecisionType {
        return DecisionType.valueOf(string)
    }
}

