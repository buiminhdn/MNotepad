package com.example.mnotepad.helpers

import com.example.mnotepad.entities.models.ChecklistItem

object TextConvertHelper {
    fun convertContentToCheckList(content: String): MutableList<ChecklistItem> {
        return content.lines()
            .filter { it.isNotBlank() }
            .map { ChecklistItem(it.trim(), false) } as MutableList<ChecklistItem>
    }

    fun convertCheckListToContentForSave(items: List<ChecklistItem>): String {
        val content: MutableList<String> = mutableListOf()
        val filterItems = items.filter { it.text.isNotBlank() }
        for (item in filterItems) {
            if (item.isChecked)
                content.add("-[x] ${item.text}")
            else
                content.add("-[o] ${item.text}")
        }
        return content.joinToString("\n")
    }

    fun convertCheckListToContent(items: List<ChecklistItem>): String {
        val content = items.map { it.text }
        return content.joinToString("\n") { line ->
            line.replaceFirst("-[x] ", "")
                .replaceFirst("-[o] ", "")
        }
    }

    fun convertCheckListContentToText(content: String) : MutableList<ChecklistItem> {
        val listItems: MutableList<ChecklistItem> = mutableListOf()
        val lines = content.split("\n")
        for (line in lines) {
            val temp = line.split("""\s""".toRegex(), 2)
            val firstItem = temp.first()
            val text = temp.last()

            val isChecked = firstItem.contains("x")

            val checkItem = ChecklistItem(isChecked = isChecked, text = text)
            listItems.add(checkItem)
        }
        return listItems
    }
}