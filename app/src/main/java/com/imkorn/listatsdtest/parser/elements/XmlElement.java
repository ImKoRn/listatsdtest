package com.imkorn.listatsdtest.parser.elements;

import com.imkorn.listatsdtest.parser.Factory;
import com.imkorn.listatsdtest.parser.InternalUtils;
import com.imkorn.listatsdtest.parser.exceptions.ParseException;

import java.util.Locale;

/**
 * Created by imkorn on 9/20/17.
 */

public class XmlElement<Type> {
    private final String name;
    private final String content;


    public XmlElement(String name, String src) throws
                                               ParseException {
        InternalUtils.assertTagName(name);

        this.name = name;
        this.content = unwrap(name, src, isEmptyContentAllowed());
    }

    protected String getName() {
        return name;
    }

    public Type getValue(Factory<String, Type> factory) throws
                                                        ParseException {
        return factory.create(content);
    }

    protected String getContent() {
        return content;
    }

    protected final String unwrap(String name, String src, boolean isEmptyContentAllowed) throws
                                                                                          ParseException {
        final String startTag = '<' + name + '>';

        final int start = src.indexOf(startTag);
        if (start == -1) {
            throw new ParseException(String.format("Tag %s not found in [%s]",
                                                   startTag,
                                                   src));
        }

        if (!src.substring(0, start)
                .trim()
                .isEmpty()) {
            throwFormat(start, src);
        }

        final String endTag = "</" + name + '>';
        final int end = src.indexOf(endTag);
        if (end == -1) {
            throw new ParseException(String.format("Tag %s not found in [%s]",
                                                   endTag,
                                                   src));
        }

        final int endOfEndTag = end + endTag.length();
        if (!src.substring(endOfEndTag, src.length())
                .trim()
                .isEmpty()) {
            throwFormat(endOfEndTag, src);
        }

        final int startContent = start + startTag.length();

        final String content = src.substring(startContent,
                                               end);

        if (!isEmptyContentAllowed && (startContent == end || content.trim().isEmpty())) {
            throw new ParseException("Empty content not allowed in " + startTag);
        }

        return content;
    }

    protected boolean isEmptyContentAllowed() {
        return false;
    }

    protected void throwFormat(int index,
                             String content) throws
                                             ParseException {
        final String msg = String.format(
                Locale.US,
                "Wrong format of XML document, exception index [%d] in [%s]",
                index,
                content);
        throw new ParseException(msg);
    }
}
