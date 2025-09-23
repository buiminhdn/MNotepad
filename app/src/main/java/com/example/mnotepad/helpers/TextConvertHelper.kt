package com.example.mnotepad.helpers

import com.example.mnotepad.entities.models.ChecklistItem

object TextConvertHelper {
    fun convertContentToCheckList(content: String): List<ChecklistItem> {
        return content.lines()
            .filter { it.isNotBlank() }
            .map { ChecklistItem(it.trim(), false) }
    }

    fun convertTextToChecklistContent(content: String): String {
        return content.lines()
            .filter { it.isNotBlank() }
            .joinToString("\n") { "- [ ] $it" }
    }

    fun convertChecklistContentToText(content: String): String {
        return content.lines().joinToString("\n") { line ->
            line.replaceFirst("- [x] ", "")
                .replaceFirst("- [ ] ", "")
        }
    }
}