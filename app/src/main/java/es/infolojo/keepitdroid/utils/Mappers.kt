package es.infolojo.keepitdroid.utils

import es.infolojo.keepitdroid.R
import es.infolojo.keepitdroid.data.FireBaseColors

fun String.toFireBaseColor(): FireBaseColors =
    when (this) {
        FireBaseColors.BLUE_NOTE.value -> FireBaseColors.BLUE_NOTE
        FireBaseColors.WHITE_NOTE.value -> FireBaseColors.WHITE_NOTE
        FireBaseColors.RED_NOTE.value -> FireBaseColors.RED_NOTE
        FireBaseColors.YELLOW_NOTE.value -> FireBaseColors.YELLOW_NOTE
        FireBaseColors.ORANGE_NOTE.value -> FireBaseColors.ORANGE_NOTE
        FireBaseColors.GREEN_NOTE.value -> FireBaseColors.GREEN_NOTE
        FireBaseColors.BLACK_NOTE.value -> FireBaseColors.BLACK_NOTE

        else -> {
            FireBaseColors.WHITE_NOTE
        }
    }

fun Int.toFireBaseColors(): FireBaseColors =
    when (this) {
        R.id.blue -> {
            FireBaseColors.BLUE_NOTE
        }

        R.id.red -> {
            FireBaseColors.RED_NOTE
        }

        R.id.yellow -> {
            FireBaseColors.YELLOW_NOTE
        }

        R.id.orange -> {
            FireBaseColors.ORANGE_NOTE
        }

        R.id.black -> {
            FireBaseColors.BLACK_NOTE
        }

        R.id.green -> {
            FireBaseColors.GREEN_NOTE
        }

        R.id.white -> {
            FireBaseColors.WHITE_NOTE
        }

        else -> {
            FireBaseColors.UNKNOWN
        }
    }

fun FireBaseColors.toIntColor(): Int =
    when (this) {
        FireBaseColors.BLUE_NOTE -> R.color.blue_note
        FireBaseColors.WHITE_NOTE -> R.color.white_note
        FireBaseColors.RED_NOTE -> R.color.red_note
        FireBaseColors.YELLOW_NOTE -> R.color.yellow_note
        FireBaseColors.ORANGE_NOTE -> R.color.green_note
        FireBaseColors.GREEN_NOTE -> R.color.green_note
        FireBaseColors.BLACK_NOTE -> R.color.black_note
        else -> 0
    }