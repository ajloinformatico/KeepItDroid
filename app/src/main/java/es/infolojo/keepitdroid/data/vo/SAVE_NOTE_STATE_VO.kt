package es.infolojo.keepitdroid.data.vo

enum class SAVE_NOTE_STATE_VO(val data: String) {
    NOTE_SAVE("The note have been save correctly"),
    TITLE_ERROR("You forgot the title"),
    BODY_ERROR("You forgot the note"),
    GLOBAL_ERROR("Where is the content ?")
}