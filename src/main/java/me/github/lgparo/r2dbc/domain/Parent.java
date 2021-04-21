package me.github.lgparo.r2dbc.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.Singular;
import lombok.Value;

import java.util.Collection;

@Value
@Builder(toBuilder = true)
public class Parent {

    @NonNull
    String id;

    @NonNull
    String name;

    int age;

    @Singular(ignoreNullCollections = true)
    Collection<Child> children;
}