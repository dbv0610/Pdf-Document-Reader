// Copyright 2002-2009, FreeHEP.
package com.wxiwei.office.thirdpart.emf.io

import java.io.IOException
import java.io.InputStream

/**
 * The RoutedInputStream allows the user to add a listener for a certain
 * delimited portion of the main inputstream. This portion is marked by a start
 * and end marker. The end marker can be null, in which case the portion runs
 * from the start marker to the end of the main inputstream. The listener is
 * informed via a route (partial inputstream) when input is available. The new
 * routed inputstream (route) is supposed to be read to the end or closed, after
 * which the main inputstream should be read again. Closing a route will not
 * close the original stream, but will discard any bytes up to and including the
 * end marker. Returning from a route without reading until the end of the route
 * means that the remaining bytes are still available on the main stream.
 * Multiple routes can be added, as long as they have different start sequences.
 * Start sequences such as "StartA, StartB, StartEmpty" are allowed, but
 * "Start, StartOther" are not since they overlap. Start and End markers can be
 * the same.
 * 
 * @author Mark Donszelmann
 */
open class RoutedInputStream(private val `in`: InputStream) : DecodingInputStream() {
    private val routes: MutableMap<ByteArray, ByteArray?>
    private val listeners: MutableMap<ByteArray, RouteListener?>

    private var buffer: ByteArray

    private var sob: Int
    private var eob = 0

    private var index = 0

    private var state: Int

    private var start: ByteArray = ByteArray(0)

    /**
     * Creates a RoutedInputStream from the underlying stream.
     * 
     * @param input
     * stream to read
     */
    init {
        routes = HashMap()
        listeners = HashMap()
        // bufferlength has to be more than 1
        buffer = ByteArray(20)
        sob = -1
        state = UNROUTED
    }

    /**
     * Returns the next byte on this stream, however if a start marker is found,
     * the associated route listener is called, which should take over reading
     * the stream. In this case this method will, after the route is finished
     * reading and closed, return the first byte after the end marker. This of
     * course unless that byte is part of the next start marker.
     */
    @Throws(IOException::class)
    override fun read(): Int {
        var result: Int

        NEWSTATE@ while (true) {
            when (state) {
                UNROUTED -> {
                    // fill the buffer with one or more bytes
                    var b: Int
                    while (sob != eob) {
                        if (sob < 0) {
                            sob = 0
                        }

                        // read a byte from the underlying stream
                        b = `in`.read()
                        if (b < 0) {
                            // underlying stream closed
                            state = CLOSING
                            continue@NEWSTATE
                        }

                        // try to find a start marker
                        buffer[eob] = b.toByte()
                        eob = (eob + 1) % buffer.size

                        // search for a route
                        val i = routes.keys.iterator()
                        while (i.hasNext()) {
                            start = i.next()
                            index = ((eob + buffer.size - start.size)
                                    % buffer.size)
                            if (equals(start, buffer, index)) {
                                state = ROUTEFOUND
                                continue@NEWSTATE
                            }
                        }
                    } // while


                    // always return what drops from the buffer
                    // the buffer is one byte longer than the longest start marker,
                    // so even
                    // if we find that marker we can still return the byte just in
                    // front of it.
                    result = buffer[sob].toInt()
                    sob = (sob + 1) % buffer.size
                    return result
                }

                ROUTEFOUND -> {
                    // found a start marker, we still need to return all bytes
                    // before
                    // the marker; i.e. from sob to index
                    if (sob == index) {
                        state = ROUTEINFORM
                        continue@NEWSTATE
                    }
                    result = buffer[sob].toInt()
                    sob = (sob + 1) % buffer.size
                    return result
                }

                ROUTEINFORM -> {
                    // we inform the routelistener to start reading
                    state = ROUTED
                    val route: Route =
                        Route(start, routes.get(start))
                    // next call will generate callbacks to this read method in
                    // state ROUTED.
                    listeners.get(start)!!.routeFound(route)

                    // route listener finished
                    state = UNROUTED
                    if (sob == eob) {
                        // we restart buffering if the buffer was empty
                        sob = -1
                        eob = 0
                        continue@NEWSTATE
                    }
                    // FIXME: we need an UNROUTING here which just returns the
                    // buffer, but does not refill it, in case the reads would
                    // block...
                    // we return a byte from the buffer and
                    // let the next call take care of rebuffering, otherwise we may
                    // block
                    result = buffer[sob].toInt()
                    sob = (sob + 1) % buffer.size
                    return result
                }

                ROUTED -> {
                    // calls end up here when the Route is reading. We should
                    // return the start marker (in buffer) followed by newly read
                    // bytes.
                    if (sob == eob) {
                        result = `in`.read()
                        if (result < 0) {
                            state = CLOSED
                            continue@NEWSTATE
                        }
                    } else {
                        result = buffer[sob].toInt()
                        sob = (sob + 1) % buffer.size
                    }
                    return result
                }

                CLOSING -> {
                    // the underlying stream is closed, no more markers can be found
                    // thus the rest of the buffer is returned
                    if (sob == eob) {
                        state = CLOSED
                        continue@NEWSTATE
                    }
                    result = buffer[sob].toInt()
                    sob = (sob + 1) % buffer.size
                    return result
                }

                CLOSED ->                // all streams are closed
                    return -1

                else -> {
                    var b: Int
                    while (sob != eob) {
                        if (sob < 0) {
                            sob = 0
                        }

                        b = `in`.read()
                        if (b < 0) {
                            state = CLOSING
                            continue@NEWSTATE
                        }

                        buffer[eob] = b.toByte()
                        eob = (eob + 1) % buffer.size

                        val i = routes.keys.iterator()
                        while (i.hasNext()) {
                            start = i.next()
                            index = ((eob + buffer.size - start.size)
                                    % buffer.size)
                            if (equals(start, buffer, index)) {
                                state = ROUTEFOUND
                                continue@NEWSTATE
                            }
                        }
                    }

                    result = buffer[sob].toInt()
                    sob = (sob + 1) % buffer.size
                    return result
                }
            } // switch
        } // while
    }

    /**
     * Adds a route for given start and end string. The strings are converted
     * according to the default encoding to start and end markers (byte[]).
     * 
     * @param start
     * start marker
     * @param end
     * end marker
     * @param listener
     * listener to inform about the route
     */
    fun addRoute(start: String, end: String?, listener: RouteListener?) {
        addRoute(
            start.toByteArray(), if (end == null) null else end.toByteArray(),
            listener
        )
    }

    /**
     * Adds a route for given start and end marker. If the end marker is null,
     * the route is indefinite, and can be read until the main stream ends. If
     * the start and end marker are equal, the route can be read for exactly
     * their length.
     * 
     * @param start
     * start marker
     * @param end
     * end marker
     * @param listener
     * listener to inform about the route
     */
    fun addRoute(start: ByteArray, end: ByteArray?, listener: RouteListener?) {
        val i = routes.keys.iterator()
        while (i.hasNext()) {
            val key = String(i.next())
            val name = String(start)
            require(!(key.startsWith(name) || name.startsWith(key))) {
                ("Route '" + name
                        + "' cannot be added since it overlaps with '" + key
                        + "'.")
            }
        }

        routes.put(start, end)
        listeners.put(start, listener)
        // we make the buffer one longer than the longest start marker, so that
        // read() can always return a byte before a marker.
        if (start.size > buffer.size - 1) {
            val tmp = ByteArray(start.size + 1)
            System.arraycopy(buffer, 0, tmp, 0, buffer.size)
            buffer = tmp
        }
    }

    /**
     * Route which can be read up to and including the end marker. When you
     * close the route, all bytes including the end marker will be
     * read/discarded before returning. If you just discard the Route, the
     * underlying stream will still return you all or part of the bytes of this
     * route. If the end marker is set to null, the stream can be read until the
     * underlying stream ends.
     */
    inner class Route(
        /**
         * Returns start marker.
         * 
         * @return start marker
         */
        val start: ByteArray?,
        /**
         * Returns end marker.
         * 
         * @return end marker
         */
        val end: ByteArray?
    ) : InputStream() {
        private lateinit var buffer: ByteArray

        private var index: Int

        private var closed: Boolean

        /**
         * Creates a route with given start and end marker.
         * 
         * @param start
         * start marker
         * @param end
         * end marker
         */
        init {
            if (end != null) {
                buffer = ByteArray(end.size)
            }
            index = 0
            closed = false
        }

        /**
         * Returns bytes of this specific route, starting with the start marker,
         * followed by any bytes up to and including the end marker. If the end
         * marker is null, the route is indefinite.
         */
        @Throws(IOException::class)
        override fun read(): Int {
            if (closed) {
                return -1
            }

            val b = this@RoutedInputStream.read()
            if (b < 0) {
                closed = true
                return b
            }

            if (end == null) {
                return b
            }

            buffer[index] = b.toByte()
            index = (index + 1) % buffer.size

            closed = equals(end, buffer, index)

            return b
        }

        /**
         * Closes the stream, and discards any bytes up to and including the end
         * marker.
         */
        @Throws(IOException::class)
        override fun close() {
            while (read() >= 0) {
                continue
            }
            closed = true
        }
    }

    companion object {
        private const val UNROUTED = 0 // the main stream is returned

        private const val ROUTEFOUND = 1 // found a route, but need to

        // still return buffer
        private const val ROUTEINFORM = 2 // buffer returned, need to

        // inform routelistener
        private const val ROUTED = 3 // the main stream is now routed to

        // the route
        private const val CLOSING = 4 // the underlying stream is closed,

        // but buffer need to be emptied
        private const val CLOSED = 5 // the main stream is closed

        /**
         * Checks if cmp is equal to buf (with start at index) for the length of
         * cmp.
         * 
         * @param cmp
         * buffer1 to compare
         * @param buf
         * buffer2 to compare
         * @param index
         * start index to start compare
         * @return true if cmp == buf for length of cmp.
         */
        private fun equals(cmp: ByteArray, buf: ByteArray, index: Int): Boolean {
            for (i in cmp.size - 1 downTo 1) {
                val j = (index + buf.size + i) % buf.size
                if (buf[j] != cmp[i]) {
                    return false
                }
            }

            return buf[(index + buf.size) % buf.size] == cmp[0]
        }
    }
}
