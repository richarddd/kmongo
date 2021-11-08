/*
 * Copyright (C) 2016/2020 Litote
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.litote.kmongo

import org.litote.kmongo.property.KCollectionSimplePropertyPath
import org.litote.kmongo.property.KMapSimplePropertyPath
import org.litote.kmongo.property.KPropertyPath
import org.litote.kmongo.service.ClassMappingType
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty1

/**
 * Returns a composed property. For example Friend.address / Address.postalCode = "address.postalCode".
 */
inline operator fun <T0, T1, reified T2> KProperty1<T0, T1?>.div(p2: KProperty1<T1, T2?>): KProperty1<T0, T2?> =
    KPropertyPath(this, p2, T2::class.java)

/**
 * Returns a collection composed property. For example Friend.addresses / Address.postalCode = "addresses.postalCode".
 */
@JvmName("divCol")
inline operator fun <T0, T1, reified T2> KProperty1<T0, Iterable<T1>?>.div(p2: KProperty1<T1, T2?>): KProperty1<T0, T2?> =
    KPropertyPath(this, p2, T2::class.java)

/**
 * Returns a map composed property. For example Friend.addresses / Address.postalCode = "addresses.postalCode".
 */
@JvmName("divMap")
inline operator fun <T0, K, T1, reified T2> KProperty1<T0, Map<out K, T1>?>.div(p2: KProperty1<T1, T2?>): KProperty1<T0, T2?> =
    KPropertyPath(this, p2, T2::class.java)

/**
 * Returns a mongo path of a property.
 */
inline fun <reified T> KProperty<T>.path(): String =
    (this as? KPropertyPath<*, T>)?.path() ?: ClassMappingType.getPath(this, T::class.java)

/**
 * Returns a collection property.
 */
inline fun <reified T, R> KProperty1<out R, Iterable<T>?>.colProperty(): KCollectionSimplePropertyPath<R, T> =
    KCollectionSimplePropertyPath(null, this, T::class.java)

/**
 * Returns a map property.
 */
inline fun <reified K, reified T> KProperty1<out Any?, Map<out K, T>?>.mapProperty(): KMapSimplePropertyPath<out Any?, K, T> =
    KMapSimplePropertyPath(null, this, K::class.java, T::class.java)

/**
 * [The positional array operator $ (projection or update)](https://docs.mongodb.com/manual/reference/operator/update/positional/)
 */
inline fun <reified T> KProperty1<out Any?, Iterable<T>?>.posOp(): KPropertyPath<out Any?, T?> = colProperty().posOp

/**
 * [The all positional operator $[]](https://docs.mongodb.com/manual/reference/operator/update/positional-all/)
 */
inline fun <reified T> KProperty1<out Any?, Iterable<T>?>.allPosOp(): KPropertyPath<out Any?, T?> =
    colProperty().allPosOp

/**
 * [The filtered positional operator $[<identifier>]](https://docs.mongodb.com/manual/reference/operator/update/positional-filtered/)
 */
inline fun <reified T> KProperty1<out Any?, Iterable<T>?>.filteredPosOp(identifier: String): KPropertyPath<out Any?, T?> =
    colProperty().filteredPosOp(identifier)

/**
 * Key projection of map.
 * Sample: `p.keyProjection(Locale.ENGLISH) / Gift::amount`
 */
inline fun <reified K, reified T> KProperty1<out Any?, Map<out K, T>?>.keyProjection(key: K): KPropertyPath<out Any?, T?> =
    mapProperty().keyProjection(key)