package hr.fer.tel.gibalica.utils

import androidx.navigation.NavDirections

sealed class NavCommand {
    data class To(val directions: NavDirections) : NavCommand()
    object Up : NavCommand()
    data class UpTo(val destinationId: Int) : NavCommand()
    object ToRoot : NavCommand()
}
