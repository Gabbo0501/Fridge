package com.example.fridgeproject.model.enums

enum class NotificationType {
    LIKE,                   //like a una mia ricetta
    REMIX,                  //remix a una mia ricetta
    REVIEW,                 //recensione a una mia ricetta
    TIP,                    //tip a una mia ricetta
    NEW_FOLLOWER,           //nuovo follower
    NEW_RECIPE,             //ricetta pubblicata da un followed
    FAVORITE_UPDATED,       //aggiornamento di una ricetta che ho tra i preferiti
    FAVORITE_REMOVED,        //rimozione di una ricetta che ho tra i preferiti
    RECOMMENDED_RECIPE       //ricetta affine alle preferenze di dieta
}