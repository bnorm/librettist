/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.text.diff

/**
 * This class gathers all the [commands][EditCommand] needed to transform
 * one objects sequence into another objects sequence.
 *
 *
 * An edit script is the most general view of the differences between two
 * sequences. It is built as the result of the comparison between two sequences
 * by the [StringsComparator] class. The user can
 * walk through it using the *visitor* design pattern.
 *
 *
 *
 * It is guaranteed that the objects embedded in the [insert][InsertCommand] come from the second sequence and that the objects embedded in
 * either the [delete commands][DeleteCommand] or [keep][KeepCommand] come from the first sequence. This can be important if subclassing
 * is used for some elements in the first sequence and the `equals`
 * method is specialized.
 *
 *
 * @see StringsComparator
 *
 * @see EditCommand
 *
 * @see CommandVisitor
 *
 * @see ReplacementsHandler
 *
 *
 * @param <T> object type
 * @since 1.0
</T> */
class EditScript<T> {
    /** Container for the commands.  */
    private val commands: MutableList<EditCommand<T>> = ArrayList<EditCommand<T>>()

    /**
     * Gets the length of the Longest Common Subsequence (LCS). The length of the
     * longest common subsequence is the number of [keep][KeepCommand] in the script.
     *
     * @return length of the Longest Common Subsequence
     */
    /** Length of the longest common subsequence.  */
    var lCSLength: Int = 0
        private set

    /**
     * Gets the number of effective modifications. The number of effective
     * modification is the number of [delete][DeleteCommand] and
     * [insert][InsertCommand] commands in the script.
     *
     * @return number of effective modifications
     */
    /** Number of modifications.  */
    var modifications: Int = 0
        private set

    /**
     * Appends a delete command to the script.
     *
     * @param command  command to add
     */
    fun append(command: DeleteCommand<T>) {
        commands.add(command)
        ++modifications
    }

    /**
     * Appends an insert command to the script.
     *
     * @param command  command to add
     */
    fun append(command: InsertCommand<T>) {
        commands.add(command)
        ++modifications
    }

    /**
     * Appends a keep command to the script.
     *
     * @param command  command to add
     */
    fun append(command: KeepCommand<T>) {
        commands.add(command)
        ++lCSLength
    }

    /**
     * Visits the script. The script implements the *visitor* design
     * pattern, this method is the entry point to which the user supplies its
     * own visitor, the script will be responsible to drive it through the
     * commands in order and call the appropriate method as each command is
     * encountered.
     *
     * @param visitor  the visitor that will visit all commands in turn
     */
    fun visit(visitor: CommandVisitor<T>) {
        commands.forEach { command -> command.accept(visitor) }
    }
}
