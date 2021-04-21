package me.github.lgparo.r2dbc.domain;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Child {

    @NonNull
    String id;

    @NonNull
    String name;

    int age;

    @NonNull
    Parent parent;
}
