// Copyright 2001, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

/**
 * Class to keep registered Actions, which should be used by the
 * TaggedIn/OutputStream. A set of recognized Actions can be added to this
 * class. A concrete implementation of this stream should install all allowed
 * actions.
 * 
 * @author Mark Donszelmann
 * @author Charles Loomis
 * @version $Id: src/main/java/org/freehep/util/io/ActionSet.java 96b41b903496
 * 2005/11/21 19:50:18 duns $
 */
class ActionSet {
    /**
     * This holds the individual actions.
     */
    protected var actions: MutableMap<Int, Action>

    protected var defaultAction: Action?

    /**
     * Creates an set for actions
     */
    init {
        actions = HashMap()
        defaultAction = Action.Unknown()
    }

    /**
     * Adds an action to the set
     * 
     * @param action
     * to be added
     */
    fun addAction(action: Action) {
        actions.put(action.code, action)
    }

    /**
     * Looks up the corresponding action for an action code.
     * 
     * @param actionCode
     * code to be looked for
     * @return corresponding action, or Action.Unknown in case Action is not
     * found.
     */
    fun get(actionCode: Int): Action? {
        var action = actions.get(actionCode)
        if (action == null) {
            action = defaultAction
        }
        return action
    }

    /**
     * Looks if an Action for code is in this set.
     * 
     * @param actionCode
     * code to be looked for
     * @return true if action exists for code
     */
    fun exists(actionCode: Int): Boolean {
        return (actions.get(actionCode) != null)
    }
}
