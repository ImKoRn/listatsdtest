package com.imkorn.listatsdtest.parser.elements;

import com.imkorn.listatsdtest.parser.Factory;
import com.imkorn.listatsdtest.parser.exceptions.ParseException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * Created by imkorn on 9/20/17.
 */
public class XmlGroup<XmlElementType extends XmlElement> extends XmlCompositeElement<List<XmlElementType>> {

    private final List<String> elements;

    public XmlGroup(String name,
                    String src) throws
                                ParseException {
        super(name,
              src);
        elements = splitByElements();
    }

    public List<XmlElementType> getValues(Factory<String, XmlElementType> factory) throws
                                                                                   ParseException {
        return super.getValue(new ListFactory(factory));
    }

    public XmlElementType getValue(int index, Factory<String, XmlElementType> factory) throws
                                                                                       ParseException {
        return factory.create(elements.get(index));
    }

    public int size() {
        return elements.size();
    }

    @Override
    protected boolean isEmptyContentAllowed() {
        return true;
    }

    private List<String> splitByElements() throws
                                           ParseException {
        final List<String> elements = new LinkedList<>();
        forElements(new Visitor() {
            private String name;

            @Override
            public void onNext(String name,
                               String element) throws
                                               ParseException {
                if (this.name != null &&
                    !this.name.equals(name)) {
                    String msg = String.format(Locale.US,
                                               "Inconsistent elements in group, " +
                                               "expected tags with name [%s] but found [%s]",
                                               this.name,
                                               name);
                    throw new ParseException(msg);
                }
                this.name = name;
                elements.add(element);
            }
        });
        return elements;
    }

    private class ListFactory implements Factory<String, List<XmlElementType>> {
        private final Factory<String, XmlElementType> factory;

        private ListFactory(Factory<String, XmlElementType> factory) {
            this.factory = factory;
        }

        @Override
        public List<XmlElementType> create(String data) throws
                                                        ParseException {
            if (elements.isEmpty()) {
                return Collections.emptyList();
            }
            List<XmlElementType> parsedElements = new ArrayList<>(elements.size());
            for (String element : elements) {
                parsedElements.add(factory.create(element));
            }
            return parsedElements;
        }
    }
}
