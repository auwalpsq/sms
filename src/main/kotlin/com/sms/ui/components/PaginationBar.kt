package com.sms.ui.components

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout

/**
 * A reusable pagination component for Vaadin views.
 * Handles next/previous navigation and page display.
 */
class PaginationBar(
    private val pageSize: Int = 10,
    private val onPageChange: (Int) -> Unit
) : HorizontalLayout() {

    private var currentPage = 1 // ✅ start from 1 instead of 0

    private val prevButton = Button("Previous").apply {
        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        isEnabled = false
        addClickListener {
            if (currentPage > 1) {
                currentPage--
                onPageChange(currentPage)
                updatePageLabel()
            }
        }
    }

    private val nextButton = Button("Next").apply {
        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        addClickListener {
            currentPage++
            onPageChange(currentPage)
            updatePageLabel()
        }
    }

    private val pageLabel = Span("Page 1")

    init {
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        isSpacing = true
        add(prevButton, pageLabel, nextButton)
    }

    fun update(totalItemsFetched: Int) {
        prevButton.isEnabled = currentPage > 1
        nextButton.isEnabled = totalItemsFetched >= pageSize
    }

    fun reset() {
        currentPage = 1
        onPageChange(currentPage)
        updatePageLabel()
    }

    private fun updatePageLabel() {
        pageLabel.text = "Page $currentPage"
    }

    fun getCurrentPage(): Int = currentPage
}