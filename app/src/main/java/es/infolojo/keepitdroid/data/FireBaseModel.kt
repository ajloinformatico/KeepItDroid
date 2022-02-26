package es.infolojo.keepitdroid.data

import com.google.gson.annotations.SerializedName
import es.infolojo.keepitdroid.utils.NOTE_BODY
import es.infolojo.keepitdroid.utils.NOTE_COLOR
import es.infolojo.keepitdroid.utils.NOTE_TITLE
import java.io.Serializable


data class FireBaseModel(
    val title: String = "",

    val content: String = "",

    val color: FireBaseColors = FireBaseColors.WHITE_NOTE,
)

