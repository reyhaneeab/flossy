package com.group11.dto;
import com.mongodb.lang.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record DentistResponse(
        @NotNull
        String id,
        @NotNull
        String officeName,
        @NotNull
        String email,
        @NotNull
        String password,
        @NotNull
        String address,
        @NotNull
        String phone,
        @Nullable
        List<String> doctors) {}