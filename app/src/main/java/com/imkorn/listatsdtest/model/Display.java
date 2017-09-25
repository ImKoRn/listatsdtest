package com.imkorn.listatsdtest.model;

import android.support.annotation.NonNull;

import com.imkorn.listatsdtest.model.entities.PrimeNumber;

import java.util.Collection;

/**
 * Created by imkorn on 24.09.17.
 */
public interface Display {
    void display(@NonNull Collection<PrimeNumber> primeNumbers);
    void displayError(@NonNull Throwable throwable);
}
