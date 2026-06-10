package com.sample.system.ssm.service.domain.model;

import java.util.UUID;

public final class Ids {
  private Ids() {}

  public static UUID newId() {
    return UUID.randomUUID();
  }
}
