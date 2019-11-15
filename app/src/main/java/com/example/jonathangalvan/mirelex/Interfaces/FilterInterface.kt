package com.example.jonathangalvan.mirelex.Interfaces

class FilterInterface(
    var id: String? = null,
    var name: String
) {
    override fun toString(): String {
        return name
    }
}