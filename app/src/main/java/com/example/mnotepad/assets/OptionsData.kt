package com.example.mnotepad.assets

import com.example.mnotepad.helpers.SORT_CREATE_DATE_FROM_NEWEST
import com.example.mnotepad.helpers.SORT_CREATE_DATE_FROM_OLDEST
import com.example.mnotepad.helpers.SORT_EDIT_DATE_FROM_NEWEST
import com.example.mnotepad.helpers.SORT_EDIT_DATE_FROM_OLDEST
import com.example.mnotepad.helpers.SORT_TITLE_A_Z
import com.example.mnotepad.helpers.SORT_TITLE_Z_A

class OptionsData {
    companion object {
        val noteSortOptions = arrayOf(
            SORT_EDIT_DATE_FROM_NEWEST,
            SORT_EDIT_DATE_FROM_OLDEST,
            SORT_TITLE_A_Z,
            SORT_TITLE_Z_A,
            SORT_CREATE_DATE_FROM_NEWEST,
            SORT_CREATE_DATE_FROM_OLDEST
        )
    }
}