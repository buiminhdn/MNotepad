package com.example.mnotepad.assets

import com.example.mnotepad.helpers.FIFTEEN_UNLOCK_TIME
import com.example.mnotepad.helpers.FIVE_UNLOCK_TIME
import com.example.mnotepad.helpers.ONE_UNLOCK_TIME
import com.example.mnotepad.helpers.SORT_COLOR
import com.example.mnotepad.helpers.SORT_CREATE_DATE_FROM_NEWEST
import com.example.mnotepad.helpers.SORT_CREATE_DATE_FROM_OLDEST
import com.example.mnotepad.helpers.SORT_EDIT_DATE_FROM_NEWEST
import com.example.mnotepad.helpers.SORT_EDIT_DATE_FROM_OLDEST
import com.example.mnotepad.helpers.SORT_TITLE_A_Z
import com.example.mnotepad.helpers.SORT_TITLE_Z_A
import com.example.mnotepad.helpers.TEN_UNLOCK_TIME
import com.example.mnotepad.helpers.THIRTY_UNLOCK_TIME

object OptionsData {
    val noteSortOptions = arrayOf(
        SORT_EDIT_DATE_FROM_NEWEST,
        SORT_EDIT_DATE_FROM_OLDEST,
        SORT_TITLE_A_Z,
        SORT_TITLE_Z_A,
        SORT_CREATE_DATE_FROM_NEWEST,
        SORT_CREATE_DATE_FROM_OLDEST,
        SORT_COLOR
    )

    val colorPalette = listOf(
        "#FFAEAB", "#FFD7A6", "#FFFFB5", "#CDFDBF", "#9FF6FD", "#A0C3FD",
        "#BBB3FE", "#FFC5FF", "#FEFFF9", "#D5E5FF", "#D7F9F8", "#FFFEE9",
        "#FEF0D5", "#FAE0E1", "#E2D3F0", "#7E9CCE", "#95B7D0", "#B8E1D3",
        "#D5E9DD", "#EAC3D6", "#8A4F91", "#BD5091", "#FD645F", "#FF8434",
        "#FEA500", "#FED47E"
    )

    val unlockTimes = arrayOf(
        ONE_UNLOCK_TIME to "1 minutes",
        FIVE_UNLOCK_TIME to "5 minutes",
        TEN_UNLOCK_TIME to "10 minutes",
        FIFTEEN_UNLOCK_TIME to "15 minutes",
        THIRTY_UNLOCK_TIME to "20 minutes"
    )
}
