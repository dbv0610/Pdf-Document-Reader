/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */
package com.wxiwei.office.fc.hpsf

import java.util.Date

/**
 * 
 * Maintains the instances of [CustomProperty] that belong to a
 * [DocumentSummaryInformation]. The class maintains the names of the
 * custom properties in a dictionary. It implements the [Map] interface
 * and by this provides a simplified view on custom properties: A property's
 * name is the key that maps to a typed value. This implementation hides
 * property IDs from the developer and regards the property names as keys to
 * typed values.
 * 
 * 
 * While this class provides a simple API to custom properties, it ignores
 * the fact that not names, but IDs are the real keys to properties. Under the
 * hood this class maintains a 1:1 relationship between IDs and names. Therefore
 * you should not use this class to process property sets with several IDs
 * mapping to the same name or with properties without a name: the result will
 * contain only a subset of the original properties. If you really need to deal
 * such property sets, use HPSF's low-level access methods.
 * 
 * 
 * An application can call the [.isPure] method to check whether a
 * property set parsed by [CustomProperties] is still pure (i.e.
 * unmodified) or whether one or more properties have been dropped.
 * 
 * 
 * This class is not thread-safe; concurrent access to instances of this
 * class must be synchronized.
 * 
 * 
 * While this class is roughly HashMap<Long></Long>,CustomProperty>, that's the
 * internal representation. To external calls, it should appear as
 * HashMap<String></String>,Object> mapping between Names and Custom Property Values.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
class CustomProperties : HashMap<Any?, CustomProperty>() {
    /**
     * 
     * Gets the dictionary which contains IDs and names of the named custom
     * properties.
     * 
     * @return the dictionary.
     */
    /**
     * 
     * Maps property IDs to property names.
     */
    val dictionary: MutableMap<Long, String> = HashMap<Long, String>()

    /**
     * 
     * Maps property names to property IDs.
     */
    private val dictionaryNameToID: MutableMap<String?, Long?> = HashMap<String?, Long?>()

    /**
     * 
     * Tells whether this [CustomProperties] instance is pure or one or
     * more properties of the underlying low-level property set has been
     * dropped.
     * 
     * @return `true` if the [CustomProperties] is pure, else
     * `false`.
     */
    /**
     * 
     * Sets the purity of the custom property set.
     * 
     * @param isPure the purity
     */
    /**
     * 
     * Tells whether this object is pure or not.
     */
    var isPure: Boolean = true


    /**
     * 
     * Puts a [CustomProperty] into this map. It is assumed that the
     * [CustomProperty] already has a valid ID. Otherwise use
     * [.put].
     */
    fun put(name: String?, cp: CustomProperty): CustomProperty? {
        if (name == null) {
            /* Ignoring a property without a name. */
            isPure = false
            return null
        }
        require(name == cp.name) {
            "Parameter \"name\" (" + name +
                    ") and custom property's name (" + cp.name +
                    ") do not match."
        }

        /* Register name and ID in the dictionary. Mapping in both directions is possible. If there is already a  */
        val idKey = cp.getID()
        val oldID = dictionaryNameToID.get(name)
        if (oldID != null) dictionary.remove(oldID)
        dictionaryNameToID.put(name, idKey)
        dictionary.put(idKey, name)

        /* Put the custom property into this map. */
        val oldCp = super.remove(oldID)
        super.put(idKey, cp)
        return oldCp
    }


    /**
     * 
     * Puts a [CustomProperty] that has not yet a valid ID into this
     * map. The method will allocate a suitable ID for the custom property:
     * 
     * 
     * 
     *  * 
     *
     *If there is already a property with the same name, take the ID
     * of that property.
     * 
     *  * 
     *
     *Otherwise find the highest ID and use its value plus one.
     * 
     * 
     * 
     * @param customProperty
     * @return If the was already a property with the same name, the
     * @throws ClassCastException
     */
    @Throws(ClassCastException::class)
    private fun put(customProperty: CustomProperty): Any? {
        val name = customProperty.name

        /* Check whether a property with this name is in the map already. */
        val oldId = dictionaryNameToID.get(name)
        if (oldId != null) customProperty.setID(oldId)
        else {
            var max: Long = 1
            val i = dictionary.keys.iterator()
            while (i.hasNext()) {
                val id: Long = i.next()
                if (id > max) max = id
            }
            customProperty.setID(max + 1)
        }
        return this.put(name, customProperty)
    }


    /**
     * 
     * Removes a custom property.
     * @param name The name of the custom property to remove
     * @return The removed property or `null` if the specified property was not found.
     * 
     * @see java.util.HashSet.remove
     */
    fun remove(name: String?): Any? {
        val id = dictionaryNameToID.get(name)
        if (id == null) return null
        dictionary.remove(id)
        dictionaryNameToID.remove(name)
        return super.remove(id)
    }

    /**
     * 
     * Adds a named string property.
     * 
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     * `null` if there was no such property before.
     */
    fun put(name: String?, value: String?): Any? {
        val p = MutableProperty()
        p.setID(-1)
        p.setType(Variant.Companion.VT_LPWSTR.toLong())
        p.setValue(value)
        val cp = CustomProperty(p, name)
        return put(cp)
    }

    /**
     * 
     * Adds a named long property.
     * 
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     * `null` if there was no such property before.
     */
    fun put(name: String?, value: Long?): Any? {
        val p = MutableProperty()
        p.setID(-1)
        p.setType(Variant.Companion.VT_I8.toLong())
        p.setValue(value)
        val cp = CustomProperty(p, name)
        return put(cp)
    }

    /**
     * 
     * Adds a named double property.
     * 
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     * `null` if there was no such property before.
     */
    fun put(name: String?, value: Double?): Any? {
        val p = MutableProperty()
        p.setID(-1)
        p.setType(Variant.Companion.VT_R8.toLong())
        p.setValue(value)
        val cp = CustomProperty(p, name)
        return put(cp)
    }

    /**
     * 
     * Adds a named integer property.
     * 
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     * `null` if there was no such property before.
     */
    fun put(name: String?, value: Int?): Any? {
        val p = MutableProperty()
        p.setID(-1)
        p.setType(Variant.Companion.VT_I4.toLong())
        p.setValue(value)
        val cp = CustomProperty(p, name)
        return put(cp)
    }

    /**
     * 
     * Adds a named boolean property.
     * 
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     * `null` if there was no such property before.
     */
    fun put(name: String?, value: Boolean?): Any? {
        val p = MutableProperty()
        p.setID(-1)
        p.setType(Variant.Companion.VT_BOOL.toLong())
        p.setValue(value)
        val cp = CustomProperty(p, name)
        return put(cp)
    }


    /**
     * 
     * Gets a named value from the custom properties.
     * 
     * @param name the name of the value to get
     * @return the value or `null` if a value with the specified
     * name is not found in the custom properties.
     */
    fun get(name: String?): Any? {
        val id = dictionaryNameToID.get(name)
        val cp = super.get(id)
        return if (cp != null) cp.getValue() else null
    }


    /**
     * 
     * Adds a named date property.
     * 
     * @param name The property's name.
     * @param value The property's value.
     * @return the property that was stored under the specified name before, or
     * `null` if there was no such property before.
     */
    fun put(name: String?, value: Date?): Any? {
        val p = MutableProperty()
        p.setID(-1)
        p.setType(Variant.Companion.VT_FILETIME.toLong())
        p.setValue(value)
        val cp = CustomProperty(p, name)
        return put(cp)
    }

    /**
     * Returns a set of all the names of our
     * custom properties. Equivalent to
     * [.nameSet]
     */
    override val keys: MutableSet<Any?>
        get() = dictionaryNameToID.keys as MutableSet<Any?>

    /**
     * Returns a set of all the names of our
     * custom properties
     */
    fun nameSet(): MutableSet<String?> {
        return dictionaryNameToID.keys
    }

    /**
     * Returns a set of all the IDs of our
     * custom properties
     */
    fun idSet(): MutableSet<String?> {
        return dictionaryNameToID.keys
    }


    /**
     * Checks against both String Name and Long ID
     */
    override fun containsKey(key: Any?): Boolean {
        if (key is Long) {
            return super.containsKey(key)
        }
        if (key is String) {
            return super.containsKey(dictionaryNameToID.get(key))
        }
        return false
    }

    /**
     * Checks against both the property, and its values.
     */
    override fun containsValue(value: CustomProperty): Boolean {
        return containsPropertyValue(value)
    }

    /**
     * Checks whether the map contains the given [CustomProperty] or a
     * custom property whose value is identical to the given value.
     * (Kotlin's typed [containsValue] only accepts [CustomProperty]
     * instances, so the value lookup of the original Java code lives here.)
     */
    fun containsPropertyValue(value: Any?): Boolean {
        if (value is CustomProperty) {
            return super.containsValue(value)
        } else {
            for (cp in super.values) {
                if (cp.getValue() === value) {
                    return true
                }
            }
        }
        return false
    }


    var codepage: Int
        /**
         * 
         * Gets the codepage.
         * 
         * @return the codepage or -1 if the codepage is undefined.
         */
        get() {
            var codepage = -1
            val i = this.values.iterator()
            while (codepage == -1 && i.hasNext()) {
                val cp = i.next()
                if (cp.getID() == PropertyIDMap.Companion.PID_CODEPAGE.toLong()) codepage =
                    (cp.getValue() as Int)
            }
            return codepage
        }
        /**
         * 
         * Sets the codepage.
         * 
         * @param codepage the codepage
         */
        set(codepage) {
            val p = MutableProperty()
            p.setID(PropertyIDMap.Companion.PID_CODEPAGE.toLong())
            p.setType(Variant.Companion.VT_I2.toLong())
            p.setValue(codepage)
            put(CustomProperty(p))
        }
}
