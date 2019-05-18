package com.example.jonathangalvan.mirelex.Interfaces

class CatalogInterface(
   var productCatalogId: Int?,
   var name: String
) {

   override fun toString(): String {
      return name
   }
}

class CatalogArrayInterface(
   var data: ArrayList<CatalogInterface>
) {}