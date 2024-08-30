package com.example.ordinarybeauty.utils

import android.location.Location
import com.example.ordinarybeauty.data.PhotoSpot
import com.example.ordinarybeauty.data.Type
import com.example.ordinarybeauty.viewmodels.FilterViewModel

fun checkFilter(filterVM: FilterViewModel, ps: PhotoSpot, curLoc: Location): Boolean {
    var pass = true

    if (filterVM.author.isNotEmpty() && !ps.authorName.contains(filterVM.author, true)){
        pass = false
    } else if (filterVM.type != Type.Unspecified && filterVM.type != ps.type){
        pass = false
    } else if (filterVM.keyword.isNotEmpty() && !(ps.title.contains(filterVM.keyword, true) || ps.description.contains(filterVM.keyword, true))){
        pass = false
    } else if (filterVM.rating > 0 && filterVM.rating > ps.rating){
        pass = false
    } else if (filterVM.numRates > 0 && filterVM.numRates > ps.rates.size){
        pass = false
    } else if (filterVM.startCreated != null && !(ps.datePosted.after(filterVM.startCreated) && ps.datePosted.before(filterVM.endCreated))){
        pass = false
    } else if (filterVM.startLast != null && !(ps.dateLastInteraction.after(filterVM.startLast) && ps.dateLastInteraction.before(filterVM.endLast))){
        pass = false
    } else if (filterVM.dist > 0){
        val finalDist = FloatArray(1)
        Location.distanceBetween(ps.lat, ps.lng, curLoc.latitude, curLoc.longitude, finalDist)
        if (finalDist.first() > filterVM.dist){
            pass = false
        }
    }

    return pass
}