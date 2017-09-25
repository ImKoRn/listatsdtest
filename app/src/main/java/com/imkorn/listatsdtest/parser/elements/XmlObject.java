package com.imkorn.listatsdtest.parser.elements;

import com.imkorn.listatsdtest.parser.Factory;
import com.imkorn.listatsdtest.parser.exceptions.ParseException;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;

import static com.imkorn.listatsdtest.parser.elements.DefaultFactories.INT_FACTORY;

/**
 * Created by imkorn on 9/20/17.
 */

public class XmlObject extends XmlCompositeElement {

    private final Map<String, String> elements;

    public XmlObject(String name,
                     String src) throws
                                 ParseException {
        super(name,
              src);
        elements = mapElements();
    }

    public <XmlElementType> XmlElement<XmlElementType>
    getValue(final String name,
             Factory<String, XmlElement<XmlElementType>> factory) throws
                                                                  ParseException {
        final String content = elements.get(name);
        if (content == null) {
            throw new ParseException("No such element");
        }
        return factory.create(content);
    }

    public int getValueAsInt(final String name) throws
                                                ParseException {
        final String content = elements.get(name);
        if (content == null) {
            throw new ParseException("No such element");
        }
        return INT_FACTORY.create(unwrap(name, content, false));
    }

    public String getValueAsString(final String name) throws
                                                      ParseException {
        final String content = elements.get(name);
        if (content == null) {
            throw new ParseException("No such element");
        }
        return unwrap(name, content, true);
    }

    @Override
    protected boolean isEmptyContentAllowed() {
        return true;
    }

    private Map<String, String> mapElements() throws
                                              ParseException {
        final Map<String, String> elements = new HashMap<>();
        forElements(new Visitor() {
            @Override
            public void onNext(String name,
                               String element) throws
                                               ParseException {
                if (elements.containsKey(name)) {
                    throw new ParseException(String.format(Locale.US,
                                                           "Tag duplication [%s]",
                                                           name));
                }

                elements.put(name, element);
            }
        });
        return elements;
    }
}
