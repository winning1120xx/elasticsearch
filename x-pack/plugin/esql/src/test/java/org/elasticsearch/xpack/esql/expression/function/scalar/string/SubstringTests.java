/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.xpack.esql.expression.function.scalar.string;

import org.apache.lucene.util.BytesRef;
import org.elasticsearch.xpack.esql.expression.function.scalar.AbstractScalarFunctionTestCase;
import org.elasticsearch.xpack.ql.expression.Expression;
import org.elasticsearch.xpack.ql.expression.Literal;
import org.elasticsearch.xpack.ql.tree.Source;
import org.elasticsearch.xpack.ql.type.DataType;
import org.elasticsearch.xpack.ql.type.DataTypes;
import org.hamcrest.Matcher;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

public class SubstringTests extends AbstractScalarFunctionTestCase {
    @Override
    protected List<Object> simpleData() {
        int start = between(0, 8);
        int length = between(0, 10 - start);
        return List.of(new BytesRef(randomAlphaOfLength(10)), start + 1, length);
    }

    @Override
    protected Expression expressionForSimpleData() {
        return new Substring(
            Source.EMPTY,
            field("str", DataTypes.KEYWORD),
            field("start", DataTypes.INTEGER),
            field("end", DataTypes.INTEGER)
        );
    }

    @Override
    protected DataType expressionForSimpleDataType() {
        return DataTypes.KEYWORD;
    }

    @Override
    protected Matcher<Object> resultMatcher(List<Object> data) {
        String str = ((BytesRef) data.get(0)).utf8ToString();
        int start = (Integer) data.get(1);
        int end = (Integer) data.get(2);
        return equalTo(new BytesRef(str.substring(start - 1, start + end - 1)));
    }

    @Override
    protected String expectedEvaluatorSimpleToString() {
        return "SubstringEvaluator[str=Keywords[channel=0], start=Ints[channel=1], length=Ints[channel=2]]";
    }

    @Override
    protected Expression constantFoldable(List<Object> data) {
        return new Substring(
            Source.EMPTY,
            new Literal(Source.EMPTY, data.get(0), DataTypes.KEYWORD),
            new Literal(Source.EMPTY, data.get(1), DataTypes.INTEGER),
            new Literal(Source.EMPTY, data.get(2), DataTypes.INTEGER)
        );
    }

    @Override
    protected void assertSimpleWithNulls(List<Object> data, Object value, int nullBlock) {
        if (nullBlock == 2) {
            String str = ((BytesRef) data.get(0)).utf8ToString();
            int start = (Integer) data.get(1);
            assertThat(value, equalTo(new BytesRef(str.substring(start - 1))));
        } else {
            assertThat(value, nullValue());
        }
    }

    @Override
    protected List<AbstractScalarFunctionTestCase.ArgumentSpec> argSpec() {
        return List.of(required(DataTypes.KEYWORD), required(integers()), optional(integers()));
    }

    @Override
    protected Expression build(Source source, List<Literal> args) {
        return new Substring(source, args.get(0), args.get(1), args.size() < 3 ? null : args.get(2));
    }

    public void testWholeString() {
        assertThat(process("a tiger", 0, null), equalTo("a tiger"));
        assertThat(process("a tiger", 1, null), equalTo("a tiger"));
    }

    public void testPositiveStartNoLength() {
        assertThat(process("a tiger", 3, null), equalTo("tiger"));
    }

    public void testNegativeStartNoLength() {
        assertThat(process("a tiger", -3, null), equalTo("ger"));
    }

    public void testPositiveStartMassiveLength() {
        assertThat(process("a tiger", 3, 1000), equalTo("tiger"));
    }

    public void testNegativeStartMassiveLength() {
        assertThat(process("a tiger", -3, 1000), equalTo("ger"));
    }

    public void testMassiveNegativeStartNoLength() {
        assertThat(process("a tiger", -300, null), equalTo("a tiger"));
    }

    public void testMassiveNegativeStartSmallLength() {
        assertThat(process("a tiger", -300, 1), equalTo("a"));
    }

    public void testPositiveStartReasonableLength() {
        assertThat(process("a tiger", 1, 3), equalTo("a t"));
    }

    public void testUnicode() {
        final String s = "a\ud83c\udf09tiger";
        assert s.length() == 8 && s.codePointCount(0, s.length()) == 7;
        assertThat(process(s, 3, 1000), equalTo("tiger"));
        assertThat(process(s, -6, 1000), equalTo("\ud83c\udf09tiger"));
    }

    public void testNegativeLength() {
        IllegalArgumentException ex = expectThrows(IllegalArgumentException.class, () -> process("a tiger", 1, -1));
        assertThat(ex.getMessage(), containsString("Length parameter cannot be negative, found [-1]"));
    }

    private String process(String str, int start, Integer length) {
        Object result = evaluator(
            new Substring(
                Source.EMPTY,
                field("str", DataTypes.KEYWORD),
                new Literal(Source.EMPTY, start, DataTypes.INTEGER),
                length == null ? null : new Literal(Source.EMPTY, length, DataTypes.INTEGER)
            )
        ).get().computeRow(row(List.of(new BytesRef(str))), 0);
        return result == null ? null : ((BytesRef) result).utf8ToString();
    }

}
