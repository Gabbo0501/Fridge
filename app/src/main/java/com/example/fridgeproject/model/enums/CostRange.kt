package com.example.fridgeproject.model.enums

enum class CostRange(val description: String){
    //name = upper limit of the range
    FIVE("0-5 $"), //0-5
    TEN("5-10 $"), //5-10
    TWENTY("10-20 $"), //...
    OVER_TWENTY("20+ $")
}