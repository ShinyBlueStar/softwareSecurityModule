package com.sample.system.ssm.service.domain.enums;

import java.util.List;

public enum SessionStatus {
  ACTIVE, EXPIRED, INVALIDATED;

  private static final List<SessionStatus> TERMINAL_STATUSES =
          List.of(EXPIRED, INVALIDATED);

  public static List<SessionStatus> terminalStatuses() {
    return TERMINAL_STATUSES;
  }

  public boolean isTerminal() {
    return TERMINAL_STATUSES.contains(this);
  }
}
