package com.example.jonathangalvan.mirelex.Interfaces

class NeighborhoodInterface(
   var neighborhoodId: String,
   var name: String
) {
    override fun toString(): String {
        return name
    }
}

class NeighborhoodArrayInterface(
    var data: ArrayList<NeighborhoodInterface>
) {}

