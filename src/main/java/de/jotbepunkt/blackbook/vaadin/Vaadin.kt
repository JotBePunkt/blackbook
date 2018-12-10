package de.jotbepunkt.blackbook.vaadin

import com.vaadin.data.Binder
import com.vaadin.data.HasValue
import kotlin.reflect.KMutableProperty1

data class Binding<BO, T>(
        val property: KMutableProperty1<BO, T?>,
        val field: HasValue<T>)

infix fun <BO, T> KMutableProperty1<BO, T?>.to(that: HasValue<T>): Binding<BO, T> = Binding(this, that)

fun <BO> Binder<BO>.bind(vararg bindings: Binding<BO, *>) =
        bindings.forEach { this.bindSingle(it) }

fun <BO> Binder<BO>.bind(bindings: List<Binding<BO, out Any>>) =
        bindings.forEach { this.bindSingle(it) }

private fun <BO, T> Binder<BO>.bindSingle(binding: Binding<BO, T>) =
        forField(binding.field)
                .bind(
                        { it -> binding.property.get(it) },
                        { it, value -> binding.property.set(it, value) }
                )
