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

package org.litote.kmongo.util

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.util.MongoIdUtil.IdPropertyWrapper.Companion.NO_ID
import org.litote.kmongo.util.ReflectProperties.lazySoft
import java.lang.reflect.Field
import java.lang.reflect.Parameter
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * Returns the Mongo Id property of the [KClass],
 * or null if no id property is found.
 */
@Suppress("UNCHECKED_CAST")
val Class<*>.idProperty: Field?
    get() = MongoIdUtil.findIdProperty(this)

/**
 * Returns the Mongo Id value (which can be null),
 * or null if no id property is found.
 */
val Any?.idValue: Any?
    get() = this?.javaClass?.idProperty?.get(this)

internal object MongoIdUtil {


    fun getSuperClasses(type: Class<*>): List<Class<*>> {
        val classList = mutableListOf<Class<*>>()
        var clazz = type
        var superclass = clazz.superclass
        while (superclass != null) {
            clazz = superclass
            classList.add(superclass)
            superclass = clazz.superclass
        }
        return classList
    }

    private sealed class IdPropertyWrapper {

        companion object {
            val NO_ID = NoIdProperty()
        }

        val field: Field?
            get() = when (this) {
                is NoIdProperty -> null
                is IdProperty -> prop
            }

        class NoIdProperty : IdPropertyWrapper()
        class IdProperty(val prop: Field) : IdPropertyWrapper()
    }

    private val propertyIdCache: MutableMap<Class<*>, IdPropertyWrapper>
            by lazySoft { ConcurrentHashMap<Class<*>, IdPropertyWrapper>() }

    fun findIdProperty(type: Class<*>): Field? =
        propertyIdCache.getOrPut(type) {
            (getAnnotatedMongoIdProperty(type)
                ?: getIdProperty(type))
                ?.let { IdPropertyWrapper.IdProperty(it) }
                ?: NO_ID

        }.field

    private fun getIdProperty(type: Class<*>): Field? =
        try {
            type.declaredFields.find { "_id" == it.name }
        } catch (error: Exception) {
            //ignore
            null
        }

    private fun getAnnotatedMongoIdProperty(type: Class<*>): Field? =
        try {
            val parameter = findPrimaryConstructorParameter(type)
            if (parameter != null) {
                type.declaredFields.firstOrNull {
                    it.name == parameter.name
                }
            } else {
                type.declaredFields.find { f ->
                    f.isAnnotationPresent(BsonId::class.java)
                }
            }
        } catch (error: Error) {
            //ignore
            null
        }

    private fun findPrimaryConstructorParameter(type: Class<*>): Parameter? =
        try {
            type.constructors.mapNotNull {
                it.parameters.firstOrNull()?.takeIf {
                    it.getAnnotation(BsonId::class.java) != null
                }
            }.firstOrNull()
                ?: getSuperClasses(type).mapNotNull { findPrimaryConstructorParameter(it) }.firstOrNull()
        } catch (error: Error) {
            null
        }

    fun getIdValue(idProperty: KProperty1<Any, *>, instance: Any): Any? {
        //idProperty.isAccessible = true
        return idProperty.get(instance)
    }

}