package com.sms.ui.components

import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import kotlin.math.ceil

class PaginationBar(
    private val pageSize: Int = 10,
    private val onPageChange: (Int) -> Unit
) : HorizontalLayout() {

    private var currentPage = 1
    private var totalRecords = 0
    private var totalPages = 1

    private val prevButton = Button("Previous").apply {
        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        isEnabled = false
        addClickListener {
            if (currentPage > 1) {
                currentPage--
                onPageChange(currentPage)
                update()
            }
        }
    }

    private val nextButton = Button("Next").apply {
        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        addClickListener {
            if (currentPage < totalPages) {
                currentPage++
                onPageChange(currentPage)
                update()
            }
        }
    }

    private val infoLabel = Span("Page 1 of 1")

    init {
        defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
        isSpacing = true
        add(prevButton, infoLabel, nextButton)
    }

    fun update(totalRecords: Int) {
        this.totalRecords = totalRecords
        this.totalPages = if (totalRecords == 0) 1 else ceil(totalRecords.toDouble() / pageSize).toInt()
        update()
    }

    private fun update() {
        val startRecord = if (totalRecords == 0) 0 else ((currentPage - 1) * pageSize) + 1
        val endRecord = minOf(currentPage * pageSize, totalRecords)
        infoLabel.text = "Page $currentPage of $totalPages — Showing $startRecord–$endRecord of $totalRecords"

        prevButton.isEnabled = currentPage > 1
        nextButton.isEnabled = currentPage < totalPages
    }

    fun reset() {
        currentPage = 1
        onPageChange(currentPage)
        update()
    }

    fun getCurrentPage(): Int = currentPage
}