package com.ispw.bootstrap;


import com.ispw.model.enums.AppMode;
import com.ispw.model.enums.FrontendProvider;
import com.ispw.model.enums.PersistencyProvider;

public record AppConfig(
        FrontendProvider frontend,
        AppMode mode,
        PersistencyProvider persistency
) {}

