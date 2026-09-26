// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException

/**
 * Generic Action, to be used with the TagIn/OutputStreams. An action can have
 * an ActionCode, a length as well as parameters.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 * @version $Id: src/main/java/org/freehep/util/io/Action.java 96b41b903496
 * 2005/11/21 19:50:18 duns $
 */
abstract class Action protected constructor(
    /**
     * @return actionCode
     */
    val code: Int
) {
    /**
     * @return name of the action
     */
    var name: String
        private set

    init {
        name = javaClass.getName()
        val dot = name.lastIndexOf(".")
        name = if (dot >= 0) name.substring(dot + 1) else name
    }

    /**
     * Read an action from the input, with given actioncode and length
     * 
     * @param actionCode
     * decoded actionCode
     * @param input
     * input to read from
     * @param length
     * length to read
     * @return action corresponding to actionCode
     * @throws IOException
     * if read fails
     */
    @Throws(IOException::class)
    abstract fun read(
        actionCode: Int, input: TaggedInputStream?,
        length: Int
    ): Action?

    override fun toString(): String {
        return "Action " + this.name + " (" + this.code + ")"
    }

    /**
     * Used for not recognized actions.
     */
    class Unknown : Action {
        private var data: IntArray = IntArray(0)

        /**
         * Create a special Action for Unknown Actions, with actioncode 0.
         */
        constructor() : super(0x00)

        /**
         * Create a special Action for Unknown Actions, with given action code.
         * 
         * @param actionCode
         * code to be used for Unknown Action.
         */
        constructor(actionCode: Int) : super(actionCode)

        @Throws(IOException::class)
        override fun read(actionCode: Int, input: TaggedInputStream?, length: Int): Action {
            val action = Unknown(actionCode)
            action.data = input!!.readUnsignedByte(length)
            return action
        }


        override fun toString(): String {
            return super.toString() + " UNKNOWN!, length " + data.size
        }
    }
}
