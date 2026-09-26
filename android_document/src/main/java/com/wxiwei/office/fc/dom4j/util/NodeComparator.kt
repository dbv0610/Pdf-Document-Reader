/*
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 *
 * This software is open source.
 * See the bottom of this file for the licence.
 */
package com.wxiwei.office.fc.dom4j.util

import com.wxiwei.office.fc.dom4j.Attribute
import com.wxiwei.office.fc.dom4j.Branch
import com.wxiwei.office.fc.dom4j.CDATA
import com.wxiwei.office.fc.dom4j.CharacterData
import com.wxiwei.office.fc.dom4j.Comment
import com.wxiwei.office.fc.dom4j.Document
import com.wxiwei.office.fc.dom4j.DocumentType
import com.wxiwei.office.fc.dom4j.Element
import com.wxiwei.office.fc.dom4j.Entity
import com.wxiwei.office.fc.dom4j.Namespace
import com.wxiwei.office.fc.dom4j.Node
import com.wxiwei.office.fc.dom4j.ProcessingInstruction
import com.wxiwei.office.fc.dom4j.QName
import com.wxiwei.office.fc.dom4j.Text

/**
 * 
 * 
 * `NodeComparator` is a [Comparator]of Node instances which
 * is capable of comparing Nodes for equality based on their values.
 * 
 * 
 * @author [James Strachan ](mailto:jstrachan@apache.org)
 * @version $Revision: 1.10 $
 */
class NodeComparator : Comparator<Any?> {
    /**
     * Compares its two arguments for order. Returns a negative integer, zero,
     * or a positive integer as the first argument is less than, equal to, or
     * greater than the second.
     * 
     * 
     * 
     * The implementor must ensure that <tt>sgn(compare(x, y)) ==
     * -sgn(compare(y, x))</tt>
     * for all <tt>x</tt> and <tt>y</tt>. (This implies that
     * <tt>compare(x, y)</tt> must throw an exception if and only if
     * <tt>compare(y, x)</tt> throws an exception.)
     * 
     * 
     * 
     * 
     * The implementor must also ensure that the relation is transitive:
     * <tt>((compare(x, y)&gt;0) &amp;&amp; (compare(y, z)&gt;0))</tt> implies
     * <tt>compare(x, z)&gt;0</tt>.
     * 
     * 
     * 
     * 
     * Finally, the implementer must ensure that <tt>compare(x, y)==0</tt>
     * implies that <tt>sgn(compare(x, z))==sgn(compare(y, z))</tt> for all
     * <tt>z</tt>.
     * 
     * 
     * 
     * 
     * It is generally the case, but *not * strictly required that
     * <tt>(compare(x, y)==0) == (x.equals(y))</tt>. Generally speaking, any
     * comparator that violates this condition should clearly indicate this
     * fact. The recommended language is "Note: this comparator imposes
     * orderings that are inconsistent with equals."
     * 
     * 
     * @param o1
     * the first object to be compared.
     * @param o2
     * the second object to be compared.
     * 
     * @return a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second.
     */
    override fun compare(o1: Any?, o2: Any?): Int {
        if (o1 === o2) {
            return 0
        } else if (o1 == null) {
            // null is less
            return -1
        } else if (o2 == null) {
            return 1
        }

        if (o1 is Node) {
            if (o2 is Node) {
                return compare(o1, o2)
            } else {
                // Node implementations are greater
                return 1
            }
        } else {
            if (o2 is Node) {
                // Node implementations are greater
                return -1
            } else {
                if (o1 is Comparable<*>) {
                    val c1 = o1 as Comparable<Any?>

                    return c1.compareTo(o2)
                } else {
                    val name1 = o1.javaClass.getName()
                    val name2 = o2.javaClass.getName()

                    return name1.compareTo(name2)
                }
            }
        }
    }

    fun compare(n1: Node, n2: Node): Int {
        val nodeType1 = n1.getNodeType().toInt()
        val nodeType2 = n2.getNodeType().toInt()
        val answer = nodeType1 - nodeType2

        if (answer != 0) {
            return answer
        } else {
            when (nodeType1.toShort()) {
                Node.Companion.ELEMENT_NODE -> return compare(n1 as Element, n2 as Element)

                Node.Companion.DOCUMENT_NODE -> return compare(n1 as Document, n2 as Document)

                Node.Companion.ATTRIBUTE_NODE -> return compare(n1 as Attribute, n2 as Attribute)

                Node.Companion.TEXT_NODE -> return compare(n1 as Text, n2 as Text)

                Node.Companion.CDATA_SECTION_NODE -> return compare(n1 as CDATA, n2 as CDATA)

                Node.Companion.ENTITY_REFERENCE_NODE -> return compare(n1 as Entity, n2 as Entity)

                Node.Companion.PROCESSING_INSTRUCTION_NODE -> return compare(
                    n1 as ProcessingInstruction,
                    n2 as ProcessingInstruction
                )

                Node.Companion.COMMENT_NODE -> return compare(n1 as Comment, n2 as Comment)

                Node.DOCUMENT_TYPE_NODE -> return compareDocType(
                    n1 as DocumentType,
                    n2 as DocumentType
                )

                Node.Companion.NAMESPACE_NODE -> return compare(n1 as Namespace, n2 as Namespace)

                else -> throw RuntimeException(
                    ("Invalid node types. node1: " + n1 + " and node2: "
                            + n2)
                )
            }
        }
    }

    fun compare(n1: Document, n2: Document): Int {
        var answer = compareDocType(n1.getDocType(), n2.getDocType())

        if (answer == 0) {
            answer = compareContent(n1, n2)
        }

        return answer
    }

    fun compare(n1: Element, n2: Element): Int {
        var answer = compare(n1.getQName(), n2.getQName())

        if (answer == 0) {
            // lets compare attributes
            val c1 = n1.attributeCount()
            val c2 = n2.attributeCount()
            answer = c1 - c2

            if (answer == 0) {
                for (i in 0..<c1) {
                    val a1 = n1.attribute(i)
                    val qn = a1?.getQName()
                    val a2 = if (qn != null) n2.attribute(qn) else null
                    answer = compare(a1, a2)

                    if (answer != 0) {
                        return answer
                    }
                }

                answer = compareContent(n1, n2)
            }
        }

        return answer
    }

    fun compare(n1: Attribute, n2: Attribute): Int {
        var answer = compare(n1.getQName(), n2.getQName())

        if (answer == 0) {
            answer = compare(n1.getValue(), n2.getValue())
        }

        return answer
    }

    fun compare(n1: QName, n2: QName): Int {
        var answer = compare(n1.getNamespaceURI(), n2.getNamespaceURI())

        if (answer == 0) {
            answer = compare(n1.getQualifiedName(), n2.getQualifiedName())
        }

        return answer
    }

    fun compare(n1: Namespace, n2: Namespace): Int {
        var answer = compare(n1.getURI(), n2.getURI())

        if (answer == 0) {
            answer = compare(n1.getPrefix(), n2.getPrefix())
        }

        return answer
    }

    fun compare(t1: CharacterData, t2: CharacterData): Int {
        return compare(t1.getText(), t2.getText())
    }

    fun compareDocType(o1: DocumentType?, o2: DocumentType?): Int {
        if (o1 === o2) {
            return 0
        } else if (o1 == null) {
            // null is less
            return -1
        } else if (o2 == null) {
            return 1
        }

        var answer = compare(o1.getPublicID(), o2.getPublicID())

        if (answer == 0) {
            answer = compare(o1.getSystemID(), o2.getSystemID())

            if (answer == 0) {
                answer = compare(o1.getName(), o2.getName())
            }
        }

        return answer
    }

    fun compare(n1: Entity, n2: Entity): Int {
        var answer = compare(n1.getName(), n2.getName())

        if (answer == 0) {
            answer = compare(n1.getText(), n2.getText())
        }

        return answer
    }

    fun compare(n1: ProcessingInstruction, n2: ProcessingInstruction): Int {
        var answer = compare(n1.getTarget(), n2.getTarget())

        if (answer == 0) {
            answer = compare(n1.getText(), n2.getText())
        }

        return answer
    }

    fun compareContent(b1: Branch, b2: Branch): Int {
        val c1 = b1.nodeCount()
        val c2 = b2.nodeCount()
        var answer = c1 - c2

        if (answer == 0) {
            for (i in 0..<c1) {
                val n1 = b1.node(i)
                val n2 = b2.node(i)
                answer = compare(n1, n2)

                if (answer != 0) {
                    break
                }
            }
        }

        return answer
    }

    fun compare(o1: String?, o2: String?): Int {
        if (o1 === o2) {
            return 0
        } else if (o1 == null) {
            // null is less
            return -1
        } else if (o2 == null) {
            return 1
        }

        return o1.compareTo(o2)
    }
} /*
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided that the
 * following conditions are met:
 * 
 * 1. Redistributions of source code must retain copyright statements and
 * notices. Redistributions must also contain a copy of this document.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The name "DOM4J" must not be used to endorse or promote products derived
 * from this Software without prior written permission of MetaStuff, Ltd. For
 * written permission, please contact dom4j-info@metastuff.com.
 * 
 * 4. Products derived from this Software may not be called "DOM4J" nor may
 * "DOM4J" appear in their names without prior written permission of MetaStuff,
 * Ltd. DOM4J is a registered trademark of MetaStuff, Ltd.
 * 
 * 5. Due credit should be given to the DOM4J Project - http://www.dom4j.org
 * 
 * THIS SOFTWARE IS PROVIDED BY METASTUFF, LTD. AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL METASTUFF, LTD. OR ITS CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * 
 * Copyright 2001-2005 (C) MetaStuff, Ltd. All Rights Reserved.
 */

