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

/**
 * 
 * This class represents custom properties in the document summary
 * information stream. The difference to normal properties is that custom
 * properties have an optional name. If the name is not `null` it
 * will be maintained in the section's dictionary.
 * 
 * @author Rainer Klute [&lt;klute@rainer-klute.de&gt;](mailto:klute@rainer-klute.de)
 */
class CustomProperty : MutableProperty {
    /**
     * 
     * Gets the property's name.
     * 
     * @return the property's name.
     */
    /**
     * 
     * Sets the property's name.
     * 
     * @param name The name to set.
     */
    var name: String?

    /**
     * 
     * Creates an empty [CustomProperty]. The set methods must be
     * called to make it usable.
     */
    constructor() {
        this.name = null
    }

    /**
     * 
     * Creates a [CustomProperty] with a name.
     * 
     * @param property This property's attributes are copied to the new custom
     * property.
     * @param name The new custom property's name.
     */
    /**
     * 
     * Creates a [CustomProperty] without a name by copying the
     * underlying [Property]' attributes.
     * 
     * @param property the property to copy
     */
    @JvmOverloads
    constructor(property: Property, name: String? = null) : super(property) {
        this.name = name
    }


    /**
     * 
     * Compares two custom properties for equality. The method returns
     * `true` if all attributes of the two custom properties are
     * equal.
     * 
     * @param o The custom property to compare with.
     * @return `true` if both custom properties are equal, else
     * `false`.
     * 
     * @see java.util.AbstractSet.equals
     */
    fun equalsContents(o: Any?): Boolean {
        val c = o as CustomProperty
        val name1 = c.name
        val name2 = this.name
        var equalNames = true
        if (name1 == null) equalNames = name2 == null
        else equalNames = name1 == name2
        return equalNames && c.getID() == this.getID() && c.getType() == this.getType() && c.getValue() == this.getValue()
    }

    /**
     * @see java.util.AbstractSet.hashCode
     */
    override fun hashCode(): Int {
        return this.getID().toInt()
    }
}
