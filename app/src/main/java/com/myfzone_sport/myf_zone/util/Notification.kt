package com.myfzone_sport.myf_zone.util

import com.myfzone_sport.myf_zone.model.coach.Coach
import com.myfzone_sport.myf_zone.model.event.Event


/**
 * Created by Amadou on 04/01/2021, 19:33
 *
 * : notification text
 *
 */

sealed class Notification {
    companion object {
        fun notificationEventTitle(event: Event) = "${event.type} - ${event.title}"

        fun notificationEventParticipation() = "eventParticipation"
        fun notificationEventAcceptParticipation() = "eventAcceptParticipation"
        fun notificationEventRefuseParticipation() = "eventRefuseParticipation"
        fun notificationEventModifyParticipation() = "eventModification"
        fun notificationEventCancelParticipation() = "eventCancellation"

        fun notificationEventParticipationMessage(coach: Coach) =
            "${coach.getName()} souhaite participer à l'évenement"

        fun notificationAcceptParticipationMessage() =
            "L'organisateur de l'évenement à accepté votre participation"

        fun notificationRefuseParticipationMessage() =
            "L'organisateur de l'évenement à refusé votre participation"

        fun notificationModifyParticipationMessage() =
            "L'organisateur de l'évenement à modifié l'évènement"

        fun notificationCancelParticipationMessage() =
            "L'organisateur de l'évenement à annulé l'évènement"
    }
}
