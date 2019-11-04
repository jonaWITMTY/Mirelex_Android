package com.example.jonathangalvan.mirelex.Interfaces

class StatesInterface (
    var data: ArrayList<StateInterface>?
){}

class StateInterface(
    var catalogId: String?,
    var name: String,
    var url: String?,
    var value1: String?,
    var value2: String?
){
    override fun toString(): String {
        return name
    }
}