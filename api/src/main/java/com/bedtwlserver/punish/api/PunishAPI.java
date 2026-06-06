package com.bedtwlserver.punish.api;

import com.bedtwlserver.punish.api.event.ServerEventRegistry;
import lombok.Getter;
import lombok.Setter;

public final class PunishAPI {
    @Getter
    @Setter
    private static PunishActionRegistry punishActionRegistry;

    @Getter
    @Setter
    private static ServerEventRegistry serverEventRegistry;
}

