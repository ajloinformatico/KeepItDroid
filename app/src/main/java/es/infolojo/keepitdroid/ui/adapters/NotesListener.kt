package es.infolojo.keepitdroid.ui.adapters

import es.infolojo.keepitdroid.data.FireBaseModel

sealed class NotesListener {
    data class DetailAction(val item: FireBaseModel, val position: Int): NotesListener()
    data class DeleteAction(val item: FireBaseModel, val position: Int): NotesListener()
}