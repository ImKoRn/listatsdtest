package com.imkorn.listatsdtest.parser;

import com.imkorn.listatsdtest.parser.exceptions.ParseException;

/**
 * Created by imkorn on 9/20/17.
 */

public interface Factory<Data, Type> {
    Type create(Data data) throws
                           ParseException;
}
