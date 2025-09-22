package com.example.mnotepad.assets

import android.graphics.Color
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
        val colorOptions = listOf(
            Color.CYAN, Color.rgb(179, 157, 219), Color.MAGENTA, Color.rgb(245, 245, 220), Color.YELLOW,
            Color.rgb(169, 169, 169), Color.GREEN, Color.rgb(244, 164, 96), Color.BLUE, Color.RED,
            Color.rgb(255, 228, 181), Color.rgb(72, 61, 139), Color.rgb(205, 92, 92), Color.rgb(255, 165, 0), Color.rgb(102, 205, 170)
        )

        val colorPalette = listOf(
            "#FFAEAB", "#FFD7A6", "#FFFFB5", "#CDFDBF", "#9FF6FD", "#A0C3FD",
            "#BBB3FE", "#FFC5FF", "#FEFFF9", "#D5E5FF", "#D7F9F8", "#FFFEE9",
            "#FEF0D5", "#FAE0E1", "#E2D3F0", "#7E9CCE", "#95B7D0", "#B8E1D3",
            "#D5E9DD", "#EAC3D6", "#8A4F91", "#BD5091", "#FD645F", "#FF8434",
            "#FEA500", "#FED47E"
        )

    }
}