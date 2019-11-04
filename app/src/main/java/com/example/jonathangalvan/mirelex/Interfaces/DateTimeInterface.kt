package com.example.jonathangalvan.mirelex.Interfaces

class DateTimeInterface(
   var startDate: String = "",
   var endDate: String = "",
   var startHour: String = "",
   var endHour: String = ""
) {}

class DateTimeArrInterface(
   var data: ArrayList<DateTimeInterface>
){}