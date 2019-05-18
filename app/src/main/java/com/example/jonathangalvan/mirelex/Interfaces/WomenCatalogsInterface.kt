package com.example.jonathangalvan.mirelex.Interfaces

class WomenCatalogsInterface(
    var bodyTypes: ArrayList<WomenCatalogInterface>,
    var hairColors: ArrayList<WomenCatalogInterface>,
    var skinColors: ArrayList<WomenCatalogInterface>
){}

class WomenCatalogInterface(
    var catalogId: String?,
    var name: String,
    var url: String?
){
    override fun toString(): String {
        return name
    }
}