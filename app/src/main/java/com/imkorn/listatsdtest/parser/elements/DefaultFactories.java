package com.imkorn.listatsdtest.parser.elements;

import com.imkorn.listatsdtest.parser.Factory;
import com.imkorn.listatsdtest.parser.exceptions.ParseException;

/**
 * Created by imkorn on 22.09.17.
 */

class DefaultFactories {
    public static final Factory<String, Integer> INT_FACTORY = new Factory<String, Integer>() {
        @Override
        public Integer create(String s) throws
                                        ParseException {
            try {
                return Integer.parseInt(s.trim());
            } catch (NumberFormatException nfe) {
                throw new ParseException(nfe);
            }
        }
    };
}
