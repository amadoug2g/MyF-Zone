package mfz.myfzone_sport.myf_zone.util.user

import mfz.myfzone_sport.myf_zone.util.user.UserAccount.currentUserDocRef

object UserAffiliation {

    fun userAffiliationStatus(myCallback: (Boolean) -> Unit) {
        val affiliationPath = currentUserDocRef.collection("ClubAffiliation")

        affiliationPath.get().addOnCompleteListener {
            if (it.isSuccessful) {
                val documents = it.result.documents

                val result = documents.size > 0

                myCallback(result)
            }
        }
    }
}