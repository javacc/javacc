/* Copyright (c) 2007, Sun Microsystems, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name of the Sun Microsystems, Inc. nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.javacc.parser;

import junit.framework.TestCase;

public final class ExpansionTest extends TestCase {

    private Token t;
    private Expansion e;

    public void setUp() {
        t = new Token();
        t.beginColumn = 2;
        t.beginLine = 3;
        e = new Expansion();
        e.column = 5;
        e.line = 6;
    }
    public void testZeroOrOneConstructor() {
        ZeroOrOne zoo = new ZeroOrOne(t, e);
        assertEquals(t.beginColumn, zoo.column);
        assertEquals(t.beginLine, zoo.line);
        assertEquals(e, zoo.expansion);
        assertSame(e.parent, zoo);
    }

    public void testZeroOrMoreConstructor() {
        ZeroOrMore zom = new ZeroOrMore(t, e);
        assertEquals(t.beginColumn, zom.column);
        assertEquals(t.beginLine, zom.line);
        assertEquals(e, zom.expansion);
        assertEquals(e.parent, zom);
    }

    public void testRZeroOrMoreConstructor() {
        RegularExpression r = new RChoice();
        RZeroOrMore rzom = new RZeroOrMore(t, r);
        assertEquals(t.beginColumn, rzom.column);
        assertEquals(t.beginLine, rzom.line);
        assertEquals(r, rzom.regexpr);
    }

    public void testROneOrMoreConstructor() {
        RegularExpression r = new RChoice();
        ROneOrMore room = new ROneOrMore(t, r);
        assertEquals(t.beginColumn, room.column);
        assertEquals(t.beginLine, room.line);
        assertEquals(r, room.regexpr);
    }

    public void testOneOrMoreConstructor() {
        Expansion rce = new RChoice();
        OneOrMore oom = new OneOrMore(t, rce);
        assertEquals(t.beginColumn, oom.column);
        assertEquals(t.beginLine, oom.line);
        assertEquals(rce, oom.expansion);
        assertEquals(rce.parent, oom);
    }


    public void testRStringLiteralConstructor() {
        RStringLiteral r = new RStringLiteral(t, "hey");
        assertEquals(t.beginColumn, r.column);
        assertEquals(t.beginLine, r.line);
        assertEquals("hey", r.image);
    }

    public void testChoiceConstructor() {
        Choice c = new Choice(t);
        assertEquals(t.beginColumn, c.column);
        assertEquals(t.beginLine, c.line);
        c = new Choice(e);
        assertEquals(e.column, c.column);
        assertEquals(e.line, c.line);
        assertEquals(e, c.choices.get(0));
    }

    public void testRJustNameConstructor() {
        RJustName r = new RJustName(t, "hey");
        assertEquals(t.beginColumn, r.column);
        assertEquals(t.beginLine, r.line);
        assertEquals("hey", r.label);
    }

    public void testSequenceConstructor() {
        Lookahead la = new Lookahead();
        Sequence s = new Sequence(t, la);
        assertEquals(t.beginColumn, s.column);
        assertEquals(t.beginLine, s.line);
        assertSame(la, s.units.get(0));
    }
}
