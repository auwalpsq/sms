package com.sms.ui.components

import com.vaadin.flow.component.ComponentEventListener
import com.vaadin.flow.component.Key
import com.vaadin.flow.component.KeyDownEvent
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.html.Span
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.orderedlayout.HorizontalLayout
import com.vaadin.flow.component.textfield.TextField

/** A reusable search bar component for views like ApplicantsView, GuardiansView, etc. */
class SearchBar(placeholderText: String = "Search...", private val onSearch: (String) -> Unit) :
        HorizontalLayout() {

    private val searchField =
            TextField().apply {
                placeholder = placeholderText
                width = "300px"
                addKeyDownListener(
                        Key.ENTER,
                        ComponentEventListener<KeyDownEvent> { onSearch(value.trim()) }
                )
            }

    private val searchButton =
            Button().apply {
                icon = Span().apply { addClassNames("ph", "ph-magnifying-glass") }
                addThemeVariants(ButtonVariant.LUMO_PRIMARY)
                addClickListener { onSearch(searchField.value.trim()) }
            }

    private val clearButton =
            Button().apply {
                icon = Span().apply { addClassNames("ph", "ph-x") }
                addThemeVariants(ButtonVariant.LUMO_TERTIARY)
                addClickListener {
                    searchField.clear()
                    onSearch("") // Clear filter
                }
            }

    init {
        alignItems = FlexComponent.Alignment.CENTER
        isSpacing = true
        add(searchField, searchButton, clearButton)
    }

    /** ✅ Expose the search value to parent views */
    var value: String
        get() = searchField.value.trim()
        set(newValue) {
            searchField.value = newValue
        }
}
