package com.sms.ui.components

import com.vaadin.flow.component.Key
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.TextField

/**
 * A reusable search bar component for views like ApplicantsView, GuardiansView, etc.
 */
class SearchBar(
    placeholderText: String = "Search...",
    onSearch: (String) -> Unit
) : HorizontalLayout() {

    private val searchField = TextField().apply {
        placeholder = placeholderText
        width = "300px"
        // ✅ Explicitly specify listener type
        addKeyDownListener(Key.ENTER, {
            onSearch(value.trim())
        })
    }

    private val searchButton = Button(Icon(VaadinIcon.SEARCH)).apply {
        addThemeVariants(ButtonVariant.LUMO_PRIMARY)
        addClickListener {
            onSearch(searchField.value.trim())
        }
    }

    private val clearButton = Button(Icon(VaadinIcon.CLOSE_SMALL)).apply {
        addThemeVariants(ButtonVariant.LUMO_TERTIARY)
        addClickListener {
            searchField.clear()
            onSearch("") // clear filter
        }
    }

    init {
        alignItems = FlexComponent.Alignment.CENTER
        isSpacing = true
        add(searchField, searchButton, clearButton)
    }
}