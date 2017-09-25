package com.imkorn.listatsdtest.parser.elements;

import com.imkorn.listatsdtest.parser.InternalUtils;
import com.imkorn.listatsdtest.parser.exceptions.ParseException;

import java.util.Locale;

/**
 * Created by imkorn on 22.09.17.
 */

public abstract class XmlCompositeElement<Type> extends XmlElement<Type> {

    protected XmlCompositeElement(String name,
                                  String src) throws
                                             ParseException {
        super(name,
              src);
    }

    protected void forElements(Visitor visitor) throws
                                                ParseException {
        final String content = getContent();

        int startElement = -1;
        boolean paired = true;
        boolean enclosing = false;
        String tag = "";
        int deep = 0;
        for (int index = 0; index < content.length(); index++) {
            final char character = content.charAt(index);

            if (character == '<') {
                if (!paired) {
                    throwFormat(index, content);
                }

                final int enclosingChar = index + 1;

                enclosing = enclosingChar < content.length() &&
                            content.charAt(enclosingChar) == '/';

                if (enclosing) {
                    if (deep == 0) {
                        throwFormat(index,
                                    content);
                    }

                    continue;
                }

                if (startElement == -1) {
                    startElement = index;
                }

                deep++;
                paired = false;

                continue;
            }

            if (character == '>') {
                if (deep <= 0) {
                    throwFormat(index, content);
                }

                paired = true;

                if (enclosing) {
                    deep--;
                    if (deep == 0) {
                        final int enclosedTagStart = index - tag.length();
                        final String enclosedTag = content.substring(enclosedTagStart,
                                                                     index);

                        if (!tag.equals(enclosedTag) ||
                            content.charAt(enclosedTagStart - 1) != '/') {
                            throw new ParseException(String.format(Locale.US,
                                                                   "Wrong enclosed tag in [%d]",
                                                                   enclosedTagStart));
                        }
                        visitor.onNext(tag, content.substring(startElement, index + 1));
                        startElement = -1;
                        enclosing = false;
                        tag = "";
                    }
                    continue;
                }

                if (deep == 1) {
                    tag = content.substring(startElement + 1, index);
                    InternalUtils.assertTagName(tag);
                }
                continue;
            }

            if (deep == 0 && !(character == ' ' ||
                               character == '\n' ||
                               character == '\t')) {
                throwFormat(index, content);
            }
        }

        if (deep != 0) {
            final String msg = String.format(
                    Locale.US,
                    "Wrong format of XML document, exception in [%s]",
                    content);
            throw new ParseException(msg);
        }
    }

    protected interface Visitor {
        void onNext(String name, String element) throws
                                                 ParseException;
    }
}
