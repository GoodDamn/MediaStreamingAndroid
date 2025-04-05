package good.damn.editor.mediastreaming.views.dialogs.option

import android.text.TextWatcher

interface MSOptionTextWatcher
: TextWatcher {
    var model: MSMOption?
}